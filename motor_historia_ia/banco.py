"""
Banco de dados (SQLite) do LivennIA: jogadores, convites, jogos, uso, feedbacks e denúncias.
"""
import json
import logging
import os
import sqlite3
from contextlib import contextmanager
from datetime import datetime, timezone

import config
from seguranca import gerar_codigo_convite

ESQUEMA = """
CREATE TABLE IF NOT EXISTS convites (
    codigo      TEXT PRIMARY KEY,
    usos_max    INTEGER NOT NULL DEFAULT 1,
    usos        INTEGER NOT NULL DEFAULT 0,
    ativo       INTEGER NOT NULL DEFAULT 1,
    nota        TEXT,
    criado_em   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS jogadores (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    apelido     TEXT NOT NULL,
    token_hash  TEXT NOT NULL UNIQUE,
    convite     TEXT,
    banido      INTEGER NOT NULL DEFAULT 0,
    -- 1 = conta premium: libera os "cérebros" de IA mais caros. A cobrança em si
    -- ainda não existe; por ora é uma marcação feita pelo administrador.
    premium     INTEGER NOT NULL DEFAULT 0,
    mochis      INTEGER NOT NULL DEFAULT 100,
    ultimo_resgate_mochi TEXT,
    criado_em   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS jogos (
    id             TEXT PRIMARY KEY,
    jogador_id     INTEGER NOT NULL REFERENCES jogadores(id) ON DELETE CASCADE,
    estado         TEXT NOT NULL,
    encerrado      INTEGER NOT NULL DEFAULT 0,
    criado_em      TEXT NOT NULL,
    atualizado_em  TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_jogos_jogador ON jogos(jogador_id);

-- Cada chamada à IA (com sucesso ou não). Sem dados pessoais: se o jogador
-- apagar a conta, o jogador_id vira NULL e o histórico de custo continua.
CREATE TABLE IF NOT EXISTS uso (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    jogador_id      INTEGER REFERENCES jogadores(id) ON DELETE SET NULL,
    dia             TEXT NOT NULL,
    criado_em       TEXT NOT NULL,
    sucesso         INTEGER NOT NULL,
    modelo          TEXT,
    tokens_entrada  INTEGER NOT NULL DEFAULT 0,
    tokens_saida    INTEGER NOT NULL DEFAULT 0,
    segundos        REAL,
    tentativas      INTEGER
);
CREATE INDEX IF NOT EXISTS idx_uso_dia ON uso(dia);
CREATE INDEX IF NOT EXISTS idx_uso_jogador_dia ON uso(jogador_id, dia);

CREATE TABLE IF NOT EXISTS personas (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    jogador_id      INTEGER NOT NULL REFERENCES jogadores(id) ON DELETE CASCADE,
    nome            TEXT NOT NULL,
    genero          TEXT NOT NULL DEFAULT 'Feminino',
    idade           TEXT,
    personalidade   TEXT,
    interesses      TEXT,
    aparencia       TEXT,
    medos           TEXT,
    detalhes_imagem TEXT,
    estilo_imagem   TEXT DEFAULT 'semi_realistic',
    avatar_emoji    TEXT DEFAULT '👤',
    retrato_versao  INTEGER NOT NULL DEFAULT 0,
    padrao          INTEGER NOT NULL DEFAULT 0,
    criado_em       TEXT NOT NULL,
    atualizado_em   TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_personas_jogador ON personas(jogador_id);

CREATE TABLE IF NOT EXISTS feedbacks (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    jogador_id  INTEGER REFERENCES jogadores(id) ON DELETE CASCADE,
    nota        INTEGER NOT NULL,
    comentario  TEXT,
    criado_em   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS cenarios_usuario (
    id              TEXT PRIMARY KEY,
    autor_id        INTEGER NOT NULL REFERENCES jogadores(id) ON DELETE CASCADE,
    autor_nome      TEXT NOT NULL,
    nome            TEXT NOT NULL,
    subtitulo       TEXT,
    emoji           TEXT DEFAULT '🎭',
    genero          TEXT NOT NULL DEFAULT 'Aventura',
    tags            TEXT,
    sinopse         TEXT,
    abertura        TEXT,
    prompt_secreto  TEXT,
    estilo_imagem   TEXT DEFAULT 'semi_realistic',
    chats           INTEGER NOT NULL DEFAULT 0,
    saves           INTEGER NOT NULL DEFAULT 0,
    modo_jogo       TEXT NOT NULL DEFAULT 'historia',
    criado_em       TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_cenarios_usuario_criado ON cenarios_usuario(criado_em DESC);

CREATE TABLE IF NOT EXISTS denuncias (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    jogador_id  INTEGER REFERENCES jogadores(id) ON DELETE CASCADE,
    jogo_id     TEXT,
    motivo      TEXT NOT NULL,
    trecho      TEXT,
    criado_em   TEXT NOT NULL,
    revisada    INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS comentarios_cenario (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    cenario_id      TEXT NOT NULL,
    jogador_id      INTEGER NOT NULL REFERENCES jogadores(id) ON DELETE CASCADE,
    autor_nome      TEXT NOT NULL,
    autor_avatar    TEXT DEFAULT '👤',
    texto           TEXT NOT NULL,
    likes           INTEGER NOT NULL DEFAULT 0,
    criado_em       TEXT NOT NULL,
    editado_em      TEXT
);
CREATE INDEX IF NOT EXISTS idx_comentarios_cenario ON comentarios_cenario(cenario_id, criado_em DESC);

CREATE TABLE IF NOT EXISTS likes_comentario (
    comentario_id   INTEGER NOT NULL REFERENCES comentarios_cenario(id) ON DELETE CASCADE,
    jogador_id      INTEGER NOT NULL REFERENCES jogadores(id) ON DELETE CASCADE,
    criado_em       TEXT NOT NULL,
    PRIMARY KEY (comentario_id, jogador_id)
);

CREATE TABLE IF NOT EXISTS seguidores (
    seguidor_id     INTEGER NOT NULL REFERENCES jogadores(id) ON DELETE CASCADE,
    criador_nome    TEXT NOT NULL,
    criado_em       TEXT NOT NULL,
    PRIMARY KEY (seguidor_id, criador_nome)
);

CREATE TABLE IF NOT EXISTS bloqueios_usuario (
    jogador_id      INTEGER NOT NULL REFERENCES jogadores(id) ON DELETE CASCADE,
    alvo_nome       TEXT NOT NULL,
    criado_em       TEXT NOT NULL,
    PRIMARY KEY (jogador_id, alvo_nome)
);
"""


