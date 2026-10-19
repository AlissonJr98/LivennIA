# LivennIA 📱✨

Aplicativo Android de **RPG Narrativo** e **Histórias Interativas (Modo Livro)** com Inteligência Artificial.

## Modos de Experiência
- **🎲 Modo RPG**: Campanhas táticas com barra de vida, inventário, escolhas numeradas e consequências reais.
- **📖 Modo História**: Livro interativo e conversa livre com personagens sem barra de vida, focado em narrativa, diálogos e imersão.
- **🔍 Aba Explorar**: Catálogo de personagens e mundos divididos em 13 estilos (Romance, Drama & Obsessão, Anime & Isekai, Sobrenatural, Realeza & Corte, Cotidiano, Fantasia, Mistério, Terror, Ficção Científica, Aventura, Ação e Histórico).

## 🔄 Atualização Direta pelo GitHub
O aplicativo verifica automaticamente novas versões publicadas neste repositório (`atualizacao/versao.json` ou *GitHub Releases*) e permite baixar e instalar a atualização diretamente de dentro do app (na inicialização, na aba **Perfil** ou em **Configurações**).

## 🔒 Segurança e Configuração Local
Arquivos sensíveis não são versionados neste repositório público:
- Para usar recursos do Firebase localmente, copie `app/google-services.json.example` para `app/google-services.json` com as credenciais do seu projeto.
- No servidor Python (`motor_historia_ia`), configure sua chave em `.env` (`GEMINI_API_KEY=...`).
