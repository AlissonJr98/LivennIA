package com.livennia.app

import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import androidx.compose.ui.zIndex
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import coil.compose.AsyncImage
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.RectangleShape
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import android.widget.Toast
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

private enum class DialogoAberto {
    Feedback,
    Denuncia,
    Memoria,
    Diarios,
    Cartas,
    Savepoints,
    ModeloIA,
    Vinculo,
    Party,
    Dados
}

data class NivelVinculo(
    val nivel: Int,
    val titulo: String,
    val descricao: String,
    val progresso: Float,
    val icone: String = "✦"
)

fun calcularVinculo(turno: Int): NivelVinculo {
    return when {
        turno <= 2 -> NivelVinculo(
            nivel = 1,
            titulo = "Primeiras Palavras",
            descricao = "Vocês acabaram de se encontrar. O personagem ainda está observando sua postura e avaliando suas intenções.",
            progresso = (turno / 3f).coerceIn(0.15f, 1f),
            icone = "🌱"
        )
        turno <= 6 -> NivelVinculo(
            nivel = 2,
            titulo = "Curiosidade Mútua",
            descricao = "O gelo inicial foi quebrado. O personagem demonstra genuíno interesse nas suas respostas e atitudes.",
            progresso = ((turno - 2) / 4f).coerceIn(0.15f, 1f),
            icone = "✨"
        )
        turno <= 12 -> NivelVinculo(
            nivel = 3,
            titulo = "Cúmplices",
            descricao = "Uma sintonia evidente surgiu entre vocês. O personagem confia nos seus conselhos diante de escolhas difíceis.",
            progresso = ((turno - 6) / 6f).coerceIn(0.15f, 1f),
            icone = "🤝"
        )
        turno <= 20 -> NivelVinculo(
            nivel = 4,
            titulo = "Confiança Profunda",
            descricao = "O personagem compartilha segredos íntimos e sentimentos reais que não revela a mais ninguém.",
            progresso = ((turno - 12) / 8f).coerceIn(0.15f, 1f),
            icone = "💖"
        )
        else -> NivelVinculo(
            nivel = 5,
            titulo = "Vínculo Inquebrável",
            descricao = "Uma conexão inabalável e histórica forjada por cada momento compartilhado nesta jornada.",
            progresso = 1f,
            icone = "👑"
        )
    }
}

fun extrairHumorPersonagem(narrativa: String, turno: Int): Pair<String, String> {
    val n = narrativa.lowercase()
    return when {
        "sorri" in n || "rir" in n || "divert" in n || "gargalh" in n -> "😄" to "Divertido(a)"
        "beij" in n || "abraç" in n || "rubor" in n || "coração" in n || "olhar intenso" in n -> "😍" to "Apaixonado(a)"
        "irrit" in n || "rosn" in n || "cerr" in n || "raiva" in n || "bravo" in n -> "😡" to "Tenso(a)"
        "medo" in n || "recu" in n || "treme" in n || "pavor" in n -> "😨" to "Em Alerta"
        "sussurr" in n || "segredo" in n || "olha nos olhos" in n || "mistér" in n -> "😏" to "Cúmplice"
        "firme" in n || "determina" in n || "espada" in n || "combate" in n -> "⚔️" to "Combativo(a)"
        turno <= 2 -> "🌱" to "Cauteloso(a)"
        turno <= 5 -> "✨" to "Curioso(a)"
        else -> "💭" to "Reflexivo(a)"
    }
}

fun gerarPensamentoSecreto(nome: String, situacao: String, turno: Int): String {
    val s = situacao.lowercase()
    return when {
        "sorri" in s || "olhar" in s -> "\"$nome pensa: 'Não sei se deveria demonstrar tanto interesse... Mas tem algo diferente nessa pessoa que me prende.'\""
        "espada" in s || "combate" in s || "arma" in s || "ataque" in s -> "\"$nome pensa: 'Se vacilarmos agora, é o fim. Preciso manter a guarda alta e cobrir nossa retaguarda.'\""
        "contrato" in s || "crédito" in s || "missão" in s -> "\"$nome pensa: 'Esse contrato esconde mais do que me disseram. Se eu não tomar cuidado, nós dois vamos afundar.'\""
        "beijo" in s || "amor" in s || "sentimento" in s -> "\"$nome pensa: 'Meu coração está disparado. Eu prometi que não me apegaria a ninguém de novo... mas já é tarde.'\""
        "medo" in s || "sombra" in s || "perigo" in s -> "\"$nome pensa: 'Preciso fingir calma. Se perceberem o quão perto do abismo estamos, o pânico vai nos matar.'\""
        turno <= 3 -> "\"$nome pensa: 'Ainda estou medindo cada palavra sua. Vamos ver se você é quem realmente diz ser.'\""
        turno <= 8 -> "\"$nome pensa: 'Confesso que você superou minhas expectativas até aqui. Uma parceria promissora...'\""
        else -> "\"$nome pensa: 'Já passamos por tanta coisa juntos que mal me lembro de como era antes de você aparecer.'\""
    }
}

