package com.livennia.app

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Paleta Oficial LivennIA (Obsidian Void, Deep Violet e Golden Amber)
private val FundoOled = Color(0xFF090A0F)
private val CardObsidian = Color(0xFF13141F)
private val CardObsidianBorda = Color(0xFF232538)
private val RoxoPrimario = Color(0xFF7C4DFF)
private val RoxoSecundario = Color(0xFF6C5CE7)
private val DouradoLivenn = Color(0xFFF5C842)
private val TextoClaro = Color(0xFFF0F0F5)
private val TextoSecundario = Color(0xFFA0A2B3)
private val TextoMudo = Color(0xFF6E7082)

// -------------------------------------------------------------
// 1. REPOSITÓRIO DINÂMICO DE PERSONAGENS DA COMUNIDADE (ZERADOS)
// -------------------------------------------------------------
object RepositorioPersonagensComunidade {
    // Lista de histórias criadas por usuários reais (inicia vazia até alguém publicar)
    val criacoes = mutableStateListOf<CenarioDto>()

    fun adicionar(cenario: CenarioDto) {
        criacoes.removeAll { it.id == cenario.id }
        criacoes.add(0, cenario)
    }

    fun remover(cenarioId: String) {
        criacoes.removeAll { it.id == cenarioId }
    }
}

// -------------------------------------------------------------
// 2. MODAL DE SELEÇÃO DO MODO DE CRIAÇÃO (IDENTIDADE LIVENNIA)
// -------------------------------------------------------------
@Composable
private fun Modifier.cliqueFuturista3D(
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale3d"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

@Composable
fun ModalModosCriacao(
    onDismiss: () -> Unit,
    onEscolherSpark: () -> Unit,
    onEscolherRapido: () -> Unit,
    onEscolherManual: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.80f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = Color(0xF2121320),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                border = BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF7C4DFF).copy(alpha = 0.85f),
                            Color(0xFF00E5FF).copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                ),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    // Puxador central elegante
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(46.dp, 4.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF))
                                ),
                                CircleShape
                            )
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = RoxoPrimario.copy(alpha = 0.18f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, RoxoPrimario.copy(alpha = 0.4f)),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✦", color = RoxoPrimario, fontSize = 14.sp)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Estúdio de Criação LivennIA",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoxoPrimario,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Como deseja dar vida à sua história?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextoClaro
                    )

                    Spacer(Modifier.height(18.dp))

                    // CARD 1: CENTELHA CRIATIVA (FUTURISTA 3D)
                    Surface(
                        color = Color(0xFF1A182D),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF))
                            )
                        ),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .cliqueFuturista3D { onEscolherSpark() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(0xFF7C4DFF).copy(alpha = 0.45f), Color(0xFF7C4DFF).copy(alpha = 0.10f))
                                            ),
                                            CircleShape
                                        )
                                        .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Centelha Criativa",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextoClaro
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = DouradoLivenn.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, DouradoLivenn.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                "IA Rápida",
                                                color = DouradoLivenn,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        "Responda a perguntas essenciais e a IA molda o universo em segundos.",
                                        fontSize = 12.sp,
                                        color = TextoSecundario,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // DUAS OPÇÕES MENORES: ESTÚDIO DE PERSONAGEM & ARQUITETO DE MUNDOS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Estúdio de Personagem (Express)
                        Surface(
                            color = Color(0xFF161826),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.2.dp,
                                Brush.verticalGradient(
                                    listOf(Color(0xFFF5C842).copy(alpha = 0.7f), Color(0xFF7C4DFF).copy(alpha = 0.25f))
                                )
                            ),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .weight(1f)
                                .cliqueFuturista3D { onEscolherRapido() }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(DouradoLivenn.copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, DouradoLivenn.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = DouradoLivenn,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Estúdio Visual", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextoClaro)
                                Text("Aparência e traços essenciais", fontSize = 11.sp, color = TextoSecundario)
                            }
                        }

                        // Arquiteto de Mundos (Manual)
                        Surface(
                            color = Color(0xFF161826),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.2.dp,
                                Brush.verticalGradient(
                                    listOf(Color(0xFF00E5FF).copy(alpha = 0.7f), Color(0xFF7C4DFF).copy(alpha = 0.25f))
                                )
                            ),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .weight(1f)
                                .cliqueFuturista3D { onEscolherManual() }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Arquiteto de Mundos", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextoClaro)
                                Text("Prólogo, regras e mecânicas", fontSize = 11.sp, color = TextoSecundario)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. WIZARD CENTELHA CRIATIVA (COM ESCOLHA DO FORMATO PELO CRIADOR)
