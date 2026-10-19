"""
API do LivennIA (FastAPI).

Rodar (na pasta do projeto):
    python -m uvicorn servidor:app --host 127.0.0.1 --port 8000

Página de testes (só fora de produção): http://127.0.0.1:8000/docs
"""
import logging
import os
import threading
import time
import uuid
from contextlib import asynccontextmanager, contextmanager
from logging.handlers import RotatingFileHandler
from typing import Annotated, Literal, Optional

from fastapi import Depends, FastAPI, HTTPException, Path, Request, Response
from fastapi.responses import FileResponse, JSONResponse
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel, ConfigDict, Field

import banco
import catalogo
import config
import motor
from seguranca import LimitadorDeTaxa, gerar_token, hash_token

try:
    import firebase_admin
    from firebase_admin import auth as firebase_auth, credentials as firebase_credentials
    _FIREBASE_DISPONIVEL = True
except ImportError:
    _FIREBASE_DISPONIVEL = False

# ---------------------------------------------------------------
# Log (nunca registramos tokens nem textos digitados pelos jogadores)
# ---------------------------------------------------------------
_arquivo_log = RotatingFileHandler(
    os.path.join(config.PASTA, "livennia_servidor.log"),
    maxBytes=2_000_000, backupCount=3, encoding="utf-8",
)
_arquivo_log.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
logging.getLogger().addHandler(_arquivo_log)
logging.getLogger().setLevel(logging.INFO)
logging.getLogger("httpx").setLevel(logging.WARNING)
logging.getLogger("google_genai").setLevel(logging.WARNING)

if not config.CHAVE_API:
    raise SystemExit(
        "Chave não encontrada. Crie um arquivo .env na pasta do projeto com a linha: "
        "GEMINI_API_KEY=sua_chave"
    )


@asynccontextmanager
async def ciclo_de_vida(app):
    banco.iniciar_banco()
    if _FIREBASE_DISPONIVEL:
        try:
            if config.FIREBASE_SERVICE_ACCOUNT:
                cred = firebase_credentials.Certificate(config.FIREBASE_SERVICE_ACCOUNT)
            else:
                cred = firebase_credentials.ApplicationDefault()
            firebase_admin.initialize_app(cred)
            logging.info("Firebase inicializado.")
        except Exception as erro:
            logging.warning("Firebase não inicializado: %r", erro)
    logging.info("Servidor iniciado.")
    yield


app = FastAPI(
    title="LivennIA API",
    version="0.1.0",
    lifespan=ciclo_de_vida,
    docs_url=None if config.PRODUCAO else "/docs",
    redoc_url=None,
    openapi_url=None if config.PRODUCAO else "/openapi.json",
)

# Não ligamos CORS de propósito: o app Android não precisa, e assim nenhum site
# qualquer consegue usar esta API pelo navegador de outra pessoa.

# ---------------------------------------------------------------
# Limitadores de taxa
# ---------------------------------------------------------------
limite_ip_geral = LimitadorDeTaxa(config.LIMITE_REQUISICOES_IP_MINUTO, 60)
limite_registro = LimitadorDeTaxa(config.LIMITE_REGISTROS_POR_IP_HORA, 3600)
limite_jogadas = LimitadorDeTaxa(config.LIMITE_JOGADAS_POR_MINUTO, 60)
limite_feedback = LimitadorDeTaxa(config.LIMITE_FEEDBACKS_HORA, 3600)
limite_avatar = LimitadorDeTaxa(20, 60)
limite_retrato = LimitadorDeTaxa(config.MAX_RETRATOS_POR_JOGADOR_HORA, 3600)
limite_capa = LimitadorDeTaxa(20, 3600)

# Contador global de retratos gerados no dia (protege a cota de imagem).
_retratos_dia = {"dia": None, "total": 0}
_trava_retratos = threading.Lock()


def caminho_retrato(jogo_id):
    return os.path.join(config.PASTA_RETRATOS, f"{jogo_id}.png")


def tem_retrato(jogo_id):
    try:
        return os.path.exists(caminho_retrato(jogo_id))
    except OSError:
        return False


def consumir_retrato_do_dia():
    with _trava_retratos:
        hoje = banco.hoje()
        if _retratos_dia["dia"] != hoje:
            _retratos_dia["dia"] = hoje
            _retratos_dia["total"] = 0
        if _retratos_dia["total"] >= config.MAX_RETRATOS_POR_DIA:
            raise HTTPException(503, "A geração de retratos atingiu o limite de hoje.")
        _retratos_dia["total"] += 1


def caminho_capa(cenario_id):
    return os.path.join(config.PASTA_CAPAS, f"{cenario_id}.png")


def tem_capa(cenario_id):
    try:
        return os.path.exists(caminho_capa(cenario_id))
    except OSError:
        return False


