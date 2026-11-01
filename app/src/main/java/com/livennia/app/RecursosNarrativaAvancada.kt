package com.livennia.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Random
import kotlin.math.sin

// ============================================================================
// 1. GERENCIADOR DE SOUNDSCAPES PROCEDURAIS (Áudio de Ambiente Imersivo)
// ============================================================================

object GerenciadorAmbienteSonoro {
    enum class TipoAmbiente(val nomeLegivel: String, val icone: String, val descricao: String) {
        DESLIGADO("Silêncio", "🔇", "Sem som de fundo"),
        TAVERNA("Taverna Acolhedora", "🍻", "Lareira crepitante e murmúrios suaves"),
        MASMORRA("Masmorra & Cripta", "🕯️", "Gotejar distante, ecos e vento soturno"),
        FLORESTA("Bosque Encantado", "🍃", "Brisa suave, folhagens e calma mágica"),
        CYBERPUNK("Neon & Chuva", "🌧️", "Chuva constante e sintetizador noturno")
    }

    var ambienteAtivo by mutableStateOf(TipoAmbiente.DESLIGADO)
        private set
    var volume by mutableStateOf(0.40f)
        private set

    private var audioTrack: AudioTrack? = null
    private var jobAudio: Job? = null
    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default)

    fun alternarAmbiente(novo: TipoAmbiente) {
        if (ambienteAtivo == novo) {
            parar()
        } else {
            ambienteAtivo = novo
            iniciarLoop(novo)
        }
    }

    fun ajustarVolume(novoVolume: Float) {
        volume = novoVolume.coerceIn(0.05f, 1.0f)
        try {
            audioTrack?.setVolume(volume)
        } catch (_: Exception) {}
    }

    fun parar() {
        ambienteAtivo = TipoAmbiente.DESLIGADO
        jobAudio?.cancel()
        jobAudio = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    private fun iniciarLoop(tipo: TipoAmbiente) {
        jobAudio?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}

        if (tipo == TipoAmbiente.DESLIGADO) return

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        audioTrack = AudioTrack.Builder()
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

        audioTrack?.setVolume(volume)
        audioTrack?.play()

        jobAudio = scope.launch {
            val buffer = ShortArray(bufferSize / 2)
            val random = Random()
            var fase1 = 0.0
            var fase2 = 0.0
            var filtroRosa = 0.0

            while (isActive) {
                for (i in buffer.indices) {
                    val amostra: Double = when (tipo) {
                        TipoAmbiente.TAVERNA -> {
                            // Crepitar suave de lareira (ruído rosa com estalos ocasionais)
                            val ruidoBranco = (random.nextDouble() * 2.0 - 1.0)
                            filtroRosa = 0.95 * filtroRosa + 0.05 * ruidoBranco
                            val estalo = if (random.nextDouble() < 0.002) (random.nextDouble() * 0.8) else 0.0
                            (filtroRosa * 0.4 + estalo) * 16000.0
                        }
                        TipoAmbiente.MASMORRA -> {
                            // Drone grave e profundo de reverberação
                            fase1 += (2.0 * Math.PI * 55.0) / sampleRate
                            fase2 += (2.0 * Math.PI * 110.0) / sampleRate
                            val drone = (sin(fase1) * 0.6 + sin(fase2) * 0.3)
                            val vento = (random.nextDouble() * 2.0 - 1.0) * 0.15
                            (drone + vento) * 14000.0
                        }
                        TipoAmbiente.FLORESTA -> {
                            // Brisa etérea calma em harmônicos
                            fase1 += (2.0 * Math.PI * 220.0) / sampleRate
                            fase2 += (2.0 * Math.PI * 329.63) / sampleRate // Mi4
                            val harmonia = (sin(fase1) * 0.4 + sin(fase2) * 0.3)
                            val brisa = (random.nextDouble() * 2.0 - 1.0) * 0.1
                            (harmonia + brisa) * 12000.0
                        }
                        TipoAmbiente.CYBERPUNK -> {
                            // Chuva constante + synthwave drone em 110Hz e 164Hz
                            fase1 += (2.0 * Math.PI * 110.0) / sampleRate
                            fase2 += (2.0 * Math.PI * 164.81) / sampleRate
                            val synth = sin(fase1) * 0.45 + sin(fase2) * 0.35
                            val chuva = (random.nextDouble() * 2.0 - 1.0) * 0.25
                            (synth + chuva) * 15000.0
                        }
                        TipoAmbiente.DESLIGADO -> 0.0
                    }
                    buffer[i] = amostra.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }
}

// ============================================================================
// 2. CARD DE CONFRONTO TÁTICO NO MODO RPG (Combate Visual Integrado)
// ============================================================================

@Composable
fun CardConfrontoTatico(
    jogo: JogoResponse,
    narrativaAtual: String,
    emCombateDetectado: Boolean,
    aoExecutarAcao: (String) -> Unit
) {
    if (jogo.modo != "rpg" || (!emCombateDetectado && !narrativaAtual.contains("combate", ignoreCase = true) && !narrativaAtual.contains("inimig", ignoreCase = true))) {
        return
    }

    val contexto = LocalContext.current
    var vidaInimigoMax by remember(jogo.turno) { mutableStateOf(60) }
    var vidaInimigoAtual by remember(jogo.turno) { mutableStateOf(45) }
    var ultimoDano by remember { mutableStateOf<String?>(null) }
    var dadoAnimado by remember { mutableStateOf(false) }

    val nomeInimigo = remember(narrativaAtual) {
        when {
            narrativaAtual.contains("drag", ignoreCase = true) -> "Dragão Ancião"
            narrativaAtual.contains("goblin", ignoreCase = true) -> "Líder Goblin"
            narrativaAtual.contains("orc", ignoreCase = true) -> "Guerreiro Orc"
            narrativaAtual.contains("lich", ignoreCase = true) -> "Lich Espectral"
            narrativaAtual.contains("lobo", ignoreCase = true) -> "Lobo das Sombras"
            narrativaAtual.contains("guarda", ignoreCase = true) -> "Capitão da Guarda"
            narrativaAtual.contains("ladr", ignoreCase = true) -> "Assassino das Sombras"
            narrativaAtual.contains("mago", ignoreCase = true) -> "Mago Renegado"
            else -> "Inimigo Hostil"
        }
    }

    Surface(
        color = Color(0xFF1E1215),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, Color(0xFFFF5252).copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header do Inimigo: Ícone, Nome e HP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFD32F2F).copy(alpha = 0.25f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⚔️", fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = nomeInimigo,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (vidaInimigoAtual > 0) "Em combate • Turno ativo" else "Derrotado!",
                            color = if (vidaInimigoAtual > 0) Color(0xFFFF8A80) else Color(0xFF81C784),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.40f),
                    shape = Cantos.pilula,
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "❤️ $vidaInimigoAtual / $vidaInimigoMax HP",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Barra de vida do Inimigo
            val progressoHp = (vidaInimigoAtual.toFloat() / vidaInimigoMax.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progressoHp },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (progressoHp > 0.4f) Color(0xFFE53935) else Color(0xFFFF1744),
                trackColor = Color.White.copy(alpha = 0.10f)
            )

            if (ultimoDano != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = ultimoDano ?: "",
                    color = Ouro,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(10.dp))

            // 4 Ações Táticas Rápidas de Combate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Golpe Físico
                Surface(
                    color = Color(0xFF2C1618),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.30f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val dado = Random().nextInt(20) + 1
                            val dano = Random().nextInt(12) + 5
                            vidaInimigoAtual = (vidaInimigoAtual - dano).coerceAtLeast(0)
                            ultimoDano = "🎲 d20 rolou $dado! Causou $dano de dano físico!"
                            executarVibracaoDano(contexto)
                            aoExecutarAcao("Ataque Físico com a arma principal (d20=$dado, dano=$dano).")
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚔️", fontSize = 16.sp)
                        Text("Golpe", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Magia / Habilidade
                Surface(
                    color = Color(0xFF1D162C),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF9080FC).copy(alpha = 0.30f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val dado = Random().nextInt(20) + 1
                            val dano = Random().nextInt(18) + 8
                            vidaInimigoAtual = (vidaInimigoAtual - dano).coerceAtLeast(0)
                            ultimoDano = "✨ Crítico Arcano! Causou $dano de dano elemental!"
                            executarVibracaoDano(contexto)
                            aoExecutarAcao("Canalizo magia arcana contra o $nomeInimigo (d20=$dado, dano=$dano)!")
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Text("Magia", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Esquiva Tática
                Surface(
                    color = Color(0xFF152226),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.30f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val dado = Random().nextInt(20) + 1
                            ultimoDano = "🛡️ Esquiva rolou $dado! Você assumiu postura defensiva."
                            aoExecutarAcao("Assumo postura defensiva e busco cobertura contra os ataques (d20=$dado).")
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛡️", fontSize = 16.sp)
                        Text("Defesa", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Usar Item
                Surface(
                    color = Color(0xFF262015),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Ouro.copy(alpha = 0.30f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            ultimoDano = "🧪 Poção utilizada com sucesso!"
                            aoExecutarAcao("Uso um item restaurador da mochila para recuperar energias.")
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧪", fontSize = 16.sp)
                        Text("Poção", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun executarVibracaoDano(context: Context) {
    try {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        v?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
    } catch (_: Exception) {}
}

// ============================================================================
// 3. DIÁLOGO DA ÁRVORE DO DESTINO (Mapa de Escolhas e Consequências)
// ============================================================================

@Composable
fun DialogoArvoreDestino(
    jogo: JogoResponse,
    chat: List<MensagemDto>,
    onFechar: () -> Unit
) {
    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onFechar() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFF131422),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Ouro, Color(0xFF7C4DFF)))),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.80f)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌿", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Árvore do Destino",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bifurcações e escolhas da sua crônica",
                                    color = TextoSuave,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp).clickable { onFechar() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✕", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Linha do tempo de nós de decisão
                    val turnosChave = remember(chat) {
                        val lista = mutableListOf<ParDecisao>()
                        lista.add(ParDecisao(1, "O Despertar da Jornada", "Primeiro contato e início da aventura", true))
                        chat.forEachIndexed { idx, msg ->
                            val turnoReal = idx + 1
                            if (turnoReal % 3 == 0 || idx == chat.size - 1) {
                                val resumo = (msg.situacao ?: msg.acao).take(55).ifBlank { "Decisão do Turno $turnoReal" }
                                lista.add(ParDecisao(turnoReal, "Turno $turnoReal", resumo, true))
                            }
                        }
                        // Adiciona nós futuros / caminhos alternativos
                        lista.add(ParDecisao(chat.size + 3, "Convergência Crítica", "✦ Ramo de destino ainda inexplorado", false))
                        lista.add(ParDecisao(chat.size + 7, "Desfecho Épico", "✦ Final alternativo bloqueado", false))
                        lista
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        turnosChave.forEachIndexed { i, no ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // Coluna visual da linha conectora
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(36.dp)
                                ) {
                                    Surface(
                                        color = if (no.desbloqueado) Ouro else Color.White.copy(alpha = 0.20f),
                                        shape = CircleShape,
                                        border = BorderStroke(2.dp, if (no.desbloqueado) Color(0xFF7C4DFF) else Color.Transparent),
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(if (no.desbloqueado) "✦" else "🔒", fontSize = 10.sp, color = if (no.desbloqueado) Color.Black else Color.White)
                                        }
                                    }
                                    if (i < turnosChave.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(48.dp)
                                                .background(if (no.desbloqueado) Ouro.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f))
                                        )
                                    }
                                }

                                Spacer(Modifier.width(10.dp))

                                // Conteúdo do Nó
                                Surface(
                                    color = if (no.desbloqueado) Color(0xFF1B1D2C) else Color(0xFF13141C),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (no.desbloqueado) Ouro.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = no.titulo,
                                                color = if (no.desbloqueado) Color.White else TextoSuave,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = if (no.desbloqueado) "Ativo" else "Bloqueado",
                                                color = if (no.desbloqueado) Ouro else TextoSuave.copy(alpha = 0.6f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = no.descricao,
                                            color = if (no.desbloqueado) Color(0xFFD6D0F5) else TextoSuave.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ParDecisao(
    val turno: Int,
    val titulo: String,
    val descricao: String,
    val desbloqueado: Boolean
)

// ============================================================================
// 4. CARD DE COMPARTILHAMENTO VIRAL 9:16 (Story/Status do Instagram & WhatsApp)
// ============================================================================

@Composable
fun DialogoCardCompartilhar(
    cenarioNome: String,
    personagemNome: String,
    citacao: String,
    nivelVinculo: Int,
    tituloVinculo: String,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onFechar() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .clickable(enabled = false) {},
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card 9:16 Vertical
                Surface(
                    color = Color(0xFF10111A),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, Brush.verticalGradient(listOf(Ouro, Color(0xFF7C4DFF)))),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 14f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Fundo com degradê cósmico
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF2C1654),
                                            Color(0xFF131422),
                                            Color(0xFF0C0D14)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Topo do Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✦", color = Ouro, fontSize = 14.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "LivennIA Crônicas",
                                        color = Ouro,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = Cantos.pilula
                                ) {
                                    Text(
                                        text = "Nvl $nivelVinculo • $tituloVinculo",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Miolo: Citação Marcante com aspas douradas
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "“",
                                    color = Ouro,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp
                                )
                                val trechoCitacao = remember(citacao) {
                                    citacao.replace("\"", "").replace("*", "").take(160)
                                }
                                Text(
                                    text = trechoCitacao.ifBlank { "Cada escolha forja um novo destino nesta jornada." },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = "”",
                                    color = Ouro,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp
                                )

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = "— $personagemNome",
                                    color = Color(0xFFA78BFA),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = cenarioNome,
                                    color = TextoSuave,
                                    fontSize = 11.sp
                                )
                            }

                            // Rodapé do Card
                            Surface(
                                color = Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Jogue no LivennIA App",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "✦ IA Narrativa",
                                        color = Ouro,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Botão de Compartilhar
                Button(
                    onClick = {
                        compartilharCardComoImagem(contexto, personagemNome, citacao)
                        onFechar()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Ouro),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Compartilhar no Instagram / WhatsApp",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

private fun compartilharCardComoImagem(context: Context, personagem: String, citacao: String) {
    try {
        val width = 720
        val height = 1280
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fundo
        val paintFundo = Paint().apply { color = android.graphics.Color.parseColor("#121320") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintFundo)

        // Borda dourada
        val paintBorda = Paint().apply {
            color = android.graphics.Color.parseColor("#F5C842")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(RectF(24f, 24f, width - 24f, height - 24f), 32f, 32f, paintBorda)

        // Textos
        val paintTitulo = Paint().apply {
            color = android.graphics.Color.parseColor("#F5C842")
            textSize = 34f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✦ LivennIA Crônicas ✦", width / 2f, 120f, paintTitulo)

        val paintTexto = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        val trecho = citacao.take(140).replace("\"", "")
        canvas.drawText("“$trecho”", width / 2f, height / 2f - 40f, paintTexto)

        val paintAutor = Paint().apply {
            color = android.graphics.Color.parseColor("#A78BFA")
            textSize = 30f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("— $personagem", width / 2f, height / 2f + 50f, paintAutor)

        val paintRodape = Paint().apply {
            color = android.graphics.Color.LTGRAY
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Viva histórias interativas geradas por IA", width / 2f, height - 90f, paintRodape)

        // Salva e compartilha
        val file = File(context.cacheDir, "story_livennia_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar Crônica"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao gerar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
