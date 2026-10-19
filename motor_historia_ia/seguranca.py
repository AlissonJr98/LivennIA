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