# Contador global de capas geradas no dia (as capas ficam salvas: é raro gerar).
_capas_dia = {"dia": None, "total": 0}
_trava_capas = threading.Lock()


def consumir_capa_do_dia():
    with _trava_capas:
        hoje = banco.hoje()
        if _capas_dia["dia"] != hoje:
            _capas_dia["dia"] = hoje
            _capas_dia["total"] = 0
        if _capas_dia["total"] >= config.MAX_CAPAS_POR_DIA:
            raise HTTPException(503, "A geração de capas atingiu o limite de hoje.")
        _capas_dia["total"] += 1


def tipo_de_imagem(caminho):
    """O provedor grátis devolve JPEG e o Gemini, PNG: o tipo tem que bater com o arquivo."""
    with open(caminho, "rb") as arquivo:
        inicio = arquivo.read(4)
    return "image/jpeg" if inicio.startswith(b"\xff\xd8") else "image/png"


def ip_do_cliente(request: Request):
    # ATENÇÃO (3C): atrás de um serviço de hospedagem com proxy, este IP será o do proxy.
    # Na hora de publicar, configuramos o uvicorn para ler o IP real com segurança.
    return request.client.host if request.client else "desconhecido"


@app.middleware("http")
async def protecoes(request: Request, chamada):
    if not limite_ip_geral.permitir(ip_do_cliente(request)):
        return JSONResponse({"detail": "Muitas requisições. Aguarde um pouco."}, status_code=429)
    tamanho = request.headers.get("content-length")
    if tamanho and tamanho.isdigit() and int(tamanho) > config.MAX_BODY_BYTES:
        return JSONResponse({"detail": "Requisição grande demais."}, status_code=413)
    return await chamada(request)


@app.exception_handler(Exception)
async def erro_inesperado(request: Request, erro: Exception):
    logging.exception("Erro inesperado em %s %s", request.method, request.url.path)
    return JSONResponse({"detail": "Erro interno do servidor."}, status_code=500)


# ---------------------------------------------------------------
# Modelos de entrada e saída (o "contrato" com o app Kotlin)
# ---------------------------------------------------------------
class Base(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)


class RegistroIn(Base):
    codigo_convite: str = Field(min_length=4, max_length=40)
    apelido: str = Field(min_length=2, max_length=20, pattern=r"^[\w .-]+$")


class RegistroOut(BaseModel):
    jogador_id: int
    apelido: str
    token: str  # guarde com segurança no app: só aparece nesta resposta!
    # True quando o apelido já existia e o jogador voltou para a conta dele.
    recuperado: bool = False

class FirebaseAuthIn(Base):
    id_token: str = Field(min_length=10)


class NovoJogoIn(Base):
    nome_personagem: str = Field(default="Viajante", min_length=1, max_length=30)
    modo: Literal["rpg", "historia"] = Field(default="rpg")
    genero: str = Field(default="Aventura", max_length=30)
    premissa: Optional[str] = Field(default=None, max_length=200)
    # Ficha do personagem (usada na história e para montar o retrato por IA).
    aparencia: Optional[str] = Field(default=None, max_length=200)
    descricao: Optional[str] = Field(default=None, max_length=300)
    idade: Optional[str] = Field(default=None, max_length=20)
    interesses: Optional[str] = Field(default=None, max_length=200)
    aversoes: Optional[str] = Field(default=None, max_length=200)
    tags: Optional[list[str]] = Field(default=None, max_length=8)
    # Vitrine: se vier um cenário oficial, o servidor injeta o prompt secreto dele
    # e ignora o gênero informado (cada cenário tem o seu).
    cenario_id: Optional[str] = Field(default=None, max_length=40)
    # Cérebro de IA escolhido (veja catalogo.CEREBROS). Sem escolha, usa o padrão.
    modelo_id: Optional[str] = Field(default=None, max_length=40)


class TurnoIn(Base):
    opcao: Optional[int] = Field(default=None, ge=1, le=4)
    acao: Optional[str] = Field(default=None, min_length=1, max_length=config.MAX_TAMANHO_ACAO)
    pedido_id: Optional[str] = Field(default=None, min_length=1, max_length=64, pattern=r"^[A-Za-z0-9_-]+$")


class FeedbackIn(Base):
    nota: int = Field(ge=1, le=5)
    comentario: Optional[str] = Field(default=None, max_length=500)


class DenunciaIn(Base):
    jogo_id: str = Field(pattern=r"^[0-9a-f]{32}$")
    motivo: Literal["ofensivo", "violento_demais", "incoerente", "outro"]
    detalhe: Optional[str] = Field(default=None, max_length=300)


class PersonagemOut(BaseModel):
    nome: str
    vida: int
    vida_max: int
    inventario: list[str]
    local: str


class CenaOut(BaseModel):
    narrativa: str
    opcoes: list[str]


