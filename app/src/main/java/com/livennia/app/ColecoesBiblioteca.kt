package com.livennia.app

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

object GerenciadorColecoes {
    private const val PREFS_NAME = "livennia_colecoes_v1"

    val COLECOES_PADRAO = listOf(
        "⭐ Favoritos",
        "⚔️ Meus RPGs",
        "⏳ Para Jogar Depois",
        "💖 Histórias de Romance"
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun obterCenarios(context: Context, colecao: String): Set<String> {
        val prefs = getPrefs(context)
        return prefs.getStringSet("col_$colecao", emptySet()) ?: emptySet()
    }

    fun alternarCenarioNaColecao(context: Context, cenarioId: String, colecao: String): Boolean {
        val prefs = getPrefs(context)
        val chave = "col_$colecao"
        val atual = prefs.getStringSet(chave, emptySet())?.toMutableSet() ?: mutableSetOf()
        val adicionou: Boolean
        if (atual.contains(cenarioId)) {
            atual.remove(cenarioId)
            adicionou = false
        } else {
            atual.add(cenarioId)
            adicionou = true
        }
        prefs.edit().putStringSet(chave, atual).apply()
        return adicionou
    }

    fun estaNaColecao(context: Context, cenarioId: String, colecao: String): Boolean {
        val prefs = getPrefs(context)
        return prefs.getStringSet("col_$colecao", emptySet())?.contains(cenarioId) == true
    }
}

@Composable
fun DialogoGerenciarColecoes(
    cenarioId: String,
    cenarioNome: String,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.80f))
                .clickable { onFechar() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFF131422),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(Ouro, Color(0xFF7C4DFF)))),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .clickable(enabled = false) {}
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📁", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Salvar em Coleção",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = cenarioNome,
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

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GerenciadorColecoes.COLECOES_PADRAO.forEach { colecao ->
                            val selecionado = remember(refreshKey) {
                                GerenciadorColecoes.estaNaColecao(contexto, cenarioId, colecao)
                            }

                            Surface(
                                color = if (selecionado) Ouro.copy(alpha = 0.15f) else Color(0xFF1B1D2C),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (selecionado) Ouro.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val adicionou = GerenciadorColecoes.alternarCenarioNaColecao(
                                            contexto,
                                            cenarioId,
                                            colecao
                                        )
                                        val msg = if (adicionou) "Adicionado a $colecao!" else "Removido de $colecao"
                                        Toast.makeText(contexto, msg, Toast.LENGTH_SHORT).show()
                                        refreshKey++
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = colecao,
                                        color = if (selecionado) Color.White else TextoSuave,
                                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (selecionado) "✓ Salvo" else "+ Adicionar",
                                        color = if (selecionado) Ouro else TextoSuave.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = onFechar,
                        colors = ButtonDefaults.buttonColors(containerColor = Ouro),
                        shape = Cantos.pilula,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Concluir", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
