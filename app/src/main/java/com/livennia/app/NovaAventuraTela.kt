package com.livennia.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Tela de criação do personagem / nova aventura com espaçamento consistente e moderno
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NovaAventuraTela(vm: AppViewModel) {
    var nome by rememberSaveable { mutableStateOf(vm.personaNome.ifBlank { "" }) }
    val ativos = vm.jogos.count { !it.encerrado }
    val limiteAtivos = vm.conta?.maxJogos ?: 3
    val podeCriar = !vm.ocupado && ativos < limiteAtivos

    BackHandler { vm.navegarPara(Tela.Personagens) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // ---- Top Bar Simétrica ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.navegarPara(Tela.Personagens) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (vm.modoCriacao == "historia") "Nova História" else "Nova Aventura RPG",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = Ouro.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (vm.modoCriacao == "historia") "📖" else "🎲", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---- Seletor de Modo: RPG vs História ----
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TituloSecao("Modo de Experiência")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val ehRpg = vm.modoCriacao != "historia"
                        Surface(
                            color = if (ehRpg) Ouro.copy(alpha = 0.16f) else SuperficieAlta,
                            shape = Cantos.cartao,
                            border = if (ehRpg) BorderStroke(2.dp, Ouro) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vm.selecionarModoCriacao("rpg") }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "🎲 Modo RPG",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ehRpg) Ouro else MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Vida, inventário, ações táticas e perigo real.",
                                    color = TextoSuave,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        val ehHistoria = vm.modoCriacao == "historia"
                        Surface(
                            color = if (ehHistoria) Ouro.copy(alpha = 0.16f) else SuperficieAlta,
                            shape = Cantos.cartao,
                            border = if (ehHistoria) BorderStroke(2.dp, Ouro) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vm.selecionarModoCriacao("historia") }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "📖 Modo História",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ehHistoria) Ouro else MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Narrativa imersiva e livre, sem barra de vida.",
                                    color = TextoSuave,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                // ---- Cenário Oficial (se selecionado) ----
                vm.cenarioSelecionado?.let { cenario ->
                    Surface(
                        color = SuperficieAlta,
                        shape = Cantos.cartao,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CapaCenario(
                                cenarioId = cenario.id,
                                temCapa = cenario.capa,
                                emoji = cenario.emoji,
                                nome = cenario.nome,
                                versao = vm.capaVersao,
                                modifier = Modifier.size(72.dp),
                                recorte = Cantos.campo
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${cenario.emoji} ${cenario.nome}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (cenario.subtitulo.isNotBlank()) {
                                    Text(
                                        cenario.subtitulo,
                                        color = TextoSuave,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    cenario.sinopse,
                                    color = TextoSuave,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ---- Protagonista / Nome + Avatar Harmônico ----
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TituloSecao("Protagonista da História")
                    Surface(
                        color = SuperficieAlta,
                        shape = Cantos.cartao,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.BottomCenter) {
                                AvatarPersonagem(
                                    seed = (nome.ifBlank { vm.personaNome }.ifBlank { "Viajante" }).hashCode(),
                                    nome = nome.ifBlank { vm.personaNome }.ifBlank { "Viajante" },
                                    tamanho = 72
                                )
                                Surface(
                                    color = Ouro,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        "✦ IA",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = nome,
                                    onValueChange = { nome = it.take(30) },
                                    shape = Cantos.campo,
                                    label = { Text("Nome do personagem") },
                                    placeholder = { Text(vm.personaNome.ifBlank { "Ex.: Viajante, Kael..." }) },
                                    singleLine = true,
                                    supportingText = { Text("${nome.length}/30") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // ---- Estilo / Gênero ----
                if (vm.cenarioSelecionado == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TituloSecao("Estilo & Gênero")
                        if (vm.generos.isEmpty()) {
                            Text("Carregando estilos...", color = TextoSuave, style = MaterialTheme.typography.labelSmall)
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                vm.generos.forEach { g ->
                                    val selecionado = g.nome == vm.generoSelecionado
                                    FilterChip(
                                        selected = selecionado,
                                        onClick = { vm.selecionarGenero(g.nome) },
                                        label = { Text("${g.emoji} ${g.nome}") },
                                        shape = Cantos.pilula,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Ouro.copy(alpha = 0.22f),
                                            selectedLabelColor = Ouro
                                        )
                                    )
                                }
                            }
                            vm.generos.find { it.nome == vm.generoSelecionado }?.let { g ->
                                Text(g.descricao, color = TextoSuave, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // ---- Ideia / Cena Inicial ----
                val ideiasDoEstilo = vm.generos
                    .find { it.nome == vm.generoSelecionado }
                    ?.ideias
                    ?.takeIf { it.isNotEmpty() }
                    ?: IDEIAS_RAPIDAS_PADRAO

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TituloSecao("Premissa Inicial (Opcional)")
                        TextButton(
                            onClick = {
                                val outras = ideiasDoEstilo.filter { it != vm.premissa }
                                val escolhida = (outras.ifEmpty { ideiasDoEstilo }).randomOrNull()
                                if (escolhida != null) vm.alterarPremissa(escolhida)
                            }
                        ) {
                            Text("🎲 Sortear", color = Ouro, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    OutlinedTextField(
                        value = vm.premissa,
                        onValueChange = { vm.alterarPremissa(it) },
                        shape = Cantos.campo,
                        placeholder = { Text("Toque em uma ideia ou digite como quer começar...") },
                        minLines = 2,
                        maxLines = 3,
                        supportingText = { Text("${vm.premissa.length}/200") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ideiasDoEstilo.take(3).forEach { ideia ->
                            val ativa = vm.premissa == ideia
                            Surface(
                                color = if (ativa) Ouro.copy(alpha = 0.20f) else SuperficieAlta,
                                shape = Cantos.pilula,
                                border = if (ativa) BorderStroke(1.dp, Ouro) else null,
                                modifier = Modifier.clickable { vm.alterarPremissa(ideia) }
                            ) {
                                Text(
                                    text = "✦ ${ideia.take(38)}...",
                                    color = if (ativa) Ouro else TextoSuave,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // ---- Ficha da Persona (Opcional) ----
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TituloSecao("Personalidade & Ficha (Opcional)")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ARQUETIPOS_PERSONA.forEach { arquetipo ->
                            val selecionado = vm.descricao.contains(arquetipo, ignoreCase = true)
                            Surface(
                                color = if (selecionado) Ouro.copy(alpha = 0.20f) else SuperficieAlta,
                                shape = Cantos.pilula,
                                border = if (selecionado) BorderStroke(1.dp, Ouro) else null,
                                modifier = Modifier.clickable {
                                    val atual = vm.descricao.trim()
                                    val novo = when {
                                        selecionado -> atual
                                        atual.isEmpty() -> arquetipo
                                        else -> "$atual, ${arquetipo.lowercase()}"
                                    }
                                    vm.alterarDescricao(novo)
                                }
                            ) {
                                Text(
                                    text = "+ $arquetipo",
                                    color = if (selecionado) Ouro else TextoSuave,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = vm.descricao,
                        onValueChange = { vm.alterarDescricao(it) },
                        shape = Cantos.campo,
                        label = { Text("Personalidade / Traços") },
                        placeholder = { Text(vm.personaDescricao.ifBlank { "Ex.: observador, leal, guarda segredos" }) },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = vm.aparencia,
                        onValueChange = { vm.alterarAparencia(it) },
                        shape = Cantos.campo,
                        label = { Text("Aparência física") },
                        placeholder = { Text(vm.personaAparencia.ifBlank { "Ex.: sobretudo preto, cabelo escuro" }) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = vm.idade,
                            onValueChange = { vm.alterarIdade(it) },
                            shape = Cantos.campo,
                            label = { Text("Idade") },
                            placeholder = { Text(vm.personaIdade.ifBlank { "20" }) },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                        OutlinedTextField(
                            value = vm.interesses,
                            onValueChange = { vm.alterarInteresses(it) },
                            shape = Cantos.campo,
                            label = { Text("Interesses") },
                            placeholder = { Text(vm.personaInteresses.ifBlank { "Música, mistérios..." }) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = vm.aversoes,
                        onValueChange = { vm.alterarAversoes(it) },
                        shape = Cantos.campo,
                        label = { Text("Aversões / Medos") },
                        placeholder = { Text(vm.personaAversoes.ifBlank { "Traição, altura..." }) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ---- Motor Narrativo / Cérebros de IA ----
                if (vm.cerebros.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TituloSecao("Motor de Inteligência Artificial")
                            Surface(color = SuperficieAlta, shape = Cantos.pilula) {
                                Text(
                                    "✨ ${vm.conta?.livens ?: 100} Livens",
                                    color = Ouro,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        vm.cerebros.forEach { cerebro ->
                            CartaoCerebro(
                                cerebro = cerebro,
                                selecionado = cerebro.id == vm.cerebroSelecionado,
                                onClick = { if (cerebro.liberado) vm.selecionarCerebro(cerebro.id) }
                            )
                        }
                    }
                }

                if (ativos >= limiteAtivos) {
                    Text(
                        "Você já tem $limiteAtivos histórias/aventuras ativas. Apague uma para criar outra.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(Modifier.height(4.dp))

                BotaoPrincipal(
                    texto = when {
                        vm.ocupado -> "A IA está escrevendo..."
                        vm.modoCriacao == "historia" -> "Começar História"
                        else -> "Começar Aventura RPG"
                    },
                    onClick = { vm.criarPersonagem(nome) },
                    enabled = podeCriar
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// Cartão de um motor de IA: nome, categoria, custo em Livens e descrição.
@Composable
private fun CartaoCerebro(cerebro: CerebroDto, selecionado: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selecionado) Ouro.copy(alpha = 0.14f) else SuperficieAlta,
        shape = Cantos.cartao,
        border = if (selecionado) BorderStroke(2.dp, Ouro) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = cerebro.liberado) { onClick() }
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                cerebro.emoji.ifBlank { "🧠" },
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(cerebro.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    ChipTag(cerebro.categoria, destaque = false)
                    ChipTag(
                        if (cerebro.custoLivens <= 0) "Grátis" else "✨ ${cerebro.custoLivens}/turno",
                        destaque = cerebro.custoLivens > 0
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    cerebro.descricao,
                    color = TextoSuave,
                    style = MaterialTheme.typography.labelSmall
                )
                if (!cerebro.liberado) {
                    Text(
                        "Resgate seus Livens diários (+30 ✨) para usar este motor.",
                        color = Ouro,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            if (selecionado) {
                Text("✓", color = Ouro, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private val IDEIAS_RAPIDAS_PADRAO = listOf(
    "Acordo num lugar desconhecido com uma pista misteriosa no bolso e alguém batendo na porta.",
    "Por um erro inesperado, preciso dividir o mesmo teto com a pessoa que era minha maior rival.",
    "Entro numa cafeteria vazia durante uma tempestade à meia-noite e encontro alguém me esperando.",
    "Assino um contrato secreto que muda meu destino e fico frente a frente com meu novo aliado."
)

private val ARQUETIPOS_PERSONA = listOf(
    "Sarcástico e observador",
    "Frio por fora, leal por dentro",
    "Corajoso e impulsivo",
    "Gentil e persuasivo",
    "Misterioso com segredos",
    "Calculista e estrategista"
)