class EventoOut(BaseModel):
    tipo: str   # "vida", "item_ganho", "item_perdido" ou "local"
    texto: str


class MensagemOut(BaseModel):
    acao: str        # o que o jogador fez (o texto da opção ou o que ele digitou)
    # O gesto do jogador em cena (a IA escreve quando ele escolhe uma opção). O chat
    # mostra isto no lugar do texto da opção; quando é null, mostra a "acao".
    situacao: Optional[str] = None
    narrativa: str   # a resposta do narrador
    eventos: list[EventoOut] = []
    # Horários (epoch em milissegundos): o chat mostra "enviado às 14:32" em cada fala.
    em_jogador: Optional[int] = None
    em_narrador: Optional[int] = None


class CenarioDoJogoOut(BaseModel):
    """O cenário oficial do jogo, do jeito que o app precisa mostrar (sem o segredo)."""
    id: str
    nome: str
    subtitulo: str = ""
    emoji: str = ""
    tags: list[str] = []
    # True quando a arte de capa já foi gerada e está salva no servidor.
    capa: bool = False


class CerebroDoJogoOut(BaseModel):
    id: str
    nome: str
    emoji: str = ""


class JogoOut(BaseModel):
    jogo_id: str
    modo: str = "rpg"
    turno: int
    avatar_seed: int
    genero: str = "Aventura"
    personagem: PersonagemOut
    cena: CenaOut
    # GET /jogos/{id}: as mensagens recentes (para montar o chat).
    # Ao jogar um turno ou criar o jogo: só a mensagem mais recente.
    historico: list[MensagemOut]
    fim_de_jogo: bool
    jogadas_restantes_hoje: int
    # True quando o personagem já tem um retrato gerado pela IA.
    retrato: bool = False
    # Preenchidos só quando a história começou por um cenário da vitrine.
    cenario: Optional[CenarioDoJogoOut] = None
    modelo: Optional[CerebroDoJogoOut] = None


class RetratoOut(BaseModel):
    retrato: bool = True


class AvatarOut(BaseModel):
    avatar_seed: int


class CenarioOut(BaseModel):
    """Um cenário da vitrine: o que o app mostra (o prompt secreto fica no servidor)."""
    id: str
    nome: str
    subtitulo: str
    emoji: str
    genero: str
    tags: list[str]
    sinopse: str
    abertura: str = ""
    # True quando a arte de capa já foi gerada e está salva no servidor.
    capa: bool = False


class CerebroOut(BaseModel):
    id: str
    nome: str
    emoji: str
    descricao: str
    premium: bool = False
    # False = o jogador não pode escolher este modelo agora (é premium e a conta é free).
    liberado: bool = True


class CapaOut(BaseModel):
    capa: bool = True


class JogoResumoOut(BaseModel):
    jogo_id: str
    modo: str = "rpg"
    avatar_seed: int
    nome: str
    turno: int
    local: str
    vida: int
    vida_max: int
    encerrado: bool
    genero: str = "Aventura"
    atualizado_em: str
    retrato: bool = False
    # Nome do cenário oficial (quando a história veio da vitrine) e se já tem capa.
    cenario_nome: Optional[str] = None
    capa: bool = False
    ultima_narrativa: Optional[str] = None


class ContaOut(BaseModel):
    apelido: str
    limite_diario: int
    jogadas_hoje: int
    jogadas_restantes_hoje: int
    jogos_ativos: int
    max_jogos: int
    premium: bool = False


IdDoJogo = Annotated[str, Path(pattern=r"^[0-9a-f]{32}$")]
IdDoCenario = Annotated[str, Path(pattern=r"^[a-z0-9-]{2,40}$")]

# ---------------------------------------------------------------
# Autenticação
# ---------------------------------------------------------------
esquema_bearer = HTTPBearer(auto_error=False)


def jogador_atual(credenciais: Optional[HTTPAuthorizationCredentials] = Depends(esquema_bearer)):
    cabecalho = {"WWW-Authenticate": "Bearer"}
    if credenciais is None or not credenciais.credentials or len(credenciais.credentials) > 200:
        raise HTTPException(401, "Não autenticado.", headers=cabecalho)
    jogador = banco.jogador_por_token_hash(hash_token(credenciais.credentials))
    if jogador is None:
        raise HTTPException(401, "Token inválido.", headers=cabecalho)
    if jogador["banido"]:
        raise HTTPException(403, "Conta suspensa.")
    return jogador


# ---------------------------------------------------------------
# Regras de limite
# ---------------------------------------------------------------
def exigir_ritmo(jogador_id):
    if not limite_jogadas.permitir(f"jogador:{jogador_id}"):
        raise HTTPException(429, "Calma! Aguarde um instante antes da próxima jogada.")


