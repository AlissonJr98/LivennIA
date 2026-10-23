package com.livennia.app

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val FundoOled = Color(0xFF090A0F)
private val CardObsidian = Color(0xFF13141F)
private val CardObsidianBorda = Color(0xFF232538)
private val RoxoPrimario = Color(0xFF7C4DFF)
private val OuroAmbar = Color(0xFFFFB300)
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
    EstiloImagemInfo("manga_bw", "Mangá Preto & Branco", "🖤", "Traço clássico em nanquim, retículas e alto contraste"),
    EstiloImagemInfo("flat_color", "Line Art / Webcomic", "✒️", "Traços limpos com cores planas e estilo moderno"),
    EstiloImagemInfo("warm_art", "Arte Quente de Livro", "📖", "Ilustração aconchegante com tons dourados e pinceladas"),
    EstiloImagemInfo("exquisite", "Fantasia Requintada", "👑", "Alta fantasia épica, detalhes minuciosos e brilhos mágicos"),
    EstiloImagemInfo("dark_fantasy", "Dark Fantasy", "🦇", "Atmosfera gótica, misteriosa com iluminação dramática"),
    EstiloImagemInfo("vintage_oil", "Pintura a Óleo Vintage", "🏛️", "Quadro clássico a óleo sobre tela com iluminação Rembrandt")
)

