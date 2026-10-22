package com.livennia.app

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun BarraDeVida(vida: Int, vidaMax: Int, modifier: Modifier = Modifier) {
    val fracao = if (vidaMax > 0) (vida.toFloat() / vidaMax).coerceIn(0f, 1f) else 0f
    val cor = when {
        fracao > 0.6f -> Color(0xFF4CAF50)
        fracao > 0.3f -> Color(0xFFFFC107)
        else -> Sangue
    }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vida",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSuave
            )
            Text(
                text = "$vida / $vidaMax",
                style = MaterialTheme.typography.labelLarge,
                color = cor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fracao },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .semantics { contentDescription = "Vida: $vida de $vidaMax" },
            color = cor,
            trackColor = SuperficieAlta
        )
    }
}

@Composable
fun AvisoCard(texto: String, ehErro: Boolean, onFechar: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (ehErro) Color(0xFF3A1A1A) else Color(0xFF1F2A1C)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = texto,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onFechar) { Text("OK") }
        }
    }
}

@Composable
fun CarregandoLinha(mensagem: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(12.dp))
        Text(mensagem, color = TextoSuave)
    }
}

@Composable
fun DialogoConfirmar(
    titulo: String,
    texto: String,
    rotuloConfirmar: String,
    onConfirmar: () -> Unit,
    onFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(rotuloConfirmar) } },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoFeedback(onEnviar: (Int, String) -> Unit, onFechar: () -> Unit) {
    var nota by remember { mutableIntStateOf(0) }
    var comentario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Como está sendo a aventura?") },
        text = {
            Column {
                Text("Sua nota (1 a 5):")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (n in 1..5) {
                        if (n == nota) {
                            Button(
                                onClick = { nota = n },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(44.dp)
                            ) { Text("$n") }
                        } else {
                            OutlinedButton(
                                onClick = { nota = n },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(44.dp)
                            ) { Text("$n") }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it.take(500) },
                    shape = Cantos.campo,
                    label = { Text("Comentário (opcional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onEnviar(nota, comentario) }, enabled = nota in 1..5) {
                Text("Enviar")
            }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

private val MOTIVOS_DENUNCIA = listOf(
    "ofensivo" to "Conteúdo ofensivo",
    "violento_demais" to "Violento demais",
    "incoerente" to "Sem sentido ou incoerente",
    "outro" to "Outro motivo"
)

// Converte texto com **negrito** e *itálico* em AnnotatedString para exibição rica.
fun parsearNarrativa(texto: String): AnnotatedString = buildAnnotatedString {
    val regex = Regex("(\\*\\*[^*]+\\*\\*|\\*[^*]+\\*)")
    var lastIndex = 0
    for (match in regex.findAll(texto)) {
        append(texto.substring(lastIndex, match.range.first))
        val trecho = match.value
        if (trecho.startsWith("**")) {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(trecho.removePrefix("**").removeSuffix("**"))
            pop()
        } else {
            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            append(trecho.removePrefix("*").removeSuffix("*"))
            pop()
        }
        lastIndex = match.range.last + 1
    }
    append(texto.substring(lastIndex))
}

// Formatação especial estilo LivennIA / Livro:
// - *ações, pensamentos e descrições* ficam em itálico e cor suave
// - "diálogos falados" ganham destaque luminoso e peso médio/negrito
fun parsearNarrativaLivro(
    texto: String,
    corFala: Color,
    corAcao: Color,
    corNormal: Color
): AnnotatedString = buildAnnotatedString {
    val regex = Regex("(\\*\\*[^*]+\\*\\*|\\*[^*]+\\*|\"[^\"]+\"|“[^”]+”)")
    var lastIndex = 0
    for (match in regex.findAll(texto)) {
        if (match.range.first > lastIndex) {
            pushStyle(SpanStyle(color = corNormal))
            append(texto.substring(lastIndex, match.range.first))
            pop()
        }
        val trecho = match.value
        when {
            trecho.startsWith("**") -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = corFala))
                append(trecho.removePrefix("**").removeSuffix("**"))
                pop()
            }
            trecho.startsWith("*") -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = corAcao))
                append(trecho.removePrefix("*").removeSuffix("*"))
                pop()
            }
            trecho.startsWith("(") && trecho.endsWith(")") -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = corAcao))
                append(trecho)
                pop()
            }
            trecho.startsWith("\"") || trecho.startsWith("“") || trecho.startsWith("—") -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = corFala))
                append(trecho)
                pop()
            }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < texto.length) {
        pushStyle(SpanStyle(color = corNormal))
        append(texto.substring(lastIndex))
        pop()
    }
}