class ErroNegocio(Exception):
    """Erro 'esperado' (convite inválido, beta lotado...) com o código HTTP a devolver."""

    def __init__(self, status, mensagem):
        super().__init__(mensagem)
        self.status = status
        self.mensagem = mensagem


def agora():
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def hoje():
    """O 'dia' do jogo (horário de Brasília), no formato AAAA-MM-DD."""
    return datetime.now(config.FUSO).strftime("%Y-%m-%d")


# ---------------------------------------------------------------
# Conexões
# ---------------------------------------------------------------
@contextmanager
def conexao():
    """Conexão curta, em modo automático (boa para leituras)."""
    c = sqlite3.connect(config.ARQUIVO_BANCO, timeout=10, isolation_level=None)
    c.row_factory = sqlite3.Row
    c.execute("PRAGMA foreign_keys = ON")
    try:
        yield c
    finally:
        c.close()


@contextmanager
def transacao():
    """Conexão com transação: ou grava tudo, ou não grava nada."""
    with conexao() as c:
        c.execute("BEGIN IMMEDIATE")
        try:
            yield c
        except BaseException:
            c.execute("ROLLBACK")
            raise
        else:
            c.execute("COMMIT")


def iniciar_banco():
    pasta = os.path.dirname(config.ARQUIVO_BANCO)
    if pasta:
        os.makedirs(pasta, exist_ok=True)
    with conexao() as c:
        c.execute("PRAGMA journal_mode = WAL")
        c.executescript(ESQUEMA)
        _migrar_colunas(c)


def _migrar_colunas(c):
    """Acrescenta colunas novas em bancos que já existiam (CREATE TABLE não mexe neles)."""
    colunas = {linha["name"] for linha in c.execute("PRAGMA table_info(jogadores)")}
    if "premium" not in colunas:
        c.execute("ALTER TABLE jogadores ADD COLUMN premium INTEGER NOT NULL DEFAULT 0")
        logging.info("Banco atualizado: coluna jogadores.premium criada.")
    if "mochis" not in colunas:
        c.execute("ALTER TABLE jogadores ADD COLUMN mochis INTEGER NOT NULL DEFAULT 100")
        logging.info("Banco atualizado: coluna jogadores.mochis criada.")
    if "ultimo_resgate_mochi" not in colunas:
        c.execute("ALTER TABLE jogadores ADD COLUMN ultimo_resgate_mochi TEXT")
        logging.info("Banco atualizado: coluna jogadores.ultimo_resgate_mochi criada.")
    if "dono" not in colunas:
        c.execute("ALTER TABLE jogadores ADD COLUMN dono INTEGER NOT NULL DEFAULT 0")
        logging.info("Banco atualizado: coluna jogadores.dono criada.")
    if "email" not in colunas:
        c.execute("ALTER TABLE jogadores ADD COLUMN email TEXT")
        logging.info("Banco atualizado: coluna jogadores.email criada.")

    colunas_personas = {linha["name"] for linha in c.execute("PRAGMA table_info(personas)")}
    if "tags" not in colunas_personas:
        c.execute("ALTER TABLE personas ADD COLUMN tags TEXT DEFAULT ''")
        logging.info("Banco atualizado: coluna personas.tags criada.")


