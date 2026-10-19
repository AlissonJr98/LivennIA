"""
Administração do beta LivennIA (só roda no seu computador/servidor, nunca pela internet).

Exemplos:
    python admin.py convites criar --quantidade 5 --nota "amigos da faculdade"
    python admin.py convites listar
    python admin.py convites desativar LIV-ABCD-EFGH
    python admin.py jogadores listar
    python admin.py jogadores banir 3
    python admin.py jogadores desbanir 3
    python admin.py jogadores premium 3
    python admin.py jogadores apagar 3
    python admin.py estatisticas
    python admin.py estatisticas --dia 2026-10-12
    python admin.py denuncias listar
    python admin.py denuncias revisar 1
"""
import argparse

import banco
import config


def cmd_convites_criar(args):
    codigos = banco.criar_convites(args.quantidade, args.usos, args.nota)
    print(f"\n{len(codigos)} convite(s) criado(s) (cada um vale para {args.usos} pessoa(s)):\n")
    for codigo in codigos:
        print(f"  {codigo}")
    print()


def cmd_convites_listar(args):
    convites = banco.listar_convites()
    if not convites:
        print("Nenhum convite criado ainda.")
        return
    print(f"\n{'CÓDIGO':<15} {'USOS':<7} {'ATIVO':<6} NOTA")
    for c in convites:
        print(f"{c['codigo']:<15} {c['usos']}/{c['usos_max']:<5} {'sim' if c['ativo'] else 'não':<6} {c['nota'] or ''}")
    print()


def cmd_convites_desativar(args):
    ok = banco.desativar_convite(args.codigo.strip().upper())
    print("Convite desativado." if ok else "Convite não encontrado.")


def cmd_jogadores_listar(args):
    jogadores = banco.listar_jogadores()
    if not jogadores:
        print("Nenhum jogador ainda.")
        return
    print(f"\n{'ID':<5} {'APELIDO':<22} {'JOGOS':<6} {'JOGADAS':<8} {'BANIDO':<7} {'PREMIUM':<8} CRIADO EM")
    for j in jogadores:
        print(f"{j['id']:<5} {j['apelido']:<22} {j['jogos']:<6} {j['jogadas']:<8} "
              f"{'sim' if j['banido'] else 'não':<7} {'sim' if j['premium'] else 'não':<8} {j['criado_em']}")
    print()


def cmd_jogadores_banir(args):
    print("Jogador banido." if banco.definir_banido(args.id, True) else "Jogador não encontrado.")


def cmd_jogadores_desbanir(args):
    print("Jogador liberado." if banco.definir_banido(args.id, False) else "Jogador não encontrado.")


def cmd_jogadores_premium(args):
    """Liga/desliga a conta premium (libera os 'cérebros' de IA mais caros no app)."""
    if not banco.definir_premium(args.id, args.ligar):
        print("Jogador não encontrado.")
    elif args.ligar:
        print(f"Jogador {args.id} agora é premium: os cérebros bloqueados liberaram.")
    else:
        print(f"Jogador {args.id} voltou ao plano gratuito.")


def cmd_jogadores_apagar(args):
    """Apaga o jogador e tudo ligado a ele (jogos, feedbacks, denúncias). Não volta atrás."""
    jogador = next((j for j in banco.listar_jogadores() if j["id"] == args.id), None)
    if jogador is None:
        print("Jogador não encontrado.")
        return
    if not args.sem_confirmar:
        print(f"Vai apagar: #{jogador['id']} {jogador['apelido']!r} "
              f"({jogador['jogos']} aventura(s), {jogador['jogadas']} jogada(s)).")
        resposta = input("Digite APAGAR para confirmar: ").strip()
        if resposta != "APAGAR":
            print("Cancelado.")
            return
    banco.apagar_jogador(args.id)
    print(f"Jogador {args.id} apagado.")


