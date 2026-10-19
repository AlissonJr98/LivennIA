"""
Banco de dados (SQLite) do LivennIA: jogadores, convites, jogos, uso, feedbacks e denúncias.
"""
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

CREATE TABLE IF NOT EXISTS feedbacks (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    jogador_id  INTEGER REFERENCES jogadores(id) ON DELETE CASCADE,
    nota        INTEGER NOT NULL,
    comentario  TEXT,
    criado_em   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS denuncias (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    jogador_id  INTEGER REFERENCES jogadores(id) ON DELETE CASCADE,
    jogo_id     TEXT,
    motivo      TEXT NOT NULL,
    trecho      TEXT,
    criado_em   TEXT NOT NULL,
    revisada    INTEGER NOT NULL DEFAULT 0
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
            "SELECT usos, usos_max, ativo FROM convites WHERE codigo = ?", (codigo,)
        ).fetchone()
        if convite is None or not convite["ativo"] or convite["usos"] >= convite["usos_max"]:
            # Mesma mensagem para qualquer problema, para não ajudar quem está chutando códigos.
            raise ErroNegocio(403, "Código de convite inválido ou já utilizado.")

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
            c.execute(
                "UPDATE jogadores SET token_hash = ? WHERE id = ?",
                (token_hash, existente["id"]),
            )
            return existente["id"], True

        total = c.execute("SELECT COUNT(*) FROM jogadores").fetchone()[0]
        if total >= config.MAX_JOGADORES:
            raise ErroNegocio(503, "O beta está lotado no momento.")

        cursor = c.execute(
            "INSERT INTO jogadores (apelido, token_hash, convite, criado_em) VALUES (?, ?, ?, ?)",
            (apelido, token_hash, codigo, agora()),
        )
        return cursor.lastrowid, False


def jogador_por_token_hash(token_hash):
    with conexao() as c:
        linha = c.execute(
            "SELECT id, apelido, banido, premium, mochis, ultimo_resgate_mochi "
            "FROM jogadores WHERE token_hash = ?",
            (token_hash,),
        ).fetchone()
    if not linha:
        return None
    d = dict(linha)
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