package com.livennia.app

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// =========================================================================
// 1. SISTEMA DE TEMAS / SKINS ESTÉTICAS DO CHAT
// =========================================================================

enum class TemaChat(
    val id: String,
    val nome: String,
    val icone: String,
    val descricao: String,
    val corFundo: Color,
    val corBalaoIA: Color,
    val corBalaoUsuario: Color,
    val corDestaque: Color,
    val corTexto: Color
) {
    COSMICO(
        id = "cosmico",
        nome = "Cósmico (Padrão)",
        icone = "🌌",
        descricao = "Roxo estelar, partículas e dourado brilhante",
        corFundo = Color(0xFF0C0D14),
        corBalaoIA = Color(0xFF131522),
        corBalaoUsuario = Color(0xFF281C44),
        corDestaque = Color(0xFFF5C842),
        corTexto = Color(0xFFEEEEF0)
    ),
    PERGAMINHO(
        id = "pergaminho",
        nome = "Pergaminho Antigo",
        icone = "📜",
        descricao = "Tons quentes de sépia para RPG medieval",
        corFundo = Color(0xFF1A1510),
        corBalaoIA = Color(0xFF262018),
        corBalaoUsuario = Color(0xFF382E20),
        corDestaque = Color(0xFFD4AF37),
        corTexto = Color(0xFFF0E6D2)
    ),
    CYBERPUNK(
        id = "cyberpunk",
        nome = "Cyberpunk Neon",
        icone = "⚡",
        descricao = "Preto profundo com ciano elétrico e neon",
        corFundo = Color(0xFF060709),
        corBalaoIA = Color(0xFF0F1522),
        corBalaoUsuario = Color(0xFF1E0C2B),
        corDestaque = Color(0xFF00E5FF),
        corTexto = Color(0xFFE0F7FA)
    ),
    MINIMALISTA(
        id = "minimalista",
        nome = "Minimalista Clean",
        icone = "⚪",
        descricao = "Escuro neutro moderno, fluido e sem ruídos",
        corFundo = Color(0xFF121214),
        corBalaoIA = Color(0xFF1A1A1E),
        corBalaoUsuario = Color(0xFF24242A),
        corDestaque = Color(0xFFB0B0C0),
        corTexto = Color(0xFFEDEDED)
    );

    companion object {
        fun obterTema(context: Context): TemaChat {
            val prefs = context.getSharedPreferences("livennia_estetica", Context.MODE_PRIVATE)
            val idSalvo = prefs.getString("tema_chat_ativo", COSMICO.id)
            return values().firstOrNull { it.id == idSalvo } ?: COSMICO
        }

        fun salvarTema(context: Context, tema: TemaChat) {
            val prefs = context.getSharedPreferences("livennia_estetica", Context.MODE_PRIVATE)
            prefs.edit().putString("tema_chat_ativo", tema.id).apply()
        }
    }
}

// =========================================================================
// 2. ORBE DOS DESTINOS: ROLETA DIÁRIA COM PRÊMIOS
// =========================================================================

data class PremioRoleta(
    val titulo: String,
    val icone: String,
    val descricao: String,
    val livens: Int = 0,
    val item: String? = null
)

