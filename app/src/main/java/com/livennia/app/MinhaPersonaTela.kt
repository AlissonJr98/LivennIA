package com.livennia.app

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val FundoOled = Color(0xFF090A0F)
private val CardObsidian = Color(0xFF13141F)
private val CardObsidianBorda = Color(0xFF232538)
private val RoxoPrimario = Color(0xFF7C4DFF)
private val DouradoLivenn = Color(0xFFF5C842)
private val TextoClaro = Color(0xFFF0F0F5)
private val TextoSecundario = Color(0xFFA0A2B3)
private val TextoMudo = Color(0xFF6E7082)

@Composable
fun ModalMinhaPersona(
    vm: AppViewModel,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current

    var nome by remember { mutableStateOf(vm.personaNome.ifBlank { vm.apelido }) }
    var genero by remember { mutableStateOf(vm.personaGenero) }
    var descricao by remember { mutableStateOf(vm.personaDescricao) }
    var idade by remember { mutableStateOf(vm.personaIdade) }
    var aparencia by remember { mutableStateOf(vm.personaAparencia) }
    var interesses by remember { mutableStateOf(vm.personaInteresses) }
    var aversoes by remember { mutableStateOf(vm.personaAversoes) }
    var tags by remember { mutableStateOf(vm.personaTags) }
    var avatar by remember { mutableStateOf(vm.personaAvatar) }

    val generos = listOf("Masculino", "Feminino", "Outro")
    val emojisAvatar = listOf("👤", "🧙‍♂️", "🧝‍♀️", "👑", "🗡️", "🦊", "🌟", "✨")

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
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onFechar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextoClaro)
                    }
                    Text(
                        "Minha Persona",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextoClaro,
                        modifier = Modifier.weight(1f)
                    )
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

                // Conteúdo do formulário da Persona
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Box de Avatar / Referência Visual (Imagem 2)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier
                                .size(140.dp, 160.dp)
                                .clickable {
                                    Toast.makeText(contexto, "Escolha um avatar de referência abaixo", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(avatar, fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = RoxoPrimario, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Avatar da Persona *", color = TextoSecundario, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Seleção rápida de emojis de avatar
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            emojisAvatar.forEach { em ->
                                val sel = em == avatar
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.25f) else CardObsidian,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { avatar = em }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(em, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Nome * (com contador 0/40)
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row {
                                Text("Nome", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                                Text(" *", color = Color(0xFFFF5252), fontSize = 13.sp)
                            }
                            Text("${nome.length}/40", color = TextoMudo, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(6.dp))
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
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (nome.isEmpty()) Text("Seu nome na história / como a IA vai te chamar", color = TextoMudo, fontSize = 13.sp)
                                    inner()
                                }
                            )
                        }
                    }

                    // 3. Gênero *
                    Column {
                        Row {
                            Text("Gênero", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                            Text(" *", color = Color(0xFFFF5252), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            generos.forEach { g ->
                                val sel = g == genero
                                Surface(
                                    color = if (sel) RoxoPrimario.copy(alpha = 0.22f) else CardObsidian,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (sel) RoxoPrimario else CardObsidianBorda),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { genero = g }
                                ) {
                                    Text(
                                        g,
                                        color = if (sel) Color.White else TextoSecundario,
                                        fontSize = 13.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // 4. Descrição *
                    Column {
                        Row {
                            Text("Descrição", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                            Text(" *", color = Color(0xFFFF5252), fontSize = 13.sp)
                        }
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
                                value = descricao,
                                onValueChange = { descricao = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (descricao.isEmpty()) {
                                        Text(
                                            "Escreva um breve resumo da história ou personalidade — quem é, qual é a sua história e como age?",
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

                    // 5. Idade
                    Column {
                        Text("Idade", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = idade,
                                onValueChange = { idade = it.take(20) },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 14.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (idade.isEmpty()) Text("Sua idade (ex: 19 anos, 24 anos)", color = TextoMudo, fontSize = 13.sp)
                                    inner()
                                }
                            )
                        }
                    }

                    // 6. Aparência (Imagem 3)
                    Column {
                        Text("Aparência", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
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
                                value = aparencia,
                                onValueChange = { aparencia = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (aparencia.isEmpty()) {
                                        Text(
                                            "Descreva a aparência dele(a): cabelo, roupa, traços do rosto, vibe ou qualquer detalhe marcante.",
                                            color = TextoMudo,
                                            fontSize = 12.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    // 7. Interesses
                    Column {
                        Text("Interesses", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            BasicTextField(
                                value = interesses,
                                onValueChange = { interesses = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (interesses.isEmpty()) Text("Compartilhe o que você gosta (música, mistérios, livros, lutas)", color = TextoMudo, fontSize = 12.sp)
                                    inner()
                                }
                            )
                        }
                    }

                    // 8. Aversões
                    Column {
                        Text("Aversões", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            BasicTextField(
                                value = aversoes,
                                onValueChange = { aversoes = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (aversoes.isEmpty()) Text("Conte o que você não gosta ou evita em situações difíceis", color = TextoMudo, fontSize = 12.sp)
                                    inner()
                                }
                            )
                        }
                    }

                    // 9. Tags
                    Column {
                        Text("Tags & Características", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoClaro)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = tags,
                                onValueChange = { tags = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(RoxoPrimario),
                                modifier = Modifier.padding(14.dp),
                                decorationBox = { inner ->
                                    if (tags.isEmpty()) Text("Informe gênero, espécie, arquétipos (ex: Humano, Mago, Calmo)", color = TextoMudo, fontSize = 12.sp)
                                    inner()
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                }

                // Botão de Salvar Persona (Salva no AppViewModel e nas Preferências)
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (nome.isBlank()) {
                                Toast.makeText(contexto, "Informe ao menos o nome da sua Persona!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            vm.salvarMinhaPersona(
                                nome = nome,
                                genero = genero,
                                descricao = descricao,
                                idade = idade,
                                aparencia = aparencia,
                                interesses = interesses,
                                aversoes = aversoes,
                                tags = tags,
                                avatar = avatar
                            )
                            Toast.makeText(contexto, "Persona '${nome}' salva! A IA agora usará suas características em todas as histórias e chats.", Toast.LENGTH_LONG).show()
                            onFechar()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoxoPrimario),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Salvar Persona", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
