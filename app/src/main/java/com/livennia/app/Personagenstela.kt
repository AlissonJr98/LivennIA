package com.livennia.app

import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.AlertDialog

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem



import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.background

import androidx.compose.foundation.border

import androidx.compose.foundation.clickable

import androidx.compose.foundation.horizontalScroll

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.foundation.layout.FlowRow

import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.RowScope

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.WindowInsets

import androidx.compose.foundation.layout.WindowInsetsSides

import androidx.compose.foundation.layout.aspectRatio

import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.navigationBarsPadding

import androidx.compose.foundation.layout.offset

import androidx.compose.foundation.layout.only

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.safeDrawing

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width

import androidx.compose.foundation.layout.widthIn

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.grid.GridCells

import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

import androidx.compose.foundation.lazy.grid.items

import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Add

import androidx.compose.material.icons.filled.Close

import androidx.compose.material.icons.filled.Delete

import androidx.compose.material.icons.filled.Home

import androidx.compose.material.icons.filled.Person

import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material.icons.filled.Search

import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.Button

import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton

import androidx.compose.material3.MaterialTheme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Scaffold

import androidx.compose.material3.SnackbarHost

import androidx.compose.material3.SnackbarHostState

import androidx.compose.material3.Surface

import androidx.compose.material3.Text

import androidx.compose.material3.TextButton

import androidx.compose.material3.pulltorefresh.PullToRefreshBox

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.text.font.FontStyle

import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.compose.ui.window.Dialog

import androidx.compose.ui.window.DialogProperties

import kotlin.math.absoluteValue



// Tela principal: abas na parte de baixo e um botão "+" central para começar

// uma aventura ou história nova.

@Composable

fun PersonagensTela(vm: AppViewModel) {

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.erro, vm.aviso) {

        vm.erro?.let { snackbarHostState.showSnackbar(it); vm.limparErro() }

        vm.aviso?.let { snackbarHostState.showSnackbar(it); vm.limparAviso() }

    }



    val contexto = LocalContext.current

    LaunchedEffect(vm.personasCarregadas, vm.personas.size, vm.temSessao) {
        if (vm.temSessao && vm.personasCarregadas && vm.personas.isEmpty()) {
            vm.mostrarModalPersonaObrigatorio = true
        }
    }

    var modalModosCriacao by remember { mutableStateOf(false) }

    var modalSpark by remember { mutableStateOf(false) }

    var modalRapido by remember { mutableStateOf(false) }

    var modalManual by remember { mutableStateOf(false) }

    var cenarioPreviaDetalhes by remember { mutableStateOf<CenarioDto?>(null) }

    var cenarioContinuarOuNovoGlobal by remember { mutableStateOf<Pair<CenarioDto, JogoResumo>?>(null) }



    Scaffold(

        snackbarHost = { SnackbarHost(snackbarHostState) },

        containerColor = Color.Transparent,

        contentWindowInsets = WindowInsets.safeDrawing.only(

            WindowInsetsSides.Top + WindowInsetsSides.Horizontal

        ),

        bottomBar = {

            BarraInferior(

                aba = vm.abaPrincipal,

                onSelecionar = { vm.selecionarAba(it) },

                onNova = {
                    if (vm.personas.isEmpty()) {
                        Toast.makeText(contexto, "Você precisa criar sua Persona primeiro para que o Bot saiba quem você é!", Toast.LENGTH_LONG).show()
                        vm.mostrarModalPersonaObrigatorio = true
                    } else {
                        modalModosCriacao = true
                    }
                }

            )

        }

    ) { padding ->

        Box(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

                .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo))),

            contentAlignment = Alignment.TopCenter

        ) {

            Box(modifier = Modifier.fillMaxSize().widthIn(max = 720.dp)) {

                when (vm.abaPrincipal) {

                    1 -> AbaHistorias(vm)

                    2 -> AbaExplorar(
                        vm,
                        onAbrirDetalhes = { cenario ->
                            val jogoExistente = vm.jogos.firstOrNull {
                                (!it.encerrado) && (
                                    (it.cenarioId != null && it.cenarioId == cenario.id) ||
                                    (it.cenarioNome != null && it.cenarioNome.equals(cenario.nome, ignoreCase = true)) ||
                                    it.nome.equals(cenario.nome, ignoreCase = true)
                                )
                            }
                            if (jogoExistente != null) {
                                cenarioContinuarOuNovoGlobal = Pair(cenario, jogoExistente)
                            } else {
                                cenarioPreviaDetalhes = cenario
                            }
                        },
                        onAbrirInfo = { cenario ->
                            cenarioPreviaDetalhes = cenario
                        },
                        onAbrirCriacao = {
                            if (vm.personas.isEmpty()) {
                                Toast.makeText(contexto, "Você precisa criar sua Persona primeiro para que o Bot saiba quem você é!", Toast.LENGTH_LONG).show()
                                vm.mostrarModalPersonaObrigatorio = true
                            } else {
                                modalModosCriacao = true
                            }
                        }
                    )

                    3 -> AbaPerfil(vm)

                    else -> AbaAventuras(vm)

                }

            }

        }

    }



    if (modalModosCriacao) {

        ModalModosCriacao(

            onDismiss = { modalModosCriacao = false },

            onEscolherSpark = { modalModosCriacao = false; modalSpark = true },

            onEscolherRapido = { modalModosCriacao = false; modalRapido = true },

            onEscolherManual = { modalModosCriacao = false; modalManual = true }

        )

    }



    if (modalSpark) {

        WizardModoSpark(

            vm = vm,

            onFechar = { modalSpark = false },

            onConcluido = { novoCenario ->

                modalSpark = false

                cenarioPreviaDetalhes = novoCenario

            }

        )

    }



    if (modalRapido) {

        ModalModoRapido(

            vm = vm,

            onFechar = { modalRapido = false },

            onConcluido = { novoCenario ->

                modalRapido = false

                cenarioPreviaDetalhes = novoCenario

            }

        )

    }



    if (modalManual) {

        ModalModoManual(

            vm = vm,

            onFechar = { modalManual = false },

            onConcluido = { novoCenario ->

                modalManual = false

                cenarioPreviaDetalhes = novoCenario

            }

        )

    }



    cenarioPreviaDetalhes?.let { cenario ->
        DetalhesPersonagemModal(
            cenario = cenario,
            versaoCapa = vm.capaVersao,
            apelidoUsuario = vm.apelido,
            vm = vm,
            onFechar = { cenarioPreviaDetalhes = null },
            onIniciarChat = {
                if (vm.personas.isEmpty()) {
                    Toast.makeText(contexto, "Você precisa criar sua Persona primeiro para que o Bot saiba quem você é!", Toast.LENGTH_LONG).show()
                    vm.mostrarModalPersonaObrigatorio = true
                } else {
                    val jogoExistente = vm.jogos.firstOrNull {
                        (!it.encerrado) && (
                            (it.cenarioId != null && it.cenarioId == cenario.id) ||
                            (it.cenarioNome != null && it.cenarioNome.equals(cenario.nome, ignoreCase = true)) ||
                            it.nome.equals(cenario.nome, ignoreCase = true)
                        )
                    }
                    if (jogoExistente != null) {
                        cenarioPreviaDetalhes = null
                        cenarioContinuarOuNovoGlobal = Pair(cenario, jogoExistente)
                    } else {
                        cenarioPreviaDetalhes = null
                        val modo = cenario.modoSeguro
                        vm.iniciarHistoriaDireta(cenario, modo)
                    }
                }
            }
        )
    }

    cenarioContinuarOuNovoGlobal?.let { (cenario, jogo) ->
        AlertDialog(
            onDismissRequest = { cenarioContinuarOuNovoGlobal = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Jornada em Andamento", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Você já tem uma jornada iniciada em '${cenario.nome}' (Turno ${jogo.turno}).",
                        color = Color(0xFFCCCED9),
                        fontSize = 14.sp
                    )
                    Text(
                        "Deseja continuar de onde parou ou começar uma nova história do início?",
                        color = TextoSuave,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val jId = jogo.jogoId
                        cenarioContinuarOuNovoGlobal = null
                        cenarioPreviaDetalhes = null
                        vm.abrirJogo(jId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Ouro)
                ) {
                    Text("▶️ Continuar Jornada", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            cenarioContinuarOuNovoGlobal = null
                            cenarioPreviaDetalhes = null
                            val modo = cenario.modoSeguro
                            vm.iniciarHistoriaDireta(cenario, modo)
                        }
                    ) {
                        Text("✨ Nova", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = {
                            val c = cenario
                            cenarioContinuarOuNovoGlobal = null
                            cenarioPreviaDetalhes = c
                        }
                    ) {
                        Text("ℹ️ Detalhes", color = Color(0xFF64B5F6), fontWeight = FontWeight.Medium)
                    }
                    TextButton(onClick = { cenarioContinuarOuNovoGlobal = null }) {
                        Text("Cancelar", color = Color(0xFF8E8E93))
                    }
                }
            },
            containerColor = Color(0xFF16161F),
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (vm.mostrarModalPersonaObrigatorio) {
        ModalMinhaPersona(
            vm = vm,
            obrigatorio = vm.personas.isEmpty(),
            onFechar = {
                vm.mostrarModalPersonaObrigatorio = false
            }
        )
    }

}



// ---------------------------------------------------------------

// Aba "RPG": a lista de personagens do Modo RPG, com barra de vida e turnos.

// ---------------------------------------------------------------

@Composable

private fun AbaAventuras(vm: AppViewModel) {

    var jogoParaApagar by rememberSaveable { mutableStateOf<JogoResumo?>(null) }

    val jogosRpg = vm.jogos.filter { it.modo != "historia" }

    val ativos = vm.jogos.count { !it.encerrado }

    val limiteAtivos = vm.conta?.maxJogos ?: 3



    PullToRefreshBox(

        isRefreshing = vm.ocupado,

        onRefresh = { vm.carregarPersonagens() },

        modifier = Modifier.fillMaxSize()

    ) {

        LazyColumn(

            modifier = Modifier.fillMaxSize(),

            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),

            verticalArrangement = Arrangement.spacedBy(8.dp)

        ) {

            item {

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text("Modo RPG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        Text(

                            "Aventuras táticas com vida, inventário e escolhas • Olá, ${vm.apelido}",

                            color = TextoSuave,

                            style = MaterialTheme.typography.labelMedium

                        )

                    }

                    TextButton(onClick = { vm.carregarPersonagens() }, enabled = !vm.ocupado) {

                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", modifier = Modifier.size(18.dp))

                    }

                }

            }



            vm.conta?.let { c ->

                item {

                    Row(

                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),

                        horizontalArrangement = Arrangement.spacedBy(8.dp)

                    ) {

                        ChipTag("✨ Livens: ${c.livens}", destaque = true)

                        ChipTag("⚡ Jogadas: ${c.jogadasRestantesHoje}", destaque = true)

                        ChipTag("🎲 RPGs: ${jogosRpg.size}")

                        ChipTag("Ativos: $ativos/$limiteAtivos")

                    }

                }

            }



            if (vm.ocupado && jogosRpg.isEmpty()) {

                items(4) { ShimmerBox(modifier = Modifier.fillMaxWidth().height(72.dp)) }

            }



            if (jogosRpg.isEmpty() && !vm.ocupado) {

                item {

                    Column(

                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Text("🎲", style = MaterialTheme.typography.displayMedium)

                        Spacer(Modifier.height(12.dp))

                        Text("Nenhuma campanha de RPG ainda", style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(4.dp))

                        Text(

                            "Toque no + para criar seu personagem de RPG com vida e inventário.",

                            color = TextoSuave,

                            style = MaterialTheme.typography.bodyMedium

                        )

                    }

                }

            }



            items(jogosRpg, key = { it.jogoId }) { jogo ->

                LinhaAventura(

                    jogo = jogo,

                    habilitado = !vm.ocupado,

                    onAbrir = { vm.abrirJogo(jogo.jogoId) },

                    onApagar = { jogoParaApagar = jogo }

                )

            }

        }

    }



    jogoParaApagar?.let { jogo ->

        DialogoConfirmar(

            titulo = "Apagar ${jogo.nome}?",

            texto = "Esta aventura será apagada para sempre.",

            rotuloConfirmar = "Apagar",

            onConfirmar = {

                jogoParaApagar = null

                vm.apagarJogo(jogo.jogoId)

            },

            onFechar = { jogoParaApagar = null }

        )

    }

}



