"""
Configurações do servidor LivennIA.

Todos os limites do beta ficam AQUI. Para mudar algo, altere o número abaixo
(ou defina a variável de ambiente indicada, no arquivo .env ou no servidor).
"""
import os
from datetime import timedelta, timezone

from dotenv import load_dotenv

PASTA = os.path.dirname(os.path.abspath(__file__))
load_dotenv(os.path.join(PASTA, ".env"))


def _inteiro(nome, padrao):
    """Lê um número do ambiente; se não existir ou for inválido, usa o padrão."""
    try:
        return int(os.environ.get(nome, padrao))
    except (TypeError, ValueError):
        return padrao


# ---------------------------------------------------------------
# Segredos e modo de execução
# ---------------------------------------------------------------
CHAVE_API = os.environ.get("GEMINI_API_KEY")

# Firebase (autenticação de contas). Arquivo JSON da service account, ou None para usar
# as credenciais padrão do ambiente (GOOGLE_APPLICATION_CREDENTIALS).
FIREBASE_SERVICE_ACCOUNT = os.environ.get("FIREBASE_SERVICE_ACCOUNT")

# Em produção (LIVENNIA_PRODUCAO=1) a página de testes /docs fica desligada.
PRODUCAO = os.environ.get("LIVENNIA_PRODUCAO", "0") == "1"

ARQUIVO_BANCO = os.environ.get(
    "LIVENNIA_BANCO", os.path.join(PASTA, "dados", "livennia.db")
)

# ---------------------------------------------------------------
# IA (Gemini)
# ---------------------------------------------------------------
# A cada falha, a próxima tentativa usa o modelo seguinte da lista.
# O primeiro da lista é quem escreve quase todos os turnos, então deixamos na frente um
# modelo leve, que a chave do plano gratuito atende de forma estável. O gemini-3.8-flash
# (melhor narrativa, 3x mais caro) fica como reserva: no plano gratuito ele às vezes
# responde 429, então não vale a pena gastar ~1s por turno tentando ele primeiro.
# O jogador pode escolher outro "cérebro" no app (veja catalogo.CEREBROS); a escolha vem
# em estado["modelo"] e é tentada antes desta lista.
MODELOS = [
    m.strip()
    for m in os.environ.get(
        "LIVENNIA_MODELOS",
        "gemini-3.5-flash-lite,gemini-3.1-flash-lite,gemini-flash-lite-latest,gemini-3.8-flash",
    ).split(",")
    if m.strip()
]
MAX_TENTATIVAS = 3              # no servidor falhamos mais rápido; o app pode tentar de novo
TEMPO_LIMITE_SEG = 40           # cancela uma chamada que demorar mais que isso
ESPERA_BASE_SEG = 1             # espera entre tentativas: 1s, 2s, 4s... (máx. 8s)
CODIGOS_TEMPORARIOS = {429, 500, 503, 504}
MAX_TOKENS_RESPOSTA = 1500
RACIOCINIO_BAIXO = False        # True = tenta respostas mais rápidas (teste antes!)
MAX_CHAMADAS_IA_SIMULTANEAS = _inteiro("LIVENNIA_IA_SIMULTANEAS", 8)
ESPERA_VAGA_IA_SEG = 5          # quanto esperar por uma vaga antes de dizer "ocupado"

# ---------------------------------------------------------------
# Retratos dos personagens (imagem gerada pela IA)
# ---------------------------------------------------------------
# Modelo de imagem do Gemini. Precisa de COTA DE IMAGEM na chave (plano pago);
# no plano gratuito todos os modelos de imagem respondem 429.
MODELO_IMAGEM = os.environ.get("LIVENNIA_MODELO_IMAGEM", "gemini-3.1-flash-image")
TEMPO_LIMITE_IMAGEM_SEG = 90

# Provedores de retrato, na ordem em que são tentados: o primeiro que funcionar vence.
#   gemini       -> melhor qualidade; exige cota de imagem paga.
#   pollinations -> grátis e sem chave (reserva). Limitações: marca d'água pequena no
#                   canto (some no recorte circular do app) e 1 imagem a cada ~45s por IP.
PROVEDORES_IMAGEM = [
    p.strip()
    for p in os.environ.get("LIVENNIA_PROVEDORES_IMAGEM", "gemini,pollinations").split(",")
    if p.strip()
]
POLLINATIONS_URL = os.environ.get(
    "LIVENNIA_POLLINATIONS_URL", "https://image.pollinations.ai/prompt"
)
POLLINATIONS_MODELO = os.environ.get("LIVENNIA_POLLINATIONS_MODELO", "flux")
POLLINATIONS_LARGURA = _inteiro("LIVENNIA_POLLINATIONS_LARGURA", 768)
POLLINATIONS_ALTURA = _inteiro("LIVENNIA_POLLINATIONS_ALTURA", 1024)
TEMPO_LIMITE_BAIXO_IMAGEM_SEG = 60   # tempo máximo esperando um provedor grátis

