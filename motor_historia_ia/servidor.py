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
from fastapi.exceptions import RequestValidationError
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

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    import logging
    logging.error("VALIDATION 422 na rota %s: %s", request.url.path, exc.errors())
    return JSONResponse(status_code=422, content={"detail": exc.errors()})


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


def caminho_persona_retrato(jogador_id, persona_id=None):
    if persona_id:
        return os.path.join(config.PASTA_RETRATOS, f"persona_{jogador_id}_{persona_id}.png")
    return os.path.join(config.PASTA_RETRATOS, f"persona_{jogador_id}.png")


def tem_persona_retrato(jogador_id, persona_id=None):
    if persona_id:
        c1 = caminho_persona_retrato(jogador_id, persona_id)
        if os.path.exists(c1):
            return True
    return os.path.exists(caminho_persona_retrato(jogador_id))


# Lotes de candidatos a retrato de persona em memória
_lotes_candidatos = {}


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
        if os.path.exists(caminho_capa(cenario_id)):
            return True
        return catalogo.cenario(cenario_id) is not None
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
    codigo_convite: Optional[str] = Field(default=None, max_length=40)


class NovoJogoIn(Base):
    nome_personagem: str = Field(default="Viajante", min_length=1, max_length=100)
    persona_id: Optional[int] = None
    modo: str = Field(default="rpg", max_length=30)
    genero: str = Field(default="Aventura", max_length=100)
    premissa: Optional[str] = Field(default=None, max_length=3000)
    # Ficha do personagem (usada na história e para montar o retrato por IA).
    aparencia: Optional[str] = Field(default=None, max_length=3000)
    descricao: Optional[str] = Field(default=None, max_length=5000)
    idade: Optional[str] = Field(default=None, max_length=100)
    interesses: Optional[str] = Field(default=None, max_length=3000)
    aversoes: Optional[str] = Field(default=None, max_length=3000)
    tags: Optional[list[str]] = Field(default=None, max_length=50)
    # Vitrine: se vier um cenário oficial, o servidor injeta o prompt secreto dele
    # e ignora o gênero informado (cada cenário tem o seu).
    cenario_id: Optional[str] = Field(default=None, max_length=100)
    cenario_nome: Optional[str] = Field(default=None, max_length=100)
    cenario_prologo: Optional[str] = Field(default=None, max_length=3000)
    # Cérebro de IA escolhido (veja catalogo.CEREBROS). Sem escolha, usa o padrão.
    modelo_id: Optional[str] = Field(default=None, max_length=100)
    temperatura: Optional[float] = Field(default=None, ge=0.0, le=2.0)


class TurnoIn(Base):
    opcao: Optional[int] = Field(default=None, ge=1, le=4)
    acao: Optional[str] = Field(default=None, min_length=1, max_length=config.MAX_TAMANHO_ACAO)
    pedido_id: Optional[str] = Field(default=None, min_length=1, max_length=64, pattern=r"^[A-Za-z0-9_-]+$")


class ConfigModeloIn(Base):
    modelo_id: Optional[str] = Field(default=None, max_length=40)
    temperatura: Optional[float] = Field(default=None, ge=0.2, le=1.4)


class TrocarVersaoIn(Base):
    indice: int = Field(ge=0)
    versao_idx: int = Field(ge=0, le=9)


class EditarMensagemIn(Base):
    indice: int = Field(ge=0)
    narrativa: Optional[str] = Field(default=None, min_length=1, max_length=2000)
    acao: Optional[str] = Field(default=None, min_length=1, max_length=500)


class MemoriaIn(Base):
    memoria: Optional[str] = Field(default=None, max_length=2000)
    auto_resumir: bool = False


class SavepointIn(Base):
    nome: Optional[str] = Field(default=None, max_length=60)


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
    versoes: list[str] = []
    versao_ativa: int = 0
    eventos: list[EventoOut] = []
    # Horários (epoch em milissegundos): o chat mostra "enviado às 14:32" em cada fala.
    em_jogador: Optional[int] = None
    em_narrador: Optional[int] = None


class DiarioOut(BaseModel):
    id: str
    titulo: str
    conteudo: str
    humor: str = "Reflexivo"
    turno: int
    criado_em: Optional[int] = None
    lido: bool = False


class CartaOut(BaseModel):
    id: str
    remetente: str
    titulo: str
    conteudo: str
    turno: int
    criada_em: Optional[int] = None
    lida: bool = False


class SavepointOut(BaseModel):
    id: str
    nome: str
    turno: int
    local: str
    resumo: str = ""
    criado_em: Optional[int] = None


class AutoRespostaOut(BaseModel):
    sugestoes: list[str]


class MemoriaOut(BaseModel):
    memoria: str


class ResgateLivenOut(BaseModel):
    ganho: int
    livens: int
    mochis: int = 0
    ja_resgatado: bool = False

ResgateMochiOut = ResgateLivenOut


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
    custo_livens: int = 0
    custo_mochi: int = 0
    categoria: str = "Geral"


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
    temperatura: float = 0.85
    memoria: str = ""
    diarios: list[DiarioOut] = []
    cartas: list[CartaOut] = []
    savepoints: list[SavepointOut] = []
    livens: Optional[int] = None
    mochis: Optional[int] = None


class RetratoOut(BaseModel):
    retrato: bool = True


class PersonaIn(BaseModel):
    model_config = ConfigDict(extra="ignore", str_strip_whitespace=True)
    nome: str = Field(default="Viajante", min_length=1, max_length=100)
    genero: str = Field(default="Feminino", max_length=50)
    idade: Optional[str] = Field(default="", max_length=50)
    personalidade: Optional[str] = Field(default="", max_length=2000)
    descricao: Optional[str] = Field(default="", max_length=2000)
    interesses: Optional[str] = Field(default="", max_length=1000)
    aparencia: Optional[str] = Field(default="", max_length=2000)
    medos: Optional[str] = Field(default="", max_length=1000)
    aversoes: Optional[str] = Field(default="", max_length=1000)
    tags: Optional[list[str]] = Field(default_factory=list)
    detalhes_imagem: Optional[str] = Field(default="", max_length=1000)
    estilo_imagem: Optional[str] = Field(default="semi_realistic", max_length=100)
    avatar_emoji: Optional[str] = Field(default="👤", max_length=20)
    padrao: Optional[bool] = False


class PersonaOut(BaseModel):
    id: int
    jogador_id: Optional[str] = ""
    nome: str
    genero: str
    idade: Optional[str] = ""
    personalidade: Optional[str] = ""
    descricao: Optional[str] = ""
    interesses: Optional[str] = ""
    aparencia: Optional[str] = ""
    medos: Optional[str] = ""
    aversoes: Optional[str] = ""
    tags: list[str] = []
    detalhes_imagem: Optional[str] = ""
    estilo_imagem: Optional[str] = "semi_realistic"
    avatar_emoji: Optional[str] = "👤"
    retrato_versao: int = 1
    versao_retrato: int = 1
    padrao: bool = False
    tem_retrato: bool = False
    criado_em: Optional[str] = ""
    atualizado_em: Optional[str] = ""


class PersonaRetratoIn(BaseModel):
    model_config = ConfigDict(extra="ignore", str_strip_whitespace=True)
    nome: Optional[str] = ""
    genero: Optional[str] = "Feminino"
    aparencia: Optional[str] = ""
    descricao: Optional[str] = ""
    personalidade: Optional[str] = ""
    idade: Optional[str] = ""
    interesses: Optional[str] = ""
    aversoes: Optional[str] = ""
    medos: Optional[str] = ""
    tags: list[str] = []
    estilo_imagem: Optional[str] = "semi_realistic"
    detalhes_imagem: Optional[str] = ""


class CandidatoRetratoOut(BaseModel):
    indice: int
    seed: int
    url: str
    url_direta: str


class CandidatosRetratoResponse(BaseModel):
    lote_id: str
    candidatos: list[CandidatoRetratoOut]


class ConfirmarCandidatoIn(BaseModel):
    lote_id: str
    indice: int
    seed: Optional[int] = None


class UploadRetratoIn(BaseModel):
    imagem_base64: str


class TrocarPersonaJogoIn(BaseModel):
    persona_id: int


class AvatarOut(BaseModel):
    avatar_seed: int


class CenarioOut(BaseModel):
    """Um cenário da vitrine: o que o app mostra (o prompt secreto fica no servidor)."""
    id: str
    nome: str
    subtitulo: str = ""
    emoji: str = "📖"
    genero: str = "Aventura"
    tags: list[str] = []
    sinopse: str = ""
    abertura: str = ""
    # True quando a arte de capa já foi gerada e está salva no servidor.
    capa: bool = False
    criadoPorUsuario: bool = False
    autorNome: str = "LivennIA Studios"
    autorAvatar: str = "🤖"
    chats: int = 0
    saves: int = 0
    modoJogo: str = "historia"


class NovoCenarioIn(BaseModel):
    model_config = ConfigDict(extra="ignore", str_strip_whitespace=True)
    id: Optional[str] = None
    nome: str
    subtitulo: Optional[str] = ""
    emoji: Optional[str] = "🎭"
    genero: Optional[str] = "Aventura"
    tags: list[str] = []
    sinopse: Optional[str] = ""
    abertura: Optional[str] = ""
    prompt_secreto: Optional[str] = ""
    estilo_imagem: Optional[str] = "semi_realistic"
    modo_jogo: Optional[str] = "historia"


