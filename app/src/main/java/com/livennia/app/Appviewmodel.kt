package com.livennia.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// As telas do app.
sealed class Tela {
    object Onboarding : Tela()
    object Entrada : Tela()
    object Login : Tela()
    object Cadastro : Tela()
    object RecuperarSenha : Tela()
    object Personagens : Tela()
    object Configuracoes : Tela()
    object NovaAventura : Tela()
    object Jogo : Tela()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = Preferencias(application)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // ---- Estado que as telas observam ----
    var tela by mutableStateOf<Tela>(
        if (!prefs.onboardingVisto) Tela.Onboarding
        else if (prefs.token.isNullOrEmpty()) Tela.Entrada else Tela.Personagens
    )
        private set

    var temaEscuro by mutableStateOf(prefs.temaEscuro)
        private set

    fun alternarTema() {
        temaEscuro = !temaEscuro
        prefs.temaEscuro = temaEscuro
    }

    // Estilo visual (Neon = preto/amarelo; Livro = pergaminho/ouro).
    var estiloTema by mutableStateOf(
        runCatching { EstiloTema.valueOf(prefs.estiloTema) }.getOrDefault(EstiloTema.NEON)
    )
        private set

    fun definirEstiloTema(estilo: EstiloTema) {
        estiloTema = estilo
        prefs.estiloTema = estilo.name
    }

    // Cor de destaque do estilo Neon (fundo preto + uma cor só).
    var corNeon by mutableStateOf(
        runCatching { CorNeon.valueOf(prefs.corNeon) }.getOrDefault(CorNeon.AMARELO)
    )
        private set

    fun definirCorNeon(cor: CorNeon) {
        corNeon = cor
        prefs.corNeon = cor.name
    }

    // Aba ativa na tela principal (0 = RPG, 1 = Histórias, 2 = Explorar, 3 = Perfil).
    var abaPrincipal by mutableStateOf(0)
        private set

    fun selecionarAba(aba: Int) {
        abaPrincipal = aba
    }

    // Modo escolhido para a criação de uma nova aventura/história ("rpg" ou "historia").
    var modoCriacao by mutableStateOf("rpg")
        private set

    fun selecionarModoCriacao(modo: String) {
        modoCriacao = if (modo == "historia") "historia" else "rpg"
    }

    // Tamanho da letra do narrador (sp), travado entre 13 e 20.
    var tamanhoLetraNarrador by mutableStateOf(prefs.tamanhoLetraNarrador)
        private set

    fun definirTamanhoLetra(valor: Float) {
        val limitado = valor.coerceIn(13f, 20f)
        tamanhoLetraNarrador = limitado
        prefs.tamanhoLetraNarrador = limitado
    }

    // Modo de leitura
    enum class ModoLeitura { RPG, HISTORIA }

    var modoLeitura by mutableStateOf(
        runCatching { ModoLeitura.valueOf(prefs.modoLeitura) }.getOrDefault(ModoLeitura.RPG)
    )
        private set

    fun definirModoLeitura(modo: ModoLeitura) {
        modoLeitura = modo
        prefs.modoLeitura = modo.name
    }

    // Filtro local de palavras sensíveis do LivennIA.
    var filtroPalavrasSensiveis by mutableStateOf(prefs.filtroPalavrasSensiveis)
        private set

    fun definirFiltroPalavrasSensiveis(ativo: Boolean) {
        filtroPalavrasSensiveis = ativo
        prefs.filtroPalavrasSensiveis = ativo
    }

    var servidor by mutableStateOf(
        prefs.servidor.takeIf { it.isNotBlank() } ?: prefs.servidorPadrao
    )
        private set

    var apelido by mutableStateOf(prefs.apelido)
        private set

    var ocupado by mutableStateOf(false)
        private set

    var mensagemOcupado by mutableStateOf("")
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    var aviso by mutableStateOf<String?>(null)
        private set

    var conta by mutableStateOf<ContaResponse?>(null)
        private set

    var jogos by mutableStateOf<List<JogoResumo>>(emptyList())
        private set

    var generos by mutableStateOf<List<GeneroInfo>>(emptyList())
        private set

    // ---- Vitrine (aba Explorar) ----
    // Lista dos cenários oficiais. O "prompt secreto" de cada um NUNCA chega aqui:
    // o app manda só o id e o servidor injeta a personalidade no narrador.
    var catalogo by mutableStateOf<List<CenarioDto>>(emptyList())
        private set

    // Os "cérebros" de IA (níveis de inteligência) que o jogador pode escolher.
    var cerebros by mutableStateOf<List<CerebroDto>>(emptyList())
        private set