# ---------------------------------------------------------------
# Jogadores e convites
# ---------------------------------------------------------------
def registrar_jogador(codigo, apelido, token_hash):
    """Usa um convite para entrar. Devolve (id_do_jogador, recuperou_conta).

    Se já existir uma conta com o mesmo apelido, o convite serve para RECUPERAR
    essa conta: o token é renovado e as aventuras antigas voltam a aparecer.
    Assim quem perde a sessão no aparelho não perde o progresso. Caso contrário,
    cria uma conta nova.

    O convite é consumido nos dois casos (ele é a credencial de entrada).
    """
    with transacao() as c:
        convite = c.execute(
            "SELECT usos, usos_max, ativo, nota FROM convites WHERE codigo = ?", (codigo,)
        ).fetchone()
        if convite is None or not convite["ativo"] or convite["usos"] >= convite["usos_max"]:
            # Mesma mensagem para qualquer problema, para não ajudar quem está chutando códigos.
            raise ErroNegocio(403, "Código de convite inválido ou já utilizado.")

        eh_admin = convite["nota"] == "dono" or codigo in {"LIV-DONO-2026", "LIVENNIA2026", "LIV-ALISSON-2026", "LIV-CAMILA-ADMIN"}

        # A conta mais recente com esse apelido (é a que o jogador estava usando).
        existente = c.execute(
            "SELECT id, banido FROM jogadores WHERE apelido = ? COLLATE NOCASE "
            "ORDER BY id DESC LIMIT 1",
            (apelido,),
        ).fetchone()
        if existente is not None and existente["banido"]:
            raise ErroNegocio(403, "Conta suspensa.")

        c.execute("UPDATE convites SET usos = usos + 1 WHERE codigo = ?", (codigo,))

        if existente is not None:
            # Recuperação: renova o token e mantém tudo que já era da conta.
            if eh_admin:
                c.execute(
                    "UPDATE jogadores SET token_hash = ?, dono = 1, premium = 1, mochis = 999999 WHERE id = ?",
                    (token_hash, existente["id"]),
                )
            else:
                c.execute(
                    "UPDATE jogadores SET token_hash = ? WHERE id = ?",
                    (token_hash, existente["id"]),
                )
            return existente["id"], True

        total = c.execute("SELECT COUNT(*) FROM jogadores").fetchone()[0]
        if total >= config.MAX_JOGADORES:
            raise ErroNegocio(503, "O beta está lotado no momento.")

        cursor = c.execute(
            "INSERT INTO jogadores (apelido, token_hash, convite, dono, premium, mochis, criado_em) VALUES (?, ?, ?, ?, ?, ?, ?)",
            (apelido, token_hash, codigo, 1 if eh_admin else 0, 1 if eh_admin else 0, 999999 if eh_admin else 100, agora()),
        )
        return cursor.lastrowid, False


def jogador_por_token_hash(token_hash):
    with conexao() as c:
        linha = c.execute(
            "SELECT id, apelido, banido, premium, mochis, ultimo_resgate_mochi, dono, email "
            "FROM jogadores WHERE token_hash = ?",
            (token_hash,),
        ).fetchone()
    if not linha:
        return None
    d = dict(linha)
    eh_admin = bool(
        d.get("dono")
        or (d.get("apelido") or "").lower() in {"alisson", "camila", "mila"}
        or (d.get("email") or "").lower() in {"334camila@gmail.com"}
        or d.get("apelido") in config.ADMINS
        or d.get("email") in config.ADMINS
    )
    if eh_admin:
        d["dono"] = 1
        d["premium"] = 1
        d["mochis"] = 999999
    d["livens"] = d.get("mochis", 0)
    return d


def resgatar_livens_diarios(jogador_id, bonus=30):
    """Concede o bônus diário de Livens (1x por dia no fuso do servidor). Devolve o novo saldo."""
    dia_atual = hoje()
    with transacao() as c:
        linha = c.execute(
            "SELECT mochis, ultimo_resgate_mochi FROM jogadores WHERE id = ?", (jogador_id,)
        ).fetchone()
        if linha is None:
            raise ErroNegocio(404, "Jogador não encontrado.")
        if linha["ultimo_resgate_mochi"] == dia_atual:
            raise ErroNegocio(409, "Você já resgatou seus Livens diários hoje. Volte amanhã!")
        novo_saldo = (linha["mochis"] or 0) + int(bonus)
        c.execute(
            "UPDATE jogadores SET mochis = ?, ultimo_resgate_mochi = ? WHERE id = ?",
            (novo_saldo, dia_atual, jogador_id),
        )
        return novo_saldo

resgatar_mochi_diario = resgatar_livens_diarios


def debitar_livens(jogador_id, custo):
    """Debita `custo` Livens da carteira do jogador. Levanta ErroNegocio(402) se faltar saldo."""
    custo = int(custo or 0)
    if custo <= 0:
        return
    with transacao() as c:
        linha = c.execute("SELECT mochis FROM jogadores WHERE id = ?", (jogador_id,)).fetchone()
        saldo = (linha["mochis"] or 0) if linha else 0
        if saldo < custo:
            raise ErroNegocio(
                402,
                f"Saldo insuficiente de Livens ({saldo} disponíveis, {custo} necessários). "
                "Resgate seu bônus diário ou escolha um motor gratuito!",
            )
        c.execute("UPDATE jogadores SET mochis = mochis - ? WHERE id = ?", (custo, jogador_id))

debitar_mochis = debitar_livens


def adicionar_livens(jogador_id, quantidade):
    with transacao() as c:
        c.execute(
            "UPDATE jogadores SET mochis = COALESCE(mochis, 0) + ? WHERE id = ?",
            (int(quantidade), jogador_id),
        )

adicionar_mochis = adicionar_livens


def definir_premium(jogador_id, premium):
    """Liga/desliga a marcação de conta premium (usada pelos 'cérebros' de IA)."""
    with transacao() as c:
        return c.execute(
            "UPDATE jogadores SET premium = ? WHERE id = ?", (int(premium), jogador_id)
        ).rowcount > 0


def apagar_jogador(jogador_id):
    """Apaga a conta e tudo ligado a ela (jogos, feedbacks, denúncias)."""
    with transacao() as c:
        c.execute("DELETE FROM jogadores WHERE id = ?", (jogador_id,))