// ---------------------------------------------------------------

// Aba "Histórias": modo livro / conversa livre LivennIA (sem HP e sem morte).

// ---------------------------------------------------------------

@Composable

private fun AbaHistorias(vm: AppViewModel) {

    var jogoParaApagar by rememberSaveable { mutableStateOf<JogoResumo?>(null) }

    val historias = vm.jogos.filter { it.modo == "historia" }

    val ativos = vm.jogos.count { !it.encerrado }

    val limiteAtivos = vm.conta?.maxJogos ?: 3



    PullToRefreshBox(

        isRefreshing = vm.ocupado,

        onRefresh = { vm.carregarPersonagens() },

        modifier = Modifier.fillMaxSize()

    ) {

        LazyColumn(

            modifier = Modifier.fillMaxSize(),

            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),

            verticalArrangement = Arrangement.spacedBy(8.dp)

        ) {

            item {

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text("Modo História", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        Text(

                            "Livros interativos e roleplay livre, sem barra de vida.",

                            color = TextoSuave,

                            style = MaterialTheme.typography.labelMedium

                        )

                    }

                    TextButton(onClick = { vm.carregarPersonagens() }, enabled = !vm.ocupado) {

                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", modifier = Modifier.size(18.dp))

                    }

                }

            }



            vm.conta?.let { c ->

                item {

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.spacedBy(8.dp)

                    ) {

                        ChipTag("⚡ Energia hoje: ${c.jogadasRestantesHoje}", destaque = true)

                        ChipTag("📖 Livros: ${historias.size}")

                        ChipTag("Total ativos: $ativos/$limiteAtivos")

                    }

                }

            }



            if (vm.ocupado && historias.isEmpty()) {

                items(3) { ShimmerBox(modifier = Modifier.fillMaxWidth().height(84.dp)) }

            }



            if (historias.isEmpty() && !vm.ocupado) {

                item {

                    Column(

                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Text("📖", style = MaterialTheme.typography.displayMedium)

                        Spacer(Modifier.height(12.dp))

                        Text("Nenhuma história começada", style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(4.dp))

                        Text(

                            "Toque no + para começar um livro interativo ou escolha um personagem em Explorar.",

                            color = TextoSuave,

                            style = MaterialTheme.typography.bodyMedium

                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedButton(onClick = { vm.comecarAventuraLivre("historia") }) {

                            Text("✨ Começar nova história")

                        }

                    }

                }

            }



            items(historias, key = { it.jogoId }) { jogo ->

                LinhaHistoria(

                    jogo = jogo,

                    habilitado = !vm.ocupado,

                    onAbrir = { vm.abrirJogo(jogo.jogoId) },

                    onApagar = { jogoParaApagar = jogo }

                )

            }

        }

    }



    jogoParaApagar?.let { jogo ->

        DialogoConfirmar(

            titulo = "Apagar história de ${jogo.nome}?",

            texto = "Este livro interativo será apagado para sempre.",

            rotuloConfirmar = "Apagar",

            onConfirmar = {

                jogoParaApagar = null

                vm.apagarJogo(jogo.jogoId)

            },

            onFechar = { jogoParaApagar = null }

        )

    }

}



// Card de uma história no Modo História (Livro / Conversa viva).

@Composable

private fun LinhaHistoria(

    jogo: JogoResumo,

    habilitado: Boolean,

    onAbrir: () -> Unit,

    onApagar: () -> Unit

) {

    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "historiaCardScale"
    )

    Surface(
        color = Color(0xFF131522),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isPressed) 3.dp else 12.dp,
        border = BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(
                    Color(0xFF7C4DFF).copy(alpha = 0.75f),
                    Color(0xFF00E5FF).copy(alpha = 0.45f),
                    Color(0xFF1D2136).copy(alpha = 0.5f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable(
                enabled = habilitado,
                interactionSource = pressInteraction,
                indication = null
            ) { onAbrir() }
    ) {

        Row(

            modifier = Modifier.padding(14.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            RetratoPersonagem(

                jogoId = jogo.jogoId,

                seed = jogo.avatarSeed,

                nome = jogo.nome,

                temRetrato = jogo.retrato,

                tamanho = 62

            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(

                        jogo.cenarioNome ?: jogo.nome,

                        style = MaterialTheme.typography.titleMedium,

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis,

                        modifier = Modifier.weight(1f, fill = false)

                    )

                    Spacer(Modifier.width(8.dp))

                    ChipTag("📖 Cap. ${jogo.turno}", destaque = true)

                }

                Text(

                    if (jogo.cenarioNome != null) "Protagonista: ${jogo.nome} • ${jogo.local}"

                    else "${jogo.genero} • ${jogo.local}",

                    color = Ouro.copy(alpha = 0.85f),

                    style = MaterialTheme.typography.labelSmall,

                    maxLines = 1,

                    overflow = TextOverflow.Ellipsis

                )

                Spacer(Modifier.height(6.dp))

                Text(

                    text = jogo.ultimaNarrativa?.replace("*", "") ?: "Toque para continuar a leitura e conversa...",

                    color = TextoSuave,

                    style = MaterialTheme.typography.bodySmall,

                    maxLines = 2,

                    overflow = TextOverflow.Ellipsis

                )

            }

            Spacer(Modifier.width(8.dp))

            Icon(

                Icons.Filled.Delete,

                contentDescription = "Apagar",

                tint = TextoSuave,

                modifier = Modifier

                    .size(20.dp)

                    .clickable(enabled = habilitado) { onApagar() }

            )

        }

    }

}



// Uma linha da lista de RPG: retrato, nome, situação, vida e gênero.

@Composable

private fun LinhaAventura(

    jogo: JogoResumo,

    habilitado: Boolean,

    onAbrir: () -> Unit,

    onApagar: () -> Unit

) {

    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "aventuraCardScale"
    )

    Surface(
        color = Color(0xFF131522),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isPressed) 3.dp else 12.dp,
        border = BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.75f),
                    Color(0xFF7C4DFF).copy(alpha = 0.45f),
                    Color(0xFF1D2136).copy(alpha = 0.5f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable(
                enabled = habilitado,
                interactionSource = pressInteraction,
                indication = null
            ) { onAbrir() }
    ) {

        Row(

            modifier = Modifier.padding(12.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            RetratoPersonagem(

                jogoId = jogo.jogoId,

                seed = jogo.avatarSeed,

                nome = jogo.nome,

                temRetrato = jogo.retrato,

                tamanho = 56

            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(

                        jogo.nome,

                        style = MaterialTheme.typography.titleMedium,

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis,

                        modifier = Modifier.weight(1f, fill = false)

                    )

                    if (jogo.encerrado) {

                        Spacer(Modifier.width(6.dp))

                        Text("💀", style = MaterialTheme.typography.labelMedium)

                    }

                }

                Text(

                    "${jogo.local} • turno ${jogo.turno}",

                    color = TextoSuave,

                    style = MaterialTheme.typography.labelMedium,

                    maxLines = 1,

                    overflow = TextOverflow.Ellipsis

                )

                Spacer(Modifier.height(6.dp))

                BarraDeVida(vida = jogo.vida, vidaMax = jogo.vidaMax)

            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Numa história da vitrine, o "gênero" de destaque é o cenário oficial.

                ChipTag(jogo.cenarioNome ?: jogo.genero)

                Spacer(Modifier.height(6.dp))

                Icon(

                    Icons.Filled.Delete,

                    contentDescription = "Apagar",

                    tint = TextoSuave,

                    modifier = Modifier

                        .size(20.dp)

                        .clickable(enabled = habilitado) { onApagar() }

                )

            }

        }

    }

}



// ---------------------------------------------------------------

// Aba "Explorar": vitrine de histórias e personagens, separando Oficiais de IA e Criações de Usuários, com Rankings e Modos de Criação.

// ---------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)

@Composable