def jogadas_restantes(jogador_id):
    usadas = banco.contar_jogadas(jogador_id, banco.hoje())
    return max(0, config.LIMITE_DIARIO_JOGADAS - usadas)


def verificar_limites(jogador_id):
    """Confere a cota do jogador e o teto global do dia. Devolve quantas jogadas restam."""
    restantes = jogadas_restantes(jogador_id)
    if restantes <= 0:
        raise HTTPException(429, "Você usou todas as jogadas de hoje. Volte amanhã!")
    jogadas_dia, tokens_dia = banco.totais_do_dia(banco.hoje())
    if jogadas_dia >= config.TETO_GLOBAL_JOGADAS_DIA or tokens_dia >= config.TETO_GLOBAL_TOKENS_DIA:
        logging.warning("Teto global do dia atingido (%s jogadas, %s tokens).", jogadas_dia, tokens_dia)
        raise HTTPException(503, "O beta atingiu o limite de hoje. Volte amanhã!")
    return restantes


_ocupados = set()
_trava_ocupados = threading.Lock()


@contextmanager
def uma_jogada_por_vez(jogador_id):
    """Cada jogador só pode ter UMA jogada em andamento. Evita duplicar jogadas e furar a cota."""
    with _trava_ocupados:
        if jogador_id in _ocupados:
            raise HTTPException(409, "Aguarde a jogada anterior terminar.")
        _ocupados.add(jogador_id)
    try:
        yield
    finally:
        with _trava_ocupados:
            _ocupados.discard(jogador_id)


def _agora_ms():
    """Horário atual em milissegundos (para o chat mostrar a hora de cada fala)."""
    return int(time.time() * 1000)


def chamar_ia(jogador_id, estado, acao, escolha=False):
    """Chama o motor, registra o uso (sempre) e traduz erros para respostas HTTP."""
    inicio = time.perf_counter()
    try:
        resultado = motor.pedir_turno(estado, acao, escolha)
    except motor.ErroIA as erro:
        banco.registrar_uso(
            jogador_id, False, None, erro.tokens_entrada, erro.tokens_saida,
            time.perf_counter() - inicio, erro.tentativas,
        )
        logging.warning("Jogada falhou para o jogador %s: %s", jogador_id, erro)
        if erro.ocupada:
            raise HTTPException(503, "O servidor está cheio agora. Tente de novo em instantes.")
        if erro.bloqueada:
            raise HTTPException(
                422, "O narrador não pôde seguir por causa de um filtro de segurança. Tente outra ação."
            )
        raise HTTPException(503, "A IA está instável agora. Tente de novo em instantes.")
    banco.registrar_uso(
        jogador_id, True, resultado.modelo, resultado.tokens_entrada, resultado.tokens_saida,
        resultado.segundos, resultado.tentativas,
    )
    return resultado


def carregar_estado_do_jogo(jogo):
    try:
        return motor.carregar_estado(jogo["estado"])
    except Exception:
        logging.exception("Estado inválido no jogo %s", jogo["id"])
        raise HTTPException(500, "Não foi possível carregar este jogo.")


def montar_saida(jogo_id, estado, encerrado, restantes, completo=False):
    mensagens = estado["historico"] if completo else estado["historico"][-1:]
    cenario = estado.get("cenario")
    modelo = estado.get("modelo")
    return JogoOut(
        jogo_id=jogo_id,
        modo=estado.get("modo", "rpg"),
        turno=estado["turno"],
        avatar_seed=estado["avatar_seed"],
        genero=estado.get("genero", "Aventura"),
        personagem=estado["personagem"],
        cena=estado["cena"],
        historico=mensagens,
        fim_de_jogo=bool(encerrado),
        jogadas_restantes_hoje=restantes,
        retrato=tem_retrato(jogo_id),
        cenario=(
            CenarioDoJogoOut(
                id=cenario["id"],
                nome=cenario["nome"],
                subtitulo=cenario.get("subtitulo") or "",
                emoji=cenario.get("emoji") or "",
                tags=list(cenario.get("tags") or []),
                capa=tem_capa(cenario["id"]),
            )
            if cenario
            else None
        ),
        modelo=(
            CerebroDoJogoOut(
                id=modelo["id"], nome=modelo["nome"], emoji=modelo.get("emoji") or ""
            )
            if modelo
            else None
        ),
    )


# ---------------------------------------------------------------
# Rotas
# ---------------------------------------------------------------
@app.get("/saude")
@app.get("/health")  # apelido: tem gente que procura por este nome
def saude():
    """Usada pela hospedagem para saber se o servidor está de pé."""
    return {"status": "ok"}


@app.get("/generos")
def listar_generos():
    """Lista os gêneros de história disponíveis (com emoji, descrição e ideias prontas)."""
    return [
        {
            "nome": nome,
            "emoji": info["emoji"],
            "descricao": info["descricao"],
            "ideias": info.get("aberturas", []),
        }
        for nome, info in motor.GENEROS.items()
    ]


