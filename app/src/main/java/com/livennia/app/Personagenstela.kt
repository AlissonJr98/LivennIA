package com.livennia.app

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo RPG", style = MaterialTheme.typography.headlineMedium)
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo História", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Livros interativos e roleplay livre com a IA, sem barra de vida.",
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Column {
                Text("Explorar", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Descubra personagens e mundos prontos ou crie sua própria história.",
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Caminho livre: dois botões neutros (sem parecer que um está selecionado).
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SuperficieAlta,
                    shape = Cantos.cartao,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !vm.ocupado) { vm.comecarAventuraLivre("rpg") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🎲 Criar RPG", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Com vida, itens e escolhas.",
                            color = TextoSuave,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Surface(
                    color = SuperficieAlta,
                    shape = Cantos.cartao,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !vm.ocupado) { vm.comecarAventuraLivre("historia") }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("📖 Criar História", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Livro vivo e conversa livre.",
                            color = TextoSuave,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Barra de busca + filtros horizontais por estilo de história.
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it.take(60) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextoSuave
                        )
                    },
                    trailingIcon = {
                        if (busca.isNotEmpty()) {
                            IconButton(onClick = { busca = "" }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Limpar busca",
                                    tint = TextoSuave
                                )
                            }
                        }
                    },
                    placeholder = { Text("Buscar personagem, tema ou tag...") },
                    singleLine = true,
                    shape = Cantos.pilula,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TituloSecao(
                    if (estiloFiltro == "Todos") "Personagens & Mundos (${catalogoFiltrado.size})"
                    else "$estiloFiltro (${catalogoFiltrado.size})"
                )
                if (estiloFiltro != "Todos" || busca.isNotBlank()) {
                    TextButton(onClick = { estiloFiltro = "Todos"; busca = "" }) {
                        Text("Ver todos", style = MaterialTheme.typography.labelSmall)
                    }
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

// Um card da vitrine: arte full-bleed, selo do estilo no topo e texto em degradê.
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
            .aspectRatio(0.66f)
            .clip(Cantos.imagem)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), Cantos.imagem)
            .clickable(enabled = habilitado) { onAbrir() }
    ) {
        CapaCenario(
            cenarioId = cenario.id,
            temCapa = cenario.capa,
            emoji = cenario.emoji,
            nome = cenario.nome,
            versao = versao,
            modifier = Modifier.fillMaxSize(),
            recorte = Cantos.imagem
        )

        // Selo do estilo de história no canto superior esquerdo.
        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            shape = Cantos.pilula,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(
                text = cenario.genero,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // Sem capa ainda: atalho discreto para gerar a arte (uma única vez).
        if (!cenario.capa) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = Cantos.pilula,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable(enabled = habilitado && !gerando) { onGerarCapa() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (gerando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(11.dp),
                            strokeWidth = 2.dp,
                            color = Ouro
                        )
                    } else {
                        Text("✨", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(3.dp))
                    Text(
                        if (gerando) "..." else "capa",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Degradê escuro de baixo para cima: deixa o texto legível sobre a arte.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.74f)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.38f to Color.Black.copy(alpha = 0.60f),
                        1f to Color.Black.copy(alpha = 0.95f)
                    )
                )
        )

        // Nome, subtítulo, sinopse e tags por cima da arte.
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "${cenario.emoji} ${cenario.nome}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (cenario.subtitulo.isNotBlank()) {
                Text(
                    cenario.subtitulo,
                    color = Ouro,
                    style = MaterialTheme.typography.labelSmall,
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
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    cenario.tags.take(3).forEach { tag -> ChipSobreArte(tag) }
                }
            }
        }
    }
}