# ---------------------------------------------------------------
# Jogos
# ---------------------------------------------------------------
def contar_jogos_ativos(jogador_id):
    with conexao() as c:
        return c.execute(
            "SELECT COUNT(*) FROM jogos WHERE jogador_id = ? AND encerrado = 0", (jogador_id,)
        ).fetchone()[0]


def criar_jogo(jogo_id, jogador_id, estado_texto, encerrado):
    momento = agora()
    with transacao() as c:
        c.execute(
            "INSERT INTO jogos (id, jogador_id, estado, encerrado, criado_em, atualizado_em) "
            "VALUES (?, ?, ?, ?, ?, ?)",
            (jogo_id, jogador_id, estado_texto, int(encerrado), momento, momento),
        )


def atualizar_jogo(jogo_id, jogador_id, estado_texto, encerrado):
    with transacao() as c:
        c.execute(
            "UPDATE jogos SET estado = ?, encerrado = ?, atualizado_em = ? "
            "WHERE id = ? AND jogador_id = ?",
            (estado_texto, int(encerrado), agora(), jogo_id, jogador_id),
        )


def obter_jogo(jogo_id, jogador_id):
    """Só devolve o jogo se ele for DESTE jogador (um jogador nunca vê o jogo do outro)."""
    with conexao() as c:
        linha = c.execute(
            "SELECT id, estado, encerrado, atualizado_em FROM jogos WHERE id = ? AND jogador_id = ?",
            (jogo_id, jogador_id),
        ).fetchone()
    if not linha:
        return None
    d = dict(linha)
    d["livens"] = d.get("mochis", 0)
    return d


def listar_jogos(jogador_id):
    with conexao() as c:
        linhas = c.execute(
            "SELECT id, estado, encerrado, atualizado_em FROM jogos "
            "WHERE jogador_id = ? ORDER BY atualizado_em DESC",
            (jogador_id,),
        ).fetchall()
    return [dict(l) for l in linhas]


def apagar_jogo(jogo_id, jogador_id):
    with transacao() as c:
        cursor = c.execute(
            "DELETE FROM jogos WHERE id = ? AND jogador_id = ?", (jogo_id, jogador_id)
        )
        return cursor.rowcount > 0


# ---------------------------------------------------------------
# Uso e limites
# ---------------------------------------------------------------
def registrar_uso(jogador_id, sucesso, modelo, tokens_entrada, tokens_saida, segundos, tentativas):
    with transacao() as c:
        c.execute(
            "INSERT INTO uso (jogador_id, dia, criado_em, sucesso, modelo, tokens_entrada, "
            "tokens_saida, segundos, tentativas) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (jogador_id, hoje(), agora(), int(sucesso), modelo, tokens_entrada,
             tokens_saida, segundos, tentativas),
        )


def contar_jogadas(jogador_id, dia):
    """Jogadas que deram certo hoje (falhas da IA não gastam a cota do jogador)."""
    with conexao() as c:
        return c.execute(
            "SELECT COALESCE(SUM(sucesso), 0) FROM uso WHERE jogador_id = ? AND dia = ?",
            (jogador_id, dia),
        ).fetchone()[0]


def totais_do_dia(dia):
    """(jogadas com sucesso, tokens gastos) de todos os jogadores no dia."""
    with conexao() as c:
        linha = c.execute(
            "SELECT COALESCE(SUM(sucesso), 0), COALESCE(SUM(tokens_entrada + tokens_saida), 0) "
            "FROM uso WHERE dia = ?",
            (dia,),
        ).fetchone()
    return linha[0], linha[1]


# ---------------------------------------------------------------
# Feedback e denúncias
# ---------------------------------------------------------------
def salvar_feedback(jogador_id, nota, comentario):
    with transacao() as c:
        c.execute(
            "INSERT INTO feedbacks (jogador_id, nota, comentario, criado_em) VALUES (?, ?, ?, ?)",
            (jogador_id, nota, comentario, agora()),
        )


def salvar_denuncia(jogador_id, jogo_id, motivo, trecho):
    with transacao() as c:
        c.execute(
            "INSERT INTO denuncias (jogador_id, jogo_id, motivo, trecho, criado_em) "
            "VALUES (?, ?, ?, ?, ?)",
            (jogador_id, jogo_id, motivo, trecho, agora()),
        )


# ---------------------------------------------------------------
# Administração (usado pelo admin.py)
# ---------------------------------------------------------------
def criar_convites(quantidade, usos_max, nota):
    codigos = []
    with transacao() as c:
        while len(codigos) < quantidade:
            codigo = gerar_codigo_convite()
            if codigo in codigos:
                continue
            existe = c.execute("SELECT 1 FROM convites WHERE codigo = ?", (codigo,)).fetchone()
            if existe:
                continue
            c.execute(
                "INSERT INTO convites (codigo, usos_max, nota, criado_em) VALUES (?, ?, ?, ?)",
                (codigo, usos_max, nota, agora()),
            )
            codigos.append(codigo)
    return codigos


def listar_convites():
    with conexao() as c:
        return [dict(l) for l in c.execute(
            "SELECT codigo, usos, usos_max, ativo, nota, criado_em FROM convites ORDER BY criado_em DESC"
        ).fetchall()]