# Quando um provedor responde "sem cota", fica de fora por este tempo antes de
# ser tentado de novo (evita perder ~1s em cada retrato com o Gemini sem cota).
ESPERA_SEM_COTA_SEG = _inteiro("LIVENNIA_ESPERA_SEM_COTA", 900)

PASTA_RETRATOS = os.environ.get(
    "LIVENNIA_RETRATOS", os.path.join(PASTA, "dados", "retratos")
)
MAX_RETRATOS_POR_JOGADOR_HORA = 12   # evita gastar cota de imagem sem querer
MAX_RETRATOS_POR_DIA = _inteiro("LIVENNIA_MAX_RETRATOS_DIA", 200)  # teto global do dia

# ---------------------------------------------------------------
# Capas dos cenários oficiais (a "vitrine" da aba Explorar)
# ---------------------------------------------------------------
# Cada cenário do catálogo tem uma capa guardada em PASTA_CAPAS. Ela é gerada UMA
# vez e fica salva: abrir a vitrine depois disso não gasta cota nenhuma.
PASTA_CAPAS = os.environ.get("LIVENNIA_CAPAS", os.path.join(PASTA, "dados", "capas"))
MAX_CAPAS_POR_DIA = _inteiro("LIVENNIA_MAX_CAPAS_DIA", 60)   # teto global do dia
CAPA_LARGURA = _inteiro("LIVENNIA_CAPA_LARGURA", 768)
CAPA_ALTURA = _inteiro("LIVENNIA_CAPA_ALTURA", 1024)

# ---------------------------------------------------------------
# Regras do jogo (iguais às do mestre.py)
# ---------------------------------------------------------------
MAX_TAMANHO_ACAO = 300
MAX_ITENS = 10
MAX_TAMANHO_ITEM = 40
VARIACAO_MAXIMA_VIDA = 30
LIMITE_NARRATIVA = 2000
TURNOS_NA_MEMORIA = 4
TURNOS_GUARDADOS = 60     # mensagens guardadas para o chat (a IA só recebe os últimos TURNOS_NA_MEMORIA)

# ---------------------------------------------------------------
# Limites do BETA (controlam o seu custo)
# ---------------------------------------------------------------
LIMITE_DIARIO_JOGADAS = _inteiro("LIVENNIA_LIMITE_DIARIO", 30)    # por jogador, por dia
MAX_JOGOS_POR_JOGADOR = _inteiro("LIVENNIA_MAX_JOGOS", 3)         # personagens ativos
MAX_JOGADORES = _inteiro("LIVENNIA_MAX_JOGADORES", 100)           # trava do beta
TETO_GLOBAL_JOGADAS_DIA = _inteiro("LIVENNIA_TETO_JOGADAS", 500)  # todos juntos, por dia
TETO_GLOBAL_TOKENS_DIA = _inteiro("LIVENNIA_TETO_TOKENS", 2_000_000)

# ---------------------------------------------------------------
# Proteções contra abuso
# ---------------------------------------------------------------
LIMITE_JOGADAS_POR_MINUTO = 6        # por jogador
LIMITE_REGISTROS_POR_IP_HORA = 10    # tentativas de registro por IP
LIMITE_REQUISICOES_IP_MINUTO = 120   # qualquer rota, por IP
LIMITE_FEEDBACKS_HORA = 10           # feedbacks + denúncias por jogador
MAX_BODY_BYTES = 10_000              # tamanho máximo do corpo de uma requisição

# ---------------------------------------------------------------
# Datas e custos
# ---------------------------------------------------------------
# O "dia" do jogo vira à meia-noite no horário de Brasília (UTC-3, sem horário de verão).
FUSO = timezone(timedelta(hours=-3))

# OPCIONAL: preencha com os preços ATUAIS da página oficial de preços do Gemini
# para o admin.py estimar o custo. Formato (dólares por 1 milhão de tokens):
#   PRECOS_POR_MILHAO = {"gemini-3.8-flash": (ENTRADA, SAIDA)}
# Enquanto estiver vazio, as estatísticas mostram só os tokens.
PRECOS_POR_MILHAO = {
    "gemini-3.8-flash": (0.75, 3.75),      # sobe para (1.50, 7.50) em 01/01/2027
    "gemini-3.1-flash-lite": (0.25, 1.50),
    "gemini-3.5-flash-lite": (0.30, 2.50),
}