@Composable
fun ModalOrbeDosDestinos(
    vm: AppViewModel,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current
    val prefs = remember { contexto.getSharedPreferences("livennia_roleta", Context.MODE_PRIVATE) }
    val dataAtualStr = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    val jaGirouHoje = remember { prefs.getString("ultimo_giro_data", "") == dataAtualStr }

    var girando by remember { mutableStateOf(false) }
    var premioSorteado by remember { mutableStateOf<PremioRoleta?>(null) }
    val anguloRoleta = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()

    val premios = remember {
        listOf(
            PremioRoleta("10 Livens", "✨", "Moedas para aventuras", livens = 10),
            PremioRoleta("Poção de Cura", "🧪", "Restaura 25 HP no RPG", item = "Poção de Vida Menor"),
            PremioRoleta("25 Livens", "🌟", "Bolsa de cristais mágicos", livens = 25),
            PremioRoleta("Arte Grátis", "🎨", "1 Ilustração de Cena sem anúncio", item = "Passe de Ilustração"),
            PremioRoleta("15 Livens", "✨", "Brilho estelar acumulado", livens = 15),
            PremioRoleta("Jackpot 50", "👑", "Fortuna dos Destinos!", livens = 50)
        )
    }

    Dialog(
        onDismissRequest = { if (!girando) onFechar() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = !girando) { onFechar() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFF131422),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(listOf(Color(0xFFF5C842), Color(0xFF7C4DFF).copy(alpha = 0.4f)))
                ),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔮 Orbe dos Destinos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if (jaGirouHoje && premioSorteado == null) "Você já reivindicou sua dádiva de hoje! Volte amanhã." else "Gire diariamente para obter bênçãos, itens e Livens!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Representação visual do Orbe / Roleta
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .rotate(anguloRoleta.value)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF7C4DFF),
                                        Color(0xFFF5C842),
                                        Color(0xFF00E5FF),
                                        Color(0xFFFF4081),
                                        Color(0xFF7C4DFF)
                                    )
                                ),
                                CircleShape
                            )
                            .border(3.dp, Color(0xFFF5C842), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFF0F1018),
                            shape = CircleShape,
                            modifier = Modifier.size(150.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (girando) "🌀" else (premioSorteado?.icone ?: "✦"),
                                        fontSize = 38.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = premioSorteado?.titulo ?: "Destino",
                                        color = Color(0xFFF5C842),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (premioSorteado != null) {
                        Surface(
                            color = Color(0xFF1C2235),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFF5C842).copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎉 Bênção Concedida!",
                                    color = Color(0xFFF5C842),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${premioSorteado?.icone} ${premioSorteado?.titulo}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Text(
                                    text = premioSorteado?.descricao ?: "",
                                    color = TextoSuave,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    Button(
                        onClick = {
                            if (!jaGirouHoje && !girando) {
                                girando = true
                                escopo.launch {
                                    val voltas = (5..8).random() * 360f
                                    val sorteioIdx = premios.indices.random()
                                    val anguloFinal = voltas + (sorteioIdx * 60f)

                                    anguloRoleta.animateTo(
                                        targetValue = anguloFinal,
                                        animationSpec = tween(durationMillis = 3200, easing = FastOutSlowInEasing)
                                    )

                                    val ganho = premios[sorteioIdx]
                                    premioSorteado = ganho
                                    girando = false

                                    // Credita os prêmios
                                    if (ganho.livens > 0) {
                                        vm.creditarLivens(ganho.livens)
                                    }
                                    prefs.edit().putString("ultimo_giro_data", dataAtualStr).apply()
                                    Toast.makeText(contexto, "Prêmio: ${ganho.titulo}!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                onFechar()
                            }
                        },
                        enabled = !girando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (jaGirouHoje && premioSorteado == null) Color.Gray.copy(alpha = 0.4f) else Color(0xFFF5C842)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when {
                                girando -> "Consultando o Cosmos..."
                                premioSorteado != null -> "Coletar e Concluir"
                                jaGirouHoje -> "Giro Diário Concluído"
                                else -> "Girar o Orbe dos Destinos"
                            },
                            color = if (jaGirouHoje && premioSorteado == null) Color.White.copy(alpha = 0.5f) else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. MEMÓRIAS SECRETAS & CONTOS BLOQUEADOS POR VÍNCULO
// =========================================================================

data class MemoriaSecreta(
    val id: String,
    val nivelExigido: Int,
    val titulo: String,
    val icone: String,
    val preambulo: String,
    val contoRevelado: String
)

@Composable
fun ModalMemoriasSecretas(
    nomePersonagem: String,
    nivelVinculoAtual: Int,
    onFechar: () -> Unit
) {
    val memorias = remember(nomePersonagem) {
        listOf(
            MemoriaSecreta(
                id = "m1",
                nivelExigido = 1,
                titulo = "O Primeiro Olhar",
                icone = "🌱",
                preambulo = "As primeiras impressões que ficaram guardadas em silêncio.",
                contoRevelado = "Quando nossos caminhos se cruzaram pela primeira vez, $nomePersonagem hesitou. Havia desconfiança nas sombras do olhar, mas sob o disfarce de firmeza, uma faísca de curiosidade despertou instantaneamente."
            ),
            MemoriaSecreta(
                id = "m2",
                nivelExigido = 3,
                titulo = "O Fardo Guardado",
                icone = "🗝️",
                preambulo = "Uma confissão rara que só a confiança verdadeira liberta.",
                contoRevelado = "Naquela noite à beira da fogueira, quando o mundo parecia dormir, $nomePersonagem quebrou o silêncio: 'Nem sempre fui assim... Antes das cinzas e das batalhas, havia alguém que eu jurava proteger.' Pela primeira vez, você viu o peso real dessa jornada."
            ),
            MemoriaSecreta(
                id = "m3",
                nivelExigido = 5,
                titulo = "O Juramento Eterno",
                icone = "💖",
                preambulo = "O laço supremo onde o destino de ambos se entrelaça para sempre.",
                contoRevelado = "Sob a constelação de Livenn, $nomePersonagem estendeu a mão com serenidade absoluta: 'Se o mundo inteiro ruir amanhã, que eu caia ao seu lado. Nosso laço transcende qualquer lenda que este reino jamais escreverá.'"
            )
        )
    }

    var memoriaAberta by remember { mutableStateOf<MemoriaSecreta?>(null) }

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
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📖 Memórias & Contos Secretos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Desbloqueie laços profundos com $nomePersonagem",
                                color = TextoSuave,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = onFechar) {
                            Text("✕", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    memorias.forEach { mem ->
                        val desbloqueado = nivelVinculoAtual >= mem.nivelExigido
                        Surface(
                            color = if (desbloqueado) Color(0xFF1A1D2E) else Color(0xFF12131C),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (desbloqueado) Color(0xFFF5C842).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (desbloqueado) {
                                        memoriaAberta = if (memoriaAberta?.id == mem.id) null else mem
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (desbloqueado) mem.icone else "🔒", fontSize = 18.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = mem.titulo,
                                                color = if (desbloqueado) Color.White else TextoSuave.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = if (desbloqueado) "Vínculo Nvl ${mem.nivelExigido} • Desbloqueado" else "Requer Vínculo Nível ${mem.nivelExigido}",
                                                color = if (desbloqueado) Color(0xFFF5C842) else Color(0xFFFF5252).copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    if (desbloqueado) {
                                        Text(if (memoriaAberta?.id == mem.id) "▲" else "▼", color = TextoSuave, fontSize = 11.sp)
                                    }
                                }

                                if (memoriaAberta?.id == mem.id && desbloqueado) {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(
                                        color = Color(0xFF0F101A),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = mem.contoRevelado,
                                            color = Color(0xFFF0E6D2),
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(12.dp)
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

// =========================================================================
// 4. ATRIBUTOS ATIVOS DO RPG (D20 MODIFIERS: STR, DEX, INT, CHA)
// =========================================================================

data class ModificadoresAtributos(
    val forca: Int,
    val destreza: Int,
    val mente: Int,
    val carisma: Int
)

fun calcularAtributosRPG(classeOuPapel: String, seed: Int): ModificadoresAtributos {
    val p = classeOuPapel.lowercase()
    return when {
        p.contains("guerreir") || p.contains("barbar") || p.contains("paladin") || p.contains("cavaleir") ->
            ModificadoresAtributos(forca = 3, destreza = 1, mente = 0, carisma = 1)
        p.contains("ladin") || p.contains("assassin") || p.contains("arqueir") || p.contains("ranger") ->
            ModificadoresAtributos(forca = 1, destreza = 3, mente = 1, carisma = 0)
        p.contains("mag") || p.contains("brux") || p.contains("feiticeir") || p.contains("arcano") ->
            ModificadoresAtributos(forca = 0, destreza = 1, mente = 3, carisma = 1)
        p.contains("bard") || p.contains("nobre") || p.contains("clerig") || p.contains("diplomata") ->
            ModificadoresAtributos(forca = 0, destreza = 1, mente = 1, carisma = 3)
        else -> {
            val base = (seed % 4)
            ModificadoresAtributos(
                forca = if (base == 0) 2 else 1,
                destreza = if (base == 1) 2 else 1,
                mente = if (base == 2) 2 else 1,
                carisma = if (base == 3) 2 else 1
            )
        }
    }
}

// =========================================================================
// 5. BACKUP & SINCRONIZAÇÃO LOCAL EM 1 CLIQUE
// =========================================================================

object GerenciadorBackupLocal {
    fun exportarBackup(context: Context, vm: AppViewModel): File? {
        return try {
            val raiz = JSONObject()
            raiz.put("versao", 1)
            raiz.put("data_exportacao", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            raiz.put("apelido", vm.apelido)

            // Salva lista de histórias / jogos
            val jogosArray = JSONArray()
            vm.jogos.forEach { j ->
                val jobj = JSONObject()
                jobj.put("jogoId", j.jogoId)
                jobj.put("cenarioNome", j.cenarioNome)
                jobj.put("turno", j.turno)
                jobj.put("genero", j.genero)
                jobj.put("modo", j.modo)
                jogosArray.put(jobj)
            }
            raiz.put("aventuras", jogosArray)

            // Salva personas
            val personasArray = JSONArray()
            vm.personas.forEach { p ->
                val pobj = JSONObject()
                pobj.put("id", p.id)
                pobj.put("nome", p.nome)
                pobj.put("genero", p.genero)
                pobj.put("aparencia", p.aparencia)
                personasArray.put(pobj)
            }
            raiz.put("personas", personasArray)

            val pastaBackup = File(context.getExternalFilesDir(null) ?: context.filesDir, "backups")
            pastaBackup.mkdirs()
            val arquivo = File(pastaBackup, "livennia_backup_${System.currentTimeMillis()}.json")
            arquivo.writeText(raiz.toString(2), Charsets.UTF_8)
            arquivo
        } catch (_: Exception) {
            null
        }
    }

    fun listarBackups(context: Context): List<File> {
        val pasta = File(context.getExternalFilesDir(null) ?: context.filesDir, "backups")
        if (!pasta.exists()) return emptyList()
        return pasta.listFiles { f -> f.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
