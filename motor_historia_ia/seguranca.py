"""
Ferramentas de segurança: tokens, códigos de convite e limite de requisições.
"""
import hashlib
import secrets
import threading
import time
from collections import deque

# Sem I, L, O, 0 e 1, para ninguém confundir ao digitar o convite.
ALFABETO_CONVITE = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"


def gerar_token():
    """Token secreto do jogador (256 bits). É mostrado UMA vez e nunca guardado em texto puro."""
    return secrets.token_urlsafe(32)


def hash_token(token):
    """No banco guardamos só a 'impressão digital' do token."""
    return hashlib.sha256(token.encode("utf-8")).hexdigest()


def gerar_codigo_convite():
    def bloco():
        return "".join(secrets.choice(ALFABETO_CONVITE) for _ in range(4))

    return f"LIV-{bloco()}-{bloco()}"


class LimitadorDeTaxa:
    """Permite no máximo `maximo` eventos por `janela_seg` segundos para cada chave.

    Fica na memória do servidor: reinicia quando o servidor reinicia e vale para um
    único processo (no beta usamos um só, então está ótimo).
    """

    def __init__(self, maximo, janela_seg):
        self.maximo = maximo
        self.janela = janela_seg
        self._eventos = {}
        self._trava = threading.Lock()
        self._ultima_limpeza = time.monotonic()

    def permitir(self, chave):
        agora = time.monotonic()
        with self._trava:
            if agora - self._ultima_limpeza > 60:
                self._limpar(agora)
            fila = self._eventos.setdefault(chave, deque())
            while fila and agora - fila[0] > self.janela:
                fila.popleft()
            if len(fila) >= self.maximo:
                return False
            fila.append(agora)
            return True

    def _limpar(self, agora):
        """Remove chaves antigas para a memória não crescer para sempre."""
        for chave in list(self._eventos):
            fila = self._eventos[chave]
            while fila and agora - fila[0] > self.janela:
                fila.popleft()
            if not fila:
                del self._eventos[chave]
        self._ultima_limpeza = agora


# ---------------------------------------------------------------
# Filtro de palavras sensíveis (Filtro de palavras sensíveis do LivennIA)
# ---------------------------------------------------------------
import json
import os
import re

_PALAVRAS_COMPILADAS = None
_TRAVA_PALAVRAS = threading.Lock()


def _carregar_palavras_sensiveis():
    global _PALAVRAS_COMPILADAS
    if _PALAVRAS_COMPILADAS is not None:
        return _PALAVRAS_COMPILADAS
    with _TRAVA_PALAVRAS:
        if _PALAVRAS_COMPILADAS is not None:
            return _PALAVRAS_COMPILADAS
        caminho = os.path.join(
            os.path.dirname(os.path.abspath(__file__)), "dados", "palavras_sensiveis.json"
        )
        termos = set()
        try:
            with open(caminho, "r", encoding="utf-8") as arq:
                dados = json.load(arq)
                for lang in ("pt", "en"):
                    for w in dados.get(lang, []):
                        if isinstance(w, str) and len(w.strip()) >= 3:
                            termos.add(w.strip().lower())
        except Exception:
            termos = {"porn", "pornografia", "nsfw", "estupro", "pedofilia"}
        # Compila regex que ignora espaços intercalados (ex.: "s e x o") com fronteira de palavra
        compilados = []
        for original in sorted(termos, key=len, reverse=True):
            compacto = "".join(c for c in original if not c.isspace())
            if len(compacto) < 3:
                continue
            partes = [re.escape(c) for c in compacto]
            padrao = r"(?<![a-zA-ZÀ-ÿ0-9])" + r"\s*".join(partes) + r"(?![a-zA-ZÀ-ÿ0-9])"
            try:
                compilados.append((original, re.compile(padrao, re.IGNORECASE)))
            except re.error:
                pass
        _PALAVRAS_COMPILADAS = compilados
        return _PALAVRAS_COMPILADAS


def verificar_palavra_sensivel(texto: str):
    """Retorna a primeira palavra sensível encontrada no texto (ou None se estiver limpo)."""
    if not texto or not isinstance(texto, str):
        return None
    for original, regex in _carregar_palavras_sensiveis():
        if regex.search(texto):
            return original
    return None
