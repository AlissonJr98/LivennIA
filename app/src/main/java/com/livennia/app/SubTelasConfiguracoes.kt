package com.livennia.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

// Paleta Oficial Emochi com assinatura LivennIA
private val FundoOled = Color(0xFF000000)
private val CardEscuro = Color(0xFF161618)
private val CardBorda = Color(0xFF26262A)
private val TextoBranco = Color(0xFFFFFFFF)
private val TextoCinza = Color(0xFF8E8E93)
private val TextoPlaceholder = Color(0xFF616165)
private val AmareloPrimario = Color(0xFFF5C842)
private val AmareloBotao = Color(0xFFE5A93C)
private val VermelhoExcluir = Color(0xFFFF453A)
private val VerdeSucesso = Color(0xFF4CAF50)

// -------------------------------------------------------------
// 1. SUB-TELA: DIAGNÓSTICO DE REDE (Imagem 1)
// -------------------------------------------------------------
@Composable
fun SubTelaDiagnosticoRede(
    vm: AppViewModel,
    onVoltar: () -> Unit
) {
    BackHandler { onVoltar() }
    val escopo = rememberCoroutineScope()
    var testando by remember { mutableStateOf(false) }
    var testado by remember { mutableStateOf(false) }
    var latenciaMs by remember { mutableLongStateOf(-1L) }
    var statusConexao by remember { mutableStateOf("Verifique sua conexão com a internet") }
    var internetOk by remember { mutableStateOf(false) }
    var servidorOk by remember { mutableStateOf(false) }

    fun executarTeste() {
        if (testando) return
        testando = true
        statusConexao = "Testando conexão com a internet e servidor LivennIA..."
        escopo.launch {
            val inicio = System.currentTimeMillis()
            var okNet = false
            var okServ = false
            withContext(Dispatchers.IO) {
                // 1. Testa conectividade DNS / Internet rápida
                runCatching {
                    val address = InetAddress.getByName("8.8.8.8")
                    okNet = address.isReachable(3000)
                }.onFailure { okNet = true }

                // 2. Testa saúde do servidor LivennIA
                runCatching {
                    val base = vm.servidor.trimEnd('/')
                    val url = URL("$base/saude")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    conn.requestMethod = "GET"
                    val code = conn.responseCode
                    okServ = (code in 200..299)
                    conn.disconnect()
                }.onFailure {
                    okServ = false
                }
            }
            latenciaMs = System.currentTimeMillis() - inicio
            internetOk = okNet || okServ
            servidorOk = okServ
            testando = false
            testado = true
            statusConexao = when {
                okServ -> "Conexão excelente! Servidor LivennIA online."
                okNet -> "Internet ativa. Conectando aos serviços LivennIA..."
                else -> "Sem conexão detectada. Verifique seu Wi-Fi ou dados móveis."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOled)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra Superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoBranco
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Diagnóstico de rede",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoBranco
                )
            }

            // Conteúdo Central
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Ícone Wi-Fi com velocímetro / antena
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(CardEscuro, shape = CircleShape)
                            .border(1.dp, CardBorda, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (testando) {
                            CircularProgressIndicator(
                                color = AmareloPrimario,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(60.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (testado && servidorOk) Icons.Default.Speed else Icons.Default.Wifi,
                                contentDescription = "Wi-Fi",
                                tint = if (testado) {
                                    if (servidorOk) VerdeSucesso else AmareloPrimario
                                } else TextoCinza,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = statusConexao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (testado && !servidorOk && !internetOk) VermelhoExcluir else TextoCinza,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Resultados detalhados caso já tenha testado
                    AnimatedVisibility(visible = testado) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                                .background(CardEscuro, shape = RoundedCornerShape(16.dp))
                                .border(1.dp, CardBorda, shape = RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Status da Internet", color = TextoCinza, fontSize = 13.sp)
                                Text(
                                    if (internetOk) "Conectado" else "Desconectado",
                                    color = if (internetOk) VerdeSucesso else VermelhoExcluir,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Servidor LivennIA", color = TextoCinza, fontSize = 13.sp)
                                Text(
                                    if (servidorOk) "Online (200 OK)" else "Verificando...",
                                    color = if (servidorOk) VerdeSucesso else AmareloPrimario,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Latência Média", color = TextoCinza, fontSize = 13.sp)
                                Text(
                                    if (latenciaMs > 0) "${latenciaMs} ms" else "--",
                                    color = AmareloPrimario,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Botão Inferior Amarelo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { executarTeste() },
                    enabled = !testando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmareloBotao,
                        disabledContainerColor = AmareloBotao.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (testando) "Verificando conexão..." else "Verificar conexão",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. SUB-TELA: SOBRE (Imagem 2)
// -------------------------------------------------------------
@Composable
fun SubTelaSobre(
    vm: AppViewModel,
    onVoltar: () -> Unit,
    onAbrirDiagnostico: () -> Unit,
    onApagarConta: () -> Unit
) {
    BackHandler { onVoltar() }
    var dialogoTermos by remember { mutableStateOf(false) }
    var dialogoPrivacidade by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOled)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra Superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoBranco
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Sobre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoBranco
                )
            }

            // Cartão de Itens Sobre (Estilo Emochi)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    color = CardEscuro,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ItemSobreLinha(
                            titulo = "Termos e Condições",
                            aoClicar = { dialogoTermos = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        ItemSobreLinha(
                            titulo = "Política de privacidade",
                            aoClicar = { dialogoPrivacidade = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        ItemSobreLinha(
                            titulo = "Excluir conta",
                            corTitulo = VermelhoExcluir,
                            aoClicar = onApagarConta
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        ItemSobreLinha(
                            titulo = "Diagnóstico de rede",
                            aoClicar = onAbrirDiagnostico
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Versão Centralizada no Rodapé
                Text(
                    text = "v1.3.4",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoCinza,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Modal Termos e Condições
    if (dialogoTermos) {
        AlertDialog(
            onDismissRequest = { dialogoTermos = false },
            title = { Text("Termos e Condições - LivennIA", color = TextoBranco) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .height(300.dp)
                ) {
                    Text(
                        text = "1. Aceitação dos Termos: Ao acessar ou utilizar o aplicativo LivennIA, você concorda em cumprir e se vincular a estes termos.\n\n" +
                                "2. Conteúdo e IA: Todas as histórias, mensagens e respostas geradas por IA são fictícias e criadas para fins de entretenimento imersivo. Não refletem opiniões reais ou conselhos profissionais.\n\n" +
                                "3. Conduta da Comunidade: É proibido gerar conteúdo de exploração, assédio direcionado ou atividades ilegais.\n\n" +
                                "4. Moeda e Assinatura: Livens e planos VIP são vinculados à sua conta e utilizáveis exclusivamente no ecossistema LivennIA.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoCinza
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoTermos = false }) {
                    Text("Entendido", color = AmareloPrimario)
                }
            },
            containerColor = CardEscuro
        )
    }

    // Modal Política de Privacidade
    if (dialogoPrivacidade) {
        AlertDialog(
            onDismissRequest = { dialogoPrivacidade = false },
            title = { Text("Política de Privacidade", color = TextoBranco) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .height(300.dp)
                ) {
                    Text(
                        text = "A sua privacidade é prioridade absoluta no LivennIA.\n\n" +
                                "1. Dados Armazenados: Guardamos seu apelido, preferências de narrativa e progresso de jogo de forma criptografada.\n\n" +
                                "2. Segurança de Conversas: Não vendemos seus dados para terceiros nem usamos seus diálogos privados para treinamento público externo sem anonimização.\n\n" +
                                "3. Direito ao Esquecimento: Você tem o direito de apagar todos os seus registros a qualquer momento através da opção 'Excluir conta'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoCinza
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoPrivacidade = false }) {
                    Text("OK", color = AmareloPrimario)
                }
            },
            containerColor = CardEscuro
        )
    }
}

@Composable
private fun ItemSobreLinha(
    titulo: String,
    corTitulo: Color = TextoBranco,
    aoClicar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = aoClicar)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            color = corTitulo,
            fontWeight = FontWeight.Normal
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextoCinza.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// -------------------------------------------------------------
// 3. SUB-TELA: PESQUISA / FEEDBACK COMMUNITY BOARD (Imagem 3)
// -------------------------------------------------------------
data class PostSugestaoItem(
    val id: String,
    val titulo: String,
    val descricao: String,
    var votos: Int,
    var votado: Boolean,
    val status: String,
    val categoria: String,
    val data: String,
    val comentarios: Int
)

@Composable
fun SubTelaPesquisa(
    vm: AppViewModel,
    onVoltar: () -> Unit,
    onNovaSugestao: () -> Unit
) {
    BackHandler { onVoltar() }
    var abaSelecionada by rememberSaveable { mutableStateOf("Boards") }
    var filtroSelecionado by rememberSaveable { mutableStateOf("Popular") }

    val posts = remember {
        mutableStateListOf(
            PostSugestaoItem(
                id = "1",
                titulo = "18+ para usuários adultos",
                descricao = "Adicionar modo 18+ com narrativas profundas e sem restrições exageradas para histórias maduras...",
                votos = 56640,
                votado = false,
                status = "Em Análise",
                categoria = "Sugestão de recurso",
                data = "Dez 08",
                comentarios = 18
            ),
            PostSugestaoItem(
                id = "2",
                titulo = "Permitir conteúdo maduro ou NSFW",
                descricao = "Criar chave segura de desbloqueio para histórias com romance e conteúdo adulto explícito...",
                votos = 24810,
                votado = false,
                status = "Em Análise",
                categoria = "Sugestão de recurso",
                data = "Dez 13",
                comentarios = 3
            ),
            PostSugestaoItem(
                id = "3",
                titulo = "Maior memória de contexto no chat",
                descricao = "Aumentar a janela de contexto para a IA lembrar de detalhes citados há 50 turnos atrás...",
                votos = 16320,
                votado = false,
                status = "Planejado",
                categoria = "Sugestão de recurso",
                data = "Dez 22",
                comentarios = 1
            ),
            PostSugestaoItem(
                id = "4",
                titulo = "Alternar SFW / NSFW dinâmico",
                descricao = "Poder ligar e desligar o modo maduro diretamente dentro da barra de ferramentas do jogo...",
                votos = 12500,
                votado = false,
                status = "Em Desenvolvimento",
                categoria = "Sugestão de recurso",
                data = "Dez 19",
                comentarios = 2
            ),
            PostSugestaoItem(
                id = "5",
                titulo = "IA não assumir a fala do meu personagem",
                descricao = "Garantir que a IA foque apenas no narrador e nos NPCs, deixando as decisões para o jogador...",
                votos = 7120,
                votado = false,
                status = "Concluído",
                categoria = "Sugestão de recurso",
                data = "Dez 12",
                comentarios = 1
            ),
            PostSugestaoItem(
                id = "6",
                titulo = "Chat com múltiplos personagens ao mesmo tempo",
                descricao = "Possibilidade de criar conversas em grupo com 2 ou mais personagens interagindo entre si...",
                votos = 6940,
                votado = false,
                status = "Planejado",
                categoria = "Sugestão de recurso",
                data = "Nov 27",
                comentarios = 14
            ),
            PostSugestaoItem(
                id = "7",
                titulo = "Remover anúncio no meio do capítulo",
                descricao = "Experiência de leitura contínua com banners fixos apenas no final de cada capítulo concluído...",
                votos = 4320,
                votado = false,
                status = "Concluído",
                categoria = "Sugestão de recurso",
                data = "Jan 06",
                comentarios = 6
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOled)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra Superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoBranco
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Feedback",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoBranco
                )
            }

            // Cabeçalho LivennIA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(AmareloPrimario, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "LivennIA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                }

                Surface(
                    color = CardEscuro,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CardBorda)
                ) {
                    Text(
                        text = "Comunidade",
                        color = AmareloPrimario,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Abas: Boards / Roadmap / Changelog
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AbaPill(
                    titulo = "Boards",
                    selecionado = abaSelecionada == "Boards",
                    aoClicar = { abaSelecionada = "Boards" }
                )
                AbaPill(
                    titulo = "Roadmap",
                    selecionado = abaSelecionada == "Roadmap",
                    aoClicar = { abaSelecionada = "Roadmap" }
                )
                AbaPill(
                    titulo = "Changelog",
                    selecionado = abaSelecionada == "Changelog",
                    aoClicar = { abaSelecionada = "Changelog" }
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            if (abaSelecionada == "Boards") {
                // Seção All Posts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Todos os Posts",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    Text(
                        text = "Tem algo a dizer? Participe da conversa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoCinza
                    )

                    Spacer(Modifier.height(10.dp))

                    // Botão Novo Post
                    Surface(
                        color = TextoBranco,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNovaSugestao() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Novo Post / Sugestão",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Filtros rápidos
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FiltroPill(
                            titulo = "Recentes",
                            selecionado = filtroSelecionado == "Recent",
                            aoClicar = { filtroSelecionado = "Recent" }
                        )
                        FiltroPill(
                            titulo = "Populares",
                            selecionado = filtroSelecionado == "Popular",
                            aoClicar = { filtroSelecionado = "Popular" }
                        )
                    }
                }

                // Lista de Posts da Comunidade
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        CardPostItem(
                            item = post,
                            onVotar = {
                                post.votado = !post.votado
                                if (post.votado) post.votos++ else post.votos--
                            }
                        )
                    }
                    item {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            } else if (abaSelecionada == "Roadmap") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CardRoadmapSecao(
                        titulo = "🎯 Em Desenvolvimento",
                        itens = listOf(
                            "Alternar SFW/NSFW dinâmico nas histórias",
                            "Modelos de IA de alta performance com vozes",
                            "Modo cooperativo em tempo real"
                        )
                    )
                    CardRoadmapSecao(
                        titulo = "📋 Planejado (Próximas Versões)",
                        itens = listOf(
                            "Salas com múltiplos personagens simultâneos",
                            "Exportação de aventuras em PDF",
                            "Criador de mundos e cenários personalizado"
                        )
                    )
                    CardRoadmapSecao(
                        titulo = "✅ Concluído Recentemente",
                        itens = listOf(
                            "Rolagem automática do chat em tempo real",
                            "Modelos de IA funcionais (Grátis, Energia e VIP)",
                            "Tela de configurações estilo Emochi v1.3.4"
                        )
                    )
                }
            } else {
                // Changelog
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CardChangelog(
                        versao = "v1.3.4 (Atual)",
                        data = "Hoje",
                        novidades = listOf(
                            "Rolagem fluida do chat com margem de segurança",
                            "Nova classificação de Modelos de IA (Grátis com anúncio, Energia e Premium)",
                            "Reconstrução completa da tela de configurações estilo Emochi",
                            "Diagnóstico de rede em tempo real com teste de latência"
                        )
                    )
                    CardChangelog(
                        versao = "v1.3.3",
                        data = "Outubro 2026",
                        novidades = listOf(
                            "Pôsteres e badges imersivos",
                            "Sistema de afinidade e vínculo com personagens",
                            "Sugestões rápidas de diálogo"
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CardPostItem(
    item: PostSugestaoItem,
    onVotar: () -> Unit
) {
    Surface(
        color = CardEscuro,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Botão de Voto (Upvote)
            Surface(
                color = if (item.votado) AmareloPrimario.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (item.votado) AmareloPrimario else Color.White.copy(alpha = 0.10f)
                ),
                modifier = Modifier
                    .width(52.dp)
                    .clickable { onVotar() }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Votar",
                        tint = if (item.votado) AmareloPrimario else TextoCinza,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (item.votos >= 1000) String.format("%.1fK", item.votos / 1000f) else item.votos.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.votado) AmareloPrimario else TextoBranco
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Detalhes da Sugestão
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoBranco
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoCinza,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge de status
                    Surface(
                        color = when (item.status) {
                            "Concluído" -> VerdeSucesso.copy(alpha = 0.20f)
                            "Planejado" -> Color(0xFF9C27B0).copy(alpha = 0.20f)
                            "Em Desenvolvimento" -> AmareloPrimario.copy(alpha = 0.20f)
                            else -> Color(0xFF2196F3).copy(alpha = 0.20f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.status) {
                                "Concluído" -> VerdeSucesso
                                "Planejado" -> Color(0xFFBA68C8)
                                "Em Desenvolvimento" -> AmareloPrimario
                                else -> Color(0xFF64B5F6)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text("•", color = TextoPlaceholder, fontSize = 10.sp)
                    Text(item.data, color = TextoPlaceholder, fontSize = 11.sp)

                    Spacer(Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = TextoPlaceholder,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.comentarios.toString(),
                            color = TextoPlaceholder,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AbaPill(titulo: String, selecionado: Boolean, aoClicar: () -> Unit) {
    Surface(
        color = if (selecionado) Color.White.copy(alpha = 0.12f) else Color.Transparent,
        shape = RoundedCornerShape(50),
        modifier = Modifier.clickable { aoClicar() }
    ) {
        Text(
            text = titulo,
            color = if (selecionado) TextoBranco else TextoCinza,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FiltroPill(titulo: String, selecionado: Boolean, aoClicar: () -> Unit) {
    Surface(
        color = if (selecionado) CardEscuro else Color.Transparent,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (selecionado) AmareloPrimario else CardBorda),
        modifier = Modifier.clickable { aoClicar() }
    ) {
        Text(
            text = titulo,
            color = if (selecionado) AmareloPrimario else TextoCinza,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun CardRoadmapSecao(titulo: String, itens: List<String>) {
    Surface(
        color = CardEscuro,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, color = TextoBranco, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            itens.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("•", color = AmareloPrimario, modifier = Modifier.padding(end = 8.dp))
                    Text(item, color = TextoCinza, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CardChangelog(versao: String, data: String, novidades: List<String>) {
    Surface(
        color = CardEscuro,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(versao, fontWeight = FontWeight.Bold, color = AmareloPrimario, fontSize = 14.sp)
                Text(data, color = TextoPlaceholder, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            novidades.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓", color = VerdeSucesso, modifier = Modifier.padding(end = 8.dp), fontSize = 11.sp)
                    Text(item, color = TextoCinza, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. SUB-TELA: SOLICITAR RECURSO (Imagem 4)
// -------------------------------------------------------------
@Composable
fun SubTelaSolicitarRecurso(
    vm: AppViewModel,
    onVoltar: () -> Unit
) {
    BackHandler { onVoltar() }
    val contexto = LocalContext.current
    var descricao by rememberSaveable { mutableStateOf("") }
    var motivo by rememberSaveable { mutableStateOf("") }
    var urgencia by rememberSaveable { mutableStateOf("Escolha uma opção") }
    var urgenciaMenuAberta by remember { mutableStateOf(false) }
    var contato by rememberSaveable { mutableStateOf("") }
    var printAnexado by rememberSaveable { mutableStateOf(false) }

    val opcoesUrgencia = listOf(
        "Baixa - Seria legal ter",
        "Média - Importante para a experiência",
        "Alta - Sinto muita falta",
        "Crítica - Essencial para o meu uso"
    )

    val podeEnviar = descricao.isNotBlank() && motivo.isNotBlank() && urgencia != "Escolha uma opção"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOled)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra Superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoBranco
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Solicitar recurso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoBranco
                )
            }

            // Formulário com rolagem vertical
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo 1: Descrição da função*
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Descrição da função*",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    CampoTextoEscuro(
                        valor = descricao,
                        onValorChange = { descricao = it },
                        placeholder = "Qual é o objetivo do recurso? Existe um produto similar que possamos referenciar?",
                        linhas = 4
                    )
                }

                // Campo 2: Por que você quer esse recurso?*
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Por que você quer esse recurso?*",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    CampoTextoEscuro(
                        valor = motivo,
                        onValorChange = { motivo = it },
                        placeholder = "Para que serve esse recurso?",
                        linhas = 3
                    )
                }

                // Campo 3: O quanto isso é urgente para você?*
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "O quanto isso é urgente para você?*",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    Box {
                        Surface(
                            color = CardEscuro,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorda),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { urgenciaMenuAberta = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = urgencia,
                                    color = if (urgencia == "Escolha uma opção") TextoPlaceholder else TextoBranco,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextoCinza,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = urgenciaMenuAberta,
                            onDismissRequest = { urgenciaMenuAberta = false },
                            modifier = Modifier.background(CardEscuro)
                        ) {
                            opcoesUrgencia.forEach { opcao ->
                                DropdownMenuItem(
                                    text = { Text(opcao, color = TextoBranco, fontSize = 13.sp) },
                                    onClick = {
                                        urgencia = opcao
                                        urgenciaMenuAberta = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Campo 4: Adicionar captura de tela
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Adicionar captura de tela",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    Surface(
                        color = CardEscuro,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (printAnexado) AmareloPrimario else CardBorda),
                        modifier = Modifier
                            .size(90.dp)
                            .clickable {
                                printAnexado = !printAnexado
                                Toast.makeText(
                                    contexto,
                                    if (printAnexado) "Captura de tela anexada!" else "Captura removida",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (printAnexado) Icons.Default.Check else Icons.Default.CloudUpload,
                                contentDescription = "Enviar",
                                tint = if (printAnexado) AmareloPrimario else TextoCinza,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (printAnexado) "Anexado" else "Enviar",
                                fontSize = 12.sp,
                                color = if (printAnexado) AmareloPrimario else TextoCinza
                            )
                        }
                    }
                }

                // Campo 5: (Opcional) Informações de contato
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "(Opcional) Informações de contato",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    CampoTextoEscuro(
                        valor = contato,
                        onValorChange = { contato = it },
                        placeholder = "Informe seu e-mail ou ID do Discord. Você será notificado quando corrigirmos.",
                        linhas = 2
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // Barra Inferior de Ação (Cancelar e Enviar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancelar
                Surface(
                    color = CardEscuro,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CardBorda),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onVoltar() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Cancelar", color = TextoBranco, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Enviar
                Surface(
                    color = if (podeEnviar) AmareloBotao else Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(enabled = podeEnviar) {
                            val relatorio = "[SUGESTÃO]: $descricao\nMotivo: $motivo\nUrgência: $urgencia\nContato: $contato"
                            vm.enviarFeedback(5, relatorio)
                            Toast.makeText(contexto, "Sugestão enviada com sucesso! Obrigado.", Toast.LENGTH_LONG).show()
                            onVoltar()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Enviar",
                            color = if (podeEnviar) Color.Black else TextoPlaceholder,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. SUB-TELA: REPORTAR ERRO (Imagem 5)
// -------------------------------------------------------------
@Composable
fun SubTelaReportarErro(
    vm: AppViewModel,
    onVoltar: () -> Unit
) {
    BackHandler { onVoltar() }
    val contexto = LocalContext.current
    var relato by rememberSaveable { mutableStateOf("") }
    var printAnexado by rememberSaveable { mutableStateOf(false) }
    var contato by rememberSaveable { mutableStateOf("") }

    val podeEnviar = relato.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOled)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra Superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoBranco
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Reportar erro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoBranco
                )
            }

            // Formulário
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Campo 1: Descreva o erro*
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Descreva o erro*",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    CampoTextoEscuro(
                        valor = relato,
                        onValorChange = { relato = it },
                        placeholder = "Você pode reportar qualquer bug encontrado aqui.",
                        linhas = 4
                    )
                }

                // Campo 2: Adicionar captura de tela
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Adicionar captura de tela",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    Surface(
                        color = CardEscuro,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (printAnexado) AmareloPrimario else CardBorda),
                        modifier = Modifier
                            .size(90.dp)
                            .clickable {
                                printAnexado = !printAnexado
                                Toast.makeText(
                                    contexto,
                                    if (printAnexado) "Captura de tela anexada!" else "Captura removida",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (printAnexado) Icons.Default.Check else Icons.Default.CloudUpload,
                                contentDescription = "Enviar",
                                tint = if (printAnexado) AmareloPrimario else TextoCinza,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (printAnexado) "Anexado" else "Enviar",
                                fontSize = 12.sp,
                                color = if (printAnexado) AmareloPrimario else TextoCinza
                            )
                        }
                    }
                }

                // Campo 3: (Opcional) Informações de contato
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "(Opcional) Informações de contato",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoBranco
                    )
                    CampoTextoEscuro(
                        valor = contato,
                        onValorChange = { contato = it },
                        placeholder = "Deixe seu e-mail ou ID do Discord. Vamos te avisar quando estiver resolvido.",
                        linhas = 2
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // Barra Inferior de Ação (Cancelar e Enviar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancelar
                Surface(
                    color = CardEscuro,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CardBorda),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onVoltar() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Cancelar", color = TextoBranco, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Enviar
                Surface(
                    color = if (podeEnviar) AmareloBotao else Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(enabled = podeEnviar) {
                            val relatorio = "[BUG REPORT]: $relato\nContato: $contato"
                            vm.enviarFeedback(1, relatorio)
                            Toast.makeText(contexto, "Bug reportado com sucesso. Nossa equipe investigará!", Toast.LENGTH_LONG).show()
                            onVoltar()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Enviar",
                            color = if (podeEnviar) Color.Black else TextoPlaceholder,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CAMPO DE TEXTO ESCURO ESTILO EMOCHI
// -------------------------------------------------------------
@Composable
private fun CampoTextoEscuro(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    linhas: Int = 1
) {
    Surface(
        color = CardEscuro,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            if (valor.isEmpty()) {
                Text(
                    text = placeholder,
                    color = TextoPlaceholder,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            BasicTextField(
                value = valor,
                onValueChange = onValorChange,
                textStyle = TextStyle(
                    color = TextoBranco,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                cursorBrush = SolidColor(AmareloPrimario),
                modifier = Modifier
                    .fillMaxWidth()
                    .height((linhas * 24).dp)
            )
        }
    }
}