private fun AbaExplorar(

    vm: AppViewModel,

    onAbrirDetalhes: (CenarioDto) -> Unit = {},

    onAbrirInfo: (CenarioDto) -> Unit = onAbrirDetalhes,

    onAbrirCriacao: () -> Unit = {}

) {

    LaunchedEffect(Unit) { vm.carregarCatalogo() }



    var subAba by rememberSaveable { mutableStateOf("Explorar") } // "Explorar" ou "Rankings"

    var filtroOrigem by rememberSaveable { mutableStateOf("Todos") } // "Todos", "IA", "Usuarios"

    var filtroModoJogo by rememberSaveable { mutableStateOf("Todos") } // "Todos", "Historia", "RPG"

    var filtroPublico by rememberSaveable { mutableStateOf("Todos") } // "Todos", "Feminino", "Masculino"

    var filtroGeneroRanking by rememberSaveable { mutableStateOf("Todos") } // "Todos", "Feminino", "Masculino"

    var filtroPeriodoRanking by rememberSaveable { mutableStateOf("Semanal") } // "Semanal", "Geral"

    var busca by rememberSaveable { mutableStateOf("") }

    var estiloFiltro by rememberSaveable { mutableStateOf("Todos") }

    var menuGeneroAberto by remember { mutableStateOf(false) }

    var menuPeriodoAberto by remember { mutableStateOf(false) }

    var menuModoAberto by remember { mutableStateOf(false) }

    var menuPublicoAberto by remember { mutableStateOf(false) }

    var menuEstiloAberto by remember { mutableStateOf(false) }



    // Combina o catálogo oficial de IA com os personagens criados pelos usuários na comunidade

    val todosOsCenarios = remember(vm.catalogo, RepositorioPersonagensComunidade.criacoes.size) {

        val lista = mutableListOf<CenarioDto>()

        lista.addAll(RepositorioPersonagensComunidade.criacoes)

        lista.addAll(vm.catalogo)

        lista

    }



    val estilosDisponiveis = remember(todosOsCenarios, vm.generos) {

        val nomes = linkedSetOf("Todos")

        vm.generos.forEach { nomes.add(it.nome) }

        todosOsCenarios.forEach { if (it.generoSeguro.isNotBlank()) nomes.add(it.generoSeguro) }

        nomes.toList()

    }



    // Filtragem para o feed Explorar

    val catalogoFiltrado = remember(todosOsCenarios, busca, estiloFiltro, filtroOrigem, filtroModoJogo, filtroPublico) {

        val termo = busca.trim().lowercase()

        todosOsCenarios.filter { c ->

            val bateOrigem = when (filtroOrigem) {

                "IA" -> !c.criadoPorUsuario

                "Usuarios" -> c.criadoPorUsuario

                else -> true

            }

            val bateModo = when (filtroModoJogo) {

                "Historia" -> c.modoSeguro != "rpg"

                "RPG" -> c.modoSeguro == "rpg"

                else -> true

            }

            val batePublico = when (filtroPublico) {

                "Feminino" -> c.tagsSeguras.any {
                    it.contains("Feminino", ignoreCase = true) ||
                    it.contains("Elas", ignoreCase = true) ||
                    it.contains("Romance", ignoreCase = true) ||
                    it.contains("Enemies to Lovers", ignoreCase = true) ||
                    it.contains("CEO", ignoreCase = true) ||
                    it.contains("Máfia", ignoreCase = true)
                }

                "Masculino" -> c.tagsSeguras.any {
                    it.contains("Masculino", ignoreCase = true) ||
                    it.contains("Eles", ignoreCase = true) ||
                    it.contains("Androide", ignoreCase = true) ||
                    it.contains("Caçadora", ignoreCase = true) ||
                    it.contains("Vampira", ignoreCase = true) ||
                    it.contains("Cassino", ignoreCase = true) ||
                    it.contains("Ação", ignoreCase = true) ||
                    it.contains("Cyberpunk", ignoreCase = true)
                }

                else -> true

            }

            val bateEstilo = estiloFiltro == "Todos" || c.generoSeguro.equals(estiloFiltro, ignoreCase = true)

            val bateBusca = termo.isEmpty() ||

                c.nome.lowercase().contains(termo) ||

                c.subtituloSeguro.lowercase().contains(termo) ||

                c.sinopseSegura.lowercase().contains(termo) ||

                c.generoSeguro.lowercase().contains(termo) ||

                c.tagsSeguras.any { it.lowercase().contains(termo) }

            bateOrigem && bateModo && batePublico && bateEstilo && bateBusca

        }

    }



    // Ranking de personagens ordenados por engajamento (chats + saves)

    val rankingsFiltrados = remember(todosOsCenarios, filtroGeneroRanking) {

        todosOsCenarios.filter { c ->

            when (filtroGeneroRanking) {

                "Feminino" -> c.tagsSeguras.any { it.contains("Feminino", ignoreCase = true) } || c.nome.contains("a", ignoreCase = true)

                "Masculino" -> c.tagsSeguras.any { it.contains("Masculino", ignoreCase = true) } || c.nome.contains("o", ignoreCase = true)

                else -> true

            }

        }.sortedByDescending { it.chats + (it.saves * 50) }

    }



    Column(modifier = Modifier.fillMaxSize()) {

        // TOP BAR: SUB-ABAS [ Explorar | Rankings ] + Busca

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .padding(horizontal = 16.dp, vertical = 8.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.SpaceBetween

        ) {

            Surface(

                color = Color.Black.copy(alpha = 0.5f),

                shape = RoundedCornerShape(50),

                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))

            ) {

                Row(modifier = Modifier.padding(3.dp)) {

                    Surface(

                        color = if (subAba == "Explorar") Color.White.copy(alpha = 0.16f) else Color.Transparent,

                        shape = RoundedCornerShape(50),

                        modifier = Modifier.clickable { subAba = "Explorar" }

                    ) {

                        Text(

                            text = "Explorar",

                            color = if (subAba == "Explorar") Color.White else TextoSuave,

                            fontWeight = if (subAba == "Explorar") FontWeight.Bold else FontWeight.Medium,

                            fontSize = 13.sp,

                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)

                        )

                    }



                    Surface(

                        color = if (subAba == "Rankings") Ouro.copy(alpha = 0.20f) else Color.Transparent,

                        shape = RoundedCornerShape(50),

                        border = if (subAba == "Rankings") BorderStroke(1.dp, Ouro.copy(alpha = 0.4f)) else null,

                        modifier = Modifier.clickable { subAba = "Rankings" }

                    ) {

                        Row(

                            verticalAlignment = Alignment.CenterVertically,

                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)

                        ) {

                            Text("🏆", fontSize = 12.sp)

                            Spacer(Modifier.width(4.dp))

                            Text(

                                text = "Rankings",

                                color = if (subAba == "Rankings") Ouro else TextoSuave,

                                fontWeight = if (subAba == "Rankings") FontWeight.Bold else FontWeight.Medium,

                                fontSize = 13.sp

                            )

                        }

                    }

                }

            }



            // Botão Criar Personagem no topo

            Surface(

                color = Color(0xFF7C4DFF),

                shape = RoundedCornerShape(50),

                modifier = Modifier.clickable { onAbrirCriacao() }

            ) {

                Row(

                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))

                    Spacer(Modifier.width(4.dp))

                    Text("Criar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                }

            }

        }



        // CONTEÚDO PRINCIPAL (EXPLORAR OU RANKINGS)

        if (subAba == "Rankings") {

            // ---- SEÇÃO RANKINGS (Matching Imagem 1) ----

            Column(

                modifier = Modifier

                    .fillMaxSize()

                    .padding(horizontal = 16.dp)

                    .verticalScroll(rememberScrollState()),

                verticalArrangement = Arrangement.spacedBy(12.dp)

            ) {

                // Banner Hero de Destaque no topo do Ranking

                Surface(

                    color = Color(0xFF16181F),

                    shape = RoundedCornerShape(16.dp),

                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),

                    modifier = Modifier

                        .fillMaxWidth()

                        .height(110.dp)

                ) {

                    Box(modifier = Modifier.fillMaxSize()) {

                        Column(

                            modifier = Modifier

                                .fillMaxSize()

                                .padding(16.dp),

                            verticalArrangement = Arrangement.Center

                        ) {

                            Text(

                                "🏆 Ranking da Comunidade",

                                style = MaterialTheme.typography.titleMedium,

                                fontWeight = FontWeight.Bold,

                                color = Color.White

                            )

                            Spacer(Modifier.height(4.dp))

                            Text(

                                "Histórias mais exploradas pelos viajantes do LivennIA.",

                                style = MaterialTheme.typography.bodySmall,

                                color = Ouro

                            )

                        }

                    }

                }



                // Linha de Filtros do Ranking (Feminino ▾ / Semanal ▾)

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    // Filtro Gênero

                    Box {

                        Surface(

                            color = SuperficieAlta,

                            shape = Cantos.pilula,

                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),

                            modifier = Modifier.clickable { menuGeneroAberto = true }

                        ) {

                            Row(

                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Text(filtroGeneroRanking, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                                Spacer(Modifier.width(4.dp))

                                Text("▾", color = TextoSuave, fontSize = 10.sp)

                            }

                        }



                        DropdownMenu(

                            expanded = menuGeneroAberto,

                            onDismissRequest = { menuGeneroAberto = false }

                        ) {

                            listOf("Todos", "Feminino", "Masculino").forEach { g ->

                                DropdownMenuItem(

                                    text = { Text(g) },

                                    onClick = {

                                        filtroGeneroRanking = g

                                        menuGeneroAberto = false

                                    }

                                )

                            }

                        }

                    }



                    // Filtro Período

                    Box {

                        Surface(

                            color = SuperficieAlta,

                            shape = Cantos.pilula,

                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),

                            modifier = Modifier.clickable { menuPeriodoAberto = true }

                        ) {

                            Row(

                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Text(filtroPeriodoRanking, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                                Spacer(Modifier.width(4.dp))

                                Text("▾", color = TextoSuave, fontSize = 10.sp)

                            }

                        }



                        DropdownMenu(

                            expanded = menuPeriodoAberto,

                            onDismissRequest = { menuPeriodoAberto = false }

                        ) {

                            listOf("Semanal", "Geral", "Mais Populares").forEach { p ->

                                DropdownMenuItem(

                                    text = { Text(p) },

                                    onClick = {

                                        filtroPeriodoRanking = p

                                        menuPeriodoAberto = false

                                    }

                                )

                            }

                        }

                    }

                }



                // Lista de Itens do Ranking com Medalhas de Pódio (🥇 🥈 🥉)

                rankingsFiltrados.forEachIndexed { index, cenario ->

                    val posicao = index + 1

                    Surface(

                        color = SuperficieAlta,

                        shape = RoundedCornerShape(14.dp),

                        border = BorderStroke(1.dp, if (posicao == 1) Ouro.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f)),

                        modifier = Modifier

                            .fillMaxWidth()

                            .clickable {
                                val modo = cenario.modoSeguro
                                vm.escolherCenario(cenario, modo)
                                vm.criarPersonagem(cenario.nome)
                            }

                    ) {

                        Row(

                            modifier = Modifier.padding(12.dp),

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            // Medalha ou Posição

                            Box(

                                modifier = Modifier.width(28.dp),

                                contentAlignment = Alignment.Center

                            ) {

                                when (posicao) {

                                    1 -> Text("🥇", fontSize = 20.sp)

                                    2 -> Text("🥈", fontSize = 20.sp)

                                    3 -> Text("🥉", fontSize = 20.sp)

                                    else -> Text("$posicao", color = TextoSuave, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                }

                            }



                            Spacer(Modifier.width(8.dp))



                            // Avatar Redondo do Personagem

                            Box(

                                modifier = Modifier

                                    .size(54.dp)

                                    .clip(RoundedCornerShape(12.dp))

                                    .background(Color.DarkGray)

                            ) {

                                CapaCenario(

                                    cenarioId = cenario.id,

                                    temCapa = cenario.capa,

                                    emoji = cenario.emojiSeguro,

                                    nome = cenario.nome,

                                    versao = vm.capaVersao,

                                    modifier = Modifier.fillMaxSize(),

                                    recorte = RoundedCornerShape(12.dp)

                                )

                            }



                            Spacer(Modifier.width(12.dp))



                            // Detalhes do Personagem no Ranking

                            Column(modifier = Modifier.weight(1f)) {

                                Text(

                                    text = cenario.nome,

                                    fontWeight = FontWeight.Bold,

                                    fontSize = 14.sp,

                                    color = Color.White

                                )

                                Spacer(Modifier.height(2.dp))



                                // Métricas (👤 saves / 💬 chats)

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Text("👤", fontSize = 10.sp)

                                    Spacer(Modifier.width(3.dp))

                                    Text(

                                        "${cenario.saves}",

                                        color = TextoSuave,

                                        fontSize = 11.sp

                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text("💬", fontSize = 10.sp)

                                    Spacer(Modifier.width(3.dp))

                                    Text(

                                        "${cenario.chats}",

                                        color = Ouro,

                                        fontSize = 11.sp,

                                        fontWeight = FontWeight.Bold

                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(

                                        text = if (cenario.modoSeguro == "rpg") "• ⚔️ RPG" else "• 📜 Leitura",

                                        color = Color(0xFF7C4DFF),

                                        fontSize = 10.sp,

                                        fontWeight = FontWeight.SemiBold

                                    )

                                    if (cenario.criadoPorUsuario) {

                                        Spacer(Modifier.width(4.dp))

                                        Text("• Por ${cenario.autorNomeSeguro}", color = TextoSuave, fontSize = 10.sp, maxLines = 1)

                                    }

                                }



                                Spacer(Modifier.height(3.dp))

                                Text(

                                    text = cenario.sinopseSegura,

                                    color = TextoSuave,

                                    fontSize = 11.sp,

                                    maxLines = 2,

                                    overflow = TextOverflow.Ellipsis

                                )

                            }

                        }

                    }

                }



                Spacer(Modifier.height(40.dp))

            }

        } else {

            // ---- SEÇÃO EXPLORAR (VITRINE COM SEPARAÇÃO IA VS USUÁRIO) ----

            LazyVerticalGrid(

                columns = GridCells.Fixed(2),

                modifier = Modifier.fillMaxSize(),

                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),

                horizontalArrangement = Arrangement.spacedBy(10.dp),

                verticalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                // Banner Hero no topo do feed

                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {

                    Surface(

                        color = Color(0xFF16181F),

                        shape = RoundedCornerShape(16.dp),

                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),

                        modifier = Modifier

                            .fillMaxWidth()

                            .height(105.dp)

                    ) {

                        Column(

                            modifier = Modifier

                                .fillMaxSize()

                                .padding(16.dp),

                            verticalArrangement = Arrangement.Center

                        ) {

                            Text(

                                "🌌 Universos & Narrativas",

                                style = MaterialTheme.typography.titleMedium,

                                fontWeight = FontWeight.Bold,

                                color = Color.White

                            )

                            Spacer(Modifier.height(4.dp))

                            Text(

                                "Histórias vivas criadas pela comunidade e pelos estúdios LivennIA.",

                                style = MaterialTheme.typography.bodySmall,

                                color = Ouro

                            )

                        }

                    }

                }



                // 1. BARRA DE BUSCA E FILTROS EM LISTAS SUSPENSAS (DROPDOWNS CLEAN)
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Linha 1: Busca elegante com cantos 3D
                        OutlinedTextField(
                            value = busca,
                            onValueChange = { busca = it.take(60) },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null, tint = TextoSuave, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                if (busca.isNotEmpty()) {
                                    IconButton(onClick = { busca = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Limpar busca", tint = TextoSuave, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            placeholder = { Text("Buscar personagem, universo ou criador...", style = MaterialTheme.typography.bodyMedium) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Linha 2: Listas Suspensas (Dropdowns) para Modo, Público e Gênero
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Dropdown Modo de Jogo & Origem
                            val modoLabel = when {
                                filtroModoJogo == "Historia" -> "📖 Histórias"
                                filtroModoJogo == "RPG" -> "🎲 RPG de Mesa"
                                filtroOrigem == "Usuarios" -> "👥 Comunidade"
                                filtroOrigem == "IA" -> "🤖 Oficiais IA"
                                else -> "🌟 Modo: Todos"
                            }
                            val modoAtivo = filtroModoJogo != "Todos" || filtroOrigem != "Todos"

                            Box {
                                Surface(
                                    color = if (modoAtivo) Ouro.copy(alpha = 0.18f) else Color(0xFF161824),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (modoAtivo) Ouro else Color.White.copy(alpha = 0.12f)),
                                    shadowElevation = if (modoAtivo) 4.dp else 1.dp,
                                    modifier = Modifier.interativo3D(escalaPressionado = 0.94f) {
                                        menuModoAberto = true
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = modoLabel,
                                            color = if (modoAtivo) Ouro else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (modoAtivo) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("▾", color = if (modoAtivo) Ouro else TextoSuave, fontSize = 11.sp)
                                    }
                                }

                                DropdownMenu(
                                    expanded = menuModoAberto,
                                    onDismissRequest = { menuModoAberto = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🌟 Todos os Modos", fontWeight = if (!modoAtivo) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            filtroModoJogo = "Todos"
                                            filtroOrigem = "Todos"
                                            menuModoAberto = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("📖 Histórias Interativas", fontWeight = if (filtroModoJogo == "Historia") FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            filtroModoJogo = "Historia"
                                            filtroOrigem = "Todos"
                                            menuModoAberto = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🎲 RPG de Mesa", fontWeight = if (filtroModoJogo == "RPG") FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            filtroModoJogo = "RPG"
                                            filtroOrigem = "Todos"
                                            menuModoAberto = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("👥 Criações da Comunidade", fontWeight = if (filtroOrigem == "Usuarios") FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            filtroOrigem = "Usuarios"
                                            filtroModoJogo = "Todos"
                                            menuModoAberto = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🤖 Oficiais IA (LivennIA)", fontWeight = if (filtroOrigem == "IA") FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            filtroOrigem = "IA"
                                            filtroModoJogo = "Todos"
                                            menuModoAberto = false
                                        }
                                    )
                                }
                            }

                            // 2. Dropdown Público (Correlacionado: Homens interagem com mulheres, mulheres com homens)
                            val publicoLabel = when (filtroPublico) {
                                "Masculino" -> "👨 Para Homens (Elas)"
                                "Feminino" -> "👩 Para Mulheres (Eles)"
                                else -> "🌐 Público: Todos"
                            }
                            val publicoAtivo = filtroPublico != "Todos"

                            Box {
                                Surface(
                                    color = if (publicoAtivo) Ouro.copy(alpha = 0.18f) else Color(0xFF161824),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (publicoAtivo) Ouro else Color.White.copy(alpha = 0.12f)),
                                    shadowElevation = if (publicoAtivo) 4.dp else 1.dp,
                                    modifier = Modifier.interativo3D(escalaPressionado = 0.94f) {
                                        menuPublicoAberto = true
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = publicoLabel,
                                            color = if (publicoAtivo) Ouro else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (publicoAtivo) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("▾", color = if (publicoAtivo) Ouro else TextoSuave, fontSize = 11.sp)
                                    }
                                }

                                DropdownMenu(
                                    expanded = menuPublicoAberto,
                                    onDismissRequest = { menuPublicoAberto = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🌐 Todos os Públicos", fontWeight = if (filtroPublico == "Todos") FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            filtroPublico = "Todos"
                                            menuPublicoAberto = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("👨 Para Homens (Interagir com Elas)", fontWeight = if (filtroPublico == "Masculino") FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            filtroPublico = "Masculino"
                                            menuPublicoAberto = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("👩 Para Mulheres (Interagir com Eles)", fontWeight = if (filtroPublico == "Feminino") FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            filtroPublico = "Feminino"
                                            menuPublicoAberto = false
                                        }
                                    )
                                }
                            }

                            // 3. Dropdown Gênero / Estilo Narrativo
                            val estiloLabel = if (estiloFiltro == "Todos") "✨ Gêneros" else "✨ $estiloFiltro"
                            val estiloAtivo = estiloFiltro != "Todos"

                            Box {
                                Surface(
                                    color = if (estiloAtivo) Ouro.copy(alpha = 0.18f) else Color(0xFF161824),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (estiloAtivo) Ouro else Color.White.copy(alpha = 0.12f)),
                                    shadowElevation = if (estiloAtivo) 4.dp else 1.dp,
                                    modifier = Modifier.interativo3D(escalaPressionado = 0.94f) {
                                        menuEstiloAberto = true
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = estiloLabel,
                                            color = if (estiloAtivo) Ouro else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (estiloAtivo) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("▾", color = if (estiloAtivo) Ouro else TextoSuave, fontSize = 11.sp)
                                    }
                                }

                                DropdownMenu(
                                    expanded = menuEstiloAberto,
                                    onDismissRequest = { menuEstiloAberto = false }
                                ) {
                                    estilosDisponiveis.forEach { estilo ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (estilo == "Todos") "✨ Todos os Gêneros" else estilo,
                                                    fontWeight = if (estiloFiltro == estilo) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                estiloFiltro = estilo
                                                menuEstiloAberto = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Botão de Limpar Filtros quando algum filtro estiver ativo
                            if (modoAtivo || publicoAtivo || estiloAtivo || busca.isNotBlank()) {
                                Surface(
                                    color = Color(0xFF2C1B1F),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.45f)),
                                    modifier = Modifier.interativo3D(escalaPressionado = 0.92f) {
                                        filtroModoJogo = "Todos"
                                        filtroPublico = "Todos"
                                        filtroOrigem = "Todos"
                                        estiloFiltro = "Todos"
                                        busca = ""
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("✕ Limpar", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }



                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {

                    Row(

                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Text(

                            when (filtroOrigem) {

                                "IA" -> "Oficiais da IA (${catalogoFiltrado.size})"

                                "Usuarios" -> "Criações dos Usuários (${catalogoFiltrado.size})"

                                else -> "Personagens & Mundos (${catalogoFiltrado.size})"

                            },

                            style = MaterialTheme.typography.titleSmall,

                            fontWeight = FontWeight.SemiBold

                        )

                        if (estiloFiltro != "Todos" || busca.isNotBlank() || filtroOrigem != "Todos") {

                            Text(

                                "Ver todos",

                                color = Ouro,

                                style = MaterialTheme.typography.labelSmall,

                                modifier = Modifier.clickable { estiloFiltro = "Todos"; busca = ""; filtroOrigem = "Todos" }

                            )

                        }

                    }

                }



                if (catalogoFiltrado.isEmpty()) {

                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {

                        Surface(

                            color = SuperficieAlta,

                            shape = Cantos.cartao,

                            modifier = Modifier.fillMaxWidth()

                        ) {

                            Column(

                                modifier = Modifier.padding(20.dp),

                                horizontalAlignment = Alignment.CenterHorizontally

                            ) {

                                if (filtroOrigem == "Usuarios") {
                                    Text("Nenhuma história da comunidade criada ainda.", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("As histórias criadas pelos usuários aparecerão aqui assim que forem publicadas.", color = TextoSuave, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = onAbrirCriacao, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7))) {
                                        Text("Criar Nova História", color = Color.White)
                                    }
                                } else {
                                    Text("Nenhum personagem encontrado para esse filtro.", color = TextoSuave)
                                    Spacer(Modifier.height(6.dp))
                                    TextButton(onClick = { busca = ""; estiloFiltro = "Todos"; filtroOrigem = "Todos" }) {
                                        Text("Limpar filtros")
                                    }
                                }

                            }

                        }

                    }

                }



                items(catalogoFiltrado, key = { it.id }) { cenario ->

                    CardCenario(

                        cenario = cenario,

                        versao = vm.capaVersao,

                        gerando = vm.gerandoCapa == cenario.id,

                        habilitado = !vm.ocupado,

                        onAbrir = {
                            onAbrirDetalhes(cenario)
                        },

                        onAbrirInfo = { onAbrirInfo(cenario) },

                        onGerarCapa = { vm.gerarCapaDoCenario(cenario.id) }

                    )

                }

            }

        }

    }

}



@Composable
private fun AbaOrigemPill(titulo: String, selecionado: Boolean, aoClicar: () -> Unit) {
    Surface(
        color = if (selecionado) Ouro.copy(alpha = 0.25f) else Color(0xFF131522),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.2.dp,
            if (selecionado) Ouro else Color.White.copy(alpha = 0.08f)
        ),
        shadowElevation = if (selecionado) 6.dp else 2.dp,
        modifier = Modifier.interativo3D(escalaPressionado = 0.93f) { aoClicar() }
    ) {
        Text(
            text = titulo,
            color = if (selecionado) Ouro else TextoSuave,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}



// Um card da vitrine: arte full-bleed estilo poster, selo de estilo, badge de popularidade e texto em degradê.

@OptIn(ExperimentalLayoutApi::class)

@Composable

private fun CardCenario(
    cenario: CenarioDto,
    versao: Int,
    gerando: Boolean,
    habilitado: Boolean,
    onAbrir: () -> Unit,
    onAbrirInfo: () -> Unit = {},
    onGerarCapa: () -> Unit
) {
    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardCenarioScale"
    )

    val corDestaque = remember(cenario.criadoPorUsuario, cenario.modoSeguro) {
        when {
            cenario.criadoPorUsuario -> Color(0xFFA78BFA)
            cenario.modoSeguro == "rpg" -> Color(0xFFFF9F43)
            else -> Color(0xFF38BDF8)
        }
    }

    Surface(
        color = Color(0xFF13141F),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(
                    corDestaque.copy(alpha = 0.75f),
                    Color(0xFF7C4DFF).copy(alpha = 0.35f),
                    Color(0xFF13141F).copy(alpha = 0.15f)
                )
            )
        ),
        shadowElevation = if (isPressed) 3.dp else 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.60f)
            .scale(cardScale)
            .clickable(
                enabled = habilitado,
                interactionSource = pressInteraction,
                indication = null
            ) { onAbrir() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Imagem Moldurada (Estilo Modelos de Persona)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0D0E15))
            ) {
                CapaCenario(
                    cenarioId = cenario.id,
                    temCapa = cenario.capa,
                    emoji = cenario.emojiSeguro,
                    nome = cenario.nome,
                    versao = versao,
                    modifier = Modifier.fillMaxSize(),
                    recorte = RoundedCornerShape(14.dp)
                )

                // Suave gradiente escuro de transição no fundo da moldura da imagem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF13141F).copy(alpha = 0.70f))
                            )
                        )
                )

                // Selo de origem no canto superior esquerdo
                Surface(
                    color = if (cenario.criadoPorUsuario) Color(0xFF6C5CE7).copy(alpha = 0.90f) else Color.Black.copy(alpha = 0.65f),
                    shape = Cantos.pilula,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (cenario.criadoPorUsuario) "👥 ${cenario.autorNomeSeguro}" else "🤖 ${cenario.generoSeguro}",
                        color = Color.White.copy(alpha = 0.95f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Badges no canto superior direito (Engajamento / Info)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = Cantos.pilula
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (cenario.modoSeguro == "rpg") "⚔️" else "📜", fontSize = 9.sp)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "${cenario.chats}",
                                color = Ouro,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onAbrirInfo() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("ℹ️", fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Informações abaixo da imagem (Idêntico aos Modelos de Persona)
            Text(
                "${cenario.emojiSeguro} ${cenario.nome}",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (cenario.subtituloSeguro.isNotBlank()) {
                Spacer(Modifier.height(1.dp))
                Text(
                    cenario.subtituloSeguro,
                    color = Ouro,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                cenario.sinopseSegura,
                color = TextoSuave,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (cenario.tagsSeguras.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    cenario.tagsSeguras.take(2).forEach { tag ->
                        Surface(
                            color = Color.White.copy(alpha = 0.07f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tag,
                                color = TextoSuave,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



// Modal de Apresentação Imersivo (Character Sheet): arte ampliada, gancho de abertura do personagem e botões modernos

@OptIn(ExperimentalLayoutApi::class)

@Composable

private fun DialogoPreviaCenario(

    cenario: CenarioDto,

    versaoCapa: Int,

    onComecarHistoria: () -> Unit,

    onComecarRpg: () -> Unit,

    onFechar: () -> Unit

) {

    Dialog(

        onDismissRequest = onFechar,

        properties = DialogProperties(usePlatformDefaultWidth = false)

    ) {

        Surface(

            color = SuperficieAlta,

            shape = RoundedCornerShape(26.dp),

            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),

            modifier = Modifier

                .fillMaxWidth(0.94f)

                .fillMaxHeight(0.88f)

        ) {

            Column(

                modifier = Modifier

                    .fillMaxSize()

                    .verticalScroll(rememberScrollState())

            ) {

                // Banner / Poster superior de alta qualidade

                Box(

                    modifier = Modifier

                        .fillMaxWidth()

                        .height(230.dp)

                ) {

                    CapaCenario(

                        cenarioId = cenario.id,

                        temCapa = cenario.capa,

                        emoji = cenario.emojiSeguro,

                        nome = cenario.nome,

                        versao = versaoCapa,

                        modifier = Modifier.fillMaxSize(),

                        recorte = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)

                    )



                    // Degradê sutil sobre a foto

                    Box(

                        modifier = Modifier

                            .fillMaxSize()

                            .background(

                                Brush.verticalGradient(

                                    0f to Color.Transparent,

                                    0.55f to Color.Black.copy(alpha = 0.40f),

                                    1f to SuperficieAlta

                                )

                            )

                    )



                    // Botão Fechar no topo direito

                    Surface(

                        color = Color.Black.copy(alpha = 0.65f),

                        shape = CircleShape,

                        modifier = Modifier

                            .align(Alignment.TopEnd)

                            .padding(12.dp)

                            .clickable { onFechar() }

                    ) {

                        Text(

                            "✕",

                            color = Color.White,

                            style = MaterialTheme.typography.bodyMedium,

                            fontWeight = FontWeight.Bold,

                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)

                        )

                    }



                    // Badges no rodapé da capa

                    Row(

                        modifier = Modifier

                            .align(Alignment.BottomStart)

                            .padding(horizontal = 16.dp, vertical = 8.dp),

                        horizontalArrangement = Arrangement.spacedBy(8.dp),

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        ChipTag(cenario.generoSeguro, destaque = true)

                        Surface(

                            color = Color.Black.copy(alpha = 0.65f),

                            shape = Cantos.pilula

                        ) {

                            Text(
                                text = if (cenario.chats > 0) "🔥 ${cenario.chats} conversas" else "✨ Nova história",
                                color = Ouro,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )

                        }

                    }

                }



                // Corpo com os detalhes do personagem

                Column(

                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),

                    verticalArrangement = Arrangement.spacedBy(14.dp)

                ) {

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

                        Text(

                            "${cenario.emojiSeguro} ${cenario.nome}",

                            style = MaterialTheme.typography.headlineSmall,

                            fontWeight = FontWeight.Bold,

                            color = MaterialTheme.colorScheme.onBackground

                        )

                        if (cenario.subtituloSeguro.isNotBlank()) {

                            Text(

                                cenario.subtituloSeguro,

                                color = Ouro,

                                style = MaterialTheme.typography.labelLarge,

                                fontWeight = FontWeight.Medium

                            )

                        }

                    }



                    // Primeira Mensagem / Cena de Abertura (Estilo Balão do Personagem)

                    if (cenario.aberturaSegura.isNotBlank()) {

                        Surface(

                            color = Fundo.copy(alpha = 0.85f),

                            shape = RoundedCornerShape(16.dp),

                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),

                            modifier = Modifier.fillMaxWidth()

                        ) {

                            Column(modifier = Modifier.padding(14.dp)) {

                                Row(

                                    verticalAlignment = Alignment.CenterVertically,

                                    horizontalArrangement = Arrangement.spacedBy(6.dp)

                                ) {

                                    Text("💬", style = MaterialTheme.typography.labelSmall)

                                    Text(

                                        "PRIMEIRA MENSAGEM DO PERSONAGEM",

                                        color = Ouro,

                                        style = MaterialTheme.typography.labelSmall,

                                        fontWeight = FontWeight.SemiBold

                                    )

                                }

                                Spacer(Modifier.height(6.dp))

                                Text(

                                    "“${cenario.aberturaSegura}”",

                                    style = MaterialTheme.typography.bodyMedium.copy(

                                        fontStyle = FontStyle.Italic

                                    ),

                                    color = Color.White.copy(alpha = 0.95f),

                                    lineHeight = 22.sp

                                )

                            }

                        }

                    }



                    // Sinopse

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                        Text(

                            "✦ Sobre a História",

                            style = MaterialTheme.typography.labelMedium,

                            color = TextoSuave,

                            fontWeight = FontWeight.SemiBold

                        )

                        Text(

                            cenario.sinopseSegura,

                            style = MaterialTheme.typography.bodyMedium,

                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.90f),

                            lineHeight = 20.sp

                        )

                    }



                    // Tags

                    if (cenario.tagsSeguras.isNotEmpty()) {

                        FlowRow(

                            horizontalArrangement = Arrangement.spacedBy(6.dp),

                            verticalArrangement = Arrangement.spacedBy(6.dp)

                        ) {

                            cenario.tagsSeguras.forEach { tag -> ChipTag(tag) }

                        }

                    }



                    Spacer(Modifier.height(4.dp))



                    // Botões de Ação

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Button(

                            onClick = onComecarHistoria,

                            modifier = Modifier

                                .fillMaxWidth()

                                .height(50.dp),

                            shape = RoundedCornerShape(50),

                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(

                                containerColor = Ouro,

                                contentColor = Color.Black

                            )

                        ) {

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Text("💬", style = MaterialTheme.typography.bodyLarge)

                                Spacer(Modifier.width(8.dp))

                                Text(

                                    "Conversar • Modo História",

                                    style = MaterialTheme.typography.titleSmall,

                                    fontWeight = FontWeight.Bold

                                )

                            }

                        }



                        OutlinedButton(

                            onClick = onComecarRpg,

                            modifier = Modifier

                                .fillMaxWidth()

                                .height(46.dp),

                            shape = RoundedCornerShape(50),

                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.65f))

                        ) {

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Text("🎲", style = MaterialTheme.typography.bodyMedium)

                                Spacer(Modifier.width(8.dp))

                                Text(

                                    "Aventura RPG (Com Ficha & Escolhas)",

                                    color = Ouro,

                                    style = MaterialTheme.typography.labelLarge,

                                    fontWeight = FontWeight.SemiBold

                                )

                            }

                        }

                    }



                    Spacer(Modifier.height(8.dp))

                }

            }

        }

    }

}



