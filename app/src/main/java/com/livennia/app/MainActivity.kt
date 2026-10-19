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
import androidx.compose.ui.Modifier

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