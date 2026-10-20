package com.livennia.app

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

private enum class DialogoAberto {
    Feedback,
    Denuncia,
    Memoria,
    Diarios,
    Cartas,
    Savepoints,
    ModeloIA,
    Vinculo
}

data class NivelVinculo(
    val nivel: Int,
    val titulo: String,
    val descricao: String,
    val progresso: Float,
    val icone: String = "✦"
)

fun calcularVinculo(turno: Int): NivelVinculo {
    return when {
        turno <= 2 -> NivelVinculo(
            nivel = 1,
            titulo = "Primeiras Palavras",
            descricao = "Vocês acabaram de se encontrar. O personagem ainda está observando sua postura e avaliando suas intenções.",
            progresso = (turno / 3f).coerceIn(0.15f, 1f),
            icone = "🌱"
        )
        turno <= 6 -> NivelVinculo(
            nivel = 2,
            titulo = "Curiosidade Mútua",
            descricao = "O gelo inicial foi quebrado. O personagem demonstra genuíno interesse nas suas respostas e atitudes.",
            progresso = ((turno - 2) / 4f).coerceIn(0.15f, 1f),
            icone = "✨"
        )
        turno <= 12 -> NivelVinculo(
            nivel = 3,
            titulo = "Cúmplices",
            descricao = "Uma sintonia evidente surgiu entre vocês. O personagem confia nos seus conselhos diante de escolhas difíceis.",
            progresso = ((turno - 6) / 6f).coerceIn(0.15f, 1f),
            icone = "🤝"
        )
        turno <= 20 -> NivelVinculo(
            nivel = 4,
            titulo = "Confiança Profunda",
            descricao = "O personagem compartilha segredos íntimos e sentimentos reais que não revela a mais ninguém.",
            progresso = ((turno - 12) / 8f).coerceIn(0.15f, 1f),
            icone = "💖"
        )
        else -> NivelVinculo(
            nivel = 5,
            titulo = "Vínculo Inquebrável",
            descricao = "Uma conexão inabalável e histórica forjada por cada momento compartilhado nesta jornada.",
            progresso = 1f,
            icone = "👑"
        )
    }
}

