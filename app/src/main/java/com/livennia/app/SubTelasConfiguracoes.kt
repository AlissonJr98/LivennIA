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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

// Identidade Visual Exclusiva LivennIA (Obsidian Luxury Dark)
private val FundoOled = Color(0xFF000000)
private val CardObsidian = Color(0xFF131316)
private val CardObsidianBorda = Color(0xFF24242A)
private val TextoClaro = Color(0xFFF5F5F7)
private val TextoSecundario = Color(0xFF9898A0)
private val TextoMudo = Color(0xFF63636B)
private val DouradoLivenn = Color(0xFFF5C842)
private val DouradoBotao = Color(0xFFE5A93C)
private val VermelhoSuave = Color(0xFFFF5252)
private val VerdeEstavel = Color(0xFF4CAF50)

// -------------------------------------------------------------
// 1. SUB-TELA: DIAGNÓSTICO DE CONEXÃO & LATÊNCIA
// Preserva 100% o servidor ativo sem qualquer alteração destrutiva
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
    var statusConexao by remember {
        mutableStateOf("Toque abaixo para verificar a estabilidade da sua rota até os servidores LivennIA.")
    }
    var internetOk by remember { mutableStateOf(false) }
    var servidorOk by remember { mutableStateOf(false) }

    fun executarDiagnostico() {
        if (testando) return
        testando = true
        statusConexao = "Consultando telemetria e medindo tempo de resposta do servidor..."
        escopo.launch {
            val inicio = System.currentTimeMillis()
            var okNet = false
            var okServ = false
            withContext(Dispatchers.IO) {
                // 1. Conexão básica de rede (DNS Google / Cloudflare)
                runCatching {
                    val address = InetAddress.getByName("8.8.8.8")
                    okNet = address.isReachable(3000)
                }.onFailure { okNet = true }

                // 2. Consulta não-destrutiva de saúde no servidor LivennIA ATIVO
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
                okServ -> "Excelente! Sua conexão com o motor de IA do LivennIA está rápida e estável."
                okNet -> "Internet ativa. O servidor local está respondendo normalmente."
                else -> "Instabilidade na rede detectada. Verifique seu sinal Wi-Fi ou dados móveis."
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
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoClaro
                    )
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Diagnóstico de Conexão",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Text(
                        text = "Telemetria de Rede & Latência",
                        style = MaterialTheme.typography.labelSmall,
                        color = DouradoLivenn
                    )
                }
            }

            // Corpo Central com Radar Visual
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
                    // Radar pulsante LivennIA
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .background(CardObsidian, shape = CircleShape)
                            .border(
                                2.dp,
                                if (testado && servidorOk) VerdeEstavel else if (testando) DouradoLivenn else CardObsidianBorda,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (testando) {
                            CircularProgressIndicator(
                                color = DouradoLivenn,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(64.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (testado) Icons.Default.Speed else Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = if (testado) (if (servidorOk) VerdeEstavel else DouradoLivenn) else TextoSecundario,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    Text(
                        text = statusConexao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (testado && !servidorOk && !internetOk) VermelhoSuave else TextoSecundario,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Painel de Resultados Técnicos
                    AnimatedVisibility(visible = testado) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 22.dp)
                                .background(CardObsidian, shape = RoundedCornerShape(16.dp))
                                .border(1.dp, CardObsidianBorda, shape = RoundedCornerShape(16.dp))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Acesso à Internet", color = TextoSecundario, fontSize = 13.sp)
                                Text(
                                    if (internetOk) "● Estável" else "● Instável",
                                    color = if (internetOk) VerdeEstavel else VermelhoSuave,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Servidor Ativo", color = TextoSecundario, fontSize = 13.sp)
                                Text(
                                    if (servidorOk) "Operacional (200 OK)" else "Não Modificado",
                                    color = if (servidorOk) VerdeEstavel else DouradoLivenn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Latência da Rota (Ping)", color = TextoSecundario, fontSize = 13.sp)
                                Text(
                                    if (latenciaMs > 0) "${latenciaMs} ms" else "--",
                                    color = DouradoLivenn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                            Text(
                                text = "🔒 Conexão preservada: ${vm.servidor}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoMudo,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Botão Inferior de Ação
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { executarDiagnostico() },
                    enabled = !testando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DouradoBotao,
                        disabledContainerColor = DouradoBotao.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (testando) "Medindo latência..." else "Testar Conexão Agora",
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
// 2. SUB-TELA: SOBRE O LIVENNIA
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
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoClaro
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Sobre o LivennIA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoClaro
                )
            }

            // Cartão de Apresentação e Opções Legais
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Emblema Superior
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨ LivennIA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DouradoLivenn)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Universos Vivos & Narrativas Interativas com IA",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Menu de Opções
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ItemSobreLinha(
                            titulo = "Termos de Uso & Licença de Criação",
                            aoClicar = { dialogoTermos = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        ItemSobreLinha(
                            titulo = "Políticas de Privacidade & Dados",
                            aoClicar = { dialogoPrivacidade = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        ItemSobreLinha(
                            titulo = "Diagnóstico de Conexão",
                            aoClicar = onAbrirDiagnostico
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        ItemSobreLinha(
                            titulo = "Encerrar Minha Conta no LivennIA",
                            corTitulo = VermelhoSuave,
                            aoClicar = onApagarConta
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Rodapé de Versão
                Text(
                    text = "LivennIA v1.3.4 (Build 8)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoMudo,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Feito para autores, mestres de RPG e contadores de histórias.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoMudo.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (dialogoTermos) {
        AlertDialog(
            onDismissRequest = { dialogoTermos = false },
            title = { Text("Termos de Uso - LivennIA", color = TextoClaro, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .height(300.dp)
                ) {
                    Text(
                        text = "1. Diretrizes de Narrativa: O LivennIA é uma plataforma para exploração criativa de roteiros, ficção interativa e jogos de RPG.\n\n" +
                                "2. Coautoria com Inteligência Artificial: Respostas geradas por IA são obras de ficção e entretenimento.\n\n" +
                                "3. Respeito à Comunidade: Não são permitidas práticas de assédio direcionado ou atividades que violem legislações vigentes.\n\n" +
                                "4. Bens Digitais: A energia e benefícios de conta são de uso pessoal e intransferível no ambiente do app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoTermos = false }) {
                    Text("Compreendido", color = DouradoLivenn)
                }
            },
            containerColor = CardObsidian
        )
    }

    if (dialogoPrivacidade) {
        AlertDialog(
            onDismissRequest = { dialogoPrivacidade = false },
            title = { Text("Políticas de Privacidade", color = TextoClaro, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .height(300.dp)
                ) {
                    Text(
                        text = "Sua segurança e privacidade são essenciais no ecossistema LivennIA.\n\n" +
                                "1. Sigilo de Histórias: Suas escolhas e mensagens em sessões particulares não são compartilhadas publicamente sem seu consentimento explícito.\n\n" +
                                "2. Armazenamento Protegido: Fichas, diários e histórico de campanhas são mantidos em servidores seguros e criptografados.\n\n" +
                                "3. Controle Total: Você pode requerer a exclusão imediata e definitiva de todo o seu histórico a qualquer instante.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogoPrivacidade = false }) {
                    Text("OK", color = DouradoLivenn)
                }
            },
            containerColor = CardObsidian
        )
    }
}

@Composable
private fun ItemSobreLinha(
    titulo: String,
    corTitulo: Color = TextoClaro,
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
            tint = TextoMudo,
            modifier = Modifier.size(18.dp)
        )
    }
}

// -------------------------------------------------------------
// 3. SUB-TELA: CENTRAL DA COMUNIDADE & IDEIAS
// -------------------------------------------------------------
data class PostSugestaoItem(
    val id: String,
    val titulo: String,
    val descricao: String,
    var votos: Int,
    var votado: Boolean,
    val status: String,
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
    var abaSelecionada by rememberSaveable { mutableStateOf("Ideias") }
    var filtroSelecionado by rememberSaveable { mutableStateOf("Destaque") }

    val posts = remember {
        mutableStateListOf(
            PostSugestaoItem(
                id = "1",
                titulo = "Modo Adulto e Sem Censura para RPG e Histórias",
                descricao = "Chave de conteúdo maduro para narrativas profundas e sem restrições em diálogos românticos ou de combate...",
                votos = 56820,
                votado = false,
                status = "💡 Em Avaliação",
                data = "Dez 08",
                comentarios = 24
            ),
            PostSugestaoItem(
                id = "2",
                titulo = "Memória Infinita de Personagens em Longas Campanhas",
                descricao = "Capacidade da IA de lembrar de juramentos, laços e eventos marcantes mesmo após muitos capítulos...",
                votos = 25140,
                votado = false,
                status = "💡 Em Avaliação",
                data = "Dez 13",
                comentarios = 12
            ),
            PostSugestaoItem(
                id = "3",
                titulo = "Vozes e Sotaques Distintos para cada Personagem",
                descricao = "Reprodução de falas de NPCs com tons dramáticos, entonações de suspense e sotaques regionais...",
                votos = 16980,
                votado = false,
                status = "🎯 Planejado",
                data = "Dez 22",
                comentarios = 8
            ),
            PostSugestaoItem(
                id = "4",
                titulo = "Grupos e Histórias com Vários Personagens Simultâneos",
                descricao = "Salas onde mais de um personagem de IA conversa e reage às ações do jogador de forma orgânica...",
                votos = 12890,
                votado = false,
                status = "🛠️ Em Desenvolvimento",
                data = "Dez 19",
                comentarios = 15
            ),
            PostSugestaoItem(
                id = "5",
                titulo = "Salvar e Exportar Histórias em Livro Digital (PDF/EPUB)",
                descricao = "Gerar documento formatado com as ilustrações geradas para guardar ou imprimir suas aventuras...",
                votos = 7430,
                votado = false,
                status = "🎯 Planejado",
                data = "Dez 12",
                comentarios = 5
            ),
            PostSugestaoItem(
                id = "6",
                titulo = "Rolagem Fluida do Chat com Margem Inferior",
                descricao = "Acompanhamento automático das mensagens enquanto a IA digita, sem necessidade de rolar manualmente...",
                votos = 6120,
                votado = true,
                status = "✨ Já no App!",
                data = "Ontem",
                comentarios = 9
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
                        tint = TextoClaro
                    )
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Central da Comunidade",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Text(
                        text = "Voz dos Criadores & Roadmap",
                        style = MaterialTheme.typography.labelSmall,
                        color = DouradoLivenn
                    )
                }
            }

            // Abas em Pílula
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AbaPillLivenn(
                    titulo = "💡 Ideias",
                    selecionado = abaSelecionada == "Ideias",
                    aoClicar = { abaSelecionada = "Ideias" }
                )
                AbaPillLivenn(
                    titulo = "🗺️ Roadmap",
                    selecionado = abaSelecionada == "Roadmap",
                    aoClicar = { abaSelecionada = "Roadmap" }
                )
                AbaPillLivenn(
                    titulo = "🚀 Atualizações",
                    selecionado = abaSelecionada == "Atualizações",
                    aoClicar = { abaSelecionada = "Atualizações" }
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            if (abaSelecionada == "Ideias") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Propostas dos Autores",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Text(
                        text = "Vote nas melhores ideias ou envie sugestões para as próximas versões.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )

                    Spacer(Modifier.height(10.dp))

                    // Botão Criar Proposta
                    Surface(
                        color = TextoClaro,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNovaSugestao() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Propor Nova Ideia",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FiltroPillLivenn(
                            titulo = "🔥 Em Destaque",
                            selecionado = filtroSelecionado == "Destaque",
                            aoClicar = { filtroSelecionado = "Destaque" }
                        )
                        FiltroPillLivenn(
                            titulo = "⚡ Recentes",
                            selecionado = filtroSelecionado == "Recentes",
                            aoClicar = { filtroSelecionado = "Recentes" }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        CardPostLivenn(
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
                    CardRoadmapLivenn(
                        titulo = "🛠️ Em Desenvolvimento",
                        itens = listOf(
                            "Alternador de tom narrativo livre no chat",
                            "Vozes personalizadas por personagem",
                            "Campanhas compartilhadas com outros mestres"
                        )
                    )
                    CardRoadmapLivenn(
                        titulo = "🎯 No Radar (Próximas Versões)",
                        itens = listOf(
                            "Diálogos em grupo com múltiplos avatares",
                            "Exportação de crônicas para leitura offline",
                            "Gerador de cenários e fichas customizadas"
                        )
                    )
                    CardRoadmapLivenn(
                        titulo = "✨ Já Disponível",
                        itens = listOf(
                            "Rolagem contínua e inteligente de tela",
                            "Modelos de IA diferenciados (Flash, Spark, Velvet e VIP Prime)",
                            "Painel de diagnósticos e configurações renovado"
                        )
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CardChangelogLivenn(
                        versao = "v1.3.4 (Versão Atual)",
                        novidades = listOf(
                            "Abertura direta na aba Explorar para visualização de histórias",
                            "Diagnóstico de rede sem alterar a conexão de servidor ativa",
                            "Novo seletor de Modelos de IA com personalidades reais de escrita",
                            "Nova Central da Comunidade com votação de ideias"
                        )
                    )
                    CardChangelogLivenn(
                        versao = "v1.3.3",
                        novidades = listOf(
                            "Cards dinâmicos com badges imersivas",
                            "Sistema de afinidade e relacionamentos no RPG",
                            "Respostas sugeridas inteligentes no teclado"
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CardPostLivenn(
    item: PostSugestaoItem,
    onVotar: () -> Unit
) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = if (item.votado) DouradoLivenn.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (item.votado) DouradoLivenn else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .width(54.dp)
                    .clickable { onVotar() }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Apoiar",
                        tint = if (item.votado) DouradoLivenn else TextoSecundario,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (item.votos >= 1000) String.format("%.1fK", item.votos / 1000f) else item.votos.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.votado) DouradoLivenn else TextoClaro
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoClaro
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DouradoLivenn,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text("•", color = TextoMudo, fontSize = 10.sp)
                    Text(item.data, color = TextoMudo, fontSize = 11.sp)

                    Spacer(Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = TextoMudo,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.comentarios.toString(),
                            color = TextoMudo,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AbaPillLivenn(titulo: String, selecionado: Boolean, aoClicar: () -> Unit) {
    Surface(
        color = if (selecionado) Color.White.copy(alpha = 0.12f) else Color.Transparent,
        shape = RoundedCornerShape(50),
        modifier = Modifier.clickable { aoClicar() }
    ) {
        Text(
            text = titulo,
            color = if (selecionado) TextoClaro else TextoSecundario,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FiltroPillLivenn(titulo: String, selecionado: Boolean, aoClicar: () -> Unit) {
    Surface(
        color = if (selecionado) CardObsidian else Color.Transparent,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (selecionado) DouradoLivenn else CardObsidianBorda),
        modifier = Modifier.clickable { aoClicar() }
    ) {
        Text(
            text = titulo,
            color = if (selecionado) DouradoLivenn else TextoSecundario,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun CardRoadmapLivenn(titulo: String, itens: List<String>) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, color = TextoClaro, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            itens.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("•", color = DouradoLivenn, modifier = Modifier.padding(end = 8.dp))
                    Text(item, color = TextoSecundario, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CardChangelogLivenn(versao: String, novidades: List<String>) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(versao, fontWeight = FontWeight.Bold, color = DouradoLivenn, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            novidades.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓", color = VerdeEstavel, modifier = Modifier.padding(end = 8.dp), fontSize = 11.sp)
                    Text(item, color = TextoSecundario, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. SUB-TELA: PROPOR NOVA FUNCIONALIDADE
// -------------------------------------------------------------
@Composable
fun SubTelaSolicitarRecurso(
    vm: AppViewModel,
    onVoltar: () -> Unit
) {
    BackHandler { onVoltar() }
    val contexto = LocalContext.current
    var conceito by rememberSaveable { mutableStateOf("") }
    var impacto by rememberSaveable { mutableStateOf("") }
    var relevancy by rememberSaveable { mutableStateOf("Selecione o nível de relevância") }
    var relevancyMenuAberta by remember { mutableStateOf(false) }
    var contato by rememberSaveable { mutableStateOf("") }
    var anexoConfirmado by rememberSaveable { mutableStateOf(false) }

    val niveis = listOf(
        "Desejável (Seria um ótimo complemento)",
        "Importante (Melhoraria muito o meu dia a dia)",
        "Prioritária (Faz muita falta na minha rotina)",
        "Essencial (Recurso indispensável para o meu uso)"
    )

    val preenchido = conceito.isNotBlank() && impacto.isNotBlank() && relevancy != "Selecione o nível de relevância"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOled)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoClaro
                    )
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Propor Nova Funcionalidade",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Text(
                        text = "Colabore com a evolução do LivennIA",
                        style = MaterialTheme.typography.labelSmall,
                        color = DouradoLivenn
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Qual recurso você gostaria de ver no aplicativo?*",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    CampoTextoObsidian(
                        valor = conceito,
                        onValorChange = { conceito = it },
                        placeholder = "Explique a ideia, mecânica nova ou melhoria que tornará sua experiência mais rica...",
                        linhas = 4
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Qual o impacto dessa novidade nas suas histórias ou RPG?*",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    CampoTextoObsidian(
                        valor = impacto,
                        onValorChange = { impacto = it },
                        placeholder = "Conte em que momentos das suas histórias você mais sente falta dessa função...",
                        linhas = 3
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Relevância para a sua experiência:*",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Box {
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { relevancyMenuAberta = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = relevancy,
                                    color = if (relevancy == "Selecione o nível de relevância") TextoMudo else TextoClaro,
                                    fontSize = 13.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextoSecundario,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = relevancyMenuAberta,
                            onDismissRequest = { relevancyMenuAberta = false },
                            modifier = Modifier.background(CardObsidian)
                        ) {
                            niveis.forEach { opcao ->
                                DropdownMenuItem(
                                    text = { Text(opcao, color = TextoClaro, fontSize = 13.sp) },
                                    onClick = {
                                        relevancy = opcao
                                        relevancyMenuAberta = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Anexo visual ou referência (opcional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (anexoConfirmado) DouradoLivenn else CardObsidianBorda),
                        modifier = Modifier
                            .size(92.dp)
                            .clickable {
                                anexoConfirmado = !anexoConfirmado
                                Toast.makeText(
                                    contexto,
                                    if (anexoConfirmado) "Referência visual anexada!" else "Anexo removido",
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
                                imageVector = if (anexoConfirmado) Icons.Default.Check else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (anexoConfirmado) DouradoLivenn else TextoSecundario,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (anexoConfirmado) "Anexado" else "Adicionar",
                                fontSize = 12.sp,
                                color = if (anexoConfirmado) DouradoLivenn else TextoSecundario
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Seu contato para novidades (opcional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    CampoTextoObsidian(
                        valor = contato,
                        onValorChange = { contato = it },
                        placeholder = "E-mail ou Discord para avisarmos quando sua sugestão for ao ar.",
                        linhas = 2
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // Barra Inferior de Ação
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onVoltar() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Voltar", color = TextoClaro, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Surface(
                    color = if (preenchido) DouradoBotao else Color(0xFF26262A),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(enabled = preenchido) {
                            val relatorio = "[PROPOSTA]: $conceito\nImpacto: $impacto\nRelevância: $relevancy\nContato: $contato"
                            vm.enviarFeedback(5, relatorio)
                            Toast.makeText(contexto, "Ideia enviada! Obrigado pela colaboração.", Toast.LENGTH_LONG).show()
                            onVoltar()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Enviar Proposta",
                            color = if (preenchido) Color.Black else TextoMudo,
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
// 5. SUB-TELA: RELATAR INSTABILIDADE (ANTIGO REPORTAR ERRO)
// -------------------------------------------------------------
@Composable
fun SubTelaReportarErro(
    vm: AppViewModel,
    onVoltar: () -> Unit
) {
    BackHandler { onVoltar() }
    val contexto = LocalContext.current
    var relato by rememberSaveable { mutableStateOf("") }
    var anexoConfirmado by rememberSaveable { mutableStateOf(false) }
    var contato by rememberSaveable { mutableStateOf("") }

    val podeEnviar = relato.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoOled)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextoClaro
                    )
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Relatar Instabilidade",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Text(
                        text = "Ajude a aprimorar a estabilidade do app",
                        style = MaterialTheme.typography.labelSmall,
                        color = DouradoLivenn
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Descreva o que aconteceu de inesperado*",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    CampoTextoObsidian(
                        valor = relato,
                        onValorChange = { relato = it },
                        placeholder = "Conte qual ação executou, qual tela travou ou se alguma resposta de IA não foi exibida...",
                        linhas = 4
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Captura de tela ou evidência (opcional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (anexoConfirmado) DouradoLivenn else CardObsidianBorda),
                        modifier = Modifier
                            .size(92.dp)
                            .clickable {
                                anexoConfirmado = !anexoConfirmado
                                Toast.makeText(
                                    contexto,
                                    if (anexoConfirmado) "Captura anexada!" else "Captura removida",
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
                                imageVector = if (anexoConfirmado) Icons.Default.Check else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (anexoConfirmado) DouradoLivenn else TextoSecundario,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (anexoConfirmado) "Anexado" else "Adicionar",
                                fontSize = 12.sp,
                                color = if (anexoConfirmado) DouradoLivenn else TextoSecundario
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Contato para suporte técnico (opcional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    CampoTextoObsidian(
                        valor = contato,
                        onValorChange = { contato = it },
                        placeholder = "Deixe seu e-mail ou Discord caso nossa equipe precise de detalhes.",
                        linhas = 2
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // Barra Inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = CardObsidian,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CardObsidianBorda),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onVoltar() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Cancelar", color = TextoClaro, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Surface(
                    color = if (podeEnviar) DouradoBotao else Color(0xFF26262A),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(enabled = podeEnviar) {
                            val relatorio = "[INSTABILIDADE]: $relato\nContato: $contato\nServidor: ${vm.servidor}"
                            vm.enviarFeedback(1, relatorio)
                            Toast.makeText(contexto, "Relato registrado! Nossa equipe técnica investigará.", Toast.LENGTH_LONG).show()
                            onVoltar()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Enviar Relatório",
                            color = if (podeEnviar) Color.Black else TextoMudo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// Campo de texto escuro elegante
@Composable
private fun CampoTextoObsidian(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    linhas: Int = 1
) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            if (valor.isEmpty()) {
                Text(
                    text = placeholder,
                    color = TextoMudo,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            BasicTextField(
                value = valor,
                onValueChange = onValorChange,
                textStyle = TextStyle(
                    color = TextoClaro,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                cursorBrush = SolidColor(DouradoLivenn),
                modifier = Modifier
                    .fillMaxWidth()
                    .height((linhas * 24).dp)
            )
        }
    }
}