@app.get("/catalogo", response_model=list[CenarioOut])
def listar_catalogo(jogador=Depends(jogador_atual)):
    """A vitrine da aba Explorar: os cenários oficiais, sem os prompts secretos."""
    itens = []
    for item in catalogo.CENARIOS:
        publico = catalogo.cenario_publico(item)
        itens.append(CenarioOut(**publico, capa=tem_capa(item["id"])))
    return itens


@app.get("/cerebros", response_model=list[CerebroOut])
def listar_cerebros(jogador=Depends(jogador_atual)):
    """Os "cérebros" de IA, com nomes de fantasia. Os premium vêm com liberado=False."""
    eh_premium = bool(jogador.get("premium"))
    return [
        CerebroOut(
            id=item["id"],
            nome=item["nome"],
            emoji=item["emoji"],
            descricao=item["descricao"],
            premium=bool(item["premium"]),
            liberado=(not item["premium"]) or eh_premium,
        )
        for item in catalogo.CEREBROS
    ]


@app.post("/registrar", response_model=RegistroOut, status_code=201)
def registrar(dados: RegistroIn, request: Request):
    if not limite_registro.permitir(ip_do_cliente(request)):
        raise HTTPException(429, "Muitas tentativas. Tente novamente mais tarde.")
    token = gerar_token()
    try:
        jogador_id, recuperado = banco.registrar_jogador(
            dados.codigo_convite.upper(), dados.apelido, hash_token(token)
        )
    except banco.ErroNegocio as erro:
        raise HTTPException(erro.status, erro.mensagem)
    apelido = dados.apelido
    if recuperado:
        # O apelido pode ter sido digitado com outra capitalização: devolvemos o original.
        with banco.conexao() as c:
            linha = c.execute("SELECT apelido FROM jogadores WHERE id = ?", (jogador_id,)).fetchone()
        if linha is not None:
            apelido = linha["apelido"]
    return RegistroOut(
        jogador_id=jogador_id, apelido=apelido, token=token, recuperado=recuperado
    )


@app.post("/auth/firebase", response_model=RegistroOut, status_code=200)
def auth_firebase(dados: FirebaseAuthIn):
    """Troca um ID token do Firebase por um token de sessão do LivennIA."""
    if not _FIREBASE_DISPONIVEL:
        raise HTTPException(503, "Autenticação por conta não está disponível no servidor.")
    try:
        decodificado = firebase_auth.verify_id_token(dados.id_token)
    except Exception:
        raise HTTPException(401, "Token do Firebase inválido ou expirado.")
    uid = decodificado.get("uid")
    email = decodificado.get("email")
    nome = decodificado.get("name") or (email.split("@")[0] if email else "Viajante")
    nome = motor.limpar_texto(nome, 20) or "Viajante"
    # Procura jogador existente pelo uid do Firebase (guardado no campo convite como 'firebase:<uid>')
    with banco.conexao() as c:
        linha = c.execute(
            "SELECT id, apelido, token_hash FROM jogadores WHERE convite = ?", (f"firebase:{uid}",)
        ).fetchone()
    if linha is not None:
        # Reemite um novo token de sessão
        token = gerar_token()
        with banco.transacao() as c:
            c.execute("UPDATE jogadores SET token_hash = ? WHERE id = ?", (hash_token(token), linha["id"]))
        return RegistroOut(jogador_id=linha["id"], apelido=linha["apelido"], token=token)
    # Cria novo jogador
    token = gerar_token()
    try:
        with banco.transacao() as c:
            total = c.execute("SELECT COUNT(*) FROM jogadores").fetchone()[0]
            if total >= config.MAX_JOGADORES:
                raise HTTPException(503, "O beta está lotado no momento.")
            cursor = c.execute(
                "INSERT INTO jogadores (apelido, token_hash, convite, criado_em) VALUES (?, ?, ?, ?)",
                (nome, hash_token(token), f"firebase:{uid}", banco.agora()),
            )
            jogador_id = cursor.lastrowid
    except HTTPException:
        raise
    except Exception as erro:
        logging.exception("Falha ao criar jogador Firebase: %r", erro)
        raise HTTPException(500, "Não foi possível criar a conta.")
    return RegistroOut(jogador_id=jogador_id, apelido=nome, token=token)


class NotificacaoIn(Base):
    token_fcm: str = Field(min_length=10)
    titulo: str = Field(min_length=1, max_length=80)
    corpo: str = Field(min_length=1, max_length=200)