class CerebroOut(BaseModel):
    id: str
    nome: str
    emoji: str
    descricao: str
    premium: bool = False
    custo_livens: int = 0
    custo_mochi: int = 0
    categoria: str = "Geral"
    temperatura_padrao: float = 0.85
    # False = o jogador não pode escolher este modelo agora (é premium e sem Livens suficientes).
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
    livens: int = 100
    mochis: int = 100
    resgate_diario_disponivel: bool = True
    persona_retrato: bool = False
    dono: bool = False


class ComentarioIn(BaseModel):
    model_config = ConfigDict(extra="ignore", str_strip_whitespace=True)
    texto: str = Field(min_length=1, max_length=500)


class ComentarioOut(BaseModel):
    id: int
    cenario_id: str
    jogador_id: int
    autor_nome: str
    autor_avatar: str = "👤"
    texto: str
    likes: int = 0
    meu_like: bool = False
    meu_comentario: bool = False
    criado_em: str
    editado_em: Optional[str] = None


class LikeComentarioOut(BaseModel):
    likes: int
    meu_like: bool


class PerfilCriadorOut(BaseModel):
    nome: str
    avatar: str = "🎭"
    uid: str
    chats: int = 0
    saves: int = 0
    seguidores: int = 0
    seguindo: int = 1
    esta_seguindo: bool = False
    esta_bloqueado: bool = False
    historias: list[CenarioOut] = []


class SeguirOut(BaseModel):
    esta_seguindo: bool
    total_seguidores: int


class DenunciaCriadorIn(BaseModel):
    model_config = ConfigDict(extra="ignore", str_strip_whitespace=True)
    motivo: str = Field(min_length=1, max_length=300)
    trecho: Optional[str] = None


IdDoJogo = Annotated[str, Path(pattern=r"^[0-9a-f]{32}$")]
IdDoCenario = Annotated[str, Path(pattern=r"^[a-zA-Z0-9_-]{2,64}$")]

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


def _resumo_savepoints(estado):
    return [
        SavepointOut(
            id=s["id"],
            nome=s["nome"],
            turno=s["turno"],
            local=s["local"],
            resumo=s.get("resumo") or "",
            criado_em=s.get("criado_em"),
        )
        for s in (estado.get("savepoints") or [])
    ]


def montar_saida(jogo_id, estado, encerrado, restantes, completo=False, mochis=None):
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
                id=modelo["id"],
                nome=modelo["nome"],
                emoji=modelo.get("emoji") or "",
                custo_livens=int(modelo.get("custo_livens") or modelo.get("custo_mochi") or 0),
                custo_mochi=int(modelo.get("custo_mochi") or 0),
                categoria=modelo.get("categoria") or "Geral",
            )
            if modelo
            else None
        ),
        temperatura=float(estado.get("temperatura") or 0.85),
        memoria=estado.get("memoria") or "",
        diarios=[DiarioOut(**d) for d in (estado.get("diarios") or [])],
        cartas=[CartaOut(**c) for c in (estado.get("cartas") or [])],
        savepoints=_resumo_savepoints(estado),
        livens=mochis,
        mochis=mochis,
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
    """A vitrine da aba Explorar: cenários da comunidade + cenários oficiais."""
    itens = []
    # 1. Cenários criados por usuários (mais recentes primeiro)
    for c_user in banco.listar_cenarios_usuario():
        itens.append(CenarioOut(
            id=c_user["id"],
            nome=c_user["nome"],
            subtitulo=c_user.get("subtitulo") or "",
            emoji=c_user.get("emoji") or "🎭",
            genero=c_user.get("genero") or "Aventura",
            tags=list(c_user.get("tags") or []),
            sinopse=c_user.get("sinopse") or "",
            abertura=c_user.get("abertura") or "",
            capa=tem_capa(c_user["id"]),
            criadoPorUsuario=True,
            autorNome=c_user.get("autor_nome") or c_user.get("autorNome") or "Comunidade",
            autorAvatar=c_user.get("autorAvatar") or c_user.get("emoji") or "🎭",
            chats=int(c_user.get("chats") or 0),
            saves=int(c_user.get("saves") or 0),
            modoJogo=c_user.get("modo_jogo") or c_user.get("modoJogo") or "historia",
        ))
    # 2. Cenários oficiais do catálogo
    for item in catalogo.CENARIOS:
        publico = catalogo.cenario_publico(item)
        itens.append(CenarioOut(**publico, capa=tem_capa(item["id"])))
    return itens


@app.post("/catalogo/cenarios", response_model=CenarioOut, status_code=201)
def criar_cenario_comunidade(dados: NovoCenarioIn, jogador=Depends(jogador_atual)):
    """Salva uma nova história/personagem criada por usuário no banco permanente."""
    c_id = dados.id or f"cenario_{int(time.time()*1000)}"
    autor_nome = jogador.get("apelido") or "Comunidade"
    banco.salvar_cenario_usuario(
        cenario_id=c_id,
        autor_id=jogador["id"],
        autor_nome=autor_nome,
        nome=dados.nome,
        subtitulo=dados.subtitulo or "",
        emoji=dados.emoji or "🎭",
        genero=dados.genero or "Aventura",
        tags=dados.tags or [],
        sinopse=dados.sinopse or "",
        abertura=dados.abertura or "",
        prompt_secreto=dados.prompt_secreto or "",
        estilo_imagem=dados.estilo_imagem or "semi_realistic",
        modo_jogo=dados.modo_jogo or "historia",
    )
    c_salvo = banco.obter_cenario_usuario(c_id)
    return CenarioOut(
        id=c_salvo["id"],
        nome=c_salvo["nome"],
        subtitulo=c_salvo.get("subtitulo") or "",
        emoji=c_salvo.get("emoji") or "🎭",
        genero=c_salvo.get("genero") or "Aventura",
        tags=list(c_salvo.get("tags") or []),
        sinopse=c_salvo.get("sinopse") or "",
        abertura=c_salvo.get("abertura") or "",
        capa=tem_capa(c_salvo["id"]),
        criadoPorUsuario=True,
        autorNome=c_salvo.get("autorNome") or autor_nome,
        autorAvatar=c_salvo.get("autorAvatar") or c_salvo.get("emoji") or "🎭",
        chats=int(c_salvo.get("chats") or 0),
        saves=int(c_salvo.get("saves") or 0),
        modoJogo=c_salvo.get("modoJogo") or "historia",
    )


