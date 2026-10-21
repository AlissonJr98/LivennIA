package com.livennia.app

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

data class PacoteLivens(
    val id: String,
    val bonus: String,
    val quantidade: Int,
    val precoBrl: String,
    val emDestaque: Boolean = false
)

@Composable
fun ModalCarteiraLivenn(
    vm: AppViewModel,
    onFechar: () -> Unit,
    onAbrirAssinatura: () -> Unit
) {
    val contexto = LocalContext.current

    val pacotes = listOf(
        PacoteLivens("p1", "Bônus Extra +80", 200, "R$ 9,99"),
        PacoteLivens("p2", "Bônus Extra +200", 500, "R$ 24,99", emDestaque = true),
        PacoteLivens("p3", "Bônus Extra +500", 1000, "R$ 50,99"),
        PacoteLivens("p4", "Bônus Extra +1000", 2000, "R$ 99,99"),
        PacoteLivens("p5", "Bônus Extra +2400", 4000, "R$ 204,99"),
        PacoteLivens("p6", "Bônus Extra +8000", 10000, "R$ 509,99")
    )

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
                // Header (Imagem 4)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onFechar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextoClaro)
                    }
                    Text(
                        "Carteira LivennIA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextoClaro,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { Toast.makeText(contexto, "Suporte e Dúvidas Financeiras", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.HeadsetMic, contentDescription = "Suporte", tint = TextoSecundario, modifier = Modifier.size(22.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card 1: Saldo de Livens (Mochi)
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 32.sp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "${vm.conta?.livens ?: 0}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp,
                                        color = Color.White
                                    )
                                    Text("Livens Disponíveis (Mochi)", color = TextoSecundario, fontSize = 12.sp)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    Toast.makeText(contexto, "Histórico de saldo da conta", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Conta Livenn", color = TextoMudo, fontSize = 12.sp)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextoMudo, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Card 2: Banner Ultra VIP 40% OFF (Imagem 4)
                    Surface(
                        color = Color(0xFF1E1710),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, DouradoLivenn.copy(alpha = 0.40f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAbrirAssinatura() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👑", fontSize = 24.sp)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Plano Ultra", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DouradoLivenn)
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFFE65100),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "40% OFF",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text("6 motores de IA premium liberados sem custo por turno", color = TextoSecundario, fontSize = 11.sp)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = DouradoLivenn, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Seção de Pacotes (Grid 2x3 - Imagem 4)
                    Column {
                        Text("Comprar Livens", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoClaro)
                        Spacer(Modifier.height(10.dp))

                        // Grid 2x3 renderizado com rows
                        val chunked = pacotes.chunked(3)
                        chunked.forEach { rowPacotes ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowPacotes.forEach { p ->
                                    Surface(
                                        color = CardObsidian,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, if (p.emDestaque) DouradoLivenn else CardObsidianBorda),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                Toast.makeText(contexto, "Processando compra de ${p.quantidade} Livens (${p.precoBrl})", Toast.LENGTH_LONG).show()
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            // Tag Bônus
                                            Surface(
                                                color = if (p.emDestaque) Color(0xFF332A10) else Color.White.copy(alpha = 0.04f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    p.bonus,
                                                    color = if (p.emDestaque) DouradoLivenn else Color(0xFFC0CA33),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }

                                            Spacer(Modifier.height(6.dp))

                                            Text(
                                                "${p.quantidade}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )

                                            Spacer(Modifier.height(6.dp))

                                            Text(if (p.emDestaque) "⭐🔥" else "⭐", fontSize = 20.sp)

                                            Spacer(Modifier.height(8.dp))

                                            Surface(
                                                color = Color.White.copy(alpha = 0.06f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    p.precoBrl,
                                                    color = TextoClaro,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Seção Uso e Tarifas (Imagem 4)
                    Column {
                        Text("Uso e Tarifas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoClaro)
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Modelos
                            Surface(
                                color = CardObsidian,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardObsidianBorda),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Psychology, contentDescription = null, tint = RoxoPrimario, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Modelos", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextoClaro)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Converse usando modelos flagship avançados.", color = TextoSecundario, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }

                            // Imagens
                            Surface(
                                color = CardObsidian,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardObsidianBorda),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = DouradoLivenn, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Imagens", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextoClaro)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Veja fotos e arte dinâmica de cenas.", color = TextoSecundario, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }

                            // Mensagens de Voz
                            Surface(
                                color = CardObsidian,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardObsidianBorda),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Voz", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextoClaro)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Ouça a voz e narração de cada personagem.", color = TextoSecundario, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}
