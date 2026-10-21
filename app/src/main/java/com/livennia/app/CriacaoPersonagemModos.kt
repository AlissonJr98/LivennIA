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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
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

private val FundoOled = Color(0xFF0A0A0C)
private val CardObsidian = Color(0xFF141418)
private val CardObsidianBorda = Color(0xFF282830)
private val DouradoLivenn = Color(0xFFF5C842)
private val AmareloBotao = Color(0xFFFFEB3B)
private val TextoClaro = Color(0xFFF0F0F2)
private val TextoSecundario = Color(0xFFA0A0AA)
private val TextoMudo = Color(0xFF6E6E78)

// -------------------------------------------------------------
// 1. REPOSITÓRIO DINÂMICO DE PERSONAGENS DA COMUNIDADE
// -------------------------------------------------------------
object RepositorioPersonagensComunidade {
    val criacoes = mutableStateListOf(
        CenarioDto(
            id = "user_sheila",
            nome = "Empresária Saudita (Sheila)",
            subtitulo = "Negócios no deserto",
            emoji = "💼",
            genero = "Cidade Moderna",
            tags = listOf("Romance", "Mistério", "Poder"),
            sinopse = "Você é filho de um executivo americano do setor de petróleo e gás. Sheila administra um império com frieza, mas esconde segredos pessoais que só você conhece.",
            abertura = "O relatório está pronto? Meu tempo é a coisa mais cara deste prédio, então seja convincente.",
            capa = false,
            criadoPorUsuario = true,
            autorNome = "Victor M.",
            autorAvatar = "👨‍💼",
            chats = 38400,
            saves = 226
        ),
        CenarioDto(
            id = "user_nana",
            nome = "Nana.",
            subtitulo = "Café de sábado à noite",
            emoji = "☕",
            genero = "Vida Escolar",
            tags = listOf("Amizade", "Drama", "Slice of Life"),
            sinopse = "Eu e meus dois amigos, Vito e Ryan, fomos a um café para um rolê tranquilo de sábado à noite. O movimento estava baixo até que ela se aproximou da nossa mesa.",
            abertura = "Oi... desculpa incomodar, mas você não é o amigo do Ryan que toca violão?",
            capa = false,
            criadoPorUsuario = true,
            autorNome = "Clara Luz",
            autorAvatar = "👩‍🎨",
            chats = 40500,
            saves = 196
        ),
        CenarioDto(
            id = "user_idol_mansion",
            nome = "Idol Mansion (K-Pop)",
            subtitulo = "Reality show de ídolos",
            emoji = "🎤",
            genero = "Romance & Drama",
            tags = listOf("K-Pop", "Reality", "Música"),
            sinopse = "O mundo inteiro parou quando o novo reality show sul-coreano 'Idol Mansion: The Ultimate Reality' foi anunciado. Convivência, treinos intensos e rivalidades nos bastidores.",
            abertura = "As câmeras já estão ligadas. Tente manter o foco durante o ensaio de hoje.",
            capa = false,
            criadoPorUsuario = true,
            autorNome = "íris rayana 🌈",
            autorAvatar = "✨",
            chats = 15200,
            saves = 45
        ),
        CenarioDto(
            id = "user_mamae",
            nome = "Mamãe Protetora",
            subtitulo = "Cuidado e família",
            emoji = "🌸",
            genero = "Vida Diária",
            tags = listOf("Família", "Acolhimento"),
            sinopse = "Ela é sua amada mãe, sempre esteve ali por você te apoiando nas escolhas difíceis e guardando histórias do seu passado.",
            abertura = "Filho, que bom que você chegou! Sente-se, acabei de tirar o jantar do forno.",
            capa = false,
            criadoPorUsuario = true,
            autorNome = "Beatriz",
            autorAvatar = "🌷",
            chats = 54300,
            saves = 258
        )
    )

    fun adicionar(cenario: CenarioDto) {
        criacoes.add(0, cenario)
    }
}

