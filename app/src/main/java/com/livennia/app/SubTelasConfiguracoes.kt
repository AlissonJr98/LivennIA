package com.livennia.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
private val RoxoPlanejado = Color(0xFFBA68C8)

// -------------------------------------------------------------
// TELAS DE ENTRADA / ATALHOS
// -------------------------------------------------------------
@Composable
fun FeedbackTela(vm: AppViewModel) {
    SubTelaPesquisa(
        vm = vm,
        onVoltar = { vm.navegarPara(Tela.Personagens) },
        onNovaSugestao = {}
    )
}

// -------------------------------------------------------------
// 1. SUB-TELA: DIAGNÓSTICO DE CONEXÃO & LATÊNCIA
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
    var dialogoEditarServidor by remember { mutableStateOf(false) }

    fun executarDiagnostico() {
        if (testando) return
        testando = true
        statusConexao = "Consultando telemetria e medindo tempo de resposta do servidor..."
        escopo.launch {
            val inicio = System.currentTimeMillis()
            var okNet = false
            var okServ = false
            withContext(Dispatchers.IO) {
                runCatching {
                    val address = InetAddress.getByName("8.8.8.8")
                    okNet = address.isReachable(3000)
                }.onFailure { okNet = true }

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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌐 Servidor: ${vm.servidor}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextoMudo,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { dialogoEditarServidor = true }) {
                                    Text("Alterar", color = DouradoLivenn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

        if (dialogoEditarServidor) {
            var novoUrl by remember { mutableStateOf(vm.servidor) }
            AlertDialog(
                onDismissRequest = { dialogoEditarServidor = false },
                title = { Text("Configurar Servidor", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Informe a URL ou IP do servidor LivennIA:", color = TextoSecundario, fontSize = 13.sp)
                        OutlinedTextField(
                            value = novoUrl,
                            onValueChange = { novoUrl = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.alterarServidor(novoUrl.trim())
                            dialogoEditarServidor = false
                            executarDiagnostico()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DouradoBotao)
                    ) {
                        Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialogoEditarServidor = false }) {
                        Text("Cancelar", color = TextoSecundario)
                    }
                }
            )
        }

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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
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
// 3. FÓRUM INTERATIVO DA COMUNIDADE & FEEDBACK
// -------------------------------------------------------------
data class ComentarioItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var autor: String,
    val data: String,
    var texto: String,
    val ehMeu: Boolean = false
)

data class PostSugestaoItem(
    val id: String,
    var titulo: String,
    var descricao: String,
    var votos: Int = 1,
    var votado: Boolean = false,
    var likes: Int = 0,
    var curtiu: Boolean = false,
    var amei: Int = 0,
    var amou: Boolean = false,
    var fogo: Int = 0,
    var fogou: Boolean = false,
    var status: String = "💡 Em Avaliação",
    var categoria: String = "Novos Recursos",
    val data: String = "Hoje",
    var autor: String = "Criador",
    val ehMeu: Boolean = false,
    val comentarios: MutableList<ComentarioItem> = mutableListOf()
)

@Composable
fun SubTelaPesquisa(
    vm: AppViewModel,
    onVoltar: () -> Unit,
    onNovaSugestao: () -> Unit = {}
) {
    BackHandler { onVoltar() }
    val contexto = LocalContext.current
    val meuApelido = vm.apelido.ifBlank { "Criador" }

    var abaSelecionada by rememberSaveable { mutableStateOf("Ideias") }
    var filtroSelecionado by rememberSaveable { mutableStateOf("Destaque") }
    var buscaQuery by rememberSaveable { mutableStateOf("") }
    var categoriaFiltro by rememberSaveable { mutableStateOf("Todas") }
    var menuCategoriaAberto by remember { mutableStateOf(false) }

    // Diálogos de interação
    var postSelecionado by remember { mutableStateOf<PostSugestaoItem?>(null) }
    var modalNovaIdeia by remember { mutableStateOf(false) }
    var postEmEdicao by remember { mutableStateOf<PostSugestaoItem?>(null) }
    var postParaExcluir by remember { mutableStateOf<PostSugestaoItem?>(null) }
    var comentarioEmEdicao by remember { mutableStateOf<Pair<PostSugestaoItem, ComentarioItem>?>(null) }

    // Começa vazio, conforme solicitado para a comunidade ir preenchendo
    val posts = remember { mutableStateListOf<PostSugestaoItem>() }

    // Itens filtrados e ordenados em ordem
    val postsFiltrados = posts.filter { p ->
        val combinaBusca = buscaQuery.isBlank() ||
                p.titulo.contains(buscaQuery, ignoreCase = true) ||
                p.descricao.contains(buscaQuery, ignoreCase = true)
        val combinaCategoria = categoriaFiltro == "Todas" || p.categoria == categoriaFiltro
        combinaBusca && combinaCategoria
    }.let { list ->
        when (filtroSelecionado) {
            "Recentes" -> list.sortedByDescending { it.id.toLongOrNull() ?: 0L }
            "Amei" -> list.sortedByDescending { it.amei + it.likes }
            "Concluidas" -> list.filter { it.status.contains("Já no App") }
            else -> list.sortedByDescending { it.votos }
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
                        text = "Voz dos Criadores & Fórum de Ideias",
                        style = MaterialTheme.typography.labelSmall,
                        color = DouradoLivenn
                    )
                }
            }

            // Abas Superiores (com rolagem horizontal evitando quebra de linha de 'Atualizações')
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AbaPillLivenn(
                    titulo = "💡 Sugestões",
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

            // TAB 1: IDEIAS & SUGESTÕES (FÓRUM INTERATIVO)
            if (abaSelecionada == "Ideias") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Campo de Busca
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextoSecundario,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = buscaQuery,
                                onValueChange = { buscaQuery = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 13.sp),
                                cursorBrush = SolidColor(DouradoLivenn),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (buscaQuery.isEmpty()) {
                                        Text("Buscar propostas, mecânicas ou bugs...", color = TextoMudo, fontSize = 13.sp)
                                    }
                                    innerTextField()
                                }
                            )
                            if (buscaQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpar",
                                    tint = TextoSecundario,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { buscaQuery = "" }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Linha com Botão '+ Propor Ideia' e Filtro de Categoria
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = TextoClaro,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable { modalNovaIdeia = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Propor Ideia",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Seletor de Categoria
                        Box {
                            Surface(
                                color = CardObsidian,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CardObsidianBorda),
                                modifier = Modifier.clickable { menuCategoriaAberto = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.FilterList, contentDescription = null, tint = DouradoLivenn, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(categoriaFiltro, color = TextoClaro, fontSize = 11.sp)
                                }
                            }

                            DropdownMenu(
                                expanded = menuCategoriaAberto,
                                onDismissRequest = { menuCategoriaAberto = false },
                                modifier = Modifier.background(CardObsidian)
                            ) {
                                listOf("Todas", "Melhorias de IA", "Novos Recursos", "Bugs & Ajustes").forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = TextoClaro, fontSize = 12.sp) },
                                        onClick = {
                                            categoriaFiltro = cat
                                            menuCategoriaAberto = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Filtros rápidos em ordem
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FiltroPillLivenn(
                            titulo = "🔥 Mais Votados",
                            selecionado = filtroSelecionado == "Destaque",
                            aoClicar = { filtroSelecionado = "Destaque" }
                        )
                        FiltroPillLivenn(
                            titulo = "⚡ Recentes",
                            selecionado = filtroSelecionado == "Recentes",
                            aoClicar = { filtroSelecionado = "Recentes" }
                        )
                        FiltroPillLivenn(
                            titulo = "❤️ Mais Amados",
                            selecionado = filtroSelecionado == "Amei",
                            aoClicar = { filtroSelecionado = "Amei" }
                        )
                        FiltroPillLivenn(
                            titulo = "✨ Já no App",
                            selecionado = filtroSelecionado == "Concluidas",
                            aoClicar = { filtroSelecionado = "Concluidas" }
                        )
                    }
                }

                // Lista de Propostas ou Estado Vazio quando não há nada ainda
                if (postsFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = CardObsidian,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CardObsidianBorda),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(DouradoLivenn.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💡", fontSize = 28.sp)
                                }
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = if (buscaQuery.isNotEmpty()) "Nenhuma proposta encontrada" else "Nenhuma proposta ainda",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoClaro
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (buscaQuery.isNotEmpty())
                                        "Tente buscar por outras palavras ou limpe os filtros."
                                    else
                                        "A central da comunidade está pronta! Seja o primeiro a propor uma ideia ou sugestão de melhoria para o LivennIA.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSecundario,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { modalNovaIdeia = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = DouradoBotao),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Propor Primeira Ideia", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(postsFiltrados, key = { it.id }) { post ->
                            CardPostLivenn(
                                item = post,
                                meuApelido = meuApelido,
                                onVotar = {
                                    post.votado = !post.votado
                                    if (post.votado) post.votos++ else post.votos--
                                },
                                onAmei = {
                                    post.amou = !post.amou
                                    if (post.amou) post.amei++ else post.amei--
                                },
                                onLike = {
                                    post.curtiu = !post.curtiu
                                    if (post.curtiu) post.likes++ else post.likes--
                                },
                                onFogo = {
                                    post.fogou = !post.fogou
                                    if (post.fogou) post.fogo++ else post.fogo--
                                },
                                onEditar = { postEmEdicao = post },
                                onExcluir = { postParaExcluir = post },
                                aoClicar = { postSelecionado = post }
                            )
                        }
                        item {
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            } else if (abaSelecionada == "Roadmap") {
                // TAB 2: ROADMAP ESTILO KANBAN COM VOTAÇÃO DEMOCRÁTICA
                val itensPlanejados = remember {
                    mutableStateListOf(
                        KanbanPlanejadoItem(
                            id = "p1",
                            titulo = "Diálogos em grupo com múltiplos avatares simultâneos",
                            descricao = "Salas interativas com até 4 personagens de IA conversando e reagindo entre si às suas ações.",
                            previsao = "Dez 2026"
                        ),
                        KanbanPlanejadoItem(
                            id = "p2",
                            titulo = "Exportação de aventuras em Livro Digital (PDF com ilustrações)",
                            descricao = "Gere um livro ilustrado com a capa, história e decisões da sua campanha para guardar ou imprimir.",
                            previsao = "Jan 2027"
                        ),
                        KanbanPlanejadoItem(
                            id = "p3",
                            titulo = "Criador de mundos e regras de campanha customizadas",
                            descricao = "Defina suas próprias fichas, sistemas de dados (d20) e leis do universo para suas campanhas.",
                            previsao = "Fev 2027"
                        ),
                        KanbanPlanejadoItem(
                            id = "p4",
                            titulo = "Mapas táticos interativos e exploração de masmorras",
                            descricao = "Visualização de salas, tesouros e encontros desenhados dinamicamente pela IA.",
                            previsao = "Mar 2027"
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Rota Democrática do LivennIA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro
                    )
                    Text(
                        text = "Você decide o futuro do app! No Planejado, vote se quer ou não o recurso. Os votos começam zerados para a comunidade decidir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )

                    // Coluna 1: Planejado (Votação Democrática)
                    ColunaRoadmapPlanejadoDemocratico(
                        itens = itensPlanejados,
                        onVotar = { item, quero ->
                            if (quero) {
                                if (item.meuVoto == "QUERO") {
                                    item.meuVoto = null
                                    item.votosQuero--
                                } else {
                                    if (item.meuVoto == "NAO_QUERO") item.votosNaoQuero--
                                    item.meuVoto = "QUERO"
                                    item.votosQuero++
                                }
                            } else {
                                if (item.meuVoto == "NAO_QUERO") {
                                    item.meuVoto = null
                                    item.votosNaoQuero--
                                } else {
                                    if (item.meuVoto == "QUERO") item.votosQuero--
                                    item.meuVoto = "NAO_QUERO"
                                    item.votosNaoQuero++
                                }
                            }
                        }
                    )

                    // Coluna 2: Em Desenvolvimento (Registros zerados)
                    ColunaRoadmapKanban(
                        titulo = "Em Desenvolvimento",
                        iconeCor = DouradoLivenn,
                        itens = listOf(
                            KanbanItem("Alternador de tom narrativo sem restrições no chat", "0 votos", "Em testes"),
                            KanbanItem("Vozes e sotaques personalizados para cada NPC", "0 votos", "Sprint atual"),
                            KanbanItem("Memória de longo prazo nas histórias de campanha", "0 votos", "Otimização de tokens")
                        )
                    )

                    // Coluna 3: Concluído Recentemente (Registros zerados)
                    ColunaRoadmapKanban(
                        titulo = "Já no App!",
                        iconeCor = VerdeEstavel,
                        itens = listOf(
                            KanbanItem("Central de Comunidade com Reações e Fórum", "0 votos", "v1.4.1"),
                            KanbanItem("Tela de Perfil com Estatísticas e Planos VIP", "0 votos", "v1.4.0"),
                            KanbanItem("Rolagem fluida do chat com margem inferior", "0 votos", "v1.3.4"),
                            KanbanItem("Modelos de IA diferenciados com estilos reais", "0 votos", "v1.3.4")
                        )
                    )
                }
            } else {
                // TAB 3: CHANGELOG COM NOTAS COMPLETAS E BARRA DE REAÇÕES
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CardChangelogCompleto(
                        versao = "LivennIA v1.4.0 — Perfil Moderno, Fórum & Central da Comunidade!",
                        data = "Hoje • Outubro 2026",
                        tags = listOf("● Novo Recurso" to VerdeEstavel, "● Melhorias" to DouradoLivenn, "● Correções" to Color(0xFF64B5F6)),
                        descricao = "Olá criadores! Lançamos a versão v1.4.0 do LivennIA, trazendo a nova Tela de Perfil e o Fórum Interativo de Feedback para construção comunitária do app.",
                        itens = listOf(
                            "1. Fórum Interativo da Comunidade: Envie propostas, vote, reaja com amei e like e acompanhe o Roadmap em tempo real.",
                            "2. Gestão do Autor: Edite ou apague suas próprias propostas e comentários a qualquer momento.",
                            "3. Tela de Perfil Redesenhada: Status de chats, nível, energia, planos VIP e caixa surpresa de aventuras.",
                            "4. Rolagem Fluida e Navegação Direta: O app abre no Explorar e acompanha o texto da IA sem cortes."
                        )
                    )

                    CardChangelogCompleto(
                        versao = "LivennIA v1.3.4 — Modelos de IA e Diagnóstico de Rede",
                        data = "Outubro 2026",
                        tags = listOf("● Novo Recurso" to VerdeEstavel, "● Melhorias" to DouradoLivenn),
                        descricao = "Adição de novos modelos narrativos de IA e diagnóstico em tempo real de conexão e latência.",
                        itens = listOf(
                            "1. Seletor de Modelos de IA: Modelos Grátis com Anúncio, Modo Energia e VIP Prime.",
                            "2. Diagnóstico de Rede: Verificação de DNS, IP, latência e conectividade com o servidor."
                        )
                    )
                }
            }
        }
    }

    // MODAL: VISUALIZAR DISCUSSÃO E COMENTÁRIOS DO POST
    if (postSelecionado != null) {
        val post = postSelecionado!!
        var novoComentario by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { postSelecionado = null },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DouradoLivenn
                        )
                        Text(post.data, color = TextoMudo, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(post.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoClaro)
                    Text("Por ${post.autor}", style = MaterialTheme.typography.bodySmall, color = DouradoLivenn.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(post.descricao, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)

                    // Barra de Reações dentro da discussão
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChipReacaoPequeno(emoji = "❤️", contador = post.amei, ativo = post.amou, corAtiva = Color(0xFFFF4081)) {
                            post.amou = !post.amou
                            if (post.amou) post.amei++ else post.amei--
                        }
                        ChipReacaoPequeno(emoji = "👍", contador = post.likes, ativo = post.curtiu, corAtiva = DouradoLivenn) {
                            post.curtiu = !post.curtiu
                            if (post.curtiu) post.likes++ else post.likes--
                        }
                        ChipReacaoPequeno(emoji = "🔥", contador = post.fogo, ativo = post.fogou, corAtiva = Color(0xFFFFB74D)) {
                            post.fogou = !post.fogou
                            if (post.fogou) post.fogo++ else post.fogo--
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    Text(
                        text = "Comentários (${post.comentarios.size})",
                        fontWeight = FontWeight.Bold,
                        color = TextoClaro,
                        fontSize = 13.sp
                    )

                    if (post.comentarios.isEmpty()) {
                        Text(
                            "Nenhum comentário ainda. Deixe sua opinião sobre esta ideia!",
                            color = TextoMudo,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        post.comentarios.forEach { c ->
                            val ehDonoComentario = c.ehMeu || c.autor == meuApelido

                            Surface(
                                color = Color.White.copy(alpha = 0.03f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(c.autor, fontWeight = FontWeight.Bold, color = DouradoLivenn, fontSize = 11.sp)
                                            if (ehDonoComentario) {
                                                Spacer(Modifier.width(4.dp))
                                                Text("(Você)", color = TextoMudo, fontSize = 9.sp)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(c.data, color = TextoMudo, fontSize = 10.sp)

                                            // Opções do Dono do Comentário (Editar e Apagar)
                                            if (ehDonoComentario) {
                                                Spacer(Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Editar Comentário",
                                                    tint = DouradoLivenn,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            comentarioEmEdicao = Pair(post, c)
                                                        }
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Apagar Comentário",
                                                    tint = VermelhoSuave,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            post.comentarios.remove(c)
                                                            Toast.makeText(contexto, "Comentário apagado!", Toast.LENGTH_SHORT).show()
                                                        }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(c.texto, color = TextoClaro, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Caixa de Envio de Novo Comentário
                    Surface(
                        color = CardObsidian,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardObsidianBorda),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = novoComentario,
                                onValueChange = { novoComentario = it },
                                textStyle = TextStyle(color = TextoClaro, fontSize = 12.sp),
                                cursorBrush = SolidColor(DouradoLivenn),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (novoComentario.isEmpty()) {
                                        Text("Escreva sua opinião...", color = TextoMudo, fontSize = 12.sp)
                                    }
                                    innerTextField()
                                }
                            )

                            IconButton(
                                onClick = {
                                    if (novoComentario.isNotBlank()) {
                                        post.comentarios.add(
                                            ComentarioItem(
                                                autor = meuApelido,
                                                data = "Hoje",
                                                texto = novoComentario.trim(),
                                                ehMeu = true
                                            )
                                        )
                                        novoComentario = ""
                                        Toast.makeText(contexto, "Comentário adicionado!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = novoComentario.isNotBlank(),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Enviar",
                                    tint = if (novoComentario.isNotBlank()) DouradoLivenn else TextoMudo,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { postSelecionado = null }) {
                    Text("Fechar", color = DouradoLivenn)
                }
            },
            containerColor = CardObsidian
        )
    }

    // MODAL: PROPOR NOVA IDEIA NO FÓRUM
    if (modalNovaIdeia) {
        var tituloIdeia by remember { mutableStateOf("") }
        var descricaoIdeia by remember { mutableStateOf("") }
        var categoriaSelecionada by remember { mutableStateOf("Novos Recursos") }

        AlertDialog(
            onDismissRequest = { modalNovaIdeia = false },
            title = {
                Text("Propor Ideia para a Comunidade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoClaro)
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Qual novidade você quer sugerir?", color = TextoSecundario, fontSize = 12.sp)
                    CampoTextoObsidian(
                        valor = tituloIdeia,
                        onValorChange = { tituloIdeia = it },
                        placeholder = "Ex.: Histórias cooperativas com amigos...",
                        linhas = 1
                    )

                    Text("Detalhes e como deve funcionar:", color = TextoSecundario, fontSize = 12.sp)
                    CampoTextoObsidian(
                        valor = descricaoIdeia,
                        onValorChange = { descricaoIdeia = it },
                        placeholder = "Explique a dinâmica ou recurso...",
                        linhas = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tituloIdeia.isNotBlank()) {
                            posts.add(
                                0,
                                PostSugestaoItem(
                                    id = System.currentTimeMillis().toString(),
                                    titulo = tituloIdeia.trim(),
                                    descricao = descricaoIdeia.trim().ifBlank { "Proposta enviada pela comunidade para avaliação." },
                                    votos = 1,
                                    votado = true,
                                    status = "💡 Em Avaliação",
                                    categoria = categoriaSelecionada,
                                    data = "Hoje",
                                    autor = meuApelido,
                                    ehMeu = true,
                                    comentarios = mutableListOf()
                                )
                            )
                            modalNovaIdeia = false
                            Toast.makeText(contexto, "Ideia publicada no fórum!", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = tituloIdeia.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DouradoBotao)
                ) {
                    Text("Publicar Ideia", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { modalNovaIdeia = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            containerColor = CardObsidian
        )
    }

    // MODAL: EDITAR POST (DONO)
    if (postEmEdicao != null) {
        val post = postEmEdicao!!
        var tituloEditado by remember(post.id) { mutableStateOf(post.titulo) }
        var descEditada by remember(post.id) { mutableStateOf(post.descricao) }

        AlertDialog(
            onDismissRequest = { postEmEdicao = null },
            title = {
                Text("Editar Proposta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoClaro)
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Título da proposta:", color = TextoSecundario, fontSize = 12.sp)
                    CampoTextoObsidian(
                        valor = tituloEditado,
                        onValorChange = { tituloEditado = it },
                        placeholder = "Título...",
                        linhas = 1
                    )

                    Text("Descrição detalhada:", color = TextoSecundario, fontSize = 12.sp)
                    CampoTextoObsidian(
                        valor = descEditada,
                        onValorChange = { descEditada = it },
                        placeholder = "Descrição...",
                        linhas = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tituloEditado.isNotBlank()) {
                            post.titulo = tituloEditado.trim()
                            post.descricao = descEditada.trim()
                            postEmEdicao = null
                            Toast.makeText(contexto, "Proposta atualizada!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = tituloEditado.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DouradoBotao)
                ) {
                    Text("Salvar Alterações", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { postEmEdicao = null }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            containerColor = CardObsidian
        )
    }

    // MODAL: CONFIRMAR EXCLUSÃO DO POST (DONO)
    if (postParaExcluir != null) {
        val post = postParaExcluir!!
        AlertDialog(
            onDismissRequest = { postParaExcluir = null },
            title = {
                Text("Excluir Proposta?", color = TextoClaro, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Deseja realmente apagar '${post.titulo}'? Todas as reações e comentários também serão removidos.",
                    color = TextoSecundario,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        posts.remove(post)
                        if (postSelecionado?.id == post.id) {
                            postSelecionado = null
                        }
                        postParaExcluir = null
                        Toast.makeText(contexto, "Proposta excluída com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VermelhoSuave)
                ) {
                    Text("Excluir", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { postParaExcluir = null }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            containerColor = CardObsidian
        )
    }

    // MODAL: EDITAR COMENTÁRIO (DONO)
    if (comentarioEmEdicao != null) {
        val (_, cAlvo) = comentarioEmEdicao!!
        var textoEditado by remember(cAlvo.id) { mutableStateOf(cAlvo.texto) }

        AlertDialog(
            onDismissRequest = { comentarioEmEdicao = null },
            title = {
                Text("Editar Comentário", color = TextoClaro, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                CampoTextoObsidian(
                    valor = textoEditado,
                    onValorChange = { textoEditado = it },
                    placeholder = "Edite sua resposta...",
                    linhas = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textoEditado.isNotBlank()) {
                            cAlvo.texto = textoEditado.trim()
                            comentarioEmEdicao = null
                            Toast.makeText(contexto, "Comentário atualizado!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DouradoBotao)
                ) {
                    Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { comentarioEmEdicao = null }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            containerColor = CardObsidian
        )
    }
}

@Composable
private fun CardPostLivenn(
    item: PostSugestaoItem,
    meuApelido: String,
    onVotar: () -> Unit,
    onAmei: () -> Unit,
    onLike: () -> Unit,
    onFogo: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    aoClicar: () -> Unit
) {
    val ehDono = item.ehMeu || item.autor == meuApelido

    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { aoClicar() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Botão de Apoio / Voto
                Surface(
                    color = if (item.votado) DouradoLivenn.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (item.votado) DouradoLivenn else Color.White.copy(alpha = 0.08f)
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

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.titulo,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextoClaro,
                            modifier = Modifier.weight(1f)
                        )

                        // Ações do dono da proposta (Editar e Apagar)
                        if (ehDono) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = onEditar,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = DouradoLivenn,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onExcluir,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Apagar",
                                        tint = VermelhoSuave,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.descricao,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        Text("Por ${item.autor}", color = TextoMudo, fontSize = 10.sp, maxLines = 1)
                        Text("•", color = TextoMudo, fontSize = 10.sp)
                        Text(item.data, color = TextoMudo, fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
            Spacer(Modifier.height(8.dp))

            // Barra de Interações: Amei ❤️, Like 👍, Fogo 🔥 e Comentários 💬
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Botão Amei ❤️
                Surface(
                    color = if (item.amou) Color(0xFFE91E63).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (item.amou) Color(0xFFE91E63) else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.clickable { onAmei() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("❤️", fontSize = 11.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.amei.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.amou) Color(0xFFFF4081) else TextoSecundario
                        )
                    }
                }

                // Botão Like 👍
                Surface(
                    color = if (item.curtiu) DouradoLivenn.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (item.curtiu) DouradoLivenn else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👍", fontSize = 11.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.likes.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.curtiu) DouradoLivenn else TextoSecundario
                        )
                    }
                }

                // Botão Fogo 🔥
                Surface(
                    color = if (item.fogou) Color(0xFFFF9800).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (item.fogou) Color(0xFFFF9800) else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.clickable { onFogo() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 11.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.fogo.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.fogou) Color(0xFFFFB74D) else TextoSecundario
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Contador de comentários 💬
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { aoClicar() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = TextoMudo,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.comentarios.size.toString(),
                        color = TextoMudo,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipReacaoPequeno(
    emoji: String,
    contador: Int,
    ativo: Boolean,
    corAtiva: Color,
    aoClicar: () -> Unit
) {
    Surface(
        color = if (ativo) corAtiva.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (ativo) corAtiva else Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.clickable { aoClicar() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                text = contador.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (ativo) corAtiva else TextoSecundario
            )
        }
    }
}

data class KanbanItem(val titulo: String, val votos: String, val data: String)

data class KanbanPlanejadoItem(
    val id: String,
    val titulo: String,
    val descricao: String,
    val previsao: String,
    var votosQuero: Int = 0,
    var votosNaoQuero: Int = 0,
    var meuVoto: String? = null
)

@Composable
private fun ColunaRoadmapPlanejadoDemocratico(
    itens: List<KanbanPlanejadoItem>,
    onVotar: (KanbanPlanejadoItem, Boolean) -> Unit
) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(RoxoPlanejado, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("🎯 Planejado (Votação da Comunidade)", fontWeight = FontWeight.Bold, color = TextoClaro, fontSize = 14.sp)
                }
                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        itens.size.toString(),
                        color = TextoSecundario,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Vote nas propostas abaixo para definir a prioridade da equipe:",
                color = TextoMudo,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(10.dp))

            itens.forEach { item ->
                val totalVotos = item.votosQuero + item.votosNaoQuero
                val pctAprovacao = if (totalVotos > 0) ((item.votosQuero.toFloat() / totalVotos) * 100).toInt() else 0

                Surface(
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.titulo,
                                color = TextoClaro,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = RoxoPlanejado.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.previsao,
                                    color = Color(0xFFD1C4E9),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.descricao,
                            color = TextoSecundario,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                        Spacer(Modifier.height(8.dp))

                        // Status da votação democrática
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (totalVotos == 0) "Aguardando votos da comunidade" else "$pctAprovacao% querem essa novidade ($totalVotos votos)",
                                color = if (totalVotos == 0) TextoMudo else DouradoLivenn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // Botões de Votação Democrática: Quero 👍 / Não vejo necessidade 👎
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val votouQuero = item.meuVoto == "QUERO"
                            Surface(
                                color = if (votouQuero) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (votouQuero) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onVotar(item, true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("👍", fontSize = 12.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Quero (${item.votosQuero})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (votouQuero) Color(0xFF81C784) else TextoClaro
                                    )
                                }
                            }

                            val votouNaoQuero = item.meuVoto == "NAO_QUERO"
                            Surface(
                                color = if (votouNaoQuero) Color(0xFFFF5252).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (votouNaoQuero) Color(0xFFFF5252) else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onVotar(item, false) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("👎", fontSize = 12.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Dispensar (${item.votosNaoQuero})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (votouNaoQuero) Color(0xFFFF8A80) else TextoSecundario
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

@Composable
private fun ColunaRoadmapKanban(
    titulo: String,
    iconeCor: Color,
    itens: List<KanbanItem>
) {
    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(iconeCor, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(titulo, fontWeight = FontWeight.Bold, color = TextoClaro, fontSize = 14.sp)
                }
                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        itens.size.toString(),
                        color = TextoSecundario,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            itens.forEach { item ->
                Surface(
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(item.titulo, color = TextoClaro, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.data, color = TextoMudo, fontSize = 11.sp)
                            Text("▲ ${item.votos}", color = DouradoLivenn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardChangelogCompleto(
    versao: String,
    data: String,
    tags: List<Pair<String, Color>>,
    descricao: String,
    itens: List<String>
) {
    var reacaoCoracao by remember { mutableIntStateOf(438) }
    var reacaoFogo by remember { mutableIntStateOf(212) }
    var reacaoJoinha by remember { mutableIntStateOf(185) }
    var reacaoFoguete by remember { mutableIntStateOf(106) }
    var reacaoFesta by remember { mutableIntStateOf(102) }

    Surface(
        color = CardObsidian,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardObsidianBorda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(data, color = TextoMudo, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(versao, fontWeight = FontWeight.Bold, color = TextoClaro, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { (tag, cor) ->
                    Surface(
                        color = cor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(tag, color = cor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(descricao, color = TextoSecundario, fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(8.dp))

            itens.forEach { item ->
                Text(item, color = TextoClaro, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(vertical = 2.dp))
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(Modifier.height(10.dp))

            // Barra de Reações Interativa (Matching Imagem 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChipReacaoEmoji("❤️", reacaoCoracao) { reacaoCoracao++ }
                ChipReacaoEmoji("🔥", reacaoFogo) { reacaoFogo++ }
                ChipReacaoEmoji("👍", reacaoJoinha) { reacaoJoinha++ }
                ChipReacaoEmoji("🚀", reacaoFoguete) { reacaoFoguete++ }
                ChipReacaoEmoji("🎉", reacaoFesta) { reacaoFesta++ }
            }
        }
    }
}

@Composable
private fun ChipReacaoEmoji(emoji: String, contador: Int, aoClicar: () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.clickable { aoClicar() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Text(contador.toString(), fontSize = 11.sp, color = TextoSecundario, fontWeight = FontWeight.SemiBold)
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
            maxLines = 1,
            softWrap = false,
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