@Composable
fun JogoTela(vm: AppViewModel) {
    val jogo = vm.jogoAtual ?: return
    if (jogo.modo == "historia" || vm.modoLeitura == AppViewModel.ModoLeitura.HISTORIA) {
        JogoModoHistoria(vm)
        return
    }
    val chat = vm.chat
    val contexto = LocalContext.current
    val haptico = LocalHapticFeedback.current
    val areaDeCopia = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.erro, vm.aviso, vm.erroRetrato) {
        vm.erro?.let { snackbarHostState.showSnackbar(it); vm.limparErro() }
        vm.aviso?.let { snackbarHostState.showSnackbar(it); vm.limparAviso() }
        vm.erroRetrato?.let { snackbarHostState.showSnackbar(it); vm.limparErroRetrato() }
    }

    // Fala o texto do narrador em voz alta.
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(contexto) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("pt", "BR")
        }
        tts = engine
        onDispose { engine.stop(); engine.shutdown() }
    }

    val listaChat = rememberLazyListState()

    // Efeito "máquina de escrever" só na última fala do narrador.
    val narrativaAtual = chat.lastOrNull()?.narrativa ?: jogo.cena.narrativa
    var visiveis by remember(narrativaAtual) { mutableIntStateOf(0) }
    LaunchedEffect(narrativaAtual) {
        val ultimo = chat.size - 1
        visiveis = 0
        while (visiveis < narrativaAtual.length) {
            delay(14)
            visiveis = minOf(narrativaAtual.length, visiveis + 3)
            if (ultimo >= 0) {
                listaChat.scrollToItem(ultimo, 100000)
            }
        }
        if (ultimo >= 0) {
            listaChat.animateScrollToItem(ultimo, 100000)
        }
    }
    val terminou = visiveis >= narrativaAtual.length

    // ---- Efeito de dano ----
    val tremor = remember { Animatable(0f) }
    LaunchedEffect(vm.danoRecente) {
        if (vm.danoRecente > 0) {
            haptico.performHapticFeedback(HapticFeedbackType.LongPress)
            repeat(6) { i -> tremor.animateTo(if (i % 2 == 0) 10f else -10f, tween(45)) }
            tremor.animateTo(0f, tween(60))
            delay(450)
            vm.limparDano()
        }
    }

    var acaoLivre by remember(jogo.turno) { mutableStateOf("") }
    var dialogo by remember { mutableStateOf<DialogoAberto?>(null) }
    var indiceEdicao by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(chat.size, vm.ocupado, vm.acaoPendente) {
        // Itens extras no fim da lista: a fala já escolhida (enquanto a IA escreve a
        // "situacao" dela) e o indicador de "digitando".
        val extras = if (vm.ocupado) (if (vm.acaoPendente != null) 2 else 1) else 0
        val alvo = chat.size + extras - 1
        if (alvo >= 0) listaChat.animateScrollToItem(alvo, 100000)
    }

    BackHandler { vm.voltarParaPersonagens() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .graphicsLayer { translationX = tremor.value }
        ) {
            // ---- Barra de cima: flutua sobre o fundo, sem faixa dura ----
            Surface(color = Color.Transparent) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.voltarParaPersonagens() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    RetratoPersonagem(
                        jogoId = jogo.jogoId,
                        seed = jogo.avatarSeed,
                        nome = jogo.personagem.nome,
                        temRetrato = jogo.retrato,
                        tamanho = 38,
                        versao = vm.retratoVersao
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            jogo.personagem.nome,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            jogo.cenario?.let { c ->
                                "${c.emoji} ${c.nome} • turno ${jogo.turno}"
                            } ?: "${jogo.personagem.local} • turno ${jogo.turno}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSuave,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (vm.gerandoRetrato) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Ouro
                        )
                        Spacer(Modifier.width(4.dp))
                    } else {
                        IconButton(onClick = { vm.gerarRetrato() }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Gerar retrato",
                                tint = TextoSuave,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Surface(
                        color = SuperficieAlta,
                        shape = Cantos.pilula,
                        modifier = Modifier
                            .clickable { vm.definirModoLeitura(AppViewModel.ModoLeitura.HISTORIA) }
                    ) {
                        Text(
                            "Livro",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ouro,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = {
                        tts?.stop()
                        tts?.speak(narrativaAtual, TextToSpeech.QUEUE_FLUSH, null, "narrativa")
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ouvir",
                            tint = Ouro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { dialogo = DialogoAberto.Denuncia }) {
                        Icon(
                            Icons.Filled.Flag,
                            contentDescription = "Denunciar",
                            tint = TextoSuave,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ---- Barra de recursos LivennIA: Livens, Memória, Diário, Cartas, Saves, Motor/Temp ----
            BarraFerramentasLiven(
                vm = vm,
                jogo = jogo,
                onAbrirDialogo = { dialogo = it }
            )

            // ---- Vida e itens: um cartão solto, arredondado (a linguagem da referência) ----
            Surface(
                color = SuperficieAlta,
                shape = Cantos.cartao,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    BarraDeVida(vida = jogo.personagem.vida, vidaMax = jogo.personagem.vidaMax)
                    Spacer(Modifier.height(4.dp))
                    val itens = jogo.personagem.inventario.joinToString(", ").ifEmpty { "nenhum" }
                    Text(
                        "🎒 $itens",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSuave,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ---- Conversa ----
            LazyColumn(
                state = listaChat,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                items(chat.size) { indice ->
                    val msg = chat[indice]
                    val ehUltimo = indice == chat.size - 1
                    val textoMostrado = if (ehUltimo) msg.narrativa.take(visiveis) else msg.narrativa

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (indice > 0) {
                            BalaoJogador(
                                texto = msg.situacao ?: msg.acao,
                                tamanhoLetra = vm.tamanhoLetraNarrador,
                                hora = msg.emJogador
                            )
                        }
                        BlocoNarrador(
                            texto = textoMostrado,
                            eventos = if (ehUltimo && !terminou) emptyList() else msg.eventos,
                            clicavel = ehUltimo && !terminou,
                            aoTocar = { visiveis = narrativaAtual.length },
                            aoCopiar = { areaDeCopia.setText(AnnotatedString(msg.narrativa)) },
                            tamanhoLetra = vm.tamanhoLetraNarrador,
                            hora = msg.emNarrador,
                            versoes = msg.versoes,
                            versaoAtiva = msg.versaoAtiva,
                            ehUltimo = ehUltimo && !vm.ocupado,
                            aoTrocarVersao = { nova -> vm.trocarVersaoMensagem(indice, nova) },
                            aoRegenerar = { vm.regenerarUltimaResposta() },
                            aoEditar = { indiceEdicao = indice }
                        )
                    }
                }

                if (vm.ocupado) {
                    vm.acaoPendente?.let { pendente ->
                        item {
                            BalaoJogador(
                                texto = pendente,
                                tamanhoLetra = vm.tamanhoLetraNarrador,
                                hora = null,
                                pendente = true
                            )
                        }
                    }
                    item { BlocoDigitando() }
                }
                item(key = "fim_chat_espaco") {
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ---- Faixa de status + opções + barra de digitação ----
            Surface(
                color = Superficie,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChipTag("⚡ ${jogo.jogadasRestantesHoje}", destaque = true)
                            ChipTag(
                                "❤ ${jogo.personagem.vida}/${jogo.personagem.vidaMax}",
                                destaque = jogo.personagem.vida <= 30
                            )
                            ChipTag("📖 ${jogo.genero}")
                            Surface(
                                color = SuperficieAlta,
                                shape = Cantos.pilula,
                                border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f)),
                                modifier = Modifier.clickable(enabled = !vm.ocupado && !vm.gerandoAutoResposta) {
                                    vm.gerarSugestoesAutoResposta { primeira ->
                                        if (acaoLivre.isBlank()) acaoLivre = primeira
                                    }
                                }
                            ) {
                                Text(
                                    if (vm.gerandoAutoResposta) "⏳ Inspirando..." else "✨ Auto-resposta",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Ouro,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                        Text(
                            "✎",
                            color = TextoSuave,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clickable { dialogo = DialogoAberto.Feedback }
                                .padding(6.dp)
                        )
                    }

                    // Gaveta de Auto-Respostas LivennIA
                    val sugestoesAtuais = remember(vm.sugestoesAutoResposta, jogo.cena.opcoes) {
                        if (vm.sugestoesAutoResposta.isNotEmpty()) {
                            vm.sugestoesAutoResposta
                        } else if (jogo.cena.opcoes.isNotEmpty()) {
                            jogo.cena.opcoes
                        } else {
                            listOf(
                                "Perguntar mais sobre isso",
                                "Observar a reação com atenção",
                                "Dar um passo à frente com cautela"
                            )
                        }
                    }
                    FaixaSugestoesAutoResposta(
                        sugestoes = sugestoesAtuais,
                        ocupado = vm.ocupado,
                        gerandoIA = vm.gerandoAutoResposta,
                        aoPedirSugestoesIA = { vm.gerarSugestoesAutoResposta() },
                        aoEscolher = { sugestao ->
                            acaoLivre = sugestao
                            haptico.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        }
                    )

                    if (jogo.fimDeJogo) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                "Fim de jogo. ${jogo.personagem.nome} não resistiu.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { vm.voltarParaPersonagens() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Voltar às aventuras") }
                        }
                    } else {
                        if (!terminou) {
                            Text(
                                "Toque na fala do narrador para pular a animação.",
                                color = TextoSuave,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        } else if (jogo.cena.opcoes.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                jogo.cena.opcoes.forEachIndexed { indice, opcao ->
                                    Surface(
                                        color = SuperficieAlta,
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Ouro.copy(alpha = 0.55f)),
                                        modifier = Modifier.clickable(enabled = !vm.ocupado) {
                                            haptico.performHapticFeedback(HapticFeedbackType.LongPress)
                                            vm.jogarOpcao(indice + 1)
                                        }
                                    ) {
                                        Text(
                                            text = opcao,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            RetratoPersonagem(
                                jogoId = jogo.jogoId,
                                seed = jogo.avatarSeed,
                                nome = jogo.personagem.nome,
                                temRetrato = jogo.retrato,
                                tamanho = 34,
                                versao = vm.retratoVersao
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = acaoLivre,
                                onValueChange = { acaoLivre = it.take(300) },
                                placeholder = { Text("Escreva sua ação...") },
                                maxLines = 3,
                                enabled = !vm.ocupado,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    val texto = acaoLivre
                                    acaoLivre = ""
                                    vm.jogarAcao(texto)
                                },
                                enabled = !vm.ocupado && acaoLivre.isNotBlank()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar",
                                    tint = Ouro
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---- Efeito de dano: piscada vermelha + selo "-X ❤" ----
        AnimatedVisibility(
            visible = vm.danoRecente > 0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0x4DFF0000))) {
                Surface(
                    color = Color(0xE6B3261E),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.align(Alignment.Center).padding(top = 40.dp)
                ) {
                    Text(
                        text = "-${vm.danoRecente} ❤",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    GerenciadorDialogosJogo(
        vm = vm,
        jogo = jogo,
        dialogo = dialogo,
        indiceEdicao = indiceEdicao,
        onFecharDialogo = { dialogo = null },
        onFecharEdicao = { indiceEdicao = null }
    )

    SnackbarHost(hostState = snackbarHostState)
    Confetti(explosao = jogo.turno == 1)
}

// Hora de cada fala, no formato do aparelho (ex.: "14:32").
private val formatoDaHora = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())

private fun horaDaMensagem(milissegundos: Long): String =
    formatoDaHora.format(java.util.Date(milissegundos))

// ---- Fala do jogador: bolha colorida à direita, com a hora embaixo ----
@Composable
private fun BalaoJogador(
    texto: String,
    tamanhoLetra: Float,
    hora: Long?,
    pendente: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            color = if (pendente) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.primary
            },
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp),
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            Text(
                text = texto,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (tamanhoLetra - 1f).sp,
                    lineHeight = ((tamanhoLetra - 1f) * 1.4f).sp
                ),
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
            )
        }
        if (hora != null) {
            Text(
                text = horaDaMensagem(hora),
                color = TextoSuave.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(end = 4.dp, top = 3.dp)
            )
        }
    }
}

// ---- Fala do narrador: texto literário e controles LivennIA no rodapé ----
@Composable
private fun BlocoNarrador(
    texto: String,
    eventos: List<EventoDto>,
    clicavel: Boolean,
    aoTocar: () -> Unit,
    aoCopiar: () -> Unit,
    tamanhoLetra: Float,
    hora: Long? = null,
    versoes: List<String> = emptyList(),
    versaoAtiva: Int = 0,
    ehUltimo: Boolean = false,
    aoTrocarVersao: (Int) -> Unit = {},
    aoRegenerar: () -> Unit = {},
    aoEditar: () -> Unit = {}
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.Top) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 3.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = Ouro,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "⧉",
                color = TextoSuave,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { aoCopiar() }.padding(2.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Surface(
            color = SuperficieAlta,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Ouro.copy(alpha = 0.22f)),
            modifier = Modifier.weight(1f).clickable(enabled = clicavel) { aoTocar() }
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = parsearNarrativa(texto),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = tamanhoLetra.sp,
                        lineHeight = (tamanhoLetra * 1.45f).sp
                    )
                )
                if (eventos.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    for (ev in eventos) {
                        val cor = when {
                            ev.tipo == "vida" && ev.texto.contains("perdeu") -> MaterialTheme.colorScheme.error
                            ev.tipo == "vida" -> Color(0xFF7BC47F)
                            ev.tipo == "item_ganho" -> Ouro
                            else -> TextoSuave
                        }
                        Text("• ${ev.texto}", color = cor, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
                RodapeMensagemLiven(
                    rotuloEsquerda = hora?.let { horaDaMensagem(it) } ?: "",
                    versoes = versoes,
                    versaoAtiva = versaoAtiva,
                    ehUltimo = ehUltimo,
                    aoTrocarVersao = aoTrocarVersao,
                    aoRegenerar = aoRegenerar,
                    aoEditar = aoEditar,
                    aoCopiar = aoCopiar
                )
            }
        }
    }
}

// ---- Indicador de que o narrador está escrevendo ----
@Composable
private fun BlocoDigitando() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Box(
            modifier = Modifier.size(16.dp).background(Ouro, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {}
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.width(110.dp).padding(top = 6.dp)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
        }
    }
}


@Composable
private fun JogoModoHistoria(vm: AppViewModel) {
    val jogo = vm.jogoAtual ?: return
    val ehHistoriaPura = jogo.modo == "historia"
    val chat = vm.chat
    val contexto = LocalContext.current
    val areaDeCopia = LocalClipboardManager.current
    val haptico = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listaChat = rememberLazyListState()

    LaunchedEffect(vm.erro, vm.aviso, vm.erroRetrato) {
        vm.erro?.let { snackbarHostState.showSnackbar(it); vm.limparErro() }
        vm.aviso?.let { snackbarHostState.showSnackbar(it); vm.limparAviso() }
        vm.erroRetrato?.let { snackbarHostState.showSnackbar(it); vm.limparErroRetrato() }
    }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(contexto) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("pt", "BR")
        }
        tts = engine
        onDispose { engine.stop(); engine.shutdown() }
    }

    val narrativaAtual = chat.lastOrNull()?.narrativa ?: jogo.cena.narrativa
    var visiveis by remember(narrativaAtual) { mutableIntStateOf(0) }
    LaunchedEffect(narrativaAtual) {
        val ultimo = chat.size - 1
        visiveis = 0
        while (visiveis < narrativaAtual.length) {
            delay(14)
            visiveis = minOf(narrativaAtual.length, visiveis + 3)
            if (ultimo >= 0) {
                listaChat.scrollToItem(ultimo, 100000)
            }
        }
        if (ultimo >= 0) {
            listaChat.animateScrollToItem(ultimo, 100000)
        }
    }
    val terminou = visiveis >= narrativaAtual.length

    var acaoLivre by remember(jogo.turno) { mutableStateOf("") }
    var mostrarSugestoes by remember(jogo.turno) { mutableStateOf(false) }
    var dialogo by remember { mutableStateOf<DialogoAberto?>(null) }
    var indiceEdicao by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(chat.size, vm.ocupado, vm.acaoPendente) {
        val extras = if (vm.ocupado) (if (vm.acaoPendente != null) 2 else 1) else 0
        val alvo = chat.size + extras - 1
        if (alvo >= 0) listaChat.animateScrollToItem(alvo, 100000)
    }

    BackHandler { vm.voltarParaPersonagens() }

    val corFala = MaterialTheme.colorScheme.onBackground
    val corAcao = TextoSuave.copy(alpha = 0.90f)
    val corNormal = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.93f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // ---- Cabeçalho imersivo estilo Livro ----
            Surface(color = Color.Transparent) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.voltarParaPersonagens() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Ouro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    RetratoPersonagem(
                        jogoId = jogo.jogoId,
                        seed = jogo.avatarSeed,
                        nome = jogo.personagem.nome,
                        temRetrato = jogo.retrato,
                        tamanho = 40,
                        versao = vm.retratoVersao
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            jogo.cenario?.let { "${it.emoji} ${it.nome}" } ?: jogo.personagem.nome,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (jogo.cenario != null) {
                                "Protagonista: ${jogo.personagem.nome} • Cap. ${jogo.turno}"
                            } else {
                                "📖 ${jogo.genero} • ${jogo.personagem.local} • Cap. ${jogo.turno}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSuave,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (vm.gerandoRetrato) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Ouro
                        )
                        Spacer(Modifier.width(6.dp))
                    } else {
                        IconButton(onClick = { vm.gerarRetrato() }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Gerar retrato",
                                tint = TextoSuave,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (!ehHistoriaPura) {
                        Surface(
                            color = SuperficieAlta,
                            shape = Cantos.pilula,
                            modifier = Modifier
                                .clickable { vm.definirModoLeitura(AppViewModel.ModoLeitura.RPG) }
                        ) {
                            Text(
                                "RPG",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ouro,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(onClick = {
                        tts?.stop()
                        tts?.speak(narrativaAtual, TextToSpeech.QUEUE_FLUSH, null, "narrativa")
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ouvir",
                            tint = Ouro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { dialogo = DialogoAberto.Denuncia }) {
                        Icon(
                            Icons.Filled.Flag,
                            contentDescription = "Denunciar",
                            tint = TextoSuave,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ---- Barra de recursos LivennIA: Livens, Memória, Diário, Cartas, Saves, Motor/Temp ----
            BarraFerramentasLiven(
                vm = vm,
                jogo = jogo,
                onAbrirDialogo = { dialogo = it }
            )

            // ---- Páginas da história e diálogos (Estilo Livro) ----
            LazyColumn(
                state = listaChat,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(chat.size) { indice ->
                    val msg = chat[indice]
                    val ehUltimo = indice == chat.size - 1
                    val textoMostrado = if (ehUltimo) msg.narrativa.take(visiveis) else msg.narrativa
                    val acaoTexto = msg.situacao ?: msg.acao
                    val ocultaAcao = indice == 0 || acaoTexto == "[CONTINUAR_HISTORIA]"

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!ocultaAcao) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                Surface(
                                    color = Ouro.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = 18.dp,
                                        bottomEnd = 4.dp
                                    ),
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.38f)),
                                    modifier = Modifier.fillMaxWidth(0.86f)
                                ) {
                                    Text(
                                        text = parsearNarrativaLivro(
                                            texto = acaoTexto,
                                            corFala = corFala,
                                            corAcao = corAcao,
                                            corNormal = corNormal
                                        ),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = (vm.tamanhoLetraNarrador - 0.5f).sp,
                                            lineHeight = ((vm.tamanhoLetraNarrador - 0.5f) * 1.5f).sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                                msg.emJogador?.let { h ->
                                    Text(
                                        text = horaDaMensagem(h),
                                        color = TextoSuave.copy(alpha = 0.65f),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = 3.dp, end = 4.dp)
                                    )
                                }
                            }
                        }

                        // Bloco literário da IA / Personagem
                        Surface(
                            color = SuperficieAlta.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(
                                topStart = 6.dp,
                                topEnd = 20.dp,
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp
                            ),
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.18f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = ehUltimo && !terminou) {
                                    visiveis = narrativaAtual.length
                                }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                                Text(
                                    text = parsearNarrativaLivro(
                                        texto = textoMostrado,
                                        corFala = corFala,
                                        corAcao = corAcao,
                                        corNormal = corNormal
                                    ),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = vm.tamanhoLetraNarrador.sp,
                                        lineHeight = (vm.tamanhoLetraNarrador * 1.65f).sp
                                    )
                                )
                                Spacer(Modifier.height(8.dp))
                                RodapeMensagemLiven(
                                    rotuloEsquerda = msg.emNarrador?.let { horaDaMensagem(it) } ?: "Cap. ${indice + 1}",
                                    versoes = msg.versoes,
                                    versaoAtiva = msg.versaoAtiva,
                                    ehUltimo = ehUltimo && !vm.ocupado,
                                    aoTrocarVersao = { nova -> vm.trocarVersaoMensagem(indice, nova) },
                                    aoRegenerar = { vm.regenerarUltimaResposta() },
                                    aoEditar = { indiceEdicao = indice },
                                    aoCopiar = { areaDeCopia.setText(AnnotatedString(msg.narrativa)) }
                                )
                            }
                        }
                    }
                }

                if (vm.ocupado) {
                    vm.acaoPendente?.let { pendente ->
                        if (pendente != "[CONTINUAR_HISTORIA]") {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Surface(
                                        color = Ouro.copy(alpha = 0.10f),
                                        shape = RoundedCornerShape(
                                            topStart = 18.dp,
                                            topEnd = 18.dp,
                                            bottomStart = 18.dp,
                                            bottomEnd = 4.dp
                                        ),
                                        modifier = Modifier.fillMaxWidth(0.86f)
                                    ) {
                                        Text(
                                            text = pendente,
                                            color = corNormal.copy(alpha = 0.65f),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = (vm.tamanhoLetraNarrador - 0.5f).sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { BlocoDigitando() }
                }
                item(key = "fim_chat_espaco") {
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ---- Barra inferior de conversa / coautoria (sem opções obrigatórias na tela) ----
            Surface(
                color = Superficie,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChipTag("⚡ ${jogo.jogadasRestantesHoje}", destaque = true)

                        Surface(
                            color = SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable(enabled = !vm.ocupado) {
                                acaoLivre = if (acaoLivre.endsWith("*")) "$acaoLivre " else "$acaoLivre*"
                            }
                        ) {
                            Text(
                                "*ação*",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ouro,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f)),
                            modifier = Modifier.clickable(enabled = !vm.ocupado && !vm.gerandoAutoResposta) {
                                vm.gerarSugestoesAutoResposta { primeira ->
                                    if (acaoLivre.isBlank()) acaoLivre = primeira
                                }
                            }
                        ) {
                            Text(
                                if (vm.gerandoAutoResposta) "⏳ Inspirando..." else "✨ Auto-resposta",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ouro,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable(enabled = !vm.ocupado && terminou) {
                                haptico.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.continuarHistoria()
                            }
                        ) {
                            Text(
                                "⏩ Continuar história",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ouro,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        if (jogo.cena.opcoes.isNotEmpty()) {
                            Surface(
                                color = if (mostrarSugestoes) Ouro.copy(alpha = 0.22f) else SuperficieAlta,
                                shape = Cantos.pilula,
                                border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable { mostrarSugestoes = !mostrarSugestoes }
                            ) {
                                Text(
                                    if (mostrarSugestoes) "💡 Ocultar ideias" else "💡 Ideias",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Ouro,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Text(
                            "✎",
                            color = TextoSuave,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clickable { dialogo = DialogoAberto.Feedback }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Gaveta de Auto-Respostas LivennIA
                    val sugestoesAtuais = remember(vm.sugestoesAutoResposta, jogo.cena.opcoes) {
                        if (vm.sugestoesAutoResposta.isNotEmpty()) {
                            vm.sugestoesAutoResposta
                        } else if (jogo.cena.opcoes.isNotEmpty()) {
                            jogo.cena.opcoes
                        } else {
                            listOf(
                                "Perguntar mais sobre isso",
                                "Observar a reação com atenção",
                                "Dar um passo à frente com cautela"
                            )
                        }
                    }
                    FaixaSugestoesAutoResposta(
                        sugestoes = sugestoesAtuais,
                        ocupado = vm.ocupado,
                        gerandoIA = vm.gerandoAutoResposta,
                        aoPedirSugestoesIA = { vm.gerarSugestoesAutoResposta() },
                        aoEscolher = { sugestao ->
                            acaoLivre = sugestao
                            haptico.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        }
                    )

                    AnimatedVisibility(visible = mostrarSugestoes && terminou && jogo.cena.opcoes.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            jogo.cena.opcoes.forEach { sugestao ->
                                Surface(
                                    color = SuperficieAlta,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.45f)),
                                    modifier = Modifier.clickable(enabled = !vm.ocupado) {
                                        acaoLivre = sugestao
                                        mostrarSugestoes = false
                                    }
                                ) {
                                    Text(
                                        text = sugestao,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = acaoLivre,
                            onValueChange = { acaoLivre = it.take(300) },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text("Escreva sua fala ou *ação*...")
                            },
                            maxLines = 4,
                            enabled = !vm.ocupado,
                            shape = RoundedCornerShape(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                val t = acaoLivre.trim()
                                if (t.isNotEmpty() && !vm.ocupado) {
                                    acaoLivre = ""
                                    mostrarSugestoes = false
                                    vm.jogarAcao(t)
                                }
                            },
                            enabled = !vm.ocupado && acaoLivre.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = Ouro
                            )
                        }
                    }
                }
            }
        }
    }

    GerenciadorDialogosJogo(
        vm = vm,
        jogo = jogo,
        dialogo = dialogo,
        indiceEdicao = indiceEdicao,
        onFecharDialogo = { dialogo = null },
        onFecharEdicao = { indiceEdicao = null }
    )

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun BarraFerramentasLiven(
    vm: AppViewModel,
    jogo: JogoResponse,
    onAbrirDialogo: (DialogoAberto) -> Unit
) {
    val saldoLivens = vm.conta?.livens ?: jogo.livens ?: 0
    val podeResgatarHoje = vm.conta?.resgateDiarioDisponivel == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selo de Vínculo & Afinidade
        val vinculo = calcularVinculo(jogo.turno)
        Surface(
            color = Ouro.copy(alpha = 0.18f),
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.50f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Vinculo) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(vinculo.icone, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Nvl ${vinculo.nivel} • ${vinculo.titulo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ouro,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Pílula de Livens (com resgate diário ao tocar se disponível)
        Surface(
            color = Ouro.copy(alpha = 0.16f),
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.45f)),
            modifier = Modifier.clickable(enabled = podeResgatarHoje) {
                vm.resgatarLivensDiarios()
            }
        ) {
            Text(
                text = if (podeResgatarHoje) "✨ $saldoLivens (+30)" else "✨ $saldoLivens",
                style = MaterialTheme.typography.labelSmall,
                color = Ouro,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Memória de Longo Prazo
        Surface(
            color = if (jogo.memoria.isNotBlank()) Ouro.copy(alpha = 0.16f) else SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Memoria) }
        ) {
            Text(
                text = if (jogo.memoria.isNotBlank()) "🧠 Memória •" else "🧠 Memória",
                style = MaterialTheme.typography.labelSmall,
                color = if (jogo.memoria.isNotBlank()) Ouro else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Diário Secreto do Personagem
        Surface(
            color = SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Diarios) }
        ) {
            Text(
                text = "📔 Diário (${jogo.diarios.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Cartas de Memória
        Surface(
            color = SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Cartas) }
        ) {
            Text(
                text = "💌 Cartas (${jogo.cartas.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Pontos de Salvamento (Savepoints)
        Surface(
            color = SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Savepoints) }
        ) {
            Text(
                text = "💾 Saves (${jogo.savepoints.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Seletor de Sabor de Modelo e Temperatura
        val emojiModelo = jogo.modelo?.emoji?.ifBlank { "🤖" } ?: "🤖"
        val nomeModelo = jogo.modelo?.nome ?: "Modelo de IA"
        Surface(
            color = SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.ModeloIA) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emojiModelo, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = nomeModelo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Ouro,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RodapeMensagemLiven(
    rotuloEsquerda: String,
    versoes: List<String>,
    versaoAtiva: Int,
    ehUltimo: Boolean,
    aoTrocarVersao: (Int) -> Unit,
    aoRegenerar: () -> Unit,
    aoEditar: () -> Unit,
    aoCopiar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (rotuloEsquerda.isNotBlank()) {
                Text(
                    text = rotuloEsquerda,
                    color = TextoSuave.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (versoes.size > 1) {
                Surface(
                    color = Ouro.copy(alpha = 0.14f),
                    shape = Cantos.pilula,
                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "‹",
                            color = if (versaoAtiva > 0) Ouro else TextoSuave.copy(alpha = 0.35f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(enabled = versaoAtiva > 0) { aoTrocarVersao(versaoAtiva - 1) }
                                .padding(horizontal = 4.dp)
                        )
                        Text(
                            text = "${versaoAtiva + 1}/${versoes.size}",
                            color = Ouro,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "›",
                            color = if (versaoAtiva < versoes.size - 1) Ouro else TextoSuave.copy(alpha = 0.35f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(enabled = versaoAtiva < versoes.size - 1) { aoTrocarVersao(versaoAtiva + 1) }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (ehUltimo) {
                Text(
                    text = "🔄 Regenerar",
                    color = Ouro,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clickable { aoRegenerar() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Text(
                text = "✎ Editar",
                color = TextoSuave.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clickable { aoEditar() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Text(
                text = "⧉ Copiar",
                color = TextoSuave.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clickable { aoCopiar() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FaixaSugestoesAutoResposta(
    sugestoes: List<String>,
    ocupado: Boolean,
    gerandoIA: Boolean,
    aoPedirSugestoesIA: () -> Unit,
    aoEscolher: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão para pedir sugestões com IA se quiser mais opções
        Surface(
            color = Ouro.copy(alpha = 0.20f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.50f)),
            modifier = Modifier.clickable(enabled = !ocupado && !gerandoIA) {
                aoPedirSugestoesIA()
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (gerandoIA) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = Ouro
                    )
                } else {
                    Text("✨", fontSize = 12.sp)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (gerandoIA) "Pensando..." else "Ideias IA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ouro,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        sugestoes.forEach { sugestao ->
            Surface(
                color = SuperficieAlta,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.clickable(enabled = !ocupado) {
                    aoEscolher(sugestao)
                }
            ) {
                Text(
                    text = sugestao,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.90f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ItemCardModeloIA(
    c: CerebroDto,
    selecionado: Boolean,
    aoSelecionar: () -> Unit
) {
    Surface(
        color = if (selecionado) Ouro.copy(alpha = 0.18f) else SuperficieAlta,
        shape = Cantos.cartao,
        border = BorderStroke(
            1.dp,
            if (selecionado) Ouro else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = c.liberado) { aoSelecionar() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(c.emoji.ifBlank { "🤖" }, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = c.nome,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
                        color = if (selecionado) Ouro else MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = c.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                color = when {
                    c.custoLivens == 0 -> Color(0xFF2E7D32).copy(alpha = 0.25f)
                    c.premium -> Color(0xFFFFB300).copy(alpha = 0.20f)
                    else -> Color(0xFF1976D2).copy(alpha = 0.25f)
                },
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = when {
                        c.custoLivens == 0 -> "Grátis"
                        c.premium -> "VIP"
                        else -> "⚡ ${c.custoLivens}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        c.custoLivens == 0 -> Color(0xFF81C784)
                        c.premium -> Ouro
                        else -> Color(0xFF90CAF9)
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun GerenciadorDialogosJogo(
    vm: AppViewModel,
    jogo: JogoResponse,
    dialogo: DialogoAberto?,
    indiceEdicao: Int?,
    onFecharDialogo: () -> Unit,
    onFecharEdicao: () -> Unit
) {
    if (indiceEdicao != null && indiceEdicao in vm.chat.indices) {
        val msgAlvo = vm.chat[indiceEdicao]
        var textoAcao by remember(indiceEdicao) { mutableStateOf(msgAlvo.situacao ?: msgAlvo.acao) }
        var textoNarrativa by remember(indiceEdicao) { mutableStateOf(msgAlvo.narrativa) }

        AlertDialog(
            onDismissRequest = onFecharEdicao,
            title = { Text("✎ Corrigir Fala (Fix Reply)") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (indiceEdicao > 0) {
                        OutlinedTextField(
                            value = textoAcao,
                            onValueChange = { textoAcao = it.take(500) },
                            label = { Text("Sua fala / ação") },
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = textoNarrativa,
                        onValueChange = { textoNarrativa = it.take(4000) },
                        label = { Text("Resposta do narrador / personagem") },
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.editarMensagem(
                            indice = indiceEdicao,
                            narrativa = textoNarrativa,
                            acao = if (indiceEdicao > 0) textoAcao else null
                        )
                        onFecharEdicao()
                    },
                    enabled = textoNarrativa.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = onFecharEdicao) { Text("Cancelar") }
            }
        )
    }

    when (dialogo) {
        DialogoAberto.Feedback -> DialogoFeedback(
            onEnviar = { nota, comentario ->
                onFecharDialogo()
                vm.enviarFeedback(nota, comentario)
            },
            onFechar = onFecharDialogo
        )
        DialogoAberto.Denuncia -> DialogoDenuncia(
            onEnviar = { motivo, detalhe ->
                onFecharDialogo()
                vm.denunciar(motivo, detalhe)
            },
            onFechar = onFecharDialogo
        )
        DialogoAberto.Memoria -> {
            var textoMemoria by remember(jogo.memoria) { mutableStateOf(jogo.memoria) }
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = { Text("🧠 Memória de Longo Prazo") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Estes fatos são sempre lembrados pela IA em todos os turnos desta história.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        OutlinedTextField(
                            value = textoMemoria,
                            onValueChange = { textoMemoria = it.take(1500) },
                            placeholder = { Text("Ex.: Somos aliados de infância; prometemos encontrar a relíquia...") },
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = { vm.resumirMemoriaComIA() },
                            enabled = !vm.ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (vm.ocupado) "⏳ Resumindo..." else "✨ Resumir história até aqui com IA")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.salvarMemoria(textoMemoria)
                            onFecharDialogo()
                        }
                    ) { Text("Salvar memória") }
                },
                dismissButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.Diarios -> {
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = { Text("📔 Diário Secreto") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Pensamentos íntimos do personagem sobre os acontecimentos recentes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        Button(
                            onClick = { vm.gerarPaginaDiario() },
                            enabled = !vm.ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (vm.ocupado) "⏳ Escrevendo no diário..." else "✨ Revelar nova página do diário")
                        }
                        if (jogo.diarios.isEmpty()) {
                            Text(
                                "Nenhuma página revelada ainda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSuave
                            )
                        } else {
                            jogo.diarios.asReversed().forEach { pagina ->
                                val dataFormatada = pagina.criadoEm?.let { horaDaMensagem(it) } ?: "Cap. ${pagina.turno}"
                                Surface(
                                    color = SuperficieAlta,
                                    shape = Cantos.cartao,
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${pagina.titulo} • Cap. ${pagina.turno}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Ouro
                                        )
                                        Text(
                                            text = "Humor: ${pagina.humor} • $dataFormatada",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextoSuave
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = pagina.conteudo,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.Cartas -> {
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = { Text("💌 Cartas de Memória") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Cartas especiais escritas pelo personagem para você guardar de recordação.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        Button(
                            onClick = { vm.gerarCartaMemoria() },
                            enabled = !vm.ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (vm.ocupado) "⏳ Escrevendo carta..." else "💌 Pedir carta de memória")
                        }
                        if (jogo.cartas.isEmpty()) {
                            Text(
                                "Nenhuma carta recebida ainda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSuave
                            )
                        } else {
                            jogo.cartas.asReversed().forEach { carta ->
                                val dataCarta = carta.criadaEm?.let { horaDaMensagem(it) } ?: "Cap. ${carta.turno}"
                                Surface(
                                    color = SuperficieAlta,
                                    shape = Cantos.cartao,
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = carta.titulo,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Ouro
                                        )
                                        Text(
                                            text = "De: ${carta.remetente} • $dataCarta",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextoSuave
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = carta.conteudo,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.Savepoints -> {
            var tituloSave by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = { Text("💾 Pontos de Salvamento") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Salve o momento atual para explorar outros rumos e voltar quando quiser.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = tituloSave,
                                onValueChange = { tituloSave = it.take(60) },
                                placeholder = { Text("Nome do save (opcional)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    vm.criarSavepoint(tituloSave)
                                    tituloSave = ""
                                }
                            ) {
                                Text("Salvar")
                            }
                        }
                        if (jogo.savepoints.isEmpty()) {
                            Text(
                                "Nenhum savepoint criado nesta aventura.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSuave
                            )
                        } else {
                            jogo.savepoints.asReversed().forEach { sp ->
                                val dataSave = sp.criadoEm?.let { horaDaMensagem(it) } ?: sp.local
                                Surface(
                                    color = SuperficieAlta,
                                    shape = Cantos.cartao,
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "${sp.nome} (Turno ${sp.turno})",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Ouro
                                        )
                                        Text(
                                            text = dataSave,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextoSuave
                                        )
                                        if (sp.resumo.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = sp.resumo,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    vm.carregarSavepoint(sp.id)
                                                    onFecharDialogo()
                                                }
                                            ) {
                                                Text("Restaurar")
                                            }
                                            TextButton(
                                                onClick = { vm.apagarSavepoint(sp.id) }
                                            ) {
                                                Text("Apagar", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.Vinculo -> {
            val vinculo = calcularVinculo(jogo.turno)
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(vinculo.icone, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Vínculo & Afinidade")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = Ouro.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "NÍVEL ${vinculo.nivel} • ${vinculo.titulo.uppercase()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Ouro
                                )
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { vinculo.progresso },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = Ouro,
                                    trackColor = SuperficieAlta
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${(vinculo.progresso * 100).toInt()}% até a próxima evolução",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextoSuave
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Como ${jogo.personagem.nome} te enxerga:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = vinculo.descricao,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSuave
                            )
                        }

                        Surface(
                            color = SuperficieAlta,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💡", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Dica: Fazer perguntas sobre o passado do personagem ou demonstrar empatia acelera o vínculo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSuave
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.ModeloIA -> {
            var cerebroIdSelecionado by remember(jogo.modelo?.id) {
                mutableStateOf(jogo.modelo?.id ?: vm.cerebros.firstOrNull()?.id ?: "")
            }
            var tempSelecionada by remember(jogo.temperatura) {
                mutableFloatStateOf(jogo.temperatura)
            }
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Modelo de IA")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Escolha o modelo que dita o tom, profundidade e criatividade da narrativa:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )

                        val gratis = vm.cerebros.filter { it.custoLivens == 0 && !it.premium }
                        val energia = vm.cerebros.filter { it.custoLivens > 0 && !it.premium }
                        val premium = vm.cerebros.filter { it.premium }

                        if (gratis.isNotEmpty()) {
                            Text(
                                text = "🎁 GRÁTIS COM ANÚNCIO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Ouro
                            )
                            gratis.forEach { c ->
                                ItemCardModeloIA(
                                    c = c,
                                    selecionado = c.id == cerebroIdSelecionado,
                                    aoSelecionar = {
                                        cerebroIdSelecionado = c.id
                                        tempSelecionada = c.temperaturaPadrao
                                    }
                                )
                            }
                        }

                        if (energia.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "⚡ USA ENERGIA (LIVENS)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64B5F6)
                            )
                            energia.forEach { c ->
                                ItemCardModeloIA(
                                    c = c,
                                    selecionado = c.id == cerebroIdSelecionado,
                                    aoSelecionar = {
                                        cerebroIdSelecionado = c.id
                                        tempSelecionada = c.temperaturaPadrao
                                    }
                                )
                            }
                        }

                        if (premium.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "👑 EXCLUSIVO PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                            premium.forEach { c ->
                                ItemCardModeloIA(
                                    c = c,
                                    selecionado = c.id == cerebroIdSelecionado,
                                    aoSelecionar = {
                                        cerebroIdSelecionado = c.id
                                        tempSelecionada = c.temperaturaPadrao
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.configurarModeloDoJogo(
                                modeloId = cerebroIdSelecionado.ifBlank { null },
                                temperatura = tempSelecionada
                            )
                            onFecharDialogo()
                        }
                    ) { Text("Confirmar Modelo") }
                },
                dismissButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Cancelar") }
                }
            )
        }
        null -> Unit
    }
}