// Pílula escura para ficar POR CIMA de uma imagem (as tags das capas).

@Composable

private fun ChipSobreArte(texto: String) {

    Surface(color = Color.Black.copy(alpha = 0.42f), shape = Cantos.pilula) {

        Text(

            texto,

            color = Color.White,

            style = MaterialTheme.typography.labelSmall,

            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)

        )

    }

}



// ---------------------------------------------------------------

// Aba "Perfil": conta, limites e atalhos.

// ---------------------------------------------------------------

@Composable

private fun AbaPerfil(vm: AppViewModel) {

    val contexto = LocalContext.current

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    var confirmarApagarConta by rememberSaveable { mutableStateOf(false) }

    var confirmarSair by rememberSaveable { mutableStateOf(false) }

    var mostrarEditarPerfil by rememberSaveable { mutableStateOf(false) }

    var mostrarDialogoFotoPerfil by rememberSaveable { mutableStateOf(false) }

    var mostrarDialogoCarteira by rememberSaveable { mutableStateOf(false) }

    var mostrarDialogoVip by rememberSaveable { mutableStateOf(false) }

    var mostrarDialogoPersona by rememberSaveable { mutableStateOf(false) }

    var mostrarDialogoLaboratorio by rememberSaveable { mutableStateOf(false) }

    var mostrarDialogoDiscord by rememberSaveable { mutableStateOf(false) }



    val jogosRpg = vm.jogos.filter { it.modo != "historia" }

    val jogosHistoria = vm.jogos.filter { it.modo == "historia" }

    val uidDisplay = "LVN-" + (vm.apelido.hashCode().absoluteValue % 900000 + 100000).toString()



    LazyColumn(

        modifier = Modifier

            .fillMaxSize()

            .background(Color(0xFF000000)),

        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp)

    ) {

        // ---- 1. TOPO: TÍTULO E ÍCONES SUPERIORES (DISCORD, ESTRELA FEEDBACK, ENGRENAGEM CONFIG) ----

        item {

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(vertical = 4.dp),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Text(

                    text = "Perfil",

                    style = MaterialTheme.typography.headlineMedium,

                    fontWeight = FontWeight.Bold,

                    color = Color.White

                )



                // Ícones de ação no topo direito

                Row(

                    horizontalArrangement = Arrangement.spacedBy(6.dp),

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    // Ícone Discord
                    IconButton(
                        onClick = { mostrarDialogoDiscord = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Surface(
                            color = Color(0xFF151828),
                            shape = CircleShape,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFF5865F2), Color(0xFF1E2238)))),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💬", fontSize = 16.sp)
                            }
                        }
                    }

                    // ÍCONE ESTRELA: FÓRUM INTERATIVO & FEEDBACK DA COMUNIDADE
                    IconButton(
                        onClick = { vm.navegarPara(Tela.Feedback) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Surface(
                            color = Color(0xFF151828),
                            shape = CircleShape,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFF1E2238)))),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⭐", fontSize = 16.sp)
                            }
                        }
                    }

                    // Ícone Configurações (Engrenagem)
                    IconButton(
                        onClick = { vm.navegarPara(Tela.Configuracoes) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Surface(
                            color = Color(0xFF151828),
                            shape = CircleShape,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF1E2238)))),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "Configurações",
                                    tint = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }



        // ---- 2. CARTÃO DO USUÁRIO: AVATAR, APELIDO, UID E BOTÃO EDITAR ----

        item {

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(vertical = 4.dp),

                verticalAlignment = Alignment.CenterVertically

            ) {

                // Avatar redondo com indicador de edição

                Box(

                    modifier = Modifier.clickable { mostrarDialogoFotoPerfil = true }

                ) {

                    RetratoPersona(
                        temRetrato = vm.personaTemRetrato,
                        emojiOuPadrao = vm.personaAvatar.ifBlank { "👤" },
                        tamanho = 72,
                        versao = vm.personaRetratoVersao,
                        recorte = CircleShape
                    )

                    Surface(

                        color = Color(0xFFF5C842),

                        shape = CircleShape,

                        border = BorderStroke(2.dp, Color.Black),

                        modifier = Modifier

                            .size(22.dp)

                            .align(Alignment.BottomEnd)

                    ) {

                        Box(contentAlignment = Alignment.Center) {

                            Text("✎", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)

                        }

                    }

                }



                Spacer(Modifier.width(16.dp))



                Column(modifier = Modifier.weight(1f)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vm.apelido,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (vm.conta?.dono == true) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFF5C842).copy(alpha = 0.20f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFF5C842).copy(alpha = 0.6f))
                            ) {
                                Text(
                                    "👑 DONO",
                                    color = Color(0xFFF5C842),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(

                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.spacedBy(8.dp)

                    ) {

                        Surface(

                            color = Color(0xFF161618),

                            shape = RoundedCornerShape(8.dp),

                            border = BorderStroke(1.dp, Color(0xFF26262A)),

                            modifier = Modifier.clickable {

                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(uidDisplay))

                                Toast.makeText(contexto, "UID copiado!", Toast.LENGTH_SHORT).show()

                            }

                        ) {

                            Row(

                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Text(

                                    text = "UID: $uidDisplay",

                                    color = Color(0xFF8E8E93),

                                    fontSize = 11.sp,

                                    fontWeight = FontWeight.Medium

                                )

                                Spacer(Modifier.width(4.dp))

                                Text("📋", fontSize = 10.sp)

                            }

                        }



                        Surface(

                            color = Color(0xFF161618),

                            shape = RoundedCornerShape(8.dp),

                            border = BorderStroke(1.dp, Color(0xFF26262A)),

                            modifier = Modifier.clickable { mostrarDialogoFotoPerfil = true }

                        ) {

                            Text(

                                text = "Editar perfil",

                                color = Color.White,

                                fontSize = 11.sp,

                                fontWeight = FontWeight.SemiBold,

                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)

                            )

                        }

                    }

                }

            }

        }



        // ---- 3. LINHA DE ESTATÍSTICAS / MÉTRICAS (CHATS, SALVOS, SEGUIDORES, SEGUINDO) ----

        item {

            Surface(

                color = Color(0xFF161618),

                shape = RoundedCornerShape(16.dp),

                border = BorderStroke(1.dp, Color(0xFF26262A)),

                modifier = Modifier.fillMaxWidth()

            ) {

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .padding(vertical = 12.dp),

                    horizontalArrangement = Arrangement.SpaceEvenly,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    MetricaPerfilItem(valor = vm.jogos.size.toString(), rotulo = "Chats")

                    DivisorVerticalPerfil()

                    MetricaPerfilItem(valor = vm.jogos.count { !it.encerrado }.toString(), rotulo = "Ativos")

                    DivisorVerticalPerfil()

                    MetricaPerfilItem(
                        valor = if (vm.conta?.dono == true) "∞" else (vm.conta?.livens ?: 0).toString(),
                        rotulo = "Livens",
                        corValor = Color(0xFFF5C842)
                    )

                    DivisorVerticalPerfil()

                    MetricaPerfilItem(valor = (vm.jogos.size + 1).toString(), rotulo = "Nível")

                }

            }

        }



        // ---- 4. CARDS DE PLANOS & BENEFÍCIOS (ULTRA E PLUS - Matching Imagem 2) ----

        item {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                // Card Ultra VIP (Dourado)

                Surface(

                    color = Color(0xFF1B1812),

                    shape = RoundedCornerShape(16.dp),

                    border = BorderStroke(1.dp, Color(0xFFF5C842).copy(alpha = 0.4f)),

                    modifier = Modifier

                        .weight(1f)

                        .clickable { vm.navegarPara(Tela.Assinatura("Ultra")) }

                ) {

                    Row(

                        modifier = Modifier.padding(14.dp),

                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(

                                text = "Liven Ultra",

                                fontWeight = FontWeight.Bold,

                                fontSize = 15.sp,

                                color = Color(0xFFF5C842)

                            )

                            Spacer(Modifier.height(2.dp))

                            Text(

                                text = "2000 Livens + Prosa VIP",

                                fontSize = 11.sp,

                                color = Color(0xFFB89832)

                            )

                        }

                        Text("👑", fontSize = 24.sp)

                    }

                }



                // Card Plus (Prateado / Aço)

                Surface(

                    color = Color(0xFF14171C),

                    shape = RoundedCornerShape(16.dp),

                    border = BorderStroke(1.dp, Color(0xFF8EAFCE).copy(alpha = 0.35f)),

                    modifier = Modifier

                        .weight(1f)

                        .clickable { vm.navegarPara(Tela.Assinatura("Plus")) }

                ) {

                    Row(

                        modifier = Modifier.padding(14.dp),

                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(

                                text = "Liven Plus",

                                fontWeight = FontWeight.Bold,

                                fontSize = 15.sp,

                                color = Color(0xFFB0CDE8)

                            )

                            Spacer(Modifier.height(2.dp))

                            Text(

                                text = "7 Modelos Pro liberados",

                                fontSize = 11.sp,

                                color = Color(0xFF7A9BB8)

                            )

                        }

                        Text("✨", fontSize = 24.sp)

                    }

                }

            }

        }



        // ---- 5. BOTÕES DE ACESSO RÁPIDO: CLUBE VIP, CARTEIRA, PERSONA, LABORATÓRIO ----

        item {

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(vertical = 4.dp),

                horizontalArrangement = Arrangement.SpaceBetween

            ) {

                BotaoAcaoRedondoPerfil(

                    icone = "👑",

                    rotulo = "VIP",

                    aoClicar = { mostrarDialogoVip = true }

                )

                BotaoAcaoRedondoPerfil(

                    icone = "⚡",

                    rotulo = "Carteira",

                    aoClicar = { mostrarDialogoCarteira = true }

                )

                BotaoAcaoRedondoPerfil(

                    icone = "🎭",

                    rotulo = "Persona",

                    aoClicar = { mostrarDialogoPersona = true }

                )

                BotaoAcaoRedondoPerfil(

                    icone = "🧪",

                    rotulo = "Laboratório",

                    aoClicar = { mostrarDialogoLaboratorio = true }

                )

            }

        }



        // ---- 6. SEÇÃO "PERSONAGEM" / MINHAS CRIAÇÕES ----

        item {

            Column(

                modifier = Modifier.fillMaxWidth(),

                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {

                Text(

                    text = "Personagem & Aventuras",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold,

                    color = Color.White

                )



                if (vm.jogos.isEmpty()) {

                    // Ilustração da Caixa Surpresa 3D

                    Surface(

                        color = Color(0xFF161618),

                        shape = RoundedCornerShape(20.dp),

                        border = BorderStroke(1.dp, Color(0xFF26262A)),

                        modifier = Modifier

                            .fillMaxWidth()

                            .padding(vertical = 8.dp)

                    ) {

                        Column(

                            modifier = Modifier

                                .fillMaxWidth()

                                .padding(vertical = 28.dp, horizontal = 16.dp),

                            horizontalAlignment = Alignment.CenterHorizontally,

                            verticalArrangement = Arrangement.Center

                        ) {

                            Text("📦", fontSize = 54.sp)

                            Spacer(Modifier.height(10.dp))

                            Text(

                                text = "Nenhum personagem ainda",

                                style = MaterialTheme.typography.bodyMedium,

                                fontWeight = FontWeight.Medium,

                                color = Color(0xFF8E8E93)

                            )

                            Spacer(Modifier.height(14.dp))

                            Button(

                                onClick = { vm.comecarAventuraLivre() },

                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C842)),

                                shape = RoundedCornerShape(50),

                                modifier = Modifier

                                    .width(140.dp)

                                    .height(42.dp)

                            ) {

                                Text(

                                    text = "Criar",

                                    color = Color.Black,

                                    fontWeight = FontWeight.Bold,

                                    fontSize = 14.sp

                                )

                            }

                        }

                    }

                } else {

                    // Lista das aventuras ativas do jogador

                    vm.jogos.take(4).forEach { jogo ->

                        Surface(

                            color = Color(0xFF161618),

                            shape = RoundedCornerShape(14.dp),

                            border = BorderStroke(1.dp, Color(0xFF26262A)),

                            modifier = Modifier

                                .fillMaxWidth()

                                .clickable { vm.abrirJogo(jogo.jogoId) }

                        ) {

                            Row(

                                modifier = Modifier.padding(12.dp),

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                AvatarPersonagem(seed = jogo.avatarSeed, nome = jogo.nome, tamanho = 46)

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {

                                    Text(

                                        text = jogo.nome,

                                        style = MaterialTheme.typography.titleSmall,

                                        fontWeight = FontWeight.Bold,

                                        color = Color.White

                                    )

                                    Text(

                                        text = "${jogo.cenarioNome} • ${if (jogo.modo == "historia") "História" else "RPG"}",

                                        style = MaterialTheme.typography.bodySmall,

                                        color = Color(0xFF8E8E93)

                                    )

                                }

                                Surface(

                                    color = Color(0xFFF5C842).copy(alpha = 0.15f),

                                    shape = RoundedCornerShape(50)

                                ) {

                                    Text(

                                        text = "Jogar ▶",

                                        color = Color(0xFFF5C842),

                                        fontSize = 12.sp,

                                        fontWeight = FontWeight.Bold,

                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)

                                    )

                                }

                            }

                        }

                    }

                }

            }

        }



        // ---- 7. CONFIGURAÇÕES ADICIONAIS & CONTA ----

        item {

            CardAtualizacaoGitHub(vm)

        }



        item {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                OutlinedButton(

                    onClick = { vm.alternarTema() },

                    shape = RoundedCornerShape(12.dp),

                    modifier = Modifier.weight(1f)

                ) {

                    Text(if (vm.temaEscuro) "☀ Claro" else "🌙 Escuro", fontSize = 12.sp)

                }



                OutlinedButton(

                    onClick = { confirmarSair = true },

                    shape = RoundedCornerShape(12.dp),

                    modifier = Modifier.weight(1f)

                ) {

                    Text("Sair da Conta", fontSize = 12.sp)

                }

            }

        }

    }



    // DIÁLOGOS DE APOIO
    if (mostrarEditarPerfil) {
        var novoNome by remember { mutableStateOf(vm.apelido) }
        AlertDialog(
            onDismissRequest = { mostrarEditarPerfil = false },
            title = { Text("Editar Perfil", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.clickable {
                                mostrarEditarPerfil = false
                                mostrarDialogoFotoPerfil = true
                            }
                        ) {
                            RetratoPersona(
                                temRetrato = vm.personaTemRetrato,
                                emojiOuPadrao = vm.personaAvatar.ifBlank { "👤" },
                                tamanho = 68,
                                versao = vm.personaRetratoVersao,
                                recorte = CircleShape
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            mostrarEditarPerfil = false
                            mostrarDialogoFotoPerfil = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("📷 Trocar foto de perfil", color = Color(0xFFF5C842), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Text("Escolha como os personagens e mestres chamarão você:", color = Color(0xFF8E8E93), fontSize = 13.sp)
                    OutlinedTextField(
                        value = novoNome,
                        onValueChange = { novoNome = it.take(25) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (novoNome.isNotBlank()) {
                            vm.alterarApelido(novoNome)
                            Toast.makeText(contexto, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                        }
                        mostrarEditarPerfil = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C842))
                ) {
                    Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEditarPerfil = false }) {
                    Text("Cancelar", color = Color(0xFF8E8E93))
                }
            },
            containerColor = Color(0xFF161618)
        )
    }

    if (mostrarDialogoFotoPerfil) {
        ModalTrocarFotoPerfil(
            vm = vm,
            onFechar = { mostrarDialogoFotoPerfil = false }
        )
    }



    if (mostrarDialogoCarteira) {
        ModalCarteiraLivenn(
            vm = vm,
            onFechar = { mostrarDialogoCarteira = false },
            onAbrirAssinatura = {
                mostrarDialogoCarteira = false
                mostrarDialogoVip = true
            }
        )
    }



    if (mostrarDialogoVip) {

        AlertDialog(

            onDismissRequest = { mostrarDialogoVip = false },

            title = { Text("Planos & Vantagens LivennIA", color = Color.White, fontWeight = FontWeight.Bold) },

            text = {

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Text("👑 Liven Ultra VIP", fontWeight = FontWeight.Bold, color = Color(0xFFF5C842), fontSize = 14.sp)

                    Text("• Acesso prioritário aos cérebros de IA Gemini 2.5 Pro.", color = Color(0xFF8E8E93), fontSize = 12.sp)

                    Text("• Prosa cinematográfica e diálogos profundos sem limite diário.", color = Color(0xFF8E8E93), fontSize = 12.sp)

                    Text("• 2000 Livens bônus creditados mensalmente.", color = Color(0xFF8E8E93), fontSize = 12.sp)

                    Spacer(Modifier.height(4.dp))

                    Text("✨ Liven Plus", fontWeight = FontWeight.Bold, color = Color(0xFFB0CDE8), fontSize = 14.sp)

                    Text("• Geração de retratos e capas de cenários sem fila.", color = Color(0xFF8E8E93), fontSize = 12.sp)

                    Text("• Bônus diário turbinado.", color = Color(0xFF8E8E93), fontSize = 12.sp)

                }

            },

            confirmButton = {

                TextButton(onClick = { mostrarDialogoVip = false }) {

                    Text("Entendido", color = Color(0xFFF5C842))

                }

            },

            containerColor = Color(0xFF161618)

        )

    }



    if (mostrarDialogoPersona) {
        ModalMinhaPersona(
            vm = vm,
            obrigatorio = vm.personas.isEmpty(),
            onFechar = { mostrarDialogoPersona = false }
        )
    }



    if (mostrarDialogoLaboratorio) {

        AlertDialog(

            onDismissRequest = { mostrarDialogoLaboratorio = false },

            title = { Text("Laboratório de Modelos de IA", color = Color.White, fontWeight = FontWeight.Bold) },

            text = {

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Text("Experimente motores de IA com diferentes diretrizes de escrita:", color = Color(0xFF8E8E93), fontSize = 13.sp)

                    Text("⚡ Liven Flash: Velocidade extrema para respostas ágeis.", color = Color.White, fontSize = 12.sp)

                    Text("✨ Liven Spark: Tom espirituoso e diálogos afiados.", color = Color.White, fontSize = 12.sp)

                    Text("🌹 Liven Velvet: Foco em romance, tensão corporal e emoção.", color = Color.White, fontSize = 12.sp)

                    Text("🍂 Liven Amber: Descrições sensoriais ricas do mundo.", color = Color.White, fontSize = 12.sp)

                    Text("👑 Liven Prime: Mestre narrador de alta inteligência.", color = Color(0xFFF5C842), fontSize = 12.sp)

                }

            },

            confirmButton = {

                TextButton(onClick = { mostrarDialogoLaboratorio = false }) {

                    Text("Explorar Modelos", color = Color(0xFFF5C842))

                }

            },

            containerColor = Color(0xFF161618)

        )

    }



    if (mostrarDialogoDiscord) {

        AlertDialog(

            onDismissRequest = { mostrarDialogoDiscord = false },

            title = { Text("Comunidade Oficial", color = Color.White, fontWeight = FontWeight.Bold) },

            text = {

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Text("Participe da comunidade de autores e mestres de RPG do LivennIA:", color = Color(0xFF8E8E93), fontSize = 13.sp)

                    Surface(

                        color = Color(0xFF202225),

                        shape = RoundedCornerShape(10.dp),

                        modifier = Modifier.fillMaxWidth()

                    ) {

                        Text(

                            text = "💬 Servidor Oficial da Comunidade LivennIA",

                            color = Color(0xFFF5C842),

                            fontWeight = FontWeight.Bold,

                            fontSize = 13.sp,

                            modifier = Modifier.padding(12.dp)

                        )

                    }

                }

            },

            confirmButton = {

                TextButton(onClick = { mostrarDialogoDiscord = false }) {

                    Text("Fechar", color = Color(0xFFF5C842))

                }

            },

            containerColor = Color(0xFF161618)

        )

    }



    if (confirmarSair) {

        DialogoConfirmar(

            titulo = "Sair da conta?",

            texto = "Suas histórias ficam salvas no servidor e você poderá retornar quando quiser.",

            rotuloConfirmar = "Sair",

            onConfirmar = {

                confirmarSair = false

                vm.sair()

            },

            onFechar = { confirmarSair = false }

        )

    }

}



