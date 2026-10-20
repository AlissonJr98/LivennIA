package com.livennia.app

import android.graphics.Color as CorDoSistema
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

class MainActivity : FragmentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // O app é sempre escuro: ícones claros nas barras do sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(CorDoSistema.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(CorDoSistema.TRANSPARENT)
        )

        setContent {
            val contexto = LocalContext.current
            var ultimoToqueVoltar by remember { mutableLongStateOf(0L) }

            // Controle inteligente do botão Voltar (Android Back):
            // Não fecha o app acidentalmente; navega de volta entre telas e abas.
            BackHandler(enabled = true) {
                when {
                    vm.tela == Tela.Jogo -> vm.voltarParaPersonagens()
                    vm.tela == Tela.NovaAventura -> vm.navegarPara(Tela.Personagens)
                    vm.tela == Tela.Configuracoes -> vm.navegarPara(Tela.Personagens)
                    vm.tela == Tela.Feedback -> vm.navegarPara(Tela.Personagens)
                    vm.tela in listOf(Tela.Login, Tela.Cadastro, Tela.RecuperarSenha) -> vm.navegarPara(Tela.Entrada)
                    vm.tela == Tela.Personagens -> {
                        if (vm.abaPrincipal != 0) {
                            vm.selecionarAba(0)
                        } else {
                            val agora = System.currentTimeMillis()
                            if (agora - ultimoToqueVoltar < 2000L) {
                                finish()
                            } else {
                                ultimoToqueVoltar = agora
                                Toast.makeText(contexto, "Pressione novamente para sair", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> finish()
                }
            }

            TemaLivennIA(estilo = vm.estiloTema, escuro = vm.temaEscuro, corNeon = vm.corNeon) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = vm.tela,
                        transitionSpec = {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                        },
                        label = "tela"
                    ) { tela ->
                        when (tela) {
                            Tela.Onboarding -> OnboardingTela(vm)
                            Tela.Entrada -> EntradaTela(vm)
                            Tela.Login -> LoginTela(vm)
                            Tela.Cadastro -> CadastroTela(vm)
                            Tela.RecuperarSenha -> RecuperarSenhaTela(vm)
                            Tela.Personagens -> PersonagensTela(vm)
                            Tela.Configuracoes -> ConfiguracoesTela(vm)
                            Tela.NovaAventura -> NovaAventuraTela(vm)
                            Tela.Jogo -> JogoTela(vm)
                            Tela.Feedback -> FeedbackTela(vm)
                        }
                    }

                    val infoAtu = vm.infoAtualizacao
                    if (vm.mostrarDialogoAtualizacao && infoAtu != null) {
                        DialogoAtualizacaoGitHub(
                            info = infoAtu,
                            versaoInstalada = vm.versaoInstaladaNome,
                            baixando = vm.baixandoAtualizacao,
                            progresso = vm.progressoAtualizacao,
                            apkPronto = vm.apkAtualizacaoBaixado?.exists() == true,
                            erro = vm.erroAtualizacao,
                            onBaixarOuInstalar = { vm.iniciarDownloadOuInstalacaoGitHub() },
                            onFechar = { vm.fecharDialogoAtualizacao() }
                        )
                    }
                }
            }
        }

        // Deep link: livennia://jogo/{id} ou livennia://home
        intent?.data?.let { uri -> processarLink(uri) }
    }

    private fun processarLink(uri: android.net.Uri) {
        if (uri.scheme != "livennia") return
        when (uri.host) {
            "jogo" -> {
                val id = uri.lastPathSegment
                if (!id.isNullOrBlank()) vm.abrirJogo(id)
            }
            "home" -> vm.irParaInicio()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri -> processarLink(uri) }
    }
}