// Shimmer (efeito de brilho) para placeholders de carregamento.
@Composable
fun ShimmerBox(modifier: Modifier = Modifier, cornerRadius: Int = 8) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            SuperficieAlta.copy(alpha = 0.3f),
            SuperficieAlta.copy(alpha = 0.7f),
            SuperficieAlta.copy(alpha = 0.3f)
        ),
        start = androidx.compose.ui.geometry.Offset(x - 300f, 0f),
        end = androidx.compose.ui.geometry.Offset(x, 0f)
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(brush)
    )
}

// Confetti simples (animação de partículas) para celebrar conquistas.
@Composable
fun Confetti(explosao: Boolean, modifier: Modifier = Modifier) {
    if (!explosao) return
    val cores = listOf(Ouro, Sangue, Color(0xFF4CAF50), Color(0xFFFFC107), Color.White)
    val particulas = remember(cores) {
        List(60) {
            ConfeteParticula(
                x = (0..1000).random().toFloat(),
                y = (0..200).random().toFloat(),
                cor = cores.random(),
                velocidade = (2..6).random().toFloat(),
                tamanho = (4..10).random().toFloat()
            )
        }
    }
    val progresso = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(explosao) {
        progresso.snapTo(0f)
        progresso.animateTo(1f, animationSpec = tween(durationMillis = 2000))
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val h = size.height
        val w = size.width
        particulas.forEach { p ->
            val y = (p.y + progresso.value * p.velocidade * 200f) % h
            val x = (p.x + progresso.value * (p.velocidade * 50f)) % w
            val alpha = (1f - progresso.value).coerceIn(0f, 1f)
            drawCircle(
                color = p.cor.copy(alpha = alpha),
                radius = p.tamanho,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

private data class ConfeteParticula(
    val x: Float,
    val y: Float,
    val cor: Color,
    val velocidade: Float,
    val tamanho: Float
)

// Modificador que reduz levemente o elemento ao ser pressionado (feedback visual).
fun Modifier.pressScale(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "pressScale"
    )
    this
        .scale(scale)
        .clickable(interactionSource = interactionSource, indication = null) { }
}

// Retrato simples do personagem, derivado do avatar_seed (sem depender de rede).
@Composable
fun AvatarPersonagem(seed: Int, nome: String, tamanho: Int = 48, modifier: Modifier = Modifier) {
    val cor = Color(0xFF000000 + (seed and 0x00FFFFFF))
    val letra = nome.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val corAnel = Ouro
    Box(
        modifier = modifier
            .size(tamanho.dp)
            .clip(CircleShape)
            .background(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(cor.copy(alpha = 0.95f), cor.copy(alpha = 0.45f))
                )
            )
            .then(
                Modifier.drawBehind {
                    drawCircle(
                        color = corAnel,
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = (tamanho / 16).dp.toPx())
                    )
                }
            )
            .semantics { contentDescription = "Retrato de $nome" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letra,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (tamanho / 2).sp
        )
    }
}

@Composable
fun DialogoDenuncia(onEnviar: (String, String) -> Unit, onFechar: () -> Unit) {
    var motivo by remember { mutableStateOf(MOTIVOS_DENUNCIA[0].first) }
    var detalhe by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Denunciar esta cena") },
        text = {
            Column {
                for ((codigo, rotulo) in MOTIVOS_DENUNCIA) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { motivo = codigo },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = motivo == codigo, onClick = { motivo = codigo })
                        Text(rotulo)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = detalhe,
                    onValueChange = { detalhe = it.take(300) },
                    shape = Cantos.campo,
                    label = { Text("Detalhes (opcional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onEnviar(motivo, detalhe) }) { Text("Enviar") } },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

// Retrato do personagem: usa a imagem gerada por IA quando existe; senão,
// cai no desenho feito a partir do avatar_seed (não depende de rede).
@Composable
fun RetratoPersonagem(
    jogoId: String,
    seed: Int,
    nome: String,
    temRetrato: Boolean,
    tamanho: Int,
    versao: Int = 0,
    modifier: Modifier = Modifier,
    recorte: Shape = CircleShape
) {
    val url = remember(temRetrato, jogoId, versao) {
        if (!temRetrato) null
        else ApiClient.baseUrlAtual()?.let { base -> "${base}jogos/$jogoId/retrato?v=$versao" }
    }
    if (url != null) {
        val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .apply { if (cabecalho != null) addHeader("Authorization", cabecalho) }
                .crossfade(true)
                .build(),
            contentDescription = "Retrato de $nome",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(tamanho.dp)
                .clip(recorte)
        )
    } else {
        AvatarPersonagem(seed = seed, nome = nome, tamanho = tamanho, modifier = modifier)
    }
}

// Retrato da Persona do usuário: gerado por IA conforme suas características
@Composable
fun RetratoPersona(
    temRetrato: Boolean,
    emojiOuPadrao: String = "👤",
    tamanho: Int = 72,
    versao: Int = 0,
    personaId: Int? = null,
    modifier: Modifier = Modifier,
    recorte: Shape = CircleShape
) {
    val url = remember(temRetrato, versao, personaId) {
        if (!temRetrato) null
        else ApiClient.baseUrlAtual()?.let { base ->
            val t = ApiClient.token
            val tokenParam = if (!t.isNullOrEmpty()) "&token=$t" else ""
            if (personaId != null && personaId > 0) "${base}personas/$personaId/retrato?v=$versao$tokenParam"
            else "${base}conta/persona/retrato?v=$versao$tokenParam"
        }
    }
    Surface(
        color = Color(0xFF161824),
        shape = recorte,
        border = BorderStroke(1.dp, Color(0xFF282B3D)),
        modifier = modifier.size(tamanho.dp)
    ) {
        if (url != null) {
            val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .apply { if (cabecalho != null) addHeader("Authorization", cabecalho) }
                    .crossfade(true)
                    .build(),
                contentDescription = "Retrato da Persona",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(emojiOuPadrao.ifBlank { "👤" }, fontSize = (tamanho * 0.45).sp)
            }
        }
    }
}

// Imagem de exemplo visual do estilo de arte da IA (como o estilo se parece)
@Composable
fun PreviaEstiloImagem(
    estiloId: String,
    modifier: Modifier = Modifier,
    recorte: Shape = RoundedCornerShape(10.dp)
) {
    val url = remember(estiloId) {
        ApiClient.baseUrlAtual()?.let { base ->
            "${base}estilos-imagem/$estiloId/exemplo"
        }
    }
    Surface(
        color = Color(0xFF1B1D2A),
        shape = recorte,
        modifier = modifier
    ) {
        if (url != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Exemplo do estilo $estiloId",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text("🎨", fontSize = 16.sp)
            }
        }
    }
}


// Capa de um cenário da vitrine: usa a arte gerada por IA quando existe; senão,
// desenha um fundo com o emoji (não depende de rede nem de cota de imagem).
@Composable
fun CapaCenario(
    cenarioId: String,
    temCapa: Boolean,
    emoji: String,
    nome: String,
    versao: Int = 0,
    modifier: Modifier = Modifier,
    recorte: Shape = Cantos.imagem
) {
    val url = remember(temCapa, cenarioId, versao) {
        if (!temCapa) null
        else ApiClient.baseUrlAtual()?.let { base -> "${base}catalogo/$cenarioId/capa?v=$versao" }
    }
    if (url != null) {
        val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .apply { if (cabecalho != null) addHeader("Authorization", cabecalho) }
                .crossfade(true)
                .build(),
            contentDescription = "Capa de $nome",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(recorte)
        )
    } else {
        Box(
            modifier = modifier
                .clip(recorte)
                .background(
                    Brush.linearGradient(
                        colors = listOf(SuperficieAlta, Ouro.copy(alpha = 0.22f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji.ifBlank { "📖" }, fontSize = 40.sp)
        }
    }
}

// Pílula cinza com um texto curto (gênero, tag, característica).
@Composable
fun ChipTag(texto: String, modifier: Modifier = Modifier, destaque: Boolean = false) {
    Surface(
        color = if (destaque) Ouro.copy(alpha = 0.18f) else SuperficieAlta,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = if (destaque) Ouro else TextoSuave,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

// Botão principal: pílula, largura cheia (estilo do app de referência).
@Composable
fun BotaoPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

// Título de seção com a barra amarela à esquerda (dá o "toque" do tema).
@Composable
fun TituloSecao(texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Ouro)
        )
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.titleMedium)
    }
}