@Composable

private fun MetricaPerfilItem(

    valor: String,

    rotulo: String,

    corValor: Color = Color.White

) {

    Column(

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center

    ) {

        Text(

            text = valor,

            fontSize = 17.sp,

            fontWeight = FontWeight.Bold,

            color = corValor

        )

        Text(

            text = rotulo,

            fontSize = 11.sp,

            color = Color(0xFF8E8E93)

        )

    }

}



@Composable

private fun DivisorVerticalPerfil() {

    Box(

        modifier = Modifier

            .width(1.dp)

            .height(24.dp)

            .background(Color.White.copy(alpha = 0.08f))

    )

}



@Composable
private fun BotaoAcaoRedondoPerfil(
    icone: String,
    rotulo: String,
    aoClicar: () -> Unit
) {
    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btnRoundScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = pressInteraction,
                indication = null
            ) { aoClicar() }
    ) {
        Surface(
            color = Color(0xFF151828),
            shape = CircleShape,
            shadowElevation = 8.dp,
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFF7C4DFF).copy(alpha = 0.8f),
                        Color(0xFF00E5FF).copy(alpha = 0.5f),
                        Color(0xFF1E2238)
                    )
                )
            ),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icone, fontSize = 23.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = rotulo,
            fontSize = 12.sp,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------------------------------------------