@app.get("/cerebros", response_model=list[CerebroOut])
def listar_cerebros(jogador=Depends(jogador_atual)):
    """Os "cérebros"/sabores de IA (LivennIA), com custo em Livens e categoria."""
    eh_premium = bool(jogador.get("premium"))
    mochis = int(jogador.get("mochis") or 0)
    return [
        CerebroOut(
            id=item["id"],
            nome=item["nome"],
            emoji=item["emoji"],
            descricao=item["descricao"],
            premium=bool(item["premium"]),
            custo_livens=int(item.get("custo_livens") or item.get("custo_mochi") or 0),
            custo_mochi=int(item.get("custo_mochi") or 0),
            categoria=item.get("categoria") or "Geral",
            temperatura_padrao=float(item.get("temperatura_padrao") or 0.85),
            liberado=(not item["premium"]) or eh_premium or (mochis >= int(item.get("custo_mochi") or 0)),
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
    """Troca um ID token do Firebase por um token de sessão do LivennIA, vinculando convite na primeira vez."""
    if not _FIREBASE_DISPONIVEL:
        raise HTTPException(503, "Autenticação por conta não está disponível no servidor.")
    try:
        decodificado = firebase_auth.verify_id_token(dados.id_token)
    except Exception as erro:
        logging.warning("Erro verify_id_token: %s", erro)
        raise HTTPException(401, "Token do Firebase inválido ou expirado.")
    uid = decodificado.get("uid")
    email = decodificado.get("email") or ""
    nome = decodificado.get("name") or (email.split("@")[0] if email else "Viajante")
    nome = motor.limpar_texto(nome, 20) or "Viajante"

    eh_dono_email = bool(
        email and (
            "alisson" in email.lower()
            or "dono" in email.lower()
            or "camila" in email.lower()
            or email.lower().strip() == "334camila@gmail.com"
        )
    )

    # 1. Procura jogador já existente pelo uid do Firebase ou pelo email
    with banco.conexao() as c:
        linha = c.execute(
            "SELECT id, apelido, token_hash, dono, premium FROM jogadores WHERE convite = ? OR (email IS NOT NULL AND email != '' AND LOWER(email) = LOWER(?))",
            (f"firebase:{uid}", email)
        ).fetchone()

    if linha is not None:
        # CONTA GOOGLE JÁ VINCULADA! Reemite token e entra imediatamente, sem código!
        token = gerar_token()
        eh_dono = bool(linha["dono"] or eh_dono_email or linha["id"] == 1 or linha["apelido"].lower() in {"alisson", "camila", "mila"})
        with banco.transacao() as c:
            c.execute(
                "UPDATE jogadores SET token_hash = ?, convite = ?, email = ?, dono = ?, premium = ?, mochis = ? WHERE id = ?",
                (hash_token(token), f"firebase:{uid}", email, 1 if eh_dono else linha["dono"], 1 if eh_dono else linha["premium"], 999999 if eh_dono else 100, linha["id"])
            )
        return RegistroOut(jogador_id=linha["id"], apelido=linha["apelido"], token=token, recuperado=True)

    # 2. Primeira vez que essa conta Google faz login:
    codigo = (dados.codigo_convite or "").strip().upper()
    if not codigo:
        if eh_dono_email:
            eh_dono = True
        else:
            raise HTTPException(403, "convite_necessario")
    else:
        eh_codigo_dono = codigo in {"LIV-DONO-2026", "LIV-ALISSON-2026", "LIVENNIA2026", "LIV-CAMILA-ADMIN"}
        with banco.transacao() as c:
            convite = c.execute(
                "SELECT usos, usos_max, ativo, nota FROM convites WHERE codigo = ?", (codigo,)
            ).fetchone()
            if eh_codigo_dono or (convite and convite.get("nota") == "dono") or eh_dono_email:
                eh_dono = True
            else:
                eh_dono = False
                if convite is None or not convite["ativo"] or convite["usos"] >= convite["usos_max"]:
                    raise HTTPException(403, "Código de convite inválido ou já utilizado.")
                c.execute("UPDATE convites SET usos = usos + 1 WHERE codigo = ?", (codigo,))

    token = gerar_token()
    with banco.transacao() as c:
        nome_final = "Camila" if ("camila" in email.lower() or "camila" in nome.lower()) else nome
        existente = c.execute(
            "SELECT id, apelido, banido, dono FROM jogadores WHERE LOWER(apelido) = LOWER(?) OR (email IS NOT NULL AND LOWER(email) = LOWER(?)) ORDER BY id ASC LIMIT 1",
            (nome_final, email)
        ).fetchone()

        if existente is not None:
            c.execute(
                "UPDATE jogadores SET token_hash = ?, convite = ?, email = ?, dono = 1, premium = 1, mochis = 999999 WHERE id = ?",
                (hash_token(token), f"firebase:{uid}", email, existente["id"])
            )
            return RegistroOut(jogador_id=existente["id"], apelido=existente["apelido"], token=token, recuperado=True)

        cursor = c.execute(
            "INSERT INTO jogadores (apelido, token_hash, convite, email, dono, premium, mochis, criado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            (nome_final, hash_token(token), f"firebase:{uid}", email, 1 if eh_dono else 0, 1 if eh_dono else 0, 999999 if eh_dono else 100, banco.agora())
        )
        jogador_id = cursor.lastrowid
        return RegistroOut(jogador_id=jogador_id, apelido=nome_final, token=token, recuperado=False)

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
    eh_dono = bool(jogador.get("dono"))
    restantes = 999999 if eh_dono else jogadas_restantes(jogador["id"])
    hoje_str = banco.hoje()
    return ContaOut(
        apelido=jogador["apelido"],
        limite_diario=999999 if eh_dono else config.LIMITE_DIARIO_JOGADAS,
        jogadas_hoje=0 if eh_dono else max(0, config.LIMITE_DIARIO_JOGADAS - restantes),
        jogadas_restantes_hoje=restantes,
        jogos_ativos=banco.contar_jogos_ativos(jogador["id"]),
        max_jogos=999999 if eh_dono else config.MAX_JOGOS_POR_JOGADOR,
        premium=True if eh_dono else bool(jogador.get("premium")),
        livens=999999 if eh_dono else int(jogador.get("mochis") or 0),
        mochis=999999 if eh_dono else int(jogador.get("mochis") or 0),
        resgate_diario_disponivel=True,
        persona_retrato=tem_persona_retrato(jogador["id"]),
        dono=eh_dono,
    )


@app.post("/carteira/resgatar-diario", response_model=ResgateLivenOut)
def resgatar_livens_diarios(jogador=Depends(jogador_atual)):
    """Resgata o bônus diário de +30 Livens."""
    saldo = banco.resgatar_livens_diarios(jogador["id"], bonus=30)
    ganho = 30
    return ResgateLivenOut(ganho=ganho, livens=saldo, mochis=saldo, ja_resgatado=(ganho == 0))

resgatar_livens_diarios = resgatar_livens_diarios


@app.delete("/conta", status_code=204)
def apagar_conta(jogador=Depends(jogador_atual)):
    """Apaga a conta e todos os dados do jogador (direito de exclusão da LGPD)."""
    banco.apagar_jogador(jogador["id"])
    logging.info("Conta %s apagada pelo próprio jogador.", jogador["id"])
    return Response(status_code=204)


def _cobrar_livens_do_modelo(jogador, cerebro_dict):
    """Se o modelo tiver custo em Livens e o jogador não for VIP, debita os Livens."""
    saldo_atual = int(jogador.get("mochis") or 0)
    if not cerebro_dict:
        return saldo_atual
    custo = int(cerebro_dict.get("custo_livens") or cerebro_dict.get("custo_mochi") or 0)
    if custo <= 0 or jogador.get("premium"):
        return saldo_atual
    try:
        return banco.debitar_livens(jogador["id"], custo)
    except banco.ErroNegocio as erro:
        raise HTTPException(erro.status, erro.mensagem)

_cobrar_livens_do_modelo = _cobrar_livens_do_modelo


@app.post("/jogos", response_model=JogoOut, status_code=201)
def criar_jogo(dados: NovoJogoIn, jogador=Depends(jogador_atual)):
    """Cria um personagem e já joga o primeiro turno (conta como 1 jogada)."""
    exigir_ritmo(jogador["id"])
    with uma_jogada_por_vez(jogador["id"]):
        eh_dono = bool(
            jogador.get("dono")
            or (jogador.get("apelido") or "").lower() in {"alisson", "camila", "mila"}
            or (jogador.get("email") or "").lower() in {"334camila@gmail.com"}
            or jogador.get("apelido") in config.ADMINS
            or jogador.get("email") in config.ADMINS
        )
        limite_jogos = 999999 if (eh_dono or jogador.get("premium")) else 20
        if banco.contar_jogos_ativos(jogador["id"]) >= limite_jogos:
            raise HTTPException(
                409, f"Limite de {limite_jogos} histórias ativas atingido. Apague uma para criar outra."
            )
        restantes = verificar_limites(jogador["id"])
        # Vitrine: se o jogador tocou num card da aba Explorar, o cenário oficial traz
        # o gênero, a abertura e o PROMPT SECRETO do personagem.
        item = None
        if dados.cenario_id or dados.cenario_nome:
            if dados.cenario_id:
                item = catalogo.cenario(dados.cenario_id)
            # Cenários criados por usuários ou comunidade usam a sinopse e premissa enviadas
            if item is None and (dados.cenario_nome or dados.cenario_id):
                nome_bot = dados.cenario_nome or dados.cenario_id
                prologo_bot = dados.cenario_prologo or dados.premissa or ""
                prompt_bot = (
                    f"IDENTIDADE DO BOT NESTA HISTÓRIA:\n"
                    f"- VOCÊ É E INTERPRETA O PERSONAGEM: {nome_bot}\n"
                    f"- SEU PRÓLOGO / QUEM VOCÊ É:\n{prologo_bot}\n"
                    f"- Você fala, pensa e age como {nome_bot}. Mantenha fidelidade a essa persona em todos os diálogos e ações.\n"
                    f"- O usuário é a Persona do jogador interagindo diretamente com você."
                )
                item = {
                    "id": dados.cenario_id or f"cenario_custom_{int(time.time())}",
                    "nome": nome_bot,
                    "genero": dados.genero or "Aventura",
                    "sinopse": prologo_bot,
                    "abertura": dados.premissa or f"*{nome_bot} olha em sua direção e se aproxima.*",
                    "prompt_secreto": prompt_bot,
                    "emoji": "🎭"
                }
        cerebro = None
        if dados.modelo_id:
            cerebro = catalogo.cerebro(dados.modelo_id)
            if cerebro is None:
                raise HTTPException(422, "Modelo de IA desconhecido.")
            custo_m = int(cerebro.get("custo_mochi") or 0)
            if cerebro["premium"] and not jogador.get("premium") and int(jogador.get("mochis") or 0) < custo_m:
                raise HTTPException(
                    402, f"Este modelo custa {custo_m} Livens ✨ por turno. Resgate seu bônus diário ou escolha um modelo gratuito."
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
        persona = None
        if dados.persona_id:
            persona = banco.obter_persona(jogador["id"], dados.persona_id)
        if not persona:
            persona = banco.obter_persona_padrao(jogador["id"])

        if item:
            try:
                banco.incrementar_chat_cenario_usuario(item["id"])
            except Exception:
                pass

        if item and dados.modo == "historia":
            # No Modo História, o Bot é o personagem do cenário!
            nome_usado = item["nome"]
        else:
            nome_usado = dados.nome_personagem
            if persona and (not dados.nome_personagem or dados.nome_personagem == "Viajante"):
                nome_usado = persona["nome"]

        premissa = dados.premissa or (item["sinopse"] if item else None)
        estado = motor.novo_estado(
            nome_usado,
            genero,
            premissa,
            ficha,
            cenario=item,
            cerebro=cerebro,
            modo=dados.modo,
            temperatura=dados.temperatura,
        )
        if persona:
            estado["persona"] = persona

        if item and item.get("abertura"):
            # Modo História com prólogo / abertura definida:
            # O primeiro turno no chat É A FALA DE ABERTURA DO BOT!
            # Sem chamada síncrona pesada à IA (início instantâneo em 0ms, sem falhas de cota ou rede).
            abertura_bot = item["abertura"]
            resultado = motor.ResultadoIA(
                narrativa=abertura_bot,
                opcoes=["Responder...", "Aproximar-se", "Observar em silêncio"],
                mudancas={},
                modelo=(cerebro.get("id") if cerebro else "abertura_catalogo"),
                tokens_entrada=0,
                tokens_saida=0,
                segundos=0.01,
                tentativas=1,
                situacao=None,
            )
            morreu = motor.registrar_turno(
                estado, "", resultado, _agora_ms(), _agora_ms()
            )
            saldo_livens = int(jogador.get("mochis") or 0)
        else:
            acao_inicial = motor.abertura_de(genero)
            resultado = chamar_ia(jogador["id"], estado, acao_inicial)
            saldo_livens = _cobrar_livens_do_modelo(jogador, estado.get("modelo"))
            morreu = motor.registrar_turno(
                estado, acao_inicial, resultado, _agora_ms(), _agora_ms()
            )
        jogo_id = uuid.uuid4().hex
        banco.criar_jogo(jogo_id, jogador["id"], motor.estado_para_texto(estado), morreu)
    return montar_saida(jogo_id, estado, morreu, restantes - 1, mochis=saldo_livens)


@app.get("/jogos", response_model=list[JogoResumoOut])
def listar_jogos(jogador=Depends(jogador_atual)):
    resumos = []
    for jogo in banco.listar_jogos(jogador["id"]):
        try:
            estado = carregar_estado_do_jogo(jogo)
            p = estado["personagem"]
            cenario = estado.get("cenario")
            cena = estado.get("cena") or {}
            ultima = (cena.get("narrativa") or "")[:140] or None
            resumos.append(JogoResumoOut(
                jogo_id=jogo["id"],
                modo=estado.get("modo", "rpg"),
                avatar_seed=estado["avatar_seed"],
                nome=p.get("nome", "Viajante"),
                turno=estado.get("turno", 0),
                local=p.get("local", "Cenário"),
                vida=p.get("vida", 100),
                vida_max=p.get("vida_max", 100),
                encerrado=bool(jogo["encerrado"]),
                genero=estado.get("genero", "Aventura"),
                atualizado_em=jogo["atualizado_em"],
                retrato=tem_retrato(jogo["id"]),
                cenario_nome=(cenario["nome"] if cenario else None),
                capa=bool(cenario and tem_capa(cenario["id"])),
                ultima_narrativa=ultima,
            ))
        except Exception as e:
            logging.exception("Falha ao carregar jogo %s na listagem: %s", jogo.get("id"), e)
            continue
    return resumos


@app.get("/jogos/{jogo_id}", response_model=JogoOut)
def ver_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    return montar_saida(
        jogo_id,
        estado,
        jogo["encerrado"],
        jogadas_restantes(jogador["id"]),
        completo=True,
        mochis=int(jogador.get("mochis") or 0),
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

@app.post("/conta/persona/retrato", response_model=RetratoOut)
def gerar_retrato_da_persona(dados: PersonaRetratoIn, jogador=Depends(jogador_atual)):
    """Gera o retrato de IA da persona do usuário com base nas suas características."""
    if not limite_retrato.permitir(f"persona_retrato:{jogador['id']}"):
        raise HTTPException(429, "Calma! Aguarde um pouco para gerar outro retrato.")
    consumir_retrato_do_dia()
    estado_persona = {
        "personagem": {"nome": dados.nome or jogador.get("apelido", "Viajante")},
        "genero": dados.genero or "Aventura",
        "ficha": {
            "aparencia": dados.aparencia,
            "descricao": dados.descricao,
            "idade": dados.idade,
            "interesses": dados.interesses,
            "aversoes": dados.aversoes,
            "tags": dados.tags,
        }
    }
    try:
        imagem = motor.gerar_retrato(estado_persona)
    except motor.ErroIA as erro:
        logging.warning("Falha ao gerar retrato da persona: %s", erro)
        raise HTTPException(
            503, "Não foi possível gerar a imagem da persona agora. Tente de novo em instantes."
        )
    os.makedirs(config.PASTA_RETRATOS, exist_ok=True)
    caminho = caminho_persona_retrato(jogador["id"])
    with open(caminho, "wb") as arquivo:
        arquivo.write(imagem)
    return RetratoOut(retrato=True)


@app.get("/conta/persona/retrato")
def ver_retrato_da_persona(token: Optional[str] = None, credenciais: Optional[HTTPAuthorizationCredentials] = Depends(esquema_bearer)):
    """Devolve a imagem de perfil da persona gerada por IA."""
    jogador = None
    if credenciais and credenciais.credentials:
        jogador = banco.jogador_por_token_hash(hash_token(credenciais.credentials))
    elif token:
        jogador = banco.jogador_por_token_hash(hash_token(token))
    
    if jogador is None:
        raise HTTPException(401, "Não autenticado.")

    caminho = caminho_persona_retrato(jogador["id"])
    if not os.path.exists(caminho):
        raise HTTPException(404, "Esta persona ainda não possui retrato gerado.")
    return FileResponse(
        caminho, media_type=tipo_de_imagem(caminho), headers={"Cache-Control": "no-cache"}
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
def ver_capa(cenario_id: IdDoCenario):
    """Devolve a arte de capa de um cenário (se já foi gerada ou fallback de estilo)."""
    item = catalogo.cenario(cenario_id)
    if item is None:
        raise HTTPException(404, "Cenário não encontrado.")
    caminho = caminho_capa(item["id"])
    if not os.path.exists(caminho):
        # Tenta gerar e salvar no disco
        try:
            os.makedirs(config.PASTA_CAPAS, exist_ok=True)
            imagem = motor.gerar_capa(item)
            with open(caminho, "wb") as f:
                f.write(imagem)
        except Exception:
            # Fallback seguro para amostra do estilo de arte
            estilo = str(item.get("estilo_imagem") or item.get("genero") or "semi_realistic").lower()
            mapa = {
                "anime": "vibrant_anime",
                "realista": "photorealistic",
                "cyberpunk": "dark_fantasy",
            }
            nome_arq = f"{mapa.get(estilo, estilo)}.jpg"
            pasta_ex = os.path.join(os.path.dirname(__file__), "dados", "estilos_exemplos")
            caminho_ex = os.path.join(pasta_ex, nome_arq)
            if not os.path.exists(caminho_ex):
                caminho_ex = os.path.join(pasta_ex, "semi_realistic.jpg")
            if os.path.exists(caminho_ex):
                return FileResponse(caminho_ex, media_type="image/jpeg", headers={"Cache-Control": "public, max-age=86400"})
            raise HTTPException(404, "Este cenário ainda não tem capa.")
    return FileResponse(
        caminho, media_type=tipo_de_imagem(caminho), headers={"Cache-Control": "public, max-age=86400"}
    )

@app.post("/conta/persona/retrato", response_model=RetratoOut)
def gerar_retrato_da_persona(dados: PersonaRetratoIn, jogador=Depends(jogador_atual)):
    """Gera o retrato de IA da persona do usuário com base nas suas características."""
    if not limite_retrato.permitir(f"persona_retrato:{jogador['id']}"):
        raise HTTPException(429, "Calma! Aguarde um pouco para gerar outro retrato.")
    consumir_retrato_do_dia()
    estado_persona = {
        "personagem": {"nome": dados.nome or jogador.get("apelido", "Viajante")},
        "genero": dados.genero or "Aventura",
        "ficha": {
            "aparencia": dados.aparencia,
            "descricao": dados.descricao,
            "idade": dados.idade,
            "interesses": dados.interesses,
            "aversoes": dados.aversoes,
            "tags": dados.tags,
        }
    }
    try:
        imagem = motor.gerar_retrato(estado_persona)
    except motor.ErroIA as erro:
        logging.warning("Falha ao gerar retrato da persona: %s", erro)
        raise HTTPException(
            503, "Não foi possível gerar a imagem da persona agora. Tente de novo em instantes."
        )
    os.makedirs(config.PASTA_RETRATOS, exist_ok=True)
    caminho = caminho_persona_retrato(jogador["id"])
    with open(caminho, "wb") as arquivo:
        arquivo.write(imagem)
    return RetratoOut(retrato=True)


@app.get("/conta/persona/retrato")
def ver_retrato_da_persona(token: Optional[str] = None, credenciais: Optional[HTTPAuthorizationCredentials] = Depends(esquema_bearer)):
    """Devolve a imagem de perfil da persona gerada por IA."""
    jogador = None
    if credenciais and credenciais.credentials:
        jogador = banco.jogador_por_token_hash(hash_token(credenciais.credentials))
    elif token:
        jogador = banco.jogador_por_token_hash(hash_token(token))
    
    if jogador is None:
        raise HTTPException(401, "Não autenticado.")

    caminho = caminho_persona_retrato(jogador["id"])
    if not os.path.exists(caminho):
        raise HTTPException(404, "Esta persona ainda não possui retrato gerado.")
    return FileResponse(
        caminho, media_type=tipo_de_imagem(caminho), headers={"Cache-Control": "no-cache"}
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
                jogo_id,
                estado,
                bool(jogo["encerrado"]),
                jogadas_restantes(jogador["id"]),
                mochis=int(jogador.get("mochis") or 0),
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
        modelo_atual = estado.get("modelo") or {}
        custo_m = int(modelo_atual.get("custo_mochi") or 0)
        if custo_m > 0 and not jogador.get("premium") and int(jogador.get("mochis") or 0) < custo_m:
            raise HTTPException(
                402, f"Livens insuficientes ({custo_m} 🍡 necessários para {modelo_atual.get('nome', 'este modelo')}). Troque para Liven Flash (grátis) ou resgate Livens."
            )
        # "escolha" = o jogador tocou numa das opções (em vez de escrever com as próprias
        # palavras): a IA também devolve a "situacao" (o gesto dele) para o chat.
        enviado_em = _agora_ms()
        resultado = chamar_ia(jogador["id"], estado, acao, escolha=dados.opcao is not None)
        saldo_livens = _cobrar_livens_do_modelo(jogador, modelo_atual)
        morreu = motor.registrar_turno(estado, acao, resultado, enviado_em, _agora_ms())
        estado["ultimo_pedido"] = dados.pedido_id
        banco.atualizar_jogo(jogo_id, jogador["id"], motor.estado_para_texto(estado), morreu)
    return montar_saida(jogo_id, estado, morreu, restantes - 1, mochis=saldo_livens)


# ---------------------------------------------------------------
# Rotas do LivennIA (Regenerar, Versões, Fix Reply,
# Auto-Resposta, Memória, Diário, Cartas de Memória e Savepoints)
# ---------------------------------------------------------------
@app.post("/jogos/{jogo_id}/config-modelo", response_model=JogoOut)
def configurar_modelo_do_jogo(dados: ConfigModeloIn, jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Altera o sabor de modelo de IA e/ou a temperatura criativa no meio da história."""
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        if dados.modelo_id is not None:
            cerebro = catalogo.cerebro(dados.modelo_id)
            if cerebro is None:
                raise HTTPException(422, "Modelo de IA desconhecido.")
            custo_m = int(cerebro.get("custo_mochi") or 0)
            if cerebro["premium"] and not jogador.get("premium") and int(jogador.get("mochis") or 0) < custo_m:
                raise HTTPException(
                    402, f"Você precisa de pelo menos {custo_m} Livens ✨ para ativar {cerebro['nome']}."
                )
            estado["modelo"] = motor._cerebro_guardado(cerebro)
            if dados.temperatura is None:
                estado["temperatura"] = float(cerebro.get("temperatura_padrao", 0.85))
        if dados.temperatura is not None:
            estado["temperatura"] = motor._temperatura_valida(dados.temperatura)
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return montar_saida(
        jogo_id,
        estado,
        bool(jogo["encerrado"]),
        jogadas_restantes(jogador["id"]),
        completo=True,
        mochis=int(jogador.get("mochis") or 0),
    )


@app.post("/jogos/{jogo_id}/regenerar", response_model=JogoOut)
def regenerar_resposta(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Regenera a última fala do narrador e guarda a nova versão (< 1/N >)."""
    exigir_ritmo(jogador["id"])
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        if jogo["encerrado"]:
            raise HTTPException(409, "Este jogo já foi encerrado.")
        estado = carregar_estado_do_jogo(jogo)
        restantes = verificar_limites(jogador["id"])
        modelo_atual = estado.get("modelo") or {}
        custo_m = int(modelo_atual.get("custo_mochi") or 0)
        if custo_m > 0 and not jogador.get("premium") and int(jogador.get("mochis") or 0) < custo_m:
            raise HTTPException(
                402, f"Livens insuficientes ({custo_m} 🍡 necessários)."
            )
        inicio = time.perf_counter()
        try:
            resultado = motor.regenerar_ultimo_turno(estado, em_narrador=_agora_ms())
        except ValueError as erro:
            raise HTTPException(422, str(erro))
        except motor.ErroIA as erro:
            banco.registrar_uso(
                jogador["id"], False, None, erro.tokens_entrada, erro.tokens_saida,
                time.perf_counter() - inicio, erro.tentativas,
            )
            if erro.bloqueada:
                raise HTTPException(422, "O filtro de segurança bloqueou a nova versão.")
            raise HTTPException(503, "Não foi possível regenerar a resposta agora.")
        banco.registrar_uso(
            jogador["id"], True, resultado.modelo, resultado.tokens_entrada, resultado.tokens_saida,
            resultado.segundos, resultado.tentativas,
        )
        saldo_livens = _cobrar_livens_do_modelo(jogador, modelo_atual)
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return montar_saida(
        jogo_id,
        estado,
        bool(jogo["encerrado"]),
        restantes - 1,
        completo=True,
        mochis=saldo_livens,
    )


@app.post("/jogos/{jogo_id}/versao", response_model=JogoOut)
def alternar_versao_mensagem(dados: TrocarVersaoIn, jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Alterna entre as versões geradas de uma mensagem (< 1/N >)."""
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        try:
            motor.trocar_versao_mensagem(estado, dados.indice, dados.versao_idx)
        except ValueError as erro:
            raise HTTPException(422, str(erro))
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return montar_saida(
        jogo_id,
        estado,
        bool(jogo["encerrado"]),
        jogadas_restantes(jogador["id"]),
        completo=True,
        mochis=int(jogador.get("mochis") or 0),
    )


@app.patch("/jogos/{jogo_id}/mensagem", response_model=JogoOut)
def editar_mensagem_do_jogo(dados: EditarMensagemIn, jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Edita/corrige diretamente a fala do narrador (Fix Reply) ou do jogador."""
    if dados.narrativa is None and dados.acao is None:
        raise HTTPException(422, "Informe a nova narrativa ou a nova ação.")
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        try:
            motor.editar_mensagem(estado, dados.indice, dados.narrativa, dados.acao)
        except ValueError as erro:
            raise HTTPException(422, str(erro))
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return montar_saida(
        jogo_id,
        estado,
        bool(jogo["encerrado"]),
        jogadas_restantes(jogador["id"]),
        completo=True,
        mochis=int(jogador.get("mochis") or 0),
    )


@app.post("/jogos/{jogo_id}/auto-resposta", response_model=AutoRespostaOut)
def sugerir_auto_respostas(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Gera 3 sugestões criativas de fala/ação em 1ª pessoa para inspirar o jogador."""
    exigir_ritmo(jogador["id"])
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    try:
        sugestoes = motor.gerar_auto_respostas(estado)
    except motor.ErroIA as erro:
        raise HTTPException(503, str(erro))
    return AutoRespostaOut(sugestoes=sugestoes)


@app.get("/jogos/{jogo_id}/memoria", response_model=MemoriaOut)
def ver_memoria_do_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    return MemoriaOut(memoria=estado.get("memoria") or "")


@app.patch("/jogos/{jogo_id}/memoria", response_model=MemoriaOut)
def atualizar_memoria_do_jogo(dados: MemoriaIn, jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Salva a Memória de Longo Prazo editada pelo jogador ou resume automaticamente via IA."""
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        if dados.auto_resumir:
            try:
                nova = motor.gerar_resumo_memoria(estado)
            except motor.ErroIA as erro:
                raise HTTPException(503, str(erro))
        else:
            nova = (dados.memoria or "").strip()[:2000]
            estado["memoria"] = nova
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return MemoriaOut(memoria=nova)


@app.get("/jogos/{jogo_id}/diarios", response_model=list[DiarioOut])
def listar_diarios_do_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    return [DiarioOut(**d) for d in (estado.get("diarios") or [])]


@app.post("/jogos/{jogo_id}/diarios/gerar", response_model=DiarioOut, status_code=201)
def gerar_diario_do_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Gera uma nova página do Diário Secreto do personagem sobre o protagonista."""
    exigir_ritmo(jogador["id"])
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        try:
            entrada = motor.gerar_pagina_diario(estado, instante_ms=_agora_ms())
        except motor.ErroIA as erro:
            raise HTTPException(503, str(erro))
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return DiarioOut(**entrada)


@app.get("/jogos/{jogo_id}/cartas", response_model=list[CartaOut])
def listar_cartas_do_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    return [CartaOut(**c) for c in (estado.get("cartas") or [])]


@app.post("/jogos/{jogo_id}/cartas/gerar", response_model=CartaOut, status_code=201)
def gerar_carta_do_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Gera uma Carta de Memória (Memory Letter) enviada pelo personagem ao jogador."""
    exigir_ritmo(jogador["id"])
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        try:
            carta = motor.gerar_carta_memoria(estado, instante_ms=_agora_ms())
        except motor.ErroIA as erro:
            raise HTTPException(503, str(erro))
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return CartaOut(**carta)


@app.get("/jogos/{jogo_id}/savepoints", response_model=list[SavepointOut])
def listar_savepoints_do_jogo(jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    estado = carregar_estado_do_jogo(jogo)
    return _resumo_savepoints(estado)


@app.post("/jogos/{jogo_id}/savepoints", response_model=SavepointOut, status_code=201)
def criar_savepoint_do_jogo(dados: SavepointIn, jogo_id: IdDoJogo, jogador=Depends(jogador_atual)):
    """Salva o estado atual como um Ponto de Salvamento (Savepoint) para ramificar a história."""
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        novo = motor.criar_savepoint(estado, nome=dados.nome, instante_ms=_agora_ms())
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return SavepointOut(
        id=novo["id"],
        nome=novo["nome"],
        turno=novo["turno"],
        local=novo["local"],
        resumo=novo.get("resumo") or "",
        criado_em=novo.get("criado_em"),
    )


@app.post("/jogos/{jogo_id}/savepoints/{save_id}/carregar", response_model=JogoOut)
def carregar_savepoint_do_jogo(jogo_id: IdDoJogo, save_id: str, jogador=Depends(jogador_atual)):
    """Restaura a história para um Ponto de Salvamento anterior."""
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        try:
            motor.carregar_savepoint(estado, save_id)
        except ValueError as erro:
            raise HTTPException(404, str(erro))
        morreu = False if estado.get("modo") == "historia" else (estado["personagem"]["vida"] <= 0)
        banco.atualizar_jogo(jogo_id, jogador["id"], motor.estado_para_texto(estado), morreu)
    return montar_saida(
        jogo_id,
        estado,
        morreu,
        jogadas_restantes(jogador["id"]),
        completo=True,
        mochis=int(jogador.get("mochis") or 0),
    )


@app.delete("/jogos/{jogo_id}/savepoints/{save_id}", status_code=204)
def apagar_savepoint_do_jogo(jogo_id: IdDoJogo, save_id: str, jogador=Depends(jogador_atual)):
    with uma_jogada_por_vez(jogador["id"]):
        jogo = banco.obter_jogo(jogo_id, jogador["id"])
        if jogo is None:
            raise HTTPException(404, "Jogo não encontrado.")
        estado = carregar_estado_do_jogo(jogo)
        try:
            motor.remover_savepoint(estado, save_id)
        except ValueError as erro:
            raise HTTPException(404, str(erro))
        banco.atualizar_jogo(
            jogo_id, jogador["id"], motor.estado_para_texto(estado), bool(jogo["encerrado"])
        )
    return Response(status_code=204)


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


# ---------------------------------------------------------------
# Endpoints de Múltiplas Personas
# ---------------------------------------------------------------
@app.get("/personas", response_model=list[PersonaOut])
def listar_minhas_personas(jogador=Depends(jogador_atual)):
    """Lista todas as personas do jogador com indicador de persona padrão e retrato."""
    linhas = banco.listar_personas(jogador["id"])
    resultado = []
    for p in linhas:
        resultado.append(PersonaOut(
            id=p["id"],
            nome=p["nome"],
            genero=p["genero"],
            idade=p["idade"] or "",
            personalidade=p["personalidade"] or "",
            descricao=p["personalidade"] or "",
            interesses=p["interesses"] or "",
            aparencia=p["aparencia"] or "",
            medos=p["medos"] or "",
            aversoes=p["medos"] or "",
            tags=p.get("tags") or [],
            detalhes_imagem=p["detalhes_imagem"] or "",
            estilo_imagem=p["estilo_imagem"] or "semi_realistic",
            avatar_emoji=p["avatar_emoji"] or "👤",
            retrato_versao=p["retrato_versao"],
            versao_retrato=p["retrato_versao"],
            padrao=bool(p["padrao"]),
            tem_retrato=tem_persona_retrato(jogador["id"], p["id"]),
            criado_em=p.get("criado_em") or "",
            atualizado_em=p.get("atualizado_em") or ""
        ))
    return resultado


@app.post("/personas", response_model=PersonaOut, status_code=201)
def criar_persona(dados: PersonaIn, jogador=Depends(jogador_atual)):
    """Cria uma nova persona respeitando o limite do plano (grátis: 3, premium: 20)."""
    dados_dict = dados.model_dump()
    if not dados_dict.get("personalidade") and dados_dict.get("descricao"):
        dados_dict["personalidade"] = dados_dict["descricao"]
    if not dados_dict.get("medos") and dados_dict.get("aversoes"):
        dados_dict["medos"] = dados_dict["aversoes"]
    try:
        p_id = banco.salvar_persona(jogador["id"], dados_dict)
    except banco.ErroNegocio as erro:
        raise HTTPException(erro.status, erro.mensagem)
    p = banco.obter_persona(jogador["id"], p_id)
    pers = p["personalidade"] or ""
    med = p["medos"] or ""
    v_ret = p["retrato_versao"] if p["retrato_versao"] else 1
    return PersonaOut(
        id=p["id"],
        jogador_id=str(jogador["id"]),
        nome=p["nome"],
        genero=p["genero"],
        idade=p["idade"] or "",
        personalidade=pers,
        descricao=pers,
        interesses=p["interesses"] or "",
        aparencia=p["aparencia"] or "",
        medos=med,
        aversoes=med,
        tags=dados.tags or [],
        detalhes_imagem=p["detalhes_imagem"] or "",
        estilo_imagem=p["estilo_imagem"] or "semi_realistic",
        avatar_emoji=p["avatar_emoji"] or "👤",
        retrato_versao=v_ret,
        versao_retrato=v_ret,
        padrao=bool(p["padrao"]),
        tem_retrato=tem_persona_retrato(jogador["id"], p["id"]),
        criado_em=p.get("criado_em") or "",
        atualizado_em=p.get("atualizado_em") or ""
    )


@app.put("/personas/{persona_id}", response_model=PersonaOut)
def atualizar_persona(persona_id: int, dados: PersonaIn, jogador=Depends(jogador_atual)):
    """Atualiza dados de uma persona existente."""
    existente = banco.obter_persona(jogador["id"], persona_id)
    if not existente:
        raise HTTPException(404, "Persona não encontrada.")
    dados_dict = dados.model_dump()
    if not dados_dict.get("personalidade") and dados_dict.get("descricao"):
        dados_dict["personalidade"] = dados_dict["descricao"]
    if not dados_dict.get("medos") and dados_dict.get("aversoes"):
        dados_dict["medos"] = dados_dict["aversoes"]
    banco.salvar_persona(jogador["id"], dados_dict, persona_id)
    p = banco.obter_persona(jogador["id"], persona_id)
    pers = p["personalidade"] or ""
    med = p["medos"] or ""
    v_ret = p["retrato_versao"] if p["retrato_versao"] else 1
    return PersonaOut(
        id=p["id"],
        jogador_id=str(jogador["id"]),
        nome=p["nome"],
        genero=p["genero"],
        idade=p["idade"] or "",
        personalidade=pers,
        descricao=pers,
        interesses=p["interesses"] or "",
        aparencia=p["aparencia"] or "",
        medos=med,
        aversoes=med,
        tags=dados.tags or [],
        detalhes_imagem=p["detalhes_imagem"] or "",
        estilo_imagem=p["estilo_imagem"] or "semi_realistic",
        avatar_emoji=p["avatar_emoji"] or "👤",
        retrato_versao=v_ret,
        versao_retrato=v_ret,
        padrao=bool(p["padrao"]),
        tem_retrato=tem_persona_retrato(jogador["id"], p["id"]),
        criado_em=p.get("criado_em") or "",
        atualizado_em=p.get("atualizado_em") or ""
    )


@app.delete("/personas/{persona_id}", status_code=204)
def deletar_minha_persona(persona_id: int, jogador=Depends(jogador_atual)):
    """Deleta uma persona do jogador."""
    existente = banco.obter_persona(jogador["id"], persona_id)
    if not existente:
        raise HTTPException(404, "Persona não encontrada.")
    banco.deletar_persona(jogador["id"], persona_id)
    caminho = caminho_persona_retrato(jogador["id"], persona_id)
    if os.path.exists(caminho):
        try:
            os.remove(caminho)
        except Exception:
            pass


@app.post("/personas/{persona_id}/padrao", response_model=PersonaOut)
def marcar_como_padrao(persona_id: int, jogador=Depends(jogador_atual)):
    """Define a persona indicada como a persona padrão do jogador."""
    existente = banco.obter_persona(jogador["id"], persona_id)
    if not existente:
        raise HTTPException(404, "Persona não encontrada.")
    banco.definir_persona_padrao(jogador["id"], persona_id)
    p = banco.obter_persona(jogador["id"], persona_id)
    pers = p["personalidade"] or ""
    med = p["medos"] or ""
    v_ret = p["retrato_versao"] if p["retrato_versao"] else 1
    return PersonaOut(
        id=p["id"],
        jogador_id=str(jogador["id"]),
        nome=p["nome"],
        genero=p["genero"],
        idade=p["idade"] or "",
        personalidade=pers,
        descricao=pers,
        interesses=p["interesses"] or "",
        aparencia=p["aparencia"] or "",
        medos=med,
        aversoes=med,
        tags=p.get("tags") or [],
        detalhes_imagem=p["detalhes_imagem"] or "",
        estilo_imagem=p["estilo_imagem"] or "semi_realistic",
        avatar_emoji=p["avatar_emoji"] or "👤",
        retrato_versao=v_ret,
        versao_retrato=v_ret,
        padrao=True,
        tem_retrato=tem_persona_retrato(jogador["id"], p["id"]),
        criado_em=p.get("criado_em") or "",
        atualizado_em=p.get("atualizado_em") or ""
    )


@app.post("/personas/{persona_id}/retrato", response_model=RetratoOut)
def gerar_retrato_persona_id(persona_id: int, dados: PersonaRetratoIn, jogador=Depends(jogador_atual)):
    """Gera ilustração por IA para uma persona específica usando detalhes e estilo de arte."""
    existente = banco.obter_persona(jogador["id"], persona_id)
    if not existente:
        raise HTTPException(404, "Persona não encontrada.")
    if not limite_retrato.permitir(f"persona_retrato:{jogador['id']}:{persona_id}"):
        raise HTTPException(429, "Calma! Aguarde um pouco para gerar outro retrato.")
    consumir_retrato_do_dia()

    estado_persona = {
        "personagem": {"nome": dados.nome or existente["nome"]},
        "genero": dados.genero or existente["genero"] or "Aventura",
        "persona": {
            "nome": dados.nome or existente["nome"],
            "genero": dados.genero or existente["genero"],
            "aparencia": dados.aparencia or existente["aparencia"] or "",
            "personalidade": dados.personalidade or dados.descricao or existente["personalidade"] or "",
            "idade": dados.idade or existente["idade"] or "",
            "interesses": dados.interesses or existente["interesses"] or "",
            "medos": dados.medos or dados.aversoes or existente["medos"] or "",
            "detalhes_imagem": dados.detalhes_imagem or existente["detalhes_imagem"] or "",
            "estilo_imagem": dados.estilo_imagem or existente["estilo_imagem"] or "semi_realistic",
        }
    }
    try:
        imagem = motor.gerar_retrato(estado_persona)
    except motor.ErroIA as erro:
        logging.warning("Falha ao gerar retrato da persona %s: %s", persona_id, erro)
        raise HTTPException(
            503, "Não foi possível gerar a imagem da persona agora. Tente de novo em instantes."
        )
    os.makedirs(config.PASTA_RETRATOS, exist_ok=True)
    caminho = caminho_persona_retrato(jogador["id"], persona_id)
    with open(caminho, "wb") as arquivo:
        arquivo.write(imagem)
    banco.incrementar_retrato_persona(jogador["id"], persona_id)
    return RetratoOut(retrato=True)


@app.get("/personas/{persona_id}/retrato")
def ver_retrato_persona_id(persona_id: int, token: Optional[str] = None, credenciais: Optional[HTTPAuthorizationCredentials] = Depends(esquema_bearer)):
    """Devolve a imagem da persona específica."""
    caminho = None
    # 1. Procura diretamente pelo ID global da persona no banco
    try:
        with banco.conexao() as c:
            linha = c.execute("SELECT jogador_id FROM personas WHERE id = ?", (persona_id,)).fetchone()
        if linha:
            c1 = caminho_persona_retrato(linha["jogador_id"], persona_id)
            if os.path.exists(c1):
                caminho = c1
    except Exception:
        pass

    # 2. Se não encontrou, tenta pelo jogador autenticado (se houver)
    if not caminho:
        jogador = None
        if credenciais and credenciais.credentials:
            jogador = banco.jogador_por_token_hash(hash_token(credenciais.credentials))
        elif token:
            jogador = banco.jogador_por_token_hash(hash_token(token))
        if jogador:
            c2 = caminho_persona_retrato(jogador["id"], persona_id)
            if os.path.exists(c2):
                caminho = c2
            else:
                c_geral = caminho_persona_retrato(jogador["id"])
                if os.path.exists(c_geral):
                    caminho = c_geral

    if not caminho or not os.path.exists(caminho):
        raise HTTPException(404, "Esta persona ainda não possui retrato gerado.")
    return FileResponse(caminho, media_type=tipo_de_imagem(caminho), headers={"Cache-Control": "public, max-age=86400"})


@app.post("/conta/persona/candidatos", response_model=CandidatosRetratoResponse)
def gerar_candidatos_persona(dados: PersonaRetratoIn, jogador=Depends(jogador_atual)):
    """Gera 4 sementes e links de candidatos a retrato para a persona."""
    estado_persona = {
        "personagem": {"nome": dados.nome or jogador.get("apelido", "Viajante")},
        "genero": dados.genero or "Feminino",
        "persona": {
            "nome": dados.nome or jogador.get("apelido", "Viajante"),
            "genero": dados.genero or "Feminino",
            "aparencia": dados.aparencia or "",
            "personalidade": dados.personalidade or dados.descricao or "",
            "idade": dados.idade or "",
            "interesses": dados.interesses or "",
            "medos": dados.medos or dados.aversoes or "",
            "tags": dados.tags or [],
            "detalhes_imagem": dados.detalhes_imagem or "",
            "estilo_imagem": dados.estilo_imagem or "semi_realistic",
        }
    }
    prompt_en = motor.prompt_retrato_en(estado_persona)
    seeds = [random.randint(1, 2_000_000_000) for _ in range(4)]
    lote_id = uuid.uuid4().hex[:12]

    agora_ts = time.time()
    expirados = [k for k, v in _lotes_candidatos.items() if agora_ts - v.get("criado_em", 0) > 1800]
    for k in expirados:
        _lotes_candidatos.pop(k, None)

    _lotes_candidatos[lote_id] = {
        "prompt_en": prompt_en,
        "seeds": seeds,
        "criado_em": agora_ts,
        "jogador_id": jogador["id"],
        "imagens": {}
    }

    candidatos = []
    largura = config.POLLINATIONS_LARGURA or 768
    altura = config.POLLINATIONS_ALTURA or 1024
    modelo_flux = config.POLLINATIONS_MODELO or "flux"
    url_base_polli = config.POLLINATIONS_URL or "https://image.pollinations.ai/prompt"

    for i, s in enumerate(seeds):
        params = {
            "width": largura,
            "height": altura,
            "seed": s,
            "model": modelo_flux,
            "nologo": "true"
        }
        url_direta = f"{url_base_polli}/{urllib.parse.quote(prompt_en[:1400])}?{urllib.parse.urlencode(params)}"
        candidatos.append(CandidatoRetratoOut(
            indice=i,
            seed=s,
            url=f"/conta/persona/candidatos/{lote_id}/{i}",
            url_direta=url_direta
        ))

    return CandidatosRetratoResponse(lote_id=lote_id, candidatos=candidatos)


@app.get("/conta/persona/candidatos/{lote_id}/{indice}")
def obter_candidato_imagem(lote_id: str, indice: int):
    """Devolve a imagem gerada para o candidato específico do lote."""
    lote = _lotes_candidatos.get(lote_id)
    if not lote or indice not in range(len(lote["seeds"])):
        raise HTTPException(404, "Candidato não encontrado ou lote expirado.")

    if indice in lote["imagens"]:
        return Response(content=lote["imagens"][indice], media_type="image/jpeg", headers={"Cache-Control": "public, max-age=3600"})

    seed = lote["seeds"][indice]
    prompt_en = lote["prompt_en"]
    largura = config.POLLINATIONS_LARGURA or 768
    altura = config.POLLINATIONS_ALTURA or 1024
    modelo_flux = config.POLLINATIONS_MODELO or "flux"
    url_base_polli = config.POLLINATIONS_URL or "https://image.pollinations.ai/prompt"
    params = {
        "width": largura,
        "height": altura,
        "seed": seed,
        "model": modelo_flux,
        "nologo": "true"
    }
    url = f"{url_base_polli}/{urllib.parse.quote(prompt_en[:1400])}?{urllib.parse.urlencode(params)}"
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "LivennIA/1.0"})
        with urllib.request.urlopen(req, timeout=35) as resp:
            dados_img = resp.read()
        lote["imagens"][indice] = dados_img
        return Response(content=dados_img, media_type="image/jpeg", headers={"Cache-Control": "public, max-age=3600"})
    except Exception as e:
        logging.warning("Erro ao baixar candidato %s do lote %s: %s", indice, lote_id, e)
        raise HTTPException(503, "Falha ao gerar esta variação de imagem.")


@app.post("/personas/{persona_id}/confirmar-candidato", response_model=RetratoOut)
def confirmar_candidato_persona(persona_id: int, dados: ConfirmarCandidatoIn, jogador=Depends(jogador_atual)):
    """Salva a imagem do candidato escolhido como retrato oficial da persona."""
    existente = banco.obter_persona(jogador["id"], persona_id)
    if not existente:
        raise HTTPException(404, "Persona não encontrada.")

    lote = _lotes_candidatos.get(dados.lote_id)
    dados_img = None
    if lote and dados.indice in lote.get("imagens", {}):
        dados_img = lote["imagens"][dados.indice]
    elif lote and dados.indice in range(len(lote["seeds"])):
        seed = lote["seeds"][dados.indice]
        prompt_en = lote["prompt_en"]
        largura = config.POLLINATIONS_LARGURA or 768
        altura = config.POLLINATIONS_ALTURA or 1024
        modelo_flux = config.POLLINATIONS_MODELO or "flux"
        url_base_polli = config.POLLINATIONS_URL or "https://image.pollinations.ai/prompt"
        params = {"width": largura, "height": altura, "seed": seed, "model": modelo_flux, "nologo": "true"}
        url = f"{url_base_polli}/{urllib.parse.quote(prompt_en[:1400])}?{urllib.parse.urlencode(params)}"
        req = urllib.request.Request(url, headers={"User-Agent": "LivennIA/1.0"})
        with urllib.request.urlopen(req, timeout=35) as resp:
            dados_img = resp.read()
        lote["imagens"][dados.indice] = dados_img

    if not dados_img:
        raise HTTPException(400, "Imagem do candidato não encontrada. Tente gerar novamente.")

    os.makedirs(config.PASTA_RETRATOS, exist_ok=True)
    caminho = caminho_persona_retrato(jogador["id"], persona_id)
    with open(caminho, "wb") as f:
        f.write(dados_img)
    banco.incrementar_retrato_persona(jogador["id"], persona_id)
    return RetratoOut(retrato=True)


@app.post("/personas/{persona_id}/upload-retrato", response_model=RetratoOut)
def upload_retrato_persona(persona_id: int, dados: UploadRetratoIn, jogador=Depends(jogador_atual)):
    """Salva uma imagem enviada pelo usuário da galeria como retrato da persona."""
    existente = banco.obter_persona(jogador["id"], persona_id)
    if not existente:
        raise HTTPException(404, "Persona não encontrada.")
    import base64
    b64 = dados.imagem_base64
    if "," in b64:
        b64 = b64.split(",")[-1]
    dados_img = base64.b64decode(b64)
    if not dados_img or len(dados_img) < 100:
        raise HTTPException(400, "Arquivo de imagem inválido.")
    os.makedirs(config.PASTA_RETRATOS, exist_ok=True)
    caminho = caminho_persona_retrato(jogador["id"], persona_id)
    with open(caminho, "wb") as f:
        f.write(dados_img)
    banco.incrementar_retrato_persona(jogador["id"], persona_id)
    return RetratoOut(retrato=True)


@app.post("/jogos/{jogo_id}/persona", response_model=JogoOut)
def trocar_persona_jogo(jogo_id: IdDoJogo, dados: TrocarPersonaJogoIn, jogador=Depends(jogador_atual)):
    """Troca a persona ativa do jogador na história."""
    jogo = banco.obter_jogo(jogo_id, jogador["id"])
    if jogo is None:
        raise HTTPException(404, "Jogo não encontrado.")
    persona = banco.obter_persona(jogador["id"], dados.persona_id)
    if not persona:
        raise HTTPException(404, "Persona não encontrada.")
    estado = carregar_estado_do_jogo(jogo)
    estado["persona"] = persona
    banco.atualizar_jogo(jogo_id, jogador["id"], motor.estado_para_texto(estado), jogo["encerrado"])
    return montar_saida(jogo_id, estado, jogo["encerrado"], restante_do_dia(jogador["id"]), mochis=int(jogador.get("mochis") or 0))


@app.get("/estilos-imagem/{estilo_id}/exemplo")
def ver_exemplo_estilo_imagem(estilo_id: str):
    """Devolve a imagem de exemplo oficial de um estilo de IA (para prévia visual)."""
    estilo_limpo = estilo_id.strip().lower().replace(" ", "_")
    mapa_sinonimos = {
        "anime": "vibrant_anime",
        "realista": "photorealistic",
        "cyberpunk": "dark_fantasy",
        "manga": "manga_bw",
        "vintage": "vintage_oil",
    }
    estilo_limpo = mapa_sinonimos.get(estilo_limpo, estilo_limpo)
    pasta = os.path.join(os.path.dirname(os.path.abspath(__file__)), "dados", "estilos_exemplos")
    caminho = os.path.join(pasta, f"{estilo_limpo}.jpg")
    if os.path.exists(caminho):
        return FileResponse(caminho, media_type="image/jpeg", headers={"Cache-Control": "public, max-age=86400"})
    caminho_padrao = os.path.join(pasta, "semi_realistic.jpg")
    if os.path.exists(caminho_padrao):
        return FileResponse(caminho_padrao, media_type="image/jpeg", headers={"Cache-Control": "public, max-age=86400"})
    if os.path.exists(pasta):
        for f in os.listdir(pasta):
            if f.endswith(".jpg"):
                return FileResponse(os.path.join(pasta, f), media_type="image/jpeg", headers={"Cache-Control": "public, max-age=86400"})
    raise HTTPException(404, "Exemplo não encontrado.")


# ---------------------------------------------------------------
# Comentários e Social (Prévia da História e Perfil do Criador)
# ---------------------------------------------------------------
@app.get("/catalogo/{cenario_id}/comentarios", response_model=list[ComentarioOut])
def listar_comentarios(cenario_id: IdDoCenario, jogador=Depends(jogador_atual)):
    return banco.listar_comentarios_cenario(cenario_id, jogador["id"])


@app.post("/catalogo/{cenario_id}/comentarios", response_model=ComentarioOut, status_code=201)
def criar_comentario(cenario_id: IdDoCenario, dados: ComentarioIn, jogador=Depends(jogador_atual)):
    autor = jogador.get("apelido") or "Jogador"
    return banco.adicionar_comentario(
        cenario_id=cenario_id,
        jogador_id=jogador["id"],
        autor_nome=autor,
        autor_avatar=jogador.get("avatar") or "👤",
        texto=dados.texto,
    )


@app.put("/catalogo/{cenario_id}/comentarios/{comentario_id}", response_model=ComentarioOut)
def editar_comentario(cenario_id: IdDoCenario, comentario_id: int, dados: ComentarioIn, jogador=Depends(jogador_atual)):
    return banco.editar_comentario(comentario_id, jogador["id"], dados.texto)


@app.delete("/catalogo/{cenario_id}/comentarios/{comentario_id}", status_code=204)
def excluir_comentario(cenario_id: IdDoCenario, comentario_id: int, jogador=Depends(jogador_atual)):
    banco.excluir_comentario(comentario_id, jogador["id"])
    return None


@app.post("/catalogo/{cenario_id}/comentarios/{comentario_id}/like", response_model=LikeComentarioOut)
def curtir_comentario(cenario_id: IdDoCenario, comentario_id: int, jogador=Depends(jogador_atual)):
    return banco.alternar_like_comentario(comentario_id, jogador["id"])


@app.get("/criadores/{criador_nome}", response_model=PerfilCriadorOut)
def ver_perfil_criador(criador_nome: str, jogador=Depends(jogador_atual)):
    dados = banco.obter_status_criador(criador_nome, jogador["id"])
    cenarios_out = [
        CenarioOut(
            id=c["id"],
            nome=c["nome"],
            subtitulo=c.get("subtitulo") or "",
            emoji=c.get("emoji") or "🎭",
            genero=c.get("genero") or "Aventura",
            tags=list(c.get("tags") or []),
            sinopse=c.get("sinopse") or "",
            abertura=c.get("abertura") or "",
            capa=tem_capa(c["id"]),
            criadoPorUsuario=True,
            autorNome=c.get("autorNome") or criador_nome,
            autorAvatar=c.get("autorAvatar") or "🎭",
            chats=int(c.get("chats") or 0),
            saves=int(c.get("saves") or 0),
            modoJogo=c.get("modoJogo") or "historia",
        )
        for c in dados.get("historias", [])
    ]
    return PerfilCriadorOut(
        nome=dados["nome"],
        avatar=dados["avatar"],
        uid=dados["uid"],
        chats=dados["chats"],
        saves=dados["saves"],
        seguidores=dados["seguidores"],
        seguindo=dados["seguindo"],
        esta_seguindo=dados["esta_seguindo"],
        esta_bloqueado=dados["esta_bloqueado"],
        historias=cenarios_out,
    )


@app.post("/criadores/{criador_nome}/seguir", response_model=SeguirOut)
def seguir_criador(criador_nome: str, jogador=Depends(jogador_atual)):
    return banco.alternar_seguir_criador(jogador["id"], criador_nome)


@app.post("/criadores/{criador_nome}/bloquear")
def bloquear_criador(criador_nome: str, jogador=Depends(jogador_atual)):
    banco.bloquear_criador(jogador["id"], criador_nome)
    return {"status": "ok", "mensagem": f"Criador {criador_nome} bloqueado."}


@app.post("/criadores/{criador_nome}/denunciar")
def denunciar_criador(criador_nome: str, dados: DenunciaCriadorIn, jogador=Depends(jogador_atual)):
    banco.denunciar_criador(jogador["id"], criador_nome, dados.motivo)
    return {"status": "ok", "mensagem": "Denúncia registrada com sucesso."}
