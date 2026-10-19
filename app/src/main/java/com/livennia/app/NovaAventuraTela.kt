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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Tela de criação do personagem ("Editar persona" no app de referência).
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NovaAventuraTela(vm: AppViewModel) {
    var nome by rememberSaveable { mutableStateOf("") }
    val ativos = vm.jogos.count { !it.encerrado }
    val limiteAtivos = vm.conta?.maxJogos ?: 3
    val podeCriar = !vm.ocupado && ativos < limiteAtivos

    BackHandler { vm.navegarPara(Tela.Personagens) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
    ) {
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            // ---- Barra de cima ----
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { vm.navegarPara(Tela.Personagens) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Voltar")
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (vm.modoCriacao == "historia") "Nova história" else "Nova aventura RPG",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(64.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---- Seletor de Modo: RPG vs História (Livro / Emochi) ----
                TituloSecao("Escolha o modo de experiência")
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
                                color = if (ehRpg) Ouro else MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Com barra de vida, inventário, escolhas táticas e perigo real.",
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
                                color = if (ehHistoria) Ouro else MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Livro vivo e conversa livre, sem barra de vida.",
                                color = TextoSuave,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // ---- Cenário oficial (quando a história veio da vitrine) ----
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
                                modifier = Modifier.size(78.dp),
                                recorte = Cantos.campo
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${cenario.emoji} ${cenario.nome}",
                                    style = MaterialTheme.typography.titleMedium
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
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Text(
                        "Você cria o protagonista desta história. O narrador entra no papel do " +
                                "personagem oficial do cenário.",
                        color = TextoSuave,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // ---- Prévia do retrato ----
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(contentAlignment = Alignment.BottomCenter) {
                        AvatarPersonagem(
                            seed = nome.ifBlank { "Viajante" }.hashCode(),
                            nome = nome.ifBlank { "Viajante" },
                            tamanho = 148
                        )
                        Surface(
                            color = Ouro,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                "✦  retrato por IA",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Text(
                    "O retrato é criado pela IA quando a aventura começa. Se a cota de imagem estiver " +
                            "esgotada, usamos um retrato desenhado.",
                    color = TextoSuave,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth()
                )

                // ---- Nome ----
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it.take(30) },
                    shape = Cantos.campo,
                    label = { Text("Nome do personagem") },
                    singleLine = true,
                    supportingText = { Text("${nome.length}/30") },
                    modifier = Modifier.fillMaxWidth()
                )

                // ---- Estilo / Gênero (em cenário oficial o estilo já vem do cenário) ----
                if (vm.cenarioSelecionado == null) {
                    TituloSecao("Estilo da história")
                    if (vm.generos.isEmpty()) {
                        Text("Carregando estilos...", color = TextoSuave, style = MaterialTheme.typography.labelSmall)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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

                // ---- Ideia para a história (com sugestões prontas estilo Emochi) ----
                val ideiasDoEstilo = vm.generos
                    .find { it.nome == vm.generoSelecionado }
                    ?.ideias
                    ?.takeIf { it.isNotEmpty() }
                    ?: IDEIAS_RAPIDAS_PADRAO

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TituloSecao("Cena inicial / Ideia (opcional)")
                    TextButton(
                        onClick = {
                            val outras = ideiasDoEstilo.filter { it != vm.premissa }
                            val escolhida = (outras.ifEmpty { ideiasDoEstilo }).randomOrNull()
                            if (escolhida != null) vm.alterarPremissa(escolhida)
                        }
                    ) {
                        Text("🎲 Sortear ideia", color = Ouro, style = MaterialTheme.typography.labelMedium)
                    }
                }

                OutlinedTextField(
                    value = vm.premissa,
                    onValueChange = { vm.alterarPremissa(it) },
                    shape = Cantos.campo,
                    label = { Text("Como a história ou encontro deve começar?") },
                    placeholder = { Text("Toque numa ideia abaixo ou escreva a sua...") },
                    minLines = 2,
                    maxLines = 4,
                    supportingText = { Text("${vm.premissa.length}/200") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Ideias prontas para ${vm.cenarioSelecionado?.genero ?: vm.generoSelecionado} (toque para usar):",
                    color = TextoSuave,
                    style = MaterialTheme.typography.labelSmall
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ideiasDoEstilo.take(4).forEach { ideia ->
                        val ativa = vm.premissa == ideia
                        Surface(
                            color = if (ativa) Ouro.copy(alpha = 0.16f) else SuperficieAlta,
                            shape = Cantos.campo,
                            border = if (ativa) BorderStroke(1.dp, Ouro) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.alterarPremissa(ideia) }
                        ) {
                            Text(
                                text = "✦ $ideia",
                                color = if (ativa) Ouro else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                // ---- Ficha do personagem (Persona estilo Emochi) ----
                TituloSecao("Sua Persona / Protagonista (opcional)")
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
                    label = { Text("Personalidade e história da sua persona") },
                    placeholder = { Text("Ex.: sarcástico, leal, guarda um segredo do passado") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = vm.aparencia,
                    onValueChange = { vm.alterarAparencia(it) },
                    shape = Cantos.campo,
                    label = { Text("Aparência física") },
                    placeholder = { Text("Ex.: cabelo escuro, cicatriz no rosto, sobretudo preto") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = vm.idade,
                        onValueChange = { vm.alterarIdade(it) },
                        shape = Cantos.campo,
                        label = { Text("Idade") },
                        singleLine = true,
                        modifier = Modifier.width(110.dp)
                    )
                    OutlinedTextField(
                        value = vm.interesses,
                        onValueChange = { vm.alterarInteresses(it) },
                        shape = Cantos.campo,
                        label = { Text("Interesses") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = vm.aversoes,
                    onValueChange = { vm.alterarAversoes(it) },
                    shape = Cantos.campo,
                    label = { Text("Aversões / medos") },
                    placeholder = { Text("Ex.: medo de altura, não suporta mentira") },
                    minLines = 1,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // ---- Cérebro da IA (Sabores de modelos inspirados no Emochi) ----
                if (vm.cerebros.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TituloSecao("Sabor de modelo da IA")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(color = SuperficieAlta, shape = Cantos.pilula) {
                                Text(
                                    "🍡 ${vm.conta?.mochis ?: 100} Mochis",
                                    color = Ouro,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            if (vm.conta?.resgateDiarioDisponivel == true) {
                                Surface(
                                    color = Ouro.copy(alpha = 0.22f),
                                    shape = Cantos.pilula,
                                    border = BorderStroke(1.dp, Ouro),
                                    modifier = Modifier.clickable { vm.resgatarMochiDiario() }
                                ) {
                                    Text(
                                        "+30 grátis",
                                        color = Ouro,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        "Escolha o sabor narrativo da IA (Vanilla V2 é grátis; sabores especiais usam Mochis por turno).",
                        color = TextoSuave,
                        style = MaterialTheme.typography.labelSmall
                    )
                    vm.cerebros.forEach { cerebro ->
                        CartaoCerebro(
                            cerebro = cerebro,
                            selecionado = cerebro.id == vm.cerebroSelecionado,
                            onClick = { if (cerebro.liberado) vm.selecionarCerebro(cerebro.id) }
                        )
                    }
                }

                if (ativos >= limiteAtivos) {
                    Text(
                        "Você já tem $limiteAtivos personagens ativos. Apague um para criar outro.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                BotaoPrincipal(
                    texto = when {
                        vm.ocupado -> "A IA está escrevendo..."
                        vm.modoCriacao == "historia" -> "Começar história"
                        else -> "Começar aventura RPG"
                    },
                    onClick = { vm.criarPersonagem(nome) },
                    enabled = podeCriar
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// Cartão de um sabor/"cérebro" de IA: nome, categoria, custo em Mochis e descrição.
@Composable
private fun CartaoCerebro(cerebro: CerebroDto, selecionado: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selecionado) Ouro.copy(alpha = 0.14f) else SuperficieAlta,
        shape = Cantos.cartao,
        border = if (selecionado) BorderStroke(2.dp, Ouro) else null,
        modifier = Modifier.fillMaxWidth().clickable(enabled = cerebro.liberado) { onClick() }
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
                    Text(cerebro.nome, style = MaterialTheme.typography.titleMedium)
                    ChipTag(cerebro.categoria, destaque = false)
                    ChipTag(
                        if (cerebro.custoMochi <= 0) "Grátis" else "🍡 ${cerebro.custoMochi}/turno",
                        destaque = cerebro.custoMochi > 0
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
                        "Resgate seus Mochis diários (+30 🍡) para usar este sabor.",
                        color = Ouro,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            if (selecionado) {
                Text("✓", color = Ouro, style = MaterialTheme.typography.titleMedium)
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
    "Misterioso com um segredo",
    "Calculista e estrategista"
)