def desativar_convite(codigo):
    with transacao() as c:
        return c.execute("UPDATE convites SET ativo = 0 WHERE codigo = ?", (codigo,)).rowcount > 0


def listar_jogadores():
    with conexao() as c:
        return [dict(l) for l in c.execute(
            "SELECT j.id, j.apelido, j.banido, j.premium, j.criado_em, "
            "(SELECT COUNT(*) FROM jogos g WHERE g.jogador_id = j.id) AS jogos, "
            "(SELECT COALESCE(SUM(u.sucesso), 0) FROM uso u WHERE u.jogador_id = j.id) AS jogadas "
            "FROM jogadores j ORDER BY j.id"
        ).fetchall()]


def definir_banido(jogador_id, banido):
    with transacao() as c:
        return c.execute(
            "UPDATE jogadores SET banido = ? WHERE id = ?", (int(banido), jogador_id)
        ).rowcount > 0


def estatisticas_do_dia(dia):
    with conexao() as c:
        geral = c.execute(
            "SELECT COUNT(*) AS chamadas, COALESCE(SUM(sucesso), 0) AS sucessos, "
            "COALESCE(SUM(tokens_entrada), 0) AS tokens_entrada, "
            "COALESCE(SUM(tokens_saida), 0) AS tokens_saida, "
            "AVG(CASE WHEN sucesso = 1 THEN segundos END) AS segundos_medio, "
            "COUNT(DISTINCT jogador_id) AS jogadores_ativos "
            "FROM uso WHERE dia = ?",
            (dia,),
        ).fetchone()
        por_modelo = c.execute(
            "SELECT modelo, COUNT(*) AS chamadas, SUM(tokens_entrada) AS tokens_entrada, "
            "SUM(tokens_saida) AS tokens_saida FROM uso WHERE dia = ? AND modelo IS NOT NULL "
            "GROUP BY modelo",
            (dia,),
        ).fetchall()
        feedback = c.execute("SELECT COUNT(*), AVG(nota) FROM feedbacks").fetchone()
        pendentes = c.execute("SELECT COUNT(*) FROM denuncias WHERE revisada = 0").fetchone()[0]
        total_jogadores = c.execute("SELECT COUNT(*) FROM jogadores").fetchone()[0]
    return {
        "geral": dict(geral),
        "por_modelo": [dict(l) for l in por_modelo],
        "feedbacks": feedback[0],
        "nota_media": feedback[1],
        "denuncias_pendentes": pendentes,
        "total_jogadores": total_jogadores,
    }


def listar_denuncias(somente_pendentes):
    consulta = "SELECT id, jogador_id, jogo_id, motivo, trecho, criado_em, revisada FROM denuncias"
    if somente_pendentes:
        consulta += " WHERE revisada = 0"
    consulta += " ORDER BY id DESC"
    with conexao() as c:
        return [dict(l) for l in c.execute(consulta).fetchall()]


def marcar_denuncia_revisada(denuncia_id):
    with transacao() as c:
        return c.execute(
            "UPDATE denuncias SET revisada = 1 WHERE id = ?", (denuncia_id,)
        ).rowcount > 0


# ---------------------------------------------------------------
# Gestão de Múltiplas Personas
# ---------------------------------------------------------------
def _deserializar_persona(linha):
    if not linha:
        return None
    d = dict(linha)
    tags_raw = d.get("tags") or ""
    if isinstance(tags_raw, str) and tags_raw.startswith("["):
        try:
            d["tags"] = json.loads(tags_raw)
        except Exception:
            d["tags"] = []
    elif isinstance(tags_raw, str) and tags_raw.strip():
        d["tags"] = [t.strip() for t in tags_raw.split(",") if t.strip()]
    elif isinstance(tags_raw, list):
        d["tags"] = tags_raw
    else:
        d["tags"] = []
    return d


def listar_personas(jogador_id):
    with conexao() as c:
        return [_deserializar_persona(l) for l in c.execute(
            "SELECT * FROM personas WHERE jogador_id = ? ORDER BY padrao DESC, id ASC",
            (jogador_id,)
        ).fetchall()]


def obter_persona(jogador_id, persona_id):
    with conexao() as c:
        linha = c.execute(
            "SELECT * FROM personas WHERE jogador_id = ? AND id = ?",
            (jogador_id, persona_id)
        ).fetchone()
        return _deserializar_persona(linha) if linha else None


def obter_persona_padrao(jogador_id):
    with conexao() as c:
        linha = c.execute(
            "SELECT * FROM personas WHERE jogador_id = ? ORDER BY padrao DESC, id ASC LIMIT 1",
            (jogador_id,)
        ).fetchone()
        return _deserializar_persona(linha) if linha else None