    var carregandoCatalogo by mutableStateOf(false)
        private set

    var erroCatalogo by mutableStateOf<String?>(null)
        private set

    // Cenário escolhido na vitrine (nulo = história livre, criada do zero).
    var cenarioSelecionado by mutableStateOf<CenarioDto?>(null)
        private set

    // Id do cérebro escolhido (nulo = o padrão do servidor).
    var cerebroSelecionado by mutableStateOf<String?>(null)
        private set

    // Muda quando alguma capa é gerada, para as imagens serem recarregadas.
    var capaVersao by mutableStateOf(0)
        private set

    // Id do cenário cuja capa está sendo gerada agora (mostra o "carregando" no card).
    var gerandoCapa by mutableStateOf<String?>(null)
        private set

    var generoSelecionado by mutableStateOf("Aventura")
        private set

    var premissa by mutableStateOf("")
        private set

    fun selecionarGenero(genero: String) {
        generoSelecionado = genero
    }

    fun alterarPremissa(texto: String) {
        premissa = texto.take(200)
    }

    // Ficha do personagem: ajuda a história a ficar coerente e serve de base
    // para o retrato gerado pela IA.
    var aparencia by mutableStateOf("")
        private set

    var descricao by mutableStateOf("")
        private set

    var idade by mutableStateOf("")
        private set

    var interesses by mutableStateOf("")
        private set

    // Do que o personagem tem medo / não gosta (o narrador usa isso na história).
    var aversoes by mutableStateOf("")
        private set

    fun alterarAparencia(texto: String) {
        aparencia = texto.take(200)
    }

    fun alterarDescricao(texto: String) {
        descricao = texto.take(300)
    }

    fun alterarIdade(texto: String) {
        idade = texto.take(20)
    }

    fun alterarInteresses(texto: String) {
        interesses = texto.take(200)
    }

    fun alterarAversoes(texto: String) {
        aversoes = texto.take(200)
    }

    fun limparFicha() {
        aparencia = ""
        descricao = ""
        idade = ""
        interesses = ""
        aversoes = ""
        cenarioSelecionado = null
    }

