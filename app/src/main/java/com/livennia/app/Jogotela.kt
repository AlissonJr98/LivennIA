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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

private enum class DialogoAberto { Feedback, Denuncia }

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
        while (visiveis < narrativaAtual.length) {
            delay(16)
            visiveis = minOf(narrativaAtual.length, visiveis + 2)
            if (ultimo >= 0) listaChat.scrollToItem(ultimo)
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

    LaunchedEffect(chat.size, vm.ocupado, vm.acaoPendente) {
        // Itens extras no fim da lista: a fala já escolhida (enquanto a IA escreve a
        // "situacao" dela) e o indicador de "digitando".
        val extras = if (vm.ocupado) (if (vm.acaoPendente != null) 2 else 1) else 0
        val alvo = chat.size + extras - 1
        if (alvo >= 0) listaChat.animateScrollToItem(alvo)
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
                // Cada rodada da conversa (a sua fala + a resposta do narrador) é um
                // bloco: pouco espaço por dentro, bastante espaço entre um bloco e outro.
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
                            aoCopiar = { areaDeCopia.setText(AnnotatedString(narrativaAtual)) },
                            tamanhoLetra = vm.tamanhoLetraNarrador,
                            hora = msg.emNarrador
                        )
                    }
                }

                if (vm.ocupado) {
                    // A escolha aparece no chat na hora do toque, enquanto a IA escreve.
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
                        // Barra de status rolável: "raios" (jogadas de hoje), vida e o
                        // cérebro de IA que está narrando esta aventura.
                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // "Raios" = a energia do dia: cada jogada gasta um.
                            ChipTag("⚡ ${jogo.jogadasRestantesHoje}", destaque = true)
                            ChipTag(
                                "❤ ${jogo.personagem.vida}/${jogo.personagem.vidaMax}",
                                destaque = jogo.personagem.vida <= 30
                            )
                            ChipTag("📖 ${jogo.genero}")
                            jogo.modelo?.let { modelo ->
                                ChipTag("${modelo.emoji.ifBlank { "🧠" }} ${modelo.nome}")
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

    when (dialogo) {
        DialogoAberto.Feedback -> DialogoFeedback(
            onEnviar = { nota, comentario ->
                dialogo = null
                vm.enviarFeedback(nota, comentario)
            },
            onFechar = { dialogo = null }
        )
        DialogoAberto.Denuncia -> DialogoDenuncia(
            onEnviar = { motivo, detalhe ->
                dialogo = null
                vm.denunciar(motivo, detalhe)
            },
            onFechar = { dialogo = null }
        )
        null -> Unit
    }

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

// ---- Fala do narrador: texto corrido com ícones à esquerda ----
@Composable
private fun BlocoNarrador(
    texto: String,
    eventos: List<EventoDto>,
    clicavel: Boolean,
    aoTocar: () -> Unit,
    aoCopiar: () -> Unit,
    tamanhoLetra: Float,
    hora: Long? = null
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
                if (hora != null) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = horaDaMensagem(hora),
                        color = TextoSuave.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
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
        while (visiveis < narrativaAtual.length) {
            delay(14)
            visiveis = minOf(narrativaAtual.length, visiveis + 2)
            if (ultimo >= 0) listaChat.scrollToItem(ultimo)
        }
    }
    val terminou = visiveis >= narrativaAtual.length

    var acaoLivre by remember(jogo.turno) { mutableStateOf("") }
    var mostrarSugestoes by remember(jogo.turno) { mutableStateOf(false) }
    var dialogo by remember { mutableStateOf<DialogoAberto?>(null) }

    LaunchedEffect(chat.size, vm.ocupado, vm.acaoPendente) {
        val extras = if (vm.ocupado) (if (vm.acaoPendente != null) 2 else 1) else 0
        val alvo = chat.size + extras - 1
        if (alvo >= 0) listaChat.animateScrollToItem(alvo)
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
            // ---- Cabeçalho estilo Livro / Emochi ----
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
                    // Se for uma partida de RPG sendo lida em modo Livro, permite voltar para a visão RPG.
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

            // ---- Páginas da história e diálogos (Estilo Emochi + Livro) ----
            LazyColumn(
                state = listaChat,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                items(chat.size) { indice ->
                    val msg = chat[indice]
                    val ehUltimo = indice == chat.size - 1
                    val textoMostrado = if (ehUltimo) msg.narrativa.take(visiveis) else msg.narrativa
                    val acaoTexto = msg.situacao ?: msg.acao
                    val ocultaAcao = indice == 0 || acaoTexto == "[CONTINUAR_HISTORIA]"

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!ocultaAcao) {
                            // Mensagem / ação do jogador alinhada à direita, com formatação Emochi
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
                                        text = parsearNarrativaEmochi(
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

                        // Bloco literário da IA / Personagem (Estilo Emochi)
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
                                    text = parsearNarrativaEmochi(
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg.emNarrador?.let { horaDaMensagem(it) } ?: "Cap. ${indice + 1}",
                                        color = TextoSuave.copy(alpha = 0.65f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "⧉ Copiar",
                                        color = TextoSuave.copy(alpha = 0.75f),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier
                                            .clickable { areaDeCopia.setText(AnnotatedString(msg.narrativa)) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
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
                    // Linha de utilitários: Energia, Sugestões opcionais, Continuar história e Dica de *ação*
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChipTag("⚡ ${jogo.jogadasRestantesHoje}", destaque = true)

                        // Botão para inserir asteriscos rapidamente (*ação*) como no Emochi
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

                        // Botão para pedir que a IA continue escrevendo a história sozinha
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

                        // Botão opcional para mostrar/ocultar ideias de resposta da IA
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

                    // Gaveta opcional de ideias (só abre se o jogador tocar em "💡 Ideias")
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

                    // Campo de conversa livre (estilo Emochi)
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

    when (dialogo) {
        DialogoAberto.Feedback -> DialogoFeedback(
            onEnviar = { nota, comentario ->
                dialogo = null
                vm.enviarFeedback(nota, comentario)
            },
            onFechar = { dialogo = null }
        )
        DialogoAberto.Denuncia -> DialogoDenuncia(
            onEnviar = { motivo, detalhe ->
                dialogo = null
                vm.denunciar(motivo, detalhe)
            },
            onFechar = { dialogo = null }
        )
        null -> Unit
    }

    SnackbarHost(hostState = snackbarHostState)
}
