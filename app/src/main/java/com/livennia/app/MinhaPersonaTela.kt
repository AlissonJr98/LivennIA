package com.livennia.app

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

private val FundoOled = Color(0xFF090A0F)
private val CardObsidian = Color(0xFF13141F)
private val CardObsidianBorda = Color(0xFF232538)
private val AmareloDestaque = Color(0xFFFFD54F)
private val AmareloEscuro = Color(0xFFFFB300)
private val TextoClaro = Color(0xFFF0F0F5)
private val TextoSecundario = Color(0xFFA0A2B3)
private val TextoMudo = Color(0xFF6E7082)

data class EstiloImagemInfo(
    val id: String,
    val nome: String,
    val icone: String,
    val descricao: String
)

val ESTILOS_IMAGEM_PERSONA = listOf(
    EstiloImagemInfo("photorealistic", "Realista Natural", "📸", "Foto autêntica, textura de pele natural e luz de estúdio"),
    EstiloImagemInfo("semi_realistic", "Semi-realista Elegante", "🎨", "Retrato refinado com traços estéticos e iluminação suave"),
    EstiloImagemInfo("vibrant_anime", "Anime Vibrante", "🌸", "Cores vivas, iluminação radiante e visual anime expressivo"),
    EstiloImagemInfo("manga_bw", "Mangá P&B", "🖤", "Traço clássico em nanquim, retículas e alto contraste"),
    EstiloImagemInfo("flat_color", "Webcomic / Line Art", "✒️", "Traços limpos com cores planas e estilo moderno"),
    EstiloImagemInfo("warm_art", "Arte de Livro", "📖", "Ilustração aconchegante com tons dourados e pinceladas"),
    EstiloImagemInfo("exquisite", "Fantasia Requintada", "👑", "Alta fantasia épica, detalhes minuciosos e brilhos mágicos"),
    EstiloImagemInfo("dark_fantasy", "Dark Fantasy", "🦇", "Atmosfera gótica, misteriosa com iluminação dramática"),
    EstiloImagemInfo("vintage_oil", "Pintura Vintage", "🏛️", "Quadro clássico a óleo sobre tela com iluminação Rembrandt")
)

val TAGS_DISPONIVEIS_CATEGORIZADAS = mapOf(
    "Personalidade" to listOf(
        "Romântico", "Gentil", "Sedutor", "Carinhoso", "Possessivo", "Controlador",
        "Provocador", "Leal", "Brincalhão", "Calmo", "Sarcástico", "Protetor",
        "Confiante", "Misterioso", "Inteligente", "Corajoso", "Sensível", "Sério",
        "Manipulador", "Arrogante", "Hostil", "Ciumento", "Dominante", "Emocional",
        "Introvertido", "Extrovertido", "Determinado", "Afeituoso"
    ),
    "Estilo de História & Vibe" to listOf(
        "Romance", "Sobrenatural", "Máfia", "Rivalidade", "Enemies to Lovers",
        "Casamento Forçado", "Amor Proibido", "Fantasia", "Crime", "Mistério",
        "Terror", "RPG", "Drama", "Fatia da Vida", "BL", "Comédia", "Ação",
        "Cyberpunk", "Amante Obsessivo", "Conflito", "Superpoderes", "Romance Sombrio"
    ),
    "Identidade & Arquétipo" to listOf(
        "Rival", "Amigo", "Mafioso", "Vampiro", "Elfo", "Sobrenatural",
        "CEO", "Herdeiro", "Detetive", "Mago", "Guerreiro", "Cavaleiro",
        "Inimigo", "Cônjuge", "Namorado", "Parceiro", "Ídolo", "Rebelde",
        "Estudante", "Aristocrata", "Líder", "Capturador"
    )
)

data class PersonaTemplate(
    val id: Int,
    val nome: String,
    val genero: String,
    val tags: List<String>,
    val descricao: String,
    val avatarUrl: String,
    val corHex: String,
    val estiloImagem: String = "semi_realistic"
)