// Barra de baixo: uma pílula flutuante, com margens e o "+" elevado no meio
// na cor do tema (separando 🎲 RPG e 📖 Histórias).
// ---------------------------------------------------------------
@Composable
private fun BarraInferior(
    aba: Int,
    onSelecionar: (Int) -> Unit,
    onNova: () -> Unit
) {
    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val plusScale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "plusScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Surface(
            color = Color(0xF50F111D),
            shape = Cantos.grande,
            shadowElevation = 24.dp,
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.8f),
                        Color(0xFF00E5FF).copy(alpha = 0.65f),
                        Color(0xFF8B5CF6).copy(alpha = 0.8f)
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemBarraEmoji("🎲", "RPG", aba == 0) { onSelecionar(0) }
                ItemBarraEmoji("📖", "Histórias", aba == 1) { onSelecionar(1) }
                Spacer(Modifier.width(56.dp))
                ItemBarra(Icons.Filled.Search, "Explorar", aba == 2) { onSelecionar(2) }
                ItemBarra(Icons.Filled.Person, "Perfil", aba == 3) { onSelecionar(3) }
            }
        }

        // O "+" flutua por cima da pílula, no meio e na cor de destaque com animação interativa 3D e efeito de profundidade
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
                .scale(plusScale)
                .size(60.dp)
                .shadow(
                    elevation = if (isPressed) 4.dp else 16.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFE082),
                            Color(0xFFFFB300),
                            Color(0xFFFF8F00)
                        )
                    )
                )
                .border(2.5.dp, Color(0xFFFFF9C4).copy(alpha = 0.95f), CircleShape)
                .clickable(
                    interactionSource = pressInteraction,
                    indication = null
                ) { onNova() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Nova aventura ou história",
                tint = Color(0xFF1B1400),
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun RowScope.ItemBarraEmoji(emoji: String, rotulo: String, ativo: Boolean, onClick: () -> Unit) {
    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else if (ativo) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "emojiScale"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(interactionSource = pressInteraction, indication = null) { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium,
            color = if (ativo) Ouro else TextoSuave.copy(alpha = 0.65f),
            modifier = Modifier.scale(scale)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = if (ativo) Ouro else TextoSuave
        )
    }
}