@app.post("/notificacoes/enviar", status_code=200)
def enviar_notificacao(dados: NotificacaoIn):
    """Envia uma notificação push via FCM (uso interno/admin)."""
    if not _FIREBASE_DISPONIVEL:
        raise HTTPException(503, "Firebase não disponível.")
    try:
        from firebase_admin import messaging
        mensagem = messaging.Message(
            notification=messaging.Notification(title=dados.titulo, body=dados.corpo),
            token=dados.token_fcm,
        )
        messaging.send(mensagem)
    except Exception as erro:
        logging.exception("Falha ao enviar notificação: %r", erro)
        raise HTTPException(500, "Não foi possível enviar a notificação.")
    return {"status": "enviado"}


@app.get("/conta", response_model=ContaOut)
def ver_conta(jogador=Depends(jogador_atual)):
    restantes = jogadas_restantes(jogador["id"])
    return ContaOut(
        apelido=jogador["apelido"],
        limite_diario=config.LIMITE_DIARIO_JOGADAS,
        jogadas_hoje=config.LIMITE_DIARIO_JOGADAS - restantes,
        jogadas_restantes_hoje=restantes,
        jogos_ativos=banco.contar_jogos_ativos(jogador["id"]),
        max_jogos=config.MAX_JOGOS_POR_JOGADOR,
        premium=bool(jogador.get("premium")),
    )


@app.delete("/conta", status_code=204)
def apagar_conta(jogador=Depends(jogador_atual)):
    """Apaga a conta e todos os dados do jogador (direito de exclusão da LGPD)."""
    banco.apagar_jogador(jogador["id"])
    logging.info("Conta %s apagada pelo próprio jogador.", jogador["id"])
    return Response(status_code=204)


@app.post("/jogos", response_model=JogoOut, status_code=201)
def criar_jogo(dados: NovoJogoIn, jogador=Depends(jogador_atual)):
    """Cria um personagem e já joga o primeiro turno (conta como 1 jogada)."""
    exigir_ritmo(jogador["id"])
    with uma_jogada_por_vez(jogador["id"]):
        if banco.contar_jogos_ativos(jogador["id"]) >= config.MAX_JOGOS_POR_JOGADOR:
            raise HTTPException(
                409, f"Limite de {config.MAX_JOGOS_POR_JOGADOR} personagens ativos. Apague um para criar outro."
            )
        restantes = verificar_limites(jogador["id"])
        # Vitrine: se o jogador tocou num card da aba Explorar, o cenário oficial traz
        # o gênero, a abertura e o PROMPT SECRETO do personagem.
        item = None
        if dados.cenario_id:
            item = catalogo.cenario(dados.cenario_id)
            if item is None:
                raise HTTPException(422, "Cenário desconhecido.")
        cerebro = None
        if dados.modelo_id:
            cerebro = catalogo.cerebro(dados.modelo_id)
            if cerebro is None:
                raise HTTPException(422, "Modelo de IA desconhecido.")
            if cerebro["premium"] and not jogador.get("premium"):
                raise HTTPException(
                    403, "Este cérebro é do plano premium. Escolha outro por enquanto."
                )
        genero = item["genero"] if item else (
            dados.genero if dados.genero in motor.GENEROS else "Aventura"
        )
        ficha = {
            "aparencia": dados.aparencia,
            "descricao": dados.descricao,
            "idade": dados.idade,
            "interesses": dados.interesses,
            "aversoes": dados.aversoes,
            "tags": dados.tags,
        }
        premissa = dados.premissa or (item["sinopse"] if item else None)
        estado = motor.novo_estado(
            dados.nome_personagem, genero, premissa, ficha, cenario=item, cerebro=cerebro, modo=dados.modo
        )
        acao_inicial = item["abertura"] if item else motor.abertura_de(genero)
        resultado = chamar_ia(jogador["id"], estado, acao_inicial)
        morreu = motor.registrar_turno(
            estado, acao_inicial, resultado, _agora_ms(), _agora_ms()
        )
        jogo_id = uuid.uuid4().hex
        banco.criar_jogo(jogo_id, jogador["id"], motor.estado_para_texto(estado), morreu)
    return montar_saida(jogo_id, estado, morreu, restantes - 1)


@app.get("/jogos", response_model=list[JogoResumoOut])
def listar_jogos(jogador=Depends(jogador_atual)):
    resumos = []
    for jogo in banco.listar_jogos(jogador["id"]):
        estado = carregar_estado_do_jogo(jogo)
        p = estado["personagem"]
        cenario = estado.get("cenario")
        cena = estado.get("cena") or {}
        ultima = (cena.get("narrativa") or "")[:140] or None
        resumos.append(JogoResumoOut(
            jogo_id=jogo["id"],
            modo=estado.get("modo", "rpg"),
            avatar_seed=estado["avatar_seed"],
            nome=p["nome"],
            turno=estado["turno"],
            local=p["local"],
            vida=p["vida"],
            vida_max=p["vida_max"],
            encerrado=bool(jogo["encerrado"]),
            genero=estado.get("genero", "Aventura"),
            atualizado_em=jogo["atualizado_em"],
            retrato=tem_retrato(jogo["id"]),
            cenario_nome=(cenario["nome"] if cenario else None),
            capa=bool(cenario and tem_capa(cenario["id"])),
            ultima_narrativa=ultima,
        ))
    return resumos