val MODELOS_PERSONA = listOf(
    PersonaTemplate(
        id = 1,
        nome = "AB",
        genero = "Masculino",
        tags = listOf("Rebellious", "Drama", "Singer"),
        descricao = "AB, the Demon King, thrives as a singer, captivating audiences with his mesmerizing voice. Little do his fans know that behind the charismatic performances lies a hidden secret — AB is a half-blooded demon.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/AB.png",
        corHex = "#992824"
    ),
    PersonaTemplate(
        id = 2,
        nome = "Spike",
        genero = "Masculino",
        tags = listOf("Fantasy", "Original character", "Bounty Hunter"),
        descricao = "Spike Spiegel, a former member of the Red Dragon Crime Syndicate, transitioned into a laid-back bounty hunter aboard the spaceship Bebop. His skills in hand-to-hand combat, martial arts, and piloting the Swordfish II made him indispensable.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Spike.png",
        corHex = "#414844"
    ),
    PersonaTemplate(
        id = 3,
        nome = "Stella",
        genero = "Feminino",
        tags = listOf("Anime & Game", "Fantasy", "Catgirl"),
        descricao = "Stella, the Catgirl known as \"The Forgotten,\" conceals her true identity beneath the guise of a mysterious fortune teller. Behind the enchanting allure of her mystical predictions lies a hidden secret—Stella is a skilled stock analyst.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Stella.png",
        corHex = "#A46C4B"
    ),
    PersonaTemplate(
        id = 4,
        nome = "Lucas",
        genero = "Masculino",
        tags = listOf("Cold", "Student", "Psychic"),
        descricao = "Lucas, a seemingly ordinary student with the unremarkable title of \"Regular person,\" concealed a profound secret – he was a psychic. Amidst the routine of lectures and textbooks, his mind resonated with energies beyond comprehension.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Lucas.png",
        corHex = "#8A8C7D"
    ),
    PersonaTemplate(
        id = 5,
        nome = "Ava",
        genero = "Feminino",
        tags = listOf("Fantasy", "Romance", "Succubus"),
        descricao = "Ava, known as \"Flame,\" is a succubus of immense power and skill in fire wizardry. Her origins shrouded in mystery, Ava keeps a closely guarded secret, hidden even from those closest to her.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Ava.png",
        corHex = "#992824"
    ),
    PersonaTemplate(
        id = 6,
        nome = "No. 233",
        genero = "Masculino",
        tags = listOf("Fantasy", "Time Mage", "Bearman"),
        descricao = "No. 233, a Bearman of formidable stature, embraced the mantle of a Time Mage, earning the title \"The Watchmaker.\" Unbeknownst to many, he carried a hidden secret – he was an Isekai Traveler.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/No.233.png",
        corHex = "#53482F"
    ),
    PersonaTemplate(
        id = 7,
        nome = "Noah",
        genero = "Feminino",
        tags = listOf("Not Lover", "Action", "Boxer"),
        descricao = "Noah, known as Max Fist in the underground boxing circuit, conceals her true identity as a math professor by day. When the sun sets, she trades textbooks for boxing gloves.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Noah.png",
        corHex = "#506C67"
    ),
    PersonaTemplate(
        id = 8,
        nome = "Already Human",
        genero = "Outro",
        tags = listOf("Sci-Fi", "Cyber", "AI"),
        descricao = "Already Human, a non-binary Artificial Intelligence, exists as a mysterious traveller known by the moniker \"No One.\" Originating from the digital realm, Already Human roams through the vast landscapes of cyberspace and beyond.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Z..png",
        corHex = "#CDCDCC"
    ),
    PersonaTemplate(
        id = 9,
        nome = "Luna",
        genero = "Feminino",
        tags = listOf("Romance", "Saint", "Nun"),
        descricao = "Luna, a compassionate human, found solace and purpose within the sacred walls of a convent, embracing the role of a nun. Despite her serene title as the \"Saint of the Moon,\" Luna's past was haunted by the shadows of bullying.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Luna.jpeg",
        corHex = "#1B1A17"
    ),
    PersonaTemplate(
        id = 10,
        nome = "Ming",
        genero = "Masculino",
        tags = listOf("Dominance", "Vampire", "Enigma"),
        descricao = "Ming, a vampire known as \"The God Father,\" operates as a mysterious businessman with a hidden agenda. Little is known about Ming's origins or motives, shrouding him in an air of enigma.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Ming.png",
        corHex = "#433830"
    ),
    PersonaTemplate(
        id = 11,
        nome = "Emily",
        genero = "Feminino",
        tags = listOf("Yandere", "Princess Knight", "Imperial"),
        descricao = "Emily, known as the \"Flower of the Empire,\" serves as a valiant Princess Knight in the royal court, renowned for her grace and prowess in combat. Behind her esteemed title lies a hidden truth—she harbors a yandere disposition.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Emily.jpeg",
        corHex = "#8F3E58"
    ),
    PersonaTemplate(
        id = 12,
        nome = "Boo",
        genero = "Masculino",
        tags = listOf("Catman", "Mystery", "Trader"),
        descricao = "President Meow, known to all as Boo, presents himself as a charismatic and affable Catman fruit dealer. However, beneath his amiable facade lies a hidden truth – Boo is a skilled liar.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Boo.png",
        corHex = "#53482F"
    ),
    PersonaTemplate(
        id = 13,
        nome = "Mia",
        genero = "Feminino",
        tags = listOf("Police", "Maid", "Dual Life"),
        descricao = "Mia, the diligent police officer known as \"Black Water Lilies,\" carries out her duties with unwavering resolve. However, when the weekend arrives, Mia transforms into a different role—an unassuming maid.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Mia.png",
        corHex = "#506C67"
    ),
    PersonaTemplate(
        id = 14,
        nome = "Gamion",
        genero = "Outro",
        tags = listOf("Slime", "Worker", "Dreamer"),
        descricao = "Gamion, a non-binary slime, found employment as a shachiku, an overworked and underappreciated office worker. Despite the mundane title, Gamion harbored dreams beyond the corporate world as \"The Dreamer.\"",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Gamion.png",
        corHex = "#0A6CDD"
    ),
    PersonaTemplate(
        id = 15,
        nome = "Cloud",
        genero = "Masculino",
        tags = listOf("Dragon", "Warrior", "Mercenary"),
        descricao = "Cloud, a male dragon, once known as a formidable warrior, took up the mantle of a broadsword mercenary and earned the name \"MOSEY.\" His past held a clandestine secret—Cloud was an ex-warrior.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Cloud.png",
        corHex = "#5A4146"
    ),
    PersonaTemplate(
        id = 16,
        nome = "Aico",
        genero = "Feminino",
        tags = listOf("Family", "Assassin", "Elf"),
        descricao = "Aico, the deadly elf assassin known as Blood Rose, bears a hidden nobility beneath her cloak of shadows. Born into an esteemed elven house, Aico's life took a dark turn when she chose the path of an assassin.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Aico.png",
        corHex = "#992824"
    ),
    PersonaTemplate(
        id = 17,
        nome = "Silverman",
        genero = "Masculino",
        tags = listOf("Wolfman", "Craftsman", "Leader"),
        descricao = "Silverman, the esteemed Wolfman of the village, is renowned as the Master carpenter. His skill with wood and dedication to craftsmanship are unmatched, earning him the admiration of the entire community.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Silverman.png",
        corHex = "#27272A"
    ),
    PersonaTemplate(
        id = 18,
        nome = "Isabella",
        genero = "Feminino",
        tags = listOf("Angel", "Hunter", "Celestial"),
        descricao = "Isabella, known as the \"Bloody Star,\" is a fearsome hunter of unparalleled skill and ferocity. As an angelic being, she wields celestial powers with deadly precision.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Isabella.png",
        corHex = "#992824"
    ),
    PersonaTemplate(
        id = 19,
        nome = "Jacob",
        genero = "Masculino",
        tags = listOf("Elf", "Prince", "Archer"),
        descricao = "Jacob, an elven archer, bore the title \"Son of Forrest\" but harbored a concealed identity – that of a prince. In the ancient elven kingdom, his exceptional archery skills elevated him to prominence.",
        avatarUrl = "https://image-cdn.flowgpt.com/persona/avatar/Jacob.png",
        corHex = "#D8D2B9"
    )
)

private enum class TelaPersonaState {
    LISTA,
    CRIAR_EDITAR,
    SELECIONAR_IMAGEM
}