// -------------------------------------------------------------
@Composable
fun WizardModoSpark(
    vm: AppViewModel,
    onFechar: () -> Unit,
    onConcluido: (CenarioDto) -> Unit
) {
    var passoAtual by remember { mutableIntStateOf(1) }
    var tropaSelecionada by remember { mutableStateOf("Conexão proibida com um aliado improvável") }
    var generoSelecionado by remember { mutableStateOf("Feminino") }
    var tomConversa by remember { mutableStateOf("Intenso e provocador") }
    var modoNarrativa by remember { mutableStateOf("historia") } // "historia" (só leitura) ou "rpg" (combates/dados)
    var nomePersonagem by remember { mutableStateOf("") }

    val contexto = LocalContext.current

    val tropasEmocionais = remember {
        listOf(
            "Conexão proibida com um aliado improvável",
            "Tentativa mútua de curar feridas do passado",
            "Rivalidade acirrada que se transforma em atração",
            "Protetor enigmático com segredos perigosos",
            "Romance acolhedor sem intrigas desnecessárias",
            "Alguém orgulhoso e distante que só se abre para você",
            "Retorno inesperado de quem jurou jamais voltar"
        )
    }

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = FundoOled,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Bar com navegação e progresso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { if (passoAtual > 1) passoAtual-- else onFechar() }) {
                        Icon(
                            if (passoAtual > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                            contentDescription = "Voltar",
                            tint = TextoClaro
                        )
                    }

                    // Linha de Progresso (Passos 1 a 5)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .padding(horizontal = 16.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(passoAtual / 5f)
                                .background(RoxoPrimario, CircleShape)
                        )
                    }

                    Text(
                        "$passoAtual/5",
                        color = TextoMudo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(18.dp))

                when (passoAtual) {
                    1 -> {
                        // PASSO 1: DINÂMICA NARRATIVA
                        Text(
                            text = "Qual dinâmica narrativa impulsiona essa história?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text(
                            text = "Escolha o gancho emocional central para a IA construir o conflito.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )

                        Spacer(Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            tropasEmocionais.forEach { tropa ->
                                val selecionado = tropa == tropaSelecionada
                                Surface(
                                    color = if (selecionado) RoxoPrimario.copy(alpha = 0.18f) else CardObsidian,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selecionado) RoxoPrimario else CardObsidianBorda
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tropaSelecionada = tropa }
                                ) {
                                    Text(
                                        text = tropa,
                                        color = if (selecionado) Color.White else TextoClaro,
                                        fontSize = 13.sp,
                                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // PASSO 2: GÊNERO DO PERSONAGEM
                        Text(
                            text = "Qual é o gênero do personagem?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text("Define como o narrador e outros personagens se referem a ele.", color = TextoSecundario, fontSize = 12.sp)

                        Spacer(Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("Feminino", "Masculino", "Não binário").forEach { g ->
                                val sel = g == generoSelecionado
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.18f) else CardObsidian,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { generoSelecionado = g }
                                ) {
                                    Text(
                                        text = g,
                                        color = if (sel) Color.White else TextoClaro,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                    }

                    3 -> {
                        // PASSO 3: TOM DE CONVERSA
                        Text(
                            text = "Qual é a postura e tom de voz?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text("Determina o estilo de fala e as reações emocionais.", color = TextoSecundario, fontSize = 12.sp)

                        Spacer(Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(
                                "Misterioso e provocador",
                                "Acolhedor, carinhoso e protetor",
                                "Frio, calculista e distante",
                                "Intenso, vulnerável e sincero"
                            ).forEach { tom ->
                                val sel = tom == tomConversa
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.18f) else CardObsidian,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tomConversa = tom }
                                ) {
                                    Text(
                                        text = tom,
                                        color = if (sel) Color.White else TextoClaro,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                    }

                    4 -> {
                        // PASSO 4: FORMATO DO JOGO (DEFINIDO PELO CRIADOR)
                        Text(
                            text = "Como outros usuários irão vivenciar esta criação?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text(
                            text = "Você decide o formato definitivo: ao clicar, quem entrar já joga nesse modo.",
                            color = TextoSecundario,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Opção 1: Só Leitura / História Narrativa
                            val selHistoria = modoNarrativa == "historia"
                            Surface(
                                color = if (selHistoria) RoxoPrimario.copy(alpha = 0.18f) else CardObsidian,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if (selHistoria) RoxoPrimario else CardObsidianBorda),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { modoNarrativa = "historia" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📜", fontSize = 28.sp)
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            "História Interativa (Só Leitura)",
                                            color = if (selHistoria) Color.White else TextoClaro,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Foco exclusivo no enredo, diálogos profundos e continuidade de narrativa sem mecânicas de combate.",
                                            color = TextoSecundario,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Opção 2: RPG Completo
                            val selRpg = modoNarrativa == "rpg"
                            Surface(
                                color = if (selRpg) RoxoPrimario.copy(alpha = 0.18f) else CardObsidian,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if (selRpg) RoxoPrimario else CardObsidianBorda),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { modoNarrativa = "rpg" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⚔️", fontSize = 28.sp)
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            "RPG de Aventura",
                                            color = if (selRpg) Color.White else TextoClaro,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Com escolhas táticas, rolagens de desafio, inventário e consequências ativas em jogo.",
                                            color = TextoSecundario,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.weight(1f))
                    }

                    5 -> {
                        // PASSO 5: NOME DO PERSONAGEM
                        Text(
                            text = "Qual é o nome do seu personagem?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text("Escolha um nome memorável para a comunidade.", color = TextoSecundario, fontSize = 12.sp)

                        Spacer(Modifier.height(24.dp))

                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = nomePersonagem,
                                onValueChange = { nomePersonagem = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(16.dp),
                                decorationBox = { inner ->
                                    if (nomePersonagem.isEmpty()) {
                                        Text("Ex: Lyra, Kaiden, Solene...", color = TextoMudo, fontSize = 16.sp)
                                    }
                                    inner()
                                }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Sugestões
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Ayla", "Dante", "Kael", "Raven").forEach { sug ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable { nomePersonagem = sug }
                                ) {
                                    Text(
                                        "+ $sug",
                                        color = DouradoLivenn,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.weight(1f))
                    }
                }

                // Botão de Avançar com Estilo LivennIA
                Button(
                    onClick = {
                        if (passoAtual < 5) {
                            passoAtual++
                        } else {
                            val finalNome = nomePersonagem.trim().ifBlank { "Personagem LivennIA" }
                            val novoCenario = CenarioDto(
                                id = "centelha_" + System.currentTimeMillis(),
                                nome = finalNome,
                                subtitulo = tropaSelecionada.take(35),
                                emoji = if (generoSelecionado == "Feminino") "🌸" else "⚡",
                                genero = "Romance & Mistério",
                                tags = listOf(generoSelecionado, if (modoNarrativa == "rpg") "RPG" else "História", tomConversa.take(15)),
                                sinopse = "Criado com dinâmica '$tropaSelecionada'. Postura: $tomConversa.",
                                abertura = "Você finalmente veio... Eu sabia que nossos caminhos se cruzariam outra vez.",
                                criadoPorUsuario = true,
                                autorNome = "Criado por IA",
                                autorAvatar = "✨",
                                chats = 0,
                                saves = 0,
                                modoJogo = modoNarrativa
                            )
                            vm.publicarCenario(novoCenario) { cenarioSalvo ->
                                onConcluido(cenarioSalvo)
                                Toast.makeText(contexto, "História '$finalNome' criada e publicada!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        if (passoAtual < 5) "Avançar" else "✦ Publicar História no LivennIA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// 4. ESTÚDIO VISUAL / EXPRESS (COM FORMATO ESCOLHIDO PELO CRIADOR)
// -------------------------------------------------------------
@Composable
fun ModalModoRapido(
    vm: AppViewModel,
    onFechar: () -> Unit,
    onConcluido: (CenarioDto) -> Unit
) {
    val contexto = LocalContext.current
    var genero by remember { mutableStateOf("Feminino") }
    var descricaoVisual by remember { mutableStateOf("") }
    var estiloImagem by remember { mutableStateOf("semi_realistic") }
    var modoJogoEscolhido by remember { mutableStateOf("historia") }
    var nome by remember { mutableStateOf("") }

    val estilos = listOf("Anime", "Dark Fantasy", "Cyberpunk", "Realista")

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = FundoOled,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onFechar) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextoClaro)
                    }
                    Text(
                        "Estúdio Visual de Personagem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextoClaro,
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Upload / Arte de Referência
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable {
                                Toast.makeText(contexto, "Selecione uma imagem de avatar da galeria", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = RoxoPrimario, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("Toque para vincular arte ou avatar", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("PNG, JPG ou GIF", color = TextoMudo, fontSize = 11.sp)
                        }
                    }

                    // Nome do Personagem
                    Column {
                        Text("Nome do Personagem", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = nome,
                                onValueChange = { nome = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 14.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (nome.isEmpty()) Text("Ex: Morgana, Kaelen...", color = TextoMudo, fontSize = 14.sp)
                                    inner()
                                }
                            )
                        }
                    }

                    // Formato da Narrativa (Escolha do Criador)
                    Column {
                        Text("Formato da História (Decidido por você)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("historia" to "📜 Só Leitura / Narrativa", "rpg" to "⚔️ RPG com Ações").forEach { (valor, rotulo) ->
                                val sel = modoJogoEscolhido == valor
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.20f) else CardObsidian,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { modoJogoEscolhido = valor }
                                ) {
                                    Text(
                                        rotulo,
                                        color = if (sel) Color.White else TextoSecundario,
                                        fontSize = 12.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Gênero
                    Column {
                        Text("Gênero", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Feminino", "Masculino", "Não binário").forEach { g ->
                                val sel = g == genero
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.20f) else CardObsidian,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier.clickable { genero = g }
                                ) {
                                    Text(
                                        g,
                                        color = if (sel) Color.White else TextoSecundario,
                                        fontSize = 12.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Descrição Visual
                    Column {
                        Text("Descrição Visual & Personalidade", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                        ) {
                            BasicTextField(
                                value = descricaoVisual,
                                onValueChange = { descricaoVisual = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (descricaoVisual.isEmpty()) {
                                        Text(
                                            "Cabelos prateados, olhos violeta intensos, postura nobre e temperamento afiado...",
                                            color = TextoMudo,
                                            fontSize = 13.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    // Estilo de Arte & Prévia Visual da IA
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estilo Visual da IA", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                            Text("Exemplos com IA", color = TextoMudo, fontSize = 11.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (est in ESTILOS_IMAGEM_PERSONA) {
                                val sel = est.id == estiloImagem
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.22f) else CardObsidian,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if (sel) 1.5.dp else 1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clickable { estiloImagem = est.id }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        PreviaEstiloImagem(
                                            estiloId = est.id,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp),
                                            recorte = RoundedCornerShape(8.dp)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            est.nome,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = if (sel) Color.White else TextoClaro,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                }

                // Botão de Publicação LivennIA
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            val finalNome = nome.trim().ifBlank { "Personagem Visual" }
                            val novoCenario = CenarioDto(
                                id = "rapido_" + System.currentTimeMillis(),
                                nome = finalNome,
                                subtitulo = estiloImagem,
                                emoji = if (genero == "Feminino") "🌸" else "✨",
                                genero = estiloImagem,
                                tags = listOf(genero, estiloImagem, if (modoJogoEscolhido == "rpg") "RPG" else "Leitura"),
                                sinopse = descricaoVisual.ifBlank { "Personagem concebido com estética $estiloImagem e narrativa envolvente." },
                                abertura = "Estava esperando por você. O que quer saber primeiro?",
                                criadoPorUsuario = true,
                                autorNome = "Criado por IA",
                                autorAvatar = "✨",
                                chats = 0,
                                saves = 0,
                                modoJogo = modoJogoEscolhido
                            )
                            vm.publicarCenario(novoCenario) { cenarioSalvo ->
                                onConcluido(cenarioSalvo)
                                Toast.makeText(contexto, "História '$finalNome' publicada com sucesso!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("✦ Publicar no LivennIA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. ARQUITETO DE MUNDOS / MODO AVANÇADO
// -------------------------------------------------------------
@Composable
fun ModalModoManual(
    vm: AppViewModel,
    onFechar: () -> Unit,
    onConcluido: (CenarioDto) -> Unit
) {
    val contexto = LocalContext.current
    var nome by remember { mutableStateOf("") }
    var subtitulo by remember { mutableStateOf("") }
    var sinopse by remember { mutableStateOf("") }
    var abertura by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("Fantasia Sombria") }
    var modoJogoEscolhido by remember { mutableStateOf("historia") }
    var visibilidadePublica by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = FundoOled,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onFechar) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextoClaro)
                    }
                    Text(
                        "Arquiteto de Mundos & Narrativas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextoClaro,
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nome
                    Column {
                        Text("Nome do Personagem ou Cenário *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = nome,
                                onValueChange = { nome = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 14.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Formato da Narrativa
                    Column {
                        Text("Modo de Jogo Decidido pelo Criador *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("historia" to "📜 Só Leitura / Narrativa", "rpg" to "⚔️ RPG com Escolhas e Ações").forEach { (valor, rotulo) ->
                                val sel = modoJogoEscolhido == valor
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.20f) else CardObsidian,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { modoJogoEscolhido = valor }
                                ) {
                                    Text(
                                        rotulo,
                                        color = if (sel) Color.White else TextoSecundario,
                                        fontSize = 12.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Subtítulo
                    Column {
                        Text("Subtítulo Curto", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = subtitulo,
                                onValueChange = { subtitulo = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 14.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Sinopse
                    Column {
                        Text("Sinopse & Contexto do Mundo *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            BasicTextField(
                                value = sinopse,
                                onValueChange = { sinopse = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Fala de Abertura
                    Column {
                        Text("Fala ou Cena de Abertura", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                        ) {
                            BasicTextField(
                                value = abertura,
                                onValueChange = { abertura = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Visibilidade
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Publicar na Comunidade", color = TextoClaro, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Ficará visível para outros viajantes no Explorar", color = TextoSecundario, fontSize = 11.sp)
                        }
                        Surface(
                            color = if (visibilidadePublica) RoxoPrimario else Color.DarkGray,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { visibilidadePublica = !visibilidadePublica }
                        ) {
                            Text(
                                if (visibilidadePublica) "PÚBLICO" else "PRIVADO",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                }

                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (nome.isBlank()) {
                                Toast.makeText(contexto, "Dê um nome ao personagem!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val novoCenario = CenarioDto(
                                id = "manual_" + System.currentTimeMillis(),
                                nome = nome.trim(),
                                subtitulo = subtitulo.trim(),
                                emoji = "📜",
                                genero = genero,
                                tags = listOf(if (modoJogoEscolhido == "rpg") "RPG" else "História", genero),
                                sinopse = sinopse.ifBlank { "História personalizada no universo LivennIA." },
                                abertura = abertura.ifBlank { "A jornada começa agora." },
                                criadoPorUsuario = true,
                                autorNome = vm.apelido.ifBlank { "Você" },
                                autorAvatar = "🧙",
                                chats = 0,
                                saves = 0,
                                modoJogo = modoJogoEscolhido
                            )
                            vm.publicarCenario(novoCenario) { cenarioSalvo ->
                                onConcluido(cenarioSalvo)
                                Toast.makeText(contexto, "Narrativa publicada com sucesso!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("✦ Salvar e Disponibilizar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. FICHA DETALHADA DO PERSONAGEM (IMAGEM 1 - PRÉVIA DA HISTÓRIA)
// -------------------------------------------------------------
@Composable
fun DetalhesPersonagemModal(
    cenario: CenarioDto,
    versaoCapa: Int = 0,
    apelidoUsuario: String = "Você",
    vm: AppViewModel? = null,
    onFechar: () -> Unit,
    onIniciarChat: () -> Unit
) {
    val contexto = LocalContext.current
    var salvo by remember { mutableStateOf(false) }
    var novoComentario by remember { mutableStateOf("") }
    var comentarioParaEditar by remember { mutableStateOf<ComentarioDto?>(null) }
    var textoEdicao by remember { mutableStateOf("") }
    var comentarioParaExcluir by remember { mutableStateOf<ComentarioDto?>(null) }
    var menuMaisAberto by remember { mutableStateOf(false) }
    var mostrarPerfilCriador by remember { mutableStateOf(false) }
    var mostrarDialogoDenuncia by remember { mutableStateOf(false) }
    var motivoDenuncia by remember { mutableStateOf("") }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var mostrarDialogoExcluir by remember { mutableStateOf(false) }

    LaunchedEffect(cenario.id) {
        vm?.carregarComentarios(cenario.id)
    }

    val listaComentarios: List<ComentarioDto> = vm?.comentarios ?: remember { mutableStateListOf<ComentarioDto>() }

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color.Black,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Conteúdo rolável
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 68.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Pôster superior com botão voltar e menu ...
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        CapaCenario(
                            cenarioId = cenario.id,
                            temCapa = cenario.capa,
                            emoji = cenario.emojiSeguro,
                            nome = cenario.nome,
                            versao = versaoCapa,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Degradê suave LivennIA para preto
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.45f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.85f),
                                            Color.Black
                                        )
                                    )
                                )
                        )

                        // Barra Superior da Capa (Voltar e ...)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.50f),
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                IconButton(onClick = onFechar) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                                }
                            }

                            Box {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.50f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    IconButton(onClick = { menuMaisAberto = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = Color.White)
                                    }
                                }
                                DropdownMenu(
                                    expanded = menuMaisAberto,
                                    onDismissRequest = { menuMaisAberto = false }
                                ) {
                                    val ehAutor = cenario.criadoPorUsuario && (
                                        cenario.autorNomeSeguro.equals(apelidoUsuario, ignoreCase = true) ||
                                        cenario.autorNomeSeguro.equals(vm?.apelido, ignoreCase = true) ||
                                        cenario.autorNomeSeguro.contains("IA", ignoreCase = true) ||
                                        cenario.autorNomeSeguro.equals("Você", ignoreCase = true) ||
                                        cenario.autorNomeSeguro.equals("Comunidade", ignoreCase = true) ||
                                        RepositorioPersonagensComunidade.criacoes.any { it.id == cenario.id } ||
                                        vm?.conta?.dono == true
                                    )
                                    if (ehAutor) {
                                        DropdownMenuItem(
                                            text = { Text("✏️ Editar História") },
                                            onClick = {
                                                menuMaisAberto = false
                                                mostrarDialogoEditar = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("🗑️ Excluir História", color = Color(0xFFFF5252)) },
                                            onClick = {
                                                menuMaisAberto = false
                                                mostrarDialogoExcluir = true
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Compartilhar História") },
                                        onClick = {
                                            menuMaisAberto = false
                                            Toast.makeText(contexto, "Link copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Denunciar História", color = Color(0xFFFF5252)) },
                                        onClick = {
                                            menuMaisAberto = false
                                            mostrarDialogoDenuncia = true
                                        }
                                    )
                                }
                            }
                        }

                        // Título e Contagem no rodapé da capa (Exatamente como na Imagem 1)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "[${cenario.nome}]${if (cenario.subtituloSeguro.isNotBlank()) " - [${cenario.subtituloSeguro}]" else ""}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 21.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${cenario.saves} saves",
                                color = Color(0xFF9E9E9E),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Card do Criador (Imagem 1: Avatar, Nickname, Criador, mini previews e >)
                    Surface(
                        color = Color(0xFF161618),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF26262A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { mostrarPerfilCriador = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF242428), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (cenario.criadoPorUsuario) cenario.autorAvatarSeguro else "🎭", fontSize = 22.sp)
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (cenario.criadoPorUsuario) cenario.autorNomeSeguro else "Dulxswords",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Criador",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            // Miniaturas de outras criações do criador + seta >
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF242428),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(cenario.emojiSeguro, fontSize = 18.sp)
                                    }
                                }
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Ver perfil",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Descrição / Sinopse
                    Text(
                        text = cenario.sinopseSegura.ifBlank { "Sem descrição disponível." },
                        color = Color(0xFFD1D1D6),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )

                    Spacer(Modifier.height(18.dp))

                    // Linha de Ação: Botão Amarelo "Chat" + Botão Salvar (Imagem 1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onIniciarChat,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE5FF2E),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Chat",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }

                        // Botão de Bookmark
                        Surface(
                            color = Color(0xFF1C1C1E),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
                            modifier = Modifier
                                .size(52.dp)
                                .clickable {
                                    salvo = !salvo
                                    Toast.makeText(
                                        contexto,
                                        if (salvo) "História salva nos favoritos!" else "Removida dos favoritos",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (salvo) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Favoritar",
                                    tint = if (salvo) Color(0xFFE5FF2E) else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Seção de Comentários (Imagem 1: 25 Comments, lista com curtidas, responder, editar, excluir)
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        Text(
                            text = "${listaComentarios.size} Comments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(14.dp))

                        if (listaComentarios.isEmpty()) {
                            Text(
                                text = "Nenhum comentário ainda. Seja o primeiro a comentar!",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                for (coment in listaComentarios) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Avatar do comentador
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF2C2C2E), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(coment.autorAvatar.ifBlank { "👤" }, fontSize = 18.sp)
                                        }

                                        Spacer(Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            // Nome do autor
                                            Text(
                                                text = coment.autorNome,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            // Texto do comentário
                                            Text(
                                                text = coment.texto,
                                                color = Color(0xFFE5E5EA),
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            // Rodapé do comentário: Data / Responder / Editar / Excluir
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = coment.criadoEm.take(10).replace("-", "/"),
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "Responder",
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.clickable {
                                                        novoComentario = "@${coment.autorNome} "
                                                    }
                                                )
                                                // Se for meu comentário, permite editar e excluir!
                                                if (coment.meuComentario) {
                                                    Text(
                                                        text = "Editar",
                                                        color = Color(0xFF64B5F6),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.clickable {
                                                            comentarioParaEditar = coment
                                                            textoEdicao = coment.texto
                                                        }
                                                    )
                                                    Text(
                                                        text = "Excluir",
                                                        color = Color(0xFFFF5252),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.clickable {
                                                            comentarioParaExcluir = coment
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Botão de like do comentário (Coração + contador)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    vm?.curtirComentario(cenario.id, coment.id)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    if (coment.meuLike) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Curtir comentário",
                                                    tint = if (coment.meuLike) Color(0xFFFF5252) else Color(0xFF8E8E93),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            if (coment.likes > 0) {
                                                Text(
                                                    text = coment.likes.toString(),
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(30.dp))
                    }
                }

                // Barra inferior fixa: "Comente agora!" (Imagem 1)
                Surface(
                    color = Color.Black.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(0xFF1C1C1E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C1E), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = novoComentario,
                            onValueChange = { novoComentario = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color(0xFFE5FF2E)),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (novoComentario.isEmpty()) {
                                    Text("Comente agora!", color = Color(0xFF8E8E93), fontSize = 14.sp)
                                }
                                inner()
                            }
                        )

                        IconButton(
                            onClick = {
                                if (novoComentario.isNotBlank()) {
                                    vm?.enviarComentario(cenario.id, novoComentario) {
                                        novoComentario = ""
                                        Toast.makeText(contexto, "Comentário enviado!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = if (novoComentario.isNotBlank()) Color(0xFFE5FF2E) else Color(0xFF555555),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal de Edição de Comentário
    comentarioParaEditar?.let { coment ->
        AlertDialog(
            onDismissRequest = { comentarioParaEditar = null },
            title = { Text("Editar Comentário") },
            text = {
                OutlinedTextField(
                    value = textoEdicao,
                    onValueChange = { textoEdicao = it },
                    label = { Text("Seu comentário") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm?.editarComentario(cenario.id, coment.id, textoEdicao) {
                            comentarioParaEditar = null
                            Toast.makeText(contexto, "Comentário atualizado!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { comentarioParaEditar = null }) { Text("Cancelar") }
            }
        )
    }

    // Confirmação de Exclusão de Comentário
    comentarioParaExcluir?.let { coment ->
        AlertDialog(
            onDismissRequest = { comentarioParaExcluir = null },
            title = { Text("Excluir comentário?") },
            text = { Text("Tem certeza de que deseja apagar o seu comentário? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    onClick = {
                        vm?.excluirComentario(cenario.id, coment.id) {
                            comentarioParaExcluir = null
                            Toast.makeText(contexto, "Comentário excluído!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Excluir", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { comentarioParaExcluir = null }) { Text("Cancelar") }
            }
        )
    }

    // Dialog de Denúncia da História
    if (mostrarDialogoDenuncia) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoDenuncia = false },
            title = { Text("Denunciar História") },
            text = {
                Column {
                    Text("Descreva o motivo da denúncia:", fontSize = 13.sp, color = TextoSecundario)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivoDenuncia,
                        onValueChange = { motivoDenuncia = it },
                        placeholder = { Text("Conteúdo inapropriado, spam, etc.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (motivoDenuncia.isNotBlank()) {
                            vm?.denunciar(motivo = motivoDenuncia, detalhe = "Denúncia do cenário ${cenario.id}", jogoId = cenario.id)
                            mostrarDialogoDenuncia = false
                            motivoDenuncia = ""
                            Toast.makeText(contexto, "Denúncia enviada para análise.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Enviar Denúncia") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoDenuncia = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarDialogoExcluir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoExcluir = false },
            title = { Text("Excluir História", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Tem certeza que deseja apagar a história '${cenario.nome}'? Esta ação é definitiva e removerá a história e seus comentários.",
                    color = Color(0xFFCCCED9),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoExcluir = false
                        vm?.excluirCenario(cenario.id) {
                            Toast.makeText(contexto, "História '${cenario.nome}' excluída!", Toast.LENGTH_SHORT).show()
                            onFechar()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Excluir", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoExcluir = false }) {
                    Text("Cancelar", color = Color(0xFF8E8E93))
                }
            },
            containerColor = Color(0xFF161618)
        )
    }

    if (mostrarDialogoEditar) {
        var edNome by remember { mutableStateOf(cenario.nome) }
        var edSubtitulo by remember { mutableStateOf(cenario.subtituloSeguro) }
        var edSinopse by remember { mutableStateOf(cenario.sinopseSegura) }
        var edAbertura by remember { mutableStateOf(cenario.aberturaSegura) }
        var edGenero by remember { mutableStateOf(cenario.generoSeguro) }
        var edTags by remember { mutableStateOf(cenario.tags?.joinToString(", ") ?: "") }
        var edEmoji by remember { mutableStateOf(cenario.emojiSeguro) }

        Dialog(
            onDismissRequest = { mostrarDialogoEditar = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = Color(0xFF141416),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF28282D)),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.90f)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Editar História",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { mostrarDialogoEditar = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color(0xFF8E8E93))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = edNome,
                            onValueChange = { edNome = it.take(40) },
                            label = { Text("Nome da História / Personagem") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = edSubtitulo,
                            onValueChange = { edSubtitulo = it.take(60) },
                            label = { Text("Subtítulo / Apelido") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = edEmoji,
                                onValueChange = { edEmoji = it.take(4) },
                                label = { Text("Emoji") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = edGenero,
                                onValueChange = { edGenero = it.take(30) },
                                label = { Text("Gênero") },
                                modifier = Modifier.weight(2f)
                            )
                        }
                        OutlinedTextField(
                            value = edTags,
                            onValueChange = { edTags = it.take(120) },
                            label = { Text("Tags (separadas por vírgula)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = edSinopse,
                            onValueChange = { edSinopse = it.take(800) },
                            label = { Text("Sinopse / Contexto") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                        OutlinedTextField(
                            value = edAbertura,
                            onValueChange = { edAbertura = it.take(800) },
                            label = { Text("Primeira Fala do Personagem (Abertura)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { mostrarDialogoEditar = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", color = Color(0xFF8E8E93))
                        }
                        Button(
                            onClick = {
                                if (edNome.isNotBlank()) {
                                    val tagsLista = edTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    val atualizado = cenario.copy(
                                        nome = edNome.trim(),
                                        subtitulo = edSubtitulo.trim(),
                                        emoji = edEmoji.ifBlank { "🎭" },
                                        genero = edGenero.ifBlank { "Aventura" },
                                        tags = tagsLista,
                                        sinopse = edSinopse.trim(),
                                        abertura = edAbertura.trim()
                                    )
                                    vm?.editarCenario(atualizado) {
                                        Toast.makeText(contexto, "História atualizada!", Toast.LENGTH_SHORT).show()
                                        mostrarDialogoEditar = false
                                        onFechar()
                                    }
                                }
                            },
                            enabled = edNome.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C842)),
                            modifier = Modifier.weight(1.5f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Salvar Alterações", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal de Perfil do Criador (Imagem 2)
    if (mostrarPerfilCriador) {
        PerfilCriadorModal(
            criadorNome = if (cenario.criadoPorUsuario) cenario.autorNomeSeguro else "Dulxswords",
            vm = vm,
            onFechar = { mostrarPerfilCriador = false },
            onIniciarHistoria = { historia ->
                mostrarPerfilCriador = false
                onFechar()
                vm?.iniciarHistoriaDireta(historia, historia.modoSeguro)
            }
        )
    }
}

// -------------------------------------------------------------
// 7. PERFIL DO CRIADOR (IMAGEM 2)
// -------------------------------------------------------------
@Composable
fun PerfilCriadorModal(
    criadorNome: String,
    vm: AppViewModel?,
    onFechar: () -> Unit,
    onIniciarHistoria: (CenarioDto) -> Unit
) {
    val contexto = LocalContext.current
    var menuOpcoesAberto by remember { mutableStateOf(false) }
    var dialogoDenunciaAberto by remember { mutableStateOf(false) }
    var motivoDenuncia by remember { mutableStateOf("") }
    var dialogoBloqueioAberto by remember { mutableStateOf(false) }

    LaunchedEffect(criadorNome) {
        vm?.carregarPerfilCriador(criadorNome)
    }

    val perfil = vm?.perfilCriador ?: PerfilCriadorDto(
        nome = criadorNome,
        avatar = "🎭",
        uid = "UID:@$criadorNome",
        chats = 921800,
        saves = 704,
        seguidores = 74,
        seguindo = 1,
        estaSeguindo = false
    )

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color.Black,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 74.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Barra Superior com Voltar e Menu de 3 Pontos (com Denunciar e Bloquear)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onFechar) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                        }

                        Box {
                            IconButton(onClick = { menuOpcoesAberto = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = menuOpcoesAberto,
                                onDismissRequest = { menuOpcoesAberto = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🚩 Denunciar perfil", color = Color(0xFFFF5252)) },
                                    onClick = {
                                        menuOpcoesAberto = false
                                        dialogoDenunciaAberto = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🚫 Bloquear", color = Color(0xFFFF5252)) },
                                    onClick = {
                                        menuOpcoesAberto = false
                                        dialogoBloqueioAberto = true
                                    }
                                )
                            }
                        }
                    }

                    // Cabeçalho do Perfil (Avatar, Nome, UID) - Imagem 2
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFF1E1E22), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(perfil.avatar.ifBlank { "🎭" }, fontSize = 32.sp)
                            }

                            Spacer(Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = perfil.nome,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = perfil.uid,
                                    color = Color(0xFF8E8E93),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Linha de Métricas (Imagem 2: Chats, Salvos, Seguidores, Seguindo)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColunaMetrica(valor = formatarNumero(perfil.chats), rotulo = "Chats")
                            ColunaMetrica(valor = formatarNumero(perfil.saves), rotulo = "Salvos")
                            ColunaMetrica(valor = formatarNumero(perfil.seguidores), rotulo = "Seguidores")
                            ColunaMetrica(valor = formatarNumero(perfil.seguindo), rotulo = "Seguindo")
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Seção "Personagem" (Histórias criadas)
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Personagem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        val historias = perfil.historias.ifEmpty {
                            vm?.catalogo?.filter { it.criadoPorUsuario && it.autorNomeSeguro == perfil.nome } ?: emptyList()
                        }

                        if (historias.isEmpty()) {
                            Text(
                                "Nenhuma história criada ainda por este autor.",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            // Grid 2 Colunas com as histórias do criador (Imagem 2)
                            val pares = historias.chunked(2)
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                for (linha in pares) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        for (hist in linha) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                CardCriacaoCriador(
                                                    historia = hist,
                                                    versaoCapa = vm?.capaVersao ?: 0,
                                                    onClick = { onIniciarHistoria(hist) }
                                                )
                                            }
                                        }
                                        if (linha.size == 1) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }

                // Botão Amarelo Fixo na base: "Seguir" (Imagem 2)
                Surface(
                    color = Color.Black.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            vm?.alternarSeguirCriador(perfil.nome)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (perfil.estaSeguindo) Color(0xFF2C2C2E) else Color(0xFFE5FF2E),
                            contentColor = if (perfil.estaSeguindo) Color(0xFFE5FF2E) else Color.Black
                        )
                    ) {
                        Icon(
                            if (perfil.estaSeguindo) Icons.Default.Check else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (perfil.estaSeguindo) Color(0xFFE5FF2E) else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (perfil.estaSeguindo) "Seguindo" else "Seguir",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (perfil.estaSeguindo) Color(0xFFE5FF2E) else Color.Black
                        )
                    }
                }
            }
        }
    }

    // Diálogo de Denúncia do Perfil
    if (dialogoDenunciaAberto) {
        AlertDialog(
            onDismissRequest = { dialogoDenunciaAberto = false },
            title = { Text("Denunciar Perfil") },
            text = {
                Column {
                    Text("Motivo da denúncia contra @${perfil.nome}:", fontSize = 13.sp, color = TextoSecundario)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivoDenuncia,
                        onValueChange = { motivoDenuncia = it },
                        placeholder = { Text("Ex: Conteúdo ofensivo, violação de regras") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (motivoDenuncia.isNotBlank()) {
                            vm?.denunciarCriador(perfil.nome, motivoDenuncia) {
                                dialogoDenunciaAberto = false
                                motivoDenuncia = ""
                                Toast.makeText(contexto, "Perfil denunciado com sucesso.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("Denunciar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoDenunciaAberto = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de Confirmação de Bloqueio
    if (dialogoBloqueioAberto) {
        AlertDialog(
            onDismissRequest = { dialogoBloqueioAberto = false },
            title = { Text("Bloquear @${perfil.nome}?") },
            text = { Text("Você não verá mais histórias criadas por este autor no feed.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    onClick = {
                        vm?.bloquearCriador(perfil.nome) {
                            dialogoBloqueioAberto = false
                            Toast.makeText(contexto, "Criador bloqueado.", Toast.LENGTH_SHORT).show()
                            onFechar()
                        }
                    }
                ) { Text("Bloquear", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { dialogoBloqueioAberto = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CardCriacaoCriador(
    historia: CenarioDto,
    versaoCapa: Int,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF141416),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF242428)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                CapaCenario(
                    cenarioId = historia.id,
                    temCapa = historia.capa,
                    emoji = historia.emojiSeguro,
                    nome = historia.nome,
                    versao = versaoCapa,
                    modifier = Modifier.fillMaxSize()
                )
                // Badge de chats na parte inferior da imagem (Imagem 2: 💬 493,3K)
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💬", fontSize = 10.sp)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = formatarNumero(historia.chats),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = historia.nome,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = historia.sinopseSegura,
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(6.dp))
                // Chips de gênero/tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    val tags = (historia.tags.orEmpty() + listOf(historia.generoSeguro)).distinct().take(2)
                    tags.forEach { tag ->
                        Surface(
                            color = Color(0xFF242428),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tag,
                                color = Color(0xFFA0A2B3),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColunaMetrica(valor: String, rotulo: String) {
    Column {
        Text(
            text = valor,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = rotulo,
            color = Color(0xFF8E8E93),
            fontSize = 11.sp
        )
    }
}

private fun formatarNumero(num: Int): String {
    return when {
        num >= 1_000_000 -> String.format(Locale.US, "%.1fM", num / 1_000_000f).replace(".", ",")
        num >= 1_000 -> String.format(Locale.US, "%.1fK", num / 1_000f).replace(".", ",")
        else -> num.toString()
    }
}