@Composable
fun ModalMinhaPersona(
    vm: AppViewModel,
    obrigatorio: Boolean = false,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current

    LaunchedEffect(Unit) {
        vm.carregarPersonas()
    }

    var modoEdicao by remember { mutableStateOf(obrigatorio && vm.personas.isEmpty()) }
    var personaEmEdicao by remember { mutableStateOf<PersonaResponse?>(null) }

    LaunchedEffect(vm.personas.size) {
        if (obrigatorio && vm.personas.isEmpty()) {
            modoEdicao = true
        }
    }

    val podeFechar = !obrigatorio || vm.personas.isNotEmpty()

    Dialog(
        onDismissRequest = {
            if (podeFechar) onFechar()
            else Toast.makeText(contexto, "Você precisa criar pelo menos uma persona para começar!", Toast.LENGTH_SHORT).show()
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
            if (modoEdicao) {
                FormularioPersona(
                    vm = vm,
                    personaExistente = personaEmEdicao,
                    obrigatorio = obrigatorio && vm.personas.isEmpty(),
                    onVoltar = {
                        if (obrigatorio && vm.personas.isEmpty()) {
                            Toast.makeText(contexto, "Você precisa criar sua persona antes de continuar!", Toast.LENGTH_SHORT).show()
                        } else {
                            modoEdicao = false
                            personaEmEdicao = null
                        }
                    },
                    onSalvo = {
                        modoEdicao = false
                        personaEmEdicao = null
                        if (obrigatorio) onFechar()
                    }
                )
            } else {
                ListaPersonas(
                    vm = vm,
                    obrigatorio = obrigatorio && vm.personas.isEmpty(),
                    onFechar = {
                        if (podeFechar) onFechar()
                        else Toast.makeText(contexto, "Você precisa criar pelo menos uma persona para começar!", Toast.LENGTH_SHORT).show()
                    },
                    onNovaPersona = {
                        if (vm.personas.size >= vm.limitePersonas) {
                            Toast.makeText(
                                contexto,
                                "Limite de ${vm.limitePersonas} personas atingido no seu plano!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            personaEmEdicao = null
                            modoEdicao = true
                        }
                    },
                    onEditarPersona = { p ->
                        personaEmEdicao = p
                        modoEdicao = true
                    }
                )
            }
        }
    }
}

@Composable
private fun ListaPersonas(
    vm: AppViewModel,
    obrigatorio: Boolean = false,
    onFechar: () -> Unit,
    onNovaPersona: () -> Unit,
    onEditarPersona: (PersonaResponse) -> Unit
) {
    val contexto = LocalContext.current
    val totalPersonas = vm.personas.size
    val limite = vm.limitePersonas

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onFechar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextoClaro)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Minhas Personas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextoClaro
                )
                Text(
                    "Protagonistas usados nas histórias e chats",
                    fontSize = 11.sp,
                    color = TextoSecundario
                )
            }

            // Badge de Cota
            Surface(
                color = if (totalPersonas >= limite) Color(0xFFD32F2F).copy(alpha = 0.2f) else RoxoPrimario.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (totalPersonas >= limite) Color(0xFFFF5252) else RoxoPrimario.copy(alpha = 0.4f)),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    "$totalPersonas / $limite",
                    color = if (totalPersonas >= limite) Color(0xFFFF5252) else RoxoPrimario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Lista com scroll
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "A Persona selecionada como padrão será o seu personagem em qualquer nova conversa ou aventura.",
                color = TextoMudo,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

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
                        Text("🎭", fontSize = 42.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Nenhuma persona criada",
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Crie sua primeira persona para definir quem você é nas histórias!",
                            color = TextoSecundario,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                vm.personas.forEach { persona ->
                    ItemCardPersona(
                        persona = persona,
                        versao = vm.personaRetratoVersao,
                        onEditar = { onEditarPersona(persona) },
                        onTornarPadrao = {
                            vm.definirPersonaPadrao(persona.id) {
                                Toast.makeText(contexto, "'${persona.nome}' agora é sua persona padrão!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onExcluir = {
                            if (vm.personas.size <= 1) {
                                Toast.makeText(contexto, "Você precisa ter pelo menos 1 persona!", Toast.LENGTH_SHORT).show()
                            } else {
                                vm.deletarPersona(persona.id) {
                                    Toast.makeText(contexto, "Persona removida!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
        }

        // Botão de Criar Nova Persona
        Box(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onNovaPersona,
                enabled = !vm.ocupado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Criar Nova Persona",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun ItemCardPersona(
    persona: PersonaResponse,
    versao: Int,
    onEditar: () -> Unit,
    onTornarPadrao: () -> Unit,
    onExcluir: () -> Unit
) {
    val pressInteraction = remember { MutableInteractionSource() }
    val isPressed by pressInteraction.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "personaCardScale"
    )

    Surface(
        color = Color(0xFF131522),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 8.dp,
        border = BorderStroke(
            if (persona.padrao) 1.5.dp else 1.2.dp,
            if (persona.padrao) Brush.horizontalGradient(listOf(OuroAmbar, Color(0xFFFFD54F), OuroAmbar))
            else Brush.verticalGradient(listOf(Color(0xFF7C4DFF).copy(alpha = 0.5f), Color(0xFF232538)))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RetratoPersona(
                    temRetrato = persona.temRetrato,
                    emojiOuPadrao = "👤",
                    tamanho = 56,
                    versao = versao + persona.versaoRetrato,
                    personaId = persona.id,
                    recorte = CircleShape
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            persona.nome.ifBlank { "Sem nome" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextoClaro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (persona.padrao) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = OuroAmbar.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, OuroAmbar.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = OuroAmbar, modifier = Modifier.size(11.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text("Padrão", color = OuroAmbar, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (persona.idade.isNotBlank()) {
                            Text("${persona.idade} anos", color = TextoMudo, fontSize = 11.sp)
                            Text("•", color = TextoMudo, fontSize = 11.sp)
                        }
                        Text(persona.genero.ifBlank { "Gênero livre" }, color = TextoMudo, fontSize = 11.sp)
                    }

                    if (persona.descricao.isNotBlank()) {
                        Text(
                            persona.descricao,
                            color = TextoSecundario,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Ações do card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!persona.padrao) {
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier.clickable { onTornarPadrao() }
                    ) {
                        Text(
                            "Definir como Padrão",
                            color = TextoSecundario,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Text("✓ Usada por padrão no Bot", color = OuroAmbar, fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextoClaro, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onExcluir, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioPersona(
    vm: AppViewModel,
    personaExistente: PersonaResponse?,
    obrigatorio: Boolean = false,
    onVoltar: () -> Unit,
    onSalvo: () -> Unit
) {
    val contexto = LocalContext.current

    var nome by remember { mutableStateOf(personaExistente?.nome ?: "") }
    var genero by remember { mutableStateOf(personaExistente?.genero ?: "Feminino") }
    var idade by remember { mutableStateOf(personaExistente?.idade ?: "") }
    var descricao by remember { mutableStateOf(personaExistente?.descricao ?: "") } // Personalidade
    var aparencia by remember { mutableStateOf(personaExistente?.aparencia ?: "") }
    var interesses by remember { mutableStateOf(personaExistente?.interesses ?: "") }
    var aversoes by remember { mutableStateOf(personaExistente?.aversoes ?: "") }
    var tags by remember { mutableStateOf(personaExistente?.tags?.joinToString(", ") ?: "") }
    var detalhesImagem by remember { mutableStateOf(personaExistente?.detalhesImagem ?: "") }
    var estiloImagem by remember { mutableStateOf(personaExistente?.estiloImagem ?: "semi_realistic") }
    var padrao by remember { mutableStateOf(personaExistente?.padrao ?: (vm.personas.isEmpty())) }

    var temRetratoLocal by remember(personaExistente) { mutableStateOf(personaExistente?.temRetrato == true) }
    var versaoRetratoLocal by remember(personaExistente) { mutableStateOf(personaExistente?.versaoRetrato ?: 0) }
    var personaIdLocal by remember(personaExistente) { mutableStateOf(personaExistente?.id) }
    var gerandoRetratoLocal by remember { mutableStateOf(false) }
    var mostrarExemploEstilo by remember { mutableStateOf(!temRetratoLocal) }

    LaunchedEffect(vm.ocupado) {
        if (!vm.ocupado && gerandoRetratoLocal) {
            gerandoRetratoLocal = false
        }
    }

    val generos = listOf("Feminino", "Masculino", "Outro")

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!obrigatorio || vm.personas.isNotEmpty()) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextoClaro)
                }
            } else {
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (obrigatorio) "Crie sua Persona (Obrigatório)"
                    else if (personaExistente == null) "Nova Persona" else "Editar Persona",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextoClaro
                )
                if (obrigatorio) {
                    Text(
                        "O Bot conversará com você através desta Persona",
                        fontSize = 11.sp,
                        color = OuroAmbar
                    )
                }
            }
            Surface(
                color = RoxoPrimario.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RoxoPrimario, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        // Formulário
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Retrato & Botão de Geração com IA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(102.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!mostrarExemploEstilo && temRetratoLocal && personaIdLocal != null && personaIdLocal!! > 0) {
                        RetratoPersona(
                            temRetrato = true,
                            emojiOuPadrao = "👤",
                            tamanho = 96,
                            versao = vm.personaRetratoVersao + versaoRetratoLocal,
                            personaId = personaIdLocal,
                            recorte = CircleShape
                        )
                    } else {
                        PreviaEstiloImagem(
                            estiloId = estiloImagem,
                            modifier = Modifier.size(96.dp),
                            recorte = CircleShape
                        )
                    }

                    if (gerandoRetratoLocal || vm.ocupado) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = RoxoPrimario,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Surface(
                    color = if (!mostrarExemploEstilo && temRetratoLocal) OuroAmbar.copy(alpha = 0.15f) else RoxoPrimario.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (!mostrarExemploEstilo && temRetratoLocal) OuroAmbar.copy(alpha = 0.4f) else RoxoPrimario.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 6.dp).clickable(enabled = temRetratoLocal) {
                        mostrarExemploEstilo = !mostrarExemploEstilo
                    }
                ) {
                    Text(
                        if (!mostrarExemploEstilo && temRetratoLocal) "✓ Meu Retrato (toque p/ estilo)" else "🎨 Estilo: ${ESTILOS_IMAGEM_PERSONA.firstOrNull { it.id == estiloImagem }?.nome ?: estiloImagem}",
                        color = if (!mostrarExemploEstilo && temRetratoLocal) OuroAmbar else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Button(
                    onClick = {
                        if (nome.isBlank()) {
                            Toast.makeText(contexto, "Digite o nome da persona antes de gerar o retrato!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val listaTags = if (tags.isNotBlank()) tags.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList()
                        val req = PersonaRetratoRequest(
                            nome = nome,
                            genero = genero,
                            aparencia = aparencia,
                            descricao = descricao,
                            idade = idade,
                            interesses = interesses,
                            aversoes = aversoes,
                            tags = listaTags,
                            estiloImagem = estiloImagem,
                            detalhesImagem = detalhesImagem
                        )
                        gerandoRetratoLocal = true
                        if (personaIdLocal != null && personaIdLocal!! > 0) {
                            vm.gerarRetratoPersonaId(personaIdLocal!!, req) {
                                temRetratoLocal = true
                                versaoRetratoLocal++
                                gerandoRetratoLocal = false
                                Toast.makeText(contexto, "✨ Retrato atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Salva primeiro para obter ID e depois gera
                            val pIn = PersonaIn(
                                nome = nome,
                                genero = genero,
                                aparencia = aparencia,
                                personalidade = descricao,
                                descricao = descricao,
                                idade = idade,
                                interesses = interesses,
                                medos = aversoes,
                                aversoes = aversoes,
                                tags = listaTags,
                                estiloImagem = estiloImagem,
                                detalhesImagem = detalhesImagem,
                                padrao = padrao
                            )
                            vm.salvarPersona(null, pIn) { salva ->
                                personaIdLocal = salva.id
                                vm.gerarRetratoPersonaId(salva.id, req) {
                                    temRetratoLocal = true
                                    versaoRetratoLocal++
                                    gerandoRetratoLocal = false
                                    Toast.makeText(contexto, "✨ Persona criada e retrato gerado!", Toast.LENGTH_SHORT).show()
                                    onSalvo()
                                }
                            }
                        }
                    },
                    enabled = !vm.ocupado && !gerandoRetratoLocal,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (temRetratoLocal) "Regerar Ilustração com IA" else "Gerar Ilustração com IA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    "Gera uma ilustração artística elegante com o estilo e características abaixo.",
                    color = TextoMudo,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // 1. Nome da Persona *
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        Text("Nome da Persona", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Text(" *", color = Color(0xFFFF5252), fontSize = 13.sp)
                    }
                    Text("${nome.length}/40", color = TextoMudo, fontSize = 11.sp)
                }
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = nome,
                        onValueChange = { nome = it.take(40) },
                        textStyle = TextStyle(color = TextoClaro, fontSize = 14.sp),
                        cursorBrush = SolidColor(RoxoPrimario),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        decorationBox = { inner ->
                            if (nome.isEmpty()) Text("Ex: Mariana, Arthur, Kaelen...", color = TextoMudo, fontSize = 13.sp)
                            inner()
                        }
                    )
                }
            }

            // 2. Gênero & Idade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Gênero", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        generos.forEach { g ->
                            val sel = g.equals(genero, ignoreCase = true)
                            Surface(
                                color = if (sel) RoxoPrimario.copy(alpha = 0.22f) else CardObsidian,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { genero = g }
                            ) {
                                Text(
                                    g,
                                    color = if (sel) Color.White else TextoSecundario,
                                    fontSize = 12.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.width(90.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Idade", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = idade,
                            onValueChange = { idade = it.take(20) },
                            textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                            cursorBrush = SolidColor(RoxoPrimario),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            decorationBox = { inner ->
                                if (idade.isEmpty()) Text("Ex: 28", color = TextoMudo, fontSize = 13.sp)
                                inner()
                            }
                        )
                    }
                }
            }

            // 3. PERSONALIDADE * (Requisito 1: apenas Personalidade, sem História)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text("Personalidade", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                    Text(" *", color = Color(0xFFFF5252), fontSize = 13.sp)
                }
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 72.dp)
                ) {
                    BasicTextField(
                        value = descricao,
                        onValueChange = { descricao = it.take(800) },
                        textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp, lineHeight = 18.sp),
                        cursorBrush = SolidColor(RoxoPrimario),
                        modifier = Modifier.padding(12.dp),
                        decorationBox = { inner ->
                            if (descricao.isEmpty()) {
                                Text(
                                    "Ex: Extrovertido, tímido, carinhoso, impulsivo, sarcástico, focado...",
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

            // 4. Aparência Física
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Aparência Física", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 60.dp)
                ) {
                    BasicTextField(
                        value = aparencia,
                        onValueChange = { aparencia = it.take(500) },
                        textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp, lineHeight = 18.sp),
                        cursorBrush = SolidColor(RoxoPrimario),
                        modifier = Modifier.padding(12.dp),
                        decorationBox = { inner ->
                            if (aparencia.isEmpty()) {
                                Text(
                                    "Cabelos, olhos, traços faciais, roupas que costuma vestir...",
                                    color = TextoMudo,
                                    fontSize = 12.sp
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            // 5. Interesses & Aversões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Interesses", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = interesses,
                            onValueChange = { interesses = it.take(300) },
                            textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                            cursorBrush = SolidColor(RoxoPrimario),
                            modifier = Modifier.padding(12.dp),
                            decorationBox = { inner ->
                                if (interesses.isEmpty()) Text("Música, viagens...", color = TextoMudo, fontSize = 12.sp)
                                inner()
                            }
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Aversões / Medos", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = aversoes,
                            onValueChange = { aversoes = it.take(300) },
                            textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                            cursorBrush = SolidColor(RoxoPrimario),
                            modifier = Modifier.padding(12.dp),
                            decorationBox = { inner ->
                                if (aversoes.isEmpty()) Text("Mentiras, altura...", color = TextoMudo, fontSize = 12.sp)
                                inner()
                            }
                        )
                    }
                }
            }

            // 6. DETALHES DA IMAGEM (Requisito 2: o que deve aparecer na imagem)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "O que deve aparecer na imagem (IA)",
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
                        .defaultMinSize(minHeight = 56.dp)
                ) {
                    BasicTextField(
                        value = detalhesImagem,
                        onValueChange = { detalhesImagem = it.take(500) },
                        textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp, lineHeight = 18.sp),
                        cursorBrush = SolidColor(RoxoPrimario),
                        modifier = Modifier.padding(12.dp),
                        decorationBox = { inner ->
                            if (detalhesImagem.isEmpty()) {
                                Text(
                                    "Ex: Pose sorrindo, segurando uma xícara, iluminação suave, fundo aconchegante...",
                                    color = TextoMudo,
                                    fontSize = 12.sp
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            // 7. ESTILO DA IMAGEM COM PRÉVIA VISUAL REAL
            var estiloParaAmpliar by remember { mutableStateOf<EstiloImagemInfo?>(null) }
            if (estiloParaAmpliar != null) {
                Dialog(onDismissRequest = { estiloParaAmpliar = null }) {
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PreviaEstiloImagem(
                                estiloId = estiloParaAmpliar!!.id,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                recorte = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "${estiloParaAmpliar!!.icone} ${estiloParaAmpliar!!.nome}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextoClaro
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                estiloParaAmpliar!!.descricao,
                                color = TextoSecundario,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    estiloImagem = estiloParaAmpliar!!.id
                                    estiloParaAmpliar = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Selecionar este Estilo", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Estilo Visual da Imagem",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextoClaro
                    )
                    Text("Toque na imagem para ampliar", color = TextoMudo, fontSize = 11.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ESTILOS_IMAGEM_PERSONA.forEach { estilo ->
                        val sel = estilo.id == estiloImagem
                        val pressInteraction = remember { MutableInteractionSource() }
                        val isPressed by pressInteraction.collectIsPressedAsState()
                        val styleScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.97f else if (sel) 1.02f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "styleScale"
                        )

                        Surface(
                            color = if (sel) Color(0xFF1E1738) else Color(0xFF131522),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = if (sel) 8.dp else 2.dp,
                            border = BorderStroke(
                                if (sel) 1.5.dp else 1.dp,
                                if (sel) Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF)))
                                else SolidColor(CardObsidianBorda)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(styleScale)
                                .clickable(
                                    interactionSource = pressInteraction,
                                    indication = null
                                ) { 
                                    estiloImagem = estilo.id 
                                    mostrarExemploEstilo = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clickable { estiloParaAmpliar = estilo }
                                ) {
                                    PreviaEstiloImagem(
                                        estiloId = estilo.id,
                                        modifier = Modifier.fillMaxSize(),
                                        recorte = RoundedCornerShape(10.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(estilo.icone, fontSize = 14.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            estilo.nome,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (sel) Color.White else TextoClaro
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        estilo.descricao,
                                        fontSize = 11.sp,
                                        color = TextoSecundario,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (sel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = RoxoPrimario, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 8. Definir como Padrão Switch
            Surface(
                color = CardObsidian,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardObsidianBorda),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Persona Padrão", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Text(
                            "Usar esta persona em todas as conversas do Bot automaticamente",
                            fontSize = 11.sp,
                            color = TextoSecundario
                        )
                    }
                    Switch(
                        checked = padrao,
                        onCheckedChange = { padrao = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OuroAmbar
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // Botão Salvar
        Box(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = {
                    if (nome.isBlank()) {
                        Toast.makeText(contexto, "Digite ao menos o nome da Persona!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val listaTags = if (tags.isNotBlank()) tags.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList()
                    val pIn = PersonaIn(
                        nome = nome,
                        genero = genero,
                        aparencia = aparencia,
                        personalidade = descricao,
                        descricao = descricao,
                        idade = idade,
                        interesses = interesses,
                        medos = aversoes,
                        aversoes = aversoes,
                        tags = listaTags,
                        estiloImagem = estiloImagem,
                        detalhesImagem = detalhesImagem,
                        padrao = padrao
                    )
                    vm.salvarPersona(personaExistente?.id, pIn) {
                        Toast.makeText(contexto, "Persona '${nome}' salva com sucesso!", Toast.LENGTH_SHORT).show()
                        onSalvo()
                    }
                },
                enabled = !vm.ocupado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Salvar Persona", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