def salvar_persona(jogador_id, dados, persona_id=None):
    with transacao() as c:
        jogador = c.execute("SELECT dono, premium FROM jogadores WHERE id = ?", (jogador_id,)).fetchone()
        eh_vip = bool(jogador and (jogador["dono"] or jogador["premium"]))
        total = c.execute("SELECT COUNT(*) FROM personas WHERE jogador_id = ?", (jogador_id,)).fetchone()[0]

        if persona_id is None:
            if not eh_vip and total >= 3:
                raise ErroNegocio(403, "Limite de 3 personas atingido no plano gratuito. Seja Premium para criar mais personas!")
            if total >= 20:
                raise ErroNegocio(403, "Limite máximo de 20 personas atingido.")

        definir_como_padrao = bool(dados.get("padrao")) or (total == 0)

        if definir_como_padrao:
            c.execute("UPDATE personas SET padrao = 0 WHERE jogador_id = ?", (jogador_id,))

        ag = agora()
        tags_dado = dados.get("tags") or []
        tags_str = json.dumps(tags_dado, ensure_ascii=False) if isinstance(tags_dado, list) else str(tags_dado)

        if persona_id is None:
            cursor = c.execute(
                """
                INSERT INTO personas (
                    jogador_id, nome, genero, idade, personalidade, interesses,
                    aparencia, medos, tags, detalhes_imagem, estilo_imagem, avatar_emoji,
                    retrato_versao, padrao, criado_em, atualizado_em
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)
                """,
                (
                    jogador_id,
                    (dados.get("nome") or "Viajante").strip() or "Viajante",
                    dados.get("genero") or "Feminino",
                    (dados.get("idade") or "").strip(),
                    (dados.get("personalidade") or dados.get("descricao") or "").strip(),
                    (dados.get("interesses") or "").strip(),
                    (dados.get("aparencia") or "").strip(),
                    (dados.get("medos") or dados.get("aversoes") or "").strip(),
                    tags_str,
                    (dados.get("detalhes_imagem") or "").strip(),
                    dados.get("estilo_imagem") or "semi_realistic",
                    dados.get("avatar_emoji") or "👤",
                    1 if definir_como_padrao else 0,
                    ag, ag
                )
            )
            return cursor.lastrowid
        else:
            c.execute(
                """
                UPDATE personas SET
                    nome = ?, genero = ?, idade = ?, personalidade = ?, interesses = ?,
                    aparencia = ?, medos = ?, tags = ?, detalhes_imagem = ?, estilo_imagem = ?, avatar_emoji = ?,
                    padrao = ?, atualizado_em = ?
                WHERE jogador_id = ? AND id = ?
                """,
                (
                    (dados.get("nome") or "Viajante").strip() or "Viajante",
                    dados.get("genero") or "Feminino",
                    (dados.get("idade") or "").strip(),
                    (dados.get("personalidade") or dados.get("descricao") or "").strip(),
                    (dados.get("interesses") or "").strip(),
                    (dados.get("aparencia") or "").strip(),
                    (dados.get("medos") or dados.get("aversoes") or "").strip(),
                    tags_str,
                    (dados.get("detalhes_imagem") or "").strip(),
                    dados.get("estilo_imagem") or "semi_realistic",
                    dados.get("avatar_emoji") or "👤",
                    1 if definir_como_padrao else 0,
                    ag,
                    jogador_id, persona_id
                )
            )
            return persona_id


def definir_persona_padrao(jogador_id, persona_id):
    with transacao() as c:
        c.execute("UPDATE personas SET padrao = 0 WHERE jogador_id = ?", (jogador_id,))
        c.execute("UPDATE personas SET padrao = 1 WHERE jogador_id = ? AND id = ?", (jogador_id, persona_id))


def incrementar_retrato_persona(jogador_id, persona_id):
    with transacao() as c:
        c.execute("UPDATE personas SET retrato_versao = retrato_versao + 1 WHERE jogador_id = ? AND id = ?", (jogador_id, persona_id))


def deletar_persona(jogador_id, persona_id):
    with transacao() as c:
        antiga = c.execute("SELECT padrao FROM personas WHERE jogador_id = ? AND id = ?", (jogador_id, persona_id)).fetchone()
        c.execute("DELETE FROM personas WHERE jogador_id = ? AND id = ?", (jogador_id, persona_id))
        if antiga and antiga["padrao"]:
            proxima = c.execute("SELECT id FROM personas WHERE jogador_id = ? ORDER BY id ASC LIMIT 1", (jogador_id,)).fetchone()
            if proxima:
                c.execute("UPDATE personas SET padrao = 1 WHERE id = ?", (proxima["id"],))


def salvar_cenario_usuario(cenario_id, autor_id, autor_nome, nome, subtitulo, emoji, genero, tags, sinopse, abertura, prompt_secreto, estilo_imagem="semi_realistic", modo_jogo="historia"):
    import json
    tags_str = json.dumps(tags if isinstance(tags, list) else [])
    with transacao() as c:
        c.execute(
            """INSERT INTO cenarios_usuario (
                id, autor_id, autor_nome, nome, subtitulo, emoji, genero, tags, sinopse, abertura, prompt_secreto, estilo_imagem, modo_jogo, criado_em
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                nome=excluded.nome,
                subtitulo=excluded.subtitulo,
                emoji=excluded.emoji,
                genero=excluded.genero,
                tags=excluded.tags,
                sinopse=excluded.sinopse,
                abertura=excluded.abertura,
                prompt_secreto=excluded.prompt_secreto,
                estilo_imagem=excluded.estilo_imagem,
                modo_jogo=excluded.modo_jogo
            """,
            (cenario_id, autor_id, autor_nome, nome, subtitulo, emoji, genero, tags_str, sinopse, abertura, prompt_secreto, estilo_imagem, modo_jogo, agora())
        )
    return cenario_id


