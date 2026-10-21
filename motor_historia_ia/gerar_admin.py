"""Script utilitário para gerenciamento de administradores / donos no LivennIA.

Exemplos de uso:
    python gerar_admin.py --criar-codigo LIV-ADMIN-01
    python gerar_admin.py --promover-email fulano@gmail.com
    python gerar_admin.py --promover-apelido Alisson
    python gerar_admin.py --listar
"""

import argparse
import sys
import banco

def criar_codigo_admin(codigo: str, usos_max: int = 1):
    codigo = codigo.strip().upper()
    with banco.transacao() as c:
        existe = c.execute("SELECT 1 FROM convites WHERE codigo = ?", (codigo,)).fetchone()
        if existe:
            c.execute("UPDATE convites SET ativo = 1, nota = 'dono', usos_max = ? WHERE codigo = ?", (usos_max, codigo))
            print(f"Convite {codigo} já existia e foi atualizado para Administrador/Dono (usos max: {usos_max}).")
        else:
            c.execute(
                "INSERT INTO convites (codigo, usos_max, nota, criado_em) VALUES (?, ?, 'dono', ?)",
                (codigo, usos_max, banco.agora())
            )
            print(f"Convite de Administrador criado com sucesso: {codigo} (usos max: {usos_max})")

def promover_por_email(email: str):
    email = email.strip()
    with banco.transacao() as c:
        linhas = c.execute(
            "UPDATE jogadores SET dono = 1, premium = 1, mochis = 999999 WHERE email = ?", (email,)
        ).rowcount
        if linhas > 0:
            print(f"Conta com email {email} foi promovida a ADMINISTRADOR / DONO com sucesso!")
        else:
            print(f"Nenhum jogador encontrado com o email '{email}'. Peça para ele fazer o primeiro login antes.")

def promover_por_apelido(apelido: str):
    apelido = apelido.strip()
    with banco.transacao() as c:
        linhas = c.execute(
            "UPDATE jogadores SET dono = 1, premium = 1, mochis = 999999 WHERE apelido = ? COLLATE NOCASE", (apelido,)
        ).rowcount
        if linhas > 0:
            print(f"Conta com apelido '{apelido}' foi promovida a ADMINISTRADOR / DONO com sucesso!")
        else:
            print(f"Nenhum jogador encontrado com o apelido '{apelido}'.")

def listar_admins():
    with banco.conexao() as c:
        convites = c.execute("SELECT codigo, usos, usos_max, ativo, nota FROM convites WHERE nota = 'dono'").fetchall()
        jogadores = c.execute("SELECT id, apelido, email, convite, dono, premium FROM jogadores WHERE dono = 1").fetchall()
    print("=== CÓDIGOS DE CONVITE PARA ADMIN ===")
    for row in convites:
        print(f"  - Código: {row['codigo']} (Usos: {row['usos']}/{row['usos_max']}, Ativo: {bool(row['ativo'])})")
    print("\n=== JOGADORES COM ACESSO DE ADMIN / DONO ===")
    for row in jogadores:
        print(f"  - ID: {row['id']} | Apelido: {row['apelido']} | Email: {row['email'] or 'N/A'} | Convite: {row['convite']}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Gerenciador de Admins do LivennIA")
    parser.add_argument("--criar-codigo", type=str, help="Cria um código de convite de administrador")
    parser.add_argument("--usos", type=int, default=1, help="Quantidade de usos máximos para o código (padrão: 1)")
    parser.add_argument("--promover-email", type=str, help="Promove um jogador existente pelo email Google")
    parser.add_argument("--promover-apelido", type=str, help="Promove um jogador existente pelo apelido")
    parser.add_argument("--listar", action="store_true", help="Lista todos os códigos de admin e jogadores administradores")

    args = parser.parse_args()

    if args.criar_codigo:
        criar_codigo_admin(args.criar_codigo, args.usos)
    elif args.promover_email:
        promover_por_email(args.promover_email)
    elif args.promover_apelido:
        promover_por_apelido(args.promover_apelido)
    elif args.listar:
        listar_admins()
    else:
        listar_admins()