// -------------------------------------------------------------
// 2. MODAL DE ESCOLHA DO MODO DE CRIAÇÃO (Imagem 3)
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
                .background(Color.Black.copy(alpha = 0.70f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, CardObsidianBorda),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Puxador central
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(36.dp, 4.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Criar personagem",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextoSecundario
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Escolha seu modo de criação preferido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )

                    Spacer(Modifier.height(18.dp))

                    // CARD 1: MODO SPARK (DESTAQUE)
                    Surface(
                        color = Color(0xFF1E1D10),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF423B15)),
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
                                        .size(46.dp)
                                        .background(Color(0xFF2C260D), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = AmareloBotao,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column {
                                    Text(
                                        "Modo Spark",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextoClaro
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "Seu personagem ideal pronto em 20 segundos...",
                                        fontSize = 12.sp,
                                        color = TextoSecundario,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AmareloBotao.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Criar",
                                    tint = AmareloBotao,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // DUAS OPÇÕES MENORES: MODO RÁPIDO & MODO MANUAL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Modo Rápido
                        Surface(
                            color = Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(14.dp),
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
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Modo rápido", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextoClaro)
                                Text("Preenchimento guiado", fontSize = 11.sp, color = TextoSecundario)
                            }
                        }

                        // Modo Manual
                        Surface(
                            color = Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(14.dp),
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
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Modo manual", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextoClaro)
                                Text("Criação avançada", fontSize = 11.sp, color = TextoSecundario)
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
// 3. WIZARD MODO SPARK EM 4 PASSOS (Imagem 5)
// -------------------------------------------------------------
@Composable
fun WizardModoSpark(
    vm: AppViewModel,
    onFechar: () -> Unit,
    onConcluido: (CenarioDto) -> Unit
) {
    var passoAtual by remember { mutableIntStateOf(1) }
    var tropaSelecionada by remember { mutableStateOf("Possuído por alguém perigoso e hostil") }
    var generoSelecionado by remember { mutableStateOf("Feminino") }
    var tomConversa by remember { mutableStateOf("Misterioso e provocador") }
    var nomePersonagem by remember { mutableStateOf("") }

    val contexto = LocalContext.current

    val tropasEmocionais = remember {
        listOf(
            "Possuído por alguém perigoso e hostil",
            "Eles me imploram para voltar",
            "Sou eu quem pode salvar um 'them' quebrado e danificado",
            "Dividido entre mim e um rival — eles me escolhem",
            "Um romance doce e sem drama",
            "Eu domo alguém orgulhoso e intocável",
            "Eu me levanto — e todos olham para mim"
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
                    .padding(18.dp)
            ) {
                // Barra de Progresso Superior
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

                    // Linha de Progresso (Passo 1/4)
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
                                .fillMaxWidth(passoAtual / 4f)
                                .background(AmareloBotao, CircleShape)
                        )
                    }

                    Text(
                        "$passoAtual/4",
                        color = TextoMudo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(18.dp))

                when (passoAtual) {
                    1 -> {
                        // PASSO 1: TEMA / TROPE EMOCIONAL
                        Text(
                            text = "Que Tipo De História Faz Seu Coração Acelerar?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text(
                            text = "(Escolha Uma opção)",
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
                                    color = if (selecionado) Color(0xFF262410) else CardObsidian,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selecionado) AmareloBotao else CardObsidianBorda
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tropaSelecionada = tropa }
                                ) {
                                    Text(
                                        text = tropa,
                                        color = if (selecionado) AmareloBotao else TextoClaro,
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
                            text = "Qual é o gênero do seu personagem?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text("Defina a identidade de quem irá interagir com você.", color = TextoSecundario, fontSize = 12.sp)

                        Spacer(Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("Feminino", "Masculino", "Não binário").forEach { g ->
                                val sel = g == generoSelecionado
                                Surface(
                                    color = if (sel) Color(0xFF262410) else CardObsidian,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (sel) AmareloBotao else CardObsidianBorda),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { generoSelecionado = g }
                                ) {
                                    Text(
                                        text = g,
                                        color = if (sel) AmareloBotao else TextoClaro,
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
                            text = "Qual é o tom da personalidade?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text("Como o personagem reage e conversa nos momentos mais íntimos.", color = TextoSecundario, fontSize = 12.sp)

                        Spacer(Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(
                                "Misterioso e provocador",
                                "Doce, carinhoso e protetor",
                                "Frio, calculista e reservado",
                                "Intenso, possessivo e apaixonado"
                            ).forEach { tom ->
                                val sel = tom == tomConversa
                                Surface(
                                    color = if (sel) Color(0xFF262410) else CardObsidian,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (sel) AmareloBotao else CardObsidianBorda),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tomConversa = tom }
                                ) {
                                    Text(
                                        text = tom,
                                        color = if (sel) AmareloBotao else TextoClaro,
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
                        // PASSO 4: NOME DO PERSONAGEM
                        Text(
                            text = "Qual é o nome do seu personagem?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro
                        )
                        Text("Dê um nome marcante ou deixe a IA sugerir.", color = TextoSecundario, fontSize = 12.sp)

                        Spacer(Modifier.height(24.dp))

                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = nomePersonagem,
                                onValueChange = { nomePersonagem = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                cursorBrush = SolidColor(AmareloBotao),
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

                        // Sugestão Rápida
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

                // Botão de Avançar
                Button(
                    onClick = {
                        if (passoAtual < 4) {
                            passoAtual++
                        } else {
                            // Criação finalizada!
                            val finalNome = nomePersonagem.trim().ifBlank { "Personagem Spark" }
                            val novoCenario = CenarioDto(
                                id = "spark_" + System.currentTimeMillis(),
                                nome = finalNome,
                                subtitulo = tropaSelecionada.take(35),
                                emoji = if (generoSelecionado == "Feminino") "🌸" else "⚡",
                                genero = "Romance & Fantasia",
                                tags = listOf(generoSelecionado, "Modo Spark", tomConversa.take(15)),
                                sinopse = "Criado com dinâmica '$tropaSelecionada'. Personalidade: $tomConversa.",
                                abertura = "Você finalmente veio... Eu sabia que nossos caminhos se cruzariam outra vez.",
                                criadoPorUsuario = true,
                                autorNome = vm.apelido.ifBlank { "Você" },
                                autorAvatar = "⚡",
                                chats = 1,
                                saves = 1
                            )
                            RepositorioPersonagensComunidade.adicionar(novoCenario)
                            onConcluido(novoCenario)
                            Toast.makeText(contexto, "Personagem '$finalNome' criado no Modo Spark!", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmareloBotao),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        if (passoAtual < 4) "Continuar" else "✨ Gerar Personagem",
                        color = Color.Black,
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
// 4. MODO RÁPIDO (Imagem 4: Upload, Visual, Estilos)
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
    var nome by remember { mutableStateOf("") }

    val estilos = listOf("Anime", "Dark Fantasy", "Webtoon", "Cyberpunk")

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
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onFechar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextoClaro)
                    }
                    Text(
                        "Criar personagem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Caixa de Imagem de Referência
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable {
                                Toast.makeText(contexto, "Selecione uma imagem de referência na galeria.", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextoMudo, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Imagens de referência", color = TextoSecundario, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("*Um avatar semelhante será gerado com base nesta imagem", color = TextoMudo, fontSize = 10.sp)
                        }
                    }

                    // Nome
                    Column {
                        Text("Nome do Personagem", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = nome,
                                onValueChange = { nome = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(AmareloBotao),
                                modifier = Modifier.padding(12.dp),
                                decorationBox = { inner ->
                                    if (nome.isEmpty()) Text("Ex: Aoi Takahashi", color = TextoMudo, fontSize = 13.sp)
                                    inner()
                                }
                            )
                        }
                    }

                    // Descrição da imagem
                    Column {
                        Text("Descrição da imagem", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))

                        // Gênero Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Masculino", "Feminino", "Não binário").forEach { g ->
                                val sel = g == genero
                                Surface(
                                    color = if (sel) AmareloBotao.copy(alpha = 0.2f) else CardObsidian,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (sel) AmareloBotao else CardObsidianBorda),
                                    modifier = Modifier.clickable { genero = g }
                                ) {
                                    Text(
                                        text = g,
                                        color = if (sel) AmareloBotao else TextoSecundario,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = descricaoVisual,
                                onValueChange = { descricaoVisual = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(AmareloBotao),
                                modifier = Modifier.padding(12.dp),
                                decorationBox = { inner ->
                                    if (descricaoVisual.isEmpty()) {
                                        Text(
                                            "(Opcional) Conte o que deve aparecer na imagem: rosto, corpo, poses, expressões, roupa, fundo, etc.",
                                            color = TextoMudo,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    // Estilo de Imagem
                    Column {
                        Text("Estilo de imagem", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            estilos.forEach { est ->
                                val sel = est == estiloImagem
                                Surface(
                                    color = if (sel) AmareloBotao.copy(alpha = 0.2f) else CardObsidian,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (sel) AmareloBotao else CardObsidianBorda),
                                    modifier = Modifier.clickable { estiloImagem = est }
                                ) {
                                    Text(
                                        est,
                                        color = if (sel) AmareloBotao else TextoClaro,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                // Botão Gerar
                Button(
                    onClick = {
                        val finalNome = nome.trim().ifBlank { "Personagem Rápido" }
                        val novo = CenarioDto(
                            id = "rapido_" + System.currentTimeMillis(),
                            nome = finalNome,
                            subtitulo = "Estilo $estiloImagem • $genero",
                            emoji = "🎭",
                            genero = estiloImagem,
                            tags = listOf(genero, estiloImagem, "Criado por Usuário"),
                            sinopse = descricaoVisual.ifBlank { "Personagem criado com arte visual em estilo $estiloImagem." },
                            abertura = "Olá! Fico feliz que você tenha me criado. Sobre o que gostaria de conversar?",
                            criadoPorUsuario = true,
                            autorNome = vm.apelido.ifBlank { "Você" },
                            autorAvatar = "🎨",
                            chats = 0,
                            saves = 0
                        )
                        RepositorioPersonagensComunidade.adicionar(novo)
                        onConcluido(novo)
                        Toast.makeText(contexto, "Personagem '$finalNome' gerado com sucesso!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmareloBotao),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Gerar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. MODO MANUAL COMPLETO (Nome, Ficha, Primeira Mensagem, Visibilidade)
// -------------------------------------------------------------
@Composable
fun ModalModoManual(
    vm: AppViewModel,
    onFechar: () -> Unit,
    onConcluido: (CenarioDto) -> Unit
) {
    val contexto = LocalContext.current
    var nome by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("Feminino") }
    var categoria by remember { mutableStateOf("Cidade Moderna") }
    var sinopse by remember { mutableStateOf("") }
    var abertura by remember { mutableStateOf("") }
    var publicoNaComunidade by remember { mutableStateOf(true) }

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
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onFechar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextoClaro)
                    }
                    Text(
                        "Criação Manual de Personagem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nome
                    Column {
                        Text("Nome do Personagem", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        CampoBasicoObsidian(valor = nome, onValorChange = { nome = it }, placeholder = "Ex: Lady Valéria")
                    }

                    // Categoria
                    Column {
                        Text("Categoria / Tema", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Cidade Moderna", "Vida Escolar", "Fantasia", "Romance & Drama", "Terror & Suspense").forEach { cat ->
                                val sel = cat == categoria
                                Surface(
                                    color = if (sel) AmareloBotao.copy(alpha = 0.2f) else CardObsidian,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (sel) AmareloBotao else CardObsidianBorda),
                                    modifier = Modifier.clickable { categoria = cat }
                                ) {
                                    Text(
                                        cat,
                                        color = if (sel) AmareloBotao else TextoSecundario,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Sinopse
                    Column {
                        Text("História / Sinopse do Personagem", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        CampoBasicoObsidian(
                            valor = sinopse,
                            onValorChange = { sinopse = it },
                            placeholder = "Explique quem é esse personagem, sua relação com o usuário e o contexto inicial...",
                            linhas = 3
                        )
                    }

                    // Primeira Mensagem (Abertura)
                    Column {
                        Text("Primeira Mensagem (Abertura do Chat)", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        CampoBasicoObsidian(
                            valor = abertura,
                            onValorChange = { abertura = it },
                            placeholder = "A primeira fala do personagem quando o jogador iniciar a conversa...",
                            linhas = 3
                        )
                    }

                    // Visibilidade
                    Column {
                        Text("Visibilidade", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = if (publicoNaComunidade) AmareloBotao.copy(alpha = 0.2f) else CardObsidian,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (publicoNaComunidade) AmareloBotao else CardObsidianBorda),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { publicoNaComunidade = true }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🌐 Público", fontWeight = FontWeight.Bold, color = if (publicoNaComunidade) AmareloBotao else TextoClaro, fontSize = 13.sp)
                                    Text("Aparece no Explorar da Comunidade", color = TextoMudo, fontSize = 10.sp)
                                }
                            }

                            Surface(
                                color = if (!publicoNaComunidade) AmareloBotao.copy(alpha = 0.2f) else CardObsidian,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (!publicoNaComunidade) AmareloBotao else CardObsidianBorda),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { publicoNaComunidade = false }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🔒 Privado", fontWeight = FontWeight.Bold, color = if (!publicoNaComunidade) AmareloBotao else TextoClaro, fontSize = 13.sp)
                                    Text("Apenas para suas conversas", color = TextoMudo, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                // Botão Salvar
                Button(
                    onClick = {
                        val finalNome = nome.trim().ifBlank { "Personagem Manual" }
                        val novo = CenarioDto(
                            id = "manual_" + System.currentTimeMillis(),
                            nome = finalNome,
                            subtitulo = categoria,
                            emoji = "📜",
                            genero = categoria,
                            tags = listOf(genero, categoria, if (publicoNaComunidade) "Comunidade" else "Privado"),
                            sinopse = sinopse.ifBlank { "Personagem personalizado criado manualmente." },
                            abertura = abertura.ifBlank { "Olá... que bom ter você aqui." },
                            criadoPorUsuario = true,
                            autorNome = vm.apelido.ifBlank { "Você" },
                            autorAvatar = "👤",
                            chats = 0,
                            saves = 0
                        )
                        if (publicoNaComunidade) {
                            RepositorioPersonagensComunidade.adicionar(novo)
                        }
                        onConcluido(novo)
                        Toast.makeText(contexto, "Personagem '$finalNome' salvo com sucesso!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmareloBotao),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Salvar Personagem", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. TELA DE DETALHES DO PERSONAGEM / HISTÓRIA (Imagem 2)
// -------------------------------------------------------------
@Composable
fun DetalhesPersonagemModal(
    cenario: CenarioDto,
    versaoCapa: Int,
    onFechar: () -> Unit,
    onIniciarChat: () -> Unit
) {
    val contexto = LocalContext.current
    var salvo by remember { mutableStateOf(false) }
    var novoComentario by remember { mutableStateOf("") }
    val comentarios = remember { mutableStateListOf<String>() }

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
                    // Pôster em tela cheia superior com botão voltar e menu
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        CapaCenario(
                            cenarioId = cenario.id,
                            temCapa = cenario.capa,
                            emoji = cenario.emoji,
                            nome = cenario.nome,
                            versao = versaoCapa,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Degradê
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

                        // Botões de Ação na barra superior da Capa
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onFechar) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                            }
                            IconButton(onClick = { Toast.makeText(contexto, "Opções compartilhadas", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Mais", tint = Color.White)
                            }
                        }

                        // Informação de saves no rodapé da capa
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
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${if (cenario.saves > 0) cenario.saves else 45} saves • ${cenario.genero}",
                                color = TextoSecundario,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Card do Criador (Conforme Imagem 2)
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar do Criador
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(DouradoLivenn.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (cenario.criadoPorUsuario) cenario.autorAvatar.ifBlank { "👤" } else "🤖", fontSize = 18.sp)
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (cenario.criadoPorUsuario) cenario.autorNome else "LivennIA Studios",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextoClaro
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (cenario.criadoPorUsuario) "🌈" else "✦", fontSize = 12.sp)
                                }
                                Text(
                                    text = if (cenario.criadoPorUsuario) "Criador Comunitário" else "Inteligência Artificial Oficial",
                                    color = if (cenario.criadoPorUsuario) DouradoLivenn else Color(0xFF64B5F6),
                                    fontSize = 10.sp,
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

                    // Sinopse e Prólogo
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "\"${cenario.subtitulo.ifBlank { cenario.nome }}\"",
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = cenario.sinopse,
                            color = TextoSecundario,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        if (cenario.abertura.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.03f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "“${cenario.abertura}”",
                                    color = DouradoLivenn,
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Botões de Ação: Chat Amarelo + Salvar (Imagem 2)
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
                                .height(52.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = AmareloBotao)
                        ) {
                            Text(
                                "Chat",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }

                        // Botão Salvar
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier
                                .size(52.dp)
                                .clickable {
                                    salvo = !salvo
                                    Toast.makeText(contexto, if (salvo) "Personagem salvo nos favoritos!" else "Removido dos favoritos", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (salvo) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Salvar",
                                    tint = if (salvo) DouradoLivenn else TextoClaro,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(Modifier.height(14.dp))

                    // Seção de Comentários (Imagem 2)
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "${comentarios.size} Comments",
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro,
                            fontSize = 14.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        if (comentarios.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛋️", fontSize = 28.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Nenhum comentário ainda", color = TextoMudo, fontSize = 12.sp)
                            }
                        } else {
                            comentarios.forEach { c ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.03f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(c, color = TextoClaro, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Campo "Comente agora!"
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = novoComentario,
                                    onValueChange = { novoComentario = it },
                                    textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                    cursorBrush = SolidColor(AmareloBotao),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { inner ->
                                        if (novoComentario.isEmpty()) Text("Comente agora!", color = TextoMudo, fontSize = 13.sp)
                                        inner()
                                    }
                                )

                                IconButton(
                                    onClick = {
                                        if (novoComentario.isNotBlank()) {
                                            comentarios.add(novoComentario.trim())
                                            novoComentario = ""
                                            Toast.makeText(contexto, "Comentário publicado!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = novoComentario.isNotBlank(),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = if (novoComentario.isNotBlank()) AmareloBotao else TextoMudo, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CampoBasicoObsidian(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    linhas: Int = 1
) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        BasicTextField(
            value = valor,
            onValueChange = onValorChange,
            textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
            cursorBrush = SolidColor(AmareloBotao),
            maxLines = linhas,
            modifier = Modifier.padding(12.dp),
            decorationBox = { inner ->
                if (valor.isEmpty()) Text(placeholder, color = TextoMudo, fontSize = 12.sp)
                inner()
            }
        )
    }
}