// Modal detalhado ao tocar em um personagem da vitrine: mostra capa, tags,
// sinopse completa, a Cena de Abertura e os botões para começar em História ou RPG.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DialogoPreviaCenario(
    cenario: CenarioDto,
    versaoCapa: Int,
    onComecarHistoria: () -> Unit,
    onComecarRpg: () -> Unit,
    onFechar: () -> Unit
) {
    Dialog(onDismissRequest = onFechar) {
        Surface(
            color = SuperficieAlta,
            shape = Cantos.cartao,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CapaCenario(
                        cenarioId = cenario.id,
                        temCapa = cenario.capa,
                        emoji = cenario.emoji,
                        nome = cenario.nome,
                        versao = versaoCapa,
                        modifier = Modifier.size(84.dp),
                        recorte = Cantos.campo
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        ChipTag(cenario.genero, destaque = true)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${cenario.emoji} ${cenario.nome}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (cenario.subtitulo.isNotBlank()) {
                            Text(
                                cenario.subtitulo,
                                color = Ouro,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                if (cenario.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cenario.tags.forEach { tag -> ChipTag(tag) }
                    }
                }

                Text(
                    cenario.sinopse,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.92f)
                )

                if (cenario.abertura.isNotBlank()) {
                    Surface(
                        color = Fundo.copy(alpha = 0.65f),
                        shape = Cantos.campo,
                        border = BorderStroke(1.dp, Ouro.copy(alpha = 0.22f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "✦ CENA DE ABERTURA",
                                color = Ouro,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "“${cenario.abertura}”",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = FontStyle.Italic
                                ),
                                color = TextoSuave
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onComecarHistoria,
                        modifier = Modifier.fillMaxWidth(),
                        shape = Cantos.campo
                    ) {
                        Text("📖 Conversar no Modo História")
                    }
                    OutlinedButton(
                        onClick = onComecarRpg,
                        modifier = Modifier.fillMaxWidth(),
                        shape = Cantos.campo,
                        border = BorderStroke(1.dp, Ouro.copy(alpha = 0.55f))
                    ) {
                        Text("🎲 Jogar no Modo RPG (Vida & Escolhas)", color = Ouro)
                    }
                    TextButton(
                        onClick = onFechar,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Fechar", color = TextoSuave)
                    }
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
    var confirmarApagarConta by rememberSaveable { mutableStateOf(false) }
    var confirmarSair by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text("Perfil", style = MaterialTheme.typography.headlineMedium)
                Text("Sua conta e preferências.", color = TextoSuave, style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Surface(
                color = SuperficieAlta,
                shape = Cantos.cartao,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AvatarPersonagem(seed = vm.apelido.hashCode(), nome = vm.apelido, tamanho = 52)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(vm.apelido, style = MaterialTheme.typography.titleMedium)
                        Text("Servidor: ${vm.servidor}", color = TextoSuave, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        vm.conta?.let { c ->
            item {
                Surface(
                    color = SuperficieAlta,
                    shape = Cantos.cartao,
                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.28f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                TituloSecao("Carteira de Livens")
                                Text(
                                    "✨ ${c.livens} Livens",
                                    color = Ouro,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                            if (c.resgateDiarioDisponivel) {
                                Button(
                                    onClick = { vm.resgatarLivensDiarios() },
                                    enabled = !vm.ocupado,
                                    shape = Cantos.pilula
                                ) {
                                    Text("🎁 Resgatar +30")
                                }
                            } else {
                                ChipTag("✓ Bônus de hoje resgatado")
                            }
                        }
                        Text(
                            "Use seus Livens para conversar com motores especiais de IA (Liven Spark, Liven Pulse, Liven Velvet, Liven Amber, Liven Eclipse, Liven Prime, Liven Oracle e Liven Infinity).",
                            color = TextoSuave,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            item {
                Surface(
                    color = SuperficieAlta,
                    shape = Cantos.cartao,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TituloSecao("Uso de hoje")
                        Text(
                            "Jogadas: ${c.jogadasHoje} de ${c.limiteDiario} (restam ${c.jogadasRestantesHoje})",
                            color = TextoSuave,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Personagens ativos: ${c.jogosAtivos} de ${c.maxJogos}",
                            color = TextoSuave,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            CardAtualizacaoGitHub(vm)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { vm.navegarPara(Tela.Configuracoes) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Configurações")
                }
                // O interruptor vale nos dois estilos (o Neon claro entrou junto).
                OutlinedButton(
                    onClick = { vm.alternarTema() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (vm.temaEscuro) "☀  Modo claro" else "🌙  Modo escuro")
                }
                OutlinedButton(
                    onClick = { confirmarApagarConta = true },
                    enabled = !vm.ocupado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apagar conta", color = MaterialTheme.colorScheme.error)
                }
                OutlinedButton(
                    onClick = { confirmarSair = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sair")
                }
            }
        }
    }

    if (confirmarApagarConta) {
        DialogoConfirmar(
            titulo = "Apagar sua conta?",
            texto = "Sua conta e todos os seus personagens serão apagados para sempre. " +
                    "Você precisará de um novo convite para voltar.",
            rotuloConfirmar = "Apagar tudo",
            onConfirmar = {
                confirmarApagarConta = false
                vm.apagarConta()
            },
            onFechar = { confirmarApagarConta = false }
        )
    }

    if (confirmarSair) {
        DialogoConfirmar(
            titulo = "Sair da conta?",
            texto = "Você vai sair e suas aventuras ficam guardadas no servidor. " +
                    "Para voltar depois vai ser preciso um código de convite novo " +
                    "(a entrada por e-mail ainda não está ativa).",
            rotuloConfirmar = "Sair",
            onConfirmar = {
                confirmarSair = false
                vm.sair()
            },
            onFechar = { confirmarSair = false }
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
            .padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 10.dp)
    ) {
        Surface(
            color = SuperficieAlta,
            shape = Cantos.grande,
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth().height(66.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemBarraEmoji("🎲", "RPG", aba == 0) { onSelecionar(0) }
                ItemBarraEmoji("📖", "Histórias", aba == 1) { onSelecionar(1) }
                Spacer(Modifier.width(62.dp))
                ItemBarra(Icons.Filled.Search, "Explorar", aba == 2) { onSelecionar(2) }
                ItemBarra(Icons.Filled.Person, "Perfil", aba == 3) { onSelecionar(3) }
            }
        }

        // O "+" flutua por cima da pílula, no meio e na cor de destaque.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
                .size(62.dp)
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
