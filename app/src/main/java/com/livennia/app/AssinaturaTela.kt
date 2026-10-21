package com.livennia.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val FundoOled = Color(0xFF0A0A0C)
private val DouradoUltra = Color(0xFFF5C842)
private val DouradoUltraFundo = Color(0x33F5C842)
private val PrataPlus = Color(0xFFB0CDE8)
private val PrataPlusFundo = Color(0x33B0CDE8)
private val CardObsidian = Color(0xFF141418)
private val CardObsidianBorda = Color(0xFF282830)
private val TextoClaro = Color(0xFFF0F0F2)
private val TextoSecundario = Color(0xFFA0A0AA)
private val TextoMudo = Color(0xFF6E6E78)
private val AmareloTimer = Color(0xFFFFD54F)

data class VantagemItem(
    val iconeEmoji: String,
    val iconeFundo: Color,
    val titulo: String,
    val tagIlimitado: Boolean = false,
    val descricao: String,
    val clicavel: Boolean = false
)

data class CicloPrecoItem(
    val id: String,
    val periodoTitulo: String,
    val precoAtual: String,
    val precoOriginal: String,
    val economia: String
)

@Composable
fun AssinaturaTela(
    vm: AppViewModel,
    planoInicial: String = "Ultra",
    onVoltar: () -> Unit
) {
    BackHandler { onVoltar() }
    val contexto = LocalContext.current

    var planoSelecionado by remember { mutableStateOf(if (planoInicial.equals("Plus", ignoreCase = true)) "Plus" else "Ultra") }
    var cicloSelecionado by remember { mutableStateOf("mensal") }

    // Modais
    var mostrarModalSuporte by remember { mutableStateOf(false) }
    var mostrarModalCheckout by remember { mutableStateOf(false) }
    var modalLegalTitulo by remember { mutableStateOf<String?>(null) }
    var modalLegalTexto by remember { mutableStateOf<String?>(null) }

    // Timer regressivo (Oferta por tempo limitado)
    var horas by remember { mutableIntStateOf(44) }
    var minutos by remember { mutableIntStateOf(30) }
    var segundos by remember { mutableIntStateOf(49) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (segundos > 0) {
                segundos--
            } else {
                segundos = 59
                if (minutos > 0) {
                    minutos--
                } else {
                    minutos = 59
                    if (horas > 0) horas--
                }
            }
        }
    }

    val ehUltra = planoSelecionado == "Ultra"

    val vantagensUltra = remember {
        listOf(
            VantagemItem(
                iconeEmoji = "🚫",
                iconeFundo = Color(0xFFFFCC00),
                titulo = "Sem anúncios",
                descricao = "Fique imerso — sem anúncios, sem pop-ups, sem interrupções."
            ),
            VantagemItem(
                iconeEmoji = "⚡",
                iconeFundo = Color(0xFF00E5FF),
                titulo = "2000 Livens grátis por mês",
                descricao = "Mais boosters de energia para momentos intensos com os melhores modelos.",
                clicavel = true
            ),
            VantagemItem(
                iconeEmoji = "🧠",
                iconeFundo = Color(0xFFFF5252),
                titulo = "Todos os Modelos Premium",
                tagIlimitado = true,
                descricao = "Acesso ilimitado a Gemini 2.5 Pro, Velvet, Amber e Prime sem fila de espera. Troque livremente.",
                clicavel = true
            ),
            VantagemItem(
                iconeEmoji = "🏷️",
                iconeFundo = Color(0xFFFF4081),
                titulo = "Economia exclusiva em compras mensais de Livens",
                descricao = "Se você comprar Livens ou pacotes mensalmente, economiza até 40% todo mês.",
                clicavel = true
            ),
            VantagemItem(
                iconeEmoji = "💭",
                iconeFundo = Color(0xFF7C4DFF),
                titulo = "Momentos ilimitados, do seu jeito",
                descricao = "Salve Momentos ilimitados, crie pontos de restauração e edite memórias de NPCs à vontade."
            ),
            VantagemItem(
                iconeEmoji = "💌",
                iconeFundo = Color(0xFF69F0AE),
                titulo = "Cartas & Confissões Secretas",
                descricao = "Seus personagens escrevem cartas secretas para você revelando sentimentos não ditos."
            ),
            VantagemItem(
                iconeEmoji = "🎙️",
                iconeFundo = Color(0xFFFF9100),
                titulo = "Voz Neural Cinematográfica",
                tagIlimitado = true,
                descricao = "Narração imersiva com entonações dramáticas, sotaques e sussurros de alta definição."
            )
        )
    }

    val vantagensPlus = remember {
        listOf(
            VantagemItem(
                iconeEmoji = "🚫",
                iconeFundo = Color(0xFFFFCC00),
                titulo = "Sem anúncios",
                descricao = "Fique imerso — sem anúncios, sem pop-ups, sem interrupções."
            ),
            VantagemItem(
                iconeEmoji = "⚡",
                iconeFundo = Color(0xFF00E5FF),
                titulo = "500 Livens grátis por mês",
                descricao = "Bônus mensal para acelerar histórias e criar retratos em alta resolução.",
                clicavel = true
            ),
            VantagemItem(
                iconeEmoji = "🧠",
                iconeFundo = Color(0xFFFF5252),
                titulo = "6 Modelos Premium",
                tagIlimitado = true,
                descricao = "Acesso ilimitado aos modelos Premium populares. Troque livremente.",
                clicavel = true
            ),
            VantagemItem(
                iconeEmoji = "🏷️",
                iconeFundo = Color(0xFFFF4081),
                titulo = "Economia exclusiva em pacotes de Livens",
                descricao = "Economize até 30% nas recargas adicionais durante sua assinatura.",
                clicavel = true
            ),
            VantagemItem(
                iconeEmoji = "💭",
                iconeFundo = Color(0xFF7C4DFF),
                titulo = "Momentos ilimitados, do seu jeito",
                descricao = "Salve Momentos ilimitados e edite memórias das suas narrativas."
            ),
            VantagemItem(
                iconeEmoji = "💌",
                iconeFundo = Color(0xFF69F0AE),
                titulo = "Cartas",
                descricao = "Seu personagem escreve cartas para você sobre coisas não ditas."
            )
        )
    }

    val ciclosUltra = remember {
        listOf(
            CicloPrecoItem("mensal", "Primeiro mês", "BRL 51.99", "BRL 86.99", "BRL 35.00"),
            CicloPrecoItem("trimestral", "Trimestral", "BRL 124.99", "BRL 239.99", "BRL 115.00"),
            CicloPrecoItem("anual", "Anual", "BRL 469.99", "BRL 869.99", "BRL 400.00")
        )
    }

    val ciclosPlus = remember {
        listOf(
            CicloPrecoItem("mensal", "Primeiro mês", "BRL 32.00", "BRL 45.99", "BRL 13.99"),
            CicloPrecoItem("trimestral", "Trimestral", "BRL 61.99", "BRL 119.99", "BRL 58.00"),
            CicloPrecoItem("anual", "Anual", "BRL 249.99", "BRL 464.99", "BRL 215.00")
        )
    }

    val ciclos = if (ehUltra) ciclosUltra else ciclosPlus
    val cicloAtual = ciclos.firstOrNull { it.id == cicloSelecionado } ?: ciclos.first()

    val corDestaque by animateColorAsState(
        targetValue = if (ehUltra) DouradoUltra else PrataPlus,
        animationSpec = tween(300),
        label = "corDestaque"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOled)
            .safeDrawingPadding()
    ) {
        // Brilho ambiente no topo (Efeito halo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (ehUltra) DouradoUltra.copy(alpha = 0.18f) else PrataPlus.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar: Voltar + Seletor Plus / Ultra + Suporte
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoClaro
                    )
                }

                // Seletor Estilo Pílula [ Plus | Ultra ]
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Opção Plus
                        Surface(
                            color = if (!ehUltra) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { planoSelecionado = "Plus" }
                        ) {
                            Text(
                                text = "Plus",
                                color = if (!ehUltra) TextoClaro else TextoMudo,
                                fontWeight = if (!ehUltra) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        // Opção Ultra
                        Surface(
                            color = if (ehUltra) DouradoUltra.copy(alpha = 0.25f) else Color.Transparent,
                            shape = RoundedCornerShape(50),
                            border = if (ehUltra) BorderStroke(1.dp, DouradoUltra.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.clickable { planoSelecionado = "Ultra" }
                        ) {
                            Text(
                                text = "Ultra",
                                color = if (ehUltra) DouradoUltra else TextoMudo,
                                fontWeight = if (ehUltra) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = { mostrarModalSuporte = true }) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Suporte",
                        tint = TextoClaro
                    )
                }
            }

            // Conteúdo Rolável
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(Modifier.height(10.dp))

                // Cabeçalho de Boas-Vindas do Plano
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (ehUltra) {
                        Surface(
                            color = DouradoUltra.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, DouradoUltra.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "✨ Mais popular",
                                color = DouradoUltra,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        text = if (ehUltra) "QUERO Ultra" else "QUERO Plus",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextoClaro,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (ehUltra) "Ganhe 19 vantagens incríveis para mestres e autores" else "Desbloqueie 18 vantagens exclusivas para sua imaginação",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Lista de Vantagens com Figuras / Badges Ilustradas
                val listaAtual = if (ehUltra) vantagensUltra else vantagensPlus
                listaAtual.forEach { item ->
                    LinhaVantagemIlustrada(item = item)
                    Spacer(Modifier.height(14.dp))
                }

                Spacer(Modifier.height(16.dp))

                // Faixa de Desconto e Cronômetro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (ehUltra) "40% OFF!" else "30% OFF!",
                            fontWeight = FontWeight.ExtraBold,
                            color = AmareloTimer,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Oferta por tempo limitado",
                            color = TextoClaro,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Caixinhas do Cronômetro
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CaixaTempoDigito(horas.toString().padStart(2, '0'))
                        Text(":", color = AmareloTimer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        CaixaTempoDigito(minutos.toString().padStart(2, '0'))
                        Text(":", color = AmareloTimer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        CaixaTempoDigito(segundos.toString().padStart(2, '0'))
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Seleção de Ciclo de Pagamento (3 Cards)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ciclos.forEach { ciclo ->
                        val selecionado = ciclo.id == cicloSelecionado
                        Surface(
                            color = if (selecionado) CardObsidian else CardObsidian.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                if (selecionado) 1.5.dp else 1.dp,
                                if (selecionado) corDestaque else CardObsidianBorda
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { cicloSelecionado = ciclo.id }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = ciclo.periodoTitulo,
                                    fontSize = 11.sp,
                                    color = TextoSecundario,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = ciclo.precoAtual,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextoClaro
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = ciclo.precoOriginal,
                                    fontSize = 10.sp,
                                    color = TextoMudo,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Botão de Assinatura Principal (CTA)
                Button(
                    onClick = { mostrarModalCheckout = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ehUltra) DouradoUltra else Color(0xFFD6E4F0)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Assine agora",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Economize ${cicloAtual.economia}",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Rodapé Legal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Política de Privacidade",
                        color = TextoMudo,
                        fontSize = 10.sp,
                        modifier = Modifier.clickable {
                            modalLegalTitulo = "Política de Privacidade"
                            modalLegalTexto = "O LivennIA protege rigorosamente todos os seus dados e históricos de criação. Suas histórias, textos e personagens nunca são compartilhados ou vendidos."
                        }
                    )
                    Text("  |  ", color = TextoMudo, fontSize = 10.sp)
                    Text(
                        text = "Termos de uso",
                        color = TextoMudo,
                        fontSize = 10.sp,
                        modifier = Modifier.clickable {
                            modalLegalTitulo = "Termos de Uso"
                            modalLegalTexto = "Ao assinar os planos Plus ou Ultra, você ganha acesso ilimitado aos modelos selecionados e créditos mensais de energia renováveis."
                        }
                    )
                    Text("  |  ", color = TextoMudo, fontSize = 10.sp)
                    Text(
                        text = "Restaurar compras",
                        color = TextoMudo,
                        fontSize = 10.sp,
                        modifier = Modifier.clickable {
                            Toast.makeText(contexto, "Consultando assinaturas ativas na sua conta...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // MODAL DE SUPORTE
    if (mostrarModalSuporte) {
        AlertDialog(
            onDismissRequest = { mostrarModalSuporte = false },
            title = {
                Text("Central de Atendimento VIP", color = TextoClaro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Dúvidas sobre assinaturas, planos ou suporte financeiro?",
                        color = TextoSecundario,
                        fontSize = 13.sp
                    )
                    Text("• E-mail: suporte@livennia.com", color = TextoClaro, fontSize = 12.sp)
                    Text("• Discord da Comunidade: Suporte 24h na aba VIP", color = TextoClaro, fontSize = 12.sp)
                    Text("• Atendimento prioritário para assinantes Ultra e Plus.", color = DouradoUltra, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarModalSuporte = false }) {
                    Text("Fechar", color = DouradoUltra)
                }
            },
            containerColor = CardObsidian
        )
    }

    // MODAL DE CHECKOUT / FINALIZAÇÃO
    if (mostrarModalCheckout) {
        AlertDialog(
            onDismissRequest = { mostrarModalCheckout = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (ehUltra) "👑 Livenn Ultra VIP" else "✨ Livenn Plus", color = corDestaque, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Plano selecionado: ${cicloAtual.periodoTitulo} por ${cicloAtual.precoAtual}",
                        color = TextoClaro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Economia garantida: ${cicloAtual.economia} no período promocional.",
                        color = DouradoUltra,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "A interface e arquitetura de assinaturas do LivennIA estão 100% integradas. Na próxima etapa, os métodos de pagamento (Google Play Billing / Pix) serão vinculados para cobrança automática.",
                        color = TextoSecundario,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarModalCheckout = false
                        Toast.makeText(contexto, "Plano selecionado com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = corDestaque)
                ) {
                    Text("Confirmar Interesse", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalCheckout = false }) {
                    Text("Voltar", color = TextoSecundario)
                }
            },
            containerColor = CardObsidian
        )
    }

    // MODAL DE TERMOS / POLÍTICAS
    if (modalLegalTitulo != null) {
        AlertDialog(
            onDismissRequest = { modalLegalTitulo = null },
            title = {
                Text(modalLegalTitulo!!, color = TextoClaro, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Text(modalLegalTexto ?: "", color = TextoSecundario, fontSize = 13.sp, lineHeight = 18.sp)
            },
            confirmButton = {
                TextButton(onClick = { modalLegalTitulo = null }) {
                    Text("Entendido", color = DouradoUltra)
                }
            },
            containerColor = CardObsidian
        )
    }
}

@Composable
private fun LinhaVantagemIlustrada(item: VantagemItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Figura Ilustrada estilo 3D Cartoon Badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(item.iconeFundo.copy(alpha = 0.15f), CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(item.iconeFundo.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(item.iconeEmoji, fontSize = 22.sp)
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextoClaro
                )

                if (item.tagIlimitado) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Text(
                            text = "Ilimitado",
                            color = TextoSecundario,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = item.descricao,
                color = TextoSecundario,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        if (item.clicavel) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextoMudo,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CaixaTempoDigito(valor: String) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Text(
            text = valor,
            color = AmareloTimer,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