@Composable
private fun RowScope.ItemBarra(icone: ImageVector, rotulo: String, ativo: Boolean, onClick: () -> Unit) {
    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else if (ativo) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "iconScale"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(interactionSource = pressInteraction, indication = null) { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icone,
            contentDescription = rotulo,
            tint = if (ativo) Ouro else TextoSuave,
            modifier = Modifier
                .size(22.dp)
                .scale(scale)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = if (ativo) Ouro else TextoSuave
        )
    }
}

@Composable
fun ModalTrocarFotoPerfil(
    vm: AppViewModel,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current
    var abaSelecionada by rememberSaveable { mutableStateOf(0) }
    var promptIa by rememberSaveable { mutableStateOf("") }
    var estiloIa by rememberSaveable { mutableStateOf("semi_realistic") }
    var indiceSelecionado by remember { mutableStateOf<Int?>(null) }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bytes = contexto.contentResolver.openInputStream(uri)?.readBytes()
                if (bytes != null && bytes.isNotEmpty()) {
                    val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    vm.uploadFotoPerfil(
                        imagemBase64 = b64,
                        onSucesso = {
                            Toast.makeText(contexto, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show()
                            onFechar()
                        },
                        onError = { err ->
                            Toast.makeText(contexto, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(contexto, "Falha ao carregar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF141416),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF28282D)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Cabeçalho com preview do avatar atual e botão fechar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RetratoPersona(
                            temRetrato = vm.personaTemRetrato,
                            emojiOuPadrao = vm.personaAvatar.ifBlank { "👤" },
                            tamanho = 54,
                            versao = vm.personaRetratoVersao,
                            recorte = CircleShape
                        )
                        Column {
                            Text(
                                text = "Foto de Perfil",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Personalize como você aparece no app",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                    IconButton(onClick = onFechar) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color(0xFF9E9E9E))
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Edição de Apelido / Nome do Perfil
                var novoApelido by remember { mutableStateOf(vm.apelido) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = novoApelido,
                        onValueChange = { novoApelido = it.take(25) },
                        label = { Text("Seu Apelido / Nome", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    if (novoApelido.isNotBlank() && novoApelido != vm.apelido) {
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                vm.alterarApelido(novoApelido)
                                Toast.makeText(contexto, "Nome atualizado!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C842)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Abas de navegação
                val abas = listOf("📸 Galeria", "✨ Gerar com IA", "🎭 Personas", "😊 Emojis")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    abas.forEachIndexed { index, titulo ->
                        val ativo = abaSelecionada == index
                        Surface(
                            color = if (ativo) Color(0xFFF5C842) else Color(0xFF1E1E22),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, if (ativo) Color(0xFFF5C842) else Color(0xFF323238)),
                            modifier = Modifier.clickable {
                                abaSelecionada = index
                                indiceSelecionado = null
                            }
                        ) {
                            Text(
                                text = titulo,
                                color = if (ativo) Color.Black else Color.White,
                                fontWeight = if (ativo) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Conteúdo da Aba Selecionada
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (abaSelecionada) {
                        0 -> {
                            // ABA GALERIA
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    color = Color(0xFF1C1C20),
                                    shape = CircleShape,
                                    border = BorderStroke(2.dp, Color(0xFFF5C842)),
                                    modifier = Modifier.size(110.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        RetratoPersona(
                                            temRetrato = vm.personaTemRetrato,
                                            emojiOuPadrao = vm.personaAvatar.ifBlank { "👤" },
                                            tamanho = 106,
                                            versao = vm.personaRetratoVersao,
                                            recorte = CircleShape
                                        )
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    text = "Sua Foto do Dispositivo",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Escolha qualquer foto ou imagem da sua galeria para usar como seu retrato oficial.",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA0A0A5),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(Modifier.height(28.dp))
                                Button(
                                    onClick = { launcherGaleria.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C842)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(50.dp)
                                ) {
                                    Text(
                                        text = "📁 Escolher da Galeria",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                        1 -> {
                            // ABA GERAR COM IA
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Descreva como você quer seu retrato:",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA0A0A5),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = promptIa,
                                    onValueChange = { promptIa = it.take(200) },
                                    placeholder = {
                                        Text(
                                            "Ex: Guerreiro cósmico com olhos brilhantes e armadura dourada...",
                                            fontSize = 13.sp,
                                            color = Color(0xFF6E6E73)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = "Estilo Artístico:",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA0A0A5),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ESTILOS_IMAGEM_PERSONA.forEach { estilo ->
                                        val sel = estilo.id == estiloIa
                                        Surface(
                                            color = if (sel) Color(0xFF2C2410) else Color(0xFF1E1E22),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, if (sel) Color(0xFFF5C842) else Color(0xFF323238)),
                                            modifier = Modifier.clickable { estiloIa = estilo.id }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            ) {
                                                Text(estilo.icone, fontSize = 14.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    estilo.nome,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (sel) Color(0xFFF5C842) else Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Botão Gerar / Candidatos
                                if (vm.candidatosRetrato.isEmpty()) {
                                    Button(
                                        onClick = {
                                            val req = PersonaRetratoRequest(
                                                nome = vm.apelido.ifBlank { "Viajante" },
                                                genero = vm.personaGenero.ifBlank { "Não informado" },
                                                descricao = promptIa.ifBlank { "Retrato estiloso de ${vm.apelido}" },
                                                aparencia = promptIa.ifBlank { "Herói lendário confiante" },
                                                detalhesImagem = promptIa,
                                                estiloImagem = estiloIa
                                            )
                                            vm.gerarCandidatosRetrato(
                                                req = req,
                                                onSucesso = {},
                                                onError = { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
                                            )
                                        },
                                        enabled = !vm.gerandoCandidatos && !vm.ocupado,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C842)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        if (vm.gerandoCandidatos) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Gerando Imagem com IA...", color = Color.Black, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("✨ Gerar Imagem com IA", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    // Retrato único gerado pela IA
                                    Text(
                                        text = "Retrato gerado pela IA:",
                                        fontSize = 13.sp,
                                        color = Color(0xFFF5C842),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(10.dp))

                                    val cand0 = vm.candidatosRetrato.firstOrNull()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CardCandidatoQuadrado(
                                            candidato = cand0,
                                            indice = 0,
                                            loteId = vm.loteCandidatosId,
                                            carregando = vm.gerandoCandidatos,
                                            progresso = 100,
                                            ehSelecionado = true,
                                            onSelecionar = { indiceSelecionado = 0 },
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .aspectRatio(0.82f)
                                        )
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                indiceSelecionado = 0
                                                val req = PersonaRetratoRequest(
                                                    nome = vm.apelido.ifBlank { "Viajante" },
                                                    genero = vm.personaGenero.ifBlank { "Não informado" },
                                                    descricao = promptIa.ifBlank { "Retrato estiloso de ${vm.apelido}" },
                                                    aparencia = promptIa.ifBlank { "Herói lendário confiante" },
                                                    detalhesImagem = promptIa,
                                                    estiloImagem = estiloIa
                                                )
                                                vm.gerarCandidatosRetrato(
                                                    req = req,
                                                    onSucesso = { indiceSelecionado = 0 },
                                                    onError = { Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show() }
                                                )
                                            },
                                            enabled = !vm.gerandoCandidatos && !vm.ocupado,
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Gerar outra", color = Color.White, fontSize = 13.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val idx = indiceSelecionado ?: 0
                                                vm.confirmarFotoPerfil(
                                                    loteId = vm.loteCandidatosId,
                                                    indice = idx,
                                                    onSucesso = {
                                                        Toast.makeText(contexto, "Foto de perfil atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                                        onFechar()
                                                    },
                                                    onError = { err ->
                                                        Toast.makeText(contexto, err, Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            },
                                            enabled = cand0 != null && !vm.gerandoCandidatos && !vm.ocupado,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C842)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1.3f).height(48.dp)
                                        ) {
                                            Text("Usar Esta Foto", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // ABA PERSONAS
                            if (vm.personas.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Nenhuma persona criada ainda", color = Color(0xFFA0A0A5), fontSize = 14.sp)
                                    Spacer(Modifier.height(10.dp))
                                    Text("Crie uma persona na aba Personagens para usar o retrato dela.", color = Color(0xFF6E6E73), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(vm.personas) { p ->
                                        val ehPadrao = p.padrao
                                        Surface(
                                            color = if (ehPadrao) Color(0xFF2C2410) else Color(0xFF1C1C20),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, if (ehPadrao) Color(0xFFF5C842) else Color(0xFF28282D)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    vm.definirPersonaPadrao(p.id) {
                                                        Toast.makeText(contexto, "Foto de perfil atualizada com a persona ${p.nome}!", Toast.LENGTH_SHORT).show()
                                                        onFechar()
                                                    }
                                                }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(12.dp)
                                            ) {
                                                RetratoPersona(
                                                    temRetrato = p.temRetrato,
                                                    emojiOuPadrao = p.genero.ifBlank { "👤" },
                                                    tamanho = 48,
                                                    versao = p.versaoRetrato,
                                                    personaId = p.id,
                                                    recorte = CircleShape
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(p.nome, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                                        if (ehPadrao) {
                                                            Spacer(Modifier.width(6.dp))
                                                            Surface(
                                                                color = Color(0xFFF5C842),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text("Ativa", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                            }
                                                        }
                                                    }
                                                    Text(p.descricao.ifBlank { p.aparencia.ifBlank { "Sem descrição" } }, color = Color(0xFF8E8E93), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                Button(
                                                    onClick = {
                                                        vm.definirPersonaPadrao(p.id) {
                                                            Toast.makeText(contexto, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show()
                                                            onFechar()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = if (ehPadrao) Color(0xFF333338) else Color(0xFFF5C842)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(34.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                                ) {
                                                    Text(
                                                        if (ehPadrao) "Em Uso" else "Usar",
                                                        color = if (ehPadrao) Color(0xFFA0A0A5) else Color.Black,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // ABA EMOJIS & ÍCONES
                            val emojis = listOf(
                                "👤", "🧙‍♂️", "🧝‍♀️", "🥷", "🦸‍♂️", "🤖", "👑", "🦊",
                                "🐉", "⚔️", "🎭", "🌟", "🚀", "💀", "🐺", "🦁",
                                "🔮", "⚡", "🌙", "🔥", "🐱", "🦅", "💎", "🛡️"
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(emojis) { emoji ->
                                    val sel = vm.personaAvatar == emoji && !vm.personaTemRetrato
                                    Surface(
                                        color = if (sel) Color(0xFF2C2410) else Color(0xFF1E1E22),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) Color(0xFFF5C842) else Color(0xFF2D2D33)),
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clickable {
                                                vm.definirAvatarEmoji(emoji)
                                                Toast.makeText(contexto, "Avatar atualizado!", Toast.LENGTH_SHORT).show()
                                                onFechar()
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Text(emoji, fontSize = 28.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

