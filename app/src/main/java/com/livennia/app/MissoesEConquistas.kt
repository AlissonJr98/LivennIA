package com.livennia.app

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemMissao(
    val id: String,
    val titulo: String,
    val descricao: String,
    val icone: String,
    val recompensaLivens: Int,
    val meta: Int,
    val progressoAtual: Int,
    val resgatado: Boolean
) {
    val concluida: Boolean get() = progressoAtual >= meta
}

object GerenciadorMissoes {
    private const val PREFS_NAME = "livennia_missoes_v1"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun hojeString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun registrarProgresso(context: Context, chave: String, incremento: Int = 1) {
        val prefs = getPrefs(context)
        val dataHoje = hojeString()
        val chaveData = "data_ultima_sessao"

        // Se mudou de dia, reseta as diárias
        if (prefs.getString(chaveData, "") != dataHoje) {
            prefs.edit()
                .putString(chaveData, dataHoje)
                .putInt("diaria_mensagens", 0)
                .putInt("diaria_dados", 0)
                .putInt("diaria_vinculos", 0)
                .putInt("diaria_cenas", 0)
                .remove("resgatado_diaria_mensagens")
                .remove("resgatado_diaria_dados")
                .remove("resgatado_diaria_vinculos")
                .remove("resgatado_diaria_cenas")
                .apply()
        }

        val atual = prefs.getInt(chave, 0)
        prefs.edit().putInt(chave, atual + incremento).apply()
    }

    fun obterMissoes(context: Context): Pair<List<ItemMissao>, List<ItemMissao>> {
        val prefs = getPrefs(context)
        val dataHoje = hojeString()
        if (prefs.getString("data_ultima_sessao", "") != dataHoje) {
            prefs.edit()
                .putString("data_ultima_sessao", dataHoje)
                .putInt("diaria_mensagens", 0)
                .putInt("diaria_dados", 0)
                .putInt("diaria_vinculos", 0)
                .putInt("diaria_cenas", 0)
                .apply()
        }

        val diarias = listOf(
            ItemMissao(
                id = "diaria_mensagens",
                titulo = "Contador de Histórias",
                descricao = "Envie 5 ações ou diálogos em qualquer jornada",
                icone = "💬",
                recompensaLivens = 15,
                meta = 5,
                progressoAtual = prefs.getInt("diaria_mensagens", 0).coerceAtMost(5),
                resgatado = prefs.getBoolean("resgatado_diaria_mensagens", false)
            ),
            ItemMissao(
                id = "diaria_dados",
                titulo = "Destino Rolado",
                descricao = "Role dados ou faça uma jogada tática no RPG",
                icone = "🎲",
                recompensaLivens = 20,
                meta = 2,
                progressoAtual = prefs.getInt("diaria_dados", 0).coerceAtMost(2),
                resgatado = prefs.getBoolean("resgatado_diaria_dados", false)
            ),
            ItemMissao(
                id = "diaria_vinculos",
                titulo = "Afinidade de Almas",
                descricao = "Avance o turno e cultive a conexão com um personagem",
                icone = "💖",
                recompensaLivens = 25,
                meta = 1,
                progressoAtual = prefs.getInt("diaria_vinculos", 0).coerceAtMost(1),
                resgatado = prefs.getBoolean("resgatado_diaria_vinculos", false)
            ),
            ItemMissao(
                id = "diaria_cenas",
                titulo = "Olhar do Bardo",
                descricao = "Gere uma ilustração de cena ou veja a galeria de arte",
                icone = "🖼️",
                recompensaLivens = 20,
                meta = 1,
                progressoAtual = prefs.getInt("diaria_cenas", 0).coerceAtMost(1),
                resgatado = prefs.getBoolean("resgatado_diaria_cenas", false)
            )
        )

        val lendarias = listOf(
            ItemMissao(
                id = "lendaria_50_turnos",
                titulo = "Viajante Experiente",
                descricao = "Alcance 50 turnos de narrativa pelo multiverso",
                icone = "🌟",
                recompensaLivens = 50,
                meta = 50,
                progressoAtual = prefs.getInt("total_turnos", 12).coerceAtMost(50),
                resgatado = prefs.getBoolean("resgatado_lendaria_50_turnos", false)
            ),
            ItemMissao(
                id = "lendaria_d20_critico",
                titulo = "Bênção dos Deuses",
                descricao = "Role 10 jogadas de dados no RPG",
                icone = "⚔️",
                recompensaLivens = 60,
                meta = 10,
                progressoAtual = prefs.getInt("total_dados_rolados", 3).coerceAtMost(10),
                resgatado = prefs.getBoolean("resgatado_lendaria_d20_critico", false)
            ),
            ItemMissao(
                id = "lendaria_amigos",
                titulo = "Círculo de Alianças",
                descricao = "Siga 2 criadores ou amigos na comunidade",
                icone = "👥",
                recompensaLivens = 40,
                meta = 2,
                progressoAtual = prefs.getInt("total_seguidos", 1).coerceAtMost(2),
                resgatado = prefs.getBoolean("resgatado_lendaria_amigos", false)
            ),
            ItemMissao(
                id = "lendaria_criador",
                titulo = "Arquiteto de Mundos",
                descricao = "Crie seu próprio personagem ou cenário",
                icone = "👑",
                recompensaLivens = 100,
                meta = 1,
                progressoAtual = prefs.getInt("total_criados", 0).coerceAtMost(1),
                resgatado = prefs.getBoolean("resgatado_lendaria_criador", false)
            )
        )

        return Pair(diarias, lendarias)
    }