def cmd_estatisticas(args):
    dia = args.dia or banco.hoje()
    e = banco.estatisticas_do_dia(dia)
    g = e["geral"]
    sucessos = g["sucessos"] or 0
    tokens_totais = (g["tokens_entrada"] or 0) + (g["tokens_saida"] or 0)

    print(f"\n=== Estatísticas de {dia} ===")
    print(f"Jogadores cadastrados (total): {e['total_jogadores']}")
    print(f"Jogadores ativos no dia:       {g['jogadores_ativos']}")
    print(f"Chamadas à IA:                 {g['chamadas']} ({sucessos} com sucesso)")
    print(f"Tokens de entrada / saída:     {g['tokens_entrada']} / {g['tokens_saida']}")
    if sucessos:
        print(f"Tokens por jogada (média):     {tokens_totais / sucessos:.0f}")
    if g["segundos_medio"] is not None:
        print(f"Tempo médio de resposta:       {g['segundos_medio']:.1f} s")
    print(f"Teto global do dia:            {config.TETO_GLOBAL_JOGADAS_DIA} jogadas / "
          f"{config.TETO_GLOBAL_TOKENS_DIA} tokens")

    custo_total = 0.0
    sem_preco = []
    for m in e["por_modelo"]:
        precos = config.PRECOS_POR_MILHAO.get(m["modelo"])
        if precos:
            custo_total += (m["tokens_entrada"] or 0) / 1e6 * precos[0]
            custo_total += (m["tokens_saida"] or 0) / 1e6 * precos[1]
        else:
            sem_preco.append(m["modelo"])
        print(f"  - {m['modelo']}: {m['chamadas']} chamadas")
    if config.PRECOS_POR_MILHAO and not sem_preco:
        print(f"Custo estimado do dia:         US$ {custo_total:.4f}")
        if sucessos:
            print(f"Custo estimado por jogada:     US$ {custo_total / sucessos:.5f}")
    else:
        print("Custo estimado: preencha PRECOS_POR_MILHAO no config.py para ver o valor em dólares.")

    nota = f"{e['nota_media']:.2f}" if e["nota_media"] is not None else "-"
    print(f"Feedbacks recebidos: {e['feedbacks']} (nota média: {nota})")
    print(f"Denúncias pendentes: {e['denuncias_pendentes']}\n")


def cmd_denuncias_listar(args):
    denuncias = banco.listar_denuncias(somente_pendentes=not args.todas)
    if not denuncias:
        print("Nenhuma denúncia.")
        return
    for d in denuncias:
        estado = "revisada" if d["revisada"] else "PENDENTE"
        print(f"\n#{d['id']} [{estado}] motivo: {d['motivo']} | jogador: {d['jogador_id']} | {d['criado_em']}")
        print(f"{d['trecho']}")
    print()


def cmd_denuncias_revisar(args):
    ok = banco.marcar_denuncia_revisada(args.id)
    print("Denúncia marcada como revisada." if ok else "Denúncia não encontrada.")


def montar_parser():
    parser = argparse.ArgumentParser(description="Administração do beta LivennIA")
    grupos = parser.add_subparsers(dest="grupo", required=True)

    convites = grupos.add_parser("convites").add_subparsers(dest="acao", required=True)
    criar = convites.add_parser("criar")
    criar.add_argument("--quantidade", type=int, default=1)
    criar.add_argument("--usos", type=int, default=1, help="quantas pessoas podem usar cada código")
    criar.add_argument("--nota", default="")
    criar.set_defaults(funcao=cmd_convites_criar)
    convites.add_parser("listar").set_defaults(funcao=cmd_convites_listar)
    desativar = convites.add_parser("desativar")
    desativar.add_argument("codigo")
    desativar.set_defaults(funcao=cmd_convites_desativar)

    jogadores = grupos.add_parser("jogadores").add_subparsers(dest="acao", required=True)
    jogadores.add_parser("listar").set_defaults(funcao=cmd_jogadores_listar)
    banir = jogadores.add_parser("banir")
    banir.add_argument("id", type=int)
    banir.set_defaults(funcao=cmd_jogadores_banir)
    desbanir = jogadores.add_parser("desbanir")
    desbanir.add_argument("id", type=int)
    desbanir.set_defaults(funcao=cmd_jogadores_desbanir)
    premium = jogadores.add_parser("premium", help="libera os cérebros de IA premium para um jogador")
    premium.add_argument("id", type=int)
    premium.add_argument("--desligar", dest="ligar", action="store_false", help="volta para o plano gratuito")
    premium.set_defaults(funcao=cmd_jogadores_premium, ligar=True)
    apagar = jogadores.add_parser("apagar", help="apaga o jogador e as aventuras dele (não volta atrás)")
    apagar.add_argument("id", type=int)
    apagar.add_argument("--sem-confirmar", dest="sem_confirmar", action="store_true",
                        help="apaga direto, sem pedir confirmação")
    apagar.set_defaults(funcao=cmd_jogadores_apagar, sem_confirmar=False)

    estat = grupos.add_parser("estatisticas")
    estat.add_argument("--dia", help="AAAA-MM-DD (padrão: hoje)")
    estat.set_defaults(funcao=cmd_estatisticas)

    denuncias = grupos.add_parser("denuncias").add_subparsers(dest="acao", required=True)
    listar = denuncias.add_parser("listar")
    listar.add_argument("--todas", action="store_true", help="inclui as já revisadas")
    listar.set_defaults(funcao=cmd_denuncias_listar)
    revisar = denuncias.add_parser("revisar")
    revisar.add_argument("id", type=int)
    revisar.set_defaults(funcao=cmd_denuncias_revisar)

    return parser


if __name__ == "__main__":
    args = montar_parser().parse_args()
    if getattr(args, "quantidade", 1) < 1 or getattr(args, "usos", 1) < 1:
        raise SystemExit("--quantidade e --usos precisam ser números maiores que zero.")
    banco.iniciar_banco()
    args.funcao(args)