def listar_cenarios_usuario():
    import json
    with conexao() as c:
        linhas = c.execute("SELECT * FROM cenarios_usuario ORDER BY criado_em DESC LIMIT 100").fetchall()
    resultado = []
    for l in linhas:
        d = dict(l)
        try:
            d["tags"] = json.loads(d.get("tags") or "[]")
        except Exception:
            d["tags"] = []
        d["criadoPorUsuario"] = True
        d["autorNome"] = d.get("autor_nome") or "Comunidade"
        d["autorAvatar"] = d.get("emoji") or "🎭"
        d["modoJogo"] = d.get("modo_jogo") or "historia"
        resultado.append(d)
    return resultado


def obter_cenario_usuario(cenario_id):
    import json
    with conexao() as c:
        linha = c.execute("SELECT * FROM cenarios_usuario WHERE id = ?", (cenario_id,)).fetchone()
    if not linha:
        return None
    d = dict(linha)
    try:
        d["tags"] = json.loads(d.get("tags") or "[]")
    except Exception:
        d["tags"] = []
    d["criadoPorUsuario"] = True
    d["autorNome"] = d.get("autor_nome") or "Comunidade"
    d["autorAvatar"] = d.get("emoji") or "🎭"
    d["modoJogo"] = d.get("modo_jogo") or "historia"
    return d


def incrementar_chat_cenario_usuario(cenario_id):
    with transacao() as c:
        c.execute("UPDATE cenarios_usuario SET chats = chats + 1 WHERE id = ?", (cenario_id,))


def adicionar_comentario(cenario_id, jogador_id, autor_nome, autor_avatar, texto):
    texto_limpo = (texto or "").strip()[:500]
    if not texto_limpo:
        raise ErroNegocio(400, "O comentário não pode ficar vazio.")
    agora_iso = agora()
    with transacao() as c:
        cur = c.execute(
            """INSERT INTO comentarios_cenario (cenario_id, jogador_id, autor_nome, autor_avatar, texto, criado_em)
               VALUES (?, ?, ?, ?, ?, ?)""",
            (cenario_id, jogador_id, autor_nome, autor_avatar or "👤", texto_limpo, agora_iso),
        )
        cid = cur.lastrowid
    return obter_comentario(cid, jogador_id)


def obter_comentario(comentario_id, jogador_id=None):
    with conexao() as c:
        linha = c.execute(
            "SELECT * FROM comentarios_cenario WHERE id = ?", (comentario_id,)
        ).fetchone()
        if not linha:
            return None
        d = dict(linha)
        if jogador_id:
            like = c.execute(
                "SELECT 1 FROM likes_comentario WHERE comentario_id = ? AND jogador_id = ?",
                (comentario_id, jogador_id),
            ).fetchone()
            d["meu_like"] = bool(like)
            d["meu_comentario"] = (d["jogador_id"] == jogador_id)
        else:
            d["meu_like"] = False
            d["meu_comentario"] = False
        return d


def listar_comentarios_cenario(cenario_id, jogador_id=None):
    with conexao() as c:
        linhas = c.execute(
            "SELECT * FROM comentarios_cenario WHERE cenario_id = ? ORDER BY criado_em DESC LIMIT 100",
            (cenario_id,),
        ).fetchall()
        resultado = []
        for l in linhas:
            d = dict(l)
            if jogador_id:
                like = c.execute(
                    "SELECT 1 FROM likes_comentario WHERE comentario_id = ? AND jogador_id = ?",
                    (d["id"], jogador_id),
                ).fetchone()
                d["meu_like"] = bool(like)
                d["meu_comentario"] = (d["jogador_id"] == jogador_id)
            else:
                d["meu_like"] = False
                d["meu_comentario"] = False
            resultado.append(d)
        return resultado


def editar_comentario(comentario_id, jogador_id, novo_texto):
    texto_limpo = (novo_texto or "").strip()[:500]
    if not texto_limpo:
        raise ErroNegocio(400, "O comentário não pode ficar vazio.")
    coment = obter_comentario(comentario_id)
    if not coment:
        raise ErroNegocio(404, "Comentário não encontrado.")
    if coment["jogador_id"] != jogador_id:
        raise ErroNegocio(403, "Você só pode editar o seu próprio comentário.")
    agora_iso = agora()
    with transacao() as c:
        c.execute(
            "UPDATE comentarios_cenario SET texto = ?, editado_em = ? WHERE id = ? AND jogador_id = ?",
            (texto_limpo, agora_iso, comentario_id, jogador_id),
        )
    return obter_comentario(comentario_id, jogador_id)


def excluir_comentario(comentario_id, jogador_id):
    coment = obter_comentario(comentario_id)
    if not coment:
        raise ErroNegocio(404, "Comentário não encontrado.")
    if coment["jogador_id"] != jogador_id:
        raise ErroNegocio(403, "Você só pode excluir o seu próprio comentário.")
    with transacao() as c:
        c.execute("DELETE FROM comentarios_cenario WHERE id = ?", (comentario_id,))
        c.execute("DELETE FROM likes_comentario WHERE comentario_id = ?", (comentario_id,))
    return True


