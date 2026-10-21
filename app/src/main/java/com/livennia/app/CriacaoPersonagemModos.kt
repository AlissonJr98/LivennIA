package com.livennia.app

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
        criacoes.add(0, cenario)
    }
}

// -------------------------------------------------------------
// 2. MODAL DE SELEÇÃO DO MODO DE CRIAÇÃO (IDENTIDADE LIVENNIA)
// -------------------------------------------------------------
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
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                border = BorderStroke(1.dp, CardObsidianBorda),
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
                            .size(42.dp, 4.dp)
                            .background(Color.White.copy(alpha = 0.22f), CircleShape)
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = RoxoPrimario.copy(alpha = 0.15f),
                            shape = CircleShape,
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

                    // CARD 1: CENTELHA CRIATIVA (ANTIGO SPARK - CARA DO LIVENNIA)
                    Surface(
                        color = Color(0xFF19162B),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, RoxoPrimario.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEscolherSpark() }
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
                                        .background(RoxoPrimario.copy(alpha = 0.20f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = RoxoPrimario,
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
                                            color = DouradoLivenn.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
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
                                tint = RoxoPrimario,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // DUAS OPÇÕES MENORES: ESTÚDIO DE PERSONAGEM & ARQUITETO DE MUNDOS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Estúdio de Personagem (Express)
                        Surface(
                            color = Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEscolherRapido() }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = DouradoLivenn,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Estúdio Visual", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextoClaro)
                                Text("Aparência e traços essenciais", fontSize = 11.sp, color = TextoSecundario)
                            }
                        }

                        // Arquiteto de Mundos (Manual)
                        Surface(
                            color = Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEscolherManual() }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF64B5F6),
                                    modifier = Modifier.size(22.dp)
                                )
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
                                autorNome = vm.apelido.ifBlank { "Você" },
                                autorAvatar = "⚡",
                                chats = 0,
                                saves = 0,
                                modoJogo = modoNarrativa
                            )
                            RepositorioPersonagensComunidade.adicionar(novoCenario)
                            onConcluido(novoCenario)
                            Toast.makeText(contexto, "História '$finalNome' criada e publicada!", Toast.LENGTH_LONG).show()
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
    var estiloImagem by remember { mutableStateOf("Anime") }
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

                    // Estilo de Arte
                    Column {
                        Text("Estilo Visual", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            estilos.forEach { est ->
                                val sel = est == estiloImagem
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.20f) else CardObsidian,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier.clickable { estiloImagem = est }
                                ) {
                                    Text(
                                        est,
                                        color = if (sel) Color.White else TextoSecundario,
                                        fontSize = 12.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
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
                                autorNome = vm.apelido.ifBlank { "Você" },
                                autorAvatar = "🎨",
                                chats = 0,
                                saves = 0,
                                modoJogo = modoJogoEscolhido
                            )
                            RepositorioPersonagensComunidade.adicionar(novoCenario)
                            onConcluido(novoCenario)
                            Toast.makeText(contexto, "História '$finalNome' publicada com sucesso!", Toast.LENGTH_LONG).show()
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
                            RepositorioPersonagensComunidade.adicionar(novoCenario)
                            onConcluido(novoCenario)
                            Toast.makeText(contexto, "Narrativa publicada com sucesso!", Toast.LENGTH_LONG).show()
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
// 6. FICHA DETALHADA DO PERSONAGEM (SEM DADOS FALSOS + ENTRADA DIRETA NO CHAT)
// -------------------------------------------------------------
@Composable
fun DetalhesPersonagemModal(
    cenario: CenarioDto,
    versaoCapa: Int,
    apelidoUsuario: String = "Você",
    onFechar: () -> Unit,
    onIniciarChat: () -> Unit
) {
    val contexto = LocalContext.current
    var salvo by remember { mutableStateOf(false) }
    var novoComentario by remember { mutableStateOf("") }
    // Comentários reais (iniciam zerados sem métricas falsas)
    val comentarios = remember { mutableStateListOf<Pair<String, String>>() }
    var mostrarPerfilCriador by remember { mutableStateOf(false) }

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
                // Conteúdo rolável
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Pôster superior com botão voltar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                    ) {
                        CapaCenario(
                            cenarioId = cenario.id,
                            temCapa = cenario.capa,
                            emoji = cenario.emojiSeguro,
                            nome = cenario.nome,
                            versao = versaoCapa,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Degradê suave LivennIA
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Transparent,
                                            FundoOled
                                        )
                                    )
                                )
                        )

                        // Barra Superior da Capa
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.60f),
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                IconButton(onClick = onFechar) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                                }
                            }

                            // Selo do Modo Decidido pelo Criador
                            Surface(
                                color = if (cenario.modoSeguro == "rpg") Color(0xFF651FFF).copy(alpha = 0.85f) else RoxoPrimario.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Text(
                                    text = if (cenario.modoSeguro == "rpg") "⚔️ Modo RPG" else "📜 Modo Leitura",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Informações no rodapé da capa (dados transparentes e reais)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = cenario.nome,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "${cenario.saves} salvamentos • ${cenario.chats} conversas • ${cenario.generoSeguro}",
                                color = TextoSecundario,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Card do Criador (Ao clicar abre informações do criador)
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { mostrarPerfilCriador = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(RoxoPrimario.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (cenario.criadoPorUsuario) cenario.autorAvatarSeguro else "🤖", fontSize = 20.sp)
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (cenario.criadoPorUsuario) cenario.autorNomeSeguro else "LivennIA Studios",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextoClaro
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(if (cenario.criadoPorUsuario) "✨" else "✦", fontSize = 12.sp)
                                }
                                Text(
                                    text = if (cenario.criadoPorUsuario) "Criador da Comunidade • Toque para ver" else "História Oficial da IA",
                                    color = if (cenario.criadoPorUsuario) DouradoLivenn else RoxoPrimario,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextoMudo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Sinopse e Gancho de Abertura
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "\"${cenario.subtituloSeguro.ifBlank { cenario.nome }}\"",
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = cenario.sinopseSegura,
                            color = TextoSecundario,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        if (cenario.aberturaSegura.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = RoxoPrimario.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, RoxoPrimario.copy(alpha = 0.22f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        "Prólogo da Narrativa:",
                                        color = RoxoPrimario,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "“${cenario.aberturaSegura}”",
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 13.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // BOTÃO PRINCIPAL: ENTRAR NO CHAT DIRETAMENTE (Modo definido pelo criador)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onIniciarChat,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (cenario.modoSeguro == "rpg") "Continuar no Modo RPG" else "Continuar História",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        // Botão de Salvar/Favoritar
                        Surface(
                            color = if (salvo) DouradoLivenn.copy(alpha = 0.20f) else CardObsidian,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (salvo) DouradoLivenn else CardObsidianBorda),
                            modifier = Modifier
                                .size(54.dp)
                                .clickable {
                                    salvo = !salvo
                                    Toast.makeText(
                                        contexto,
                                        if (salvo) "Salvo nos seus favoritos!" else "Removido dos favoritos",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (salvo) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Favoritar",
                                    tint = if (salvo) DouradoLivenn else TextoSecundario
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = CardObsidianBorda, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(16.dp))

                    // SEÇÃO DE COMENTÁRIOS DA COMUNIDADE (ZERADA POR PADRÃO, SEM NÚMEROS FALSOS)
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Comentários da Comunidade (${comentarios.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextoClaro
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Campo de Comentário
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = CardObsidian,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CardObsidianBorda),
                                modifier = Modifier.weight(1f)
                            ) {
                                BasicTextField(
                                    value = novoComentario,
                                    onValueChange = { novoComentario = it },
                                    textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                    cursorBrush = SolidColor(RoxoPrimario),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    decorationBox = { inner ->
                                        if (novoComentario.isEmpty()) {
                                            Text("Deixe sua opinião sobre esta história...", color = TextoMudo, fontSize = 13.sp)
                                        }
                                        inner()
                                    }
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (novoComentario.isNotBlank()) {
                                        comentarios.add(0, (apelidoUsuario.ifBlank { "Você" } to novoComentario.trim()))
                                        novoComentario = ""
                                        Toast.makeText(contexto, "Comentário publicado!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Surface(
                                    color = RoxoPrimario,
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Lista de Comentários ou Estado Vazio
                        if (comentarios.isEmpty()) {
                            Surface(
                                color = Color.White.copy(alpha = 0.02f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TextoMudo, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Nenhum comentário ainda.", color = TextoSecundario, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Participe da narrativa e seja o primeiro a deixar suas impressões!", color = TextoMudo, fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                comentarios.forEach { (autor, texto) ->
                                    Surface(
                                        color = CardObsidian,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, CardObsidianBorda),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(autor, fontWeight = FontWeight.Bold, color = DouradoLivenn, fontSize = 12.sp)
                                            Spacer(Modifier.height(2.dp))
                                            Text(texto, color = TextoClaro, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    // Modal de Perfil do Criador
    if (mostrarPerfilCriador) {
        Dialog(onDismissRequest = { mostrarPerfilCriador = false }) {
            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, RoxoPrimario.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(RoxoPrimario.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (cenario.criadoPorUsuario) cenario.autorAvatarSeguro else "🤖", fontSize = 32.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = if (cenario.criadoPorUsuario) cenario.autorNomeSeguro else "LivennIA Studios",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    Text(
                        text = if (cenario.criadoPorUsuario) "Criador Comunitário" else "Inteligência Artificial Oficial",
                        color = DouradoLivenn,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = if (cenario.criadoPorUsuario)
                            "Este autor publicou histórias originais no LivennIA. Ao continuar uma narrativa criada por ele, você joga exatamente no formato configurado (${if (cenario.modoSeguro == "rpg") "RPG de Aventura" else "História Narrativa"})."
                        else
                            "História oficial gerada pelos motores centrais do LivennIA.",
                        color = TextoSecundario,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = {
                            mostrarPerfilCriador = false
                            onIniciarChat()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Entrar na História do Criador", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
