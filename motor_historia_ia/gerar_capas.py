"""Gera (uma única vez) a arte de capa de todos os cenários oficiais.

As capas ficam salvas em dados/capas/ — depois disso abrir a vitrine não gasta nada.
O provedor grátis aceita ~1 imagem a cada 45s por IP, então os pedidos vão em fila,
com pausa entre eles. Rodar da pasta do projeto:

    python gerar_capas.py
"""
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import catalogo  # noqa: E402
import config  # noqa: E402
import motor  # noqa: E402

PAUSA_SEG = 50          # a cota grátis libera ~1 imagem a cada 45s
TENTATIVAS = 2


def caminho(cenario_id):
    return os.path.join(config.PASTA_CAPAS, f"{cenario_id}.png")


def main():
    os.makedirs(config.PASTA_CAPAS, exist_ok=True)
    faltando = [c for c in catalogo.CENARIOS if not os.path.exists(caminho(c["id"]))]
    print(f"{len(faltando)} capa(s) para gerar de {len(catalogo.CENARIOS)}.", flush=True)
    for indice, item in enumerate(faltando, start=1):
        print(f"[{indice}/{len(faltando)}] {item['nome']} ...", flush=True)
        for tentativa in range(1, TENTATIVAS + 1):
            inicio = time.perf_counter()
            try:
                imagem = motor.gerar_capa(item)
            except motor.ErroIA as erro:
                print(f"    falhou ({time.perf_counter() - inicio:.0f}s): {erro}", flush=True)
            else:
                with open(caminho(item["id"]), "wb") as arquivo:
                    arquivo.write(imagem)
                print(
                    f"    ok: {len(imagem) // 1024} KB em {time.perf_counter() - inicio:.0f}s",
                    flush=True,
                )
                break
            if tentativa < TENTATIVAS:
                print(f"    esperando {PAUSA_SEG + 20}s para tentar de novo...", flush=True)
                time.sleep(PAUSA_SEG + 20)
        if indice < len(faltando):
            time.sleep(PAUSA_SEG)
    prontas = [c["id"] for c in catalogo.CENARIOS if os.path.exists(caminho(c["id"]))]
    print(f"Concluído. Capas prontas: {len(prontas)}/{len(catalogo.CENARIOS)}", flush=True)
    for c in catalogo.CENARIOS:
        marca = "ok " if os.path.exists(caminho(c["id"])) else "SEM"
        print(f"  {marca} {c['id']}", flush=True)


if __name__ == "__main__":
    main()