    // ---- Vitrine: escolher cenário e cérebro ----
    fun carregarCatalogo(forcar: Boolean = false) {
        if (!forcar && catalogo.isNotEmpty() && cerebros.isNotEmpty()) return
        if (!temSessao || carregandoCatalogo) return
        val api = obterApi() ?: return
        carregandoCatalogo = true
        erroCatalogo = null
        viewModelScope.launch {
            try {
                catalogo = api.catalogo()
                cerebros = api.cerebros()
                // Sem escolha ainda: já deixa marcado o primeiro cérebro liberado.
                if (cerebroSelecionado == null) {
                    cerebroSelecionado = cerebros.firstOrNull { it.liberado }?.id
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val problema = e.paraErroApp()
                if (problema.sessaoInvalida) encerrarSessao() else erroCatalogo = problema.mensagem
            } finally {
                carregandoCatalogo = false
            }
        }
    }

    // Tocar num card da vitrine: guarda o cenário (a personalidade secreta fica no
    // servidor) e vai para a criação do personagem já com o gênero certo.
    fun escolherCenario(cenario: CenarioDto, modo: String? = null) {
        cenarioSelecionado = cenario
        if (modo != null) {
            modoCriacao = if (modo == "historia") "historia" else "rpg"
        }
        if (cenario.genero.isNotBlank()) generoSelecionado = cenario.genero
        navegarPara(Tela.NovaAventura)
    }

    // Botão "+": aventura livre, sem cenário oficial.
    fun comecarAventuraLivre(modo: String? = null) {
        cenarioSelecionado = null
        modoCriacao = when {
            modo != null -> if (modo == "historia") "historia" else "rpg"
            abaPrincipal == 1 -> "historia"
            else -> "rpg"
        }
        navegarPara(Tela.NovaAventura)
    }

    fun selecionarCerebro(id: String) {
        cerebroSelecionado = id
    }

    // Gera (uma única vez) a arte de capa de um cenário. Depois ela fica salva no
    // servidor e abrir a vitrine não gasta nada.
    fun gerarCapaDoCenario(id: String) {
        if (gerandoCapa != null) return
        val api = obterApi() ?: return
        gerandoCapa = id
        viewModelScope.launch {
            try {
                api.gerarCapa(id)
                capaVersao += 1
                catalogo = catalogo.map { if (it.id == id) it.copy(capa = true) else it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val problema = e.paraErroApp()
                if (problema.sessaoInvalida) encerrarSessao() else erro = problema.mensagem
            } finally {
                gerandoCapa = null
            }
        }
    }

    // ---- Retrato gerado por IA ----
    // `retratoVersao` muda quando a imagem é refeita, para o app não mostrar a antiga.
    var retratoVersao by mutableStateOf(0)
        private set

    var gerandoRetrato by mutableStateOf(false)
        private set

    var erroRetrato by mutableStateOf<String?>(null)
        private set

    fun limparErroRetrato() {
        erroRetrato = null
    }

    // Gera (ou regera) o retrato do personagem no servidor. Não bloqueia o resto do app.
    fun gerarRetrato() {
        val id = jogoAtual?.jogoId ?: return
        if (gerandoRetrato) return
        val api = obterApi() ?: return
        erroRetrato = null
        gerandoRetrato = true
        viewModelScope.launch {
            try {
                api.gerarRetrato(id)
                retratoVersao += 1
                jogoAtual = jogoAtual?.copy(retrato = true)
                jogos = jogos.map { if (it.jogoId == id) it.copy(retrato = true) else it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val problema = e.paraErroApp()
                if (problema.sessaoInvalida) encerrarSessao() else erroRetrato = problema.mensagem
            } finally {
                gerandoRetrato = false
            }
        }
    }

    // Sorteia outro avatar desenhado (e descarta o retrato por IA, se houver).
    fun sortearAvatar() {
        val id = jogoAtual?.jogoId ?: return
        executar("Sorteando novo avatar...") { api ->
            val resposta = api.sortearAvatar(id)
            jogoAtual = jogoAtual?.copy(avatarSeed = resposta.avatarSeed, retrato = false)
            jogos = jogos.map {
                if (it.jogoId == id) it.copy(avatarSeed = resposta.avatarSeed, retrato = false) else it
            }
            retratoVersao += 1
        }
    }

    var jogoAtual by mutableStateOf<JogoResponse?>(null)
        private set

    // Mensagens da aventura atual, em estilo chat (o servidor guarda o histórico;
    // aqui acumulamos a cada turno para mostrar tudo na tela).
    var chat by mutableStateOf<List<MensagemDto>>(emptyList())
        private set

    // Dano sofrido no último turno (para o efeito visual de dano na tela de jogo).
    var danoRecente by mutableStateOf(0)
        private set

    // A escolha que o jogador acabou de tocar, mostrada no chat na hora (na frente do
    // indicador de "digitando") enquanto a IA escreve a "situacao" dela.
    var acaoPendente by mutableStateOf<String?>(null)
        private set

    fun limparDano() {
        danoRecente = 0
    }

    // Registra a nova cena e acumula a mensagem no chat, calculando o dano sofrido.
    private fun aplicarTurno(resposta: JogoResponse) {
        val vidaAntes = jogoAtual?.personagem?.vida ?: resposta.personagem.vida
        danoRecente = (vidaAntes - resposta.personagem.vida).coerceAtLeast(0)
        jogoAtual = resposta
        chat = chat + resposta.historico.orEmpty()
        (resposta.livens ?: resposta.mochis)?.let { novoSaldo ->
            conta = conta?.copy(livens = novoSaldo)
        }
    }

    private fun aplicarJogoCompleto(resposta: JogoResponse) {
        jogoAtual = resposta
        chat = resposta.historico.orEmpty()
        (resposta.livens ?: resposta.mochis)?.let { novoSaldo ->
            conta = conta?.copy(livens = novoSaldo)
        }
    }

    // ---- Atualização do aplicativo via GitHub ----
    private val versaoInicial = AtualizadorGitHub.obterVersaoInstalada(application)

    var versaoInstaladaCodigo by mutableStateOf(versaoInicial.first)
        private set

    var versaoInstaladaNome by mutableStateOf(versaoInicial.second)
        private set

    var infoAtualizacao by mutableStateOf<InfoAtualizacao?>(null)
        private set

    var temNovaAtualizacao by mutableStateOf(false)
        private set

    var mostrarDialogoAtualizacao by mutableStateOf(false)
        private set

    var verificandoAtualizacao by mutableStateOf(false)
        private set

    var baixandoAtualizacao by mutableStateOf(false)
        private set

    var progressoAtualizacao by mutableStateOf(0f)
        private set

    var statusAtualizacao by mutableStateOf<String?>(null)
        private set

    var erroAtualizacao by mutableStateOf<String?>(null)
        private set

    var apkAtualizacaoBaixado by mutableStateOf<java.io.File?>(null)
        private set

    init {
        ApiClient.token = prefs.token
        verificarAtualizacaoGitHub(manual = false)
        if (tela == Tela.Personagens) {
            // Retoma a última aventura se houver uma salva no aparelho.
            if (!prefs.ultimoJogoId.isNullOrEmpty()) retomarUltimoJogo()
            else carregarPersonagens()
        }
    }

    // ---- Infraestrutura ----
    // Muda sempre que o app troca de tela por vontade do usuário (ou por link).
    // Serve para o "retomar a última aventura" não sobrescrever uma escolha mais nova.
    private var versaoNavegacao = 0

    // Há sessão salva? Na tela de convite ainda não há.
    private val temSessao: Boolean
        get() = !prefs.token.isNullOrEmpty()

    private fun obterApi(): LivennIAApi? {
        val url = normalizarServidor(servidor)
        if (url == null) {
            erro = "Endereço do servidor inválido. Exemplo: http://192.168.0.15:8000"
            return null
        }
        return ApiClient.obter(url)
    }

    // Roda uma ação de rede com indicador de "ocupado" e tratamento de erro.
    // `tentativas`: quantas vezes tentar em caso de falha de rede/timeout (não repete em 4xx).
    private fun executar(mensagem: String, tentativas: Int = 1, bloco: suspend (LivennIAApi) -> Unit) {
        if (ocupado) return
        erro = null
        aviso = null
        val api = obterApi()
        if (api == null) {
            acaoPendente = null
            return
        }
        mensagemOcupado = mensagem
        ocupado = true
        viewModelScope.launch {
            try {
                var ultimoErro: Throwable? = null
                var ok = false
                // Importante: usar `for` com `break`. Antes era `repeat` com `return@repeat`,
                // que NÃO interrompe o laço — cada tentativa enviava de novo, aplicando
                // vários turnos na história a cada clique.
                for (i in 0 until tentativas) {
                    try {
                        bloco(api)
                        ok = true
                        break
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        ultimoErro = e
                        val problema = e.paraErroApp()
                        // Sessão inválida ou 4xx (422 opção inválida, 429 cota...) não se repete:
                        // repetir uma jogada criaria turnos extras na aventura.
                        if (problema.sessaoInvalida || e is retrofit2.HttpException && e.code() in 400..499) {
                            break
                        }
                        if (i < tentativas - 1) {
                            kotlinx.coroutines.delay(1000L * (i + 1))
                        }
                    }
                }
                if (!ok && ultimoErro != null) {
                    val problema = ultimoErro!!.paraErroApp()
                    erro = problema.mensagem
                    if (problema.sessaoInvalida) encerrarSessao()
                }
            } finally {
                ocupado = false
                acaoPendente = null
            }
        }
    }

    private suspend fun atualizarListas(api: LivennIAApi) {
        conta = api.conta()
        jogos = api.jogos()
    }

    private fun encerrarSessao() {
        prefs.limparSessao()
        ApiClient.token = null
        jogoAtual = null
        chat = emptyList()
        jogos = emptyList()
        conta = null
        tela = Tela.Entrada
    }

    // Reabre a última aventura do jogador (o app fecha e volta de onde parou).
    private fun retomarUltimoJogo() {
        val id = prefs.ultimoJogoId ?: return carregarPersonagens()
        val api = obterApi() ?: return
        val versao = versaoNavegacao
        viewModelScope.launch {
            try {
                val resposta = api.jogo(id)
                // Alguém (o usuário ou um link) já escolheu outra tela: não mexemos mais.
                if (versaoNavegacao != versao) return@launch
                jogoAtual = resposta
                chat = resposta.historico.orEmpty()
                tela = Tela.Jogo
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (versaoNavegacao != versao) return@launch
                // Jogo apagado ou sessão inválida: volta para a lista normalmente.
                prefs.ultimoJogoId = null
                jogoAtual = null
                chat = emptyList()
                carregarPersonagens()
            }
        }
    }

    // ---- Ações das telas ----
    fun alterarServidor(texto: String) {
        servidor = texto
    }

    fun limparErro() {
        erro = null
    }

    fun definirErro(mensagem: String) {
        erro = mensagem
    }

    fun definirAviso(mensagem: String) {
        aviso = mensagem
    }

    fun limparAviso() {
        aviso = null
    }

    fun navegarPara(novaTela: Tela) {
        versaoNavegacao++
        tela = novaTela
    }

    fun marcarOnboardingVisto() {
        prefs.onboardingVisto = true
    }

    fun entrar(codigo: String, apelidoDigitado: String) {
        executar("Entrando...") { api ->
            val resposta = api.registrar(RegistroRequest(codigo.trim(), apelidoDigitado.trim()))
            prefs.token = resposta.token
            prefs.apelido = resposta.apelido
            prefs.servidor = servidor.trim()
            ApiClient.token = resposta.token
            apelido = resposta.apelido
            // O servidor avisou que já existia uma conta com esse apelido e ela foi
            // recuperada: as aventuras antigas voltam para a lista.
            if (resposta.recuperado) {
                aviso = "Bem-vindo de volta! Suas aventuras antigas estão aqui."
            }
            tela = Tela.Personagens
            atualizarListas(api)
        }
    }

    fun sair() {
        erro = null
        aviso = null
        firebaseAuth.signOut()
        encerrarSessao()
    }

    private suspend fun trocarTokenFirebase() {
        val usuario = firebaseAuth.currentUser
            ?: throw IllegalStateException("Sem usuário autenticado.")
        val idToken = usuario.getIdToken(false).await().token
            ?: throw IllegalStateException("Token inválido.")
        val api = obterApi() ?: throw IllegalStateException("Sem API.")
        val resposta = api.authFirebase(com.livennia.app.FirebaseAuthIn(idToken))
        prefs.token = resposta.token
        prefs.apelido = resposta.apelido
        prefs.servidor = servidor.trim()
        ApiClient.token = resposta.token
        apelido = resposta.apelido
        tela = Tela.Personagens
        atualizarListas(api)
    }

    fun loginComEmail(email: String, senha: String) {
        executar("Entrando...") { _ ->
            firebaseAuth.signInWithEmailAndPassword(email.trim(), senha).await()
            trocarTokenFirebase()
        }
    }

    fun cadastrarComEmail(nome: String, email: String, senha: String) {
        executar("Criando conta...") { _ ->
            val resultado = firebaseAuth.createUserWithEmailAndPassword(email.trim(), senha).await()
            resultado.user?.updateProfile(
                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(nome.trim().ifEmpty { "Viajante" })
                    .build()
            )?.await()
            trocarTokenFirebase()
        }
    }

    fun recuperarSenha(email: String) {
        executar("Enviando email...") { _ ->
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            aviso = "Email de recuperação enviado. Verifique sua caixa de entrada."
        }
    }

    fun loginComGoogle(idToken: String) {
        executar("Entrando com Google...") { _ ->
            val credencial = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credencial).await()
            trocarTokenFirebase()
        }
    }

    fun carregarPersonagens() {
        // Sem sessão não há o que carregar: isso só causaria um 401 confuso.
        if (!temSessao) {
            tela = Tela.Entrada
            return
        }
        executar("Carregando...") { api ->
            atualizarListas(api)
            try { generos = api.generos() } catch (_: Exception) {}
        }
        // A vitrine vem em paralelo, sem travar a lista de aventuras.
        carregarCatalogo()
    }

    fun criarPersonagem(nome: String) {
        val nomeLimpo = nome.trim().take(30).ifEmpty { "Viajante" }
        val cenario = cenarioSelecionado
        val genero = cenario?.genero?.takeIf { it.isNotBlank() } ?: generoSelecionado
        val premissaLimpa = premissa.trim().takeIf { it.isNotEmpty() }
        val fichaAparencia = aparencia.trim().takeIf { it.isNotEmpty() }
        val fichaDescricao = descricao.trim().takeIf { it.isNotEmpty() }
        val fichaIdade = idade.trim().takeIf { it.isNotEmpty() }
        val fichaInteresses = interesses.trim().takeIf { it.isNotEmpty() }
        val fichaAversoes = aversoes.trim().takeIf { it.isNotEmpty() }
        val cerebro = cerebroSelecionado
        val modo = modoCriacao
        executar("O narrador está escrevendo o início da sua história...") { api ->
            val resposta = api.criarJogo(
                NovoJogoRequest(
                    nomePersonagem = nomeLimpo,
                    modo = modo,
                    genero = genero,
                    premissa = premissaLimpa,
                    aparencia = fichaAparencia,
                    descricao = fichaDescricao,
                    idade = fichaIdade,
                    interesses = fichaInteresses,
                    aversoes = fichaAversoes,
                    cenarioId = cenario?.id,
                    modeloId = cerebro
                )
            )
            aplicarJogoCompleto(resposta)
            retratoVersao = 0
            erroRetrato = null
            prefs.ultimoJogoId = resposta.jogoId
            abaPrincipal = if (resposta.modo == "historia") 1 else 0
            tela = Tela.Jogo
            limparFicha()
            // A imagem demora: pedimos depois, sem travar a abertura da aventura.
            if (!resposta.retrato) gerarRetrato()
        }
    }

    fun abrirJogo(id: String) {
        versaoNavegacao++
        executar("Abrindo sua aventura...") { api ->
            val resposta = api.jogo(id)
            aplicarJogoCompleto(resposta)
            retratoVersao = 0
            erroRetrato = null
            prefs.ultimoJogoId = id
            abaPrincipal = if (resposta.modo == "historia") 1 else 0
            tela = Tela.Jogo
        }
    }

    fun voltarParaPersonagens() {
        versaoNavegacao++
        val eraHistoria = jogoAtual?.modo == "historia"
        jogoAtual = null
        chat = emptyList()
        if (!temSessao) {
            tela = Tela.Entrada
            return
        }
        abaPrincipal = if (eraHistoria) 1 else 0
        tela = Tela.Personagens
        carregarPersonagens()
    }

    // Abre a tela inicial (usada pelo link livennia://home e pelo widget).
    fun irParaInicio() {
        versaoNavegacao++
        if (!temSessao) {
            tela = Tela.Entrada
            return
        }
        tela = Tela.Personagens
        carregarPersonagens()
    }

    fun jogarOpcao(numero: Int) {
        val id = jogoAtual?.jogoId ?: return
        // Mostra a escolha no chat imediatamente; quando a IA responder, ela é trocada
        // pela "situacao" (o gesto do personagem escrito pelo narrador).
        acaoPendente = jogoAtual?.cena?.opcoes?.getOrNull(numero - 1)
        // O mesmo pedido é reaproveitado nas tentativas, então uma repetição não
        // avança a história duas vezes.
        val pedido = java.util.UUID.randomUUID().toString()
        executar("O narrador está pensando...", tentativas = 3) { api ->
            aplicarTurno(api.turno(id, TurnoRequest(opcao = numero, pedidoId = pedido)))
        }
    }

    fun jogarAcao(texto: String) {
        val id = jogoAtual?.jogoId ?: return
        val acao = texto.trim().take(300)
        if (acao.isEmpty()) return
        if (filtroPalavrasSensiveis) {
            val bloqueada = FiltroPalavras.verificar(getApplication(), acao)
            if (bloqueada != null) {
                erro = "Mensagem bloqueada pelo filtro de palavras sensíveis (você pode ajustar nas Configurações)."
                return
            }
        }
        acaoPendente = acao
        val pedido = java.util.UUID.randomUUID().toString()
        executar("O narrador está pensando...", tentativas = 3) { api ->
            aplicarTurno(api.turno(id, TurnoRequest(acao = acao, pedidoId = pedido)))
        }
    }

    // Pede para a IA continuar a narrativa no Modo História sem que o jogador precise falar algo.
    fun continuarHistoria() {
        val id = jogoAtual?.jogoId ?: return
        acaoPendente = null
        val pedido = java.util.UUID.randomUUID().toString()
        executar("O narrador está continuando a história...", tentativas = 3) { api ->
            aplicarTurno(api.turno(id, TurnoRequest(acao = "[CONTINUAR_HISTORIA]", pedidoId = pedido)))
        }
    }

    // ---- Recursos LivennIA ----
    var sugestoesAutoResposta by mutableStateOf<List<String>>(emptyList())
        private set

    var gerandoAutoResposta by mutableStateOf(false)
        private set

    fun limparSugestoesAutoResposta() {
        sugestoesAutoResposta = emptyList()
    }

    fun resgatarLivensDiarios() {
        executar("Resgatando bônus diário de Livens...") { api ->
            val res = api.resgatarLivensDiarios()
            conta = conta?.copy(livens = res.livens, resgateDiarioDisponivel = false)
            runCatching { cerebros = api.cerebros() }
            aviso = if (res.jaResgatado) {
                "Você já resgatou seus Livens de hoje! Saldo: ${res.livens} ✨"
            } else {
                "+${res.ganho} Livens resgatados! Saldo atual: ${res.livens} ✨"
            }
        }
    }

    fun resgatarMochiDiario() = resgatarLivensDiarios()

    fun configurarModeloDoJogo(modeloId: String?, temperatura: Float?) {
        val id = jogoAtual?.jogoId ?: return
        executar("Atualizando cérebro de IA e temperatura...") { api ->
            val resposta = api.configurarModelo(id, ConfigModeloRequest(modeloId = modeloId, temperatura = temperatura))
            aplicarJogoCompleto(resposta)
            aviso = "Modelo e temperatura atualizados!"
        }
    }

    fun regenerarUltimaResposta() {
        val id = jogoAtual?.jogoId ?: return
        executar("Regenerando resposta do narrador...", tentativas = 2) { api ->
            val resposta = api.regenerar(id)
            aplicarJogoCompleto(resposta)
        }
    }

    fun trocarVersaoMensagem(indice: Int, versaoIdx: Int) {
        val id = jogoAtual?.jogoId ?: return
        executar("Alternando versão...") { api ->
            val resposta = api.trocarVersao(id, TrocarVersaoRequest(indice = indice, versaoIdx = versaoIdx))
            aplicarJogoCompleto(resposta)
        }
    }

    fun editarMensagem(indice: Int, narrativa: String? = null, acao: String? = null) {
        val id = jogoAtual?.jogoId ?: return
        if (filtroPalavrasSensiveis) {
            val textoVerificar = listOfNotNull(narrativa, acao).joinToString(" ")
            if (FiltroPalavras.verificar(getApplication(), textoVerificar) != null) {
                erro = "Texto bloqueado pelo filtro de palavras sensíveis."
                return
            }
        }
        executar("Salvando edição da fala...") { api ->
            val resposta = api.editarMensagem(
                id,
                EditarMensagemRequest(indice = indice, narrativa = narrativa, acao = acao)
            )
            aplicarJogoCompleto(resposta)
            aviso = "Fala atualizada!"
        }
    }

    fun gerarSugestoesAutoResposta(aoPrimeiraSugestao: ((String) -> Unit)? = null) {
        val id = jogoAtual?.jogoId ?: return
        if (gerandoAutoResposta) return
        val api = obterApi() ?: return
        gerandoAutoResposta = true
        viewModelScope.launch {
            try {
                val res = api.autoResposta(id)
                sugestoesAutoResposta = res.sugestoes
                if (aoPrimeiraSugestao != null && res.sugestoes.isNotEmpty()) {
                    aoPrimeiraSugestao(res.sugestoes.first())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val problema = e.paraErroApp()
                if (problema.sessaoInvalida) encerrarSessao() else erro = problema.mensagem
            } finally {
                gerandoAutoResposta = false
            }
        }
    }

    fun salvarMemoria(memoriaTexto: String) {
        val id = jogoAtual?.jogoId ?: return
        executar("Salvando memória de longo prazo...") { api ->
            val res = api.atualizarMemoria(id, MemoriaRequest(memoria = memoriaTexto, autoResumir = false))
            jogoAtual = jogoAtual?.copy(memoria = res.memoria)
            aviso = "Memória da história atualizada!"
        }
    }

    fun resumirMemoriaComIA() {
        val id = jogoAtual?.jogoId ?: return
        executar("A IA está resumindo os fatos importantes da aventura...") { api ->
            val res = api.atualizarMemoria(id, MemoriaRequest(autoResumir = true))
            jogoAtual = jogoAtual?.copy(memoria = res.memoria)
            aviso = "Memória resumida automaticamente pela IA!"
        }
    }

    fun gerarPaginaDiario() {
        val id = jogoAtual?.jogoId ?: return
        executar("O personagem está escrevendo no diário secreto...") { api ->
            val nova = api.gerarDiario(id)
            val atuais = jogoAtual?.diarios.orEmpty()
            jogoAtual = jogoAtual?.copy(diarios = atuais + nova)
            aviso = "Nova página do diário revelada!"
        }
    }

    fun gerarCartaMemoria() {
        val id = jogoAtual?.jogoId ?: return
        executar("Escrevendo uma carta de memória especial...") { api ->
            val nova = api.gerarCarta(id)
            val atuais = jogoAtual?.cartas.orEmpty()
            jogoAtual = jogoAtual?.copy(cartas = atuais + nova)
            aviso = "Você recebeu uma nova Carta de Memória!"
        }
    }

    fun criarSavepoint(nome: String?) {
        val id = jogoAtual?.jogoId ?: return
        executar("Criando ponto de salvamento...") { api ->
            val novo = api.criarSavepoint(id, SavepointRequest(nome = nome?.trim()?.takeIf { it.isNotEmpty() }))
            val atuais = jogoAtual?.savepoints.orEmpty()
            jogoAtual = jogoAtual?.copy(savepoints = (atuais + novo).takeLast(10))
            aviso = "Ponto de salvamento '${novo.nome}' criado!"
        }
    }

    fun carregarSavepoint(saveId: String) {
        val id = jogoAtual?.jogoId ?: return
        executar("Voltando no tempo para o ponto salvo...") { api ->
            val resposta = api.carregarSavepoint(id, saveId)
            aplicarJogoCompleto(resposta)
            aviso = "História restaurada para o turno ${resposta.turno}!"
        }
    }

    fun apagarSavepoint(saveId: String) {
        val id = jogoAtual?.jogoId ?: return
        executar("Removendo ponto salvo...") { api ->
            api.apagarSavepoint(id, saveId).garantir()
            val atuais = jogoAtual?.savepoints.orEmpty().filterNot { it.id == saveId }
            jogoAtual = jogoAtual?.copy(savepoints = atuais)
        }
    }

    fun apagarJogo(id: String) {
        executar("Apagando personagem...") { api ->
            api.apagarJogo(id).garantir()
            if (prefs.ultimoJogoId == id) prefs.ultimoJogoId = null
            atualizarListas(api)
        }
    }

    fun apagarConta() {
        executar("Apagando sua conta...") { api ->
            api.apagarConta().garantir()
            encerrarSessao()
            aviso = "Sua conta e todos os seus dados foram apagados."
        }
    }

    fun enviarFeedback(nota: Int, comentario: String) {
        executar("Enviando...") { api ->
            val texto = comentario.trim().takeIf { it.isNotEmpty() }
            api.feedback(FeedbackRequest(nota, texto)).garantir()
            aviso = "Obrigado pelo feedback!"
        }
    }

    fun denunciar(motivo: String, detalhe: String) {
        val id = jogoAtual?.jogoId ?: return
        executar("Enviando denúncia...") { api ->
            val texto = detalhe.trim().takeIf { it.isNotEmpty() }
            api.denuncia(DenunciaRequest(id, motivo, texto)).garantir()
            aviso = "Denúncia enviada. Obrigado por avisar."
        }
    }

    fun fecharDialogoAtualizacao() {
        if (!baixandoAtualizacao) {
            mostrarDialogoAtualizacao = false
        }
    }

    fun verificarAtualizacaoGitHub(manual: Boolean = false) {
        if (verificandoAtualizacao || baixandoAtualizacao) return
        verificandoAtualizacao = true
        erroAtualizacao = null
        if (manual) statusAtualizacao = "Consultando repositório no GitHub..."
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                val res = AtualizadorGitHub.verificar(app)
                versaoInstaladaCodigo = res.versaoInstaladaCodigo
                versaoInstaladaNome = res.versaoInstaladaNome
                infoAtualizacao = res.info
                temNovaAtualizacao = res.disponivel
                if (res.disponivel && res.info != null) {
                    statusAtualizacao = "Nova versão v${res.info.versaoNome} disponível! ${res.info.notas}"
                    mostrarDialogoAtualizacao = true
                } else if (res.info != null) {
                    statusAtualizacao = "Você já está na versão mais recente do GitHub (v${res.info.versaoNome})."
                    if (manual) aviso = "Aplicativo já está atualizado (v${res.versaoInstaladaNome})."
                } else {
                    statusAtualizacao = "Nenhuma versão publicada ainda no GitHub."
                    if (manual) aviso = "Nenhuma versão publicada ainda no repositório GitHub."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                statusAtualizacao = "Não foi possível verificar o GitHub agora."
                if (manual) erro = "Falha ao consultar GitHub: ${e.message ?: "erro de rede"}"
            } finally {
                verificandoAtualizacao = false
            }
        }
    }

    fun iniciarDownloadOuInstalacaoGitHub() {
        val app = getApplication<Application>()
        val arquivoExistente = apkAtualizacaoBaixado
        if (arquivoExistente != null && arquivoExistente.exists()) {
            val abriuInstalador = AtualizadorGitHub.instalarApk(app, arquivoExistente)
            if (!abriuInstalador) {
                erroAtualizacao = "Permita 'Instalar apps desconhecidos' para o LivennIA e toque em Instalar novamente."
                aviso = "Autorize a instalação desta fonte e toque em Instalar novamente."
            }
            return
        }

        val info = infoAtualizacao ?: return
        if (baixandoAtualizacao) return
        baixandoAtualizacao = true
        progressoAtualizacao = 0f
        erroAtualizacao = null
        statusAtualizacao = "Baixando APK v${info.versaoNome} do GitHub..."
        viewModelScope.launch {
            try {
                val arquivo = AtualizadorGitHub.baixarApk(app, info.apkUrl) { prog ->
                    progressoAtualizacao = prog
                }
                apkAtualizacaoBaixado = arquivo
                statusAtualizacao = "Download concluído! Abrindo instalador..."
                val abriuInstalador = AtualizadorGitHub.instalarApk(app, arquivo)
                if (!abriuInstalador) {
                    erroAtualizacao = "Ative a permissão 'Instalar apps desconhecidos' na tela que abriu e depois toque em 'Instalar atualização agora'."
                    mostrarDialogoAtualizacao = true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val msg = e.message ?: "Erro ao baixar atualização do GitHub."
                erroAtualizacao = msg
                statusAtualizacao = msg
                erro = msg
            } finally {
                baixandoAtualizacao = false
            }
        }
    }
}