def alternar_like_comentario(comentario_id, jogador_id):
    coment = obter_comentario(comentario_id)
    if not coment:
        raise ErroNegocio(404, "Comentário não encontrado.")
    with transacao() as c:
        ja_curtiu = c.execute(
            "SELECT 1 FROM likes_comentario WHERE comentario_id = ? AND jogador_id = ?",
            (comentario_id, jogador_id),
        ).fetchone()
        if ja_curtiu:
            c.execute("DELETE FROM likes_comentario WHERE comentario_id = ? AND jogador_id = ?", (comentario_id, jogador_id))
            c.execute("UPDATE comentarios_cenario SET likes = MAX(0, likes - 1) WHERE id = ?", (comentario_id,))
            meu_like = False
        else:
            c.execute("INSERT INTO likes_comentario (comentario_id, jogador_id, criado_em) VALUES (?, ?, ?)", (comentario_id, jogador_id, agora()))
            c.execute("UPDATE comentarios_cenario SET likes = likes + 1 WHERE id = ?", (comentario_id,))
            meu_like = True
        linha = c.execute("SELECT likes FROM comentarios_cenario WHERE id = ?", (comentario_id,)).fetchone()
        likes_atualizados = int(linha["likes"]) if linha else 0
    return {"likes": likes_atualizados, "meu_like": meu_like}


def alternar_seguir_criador(seguidor_id, criador_nome):
    nome_alvo = (criador_nome or "").strip()
    if not nome_alvo:
        raise ErroNegocio(400, "Nome do criador inválido.")
    with transacao() as c:
        ja_segue = c.execute(
            "SELECT 1 FROM seguidores WHERE seguidor_id = ? AND criador_nome = ?",
            (seguidor_id, nome_alvo),
        ).fetchone()
        if ja_segue:
            c.execute("DELETE FROM seguidores WHERE seguidor_id = ? AND criador_nome = ?", (seguidor_id, nome_alvo))
            esta_seguindo = False
        else:
            c.execute("INSERT INTO seguidores (seguidor_id, criador_nome, criado_em) VALUES (?, ?, ?)", (seguidor_id, nome_alvo, agora()))
            esta_seguindo = True
        cont = c.execute("SELECT COUNT(*) as tot FROM seguidores WHERE criador_nome = ?", (nome_alvo,)).fetchone()
        total_seguidores = int(cont["tot"]) if cont else 0
    return {"esta_seguindo": esta_seguindo, "total_seguidores": total_seguidores}


def obter_status_criador(criador_nome, seguidor_id=None):
    nome_alvo = (criador_nome or "").strip()
    with conexao() as c:
        cont_seg = c.execute("SELECT COUNT(*) as tot FROM seguidores WHERE criador_nome = ?", (nome_alvo,)).fetchone()
        total_seguidores = int(cont_seg["tot"]) if cont_seg else 0
        esta_seguindo = False
        esta_bloqueado = False
        if seguidor_id:
            s = c.execute("SELECT 1 FROM seguidores WHERE seguidor_id = ? AND criador_nome = ?", (seguidor_id, nome_alvo)).fetchone()
            esta_seguindo = bool(s)
            b = c.execute("SELECT 1 FROM bloqueios_usuario WHERE jogador_id = ? AND alvo_nome = ?", (seguidor_id, nome_alvo)).fetchone()
            esta_bloqueado = bool(b)
        # Total de chats e saves dos cenários deste criador
        stats = c.execute(
            "SELECT SUM(chats) as total_chats, SUM(saves) as total_saves FROM cenarios_usuario WHERE autor_nome = ?",
            (nome_alvo,)
        ).fetchone()
        chats = int(stats["total_chats"] or 0) if stats else 0
        saves = int(stats["total_saves"] or 0) if stats else 0
        # Histórias criadas por ele
        linhas = c.execute(
            "SELECT * FROM cenarios_usuario WHERE autor_nome = ? ORDER BY criado_em DESC",
            (nome_alvo,)
        ).fetchall()
        import json
        historias = []
        for l in linhas:
            d = dict(l)
            try:
                d["tags"] = json.loads(d.get("tags") or "[]")
            except Exception:
                d["tags"] = []
            d["criadoPorUsuario"] = True
            d["autorNome"] = d.get("autor_nome") or nome_alvo
            d["autorAvatar"] = d.get("emoji") or "🎭"
            d["modoJogo"] = d.get("modo_jogo") or "historia"
            historias.append(d)

    return {
        "nome": nome_alvo,
        "avatar": (historias[0]["autorAvatar"] if historias else "🎭"),
        "uid": f"UID:@{nome_alvo}",
        "chats": max(chats, 1),
        "saves": max(saves, 0),
        "seguidores": total_seguidores,
        "seguindo": 1,
        "esta_seguindo": esta_seguindo,
        "esta_bloqueado": esta_bloqueado,
        "historias": historias,
    }


def bloquear_criador(jogador_id, criador_nome):
    nome_alvo = (criador_nome or "").strip()
    with transacao() as c:
        c.execute(
            "INSERT OR REPLACE INTO bloqueios_usuario (jogador_id, alvo_nome, criado_em) VALUES (?, ?, ?)",
            (jogador_id, nome_alvo, agora())
        )
    return True


def denunciar_criador(jogador_id, criador_nome, motivo):
    with transacao() as c:
        c.execute(
            "INSERT INTO denuncias (jogador_id, jogo_id, motivo, trecho, criado_em) VALUES (?, ?, ?, ?, ?)",
            (jogador_id, f"perfil:{criador_nome}", motivo or "Perfil inapropriado", f"Denúncia do criador {criador_nome}", agora())
        )
    return True

