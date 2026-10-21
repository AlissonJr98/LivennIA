package com.livennia.app

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        // O topo e as laterais respeitam a barra de status; a barra de baixo cuida
        // sozinha do espaço da barra de navegação do aparelho.
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        bottomBar = {
            BarraInferior(
                aba = vm.abaPrincipal,
                onSelecionar = { vm.selecionarAba(it) },
                onNova = { vm.comecarAventuraLivre() }
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
                    2 -> AbaExplorar(vm)
                    3 -> AbaPerfil(vm)
                    else -> AbaAventuras(vm)
                }
            }
        }
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
    Surface(
        color = SuperficieAlta,
        shape = Cantos.cartao,
        border = androidx.compose.foundation.BorderStroke(1.dp, Ouro.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth().clickable(enabled = habilitado) { onAbrir() }
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
    Surface(
        color = SuperficieAlta,
        shape = Cantos.cartao,
        modifier = Modifier.fillMaxWidth().clickable(enabled = habilitado) { onAbrir() }
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
// Aba "Explorar": a vitrine de cenários e personagens oficiais do LivennIA.
// O "prompt secreto" de cada cenário fica só no servidor: tocar no card abre a
// prévia da história e permite começar em Modo História ou Modo RPG.
// ---------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AbaExplorar(vm: AppViewModel) {
    LaunchedEffect(Unit) { vm.carregarCatalogo() }

    var busca by rememberSaveable { mutableStateOf("") }
    var estiloFiltro by rememberSaveable { mutableStateOf("Todos") }
    var cenarioPrevia by remember { mutableStateOf<CenarioDto?>(null) }

    // Lista de estilos disponíveis para filtrar na barra horizontal.
    val estilosDisponiveis = remember(vm.catalogo, vm.generos) {
        val nomes = linkedSetOf("Todos")
        vm.generos.forEach { nomes.add(it.nome) }
        vm.catalogo.forEach { if (it.genero.isNotBlank()) nomes.add(it.genero) }
        nomes.toList()
    }

    val catalogoFiltrado = remember(vm.catalogo, busca, estiloFiltro) {
        val termo = busca.trim().lowercase()
        vm.catalogo.filter { c ->
            val bateEstilo = estiloFiltro == "Todos" || c.genero.equals(estiloFiltro, ignoreCase = true)
            val bateBusca = termo.isEmpty() ||
                c.nome.lowercase().contains(termo) ||
                c.subtitulo.lowercase().contains(termo) ||
                c.sinopse.lowercase().contains(termo) ||
                c.genero.lowercase().contains(termo) ||
                c.tags.any { it.lowercase().contains(termo) }
            bateEstilo && bateBusca
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Explorar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Descubra personagens e mundos prontos ou crie o seu.",
                    color = TextoSuave,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Barra de busca + filtros horizontais compactos
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it.take(60) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextoSuave,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (busca.isNotEmpty()) {
                            IconButton(onClick = { busca = "" }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Limpar busca",
                                    tint = TextoSuave,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    placeholder = { Text("Buscar personagem, tema ou tag...", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    shape = Cantos.pilula,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    estilosDisponiveis.forEach { estilo ->
                        val ativo = estilo == estiloFiltro
                        val emoji = when (estilo) {
                            "Todos" -> "🔥"
                            else -> vm.generos.find { it.nome == estilo }?.emoji ?: "✦"
                        }
                        Surface(
                            color = if (ativo) Ouro.copy(alpha = 0.20f) else SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(
                                1.dp,
                                if (ativo) Ouro else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.clickable { estiloFiltro = estilo }
                        ) {
                            Text(
                                text = "$emoji $estilo",
                                color = if (ativo) Ouro else TextoSuave,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (ativo) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Ações rápidas compactas: Criar RPG / Criar História
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = SuperficieAlta,
                    shape = Cantos.pilula,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !vm.ocupado) { vm.comecarAventuraLivre("rpg") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🎲", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(6.dp))
                        Text("Criar RPG", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    }
                }
                Surface(
                    color = SuperficieAlta,
                    shape = Cantos.pilula,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !vm.ocupado) { vm.comecarAventuraLivre("historia") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("📖", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(6.dp))
                        Text("Criar História", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
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
                    if (estiloFiltro == "Todos") "Personagens & Mundos (${catalogoFiltrado.size})"
                    else "$estiloFiltro (${catalogoFiltrado.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (estiloFiltro != "Todos" || busca.isNotBlank()) {
                    Text(
                        "Ver todos",
                        color = Ouro,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable { estiloFiltro = "Todos"; busca = "" }
                    )
                }
            }
        }

        if (vm.catalogo.isEmpty()) {
            if (vm.erroCatalogo != null) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            vm.erroCatalogo ?: "",
                            color = TextoSuave,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = { vm.carregarCatalogo(forcar = true) }) {
                            Text("Tentar de novo")
                        }
                    }
                }
            } else {
                items(6) { ShimmerBox(modifier = Modifier.fillMaxWidth().aspectRatio(0.66f)) }
            }
        } else if (catalogoFiltrado.isEmpty()) {
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
                        Text("Nenhum personagem encontrado para esse filtro.", color = TextoSuave)
                        Spacer(Modifier.height(6.dp))
                        TextButton(onClick = { busca = ""; estiloFiltro = "Todos" }) {
                            Text("Limpar filtros")
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
                onAbrir = { cenarioPrevia = cenario },
                onGerarCapa = { vm.gerarCapaDoCenario(cenario.id) }
            )
        }
    }

    cenarioPrevia?.let { cenario ->
        DialogoPreviaCenario(
            cenario = cenario,
            versaoCapa = vm.capaVersao,
            onComecarHistoria = {
                cenarioPrevia = null
                vm.escolherCenario(cenario, "historia")
            },
            onComecarRpg = {
                cenarioPrevia = null
                vm.escolherCenario(cenario, "rpg")
            },
            onFechar = { cenarioPrevia = null }
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
    onGerarCapa: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(16.dp))
            .clickable(enabled = habilitado) { onAbrir() }
    ) {
        CapaCenario(
            cenarioId = cenario.id,
            temCapa = cenario.capa,
            emoji = cenario.emoji,
            nome = cenario.nome,
            versao = versao,
            modifier = Modifier.fillMaxSize(),
            recorte = RoundedCornerShape(16.dp)
        )

        // Selo do gênero no canto superior esquerdo
        Surface(
            color = Color.Black.copy(alpha = 0.60f),
            shape = Cantos.pilula,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(
                text = cenario.genero,
                color = Color.White.copy(alpha = 0.95f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // Badge flutuante de engajamento no canto superior direito
        Surface(
            color = Color.Black.copy(alpha = 0.60f),
            shape = Cantos.pilula,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(3.dp))
                val contagem = ((cenario.id.hashCode().absoluteValue % 900) + 120) / 100f
                Text(
                    text = String.format(java.util.Locale.US, "%.1fk", contagem),
                    color = Ouro,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Degradê escuro de baixo para cima
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.32f to Color.Black.copy(alpha = 0.65f),
                        1f to Color.Black.copy(alpha = 0.96f)
                    )
                )
        )

        // Nome, subtítulo, sinopse e tags
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                "${cenario.emoji} ${cenario.nome}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (cenario.subtitulo.isNotBlank()) {
                Text(
                    cenario.subtitulo,
                    color = Ouro,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                cenario.sinopse,
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (cenario.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    cenario.tags.take(2).forEach { tag -> ChipSobreArte(tag) }
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
                        emoji = cenario.emoji,
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
                        ChipTag(cenario.genero, destaque = true)
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = Cantos.pilula
                        ) {
                            val contagem = ((cenario.id.hashCode().absoluteValue % 900) + 120) / 100f
                            Text(
                                text = "🔥 " + String.format(java.util.Locale.US, "%.1fk", contagem) + " histórias",
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
                            "${cenario.emoji} ${cenario.nome}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (cenario.subtitulo.isNotBlank()) {
                            Text(
                                cenario.subtitulo,
                                color = Ouro,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Primeira Mensagem / Cena de Abertura (Estilo Balão do Personagem)
                    if (cenario.abertura.isNotBlank()) {
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
                                    "“${cenario.abertura}”",
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
                            cenario.sinopse,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.90f),
                            lineHeight = 20.sp
                        )
                    }

                    // Tags
                    if (cenario.tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cenario.tags.forEach { tag -> ChipTag(tag) }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Botões de Ação estilo Emochi / Talkie
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

                // Ícones de ação no topo direito (Matching Imagem 2 do Emochi com assinatura LivennIA)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícone Discord
                    IconButton(
                        onClick = { mostrarDialogoDiscord = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Surface(
                            color = Color(0xFF161618),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFF26262A)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💬", fontSize = 16.sp)
                            }
                        }
                    }

                    // ÍCONE ESTRELA: FÓRUM INTERATIVO & FEEDBACK DA COMUNIDADE (Solicitação do usuário)
                    IconButton(
                        onClick = { vm.navegarPara(Tela.Feedback) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Surface(
                            color = Color(0xFF161618),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFFF5C842).copy(alpha = 0.5f)),
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
                        modifier = Modifier.size(38.dp)
                    ) {
                        Surface(
                            color = Color(0xFF161618),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFF26262A)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configurações",
                                    tint = Color.White,
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
                    modifier = Modifier.clickable { mostrarEditarPerfil = true }
                ) {
                    AvatarPersonagem(
                        seed = vm.apelido.hashCode(),
                        nome = vm.apelido,
                        tamanho = 72
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
                    Text(
                        text = vm.apelido,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                            modifier = Modifier.clickable { mostrarEditarPerfil = true }
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
                    MetricaPerfilItem(valor = (vm.conta?.livens ?: 0).toString(), rotulo = "Livens", corValor = Color(0xFFF5C842))
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
                    // Ilustração da Caixa Surpresa 3D (Matching Imagem 2 do Emochi com assinatura LivennIA)
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
            title = { Text("Editar Apelido", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

    if (mostrarDialogoCarteira) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCarteira = false },
            title = { Text("Carteira de Livens", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Saldo atual: ✨ ${vm.conta?.livens ?: 0} Livens",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5C842)
                    )
                    Text(
                        text = "Os Livens são usados para turnos narrativos nos modelos especiais como Liven Spark, Velvet, Amber e Eclipse.",
                        color = Color(0xFF8E8E93),
                        fontSize = 13.sp
                    )
                    if (vm.conta?.resgateDiarioDisponivel == true) {
                        Button(
                            onClick = {
                                vm.resgatarLivensDiarios()
                                mostrarDialogoCarteira = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C842)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🎁 Resgatar Bônus Diário (+30)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "✓ Bônus diário de hoje já resgatado! Volte amanhã para mais.",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoCarteira = false }) {
                    Text("Fechar", color = Color(0xFFF5C842))
                }
            },
            containerColor = Color(0xFF161618)
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
        AlertDialog(
            onDismissRequest = { mostrarDialogoPersona = false },
            title = { Text("Persona do Jogador", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Personalize como os personagens de IA percebem seu protagonista em todas as histórias:", color = Color(0xFF8E8E93), fontSize = 13.sp)
                    Text("• Nome Ativo: ${vm.apelido}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Estilo de Narrativa Padrão: ${if (vm.modoLeitura == AppViewModel.ModoLeitura.HISTORIA) "História Contínua" else "RPG Dinâmico"}", color = Color.White, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoPersona = false }) {
                    Text("OK", color = Color(0xFFF5C842))
                }
            },
            containerColor = Color(0xFF161618)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { aoClicar() }
    ) {
        Surface(
            color = Color(0xFF161618),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color(0xFF26262A)),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icone, fontSize = 22.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = rotulo,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Surface(
            color = SuperficieAlta,
            shape = Cantos.grande,
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth().height(60.dp)
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

        // O "+" flutua por cima da pílula, no meio e na cor de destaque.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Ouro)
                .clickable { onNova() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Nova aventura ou história",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun RowScope.ItemBarraEmoji(emoji: String, rotulo: String, ativo: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium,
            color = if (ativo) Ouro else TextoSuave.copy(alpha = 0.65f)
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
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icone,
            contentDescription = rotulo,
            tint = if (ativo) Ouro else TextoSuave,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = if (ativo) Ouro else TextoSuave
        )
    }
}