@app.get("/jogos/{jogo_id}", response_model=JogoOut)
def ver_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    return montar_saida(
        jogo_id, estado, jogo["encerrado"], jogadas_restantes(jogador["id"]), completo=True
    )


@app.post("/jogos/{jogo_id}/avatar", response_model=AvatarOut)
def sortear_avatar(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Sorteia outro retrato para o personagem. Não usa a IA e não gasta jogadas."""
    if not limite_avatar.permitir(f"jogador:{jogador['id']}"):
        raise HTTPException(429, "Calma! Aguarde um instante antes de sortear de novo.")
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        estado["avatar_seed"] = motor.novo_avatar_seed()
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
        # O retrato antigo (IA) deixa de combinar: apagamos para voltar ao desenho.
        try:
            if os.path.exists(caminho_retrato(jogo_id)):
                os.remove(caminho_retrato(jogo_id))
        except OSError:
            pass
    return AvatarOut(avatar_seed=estado["avatar_seed"])


@app.post("/jogos/{jogo_id}/retrato", response_model=RetratoOut)
def gerar_retrato_do_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Gera (ou regera) o retrato do personagem com a IA. Não gasta jogadas."""
    # O provedor grátis não precisa de chave; o Gemini precisa. Sem nenhum, não dá.
    usaveis = [
        p
        for p in config.PROVEDORES_IMAGEM
        if p == "pollinations" or (p == "gemini" and config.CHAVE_API)
    ]
    if not usaveis:
        raise HTTPException(503, "A geração de imagens não está configurada no servidor.")
    if not limite_retrato.permitir(f"retrato:{jogador['id']}"):
        raise HTTPException(429, "Calma! Aguarde um pouco para gerar outro retrato.")
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    consumir_retrato_do_dia()
    try:
        imagem = motor.gerar_retrato(estado)
    except motor.ErroIA as erro:
        # O motivo técnico fica no log; o app recebe algo amigável e cai no avatar desenhado.
        logging.warning("Retrato não gerado: %s", erro)
        raise HTTPException(
            503, "Não foi possível gerar o retrato agora. Tente de novo em instantes."
        )
    except HTTPException:
        raise
    except Exception as erro:
        logging.warning("Falha ao gerar retrato: %s", erro)
        raise HTTPException(
            503, "Não foi possível gerar o retrato agora (talvez sem cota de imagem). Tente de novo."
        )
    os.makedirs(config.PASTA_RETRATOS, exist_ok=True)
    with open(caminho_retrato(jogo_id), "wb") as arquivo:
        arquivo.write(imagem)
    return RetratoOut(retrato=True)


@app.get("/jogos/{jogo_id}/retrato")
def ver_retrato(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Devolve a imagem do retrato do personagem (se já foi gerado)."""
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    caminho = caminho_retrato(jogo_id)
    if not os.path.exists(caminho):
        raise HTTPException(404, "Este personagem ainda não tem retrato.")
    # O Gemini devolve PNG, mas o provedor grátis devolve JPEG: o tipo tem que bater.
    return FileResponse(
        caminho, media_type=tipo_de_imagem(caminho), headers={"Cache-Control": "public, max-age=86400"}
    )


@app.post("/catalogo/{cenario_id}/capa", response_model=CapaOut)
def gerar_capa_do_cenario(cenario_id: IdDoCenario, jogador=Depends(jogador_atual)):
    """Gera (uma única vez) a arte de capa de um cenário. Depois ela fica salva."""
    item = catalogo.cenario(cenario_id)
    if item is None:
        raise HTTPException(404, "Cenário não encontrado.")
    if tem_capa(item["id"]):
        return CapaOut(capa=True)
    usaveis = [
        p
        for p in config.PROVEDORES_IMAGEM
        if p == "pollinations" or (p == "gemini" and config.CHAVE_API)
    ]
    if not usaveis:
        raise HTTPException(503, "A geração de imagens não está configurada no servidor.")
    if not limite_capa.permitir(f"capa:{jogador['id']}"):
        raise HTTPException(429, "Calma! Aguarde um pouco para gerar outra capa.")
    consumir_capa_do_dia()
    try:
        imagem = motor.gerar_capa(item)
    except motor.ErroIA as erro:
        logging.warning("Capa não gerada (%s): %s", item["id"], erro)
        raise HTTPException(
            503, "Não foi possível gerar a capa agora. Tente de novo em instantes."
        )
    except HTTPException:
        raise
    except Exception as erro:
        logging.warning("Falha ao gerar capa (%s): %s", item["id"], erro)
        raise HTTPException(503, "Não foi possível gerar a capa agora.")
    os.makedirs(config.PASTA_CAPAS, exist_ok=True)
    with open(caminho_capa(item["id"]), "wb") as arquivo:
        arquivo.write(imagem)
    return CapaOut(capa=True)


@app.get("/catalogo/{cenario_id}/capa")
def ver_capa(cenario_id: IdDoCenario, jogador=Depends(jogador_atual)):
    """Devolve a arte de capa de um cenário (se já foi gerada)."""
    item = catalogo.cenario(cenario_id)
    if item is None:
        raise HTTPException(404, "Cenário não encontrado.")
    caminho = caminho_capa(item["id"])
    if not os.path.exists(caminho):
        raise HTTPException(404, "Este cenário ainda não tem capa.")
    return FileResponse(
        caminho, media_type=tipo_de_imagem(caminho), headers={"Cache-Control": "public, max-age=86400"}
    )


@app.post("/jogos/{jogo_id}/turno", response_model=JogoOut)
def jogar_turno(dados: TurnoIn, jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Envie 'opcao' (número da opção da cena, 1 a 4) OU 'acao' (texto livre)."""
    if (dados.opcao is None) == (dados.acao is None):
        raise HTTPException(422, "Envie 'opcao' OU 'acao' (apenas um dos dois).")
    exigir_ritmo(jogador["id"])
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)

        # Reenvio do MESMO pedido (ex.: a rede caiu e o app tentou de novo): devolve
        # o resultado já aplicado, sem criar outro turno nem consumir outra jogada.
        if dados.pedido_id and estado.get("ultimo_pedido") == dados.pedido_id:
            return montar_saida(
                jogo_id, estado, bool(jogo["encerrado"]), jogadas_restantes(jogador["id"])
            )

        if jogo["encerrado"]:
            raise HTTPException(409, "Este personagem já morreu. Crie um novo jogo.")

        if dados.opcao is not None:
            opcoes = estado["cena"]["opcoes"]
            if dados.opcao > len(opcoes):
                raise HTTPException(422, "Opção inexistente nesta cena.")
            acao = opcoes[dados.opcao - 1]
        else:
            acao = motor.limpar_texto(dados.acao, config.MAX_TAMANHO_ACAO)
            if not acao:
                raise HTTPException(422, "Ação vazia.")

        restantes = verificar_limites(jogador["id"])
        # "escolha" = o jogador tocou numa das opções (em vez de escrever com as próprias
        # palavras): a IA também devolve a "situacao" (o gesto dele) para o chat.
        enviado_em = _agora_ms()
        resultado = chamar_ia(jogador["id"], estado, acao, escolha=dados.opcao is not None)
        morreu = motor.registrar_turno(estado, acao, resultado, enviado_em, _agora_ms())
        estado["ultimo_pedido"] = dados.pedido_id
        banco.atualizar_jogo(jogo_id, jogador["id"], motor.estado_para_texto(estado), morreu)
    return montar_saida(jogo_id, estado, morreu, restantes - 1)


@app.delete("/jogos/{jogo_id}", status_code=204)
def apagar_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    if not banco.apagar_jogo(jogo_id, jogador["id"]):
        raise HTTPException(404, "Jogo não encontrado.")
    return Response(status_code=204)


@app.post("/feedback", status_code=201)
def enviar_feedback(dados: FeedbackIn, jogador=Depends(jogador_atual)):
    if not limite_feedback.permitir(f"jogador:{jogador['id']}"):
        raise HTTPException(429, "Você já enviou bastante feedback. Obrigado! Tente mais tarde.")
    comentario = motor.limpar_texto(dados.comentario, 500) if dados.comentario else None
    banco.salvar_feedback(jogador["id"], dados.nota, comentario)
    return {"status": "recebido"}


@app.post("/denuncias", status_code=201)
def enviar_denuncia(dados: DenunciaIn, jogador=Depends(jogador_atual)):
    """Denuncia a cena atual de um jogo (conteúdo ofensivo, violento demais, sem sentido...)."""
    if not limite_feedback.permitir(f"jogador:{jogador['id']}"):
        raise HTTPException(429, "Você já enviou bastante feedback. Obrigado! Tente mais tarde.")
    jogo = banco.obter_jogo(dados.jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    detalhe = motor.limpar_texto(dados.detalhe, 300) if dados.detalhe else ""
    trecho = f"{detalhe}\n---\n{estado['cena']['narrativa']}"[:1500]
    banco.salvar_denuncia(jogador["id"], dados.jogo_id, dados.motivo, trecho)
    logging.info("Nova denúncia (%s) do jogador %s.", dados.motivo, jogador["id"])
    return {"status": "recebido"}