fun exportarHistoriaPdf(context: android.content.Context, jogo: JogoResponse, chat: List<MensagemDto>) {
    try {
        val pdfDoc = PdfDocument()
        val paintTitulo = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = AndroidColor.rgb(20, 20, 30)
        }
        val paintSub = Paint().apply {
            textSize = 12f
            color = AndroidColor.rgb(100, 100, 120)
        }
        val paintCap = Paint().apply {
            textSize = 13f
            isFakeBoldText = true
            color = AndroidColor.rgb(180, 120, 20)
        }
        val paintTexto = Paint().apply {
            textSize = 11f
            color = AndroidColor.rgb(30, 30, 40)
        }
        val paintFala = Paint().apply {
            textSize = 11f
            color = AndroidColor.rgb(140, 80, 0)
            isFakeBoldText = true
        }
        val paintLinha = Paint().apply {
            color = AndroidColor.rgb(220, 200, 160)
            strokeWidth = 1.2f
        }

        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        var y = 60f

        val titulo = jogo.cenario?.nome ?: jogo.personagem.nome
        val autor = jogo.cenario?.autorNome ?: "LivennIA"

        // Cabeçalho da Primeira Página
        canvas.drawText("LIVENNIA • CRÔNICAS INTERATIVAS", 50f, y, paintSub)
        y += 26f
        canvas.drawText(titulo, 50f, y, paintTitulo)
        y += 20f
        canvas.drawText("Autor(a): $autor  •  Gênero: ${jogo.genero}  •  Turnos: ${chat.size}", 50f, y, paintSub)
        y += 18f
        val dataStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        canvas.drawText("Protagonista: ${jogo.personagem.nome}  •  Data do Registro: $dataStr", 50f, y, paintSub)
        y += 24f
        canvas.drawLine(50f, y, pageWidth - 50f, y, paintLinha)
        y += 28f

        fun quebrarLinhas(texto: String, paint: Paint, maxWidth: Float): List<String> {
            val palavras = texto.split(Regex("\\s+"))
            val linhas = mutableListOf<String>()
            var linhaAtual = StringBuilder()
            for (p in palavras) {
                val teste = if (linhaAtual.isEmpty()) p else "$linhaAtual $p"
                if (paint.measureText(teste) > maxWidth) {
                    if (linhaAtual.isNotEmpty()) linhas.add(linhaAtual.toString())
                    linhaAtual = StringBuilder(p)
                } else {
                    linhaAtual = StringBuilder(teste)
                }
            }
            if (linhaAtual.isNotEmpty()) linhas.add(linhaAtual.toString())
            return linhas
        }

        for ((idx, msg) in chat.withIndex()) {
            val capTitulo = "Capítulo ${idx + 1}"
            val linhasAcao = if (msg.acao != "[CONTINUAR_HISTORIA]" && idx > 0) {
                quebrarLinhas("➔ ${jogo.personagem.nome}: ${msg.situacao ?: msg.acao}", paintFala, pageWidth - 100f)
            } else emptyList()
            val linhasNarrativa = quebrarLinhas(msg.narrativa, paintTexto, pageWidth - 100f)

            val alturaBloco = 26f + (linhasAcao.size * 16f) + (linhasNarrativa.size * 16f) + 20f
            if (y + alturaBloco > pageHeight - 60f) {
                pdfDoc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }

            canvas.drawText(capTitulo, 50f, y, paintCap)
            y += 18f

            if (linhasAcao.isNotEmpty()) {
                for (l in linhasAcao) {
                    canvas.drawText(l, 50f, y, paintFala)
                    y += 16f
                }
                y += 4f
            }

            for (l in linhasNarrativa) {
                canvas.drawText(l, 50f, y, paintTexto)
                y += 16f
            }
            y += 22f
        }

        pdfDoc.finishPage(page)

        val nomeLimpo = titulo.replace(Regex("[^a-zA-Z0-9]"), "_").take(30)
        val arquivoPdf = java.io.File(context.cacheDir, "Livro_${nomeLimpo}.pdf")
        val fos = java.io.FileOutputStream(arquivoPdf)
        pdfDoc.writeTo(fos)
        fos.close()
        pdfDoc.close()

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivoPdf
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Livro Digital: $titulo")
            putExtra(Intent.EXTRA_TEXT, "📖 Confira a história de '$titulo' registrada no LivennIA!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Exportar Livro Digital (PDF)"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Erro ao exportar PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun modularVozTTS(tts: TextToSpeech?, genero: String, personagem: String? = null) {
    val p = personagem?.lowercase() ?: ""
    val g = genero.lowercase()
    when {
        "rainha" in p || "princesa" in p || "garota" in p || "menina" in p || "mulher" in p || "elfa" in p || "maga" in p || "romance" in g || "elas" in g -> {
            tts?.setPitch(1.18f)
            tts?.setSpeechRate(1.02f)
        }
        "cyberpunk" in g || "guerreiro" in p || "cavaleiro" in p || "monstro" in p || "vilão" in p || "máfia" in g || "sombria" in g -> {
            tts?.setPitch(0.85f)
            tts?.setSpeechRate(0.96f)
        }
        "ação" in g || "aventura" in g -> {
            tts?.setPitch(1.04f)
            tts?.setSpeechRate(1.05f)
        }
        else -> {
            tts?.setPitch(1.0f)
            tts?.setSpeechRate(1.0f)
        }
    }
}

data class PresenteItem(
    val icone: String,
    val nome: String,
    val descricao: String,
    val afeto: Int,
    val custoLivens: Int,
    val acaoNarrativa: String
)

@Composable
fun ModalPresentearPersonagem(
    nomePersonagem: String,
    saldoLivens: Int,
    onEnviarPresente: (PresenteItem) -> Unit,
    onFechar: () -> Unit
) {
    val presentes = listOf(
        PresenteItem("☕", "Café Quente Especial", "Aquece a conversa e relaxa a tensão", 15, 5, "*Oferece uma xícara fumegante de café fresco para $nomePersonagem.*"),
        PresenteItem("💐", "Flores de Neon Silvestres", "Um gesto de carinho e apreciação visual", 25, 10, "*Presenteia $nomePersonagem com um lindo arranjo de flores luminescentes.*"),
        PresenteItem("🍷", "Taça de Vinho Envelhecido", "Abre espaço para confissões íntimas e risadas", 35, 15, "*Serve duas taças de vinho nobre e brinda com $nomePersonagem.*"),
        PresenteItem("🗡️", "Adaga de Prata Gravada", "Símbolo de proteção e confiança mútua", 50, 20, "*Entrega a $nomePersonagem uma lâmina refinada forjada com runas de proteção.*"),
        PresenteItem("📜", "Pergaminho com Poema", "Palavras sinceras tocando a alma", 45, 18, "*Entrega uma carta escrita à mão com palavras dedicadas a $nomePersonagem.*"),
        PresenteItem("✨", "Orbe de Luz Eterna", "Uma relíquia misteriosa e cativante", 60, 25, "*Coloca nas mãos de $nomePersonagem uma esfera luminosa que irradia calma e calor.*")
    )

    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎁", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Presentear $nomePersonagem", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Seus Livens: ✨ $saldoLivens", style = MaterialTheme.typography.labelSmall, color = Ouro)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Envie um agrado para surpreender $nomePersonagem! A IA responderá imediatamente com uma reação emocional personalizada e o vínculo aumentará.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
                presentes.forEach { p ->
                    val podePagar = saldoLivens >= p.custoLivens
                    Surface(
                        color = Color(0xFF161826),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (podePagar) Ouro.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .cliqueFuturista3D {
                                onEnviarPresente(p)
                                onFechar()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.icone, fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.nome, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(p.descricao, color = TextoSuave, fontSize = 11.sp)
                                Spacer(Modifier.height(2.dp))
                                Text("💖 +${p.afeto} Vínculo", color = Color(0xFFFF80AB), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Surface(
                                color = if (podePagar) Ouro.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                shape = Cantos.pilula,
                                border = BorderStroke(1.dp, if (podePagar) Ouro else Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    "✨ ${p.custoLivens}",
                                    color = if (podePagar) Ouro else TextoSuave,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) { Text("Fechar") }
        }
    )
}

data class MomentoCG(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val nivelNecessario: Int,
    val descricaoLore: String
)

@Composable
fun ModalGaleriaMomentos(
    nomePersonagem: String,
    cenarioId: String?,
    nivelVinculoAtual: Int,
    onFechar: () -> Unit
) {
    val momentos = listOf(
        MomentoCG("1", "Primeiro Encontro", "O início de tudo", 1, "O olhar trocado quando seus caminhos se cruzaram pela primeira vez."),
        MomentoCG("2", "Chuva & Confidências", "Quebrando o gelo", 2, "Um momento a sós onde o silêncio falou mais alto que o perigo ao redor."),
        MomentoCG("3", "Cúmplices no Perigo", "Confiança forjada no caos", 3, "De costas um para o outro, sabendo que suas vidas dependem dessa sintonia."),
        MomentoCG("4", "Confissão à Meia-Noite", "Segredos sem máscaras", 4, "A vulnerabilidade que nunca foi mostrada a ninguém antes de você."),
        MomentoCG("5", "Juramento Inquebrável", "Destinos entrelaçados", 5, "Uma aliança além das palavras, eternizada em cada decisão tomada juntos.")
    )

    var momentoEmZoom by remember { mutableStateOf<MomentoCG?>(null) }
    val contexto = LocalContext.current

    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📸", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Galeria de Momentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$nomePersonagem • Memórias Exclusivas", style = MaterialTheme.typography.labelSmall, color = Ouro)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Momentos visuais memoráveis inspirados na sua jornada. Conforme você conversa e fortalece o Vínculo com $nomePersonagem, novas memórias são reveladas!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )

                momentos.forEach { m ->
                    val desbloqueado = nivelVinculoAtual >= m.nivelNecessario
                    Surface(
                        color = if (desbloqueado) Color(0xFF161928) else Color(0xFF0F1018),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (desbloqueado) Ouro.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .cliqueFuturista3D {
                                if (desbloqueado) {
                                    momentoEmZoom = m
                                } else {
                                    Toast.makeText(contexto, "🔒 Desbloqueia no Nível ${m.nivelNecessario} de Vínculo! Continue jogando com $nomePersonagem.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (desbloqueado) Color(0xFF22263D) else Color(0xFF14151E)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (desbloqueado) {
                                    Text("🖼️", fontSize = 24.sp)
                                } else {
                                    Text("🔒", fontSize = 20.sp)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = m.titulo,
                                        color = if (desbloqueado) Color.White else TextoSuave,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    if (desbloqueado) {
                                        Surface(color = Ouro.copy(alpha = 0.2f), shape = Cantos.pilula) {
                                            Text("REVELADO", color = Ouro, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = if (desbloqueado) m.descricaoLore else "Requer Vínculo Nível ${m.nivelNecessario} (${m.subtitulo})",
                                    color = if (desbloqueado) Color.White.copy(alpha = 0.85f) else TextoSuave.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) { Text("Fechar") }
        }
    )

    momentoEmZoom?.let { mz ->
        Dialog(
            onDismissRequest = { momentoEmZoom = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (!cenarioId.isNullOrBlank()) {
                    CapaCenario(
                        cenarioId = cenarioId,
                        temCapa = true,
                        emoji = "📸",
                        nome = nomePersonagem,
                        alignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📸 Momento: ${mz.titulo}", color = Ouro, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton(onClick = { momentoEmZoom = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                Surface(
                    color = Color(0xFF0F101A).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(mz.titulo, color = Ouro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(mz.descricaoLore, color = Color.White, fontSize = 13.sp, lineHeight = 19.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("★ Registro Oficial LivennIA • Memória Guardada", color = TextoSuave, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoIlustracaoCenaOpcoes(
    nomeModeloAtual: String,
    onAssistirAnuncio: () -> Unit,
    onMelhorarPlano: () -> Unit,
    onFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFFFD700).copy(alpha = 0.20f),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🖼️", fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Ilustrar Cena com IA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Recurso Avançado", style = MaterialTheme.typography.labelSmall, color = Ouro)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "A ilustração cinematográfica da cena atual em ultra resolução é um recurso dos Modelos de IA Avançados do LivennIA.\n\nNo momento você está usando o modelo gratuito ($nomeModeloAtual). Escolha como deseja liberar a arte:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave,
                    lineHeight = 19.sp
                )

                // Opção 1: Assistir Anúncio
                Surface(
                    color = Color(0xFF161826),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.40f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAssistirAnuncio() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.20f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎬", fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Assistir a um Anúncio",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                "Assista a um vídeo curto e desbloqueie a arte da cena gratuitamente",
                                color = TextoSuave,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Opção 2: Melhorar Plano (Assinatura)
                Surface(
                    color = Color(0xFF1F1A12),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.50f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMelhorarPlano() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Ouro.copy(alpha = 0.20f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Melhorar o Plano",
                                    fontWeight = FontWeight.Bold,
                                    color = Ouro,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = Ouro.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "VIP",
                                        color = Ouro,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                "Desbloqueie todos os modelos e gere ilustrações ilimitadas sem anúncios",
                                color = TextoSuave,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onFechar) {
                Text("Cancelar", color = TextoSuave)
            }
        }
    )
}

@Composable
fun DialogoSimuladorAnuncio(
    onConcluido: () -> Unit,
    onFechar: () -> Unit
) {
    var segundosRestantes by remember { mutableIntStateOf(5) }
    LaunchedEffect(Unit) {
        while (segundosRestantes > 0) {
            delay(1000L)
            segundosRestantes--
        }
        delay(300L)
        onConcluido()
    }

    Dialog(
        onDismissRequest = { /* Aguarda concluir */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            color = Color(0xFF0F1016),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Anúncio de Recompensa",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8E8E93)
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = Cantos.pilula
                    ) {
                        Text(
                            text = if (segundosRestantes > 0) "Recompensa em ${segundosRestantes}s" else "Concluído! ✨",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (segundosRestantes > 0) Color.White else Color(0xFF00E676),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E2138), Color(0xFF0B0D18))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎬", fontSize = 38.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "LivennIA Ad Rewards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Obrigado por apoiar a IA gratuita!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (segundosRestantes > 0) {
                    LinearProgressIndicator(
                        progress = { (5 - segundosRestantes) / 5f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(Cantos.pilula),
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF26262A)
                    )
                } else {
                    Text(
                        "Recompensa desbloqueada! Pintando cena com IA...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ModoCinemaVisualNovel(
    jogo: JogoResponse,
    chat: List<MensagemDto>,
    vm: AppViewModel,
    narrativaAtual: String,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current
    var somAtivo by remember { mutableStateOf(SintetizadorAmbienteLivennIA.ativo) }
    var visivelCinema by remember(narrativaAtual) { mutableIntStateOf(0) }

    LaunchedEffect(narrativaAtual) {
        visivelCinema = 0
        while (visivelCinema < narrativaAtual.length) {
            delay(16)
            visivelCinema = minOf(narrativaAtual.length, visivelCinema + 3)
        }
    }

    val terminouTexto = visivelCinema >= narrativaAtual.length
    val humor = remember(narrativaAtual, jogo.turno) { extrairHumorPersonagem(narrativaAtual, jogo.turno) }
    val vinculo = remember(jogo.turno) { calcularVinculo(jogo.turno) }
    val nomePersonagem = jogo.cenario?.nome ?: jogo.personagem.nome
    val cenarioId = jogo.cenario?.id

    val base = ApiClient.baseUrlAtual() ?: ""
    val urlCenaOuCapa = remember(jogo.cena.imagemUrl, cenarioId, vm.capaVersao) {
        when {
            !jogo.cena.imagemUrl.isNullOrBlank() -> "${base}${jogo.cena.imagemUrl}"
            !cenarioId.isNullOrBlank() -> "${base}catalogo/$cenarioId/capa?v=${vm.capaVersao}"
            jogo.retrato -> "${base}jogos/${jogo.jogoId}/retrato?v=${vm.retratoVersao}"
            else -> null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .zIndex(100f)
            .clickable {
                if (!terminouTexto) {
                    visivelCinema = narrativaAtual.length
                }
            }
    ) {
        if (urlCenaOuCapa != null) {
            AsyncImage(
                model = ImageRequest.Builder(contexto)
                    .data(urlCenaOuCapa)
                    .crossfade(true)
                    .build(),
                contentDescription = "Cena Cinemática",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF13141F), Color(0xFF07080C))
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.70f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        ParticulasFlutuantesLivennIA(
            modifier = Modifier.fillMaxSize(),
            quantidade = 24,
            cor = Ouro
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = Cantos.pilula,
                border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎬 Modo Cinema", color = Ouro, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text("Cap. ${jogo.turno}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(onClick = {
                        val gen = jogo.cenario?.tags?.firstOrNull() ?: jogo.genero
                        SintetizadorAmbienteLivennIA.alternar(gen) { somAtivo = it }
                    }) {
                        Text(if (somAtivo) "🎵" else "🔇", fontSize = 16.sp)
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(onClick = onFechar) {
                        Icon(Icons.Default.Close, contentDescription = "Sair do Modo Cinema", tint = Color.White)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(y = 10.dp).zIndex(2f).padding(start = 14.dp)
            ) {
                Surface(
                    color = Color(0xFF1E2030),
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 6.dp, bottomStart = 6.dp),
                    border = BorderStroke(1.5.dp, Ouro),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(nomePersonagem, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("${humor.first} ${humor.second}", color = Ouro, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Surface(
                color = Color(0xFF0C0D16).copy(alpha = 0.94f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Ouro.copy(alpha = 0.6f), Color(0xFF7C4DFF).copy(alpha = 0.4f)))),
                shadowElevation = 14.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = narrativaAtual.take(visivelCinema),
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (terminouTexto) "▼ Toque para avançar" else "•••",
                            color = Ouro.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${vinculo.icone} Vínculo: ${vinculo.titulo}",
                            color = Color(0xFFFF80AB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JogoTela(vm: AppViewModel) {
    val jogo = vm.jogoAtual ?: return
    if (jogo.modo == "historia" || vm.modoLeitura == AppViewModel.ModoLeitura.HISTORIA) {
        JogoModoHistoria(vm)
        return
    }
    val chat = vm.chat
    val contexto = LocalContext.current
    val haptico = LocalHapticFeedback.current
    val areaDeCopia = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.erro, vm.aviso, vm.erroRetrato) {
        vm.erro?.let { snackbarHostState.showSnackbar(it); vm.limparErro() }
        vm.aviso?.let { snackbarHostState.showSnackbar(it); vm.limparAviso() }
        vm.erroRetrato?.let { snackbarHostState.showSnackbar(it); vm.limparErroRetrato() }
    }

    // Fala o texto do narrador em voz alta.
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(contexto) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("pt", "BR")
        }
        tts = engine
        onDispose { engine.stop(); engine.shutdown(); SintetizadorAmbienteLivennIA.parar() }
    }

    val listaChat = rememberLazyListState()

    // Efeito "máquina de escrever" só na última fala do narrador.
    val narrativaAtual = chat.lastOrNull()?.narrativa ?: jogo.cena.narrativa
    var visiveis by remember(narrativaAtual) { mutableIntStateOf(0) }
    LaunchedEffect(narrativaAtual) {
        val ultimo = chat.size - 1
        visiveis = 0
        while (visiveis < narrativaAtual.length) {
            delay(14)
            visiveis = minOf(narrativaAtual.length, visiveis + 3)
            if (ultimo >= 0) {
                listaChat.scrollToItem(ultimo, 100000)
            }
        }
        if (ultimo >= 0) {
            listaChat.animateScrollToItem(ultimo, 100000)
        }
    }
    val terminou = visiveis >= narrativaAtual.length

    // ---- Efeito de dano ----
    val tremor = remember { Animatable(0f) }
    LaunchedEffect(vm.danoRecente) {
        if (vm.danoRecente > 0) {
            haptico.performHapticFeedback(HapticFeedbackType.LongPress)
            repeat(6) { i -> tremor.animateTo(if (i % 2 == 0) 10f else -10f, tween(45)) }
            tremor.animateTo(0f, tween(60))
            delay(450)
            vm.limparDano()
        }
    }

    var acaoLivre by remember(jogo.turno) { mutableStateOf("") }
    var dialogo by remember { mutableStateOf<DialogoAberto?>(null) }
    var indiceEdicao by remember { mutableStateOf<Int?>(null) }
    var modoCinema by remember { mutableStateOf(false) }
    var modalPresentesAberto by remember { mutableStateOf(false) }
    var modalGaleriaAberto by remember { mutableStateOf(false) }
    var mostrarDialogoIlustracaoGratis by remember { mutableStateOf(false) }
    var simulandoAnuncioIlustracao by remember { mutableStateOf(false) }
    val corFala = MaterialTheme.colorScheme.onBackground
    val corAcao = TextoSuave
    val corNormal = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.90f)

    LaunchedEffect(chat.size, vm.ocupado, vm.acaoPendente) {
        // Itens extras no fim da lista: a fala já escolhida (enquanto a IA escreve a
        // "situacao" dela) e o indicador de "digitando".
        val extras = if (vm.ocupado) (if (vm.acaoPendente != null) 2 else 1) else 0
        val alvo = chat.size + extras - 1
        if (alvo >= 0) listaChat.animateScrollToItem(alvo, 100000)
    }

    BackHandler { vm.voltarParaPersonagens() }

    var mostrarPersonaModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (modoCinema) {
            ModoCinemaVisualNovel(
                jogo = jogo,
                chat = chat,
                vm = vm,
                narrativaAtual = narrativaAtual,
                onFechar = { modoCinema = false }
            )
        }
        // Arte de Fundo Imersiva Full-Bleed do Cenário / Personagem (RPG)
        val cenarioId = jogo.cenario?.id
        val urlFundo = remember(jogo.cenario, jogo.retrato, vm.capaVersao, vm.retratoVersao) {
            val base = ApiClient.baseUrlAtual() ?: ""
            when {
                jogo.cenario?.capa == true && !cenarioId.isNullOrBlank() -> "${base}catalogo/$cenarioId/capa?v=${vm.capaVersao}"
                jogo.retrato -> "${base}jogos/${jogo.jogoId}/retrato?v=${vm.retratoVersao}"
                !cenarioId.isNullOrBlank() -> "${base}catalogo/$cenarioId/capa?v=${vm.capaVersao}"
                else -> null
            }
        }
        if (urlFundo != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(urlFundo)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF090A0F)))
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
                    )
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
            )
        }

        // Película escura suave em degradê
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.70f),
                            Color.Black.copy(alpha = 0.50f),
                            Color.Black.copy(alpha = 0.90f)
                        )
                    )
                )
        )
        ParticulasFlutuantesLivennIA(modifier = Modifier.fillMaxSize(), quantidade = 18, cor = Ouro)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .graphicsLayer { translationX = tremor.value }
        ) {
            // ---- Barra de cima: flutua sobre o fundo, sem faixa dura ----
            Surface(color = Color.Transparent) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.voltarParaPersonagens() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    RetratoPersonagem(
                        jogoId = jogo.jogoId,
                        seed = jogo.avatarSeed,
                        nome = jogo.personagem.nome,
                        temRetrato = jogo.retrato,
                        tamanho = 38,
                        versao = vm.retratoVersao
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            jogo.personagem.nome,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            jogo.cenario?.let { c ->
                                "${c.emoji} ${c.nome} • turno ${jogo.turno}"
                            } ?: "${jogo.personagem.local} • turno ${jogo.turno}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSuave,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (vm.gerandoRetrato) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Ouro
                        )
                        Spacer(Modifier.width(4.dp))
                    } else {
                        IconButton(onClick = { vm.gerarRetrato() }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Gerar retrato",
                                tint = TextoSuave,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Surface(
                        color = if (vm.partyAtiva.isNotEmpty()) Ouro.copy(alpha = 0.20f) else SuperficieAlta,
                        shape = Cantos.pilula,
                        border = BorderStroke(1.dp, if (vm.partyAtiva.isNotEmpty()) Ouro.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .clickable { dialogo = DialogoAberto.Party }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text("👥", fontSize = 11.sp)
                            Spacer(Modifier.width(3.dp))
                            Text(
                                if (vm.partyAtiva.isNotEmpty()) "Party (${vm.partyAtiva.size})" else "Party",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (vm.partyAtiva.isNotEmpty()) Ouro else TextoSuave
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Surface(
                        color = SuperficieAlta,
                        shape = Cantos.pilula,
                        modifier = Modifier
                            .clickable { vm.definirModoLeitura(AppViewModel.ModoLeitura.HISTORIA) }
                    ) {
                        Text(
                            "Livro",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ouro,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = {
                        tts?.stop()
                        modularVozTTS(tts, jogo.genero, jogo.cenario?.nome)
                        tts?.speak(narrativaAtual, TextToSpeech.QUEUE_FLUSH, null, "narrativa")
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ouvir",
                            tint = Ouro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { dialogo = DialogoAberto.Feedback }) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Avaliar / Feedback",
                            tint = Ouro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Card Flutuante de Introdução do Cenário (Imagem 1 e 5)
            if (jogo.cenario != null) {
                Surface(
                    color = Color(0xFF13141F).copy(alpha = 0.88f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = jogo.cenario?.nome ?: jogo.personagem.nome,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "@${jogo.cenario?.autorNome ?: "LivennIA"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFF5C842),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable {
                                    Toast.makeText(contexto, "História salva nos seus favoritos!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("+ Salvar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Intro. ${jogo.cenario?.sinopse?.ifBlank { null } ?: jogo.cena.narrativa}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // ---- Barra de recursos LivennIA: Livens, Memória, Diário, Cartas, Saves, Motor/Temp ----
            BarraFerramentasLiven(
                vm = vm,
                jogo = jogo,
                onAbrirDialogo = { dialogo = it },
                onAtivarModoCinema = { modoCinema = true },
                onExportarPdf = { exportarHistoriaPdf(contexto, jogo, chat) },
                onAbrirPresentes = { modalPresentesAberto = true },
                onAbrirGaleria = { modalGaleriaAberto = true }
            )

            // ---- Termômetro de Vínculo & Nível de Afinidade (Estilo Emochi/Visual Novel) ----
            val progressoVinculo = remember(jogo.turno) {
                ((jogo.turno * 12) % 100) / 100f
            }
            val nivelVinculo = remember(jogo.turno) {
                when {
                    jogo.turno <= 3 -> "Desconhecido"
                    jogo.turno <= 7 -> "Curioso"
                    jogo.turno <= 14 -> "Amistoso"
                    jogo.turno <= 22 -> "Companheiro Leal"
                    else -> "Vínculo Eterno"
                }
            }
            val humorAtual = remember(narrativaAtual, jogo.turno) { extrairHumorPersonagem(narrativaAtual, jogo.turno) }
            Surface(
                color = Color(0xFF131522),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.35f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clickable { dialogo = DialogoAberto.Vinculo }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💖", fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Vínculo: $nivelVinculo  •  ${humorAtual.first} ${humorAtual.second}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF80AB),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF232538))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressoVinculo.coerceAtLeast(0.15f))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF4081), Color(0xFFFF80AB))
                                    )
                                )
                        )
                    }
                }
            }

            // ---- Vida e itens: Mochila Interativa 3D ----
            var modalInventarioAberto by remember { mutableStateOf(false) }

            Surface(
                color = SuperficieAlta,
                shape = Cantos.cartao,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    BarraDeVida(vida = jogo.personagem.vida, vidaMax = jogo.personagem.vidaMax)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🎒 Mochila & Equipamentos (${jogo.personagem.inventario.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ouro,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Ver Todos ➔",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSuave,
                            modifier = Modifier.cliqueFuturista3D { modalInventarioAberto = true }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    if (jogo.personagem.inventario.isEmpty()) {
                        Text("Mochila vazia no momento", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                    } else {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            jogo.personagem.inventario.forEach { itemNome ->
                                val icone = when {
                                    "espada" in itemNome.lowercase() || "lâmina" in itemNome.lowercase() || "adaga" in itemNome.lowercase() -> "🗡️"
                                    "poção" in itemNome.lowercase() || "cura" in itemNome.lowercase() || "elixir" in itemNome.lowercase() -> "🧪"
                                    "escudo" in itemNome.lowercase() || "armadura" in itemNome.lowercase() -> "🛡️"
                                    "chave" in itemNome.lowercase() -> "🗝️"
                                    "pergaminho" in itemNome.lowercase() || "livro" in itemNome.lowercase() || "mapa" in itemNome.lowercase() -> "📜"
                                    "ouro" in itemNome.lowercase() || "moeda" in itemNome.lowercase() -> "🪙"
                                    "anel" in itemNome.lowercase() || "amuleto" in itemNome.lowercase() || "joia" in itemNome.lowercase() -> "💍"
                                    else -> "🎒"
                                }
                                Surface(
                                    color = Color(0xFF181A26),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                                    modifier = Modifier.cliqueFuturista3D {
                                        acaoLivre = "Usar $itemNome"
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(icone, fontSize = 12.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(itemNome, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (modalInventarioAberto) {
                ModalInventarioRPG(
                    jogo = jogo,
                    onUsarItem = { item ->
                        acaoLivre = "Usar $item"
                        modalInventarioAberto = false
                    },
                    onFechar = { modalInventarioAberto = false }
                )
            }

            // ---- Conversa ----
            LazyColumn(
                state = listaChat,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                items(chat.size) { indice ->
                    val msg = chat[indice]
                    val ehUltimo = indice == chat.size - 1
                    val textoMostrado = if (ehUltimo) msg.narrativa.take(visiveis) else msg.narrativa

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (indice > 0) {
                            BalaoJogador(
                                texto = msg.situacao ?: msg.acao,
                                tamanhoLetra = vm.tamanhoLetraNarrador,
                                hora = msg.emJogador
                            )
                        }
                        val blocos = remember(textoMostrado) { extrairBlocosFala(textoMostrado) }
                        if (blocos.size > 1 || (blocos.size == 1 && blocos.first().personagem != null)) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                blocos.forEachIndexed { bIdx, bloco ->
                                    val ehUltimoBloco = bIdx == blocos.size - 1
                                    CardFalaGrupo(
                                        bloco = bloco,
                                        party = vm.partyAtiva,
                                        tamanhoLetra = vm.tamanhoLetraNarrador,
                                        corFala = corFala,
                                        corAcao = corAcao,
                                        corNormal = corNormal,
                                        ehModoHistoria = false,
                                        clicavel = ehUltimo && !terminou,
                                        aoTocar = { visiveis = narrativaAtual.length },
                                        aoCopiar = { areaDeCopia.setText(AnnotatedString(bloco.texto)) },
                                        aoOuvir = {
                                            tts?.stop()
                                            modularVozTTS(tts, jogo.genero, jogo.cenario?.nome)
                                            tts?.speak(bloco.texto, TextToSpeech.QUEUE_FLUSH, null, "fala_${indice}_$bIdx")
                                        },
                                        rodape = if (ehUltimoBloco) {
                                            {
                                                if (msg.eventos.isNotEmpty() && (!ehUltimo || terminou)) {
                                                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                                        for (ev in msg.eventos) {
                                                            val cor = when {
                                                                ev.tipo == "vida" && ev.texto.contains("perdeu") -> MaterialTheme.colorScheme.error
                                                                ev.tipo == "vida" -> Color(0xFF7BC47F)
                                                                ev.tipo == "item_ganho" -> Ouro
                                                                else -> TextoSuave
                                                            }
                                                            Text("• ${ev.texto}", color = cor, style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                                RodapeMensagemLiven(
                                                    rotuloEsquerda = msg.emNarrador?.let { horaDaMensagem(it) } ?: "Turno ${indice + 1}",
                                                    versoes = msg.versoes,
                                                    versaoAtiva = msg.versaoAtiva,
                                                    ehUltimo = ehUltimo && !vm.ocupado,
                                                    aoTrocarVersao = { nova -> vm.trocarVersaoMensagem(indice, nova) },
                                                    aoRegenerar = { vm.regenerarUltimaResposta() },
                                                    aoEditar = { indiceEdicao = indice },
                                                    aoCopiar = { areaDeCopia.setText(AnnotatedString(msg.narrativa)) }
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        } else {
                            BlocoNarrador(
                                texto = textoMostrado,
                                eventos = if (ehUltimo && !terminou) emptyList() else msg.eventos,
                                clicavel = ehUltimo && !terminou,
                                aoTocar = { visiveis = narrativaAtual.length },
                                aoCopiar = { areaDeCopia.setText(AnnotatedString(msg.narrativa)) },
                                tamanhoLetra = vm.tamanhoLetraNarrador,
                                hora = msg.emNarrador,
                                versoes = msg.versoes,
                                versaoAtiva = msg.versaoAtiva,
                                ehUltimo = ehUltimo && !vm.ocupado,
                                aoTrocarVersao = { nova -> vm.trocarVersaoMensagem(indice, nova) },
                                aoRegenerar = { vm.regenerarUltimaResposta() },
                                aoEditar = { indiceEdicao = indice }
                            )
                        }
                    }
                }

                if (!jogo.cena.imagemUrl.isNullOrBlank()) {
                    item(key = "arte_cena_atual") {
                        val base = remember { ApiClient.baseUrlAtual() }
                        val fullUrl = remember(jogo.cena.imagemUrl, base) {
                            val u = jogo.cena.imagemUrl
                            if (base != null && u != null) "${base}${u.removePrefix("/")}" else u
                        }
                        if (fullUrl != null) {
                            val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFF10121C),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.5.dp, Ouro.copy(alpha = 0.55f)),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(fullUrl)
                                                .apply { if (cabecalho != null) addHeader("Authorization", cabecalho) }
                                                .crossfade(true)
                                                .allowHardware(true)
                                                .build(),
                                            contentDescription = "Cena Ilustrada da História",
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.TopCenter,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.70f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✨ Ilustração da Cena", fontSize = 11.sp, color = Ouro, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!jogo.cena.imagemUrl.isNullOrBlank()) {
                    item(key = "arte_cena_atual_historia") {
                        val base = remember { ApiClient.baseUrlAtual() }
                        val fullUrl = remember(jogo.cena.imagemUrl, base) {
                            val u = jogo.cena.imagemUrl
                            if (base != null && u != null) "${base}${u.removePrefix("/")}" else u
                        }
                        if (fullUrl != null) {
                            val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFF10121C),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.5.dp, Ouro.copy(alpha = 0.55f)),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(fullUrl)
                                                .apply { if (cabecalho != null) addHeader("Authorization", cabecalho) }
                                                .crossfade(true)
                                                .allowHardware(true)
                                                .build(),
                                            contentDescription = "Arte da Cena Atual",
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.60f),
                                            shape = Cantos.pilula,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✨ Ilustração da Cena Gerada por IA", style = MaterialTheme.typography.labelSmall, color = Ouro)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (vm.ocupado) {
                    vm.acaoPendente?.let { pendente ->
                        item {
                            BalaoJogador(
                                texto = pendente,
                                tamanhoLetra = vm.tamanhoLetraNarrador,
                                hora = null,
                                pendente = true
                            )
                        }
                    }
                    item { BlocoDigitando() }
                }

                if (!jogo.cena.imagemUrl.isNullOrBlank()) {
                    item(key = "arte_cena_atual_historia") {
                        val base = remember { ApiClient.baseUrlAtual() }
                        val fullUrl = remember(jogo.cena.imagemUrl, base) {
                            val u = jogo.cena.imagemUrl
                            if (base != null && u != null) "${base}${u.removePrefix("/")}" else u
                        }
                        if (fullUrl != null) {
                            val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFF10121C),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.5.dp, Ouro.copy(alpha = 0.55f)),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(fullUrl)
                                                .apply { if (cabecalho != null) addHeader("Authorization", cabecalho) }
                                                .crossfade(true)
                                                .allowHardware(true)
                                                .build(),
                                            contentDescription = "Cena Ilustrada da História",
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.TopCenter,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.70f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✨ Ilustração da Cena", fontSize = 11.sp, color = Ouro, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item(key = "fim_chat_espaco") {
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ---- Faixa de status + opções + barra de digitação ----
            Surface(
                color = Superficie,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChipTag("⚡ ${jogo.jogadasRestantesHoje}", destaque = true)
                            ChipTag(
                                "❤ ${jogo.personagem.vida}/${jogo.personagem.vidaMax}",
                                destaque = jogo.personagem.vida <= 30
                            )
                            ChipTag("📖 ${jogo.genero}")
                            Surface(
                                color = SuperficieAlta,
                                shape = Cantos.pilula,
                                border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f)),
                                modifier = Modifier.cliqueFuturista3D {
                                    if (!vm.ocupado && !vm.gerandoAutoResposta) {
                                        vm.gerarSugestoesAutoResposta { primeira ->
                                            if (acaoLivre.isBlank()) acaoLivre = primeira
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    if (vm.gerandoAutoResposta) "⏳ Inspirando..." else "✨ Auto-resposta",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Ouro,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                color = if (jogo.cena.imagemUrl != null) Ouro.copy(alpha = 0.20f) else SuperficieAlta,
                                shape = Cantos.pilula,
                                border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f)),
                                modifier = Modifier.cliqueFuturista3D {
                                    if (!vm.ocupado && !vm.ilustrandoCena) {
                                        val ehPago = (jogo.modelo?.ehPago == true) || (vm.conta?.premium == true)
                                        if (ehPago) {
                                            vm.ilustrarCenaAtual(
                                                comAnuncio = false,
                                                onSucesso = { Toast.makeText(contexto, "Cena ilustrada com sucesso!", Toast.LENGTH_SHORT).show() },
                                                onError = { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
                                            )
                                        } else {
                                            mostrarDialogoIlustracaoGratis = true
                                        }
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (vm.ilustrandoCena) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Ouro)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Pintando cena...", style = MaterialTheme.typography.labelSmall, color = Ouro)
                                    } else {
                                        Text(
                                            if (jogo.cena.imagemUrl != null) "🖼️ Arte da Cena •" else "✨ 🖼️ Ilustrar Cena",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Ouro
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            "✎",
                            color = TextoSuave,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clickable { dialogo = DialogoAberto.Feedback }
                                .padding(6.dp)
                        )
                    }

                    // Gaveta de Auto-Respostas LivennIA
                    val sugestoesAtuais = remember(vm.sugestoesAutoResposta, jogo.cena.opcoes) {
                        if (vm.sugestoesAutoResposta.isNotEmpty()) {
                            vm.sugestoesAutoResposta
                        } else if (jogo.cena.opcoes.isNotEmpty()) {
                            jogo.cena.opcoes
                        } else {
                            listOf(
                                "Perguntar mais sobre isso",
                                "Observar a reação com atenção",
                                "Dar um passo à frente com cautela"
                            )
                        }
                    }
                    FaixaSugestoesAutoResposta(
                        sugestoes = sugestoesAtuais,
                        ocupado = vm.ocupado,
                        gerandoIA = vm.gerandoAutoResposta,
                        aoPedirSugestoesIA = { vm.gerarSugestoesAutoResposta() },
                        aoEscolher = { sugestao ->
                            acaoLivre = sugestao
                            haptico.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        }
                    )

                    if (jogo.fimDeJogo) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                "Fim de jogo. ${jogo.personagem.nome} não resistiu.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { vm.voltarParaPersonagens() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Voltar às aventuras") }
                        }
                    } else {
                        if (!terminou) {
                            Text(
                                "Toque na fala do narrador para pular a animação.",
                                color = TextoSuave,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        } else if (jogo.cena.opcoes.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                jogo.cena.opcoes.forEachIndexed { indice, opcao ->
                                    Surface(
                                        color = SuperficieAlta,
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Ouro.copy(alpha = 0.55f)),
                                        modifier = Modifier.clickable(enabled = !vm.ocupado) {
                                            haptico.performHapticFeedback(HapticFeedbackType.LongPress)
                                            vm.jogarOpcao(indice + 1)
                                        }
                                    ) {
                                        Text(
                                            text = opcao,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            RetratoPersonagem(
                                jogoId = jogo.jogoId,
                                seed = jogo.avatarSeed,
                                nome = jogo.personagem.nome,
                                temRetrato = jogo.retrato,
                                tamanho = 34,
                                versao = vm.retratoVersao
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = acaoLivre,
                                onValueChange = { acaoLivre = it.take(300) },
                                placeholder = { Text("Escreva sua ação...") },
                                maxLines = 3,
                                enabled = !vm.ocupado,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(4.dp))
                            IconButton(
                                onClick = { dialogo = DialogoAberto.Dados },
                                enabled = !vm.ocupado
                            ) {
                                Text("🎲", fontSize = 20.sp)
                            }
                            IconButton(
                                onClick = {
                                    val texto = acaoLivre
                                    acaoLivre = ""
                                    vm.jogarAcao(texto)
                                },
                                enabled = !vm.ocupado && acaoLivre.isNotBlank()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar",
                                    tint = Ouro
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---- Efeito de dano: piscada vermelha + selo "-X ❤" ----
        AnimatedVisibility(
            visible = vm.danoRecente > 0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0x4DFF0000))) {
                Surface(
                    color = Color(0xE6B3261E),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.align(Alignment.Center).padding(top = 40.dp)
                ) {
                    Text(
                        text = "-${vm.danoRecente} ❤",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    GerenciadorDialogosJogo(
        vm = vm,
        jogo = jogo,
        dialogo = dialogo,
        indiceEdicao = indiceEdicao,
        onFecharDialogo = { dialogo = null },
        onFecharEdicao = { indiceEdicao = null },
        aoInserirNoChat = { texto -> acaoLivre = if (acaoLivre.isBlank()) texto else "$acaoLivre $texto" }
    )

    if (modalPresentesAberto) {
        val nomeP = jogo.cenario?.nome ?: "Personagem"
        val saldo = vm.conta?.livens ?: jogo.livens ?: 0
        ModalPresentearPersonagem(
            nomePersonagem = nomeP,
            saldoLivens = saldo,
            onEnviarPresente = { presente ->
                modalPresentesAberto = false
                vm.jogarAcao(presente.acaoNarrativa)
                Toast.makeText(contexto, "Você presenteou $nomeP com ${presente.icone} ${presente.nome}!", Toast.LENGTH_SHORT).show()
            },
            onFechar = { modalPresentesAberto = false }
        )
    }

    if (modalGaleriaAberto) {
        val nomeP = jogo.cenario?.nome ?: "Personagem"
        val vinculoAtual = calcularVinculo(jogo.turno).nivel
        ModalGaleriaMomentos(
            nomePersonagem = nomeP,
            cenarioId = jogo.cenario?.id,
            nivelVinculoAtual = vinculoAtual,
            onFechar = { modalGaleriaAberto = false }
        )
    }

    if (mostrarDialogoIlustracaoGratis) {
        val nomeModelo = jogo.modelo?.nome ?: "Liven Flash (Grátis)"
        DialogoIlustracaoCenaOpcoes(
            nomeModeloAtual = nomeModelo,
            onAssistirAnuncio = {
                mostrarDialogoIlustracaoGratis = false
                simulandoAnuncioIlustracao = true
            },
            onMelhorarPlano = {
                mostrarDialogoIlustracaoGratis = false
                vm.navegarPara(Tela.Assinatura("Ultra"))
            },
            onFechar = { mostrarDialogoIlustracaoGratis = false }
        )
    }

    if (simulandoAnuncioIlustracao) {
        DialogoSimuladorAnuncio(
            onConcluido = {
                simulandoAnuncioIlustracao = false
                vm.ilustrarCenaAtual(
                    comAnuncio = true,
                    onSucesso = { Toast.makeText(contexto, "Cena ilustrada com sucesso!", Toast.LENGTH_SHORT).show() },
                    onError = { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
                )
            },
            onFechar = { simulandoAnuncioIlustracao = false }
        )
    }

    if (mostrarPersonaModal) {
        ModalMinhaPersona(
            vm = vm,
            jogoAtualId = jogo.jogoId,
            onFechar = { mostrarPersonaModal = false }
        )
    }

    SnackbarHost(hostState = snackbarHostState)
    Confetti(explosao = jogo.turno == 1)
}

// Hora de cada fala, no formato do aparelho (ex.: "14:32").
private val formatoDaHora = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())

private fun horaDaMensagem(milissegundos: Long): String =
    formatoDaHora.format(java.util.Date(milissegundos))

// ---- Fala do jogador: bolha colorida à direita, com a hora embaixo ----
@Composable
private fun BalaoJogador(
    texto: String,
    tamanhoLetra: Float,
    hora: Long?,
    pendente: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            color = if (pendente) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.primary
            },
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp),
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            Text(
                text = texto,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (tamanhoLetra - 1f).sp,
                    lineHeight = ((tamanhoLetra - 1f) * 1.4f).sp
                ),
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
            )
        }
        if (hora != null) {
            Text(
                text = horaDaMensagem(hora),
                color = TextoSuave.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(end = 4.dp, top = 3.dp)
            )
        }
    }
}

// ---- Fala do narrador: texto literário e controles LivennIA no rodapé ----
@Composable
private fun BlocoNarrador(
    texto: String,
    eventos: List<EventoDto>,
    clicavel: Boolean,
    aoTocar: () -> Unit,
    aoCopiar: () -> Unit,
    tamanhoLetra: Float,
    hora: Long? = null,
    versoes: List<String> = emptyList(),
    versaoAtiva: Int = 0,
    ehUltimo: Boolean = false,
    aoTrocarVersao: (Int) -> Unit = {},
    aoRegenerar: () -> Unit = {},
    aoEditar: () -> Unit = {}
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.Top) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 3.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = Ouro,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "⧉",
                color = TextoSuave,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { aoCopiar() }.padding(2.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Surface(
            color = SuperficieAlta,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Ouro.copy(alpha = 0.22f)),
            modifier = Modifier.weight(1f).clickable(enabled = clicavel) { aoTocar() }
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = parsearNarrativa(texto),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = tamanhoLetra.sp,
                        lineHeight = (tamanhoLetra * 1.45f).sp
                    )
                )
                if (eventos.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    for (ev in eventos) {
                        val cor = when {
                            ev.tipo == "vida" && ev.texto.contains("perdeu") -> MaterialTheme.colorScheme.error
                            ev.tipo == "vida" -> Color(0xFF7BC47F)
                            ev.tipo == "item_ganho" -> Ouro
                            else -> TextoSuave
                        }
                        Text("• ${ev.texto}", color = cor, style = MaterialTheme.typography.labelSmall)
                    }
                }
                var revelouPensamento by remember { mutableStateOf(false) }
                Row(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                    Surface(
                        color = if (revelouPensamento) Color(0xFF6C5CE7).copy(alpha = 0.20f) else Color.White.copy(alpha = 0.06f),
                        shape = Cantos.pilula,
                        border = BorderStroke(1.dp, if (revelouPensamento) Color(0xFF6C5CE7).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.clickable { revelouPensamento = !revelouPensamento }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (revelouPensamento) "💭 Ocultar Pensamento" else "💭 Revelar Pensamento", color = if (revelouPensamento) Color(0xFFA29BFE) else TextoSuave, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (revelouPensamento) {
                    Surface(
                        color = Color(0xFF16152B),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.40f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = gerarPensamentoSecreto("Personagem", texto, 5),
                            color = Color(0xFFD6D0F5),
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                RodapeMensagemLiven(
                    rotuloEsquerda = hora?.let { horaDaMensagem(it) } ?: "",
                    versoes = versoes,
                    versaoAtiva = versaoAtiva,
                    ehUltimo = ehUltimo,
                    aoTrocarVersao = aoTrocarVersao,
                    aoRegenerar = aoRegenerar,
                    aoEditar = aoEditar,
                    aoCopiar = aoCopiar
                )
            }
        }
    }
}

// ---- Balão de fala de companheiro ou mestre em chat de grupo / RPG de mesa ----
@Composable
private fun CardFalaGrupo(
    bloco: BlocoFalaPersonagem,
    party: List<MembroPartyDto>,
    tamanhoLetra: Float,
    corFala: Color,
    corAcao: Color,
    corNormal: Color,
    ehModoHistoria: Boolean = false,
    clicavel: Boolean = false,
    aoTocar: () -> Unit = {},
    aoCopiar: () -> Unit = {},
    aoOuvir: (() -> Unit)? = null,
    rodape: (@Composable () -> Unit)? = null
) {
    val membro = remember(bloco.personagem, party) {
        party.firstOrNull { it.nome.equals(bloco.personagem, ignoreCase = true) }
    }
    val corDestaque = remember(bloco.personagem, bloco.isMestre, ehModoHistoria) {
        when {
            bloco.isMestre && ehModoHistoria -> Color(0xFFB388FF)
            bloco.isMestre -> Color(0xFFFFD54F)
            else -> corDoPersonagem(bloco.personagem)
        }
    }
    val papel = membro?.papel ?: if (bloco.isMestre) {
        if (ehModoHistoria) "Narrador" else "Mestre da Mesa"
    } else "Personagem"
    val avatarUrl = membro?.avatarUrl
    val nomeExibicao = bloco.personagem ?: if (bloco.isMestre) {
        if (ehModoHistoria) "Narrador" else "Mestre"
    } else "Narrador"

    Surface(
        color = Color(0xFF13141F).copy(alpha = 0.92f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, corDestaque.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clicavel) { aoTocar() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabeçalho com Avatar, Nome, Papel e Áudio
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = nomeExibicao,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, corDestaque, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(corDestaque.copy(alpha = 0.22f))
                            .border(1.5.dp, corDestaque, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bloco.isMestre) {
                            Text(if (ehModoHistoria) "📜" else "🎲", fontSize = 16.sp)
                        } else {
                            val emoji = membro?.emoji ?: bloco.personagem?.take(1)?.uppercase() ?: "👤"
                            Text(emoji, fontSize = 15.sp)
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = corDestaque.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, corDestaque.copy(alpha = 0.50f))
                        ) {
                            Text(
                                text = nomeExibicao,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = corDestaque,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "• $papel",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSuave
                        )
                    }
                }
                if (aoOuvir != null) {
                    IconButton(
                        onClick = aoOuvir,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ouvir fala",
                            tint = corDestaque.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Corpo da fala com estilo literário
            Text(
                text = parsearNarrativaLivro(
                    texto = bloco.texto,
                    corFala = corFala,
                    corAcao = corAcao,
                    corNormal = corNormal
                ),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = tamanhoLetra.sp,
                    lineHeight = (tamanhoLetra * 1.55f).sp
                )
            )

            if (rodape != null) {
                Spacer(Modifier.height(10.dp))
                rodape()
            }
        }
    }
}

// ---- Indicador de que o narrador está escrevendo (Estilo 3D com Bolhas Orgânicas) ----
@Composable
private fun BlocoDigitando(nomePersonagem: String = "Digitando...") {
    val infiniteTransition = rememberInfiniteTransition(label = "bolhasDigitando")
    val ponto1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(450),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )
    val ponto2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )
    val ponto3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFF131522),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.35f)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = nomePersonagem,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSuave,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .offset(y = ponto1.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Ouro)
                )
                Box(
                    modifier = Modifier
                        .offset(y = ponto2.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C4DFF))
                )
                Box(
                    modifier = Modifier
                        .offset(y = ponto3.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                )
            }
        }
    }
}


@Composable
private fun JogoModoHistoria(vm: AppViewModel) {
    val jogo = vm.jogoAtual ?: return
    val ehHistoriaPura = jogo.modo == "historia"
    val chat = vm.chat
    val contexto = LocalContext.current
    val areaDeCopia = LocalClipboardManager.current
    val haptico = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listaChat = rememberLazyListState()

    LaunchedEffect(vm.erro, vm.aviso, vm.erroRetrato) {
        vm.erro?.let { snackbarHostState.showSnackbar(it); vm.limparErro() }
        vm.aviso?.let { snackbarHostState.showSnackbar(it); vm.limparAviso() }
        vm.erroRetrato?.let { snackbarHostState.showSnackbar(it); vm.limparErroRetrato() }
    }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(contexto) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("pt", "BR")
        }
        tts = engine
        onDispose { engine.stop(); engine.shutdown(); SintetizadorAmbienteLivennIA.parar() }
    }

    val narrativaAtual = chat.lastOrNull()?.narrativa ?: jogo.cena.narrativa
    var visiveis by remember(narrativaAtual) { mutableIntStateOf(0) }
    LaunchedEffect(narrativaAtual) {
        val ultimo = chat.size - 1
        visiveis = 0
        while (visiveis < narrativaAtual.length) {
            delay(14)
            visiveis = minOf(narrativaAtual.length, visiveis + 3)
            if (ultimo >= 0) {
                listaChat.scrollToItem(ultimo, 100000)
            }
        }
        if (ultimo >= 0) {
            listaChat.animateScrollToItem(ultimo, 100000)
        }
    }
    val terminou = visiveis >= narrativaAtual.length

    var acaoLivre by remember(jogo.turno) { mutableStateOf("") }
    var mostrarSugestoes by remember(jogo.turno) { mutableStateOf(false) }
    var dialogo by remember { mutableStateOf<DialogoAberto?>(null) }
    var indiceEdicao by remember { mutableStateOf<Int?>(null) }
    var modoCinema by remember { mutableStateOf(false) }
    var modalPresentesAberto by remember { mutableStateOf(false) }
    var modalGaleriaAberto by remember { mutableStateOf(false) }
    var mostrarDialogoIlustracaoGratis by remember { mutableStateOf(false) }
    var simulandoAnuncioIlustracao by remember { mutableStateOf(false) }

    LaunchedEffect(chat.size, vm.ocupado, vm.acaoPendente) {
        val extras = if (vm.ocupado) (if (vm.acaoPendente != null) 2 else 1) else 0
        val alvo = chat.size + extras - 1
        if (alvo >= 0) listaChat.animateScrollToItem(alvo, 100000)
    }

    BackHandler { vm.voltarParaPersonagens() }

    val corFala = MaterialTheme.colorScheme.onBackground
    val corAcao = TextoSuave.copy(alpha = 0.90f)
    val corNormal = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.93f)
    var mostrarPersonaModal by remember { mutableStateOf(false) }
    var mostrarPerfilHistoria by remember { mutableStateOf(false) }

    val cenarioCompleto = remember(jogo.cenario, vm.catalogo) {
        val cid = jogo.cenario?.id
        if (cid != null) {
            vm.catalogo.firstOrNull { it.id == cid } ?: CenarioDto(
                id = cid,
                nome = jogo.cenario?.nome ?: jogo.personagem.nome,
                subtitulo = jogo.cenario?.subtitulo ?: "",
                genero = jogo.genero,
                sinopse = jogo.cenario?.sinopse ?: "",
                tags = jogo.cenario?.tags ?: emptyList(),
                emoji = jogo.cenario?.emoji ?: "📖",
                capa = jogo.cenario?.capa ?: false,
                autorNome = jogo.cenario?.autorNome ?: "LivennIA"
            )
        } else {
            CenarioDto(
                id = "custom_${jogo.jogoId}",
                nome = jogo.personagem.nome,
                subtitulo = jogo.personagem.local,
                genero = jogo.genero,
                sinopse = jogo.cena.narrativa,
                tags = emptyList(),
                emoji = "📖",
                capa = jogo.retrato,
                autorNome = "LivennIA"
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (modoCinema) {
            ModoCinemaVisualNovel(
                jogo = jogo,
                chat = chat,
                vm = vm,
                narrativaAtual = narrativaAtual,
                onFechar = { modoCinema = false }
            )
        }
        // Arte de Fundo Imersiva Full-Bleed do Cenário / Personagem (Modo História)
        val cenarioId = jogo.cenario?.id
        val urlFundo = remember(jogo.cenario, jogo.retrato, vm.capaVersao, vm.retratoVersao, vm.personaAtiva) {
            val base = ApiClient.baseUrlAtual() ?: ""
            val persona = vm.personaAtiva
            when {
                jogo.cenario?.capa == true && !cenarioId.isNullOrBlank() -> "${base}catalogo/$cenarioId/capa?v=${vm.capaVersao}"
                jogo.retrato -> "${base}jogos/${jogo.jogoId}/retrato?v=${vm.retratoVersao}"
                persona != null && persona.temRetrato -> "${base}personas/${persona.id}/retrato?v=${persona.versaoRetrato}"
                !cenarioId.isNullOrBlank() -> "${base}catalogo/$cenarioId/capa?v=${vm.capaVersao}"
                else -> null
            }
        }
        if (urlFundo != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(urlFundo)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF090A0F)))
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
                    )
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
            )
        }

        // Película escura suave em degradê para legibilidade perfeita
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.70f),
                            Color.Black.copy(alpha = 0.50f),
                            Color.Black.copy(alpha = 0.90f)
                        )
                    )
                )
        )
        ParticulasFlutuantesLivennIA(modifier = Modifier.fillMaxSize(), quantidade = 18, cor = Ouro)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // ---- Cabeçalho imersivo estilo Livro ----
            Surface(color = Color.Transparent) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.voltarParaPersonagens() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Ouro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Avatar da História no topo (ao clicar vai para o perfil da história)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Ouro.copy(alpha = 0.55f), CircleShape)
                            .clickable { mostrarPerfilHistoria = true }
                    ) {
                        CapaCenario(
                            cenarioId = cenarioCompleto.id,
                            temCapa = cenarioCompleto.capa,
                            emoji = cenarioCompleto.emojiSeguro.ifBlank { "📖" },
                            nome = cenarioCompleto.nome,
                            versao = vm.capaVersao,
                            modifier = Modifier.fillMaxSize(),
                            recorte = CircleShape
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable { mostrarPerfilHistoria = true }
                    ) {
                        Text(
                            text = "${cenarioCompleto.emojiSeguro} ${cenarioCompleto.nome}".trim(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(0xFF00E676), CircleShape)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Online • ${cenarioCompleto.autorNomeSeguro} • Cap. ${jogo.turno}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSuave,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    if (!ehHistoriaPura) {
                        Surface(
                            color = SuperficieAlta,
                            shape = Cantos.pilula,
                            modifier = Modifier
                                .clickable { vm.definirModoLeitura(AppViewModel.ModoLeitura.RPG) }
                        ) {
                            Text(
                                "RPG",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ouro,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(onClick = {
                        tts?.stop()
                        modularVozTTS(tts, jogo.genero, jogo.cenario?.nome)
                        tts?.speak(narrativaAtual, TextToSpeech.QUEUE_FLUSH, null, "narrativa")
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ouvir",
                            tint = Ouro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { dialogo = DialogoAberto.Feedback }) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Avaliar / Feedback",
                            tint = Ouro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ---- Barra de recursos LivennIA: Livens, Memória, Diário, Cartas, Saves, Motor/Temp ----
            BarraFerramentasLiven(
                vm = vm,
                jogo = jogo,
                onAbrirDialogo = { dialogo = it },
                onAtivarModoCinema = { modoCinema = true },
                onExportarPdf = { exportarHistoriaPdf(contexto, jogo, chat) },
                onAbrirPresentes = { modalPresentesAberto = true },
                onAbrirGaleria = { modalGaleriaAberto = true }
            )

            // ---- Termômetro de Vínculo & Nível de Afinidade (Estilo Emochi/Visual Novel) ----
            val progressoVinculo = remember(jogo.turno) {
                ((jogo.turno * 12) % 100) / 100f
            }
            val nivelVinculo = remember(jogo.turno) {
                when {
                    jogo.turno <= 3 -> "Desconhecido"
                    jogo.turno <= 7 -> "Curioso"
                    jogo.turno <= 14 -> "Amistoso"
                    jogo.turno <= 22 -> "Companheiro Leal"
                    else -> "Vínculo Eterno"
                }
            }
            val humorAtual = remember(narrativaAtual, jogo.turno) { extrairHumorPersonagem(narrativaAtual, jogo.turno) }
            Surface(
                color = Color(0xFF131522),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.35f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clickable { dialogo = DialogoAberto.Vinculo }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💖", fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Vínculo: $nivelVinculo  •  ${humorAtual.first} ${humorAtual.second}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF80AB),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF232538))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressoVinculo.coerceAtLeast(0.15f))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF4081), Color(0xFFFF80AB))
                                    )
                                )
                        )
                    }
                }
            }

            // ---- Páginas da história e diálogos (Estilo Livro) ----
            LazyColumn(
                state = listaChat,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(chat.size) { indice ->
                    val msg = chat[indice]
                    val ehUltimo = indice == chat.size - 1
                    val textoMostrado = if (ehUltimo) msg.narrativa.take(visiveis) else msg.narrativa
                    val acaoTexto = msg.situacao ?: msg.acao
                    val ocultaAcao = indice == 0 || acaoTexto == "[CONTINUAR_HISTORIA]"

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!ocultaAcao) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .padding(bottom = 3.dp, end = 4.dp)
                                        .clickable { mostrarPersonaModal = true }
                                ) {
                                    val personaAtivaMsg = vm.personaAtiva
                                    val nomeExibicaoMsg = personaAtivaMsg?.nome?.ifBlank { null }
                                        ?: vm.personaNome.ifBlank { null }
                                        ?: vm.apelido.ifBlank { "Minha Persona" }
                                    Text(
                                        text = nomeExibicaoMsg,
                                        color = Ouro.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    if (personaAtivaMsg != null && personaAtivaMsg.temRetrato) {
                                        RetratoPersona(
                                            temRetrato = true,
                                            emojiOuPadrao = personaAtivaMsg.genero.ifBlank { "👤" },
                                            tamanho = 14,
                                            versao = personaAtivaMsg.versaoRetrato,
                                            personaId = personaAtivaMsg.id,
                                            recorte = CircleShape
                                        )
                                    } else if (vm.personaTemRetrato) {
                                        RetratoPersona(
                                            temRetrato = true,
                                            emojiOuPadrao = vm.personaAvatar.ifBlank { "👤" },
                                            tamanho = 14,
                                            versao = vm.personaRetratoVersao,
                                            recorte = CircleShape
                                        )
                                    } else {
                                        Text(vm.personaAvatar.ifBlank { "👤" }, fontSize = 11.sp)
                                    }
                                }
                                Surface(
                                    color = Ouro.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = 18.dp,
                                        bottomEnd = 4.dp
                                    ),
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.38f)),
                                    modifier = Modifier.fillMaxWidth(0.86f)
                                ) {
                                    Text(
                                        text = parsearNarrativaLivro(
                                            texto = acaoTexto,
                                            corFala = corFala,
                                            corAcao = corAcao,
                                            corNormal = corNormal
                                        ),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = (vm.tamanhoLetraNarrador - 0.5f).sp,
                                            lineHeight = ((vm.tamanhoLetraNarrador - 0.5f) * 1.5f).sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                                msg.emJogador?.let { h ->
                                    Text(
                                        text = horaDaMensagem(h),
                                        color = TextoSuave.copy(alpha = 0.65f),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = 3.dp, end = 4.dp)
                                    )
                                }
                            }
                        }

                        val blocos = remember(textoMostrado) { extrairBlocosFala(textoMostrado) }
                        if (blocos.size > 1 || (blocos.size == 1 && blocos.first().personagem != null)) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                blocos.forEachIndexed { bIdx, bloco ->
                                    val ehUltimoBloco = bIdx == blocos.size - 1
                                    CardFalaGrupo(
                                        bloco = bloco,
                                        party = vm.partyAtiva,
                                        tamanhoLetra = vm.tamanhoLetraNarrador,
                                        corFala = corFala,
                                        corAcao = corAcao,
                                        corNormal = corNormal,
                                        ehModoHistoria = true,
                                        clicavel = ehUltimo && !terminou,
                                        aoTocar = { visiveis = narrativaAtual.length },
                                        aoCopiar = { areaDeCopia.setText(AnnotatedString(bloco.texto)) },
                                        aoOuvir = {
                                            tts?.stop()
                                            modularVozTTS(tts, jogo.genero, jogo.cenario?.nome)
                                            tts?.speak(bloco.texto, TextToSpeech.QUEUE_FLUSH, null, "fala_${indice}_$bIdx")
                                        },
                                        rodape = if (ehUltimoBloco) {
                                            {
                                                RodapeMensagemLiven(
                                                    rotuloEsquerda = msg.emNarrador?.let { horaDaMensagem(it) } ?: "Cap. ${indice + 1}",
                                                    versoes = msg.versoes,
                                                    versaoAtiva = msg.versaoAtiva,
                                                    ehUltimo = ehUltimo && !vm.ocupado,
                                                    aoTrocarVersao = { nova -> vm.trocarVersaoMensagem(indice, nova) },
                                                    aoRegenerar = { vm.regenerarUltimaResposta() },
                                                    aoEditar = { indiceEdicao = indice },
                                                    aoCopiar = { areaDeCopia.setText(AnnotatedString(msg.narrativa)) }
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        } else {
                            // Bloco literário clássico da IA / Personagem
                            Surface(
                                color = Color(0xFF13141F).copy(alpha = 0.90f),
                                shape = RoundedCornerShape(
                                    topStart = 6.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = ehUltimo && !terminou) {
                                        visiveis = narrativaAtual.length
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                tts?.stop()
                                                modularVozTTS(tts, jogo.genero, jogo.cenario?.nome)
                                                tts?.speak(msg.narrativa, TextToSpeech.QUEUE_FLUSH, null, "fala_$indice")
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Ouvir",
                                                tint = Color.White.copy(alpha = 0.75f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (!vm.ocupado && !vm.ilustrandoCena) {
                                                    val ehPago = (jogo.modelo?.ehPago == true) || (vm.conta?.premium == true)
                                                    if (ehPago) {
                                                        vm.ilustrarCenaAtual(
                                                            comAnuncio = false,
                                                            onSucesso = { Toast.makeText(contexto, "Cena ilustrada com sucesso!", Toast.LENGTH_SHORT).show() },
                                                            onError = { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
                                                        )
                                                    } else {
                                                        mostrarDialogoIlustracaoGratis = true
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Image,
                                                contentDescription = "Cena",
                                                tint = Color.White.copy(alpha = 0.75f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = parsearNarrativaLivro(
                                            texto = textoMostrado,
                                            corFala = corFala,
                                            corAcao = corAcao,
                                            corNormal = corNormal
                                        ),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = vm.tamanhoLetraNarrador.sp,
                                            lineHeight = (vm.tamanhoLetraNarrador * 1.65f).sp
                                        )
                                    )
                                    var revelouPensamentoHistoria by remember { mutableStateOf(false) }
                                    Row(modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)) {
                                        Surface(
                                            color = if (revelouPensamentoHistoria) Color(0xFF6C5CE7).copy(alpha = 0.20f) else Color.White.copy(alpha = 0.06f),
                                            shape = Cantos.pilula,
                                            border = BorderStroke(1.dp, if (revelouPensamentoHistoria) Color(0xFF6C5CE7).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)),
                                            modifier = Modifier.clickable { revelouPensamentoHistoria = !revelouPensamentoHistoria }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    if (revelouPensamentoHistoria) "💭 Ocultar Pensamento" else "💭 Revelar Pensamento",
                                                    color = if (revelouPensamentoHistoria) Color(0xFFA29BFE) else TextoSuave,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                    if (revelouPensamentoHistoria) {
                                        Surface(
                                            color = Color(0xFF16152B),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.40f)),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = gerarPensamentoSecreto(cenarioCompleto.nome, msg.narrativa, indice + 1),
                                                color = Color(0xFFD6D0F5),
                                                fontSize = 12.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                lineHeight = 17.sp,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    RodapeMensagemLiven(
                                        rotuloEsquerda = msg.emNarrador?.let { horaDaMensagem(it) } ?: "Cap. ${indice + 1}",
                                        versoes = msg.versoes,
                                        versaoAtiva = msg.versaoAtiva,
                                        ehUltimo = ehUltimo && !vm.ocupado,
                                        aoTrocarVersao = { nova -> vm.trocarVersaoMensagem(indice, nova) },
                                        aoRegenerar = { vm.regenerarUltimaResposta() },
                                        aoEditar = { indiceEdicao = indice },
                                        aoCopiar = { areaDeCopia.setText(AnnotatedString(msg.narrativa)) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (!jogo.cena.imagemUrl.isNullOrBlank()) {
                    item(key = "arte_cena_atual_historia") {
                        val base = remember { ApiClient.baseUrlAtual() }
                        val fullUrl = remember(jogo.cena.imagemUrl, base) {
                            val u = jogo.cena.imagemUrl
                            if (base != null && u != null) "${base}${u.removePrefix("/")}" else u
                        }
                        if (fullUrl != null) {
                            val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFF10121C),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.5.dp, Ouro.copy(alpha = 0.55f)),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(fullUrl)
                                                .apply { if (cabecalho != null) addHeader("Authorization", cabecalho) }
                                                .crossfade(true)
                                                .allowHardware(true)
                                                .build(),
                                            contentDescription = "Arte da Cena Atual",
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.60f),
                                            shape = Cantos.pilula,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✨ Ilustração da Cena Gerada por IA", style = MaterialTheme.typography.labelSmall, color = Ouro)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (vm.ocupado) {
                    vm.acaoPendente?.let { pendente ->
                        if (pendente != "[CONTINUAR_HISTORIA]") {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Surface(
                                        color = Ouro.copy(alpha = 0.10f),
                                        shape = RoundedCornerShape(
                                            topStart = 18.dp,
                                            topEnd = 18.dp,
                                            bottomStart = 18.dp,
                                            bottomEnd = 4.dp
                                        ),
                                        modifier = Modifier.fillMaxWidth(0.86f)
                                    ) {
                                        Text(
                                            text = pendente,
                                            color = corNormal.copy(alpha = 0.65f),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = (vm.tamanhoLetraNarrador - 0.5f).sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { BlocoDigitando() }
                }

                if (!jogo.cena.imagemUrl.isNullOrBlank()) {
                    item(key = "arte_cena_atual_historia") {
                        val base = remember { ApiClient.baseUrlAtual() }
                        val fullUrl = remember(jogo.cena.imagemUrl, base) {
                            val u = jogo.cena.imagemUrl
                            if (base != null && u != null) "${base}${u.removePrefix("/")}" else u
                        }
                        if (fullUrl != null) {
                            val cabecalho = remember { ApiClient.cabecalhoAutorizacao() }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFF10121C),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.5.dp, Ouro.copy(alpha = 0.55f)),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(fullUrl)
                                                .apply { if (cabecalho != null) addHeader("Authorization", cabecalho) }
                                                .crossfade(true)
                                                .allowHardware(true)
                                                .build(),
                                            contentDescription = "Cena Ilustrada da História",
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.TopCenter,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.70f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✨ Ilustração da Cena", fontSize = 11.sp, color = Ouro, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item(key = "fim_chat_espaco") {
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ---- Barra inferior de conversa / coautoria (sem opções obrigatórias na tela) ----
            Surface(
                color = Superficie,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChipTag("⚡ ${jogo.jogadasRestantesHoje}", destaque = true)

                        Surface(
                            color = SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable(enabled = !vm.ocupado) {
                                acaoLivre = if (acaoLivre.endsWith("*")) "$acaoLivre " else "$acaoLivre*"
                            }
                        ) {
                            Text(
                                "*ação*",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ouro,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f)),
                            modifier = Modifier.cliqueFuturista3D {
                                if (!vm.ocupado && !vm.gerandoAutoResposta) {
                                    vm.gerarSugestoesAutoResposta { primeira ->
                                        if (acaoLivre.isBlank()) acaoLivre = primeira
                                    }
                                }
                            }
                        ) {
                            Text(
                                if (vm.gerandoAutoResposta) "⏳ Inspirando..." else "✨ Auto-resposta",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ouro,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = if (jogo.cena.imagemUrl != null) Ouro.copy(alpha = 0.20f) else SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f)),
                            modifier = Modifier.cliqueFuturista3D {
                                if (!vm.ocupado && !vm.ilustrandoCena) {
                                    val ehPago = (jogo.modelo?.ehPago == true) || (vm.conta?.premium == true)
                                    if (ehPago) {
                                        vm.ilustrarCenaAtual(
                                            comAnuncio = false,
                                            onSucesso = { Toast.makeText(contexto, "Cena ilustrada com sucesso!", Toast.LENGTH_SHORT).show() },
                                            onError = { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
                                        )
                                    } else {
                                        mostrarDialogoIlustracaoGratis = true
                                    }
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (vm.ilustrandoCena) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Ouro)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Pintando cena...", style = MaterialTheme.typography.labelSmall, color = Ouro)
                                } else {
                                    Text(
                                        if (jogo.cena.imagemUrl != null) "🖼️ Arte da Cena •" else "✨ 🖼️ Ilustrar Cena",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Ouro
                                    )
                                }
                            }
                        }

                        Surface(
                            color = SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable(enabled = !vm.ocupado && terminou) {
                                haptico.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.continuarHistoria()
                            }
                        ) {
                            Text(
                                "⏩ Continuar história",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ouro,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        if (jogo.cena.opcoes.isNotEmpty()) {
                            Surface(
                                color = if (mostrarSugestoes) Ouro.copy(alpha = 0.22f) else SuperficieAlta,
                                shape = Cantos.pilula,
                                border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable { mostrarSugestoes = !mostrarSugestoes }
                            ) {
                                Text(
                                    if (mostrarSugestoes) "💡 Ocultar ideias" else "💡 Ideias",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Ouro,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Text(
                            "✎",
                            color = TextoSuave,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clickable { dialogo = DialogoAberto.Feedback }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Gaveta de Auto-Respostas LivennIA
                    val sugestoesAtuais = remember(vm.sugestoesAutoResposta, jogo.cena.opcoes) {
                        if (vm.sugestoesAutoResposta.isNotEmpty()) {
                            vm.sugestoesAutoResposta
                        } else if (jogo.cena.opcoes.isNotEmpty()) {
                            jogo.cena.opcoes
                        } else {
                            listOf(
                                "Perguntar mais sobre isso",
                                "Observar a reação com atenção",
                                "Dar um passo à frente com cautela"
                            )
                        }
                    }
                    FaixaSugestoesAutoResposta(
                        sugestoes = sugestoesAtuais,
                        ocupado = vm.ocupado,
                        gerandoIA = vm.gerandoAutoResposta,
                        aoPedirSugestoesIA = { vm.gerarSugestoesAutoResposta() },
                        aoEscolher = { sugestao ->
                            acaoLivre = sugestao
                            haptico.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        }
                    )

                    AnimatedVisibility(visible = mostrarSugestoes && terminou && jogo.cena.opcoes.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            jogo.cena.opcoes.forEach { sugestao ->
                                Surface(
                                    color = SuperficieAlta,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.45f)),
                                    modifier = Modifier.clickable(enabled = !vm.ocupado) {
                                        acaoLivre = sugestao
                                        mostrarSugestoes = false
                                    }
                                ) {
                                    Text(
                                        text = sugestao,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = acaoLivre,
                            onValueChange = { acaoLivre = it.take(300) },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text("Enviar uma mensagem...", color = Color.White.copy(alpha = 0.45f))
                            },
                            maxLines = 4,
                            enabled = !vm.ocupado,
                            shape = RoundedCornerShape(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                val t = acaoLivre.trim()
                                if (t.isNotEmpty() && !vm.ocupado) {
                                    acaoLivre = ""
                                    mostrarSugestoes = false
                                    vm.jogarAcao(t)
                                }
                            },
                            enabled = !vm.ocupado && acaoLivre.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = Color(0xFF7C4DFF)
                            )
                        }
                    }

                    // Pílulas Rápidas no Rodapé (Persona Ativa, Modelo IA, Menu) - Imagens 1 e 5
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pílula 1: Minha Persona (Ao tocar abre o modal para editar)
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { mostrarPersonaModal = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val personaAtivaRodape = vm.personaAtiva
                                if (personaAtivaRodape != null && personaAtivaRodape.temRetrato) {
                                    RetratoPersona(
                                        temRetrato = true,
                                        emojiOuPadrao = personaAtivaRodape.genero.ifBlank { "👤" },
                                        tamanho = 18,
                                        versao = personaAtivaRodape.versaoRetrato,
                                        personaId = personaAtivaRodape.id,
                                        recorte = CircleShape
                                    )
                                } else if (vm.personaTemRetrato) {
                                    RetratoPersona(
                                        temRetrato = true,
                                        emojiOuPadrao = vm.personaAvatar.ifBlank { "👤" },
                                        tamanho = 18,
                                        versao = vm.personaRetratoVersao,
                                        recorte = CircleShape
                                    )
                                } else {
                                    Text(vm.personaAvatar.ifBlank { "👤" }, fontSize = 12.sp)
                                }
                                Spacer(Modifier.width(5.dp))
                                val nomeRodape = personaAtivaRodape?.nome?.ifBlank { null }
                                    ?: vm.personaNome.ifBlank { null }
                                    ?: vm.apelido.ifBlank { "Minha Persona" }
                                Text(
                                    text = nomeRodape,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Pílula 2: Motor de IA / Cérebro Ativo
                        val emojiAtual = jogo.modelo?.emoji?.ifBlank { "🧠" }
                            ?: vm.cerebros.firstOrNull { it.id == vm.cerebroSelecionado }?.emoji?.ifBlank { "🧠" }
                            ?: "🧠"
                        val nomeAtual = jogo.modelo?.nome
                            ?: vm.cerebros.firstOrNull { it.id == vm.cerebroSelecionado }?.nome
                            ?: "Livenn Flash"
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.clickable { dialogo = DialogoAberto.ModeloIA }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(emojiAtual, fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = nomeAtual,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Pílula 3: Menu de Opções (Savepoint, Diário, Ações)
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { dialogo = DialogoAberto.Savepoints }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("≡", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    GerenciadorDialogosJogo(
        vm = vm,
        jogo = jogo,
        dialogo = dialogo,
        indiceEdicao = indiceEdicao,
        onFecharDialogo = { dialogo = null },
        onFecharEdicao = { indiceEdicao = null },
        aoInserirNoChat = { texto -> acaoLivre = if (acaoLivre.isBlank()) texto else "$acaoLivre $texto" }
    )

    if (modalPresentesAberto) {
        val nomeP = jogo.cenario?.nome ?: cenarioCompleto.nome
        val saldo = vm.conta?.livens ?: jogo.livens ?: 0
        ModalPresentearPersonagem(
            nomePersonagem = nomeP,
            saldoLivens = saldo,
            onEnviarPresente = { presente ->
                modalPresentesAberto = false
                vm.jogarAcao(presente.acaoNarrativa)
                Toast.makeText(contexto, "Você presenteou $nomeP com ${presente.icone} ${presente.nome}!", Toast.LENGTH_SHORT).show()
            },
            onFechar = { modalPresentesAberto = false }
        )
    }

    if (modalGaleriaAberto) {
        val nomeP = jogo.cenario?.nome ?: cenarioCompleto.nome
        val vinculoAtual = calcularVinculo(jogo.turno).nivel
        ModalGaleriaMomentos(
            nomePersonagem = nomeP,
            cenarioId = jogo.cenario?.id,
            nivelVinculoAtual = vinculoAtual,
            onFechar = { modalGaleriaAberto = false }
        )
    }

    if (mostrarDialogoIlustracaoGratis) {
        val nomeModelo = jogo.modelo?.nome ?: "Liven Flash (Grátis)"
        DialogoIlustracaoCenaOpcoes(
            nomeModeloAtual = nomeModelo,
            onAssistirAnuncio = {
                mostrarDialogoIlustracaoGratis = false
                simulandoAnuncioIlustracao = true
            },
            onMelhorarPlano = {
                mostrarDialogoIlustracaoGratis = false
                vm.navegarPara(Tela.Assinatura("Ultra"))
            },
            onFechar = { mostrarDialogoIlustracaoGratis = false }
        )
    }

    if (simulandoAnuncioIlustracao) {
        DialogoSimuladorAnuncio(
            onConcluido = {
                simulandoAnuncioIlustracao = false
                vm.ilustrarCenaAtual(
                    comAnuncio = true,
                    onSucesso = { Toast.makeText(contexto, "Cena ilustrada com sucesso!", Toast.LENGTH_SHORT).show() },
                    onError = { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
                )
            },
            onFechar = { simulandoAnuncioIlustracao = false }
        )
    }

    if (mostrarPerfilHistoria) {
        DetalhesPersonagemModal(
            cenario = cenarioCompleto,
            versaoCapa = vm.capaVersao,
            apelidoUsuario = vm.apelido,
            vm = vm,
            onFechar = { mostrarPerfilHistoria = false },
            onIniciarChat = { mostrarPerfilHistoria = false }
        )
    }

    if (mostrarPersonaModal) {
        ModalMinhaPersona(
            vm = vm,
            jogoAtualId = jogo.jogoId,
            onFechar = { mostrarPersonaModal = false }
        )
    }

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun BarraFerramentasLiven(
    vm: AppViewModel,
    jogo: JogoResponse,
    onAbrirDialogo: (DialogoAberto) -> Unit,
    onAtivarModoCinema: (() -> Unit)? = null,
    onExportarPdf: (() -> Unit)? = null,
    onAbrirPresentes: (() -> Unit)? = null,
    onAbrirGaleria: (() -> Unit)? = null
) {
    val saldoLivens = vm.conta?.livens ?: jogo.livens ?: 0
    val podeResgatarHoje = vm.conta?.resgateDiarioDisponivel == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selo de Vínculo & Afinidade
        val vinculo = calcularVinculo(jogo.turno)
        Surface(
            color = Ouro.copy(alpha = 0.18f),
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.50f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Vinculo) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(vinculo.icone, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Nvl ${vinculo.nivel} • ${vinculo.titulo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ouro,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Pílula de Livens (com resgate diário ao tocar se disponível)
        Surface(
            color = Ouro.copy(alpha = 0.16f),
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.45f)),
            modifier = Modifier.clickable(enabled = podeResgatarHoje) {
                vm.resgatarLivensDiarios()
            }
        ) {
            Text(
                text = if (podeResgatarHoje) "✨ $saldoLivens (+30)" else "✨ $saldoLivens",
                style = MaterialTheme.typography.labelSmall,
                color = Ouro,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Memória de Longo Prazo
        Surface(
            color = if (jogo.memoria.isNotBlank()) Ouro.copy(alpha = 0.16f) else SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Memoria) }
        ) {
            Text(
                text = if (jogo.memoria.isNotBlank()) "🧠 Memória •" else "🧠 Memória",
                style = MaterialTheme.typography.labelSmall,
                color = if (jogo.memoria.isNotBlank()) Ouro else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Diário Secreto do Personagem
        Surface(
            color = SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Diarios) }
        ) {
            Text(
                text = "📔 Diário (${jogo.diarios.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Cartas de Memória
        Surface(
            color = SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Cartas) }
        ) {
            Text(
                text = "💌 Cartas (${jogo.cartas.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Pontos de Salvamento (Savepoints)
        Surface(
            color = SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.clickable { onAbrirDialogo(DialogoAberto.Savepoints) }
        ) {
            Text(
                text = "💾 Saves (${jogo.savepoints.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Som Ambiente Sintetizado (Imersão Offline)
        var somAtivo by remember { mutableStateOf(SintetizadorAmbienteLivennIA.ativo) }
        Surface(
            color = if (somAtivo) Ouro.copy(alpha = 0.22f) else SuperficieAlta,
            shape = Cantos.pilula,
            border = BorderStroke(1.dp, if (somAtivo) Ouro else Ouro.copy(alpha = 0.30f)),
            modifier = Modifier.cliqueFuturista3D {
                val gen = jogo.cenario?.tags?.firstOrNull() ?: jogo.genero
                SintetizadorAmbienteLivennIA.alternar(gen) { somAtivo = it }
            }
        ) {
            Text(
                text = if (somAtivo) "🎵 Som Ambiente • Ligado" else "🎵 Som Ambiente",
                style = MaterialTheme.typography.labelSmall,
                color = if (somAtivo) Ouro else MaterialTheme.colorScheme.onBackground,
                fontWeight = if (somAtivo) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        if (onAtivarModoCinema != null) {
            Surface(
                color = Color(0xFF6C5CE7).copy(alpha = 0.25f),
                shape = Cantos.pilula,
                border = BorderStroke(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.65f)),
                modifier = Modifier.cliqueFuturista3D { onAtivarModoCinema() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎬 Modo Cinema", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA29BFE), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (onExportarPdf != null) {
            Surface(
                color = SuperficieAlta,
                shape = Cantos.pilula,
                border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                modifier = Modifier.cliqueFuturista3D { onExportarPdf() }
            ) {
                Text(
                    text = "📖 Livro (PDF)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        if (onAbrirPresentes != null) {
            Surface(
                color = Color(0xFFFF4081).copy(alpha = 0.22f),
                shape = Cantos.pilula,
                border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.60f)),
                modifier = Modifier.cliqueFuturista3D { onAbrirPresentes() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎁 Presentear", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF80AB), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (onAbrirGaleria != null) {
            Surface(
                color = Color(0xFF00E5FF).copy(alpha = 0.20f),
                shape = Cantos.pilula,
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.50f)),
                modifier = Modifier.cliqueFuturista3D { onAbrirGaleria() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📸 Galeria CG", style = MaterialTheme.typography.labelSmall, color = Color(0xFF80D8FF), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RodapeMensagemLiven(
    rotuloEsquerda: String,
    versoes: List<String>,
    versaoAtiva: Int,
    ehUltimo: Boolean,
    aoTrocarVersao: (Int) -> Unit,
    aoRegenerar: () -> Unit,
    aoEditar: () -> Unit,
    aoCopiar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (rotuloEsquerda.isNotBlank()) {
                Text(
                    text = rotuloEsquerda,
                    color = TextoSuave.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (versoes.size > 1) {
                Surface(
                    color = Ouro.copy(alpha = 0.14f),
                    shape = Cantos.pilula,
                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "‹",
                            color = if (versaoAtiva > 0) Ouro else TextoSuave.copy(alpha = 0.35f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(enabled = versaoAtiva > 0) { aoTrocarVersao(versaoAtiva - 1) }
                                .padding(horizontal = 4.dp)
                        )
                        Text(
                            text = "${versaoAtiva + 1}/${versoes.size}",
                            color = Ouro,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "›",
                            color = if (versaoAtiva < versoes.size - 1) Ouro else TextoSuave.copy(alpha = 0.35f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(enabled = versaoAtiva < versoes.size - 1) { aoTrocarVersao(versaoAtiva + 1) }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (ehUltimo) {
                Text(
                    text = "🔄 Regenerar",
                    color = Ouro,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clickable { aoRegenerar() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Text(
                text = "✎ Editar",
                color = TextoSuave.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clickable { aoEditar() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Text(
                text = "⧉ Copiar",
                color = TextoSuave.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clickable { aoCopiar() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FaixaSugestoesAutoResposta(
    sugestoes: List<String>,
    ocupado: Boolean,
    gerandoIA: Boolean,
    aoPedirSugestoesIA: () -> Unit,
    aoEscolher: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão para pedir sugestões com IA se quiser mais opções
        Surface(
            color = Ouro.copy(alpha = 0.20f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.50f)),
            modifier = Modifier.clickable(enabled = !ocupado && !gerandoIA) {
                aoPedirSugestoesIA()
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (gerandoIA) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = Ouro
                    )
                } else {
                    Text("✨", fontSize = 12.sp)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (gerandoIA) "Pensando..." else "Ideias IA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ouro,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        sugestoes.forEach { sugestao ->
            Surface(
                color = SuperficieAlta,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.clickable(enabled = !ocupado) {
                    aoEscolher(sugestao)
                }
            ) {
                Text(
                    text = sugestao,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.90f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ItemCardModeloIA(
    c: CerebroDto,
    selecionado: Boolean,
    aoSelecionar: () -> Unit
) {
    Surface(
        color = if (selecionado) Ouro.copy(alpha = 0.18f) else SuperficieAlta,
        shape = Cantos.cartao,
        border = BorderStroke(
            1.dp,
            if (selecionado) Ouro else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = c.liberado) { aoSelecionar() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(c.emoji.ifBlank { "🤖" }, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = c.nome,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
                        color = if (selecionado) Ouro else MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = c.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                color = when {
                    c.custoLivens == 0 -> Color(0xFF2E7D32).copy(alpha = 0.25f)
                    c.premium -> Color(0xFFFFB300).copy(alpha = 0.20f)
                    else -> Color(0xFF1976D2).copy(alpha = 0.25f)
                },
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = when {
                        c.custoLivens == 0 -> "Grátis"
                        c.premium -> "VIP"
                        else -> "⚡ ${c.custoLivens}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        c.custoLivens == 0 -> Color(0xFF81C784)
                        c.premium -> Ouro
                        else -> Color(0xFF90CAF9)
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun GerenciadorDialogosJogo(
    vm: AppViewModel,
    jogo: JogoResponse,
    dialogo: DialogoAberto?,
    indiceEdicao: Int?,
    onFecharDialogo: () -> Unit,
    onFecharEdicao: () -> Unit,
    aoInserirNoChat: (String) -> Unit = {}
) {
    if (indiceEdicao != null && indiceEdicao in vm.chat.indices) {
        val msgAlvo = vm.chat[indiceEdicao]
        var textoAcao by remember(indiceEdicao) { mutableStateOf(msgAlvo.situacao ?: msgAlvo.acao) }
        var textoNarrativa by remember(indiceEdicao) { mutableStateOf(msgAlvo.narrativa) }

        AlertDialog(
            onDismissRequest = onFecharEdicao,
            title = { Text("✎ Corrigir Fala (Fix Reply)") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (indiceEdicao > 0) {
                        OutlinedTextField(
                            value = textoAcao,
                            onValueChange = { textoAcao = it.take(500) },
                            label = { Text("Sua fala / ação") },
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = textoNarrativa,
                        onValueChange = { textoNarrativa = it.take(4000) },
                        label = { Text("Resposta do narrador / personagem") },
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.editarMensagem(
                            indice = indiceEdicao,
                            narrativa = textoNarrativa,
                            acao = if (indiceEdicao > 0) textoAcao else null
                        )
                        onFecharEdicao()
                    },
                    enabled = textoNarrativa.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = onFecharEdicao) { Text("Cancelar") }
            }
        )
    }

    when (dialogo) {
        DialogoAberto.Feedback -> DialogoFeedback(
            onEnviar = { nota, comentario ->
                onFecharDialogo()
                vm.enviarFeedback(nota, comentario)
            },
            onFechar = onFecharDialogo
        )
        DialogoAberto.Denuncia -> DialogoDenuncia(
            onEnviar = { motivo, detalhe ->
                onFecharDialogo()
                vm.denunciar(motivo, detalhe)
            },
            onFechar = onFecharDialogo
        )
        DialogoAberto.Memoria -> {
            var textoMemoria by remember(jogo.memoria) { mutableStateOf(jogo.memoria) }
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = { Text("🧠 Memória de Longo Prazo") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Estes fatos são sempre lembrados pela IA em todos os turnos desta história.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        OutlinedTextField(
                            value = textoMemoria,
                            onValueChange = { textoMemoria = it.take(1500) },
                            placeholder = { Text("Ex.: Somos aliados de infância; prometemos encontrar a relíquia...") },
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = { vm.resumirMemoriaComIA() },
                            enabled = !vm.ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (vm.ocupado) "⏳ Resumindo..." else "✨ Resumir história até aqui com IA")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.salvarMemoria(textoMemoria)
                            onFecharDialogo()
                        }
                    ) { Text("Salvar memória") }
                },
                dismissButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.Diarios -> {
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = { Text("📔 Diário Secreto") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Pensamentos íntimos do personagem sobre os acontecimentos recentes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        Button(
                            onClick = { vm.gerarPaginaDiario() },
                            enabled = !vm.ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (vm.ocupado) "⏳ Escrevendo no diário..." else "✨ Revelar nova página do diário")
                        }
                        if (jogo.diarios.isEmpty()) {
                            Text(
                                "Nenhuma página revelada ainda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSuave
                            )
                        } else {
                            jogo.diarios.asReversed().forEach { pagina ->
                                val dataFormatada = pagina.criadoEm?.let { horaDaMensagem(it) } ?: "Cap. ${pagina.turno}"
                                Surface(
                                    color = SuperficieAlta,
                                    shape = Cantos.cartao,
                                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${pagina.titulo} • Cap. ${pagina.turno}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Ouro
                                        )
                                        Text(
                                            text = "Humor: ${pagina.humor} • $dataFormatada",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextoSuave
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = pagina.conteudo,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.Cartas -> {
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = { Text("💌 Cartas de Memória") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Cartas especiais escritas pelo personagem para você guardar de recordação.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        Button(
                            onClick = { vm.gerarCartaMemoria() },
                            enabled = !vm.ocupado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (vm.ocupado) "⏳ Escrevendo carta..." else "💌 Pedir carta de memória")
                        }
                        if (jogo.cartas.isEmpty()) {
                            Text(
                                "Nenhuma carta recebida ainda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSuave
                            )
                        } else {
                            jogo.cartas.asReversed().forEach { carta ->
                                val dataCarta = carta.criadaEm?.let { horaDaMensagem(it) } ?: "Cap. ${carta.turno}"
                                Surface(
                                    color = Color(0xFF141524),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFFF80AB), Color(0xFF6C5CE7), Color(0xFF00E5FF), Ouro))),
                                    shadowElevation = 8.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("💌", fontSize = 16.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = carta.titulo,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Ouro
                                                )
                                            }
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                shape = Cantos.pilula
                                            ) {
                                                Text("★★★★★ RARA", color = Color(0xFFFFD54F), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Escrita por ${carta.remetente} • $dataCarta",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextoSuave
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = carta.conteudo,
                                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                                            color = Color.White.copy(alpha = 0.92f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.Savepoints -> {
            var tituloSave by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = { Text("💾 Pontos de Salvamento") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Salve o momento atual para explorar outros rumos e voltar quando quiser.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = tituloSave,
                                onValueChange = { tituloSave = it.take(60) },
                                placeholder = { Text("Nome do save (opcional)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    vm.criarSavepoint(tituloSave)
                                    tituloSave = ""
                                }
                            ) {
                                Text("Salvar")
                            }
                        }
                        if (jogo.savepoints.isEmpty()) {
                            Text(
                                "Nenhum savepoint criado nesta aventura.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSuave
                            )
                        } else {
                            jogo.savepoints.asReversed().forEach { sp ->
                                val dataSave = sp.criadoEm?.let { horaDaMensagem(it) } ?: sp.local
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(28.dp).padding(top = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Ouro, CircleShape)
                                                .border(2.dp, Color(0xFF161824), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(55.dp)
                                                .background(Ouro.copy(alpha = 0.35f))
                                        )
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF161824),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, Ouro.copy(alpha = 0.35f)),
                                        modifier = Modifier.weight(1f).padding(bottom = 10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = sp.nome.ifBlank { "Momento da História" },
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Ouro
                                                )
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.45f),
                                                    shape = Cantos.pilula
                                                ) {
                                                    Text("Turno ${sp.turno}", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                            Text(
                                                text = dataSave,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextoSuave
                                            )
                                            if (sp.resumo.isNotBlank()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = sp.resumo,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        vm.carregarSavepoint(sp.id)
                                                        onFecharDialogo()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Ouro),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text("🌿 Voltar a este Rumo", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                                TextButton(
                                                    onClick = { vm.apagarSavepoint(sp.id) },
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text("Apagar", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.Vinculo -> {
            val vinculo = calcularVinculo(jogo.turno)
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(vinculo.icone, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Vínculo & Afinidade")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = Ouro.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.40f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "NÍVEL ${vinculo.nivel} • ${vinculo.titulo.uppercase()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Ouro
                                )
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { vinculo.progresso },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = Ouro,
                                    trackColor = SuperficieAlta
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${(vinculo.progresso * 100).toInt()}% até a próxima evolução",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextoSuave
                                )
                            }
                        }

                        val narrativaUltima = vm.chat.lastOrNull()?.narrativa ?: jogo.cena.narrativa
                        val humor = remember(narrativaUltima, jogo.turno) { extrairHumorPersonagem(narrativaUltima, jogo.turno) }
                        Surface(
                            color = Color(0xFF1B1D2C),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF80AB).copy(alpha = 0.40f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(humor.first, fontSize = 28.sp)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Humor Atual: ${humor.second}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFFFF80AB),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "O personagem reage com emoções reais às suas palavras e atitudes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoSuave
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Como ${jogo.personagem.nome} te enxerga:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = vinculo.descricao,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSuave
                            )
                        }

                        Surface(
                            color = SuperficieAlta,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💡", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Dica: Fazer perguntas sobre o passado do personagem ou demonstrar empatia acelera o vínculo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSuave
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Fechar") }
                }
            )
        }
        DialogoAberto.ModeloIA -> {
            var cerebroIdSelecionado by remember(jogo.modelo?.id, vm.cerebroSelecionado) {
                mutableStateOf(jogo.modelo?.id ?: vm.cerebroSelecionado ?: vm.cerebros.firstOrNull()?.id ?: "")
            }
            var tempSelecionada by remember(jogo.temperatura) {
                mutableFloatStateOf(jogo.temperatura)
            }
            AlertDialog(
                onDismissRequest = onFecharDialogo,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Modelo de IA")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Escolha o modelo que dita o tom, profundidade e criatividade da narrativa:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )

                        val gratis = vm.cerebros.filter { it.custoLivens == 0 && !it.premium }
                        val energia = vm.cerebros.filter { it.custoLivens > 0 && !it.premium }
                        val premium = vm.cerebros.filter { it.premium }

                        if (gratis.isNotEmpty()) {
                            Text(
                                text = "🎁 GRÁTIS COM ANÚNCIO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Ouro
                            )
                            gratis.forEach { c ->
                                ItemCardModeloIA(
                                    c = c,
                                    selecionado = c.id == cerebroIdSelecionado,
                                    aoSelecionar = {
                                        cerebroIdSelecionado = c.id
                                        tempSelecionada = c.temperaturaPadrao
                                    }
                                )
                            }
                        }

                        if (energia.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "⚡ USA ENERGIA (LIVENS)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64B5F6)
                            )
                            energia.forEach { c ->
                                ItemCardModeloIA(
                                    c = c,
                                    selecionado = c.id == cerebroIdSelecionado,
                                    aoSelecionar = {
                                        cerebroIdSelecionado = c.id
                                        tempSelecionada = c.temperaturaPadrao
                                    }
                                )
                            }
                        }

                        if (premium.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "👑 EXCLUSIVO PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                            premium.forEach { c ->
                                ItemCardModeloIA(
                                    c = c,
                                    selecionado = c.id == cerebroIdSelecionado,
                                    aoSelecionar = {
                                        cerebroIdSelecionado = c.id
                                        tempSelecionada = c.temperaturaPadrao
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.configurarModeloDoJogo(
                                modeloId = cerebroIdSelecionado.ifBlank { null },
                                temperatura = tempSelecionada
                            )
                            onFecharDialogo()
                        }
                    ) { Text("Confirmar Modelo") }
                },
                dismissButton = {
                    TextButton(onClick = onFecharDialogo) { Text("Cancelar") }
                }
            )
        }
        DialogoAberto.Party -> DialogoParty(
            vm = vm,
            jogo = jogo,
            ehModoHistoria = jogo.modo == "historia",
            onFechar = onFecharDialogo,
            aoInserirNoChat = aoInserirNoChat
        )
        DialogoAberto.Dados -> {
            if (jogo.modo == "rpg") {
                DialogoRolagemDados(
                    vm = vm,
                    onFechar = onFecharDialogo,
                    aoInserirNoChat = aoInserirNoChat
                )
            } else {
                onFecharDialogo()
            }
        }
        null -> Unit
    }
}

// ---- Diálogo de Gerenciamento de Grupo / Party / Elenco ----
@Composable
private fun DialogoParty(
    vm: AppViewModel,
    jogo: JogoResponse,
    ehModoHistoria: Boolean,
    onFechar: () -> Unit,
    aoInserirNoChat: (String) -> Unit
) {
    var abaAdicionar by remember { mutableIntStateOf(0) }
    var nomeCustom by remember { mutableStateOf("") }
    var papelCustom by remember { mutableStateOf("") }
    var descCustom by remember { mutableStateOf("") }
    val contexto = LocalContext.current

    val arquetipos = remember(ehModoHistoria) {
        if (ehModoHistoria) {
            listOf(
                Triple("🌹 Evelyn", "Rival da Corte", "Sagaz, provocadora, refinada e estrategista."),
                Triple("💼 Liam", "Parceiro Investigativo", "Observador, leal, pragmático e protetor."),
                Triple("🌸 Sophie", "Amiga de Infância", "Carinhosa, compreensiva e sempre ao seu lado."),
                Triple("👑 Julian", "Nobre Conquistador", "Sedutor, influente, carismático e aristocrata."),
                Triple("🕵️ Victor", "Antagonista Misterioso", "Intrigante, calculista e com segredos perigosos."),
                Triple("☕ Clara", "Confidente Próxima", "Doce, acolhedora e excelente ouvinte.")
            )
        } else {
            listOf(
                Triple("🛡️ Kaelen", "Paladino Protetor", "Leal, disciplinado e pronto para proteger o grupo."),
                Triple("🔮 Lyra", "Maga Arcana", "Erudita, misteriosa e especialista em feitiços ancestrais."),
                Triple("🗡️ Vael", "Ladino Sorrateiro", "Ágil, irônico e mestre em desarmar armadilhas."),
                Triple("✨ Elena", "Curandeira da Luz", "Bondosa, empática e devota das artes de cura."),
                Triple("🏹 Sylvan", "Arqueiro Élfico", "Observador silencioso de pontaria letal."),
                Triple("⚔️ Thorne", "Guerreiro Bárbaro", "Feroz, destemido e implacável em batalha.")
            )
        }
    }

    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👥", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        if (ehModoHistoria) "Elenco da História" else "Party & Companheiros de RPG",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        if (ehModoHistoria) "${vm.partyAtiva.size} integrante(s) no elenco" else "${vm.partyAtiva.size} companheiro(s) na party",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSuave
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    if (ehModoHistoria) {
                        "Adicione personagens ao elenco desta história (amigos, rivais, confidentes ou pretendentes). Cada um terá seu balão de fala individual na conversa, e você pode falar diretamente com eles usando @Nome!"
                    } else {
                        "Adicione companheiros de jornada à sua mesa de RPG. Na conversa, cada membro terá seu próprio balão com avatar, e você pode chamá-los para combates e desafios com @Nome!"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )

                // Lista de membros ativos
                if (vm.partyAtiva.isNotEmpty()) {
                    Text(
                        if (ehModoHistoria) "🎭 INTEGRANTES DO ELENCO" else "⚔️ INTEGRANTES DA PARTY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (ehModoHistoria) Color(0xFFB388FF) else Ouro
                    )
                    vm.partyAtiva.forEach { membro ->
                        val cor = corDoPersonagem(membro.nome)
                        Surface(
                            color = SuperficieAlta,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, cor.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(cor.copy(alpha = 0.20f))
                                        .border(1.5.dp, cor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(membro.emoji.ifBlank { "👤" }, fontSize = 16.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        membro.nome,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = cor
                                    )
                                    Text(
                                        membro.papel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextoSuave
                                    )
                                    if (membro.descricao.isNotBlank()) {
                                        Text(
                                            membro.descricao,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = TextoSuave.copy(alpha = 0.75f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = cor.copy(alpha = 0.18f),
                                    shape = Cantos.pilula,
                                    border = BorderStroke(1.dp, cor.copy(alpha = 0.40f)),
                                    modifier = Modifier.clickable {
                                        aoInserirNoChat("@${membro.nome} ")
                                        Toast.makeText(contexto, "@${membro.nome} inserido no chat!", Toast.LENGTH_SHORT).show()
                                        onFechar()
                                    }
                                ) {
                                    Text(
                                        "@Falar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                IconButton(
                                    onClick = { vm.removerMembroParty(membro.nome) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remover",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        color = SuperficieAlta.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (ehModoHistoria) {
                                "Nenhum personagem adicionado ao elenco ainda. Convide uma de suas Personas ou escolha um arquétipo abaixo!"
                            } else {
                                "Nenhum companheiro na party ainda. Convide uma de suas Personas ou escolha um arquétipo abaixo!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    if (ehModoHistoria) "➕ ADICIONAR AO ELENCO" else "➕ ADICIONAR À PARTY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (ehModoHistoria) Color(0xFFB388FF) else Ouro
                )

                // Seletor de abas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Personas", "Arquétipos", "Criar Novo").forEachIndexed { index, aba ->
                        val selecionada = abaAdicionar == index
                        Surface(
                            color = if (selecionada) (if (ehModoHistoria) Color(0xFF7C4DFF).copy(alpha = 0.35f) else Ouro.copy(alpha = 0.22f)) else SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, if (selecionada) (if (ehModoHistoria) Color(0xFFB388FF) else Ouro) else Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.weight(1f).clickable { abaAdicionar = index }
                        ) {
                            Text(
                                aba,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selecionada) (if (ehModoHistoria) Color(0xFFB388FF) else Ouro) else TextoSuave,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                when (abaAdicionar) {
                    0 -> { // Personas salvas
                        if (vm.personas.isEmpty()) {
                            Text("Você ainda não criou nenhuma Persona extra.", style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                vm.personas.forEach { p ->
                                    val jaNaParty = vm.partyAtiva.any { it.nome.equals(p.nome, ignoreCase = true) }
                                    Surface(
                                        color = SuperficieAlta,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(p.genero.ifBlank { "👤" }, fontSize = 20.sp)
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(p.nome, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text(p.descricao.ifBlank { "Persona salva" }, style = MaterialTheme.typography.labelSmall, color = TextoSuave, maxLines = 1)
                                            }
                                            Button(
                                                onClick = {
                                                    vm.adicionarMembroParty(
                                                        nome = p.nome,
                                                        papel = if (ehModoHistoria) "Personagem" else "Companheiro(a)",
                                                        emoji = p.genero.ifBlank { "👤" },
                                                        avatarUrl = p.avatarUrl,
                                                        descricao = p.descricao
                                                    )
                                                },
                                                enabled = !jaNaParty,
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                            ) {
                                                Text(if (jaNaParty) "No Elenco" else "+ Convidar", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Arquétipos prontos
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            arquetipos.forEach { (nome, papel, desc) ->
                                val jaNaParty = vm.partyAtiva.any { it.nome.contains(nome.substringAfter(" ")) }
                                Surface(
                                    color = SuperficieAlta,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(nome.take(2), fontSize = 20.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(nome.substringAfter(" "), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                            Text(papel, style = MaterialTheme.typography.labelSmall, color = if (ehModoHistoria) Color(0xFFB388FF) else Ouro)
                                            Text(desc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextoSuave, maxLines = 1)
                                        }
                                        Button(
                                            onClick = {
                                                vm.adicionarMembroParty(
                                                    nome = nome.substringAfter(" "),
                                                    papel = papel,
                                                    emoji = nome.take(2).trim(),
                                                    descricao = desc
                                                )
                                            },
                                            enabled = !jaNaParty,
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Text(if (jaNaParty) "No Elenco" else "+ Adicionar", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Criar personalizado
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = nomeCustom,
                                onValueChange = { nomeCustom = it.take(40) },
                                label = { Text("Nome do Personagem") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = papelCustom,
                                onValueChange = { papelCustom = it.take(40) },
                                label = { Text(if (ehModoHistoria) "Papel (Ex: Rival, Confidente, Antagonista)" else "Classe / Papel (Ex: Mago, Ladino, Guerreiro)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = descCustom,
                                onValueChange = { descCustom = it.take(150) },
                                label = { Text("Breve Personalidade ou Descrição") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )
                            Button(
                                onClick = {
                                    if (nomeCustom.isNotBlank()) {
                                        vm.adicionarMembroParty(
                                            nome = nomeCustom.trim(),
                                            papel = papelCustom.ifBlank { if (ehModoHistoria) "Personagem" else "Companheiro" },
                                            emoji = "👤",
                                            descricao = descCustom.trim()
                                        )
                                        nomeCustom = ""
                                        papelCustom = ""
                                        descCustom = ""
                                    }
                                },
                                enabled = nomeCustom.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (ehModoHistoria) "Entrar no Elenco" else "Entrar na Party")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) { Text("Fechar") }
        }
    )
}

// ---- Diálogo de Rolagem de Dados de RPG (d20, d100, etc.) ----
@Composable
private fun DialogoRolagemDados(
    vm: AppViewModel,
    onFechar: () -> Unit,
    aoInserirNoChat: (String) -> Unit
) {
    var ladosSelecionados by remember { mutableIntStateOf(20) }
    var modificador by remember { mutableIntStateOf(0) }
    var motivo by remember { mutableStateOf("") }
    var resultadoDado by remember { mutableStateOf<DadoResponse?>(null) }
    var rolando by remember { mutableStateOf(false) }
    val haptico = LocalHapticFeedback.current

    val dadosDisponiveis = listOf(20, 100, 12, 10, 8, 6, 4)

    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎲", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text("Mesa de RPG • Rolar Dados", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Escolha o dado e modificadores. O resultado será interpretado pelo Mestre da Mesa na próxima rodada!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )

                // Seletor de Tipo de Dado
                Text("TIPO DE DADO", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Ouro)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dadosDisponiveis.forEach { d ->
                        val selecionado = ladosSelecionados == d
                        Surface(
                            color = if (selecionado) Ouro else SuperficieAlta,
                            shape = Cantos.pilula,
                            border = BorderStroke(1.dp, if (selecionado) Ouro else Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.weight(1f).clickable {
                                ladosSelecionados = d
                                resultadoDado = null
                            }
                        ) {
                            Text(
                                "d$d",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selecionado) Color.Black else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 7.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Modificador de Atributo (+/-)
                Text("MODIFICADOR DE ATRIBUTO", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Ouro)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf(-5, -2, -1, 0, 1, 2, 3, 5).forEach { m ->
                        val sel = modificador == m
                        Surface(
                            color = if (sel) Ouro.copy(alpha = 0.25f) else SuperficieAlta,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (sel) Ouro else Color.White.copy(alpha = 0.10f)),
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .clickable { modificador = m }
                        ) {
                            Text(
                                if (m > 0) "+$m" else "$m",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (sel) Ouro else TextoSuave,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Motivo da rolagem (ex: Ataque, Furtividade)
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it.take(50) },
                    label = { Text("Ação / Teste (Ex: Ataque com Espada, Persuasão)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Animação 3D do Dado Girando
                var anguloDado by remember { mutableFloatStateOf(0f) }
                val animAngulo by animateFloatAsState(
                    targetValue = if (rolando) anguloDado + 720f else anguloDado,
                    animationSpec = tween(durationMillis = 650),
                    label = "giroDado"
                )

                // Botão de Rolar com efeito 3D
                Button(
                    onClick = {
                        rolando = true
                        anguloDado += 720f
                        haptico.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.rolarDado(
                            lados = ladosSelecionados,
                            modificador = modificador,
                            motivo = motivo.ifBlank { null }
                        ) { res ->
                            resultadoDado = res
                            rolando = false
                            haptico.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .interativo3D(escalaPressionado = 0.94f) {},
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ouro)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "🎲",
                            fontSize = 22.sp,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = animAngulo
                                rotationX = animAngulo / 2f
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (rolando) "ROLANDO DADO..." else "ROLAR d$ladosSelecionados (Mesa 3D)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1B1400)
                        )
                    }
                }

                // Exibição do Resultado
                resultadoDado?.let { res ->
                    val corStatus = when {
                        res.critico -> Color(0xFFFFD54F) // Ouro radiante
                        res.desastre -> MaterialTheme.colorScheme.error // Vermelho desastre
                        res.total >= 10 -> Color(0xFF81C784) // Verde sucesso
                        else -> Color(0xFFFFB74D) // Laranja falha
                    }

                    Surface(
                        color = Color(0xFF13141F),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, corStatus),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${res.total}",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = corStatus
                            )
                            Text(
                                text = res.textoFormatado,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = corStatus,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    aoInserirNoChat(res.textoFormatado)
                                    onFechar()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("✦ Inserir Rolagem no Chat")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) { Text("Fechar") }
        }
    )
}

// -------------------------------------------------------------
// SINTETIZADOR DE ÁUDIO AMBIENTE SUTIL EM TEMPO REAL (OFFLINE)
// -------------------------------------------------------------
object SintetizadorAmbienteLivennIA {
    private var job: Job? = null
    private var track: AudioTrack? = null
    var ativo = false
        private set

    fun alternar(genero: String = "Fantasia", onStatusMudou: (Boolean) -> Unit) {
        if (ativo) {
            parar()
            onStatusMudou(false)
        } else {
            iniciar(genero)
            onStatusMudou(true)
        }
    }

    fun parar() {
        ativo = false
        job?.cancel()
        job = null
        try {
            track?.pause()
            track?.flush()
            track?.release()
        } catch (_: Exception) {}
        track = null
    }

    fun iniciar(genero: String = "Fantasia") {
        parar()
        ativo = true

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            track?.play()
        } catch (_: Exception) {
            ativo = false
            return
        }

        val gLower = genero.lowercase()
        val freqs = when {
            "cyber" in gLower || "fic" in gLower -> doubleArrayOf(55.0, 110.0, 164.81)
            "horror" in gLower || "gotic" in gLower || "dark" in gLower -> doubleArrayOf(43.65, 87.31, 130.81)
            else -> doubleArrayOf(65.41, 130.81, 196.00)
        }

        job = CoroutineScope(Dispatchers.Default).launch {
            val chunk = ShortArray(bufferSize / 2)
            var t = 0L

            while (isActive && ativo) {
                for (i in chunk.indices) {
                    val tempoSec = t / sampleRate.toDouble()
                    val lfo = 0.65 + 0.35 * sin(tempoSec * 0.4 * Math.PI)
                    var valor = 0.0
                    for (f in freqs) {
                        valor += sin(2.0 * Math.PI * f * tempoSec)
                    }
                    val ruido = (Math.random() - 0.5) * 0.06
                    val amostra = ((valor / freqs.size * 0.22 + ruido) * lfo).coerceIn(-1.0, 1.0)
                    chunk[i] = (amostra * 32767).toInt().toShort()
                    t++
                }
                track?.write(chunk, 0, chunk.size)
            }
        }
    }
}

// -------------------------------------------------------------
// MODAL DE INVENTÁRIO & EQUIPAMENTOS 3D DO MODO RPG
// -------------------------------------------------------------
@Composable
fun ModalInventarioRPG(
    jogo: JogoResponse,
    onUsarItem: (String) -> Unit,
    onFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎒", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Inventário & Equipamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Itens carregados pelo seu herói", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SuperficieAlta,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        BarraDeVida(vida = jogo.personagem.vida, vidaMax = jogo.personagem.vidaMax)
                    }
                }

                if (jogo.personagem.inventario.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Text("Sua mochila está vazia no momento.", color = TextoSuave, fontSize = 14.sp)
                    }
                } else {
                    Text("Toque em um item para usá-lo na aventura:", fontSize = 12.sp, color = Ouro, fontWeight = FontWeight.SemiBold)
                    for (itemNome in jogo.personagem.inventario) {
                        val icone = when {
                            "espada" in itemNome.lowercase() || "lâmina" in itemNome.lowercase() || "adaga" in itemNome.lowercase() -> "🗡️"
                            "poção" in itemNome.lowercase() || "cura" in itemNome.lowercase() || "elixir" in itemNome.lowercase() -> "🧪"
                            "escudo" in itemNome.lowercase() || "armadura" in itemNome.lowercase() -> "🛡️"
                            "chave" in itemNome.lowercase() -> "🗝️"
                            "pergaminho" in itemNome.lowercase() || "livro" in itemNome.lowercase() || "mapa" in itemNome.lowercase() -> "📜"
                            "ouro" in itemNome.lowercase() || "moeda" in itemNome.lowercase() -> "🪙"
                            "anel" in itemNome.lowercase() || "amuleto" in itemNome.lowercase() || "joia" in itemNome.lowercase() -> "💍"
                            else -> "🎒"
                        }
                        Surface(
                            color = Color(0xFF161824),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .cliqueFuturista3D { onUsarItem(itemNome) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(icone, fontSize = 22.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(itemNome, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("Toque para usar ou empunhar na ação", fontSize = 11.sp, color = TextoSuave)
                                }
                                Text("✦ Usar", color = Ouro, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) { Text("Fechar") }
        }
    )
}