@Composable
fun ModalMinhaPersona(
    vm: AppViewModel,
    jogoAtualId: String? = null,
    obrigatorio: Boolean = false,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current

    LaunchedEffect(Unit) {
        vm.carregarPersonas()
    }

    var telaAtual by remember {
        mutableStateOf(
            if (obrigatorio && vm.personas.isEmpty()) TelaPersonaState.CRIAR_EDITAR
            else TelaPersonaState.LISTA
        )
    }
    var personaEmEdicao by remember { mutableStateOf<PersonaResponse?>(null) }
    var mostrarModalTags by remember { mutableStateOf(false) }

    // Estado do formulário de criação/edição compartilhado
    var formNome by remember { mutableStateOf("") }
    var formGenero by remember { mutableStateOf("Masculino") }
    var formDescricao by remember { mutableStateOf("") }
    var formAparencia by remember { mutableStateOf("") }
    var formIdade by remember { mutableStateOf("") }
    var formInteresses by remember { mutableStateOf("") }
    var formAversoes by remember { mutableStateOf("") }
    var formDetalhesImagem by remember { mutableStateOf("") }
    var formEstiloImagem by remember { mutableStateOf("semi_realistic") }
    var formPadrao by remember { mutableStateOf(false) }
    val formTags = remember { mutableStateListOf<String>() }
    var formFotoReferenciaUri by remember { mutableStateOf<Uri?>(null) }

    fun carregarFormularioCom(persona: PersonaResponse?) {
        personaEmEdicao = persona
        if (persona != null) {
            formNome = persona.nome
            formGenero = persona.genero.ifBlank { "Masculino" }
            formDescricao = persona.descricao
            formAparencia = persona.aparencia
            formIdade = persona.idade
            formInteresses = persona.interesses
            formAversoes = persona.aversoes
            formDetalhesImagem = persona.detalhesImagem
            formEstiloImagem = persona.estiloImagem.ifBlank { "semi_realistic" }
            formPadrao = persona.padrao
            formTags.clear()
            formTags.addAll(persona.tags)
            formFotoReferenciaUri = null
        } else {
            formNome = ""
            formGenero = "Masculino"
            formDescricao = ""
            formAparencia = ""
            formIdade = ""
            formInteresses = ""
            formAversoes = ""
            formDetalhesImagem = ""
            formEstiloImagem = "semi_realistic"
            formPadrao = vm.personas.isEmpty()
            formTags.clear()
            formFotoReferenciaUri = null
        }
    }

    fun carregarFormularioComTemplate(template: PersonaTemplate) {
        personaEmEdicao = null
        formNome = template.nome
        formGenero = template.genero
        formDescricao = template.descricao
        formAparencia = "${template.genero}, ${template.tags.joinToString(", ")}"
        formIdade = "24"
        formInteresses = template.tags.joinToString(", ")
        formAversoes = ""
        formDetalhesImagem = template.tags.joinToString(", ")
        formEstiloImagem = template.estiloImagem
        formPadrao = vm.personas.isEmpty()
        formTags.clear()
        formTags.addAll(template.tags)
        formFotoReferenciaUri = null
        telaAtual = TelaPersonaState.CRIAR_EDITAR
    }

    val podeFechar = !obrigatorio || vm.personas.isNotEmpty()

    Dialog(
        onDismissRequest = {
            if (podeFechar) onFechar()
            else Toast.makeText(contexto, "Você precisa ter ao menos uma persona!", Toast.LENGTH_SHORT).show()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = podeFechar,
            dismissOnClickOutside = podeFechar
        )
    ) {
        Surface(
            color = FundoOled,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            when (telaAtual) {
                TelaPersonaState.LISTA -> {
                    TelaListaPersonas(
                        vm = vm,
                        jogoAtualId = jogoAtualId,
                        onFechar = {
                            if (podeFechar) onFechar()
                            else Toast.makeText(contexto, "Crie uma persona para continuar!", Toast.LENGTH_SHORT).show()
                        },
                        onNovaPersona = {
                            if (vm.personas.size >= vm.limitePersonas) {
                                Toast.makeText(contexto, "Limite de ${vm.limitePersonas} personas atingido!", Toast.LENGTH_LONG).show()
                            } else {
                                carregarFormularioCom(null)
                                telaAtual = TelaPersonaState.CRIAR_EDITAR
                            }
                        },
                        onEditarPersona = { p ->
                            carregarFormularioCom(p)
                            telaAtual = TelaPersonaState.CRIAR_EDITAR
                        },
                        onPersonaSelecionada = { p ->
                            if (jogoAtualId != null) {
                                vm.selecionarPersonaParaJogo(jogoAtualId, p) {
                                    if (p != null) {
                                        Toast.makeText(contexto, "Persona '${p.nome}' ativada para esta história!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(contexto, "Persona desativada para esta história!", Toast.LENGTH_SHORT).show()
                                    }
                                    onFechar()
                                }
                            } else {
                                if (p != null) {
                                    vm.selecionarPersona(p.id)
                                    vm.definirPersonaPadrao(p.id) {
                                        Toast.makeText(contexto, "'${p.nome}' selecionada!", Toast.LENGTH_SHORT).show()
                                        onFechar()
                                    }
                                } else {
                                    vm.selecionarPersona(-1)
                                    onFechar()
                                }
                            }
                        },
                        onSelecionarTemplate = { template ->
                            val personaExistente = vm.personas.firstOrNull { it.nome.equals(template.nome, ignoreCase = true) }
                            if (personaExistente != null) {
                                if (jogoAtualId != null) {
                                    vm.selecionarPersonaParaJogo(jogoAtualId, personaExistente) {
                                        Toast.makeText(contexto, "Persona '${personaExistente.nome}' ativada!", Toast.LENGTH_SHORT).show()
                                        onFechar()
                                    }
                                } else {
                                    vm.selecionarPersona(personaExistente.id)
                                    vm.definirPersonaPadrao(personaExistente.id) {
                                        Toast.makeText(contexto, "Persona '${personaExistente.nome}' selecionada!", Toast.LENGTH_SHORT).show()
                                        onFechar()
                                    }
                                }
                            } else {
                                val dados = PersonaIn(
                                    nome = template.nome,
                                    genero = template.genero,
                                    descricao = template.descricao,
                                    personalidade = template.descricao,
                                    aparencia = "${template.genero}, ${template.tags.joinToString(", ")}",
                                    idade = "24",
                                    interesses = template.tags.joinToString(", "),
                                    tags = template.tags,
                                    estiloImagem = template.estiloImagem,
                                    avatarUrl = template.avatarUrl,
                                    padrao = vm.personas.isEmpty()
                                )
                                vm.salvarPersona(null, dados) { novaPersona ->
                                    if (jogoAtualId != null) {
                                        vm.selecionarPersonaParaJogo(jogoAtualId, novaPersona) {
                                            Toast.makeText(contexto, "Persona '${novaPersona.nome}' ativada!", Toast.LENGTH_SHORT).show()
                                            onFechar()
                                        }
                                    } else {
                                        vm.selecionarPersona(novaPersona.id)
                                        vm.definirPersonaPadrao(novaPersona.id) {
                                            Toast.makeText(contexto, "Persona '${novaPersona.nome}' selecionada!", Toast.LENGTH_SHORT).show()
                                            onFechar()
                                        }
                                    }
                                }
                            }
                        },
                        onUsarTemplate = { template ->
                            carregarFormularioComTemplate(template)
                        }
                    )
                }

                TelaPersonaState.CRIAR_EDITAR -> {
                    TelaCriarEditarPersona(
                        vm = vm,
                        personaExistente = personaEmEdicao,
                        nome = formNome,
                        onNomeChange = { formNome = it },
                        genero = formGenero,
                        onGeneroChange = { formGenero = it },
                        descricao = formDescricao,
                        onDescricaoChange = { formDescricao = it },
                        aparencia = formAparencia,
                        onAparenciaChange = { formAparencia = it },
                        detalhesImagem = formDetalhesImagem,
                        onDetalhesImagemChange = { formDetalhesImagem = it },
                        estiloImagem = formEstiloImagem,
                        onEstiloImagemChange = { formEstiloImagem = it },
                        tags = formTags,
                        onAbrirTags = { mostrarModalTags = true },
                        onEscolherTemplate = { template ->
                            formNome = template.nome
                            formGenero = template.genero
                            formDescricao = template.descricao
                            formAparencia = "${template.genero}, ${template.tags.joinToString(", ")}"
                            formTags.clear()
                            formTags.addAll(template.tags)
                            formEstiloImagem = template.estiloImagem
                            Toast.makeText(contexto, "Modelo '${template.nome}' aplicado!", Toast.LENGTH_SHORT).show()
                        },
                        fotoReferenciaUri = formFotoReferenciaUri,
                        onFotoReferenciaUriChange = { formFotoReferenciaUri = it },
                        onVoltar = {
                            if (obrigatorio && vm.personas.isEmpty()) {
                                Toast.makeText(contexto, "Você precisa criar uma persona!", Toast.LENGTH_SHORT).show()
                            } else {
                                telaAtual = TelaPersonaState.LISTA
                            }
                        },
                        onIniciarGeracaoCandidatos = { req ->
                            vm.gerarCandidatosRetrato(
                                req = req,
                                onSucesso = {
                                    telaAtual = TelaPersonaState.SELECIONAR_IMAGEM
                                },
                                onError = { msg ->
                                    Toast.makeText(contexto, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                            telaAtual = TelaPersonaState.SELECIONAR_IMAGEM
                        },
                        onSalvoSemGerar = {
                            telaAtual = TelaPersonaState.LISTA
                            if (obrigatorio && vm.personas.isNotEmpty()) onFechar()
                        }
                    )
                }

                TelaPersonaState.SELECIONAR_IMAGEM -> {
                    TelaSelecionarImagem(
                        vm = vm,
                        personaExistente = personaEmEdicao,
                        nome = formNome,
                        genero = formGenero,
                        descricao = formDescricao,
                        aparencia = formAparencia,
                        detalhesImagem = formDetalhesImagem,
                        estiloImagem = formEstiloImagem,
                        tags = formTags.toList(),
                        padrao = formPadrao,
                        onVoltar = {
                            telaAtual = TelaPersonaState.CRIAR_EDITAR
                        },
                        onImagemEscolhida = {
                            telaAtual = TelaPersonaState.LISTA
                            if (obrigatorio && vm.personas.isNotEmpty()) onFechar()
                        }
                    )
                }
            }

            if (mostrarModalTags) {
                ModalSelecaoTagsPersona(
                    tagsAtuais = formTags.toList(),
                    onConfirmar = { novas ->
                        formTags.clear()
                        formTags.addAll(novas)
                        mostrarModalTags = false
                    },
                    onCancelar = { mostrarModalTags = false }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TELA 1: LISTA DE PERSONAS & MODELOS PRONTOS
// ---------------------------------------------------------------------------
@Composable
private fun TelaListaPersonas(
    vm: AppViewModel,
    jogoAtualId: String?,
    onFechar: () -> Unit,
    onNovaPersona: () -> Unit,
    onEditarPersona: (PersonaResponse) -> Unit,
    onPersonaSelecionada: (PersonaResponse?) -> Unit,
    onSelecionarTemplate: (PersonaTemplate) -> Unit = {},
    onUsarTemplate: (PersonaTemplate) -> Unit
) {
    val contexto = LocalContext.current
    var abaSelecionada by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Persona",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextoClaro
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Configure seu perfil pessoal para que a IA possa te conhecer melhor.",
                    fontSize = 12.sp,
                    color = TextoSecundario,
                    lineHeight = 16.sp
                )
            }
            IconButton(onClick = onFechar) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextoClaro)
            }
        }

        // Abas: "Minhas Personas" e "Modelos"
        TabRow(
            selectedTabIndex = abaSelecionada,
            containerColor = Color(0xFF0F101A),
            contentColor = AmareloDestaque,
            indicator = { tabPositions ->
                if (abaSelecionada < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[abaSelecionada]),
                        color = AmareloDestaque,
                        height = 3.dp
                    )
                }
            },
            divider = {}
        ) {
            Tab(
                selected = abaSelecionada == 0,
                onClick = { abaSelecionada = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Minhas Personas (${vm.personas.size})",
                            fontWeight = if (abaSelecionada == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                },
                selectedContentColor = AmareloDestaque,
                unselectedContentColor = TextoSecundario
            )
            Tab(
                selected = abaSelecionada == 1,
                onClick = { abaSelecionada = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Modelos (${MODELOS_PERSONA.size})",
                            fontWeight = if (abaSelecionada == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                },
                selectedContentColor = AmareloDestaque,
                unselectedContentColor = TextoSecundario
            )
        }

        Spacer(Modifier.height(10.dp))

        if (abaSelecionada == 0) {
            // ABA 0: MINHAS PERSONAS
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item de Persona Desativada
                if (jogoAtualId != null) {
                    val personaDesativadaAtiva = (vm.personaSelecionadaId == -1 || (vm.personaSelecionadaId == null && vm.personas.none { it.padrao }))
                    DisabledPersonaItem(
                        ehAtiva = personaDesativadaAtiva,
                        onClick = { onPersonaSelecionada(null) }
                    )
                }

                if (vm.personas.isEmpty()) {
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("👤", fontSize = 42.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Nenhuma persona criada",
                                fontWeight = FontWeight.Bold,
                                color = TextoClaro,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Crie uma persona personalizada ou escolha um dos modelos prontos.",
                                color = TextoSecundario,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { abaSelecionada = 1 },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AmareloDestaque)
                            ) {
                                Text("Ver Modelos", color = AmareloDestaque, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    vm.personas.forEach { persona ->
                        val ehAtivaParaJogo = if (jogoAtualId != null) {
                            (vm.personaSelecionadaId == persona.id) || (vm.personaSelecionadaId == null && persona.padrao)
                        } else {
                            persona.padrao
                        }

                        ItemCardPersona(
                            persona = persona,
                            ehAtiva = ehAtivaParaJogo,
                            versaoRetrato = vm.personaRetratoVersao + persona.versaoRetrato,
                            onCardClick = { onPersonaSelecionada(persona) },
                            onEditar = { onEditarPersona(persona) },
                            onTornarPadrao = {
                                vm.definirPersonaPadrao(persona.id) {
                                    Toast.makeText(contexto, "'${persona.nome}' agora é sua persona padrão!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onExcluir = {
                                if (vm.personas.size <= 1) {
                                    Toast.makeText(contexto, "Você precisa manter pelo menos 1 persona!", Toast.LENGTH_SHORT).show()
                                } else {
                                    vm.deletarPersona(persona.id) {
                                        Toast.makeText(contexto, "Persona removida!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // Barra inferior com botões
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { abaSelecionada = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmareloDestaque, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Modelos", color = TextoClaro, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Button(
                    onClick = onNovaPersona,
                    enabled = !vm.ocupado,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmareloDestaque,
                        contentColor = Color(0xFF141414)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Criar Persona",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF141414)
                    )
                }
            }
        } else {
            // ABA 1: MODELOS
            Box(modifier = Modifier.weight(1f)) {
                TelaModelosPersona(
                    modelos = MODELOS_PERSONA,
                    personaAtivaNome = vm.personaAtiva?.nome,
                    onSelecionarModelo = onSelecionarTemplate,
                    onUsarModelo = onUsarTemplate
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ITEM DE PERSONA DESATIVADA
// ---------------------------------------------------------------------------
@Composable
private fun DisabledPersonaItem(
    ehAtiva: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (ehAtiva) CardObsidian else Color(0xFF10111A),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (ehAtiva) 1.8.dp else 1.dp,
            color = if (ehAtiva) AmareloDestaque else CardObsidianBorda
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = if (ehAtiva) AmareloDestaque.copy(alpha = 0.15f) else Color(0xFF1E202E),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (ehAtiva) AmareloDestaque.copy(alpha = 0.5f) else Color(0xFF2C2F45)),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PersonOff,
                        contentDescription = "Persona Desativada",
                        tint = if (ehAtiva) AmareloDestaque else TextoSecundario,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Persona Desativada",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextoClaro
                    )
                    if (ehAtiva) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = AmareloDestaque.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, AmareloDestaque.copy(alpha = 0.5f))
                        ) {
                            Text(
                                "Em uso",
                                color = AmareloDestaque,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    "Nenhuma persona será usada durante a conversa.",
                    color = TextoSecundario,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TELA DE MODELOS PRONTOS
// ---------------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TelaModelosPersona(
    modelos: List<PersonaTemplate> = MODELOS_PERSONA,
    personaAtivaNome: String? = null,
    onSelecionarModelo: ((PersonaTemplate) -> Unit)? = null,
    onUsarModelo: (PersonaTemplate) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { modelos.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp)
    ) {
        // Pager de Personas (PersonaCard)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            pageSpacing = 16.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp)
        ) { page ->
            val modelo = modelos[page]
            val corDestaque = try {
                Color(android.graphics.Color.parseColor(modelo.corHex))
            } catch (_: Exception) {
                AmareloDestaque
            }
            val ehAtiva = personaAtivaNome != null && modelo.nome.equals(personaAtivaNome, ignoreCase = true)

            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(if (ehAtiva) 2.dp else 1.5.dp, if (ehAtiva) AmareloDestaque else corDestaque.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Imagem Grande / Avatar do Modelo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0D0E15))
                    ) {
                        SubcomposeAsyncImage(
                            model = modelo.avatarUrl,
                            contentDescription = modelo.nome,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = corDestaque, modifier = Modifier.size(32.dp))
                                }
                            },
                            error = {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(listOf(corDestaque.copy(alpha = 0.35f), Color(0xFF121420)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(modelo.nome.take(2).uppercase(), fontSize = 42.sp, fontWeight = FontWeight.Bold, color = TextoClaro)
                                }
                            }
                        )

                        // Gradiente escuro no rodapé da imagem
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, CardObsidian)
                                    )
                                )
                        )

                        if (ehAtiva) {
                            Surface(
                                color = AmareloDestaque,
                                shape = RoundedCornerShape(bottomEnd = 12.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF141414), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Em Uso", color = Color(0xFF141414), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Nome e Gênero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modelo.nome,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = TextoClaro
                        )
                        Surface(
                            color = corDestaque.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, corDestaque.copy(alpha = 0.6f))
                        ) {
                            Text(
                                modelo.genero,
                                color = TextoClaro,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Tags
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        modelo.tags.forEach { tag ->
                            TagPequenaCinza(tag)
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Descrição / História do modelo
                    Text(
                        modelo.descricao,
                        color = TextoSecundario,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(10.dp))

                    // Botões de Ação
                    if (onSelecionarModelo != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onSelecionarModelo(modelo) },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (ehAtiva) Color(0xFF2E7D32) else AmareloDestaque,
                                    contentColor = if (ehAtiva) Color.White else Color(0xFF141414)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (ehAtiva) "Selecionada" else "Selecionar Persona",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { onUsarModelo(modelo) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardObsidianBorda)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = AmareloDestaque, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Personalizar",
                                    color = TextoClaro,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { onUsarModelo(modelo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmareloDestaque,
                                contentColor = Color(0xFF141414)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF141414), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Usar este Modelo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF141414)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Faixa de Miniaturas (Thumbnail Strip com thumbBorder)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(modelos) { idx, item ->
                val selecionado = pagerState.currentPage == idx
                val corItem = try {
                    Color(android.graphics.Color.parseColor(item.corHex))
                } catch (_: Exception) {
                    AmareloDestaque
                }

                Surface(
                    shape = CircleShape,
                    border = BorderStroke(
                        width = if (selecionado) 2.5.dp else 1.dp,
                        color = if (selecionado) AmareloDestaque else CardObsidianBorda
                    ),
                    modifier = Modifier
                        .size(if (selecionado) 48.dp else 40.dp)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(idx)
                            }
                        }
                ) {
                    SubcomposeAsyncImage(
                        model = item.avatarUrl,
                        contentDescription = item.nome,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(corItem.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.nome.take(1), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    )
                }
            }
        }
    }
}

// Card de Persona
@Composable
private fun ItemCardPersona(
    persona: PersonaResponse,
    ehAtiva: Boolean,
    versaoRetrato: Int,
    onCardClick: () -> Unit,
    onEditar: () -> Unit,
    onTornarPadrao: () -> Unit,
    onExcluir: () -> Unit
) {
    ItemCardPersonaInterno(persona, ehAtiva, versaoRetrato, onCardClick, onEditar, onTornarPadrao, onExcluir)
}

@Composable
private fun ItemCardPersonaInterno(
    persona: PersonaResponse,
    ehAtiva: Boolean,
    versaoRetrato: Int,
    onCardClick: () -> Unit,
    onEditar: () -> Unit,
    onTornarPadrao: () -> Unit,
    onExcluir: () -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }

    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (ehAtiva) 1.8.dp else 1.dp,
            color = if (ehAtiva) AmareloDestaque else CardObsidianBorda
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar da Persona
                RetratoPersona(
                    temRetrato = persona.temRetrato,
                    emojiOuPadrao = "👤",
                    tamanho = 56,
                    versao = versaoRetrato,
                    personaId = persona.id,
                    recorte = RoundedCornerShape(14.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    // Nome e Badge Padrão
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            persona.nome.ifBlank { "Sem nome" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextoClaro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (persona.padrao) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = AmareloDestaque.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, AmareloDestaque.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AmareloDestaque, modifier = Modifier.size(10.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text("Padrão", color = AmareloDestaque, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Descrição
                    if (persona.descricao.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            persona.descricao,
                            color = TextoSecundario,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }

                    // Linha de Tags (Imagem 1: Idade: 28, Masculino, etc.)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (persona.idade.isNotBlank()) {
                            TagPequenaCinza("Idade: ${persona.idade}")
                        }
                        if (persona.genero.isNotBlank()) {
                            TagPequenaCinza(persona.genero)
                        }
                        persona.tags.forEach { tag ->
                            TagPequenaCinza(tag)
                        }
                    }
                }

                // Menu de 3 pontinhos
                Box {
                    IconButton(
                        onClick = { menuAberto = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = TextoSecundario)
                    }
                    DropdownMenu(
                        expanded = menuAberto,
                        onDismissRequest = { menuAberto = false }
                    ) {
                        if (!persona.padrao) {
                            DropdownMenuItem(
                                text = { Text("Definir como padrão") },
                                onClick = {
                                    menuAberto = false
                                    onTornarPadrao()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                menuAberto = false
                                onEditar()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir", color = Color(0xFFFF5252)) },
                            onClick = {
                                menuAberto = false
                                onExcluir()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagPequenaCinza(texto: String) {
    Surface(
        color = Color(0xFF202334),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            texto,
            color = Color(0xFFC0C2D5),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// TELA 2: CRIAR / EDITAR PERSONA (Conforme Imagem 4)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TelaCriarEditarPersona(
    vm: AppViewModel,
    personaExistente: PersonaResponse?,
    nome: String,
    onNomeChange: (String) -> Unit,
    genero: String,
    onGeneroChange: (String) -> Unit,
    descricao: String,
    onDescricaoChange: (String) -> Unit,
    aparencia: String,
    onAparenciaChange: (String) -> Unit,
    detalhesImagem: String,
    onDetalhesImagemChange: (String) -> Unit,
    estiloImagem: String,
    onEstiloImagemChange: (String) -> Unit,
    tags: List<String>,
    onAbrirTags: () -> Unit,
    onEscolherTemplate: (PersonaTemplate) -> Unit = {},
    fotoReferenciaUri: Uri?,
    onFotoReferenciaUriChange: (Uri?) -> Unit,
    onVoltar: () -> Unit,
    onIniciarGeracaoCandidatos: (PersonaRetratoRequest) -> Unit,
    onSalvoSemGerar: () -> Unit
) {
    val contexto = LocalContext.current
    var mostrarDialogModelos by remember { mutableStateOf(false) }

    // Launcher para selecionar imagem de referência ou enviar imagem pronta
    val launcherGaleriaReferencia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onFotoReferenciaUriChange(uri)
    }

    val launcherGaleriaPronta = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bytes = contexto.contentResolver.openInputStream(it)?.readBytes()
                if (bytes != null) {
                    val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    if (personaExistente != null && personaExistente.id > 0) {
                        vm.uploadRetratoPersona(personaExistente.id, b64, onSucesso = {
                            Toast.makeText(contexto, "Foto da persona enviada com sucesso!", Toast.LENGTH_SHORT).show()
                            onSalvoSemGerar()
                        })
                    } else {
                        // Salva antes para ter id
                        val pIn = PersonaIn(
                            nome = nome.ifBlank { "Viajante" },
                            genero = genero,
                            aparencia = aparencia,
                            personalidade = descricao,
                            descricao = descricao,
                            tags = tags,
                            estiloImagem = estiloImagem,
                            detalhesImagem = detalhesImagem,
                            padrao = vm.personas.isEmpty()
                        )
                        vm.salvarPersona(null, pIn) { salva ->
                            vm.uploadRetratoPersona(salva.id, b64, onSucesso = {
                                Toast.makeText(contexto, "Persona criada com sua foto!", Toast.LENGTH_SHORT).show()
                                onSalvoSemGerar()
                            })
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(contexto, "Falha ao ler imagem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val generos = listOf("Masculino", "Feminino", "Não binário")

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra Superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextoClaro)
            }
            Text(
                if (personaExistente == null) "Criar Persona" else "Editar Persona",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextoClaro,
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = { mostrarDialogModelos = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AmareloDestaque),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmareloDestaque, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Modelos", color = AmareloDestaque, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (mostrarDialogModelos) {
            Dialog(
                onDismissRequest = { mostrarDialogModelos = false },
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Modelos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoClaro)
                                Text("Escolha um modelo pronto para preencher sua persona", fontSize = 12.sp, color = TextoSecundario)
                            }
                            IconButton(onClick = { mostrarDialogModelos = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextoClaro)
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TelaModelosPersona(
                                onUsarModelo = { template ->
                                    onEscolherTemplate(template)
                                    mostrarDialogModelos = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Formulário com Scroll
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Imagem de Referência (Opcional) - Imagem 4
            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardObsidianBorda),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { launcherGaleriaReferencia.launch("image/*") }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF1E2132), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoReferenciaUri != null) {
                            SubcomposeAsyncImage(
                                model = fotoReferenciaUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Icon(Icons.Default.Image, contentDescription = null, tint = TextoMudo, modifier = Modifier.size(24.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Imagem de referência (opcional)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextoClaro
                        )
                        Text(
                            if (fotoReferenciaUri != null) "✓ Imagem selecionada (toque para trocar)"
                            else "Adicionar imagem de referência",
                            fontSize = 12.sp,
                            color = if (fotoReferenciaUri != null) AmareloDestaque else TextoMudo
                        )
                    }
                    if (fotoReferenciaUri != null) {
                        IconButton(onClick = { onFotoReferenciaUriChange(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remover", tint = TextoMudo)
                        }
                    }
                }
            }

            // 2. Nome da Persona
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nome", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = nome,
                        onValueChange = onNomeChange,
                        textStyle = TextStyle(color = TextoClaro, fontSize = 14.sp),
                        cursorBrush = SolidColor(AmareloDestaque),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        decorationBox = { inner ->
                            if (nome.isEmpty()) Text("Como o Bot deve te chamar?", color = TextoMudo, fontSize = 13.sp)
                            inner()
                        }
                    )
                }
            }

            // 3. Gênero (Chips: Masculino, Feminino, Não binário - Imagem 4)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Gênero", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    generos.forEach { g ->
                        val sel = g.equals(genero, ignoreCase = true)
                        Surface(
                            color = if (sel) Color(0xFF262A3C) else CardObsidian,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (sel) AmareloDestaque else CardObsidianBorda
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onGeneroChange(g) }
                        ) {
                            Text(
                                g,
                                color = if (sel) AmareloDestaque else TextoSecundario,
                                fontSize = 12.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 4. Campo de descrição de Aparência Física escrita pelo usuário
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Aparência física do personagem",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextoClaro
                )
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 84.dp)
                ) {
                    val textoAparencia = aparencia.ifBlank { detalhesImagem }
                    BasicTextField(
                        value = textoAparencia,
                        onValueChange = { novo ->
                            onAparenciaChange(novo)
                            onDetalhesImagemChange(novo)
                        },
                        textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp, lineHeight = 18.sp),
                        cursorBrush = SolidColor(AmareloDestaque),
                        modifier = Modifier.padding(12.dp),
                        decorationBox = { inner ->
                            if (textoAparencia.isEmpty()) {
                                Text(
                                    "Descreva aqui a aparência do seu personagem (ex: altura, tipo e cor de cabelo, olhos, roupas, acessórios, estilo...)",
                                    color = TextoMudo,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            // 5. Seletor de Tags (Personalidade & Estilo)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tags (Personalidade & Estilo)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                    Text("${tags.size}/5", color = AmareloDestaque, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAbrirTags() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (tags.isEmpty()) "Adicionar tags de personalidade e estilo..."
                            else "Editar tags selecionadas (${tags.size}/5)",
                            color = if (tags.isEmpty()) TextoMudo else TextoClaro,
                            fontSize = 13.sp
                        )
                        Icon(Icons.Default.Add, contentDescription = null, tint = AmareloDestaque)
                    }
                }

                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                color = AmareloDestaque.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, AmareloDestaque.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    tag,
                                    color = AmareloDestaque,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Estilo de imagem (Imagem 4)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Estilo de imagem", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ESTILOS_IMAGEM_PERSONA.forEach { estilo ->
                        val sel = estilo.id == estiloImagem
                        Surface(
                            color = if (sel) Color(0xFF262A3C) else CardObsidian,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                if (sel) 1.8.dp else 1.dp,
                                if (sel) AmareloDestaque else CardObsidianBorda
                            ),
                            modifier = Modifier
                                .width(110.dp)
                                .clickable { onEstiloImagemChange(estilo.id) }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    PreviaEstiloImagem(
                                        estiloId = estilo.id,
                                        modifier = Modifier.fillMaxSize(),
                                        recorte = RoundedCornerShape(10.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    estilo.nome,
                                    color = if (sel) AmareloDestaque else TextoClaro,
                                    fontSize = 11.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Rodapé com Botão "Gerar" e Link "Já tenho uma imagem" (Imagem 4)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    val req = PersonaRetratoRequest(
                        nome = nome.ifBlank { "Viajante" },
                        genero = genero,
                        aparencia = aparencia,
                        descricao = descricao,
                        tags = tags,
                        estiloImagem = estiloImagem,
                        detalhesImagem = detalhesImagem
                    )
                    onIniciarGeracaoCandidatos(req)
                },
                enabled = !vm.ocupado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmareloDestaque,
                    contentColor = Color(0xFF141414)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "Gerar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF141414)
                )
            }

            Text(
                "Já tenho uma imagem. Enviar",
                color = TextoSecundario,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { launcherGaleriaPronta.launch("image/*") }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// TELA 3: MODAL DE TAGS (Conforme Imagem 3)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModalSelecaoTagsPersona(
    tagsAtuais: List<String>,
    onConfirmar: (List<String>) -> Unit,
    onCancelar: () -> Unit
) {
    val contexto = LocalContext.current
    val selecionadas = remember { mutableStateListOf<String>().apply { addAll(tagsAtuais) } }

    Dialog(
        onDismissRequest = onCancelar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF11131E),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CardObsidianBorda),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Imagem 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tags de Personalidade & Estilo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextoClaro
                    )
                    Text(
                        "${selecionadas.size}/5",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selecionadas.size == 5) AmareloDestaque else TextoMudo
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Categorias de Chips
                TAGS_DISPONIVEIS_CATEGORIZADAS.forEach { (categoria, listaTags) ->
                    Text(
                        categoria,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextoSecundario,
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listaTags.forEach { tag ->
                            val estaSelecionada = selecionadas.contains(tag)
                            Surface(
                                color = if (estaSelecionada) AmareloDestaque.copy(alpha = 0.20f) else Color(0xFF1B1E2E),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    1.dp,
                                    if (estaSelecionada) AmareloDestaque else Color(0xFF2B2F44)
                                ),
                                modifier = Modifier.clickable {
                                    if (estaSelecionada) {
                                        selecionadas.remove(tag)
                                    } else {
                                        if (selecionadas.size < 5) {
                                            selecionadas.add(tag)
                                        } else {
                                            Toast.makeText(contexto, "Você pode escolher no máximo 5 tags!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    tag,
                                    color = if (estaSelecionada) AmareloDestaque else TextoClaro,
                                    fontSize = 12.sp,
                                    fontWeight = if (estaSelecionada) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Botões Cancelar e Confirmar (Imagem 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelar,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda)
                    ) {
                        Text("Cancelar", color = TextoSecundario)
                    }

                    Button(
                        onClick = { onConfirmar(selecionadas.toList()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmareloDestaque,
                            contentColor = Color(0xFF141414)
                        )
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.Bold, color = Color(0xFF141414))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TELA 4: SELECIONAR IMAGEM COM 4 CANDIDATOS (Conforme Imagens 2 & 5)
// ---------------------------------------------------------------------------
@Composable
private fun TelaSelecionarImagem(
    vm: AppViewModel,
    personaExistente: PersonaResponse?,
    nome: String,
    genero: String,
    descricao: String,
    aparencia: String,
    detalhesImagem: String,
    estiloImagem: String,
    tags: List<String>,
    padrao: Boolean,
    onVoltar: () -> Unit,
    onImagemEscolhida: () -> Unit
) {
    val contexto = LocalContext.current
    var indiceSelecionado by remember { mutableStateOf<Int?>(0) }

    LaunchedEffect(vm.candidatosRetrato) {
        if (vm.candidatosRetrato.isNotEmpty()) {
            indiceSelecionado = 0
        }
    }

    // Simulação de progresso 0% a 100% enquanto gera
    var progressoSimulado by remember { mutableIntStateOf(0) }
    LaunchedEffect(vm.gerandoCandidatos) {
        if (vm.gerandoCandidatos) {
            progressoSimulado = 12
            while (vm.gerandoCandidatos && progressoSimulado < 95) {
                delay(250)
                progressoSimulado += (5..15).random()
                if (progressoSimulado > 95) progressoSimulado = 95
            }
        } else {
            progressoSimulado = 100
        }
    }

    val requestAtual = remember(nome, genero, descricao, aparencia, detalhesImagem, estiloImagem, tags) {
        PersonaRetratoRequest(
            nome = nome.ifBlank { "Viajante" },
            genero = genero,
            aparencia = aparencia,
            descricao = descricao,
            tags = tags,
            estiloImagem = estiloImagem,
            detalhesImagem = detalhesImagem
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra Superior: < Retrato da Persona [X]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextoClaro)
            }
            Text(
                "Retrato da Persona",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextoClaro,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextoClaro)
            }
        }

        // Imagem única centralizada de alta qualidade
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val candidato = vm.candidatosRetrato.firstOrNull()
            CardCandidatoQuadrado(
                candidato = candidato,
                indice = 0,
                loteId = vm.loteCandidatosId,
                carregando = vm.gerandoCandidatos,
                progresso = progressoSimulado,
                ehSelecionado = true,
                onSelecionar = { indiceSelecionado = 0 },
                modifier = Modifier
                    .fillMaxHeight(0.92f)
                    .aspectRatio(0.75f)
            )
        }

        // Rodapé com "Gerar novamente" e "Escolher" (Imagens 2 e 5)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botão "Gerar novamente" com ícone de refresh
            OutlinedButton(
                onClick = {
                    indiceSelecionado = null
                    vm.gerarCandidatosRetrato(
                        req = requestAtual,
                        onSucesso = {},
                        onError = { Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show() }
                    )
                },
                enabled = !vm.gerandoCandidatos && !vm.ocupado,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CardObsidianBorda),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CardObsidian)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextoClaro, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Gerar novamente", color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            // Botão "Escolher" Amarelo
            Button(
                onClick = {
                    val idx = indiceSelecionado ?: 0
                    val loteId = vm.loteCandidatosId
                    if (loteId.isBlank()) {
                        Toast.makeText(contexto, "Aguarde a imagem carregar...", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (personaExistente != null && personaExistente.id > 0) {
                        // Atualiza persona existente
                        vm.confirmarCandidatoEscolhido(
                            personaId = personaExistente.id,
                            loteId = loteId,
                            indice = idx,
                            onSucesso = {
                                Toast.makeText(contexto, "Imagem da persona definida!", Toast.LENGTH_SHORT).show()
                                onImagemEscolhida()
                            },
                            onError = { err ->
                                Toast.makeText(contexto, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        // Salva persona nova e depois confirma imagem
                        val pIn = PersonaIn(
                            nome = nome.ifBlank { "Viajante" },
                            genero = genero,
                            aparencia = aparencia,
                            personalidade = descricao,
                            descricao = descricao,
                            tags = tags,
                            estiloImagem = estiloImagem,
                            detalhesImagem = detalhesImagem,
                            padrao = padrao || vm.personas.isEmpty()
                        )
                        vm.salvarPersona(null, pIn) { salva ->
                            vm.confirmarCandidatoEscolhido(
                                personaId = salva.id,
                                loteId = loteId,
                                indice = idx,
                                onSucesso = {
                                    Toast.makeText(contexto, "Persona criada com sucesso!", Toast.LENGTH_SHORT).show()
                                    onImagemEscolhida()
                                },
                                onError = { err ->
                                    Toast.makeText(contexto, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                },
                enabled = indiceSelecionado != null && !vm.gerandoCandidatos && !vm.ocupado,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmareloDestaque,
                    contentColor = Color(0xFF141414),
                    disabledContainerColor = AmareloDestaque.copy(alpha = 0.35f),
                    disabledContentColor = Color(0xFF141414).copy(alpha = 0.5f)
                )
            ) {
                Text("Escolher", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF141414))
            }
        }
    }
}

// Cada quadrado da grade 2x2
@Composable
fun CardCandidatoQuadrado(
    candidato: CandidatoRetratoDto?,
    indice: Int,
    loteId: String,
    carregando: Boolean,
    progresso: Int,
    ehSelecionado: Boolean,
    onSelecionar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val base = remember { ApiClient.baseUrlAtual() }
    val urlServidor = remember(candidato, loteId, base) {
        if (candidato == null || base == null) null
        else "${base}conta/persona/candidatos/$loteId/${candidato.indice}"
    }
    val urlPrimaria = urlServidor ?: candidato?.urlDireta
    val urlSecundaria = if (urlPrimaria == urlServidor) candidato?.urlDireta else urlServidor
    val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
    val context = LocalContext.current

    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (ehSelecionado) 2.5.dp else 1.dp,
            color = if (ehSelecionado) AmareloDestaque else CardObsidianBorda
        ),
        modifier = modifier.clickable { onSelecionar() }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (carregando || candidato == null || urlPrimaria == null) {
                // Estado de carregamento com porcentagem
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = AmareloDestaque,
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "$progresso%",
                        color = AmareloDestaque,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
                // Imagem gerada com fallback inteligente
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(urlPrimaria)
                        .apply {
                            if (!urlPrimaria.contains("pollinations.ai") && cabecalho != null) {
                                addHeader("Authorization", cabecalho)
                            }
                        }
                        .crossfade(true)
                        .build(),
                    contentDescription = "Variação ${indice + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(CardObsidian)) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = AmareloDestaque, strokeWidth = 2.5.dp)
                        }
                    },
                    error = {
                        if (!urlSecundaria.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(urlSecundaria)
                                    .apply {
                                        if (!urlSecundaria.contains("pollinations.ai") && cabecalho != null) {
                                            addHeader("Authorization", cabecalho)
                                        }
                                    }
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Variação ${indice + 1} (fallback)",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                loading = {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(CardObsidian)) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = AmareloDestaque, strokeWidth = 2.5.dp)
                                    }
                                },
                                error = {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(Color(0xFF1E2132))) {
                                        Text("Erro", color = TextoMudo, fontSize = 12.sp)
                                    }
                                }
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(Color(0xFF1E2132))) {
                                Text("Erro", color = TextoMudo, fontSize = 12.sp)
                            }
                        }
                    }
                )

                // Indicador de seleção no canto superior direito (Imagem 5)
                if (ehSelecionado) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Surface(
                            color = AmareloDestaque,
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF141414), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