    fun resgatar(context: Context, missaoId: String, recompensa: Int, aoCreditar: (Int) -> Unit): Boolean {
        val prefs = getPrefs(context)
        val chaveResgate = "resgatado_$missaoId"
        if (prefs.getBoolean(chaveResgate, false)) return false

        prefs.edit().putBoolean(chaveResgate, true).apply()
        aoCreditar(recompensa)
        return true
    }
}

@Composable
fun DialogoMissoesEConquistas(
    vm: AppViewModel,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current
    var abaAtiva by remember { mutableStateOf("Diárias") }
    var refreshKey by remember { mutableStateOf(0) }

    val (diarias, lendarias) = remember(refreshKey) {
        GerenciadorMissoes.obterMissoes(contexto)
    }

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
                    .fillMaxHeight(0.85f)
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
                            Text("🏆", fontSize = 22.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Quadro de Conquistas",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val saldo = vm.conta?.livens ?: 0
                                Text(
                                    text = "Saldo: ✨ $saldo Livens",
                                    color = Ouro,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
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

                    // Seletor de Abas (Diárias vs Lendárias)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B1D2C), RoundedCornerShape(50))
                            .padding(3.dp)
                    ) {
                        listOf("Diárias", "Lendárias").forEach { aba ->
                            val ativa = abaAtiva == aba
                            Surface(
                                color = if (ativa) Ouro.copy(alpha = 0.20f) else Color.Transparent,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, if (ativa) Ouro.copy(alpha = 0.5f) else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { abaAtiva = aba }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (aba == "Diárias") "☀️ Missões Diárias" else "👑 Conquistas Lendárias",
                                        color = if (ativa) Ouro else TextoSuave,
                                        fontSize = 12.sp,
                                        fontWeight = if (ativa) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Lista de Missões
                    val listaAtual = if (abaAtiva == "Diárias") diarias else lendarias
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listaAtual.forEach { missao ->
                            CardMissao(
                                missao = missao,
                                aoResgatar = {
                                    val sucesso = GerenciadorMissoes.resgatar(
                                        contexto,
                                        missao.id,
                                        missao.recompensaLivens
                                    ) { recompensa ->
                                        vm.creditarLivens(recompensa)
                                        Toast.makeText(contexto, "+$recompensa Livens resgatados com sucesso! 🎉", Toast.LENGTH_SHORT).show()
                                    }
                                    if (sucesso) refreshKey++
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardMissao(
    missao: ItemMissao,
    aoResgatar: () -> Unit
) {
    Surface(
        color = Color(0xFF181A28),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            when {
                missao.resgatado -> Color.White.copy(alpha = 0.08f)
                missao.concluida -> Ouro.copy(alpha = 0.55f)
                else -> Color(0xFF7C4DFF).copy(alpha = 0.25f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (missao.concluida && !missao.resgatado) Ouro.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(missao.icone, fontSize = 20.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = missao.titulo,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "+${missao.recompensaLivens} Livens",
                        color = Ouro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = missao.descricao,
                    color = TextoSuave,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progresso = (missao.progressoAtual.toFloat() / missao.meta.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progresso },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = Ouro,
                        trackColor = Color.White.copy(alpha = 0.10f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${missao.progressoAtual}/${missao.meta}",
                        color = TextoSuave,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Botão de Resgate
            when {
                missao.resgatado -> {
                    Surface(
                        color = Color.White.copy(alpha = 0.06f),
                        shape = Cantos.pilula
                    ) {
                        Text(
                            text = "Coletado",
                            color = TextoSuave.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                missao.concluida -> {
                    Button(
                        onClick = aoResgatar,
                        colors = ButtonDefaults.buttonColors(containerColor = Ouro),
                        shape = Cantos.pilula,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Resgatar",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
                else -> {
                    Surface(
                        color = Color.White.copy(alpha = 0.06f),
                        shape = Cantos.pilula
                    ) {
                        Text(
                            text = "Em curso",
                            color = TextoSuave,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}
