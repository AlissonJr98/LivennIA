package com.livennia.app

import androidx.activity.compose.BackHandler

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


enum class SubTelaAtiva {
    DiagnosticoRede,
    Sobre,
    Pesquisa,
    SolicitarRecurso,
    ReportarErro
}

private enum class DialogoConfig {
    PreferenciasConteudo,
    Idioma,
    Bloqueados,
    TamanhoFonte,
    ModoLeitura,
    ReportarErro,
    SolicitarRecurso,
    Pesquisa,
    Comunidade,
    SairConta,
    Sobre,
    DiagnosticoRede,
    Servidor,
    ApagarConta
}

@Composable
fun ConfiguracoesTela(vm: AppViewModel) {
    val contexto = LocalContext.current
    var dialogoAberto by remember { mutableStateOf<DialogoConfig?>(null) }
    var subTelaAtiva by rememberSaveable { mutableStateOf<SubTelaAtiva?>(null) }

    val telaAtiva = subTelaAtiva
    if (telaAtiva != null) {
        when (telaAtiva) {
            SubTelaAtiva.DiagnosticoRede -> SubTelaDiagnosticoRede(
                vm = vm,
                onVoltar = { subTelaAtiva = null }
            )
            SubTelaAtiva.Sobre -> SubTelaSobre(
                vm = vm,
                onVoltar = { subTelaAtiva = null },
                onAbrirDiagnostico = { subTelaAtiva = SubTelaAtiva.DiagnosticoRede },
                onApagarConta = { dialogoAberto = DialogoConfig.ApagarConta }
            )
            SubTelaAtiva.Pesquisa -> SubTelaPesquisa(
                vm = vm,
                onVoltar = { subTelaAtiva = null },
                onNovaSugestao = { subTelaAtiva = SubTelaAtiva.SolicitarRecurso }
            )
            SubTelaAtiva.SolicitarRecurso -> SubTelaSolicitarRecurso(
                vm = vm,
                onVoltar = { subTelaAtiva = null }
            )
            SubTelaAtiva.ReportarErro -> SubTelaReportarErro(
                vm = vm,
                onVoltar = { subTelaAtiva = null }
            )
        }
        return
    }

    BackHandler { vm.navegarPara(Tela.Personagens) }

    // Estados persistidos / locais dos switches
    var notificacoesAtivas by rememberSaveable { mutableStateOf(false) }
    var vibracaoAtiva by rememberSaveable { mutableStateOf(true) }
    var vozAutomatica by rememberSaveable { mutableStateOf(false) }

    val corFundo = Color(0xFF000000)
    val corCard = Color(0xFF161618)
    val corSecaoTitulo = Color(0xFF8E8E93)
    val corChevron = Color(0xFF5E5E62)
    val corAmareloSwitch = Color(0xFFF5C842)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(corFundo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // ---- Topo com seta de voltar e título "Configurações" ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.navegarPara(Tela.Personagens) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Configurações",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // ---- Lista de seções estilo Emochi ----
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Seção 1: Personalização
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Personalização",
                        style = MaterialTheme.typography.labelMedium,
                        color = corSecaoTitulo,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Surface(
                        color = corCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ItemConfigLinha(
                                titulo = "Preferências de conteúdo",
                                aoClicar = { dialogoAberto = DialogoConfig.PreferenciasConteudo }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Idioma(Usar o idioma do sistema)",
                                aoClicar = { dialogoAberto = DialogoConfig.Idioma }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Lista de bloqueados",
                                aoClicar = { dialogoAberto = DialogoConfig.Bloqueados }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Tamanho da letra do narrador (${vm.tamanhoLetraNarrador.toInt()})",
                                aoClicar = { dialogoAberto = DialogoConfig.TamanhoFonte }
                            )
                        }
                    }
                }

                // Seção 2: Notificações
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Notificações",
                        style = MaterialTheme.typography.labelMedium,
                        color = corSecaoTitulo,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Surface(
                        color = corCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ItemConfigSwitchLinha(
                                titulo = "Ativar notificações",
                                checked = notificacoesAtivas,
                                corAtiva = corAmareloSwitch,
                                onCheckedChange = { notificacoesAtivas = it }
                            )
                            DivisorConfig()
                            ItemConfigSwitchLinha(
                                titulo = "Vibração de nova mensagem",
                                checked = vibracaoAtiva,
                                corAtiva = corAmareloSwitch,
                                onCheckedChange = { vibracaoAtiva = it }
                            )
                        }
                    }
                }

                // Seção 3: Recursos de chat
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Recursos de chat",
                        style = MaterialTheme.typography.labelMedium,
                        color = corSecaoTitulo,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Surface(
                        color = corCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ItemConfigSwitchLinha(
                                titulo = "Reprodução de voz automática",
                                checked = vozAutomatica,
                                corAtiva = corAmareloSwitch,
                                onCheckedChange = { vozAutomatica = it }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Modo de leitura (${if (vm.modoLeitura == AppViewModel.ModoLeitura.HISTORIA) "História" else "RPG"})",
                                aoClicar = { dialogoAberto = DialogoConfig.ModoLeitura }
                            )
                        }
                    }
                }

                // Seção 4: Feedback
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Feedback",
                        style = MaterialTheme.typography.labelMedium,
                        color = corSecaoTitulo,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Surface(
                        color = corCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ItemConfigLinha(
                                titulo = "Reportar erro",
                                aoClicar = { subTelaAtiva = SubTelaAtiva.ReportarErro }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Solicitar recurso",
                                aoClicar = { subTelaAtiva = SubTelaAtiva.SolicitarRecurso }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Pesquisa",
                                aoClicar = { subTelaAtiva = SubTelaAtiva.Pesquisa }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Entrar no Discord",
                                aoClicar = { dialogoAberto = DialogoConfig.Comunidade }
                            )
                        }
                    }
                }

                // Seção 5: Minha conta
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Minha conta",
                        style = MaterialTheme.typography.labelMedium,
                        color = corSecaoTitulo,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Surface(
                        color = corCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ItemConfigLinha(
                                titulo = "Sair da conta",
                                aoClicar = { dialogoAberto = DialogoConfig.SairConta }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Sobre",
                                aoClicar = { subTelaAtiva = SubTelaAtiva.Sobre }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Diagnóstico de rede",
                                aoClicar = { subTelaAtiva = SubTelaAtiva.DiagnosticoRede }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Endereço do servidor",
                                subtexto = vm.servidor,
                                aoClicar = { dialogoAberto = DialogoConfig.Servidor }
                            )
                            DivisorConfig()
                            ItemConfigLinha(
                                titulo = "Apagar conta",
                                corTitulo = MaterialTheme.colorScheme.error,
                                aoClicar = { dialogoAberto = DialogoConfig.ApagarConta }
                            )
                        }
                    }
                }

                // Rodapé com versão centralizada
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "v1.3.4",
                        style = MaterialTheme.typography.labelSmall,
                        color = corChevron
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ---- Diálogos das opções ----
    when (dialogoAberto) {
        DialogoConfig.PreferenciasConteudo -> {
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Preferências de Conteúdo") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Filtro de palavras sensíveis", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Bloqueia termos ofensivos localmente antes do envio",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSuave
                                )
                            }
                            Switch(
                                checked = vm.filtroPalavrasSensiveis,
                                onCheckedChange = { vm.definirFiltroPalavrasSensiveis(it) }
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        Text("Estilo Visual", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                color = if (vm.estiloTema == EstiloTema.NEON) Ouro.copy(alpha = 0.2f) else corCard,
                                shape = RoundedCornerShape(12.dp),
                                border = if (vm.estiloTema == EstiloTema.NEON) BorderStroke(1.dp, Ouro) else null,
                                modifier = Modifier.weight(1f).clickable { vm.definirEstiloTema(EstiloTema.NEON) }
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("Neon", fontWeight = FontWeight.Bold)
                                    Text("Preto e cores vibrantes", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                                }
                            }
                            Surface(
                                color = if (vm.estiloTema == EstiloTema.LIVRO) Ouro.copy(alpha = 0.2f) else corCard,
                                shape = RoundedCornerShape(12.dp),
                                border = if (vm.estiloTema == EstiloTema.LIVRO) BorderStroke(1.dp, Ouro) else null,
                                modifier = Modifier.weight(1f).clickable { vm.definirEstiloTema(EstiloTema.LIVRO) }
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("Livro", fontWeight = FontWeight.Bold)
                                    Text("Pergaminho e ouro", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("Concluído") }
                }
            )
        }

        DialogoConfig.Idioma -> {
            var idiomaSelecionado by remember { mutableStateOf("Sistema") }
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Idioma do Aplicativo") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Usar o idioma do sistema", "Português (Brasil)", "English (US)", "Español").forEach { opt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { idiomaSelecionado = opt }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = idiomaSelecionado == opt || (idiomaSelecionado == "Sistema" && opt.startsWith("Usar")),
                                    onClick = { idiomaSelecionado = opt }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(opt)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("Salvar") }
                }
            )
        }

        DialogoConfig.Bloqueados -> {
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Lista de Bloqueados") },
                text = {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            "Nenhum personagem ou usuário bloqueado no momento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoSuave
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Personagens silenciados não aparecem no seu feed do Explorar.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSuave
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("OK") }
                }
            )
        }

        DialogoConfig.TamanhoFonte -> {
            var tamanho by remember { mutableStateOf(vm.tamanhoLetraNarrador) }
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Tamanho da Letra do Narrador") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Tamanho atual: ${tamanho.toInt()} sp",
                            color = Ouro,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = tamanho,
                            onValueChange = {
                                tamanho = it
                                vm.definirTamanhoLetra(it)
                            },
                            valueRange = 13f..22f,
                            steps = 8
                        )
                        Surface(
                            color = corCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“A noite caiu sobre o vale e as estrelas guiaram nossos passos.”",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = tamanho.sp),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("Pronto") }
                }
            )
        }

        DialogoConfig.ModoLeitura -> {
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Modo de Leitura Padrão") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            color = if (vm.modoLeitura == AppViewModel.ModoLeitura.HISTORIA) Ouro.copy(alpha = 0.2f) else corCard,
                            shape = RoundedCornerShape(12.dp),
                            border = if (vm.modoLeitura == AppViewModel.ModoLeitura.HISTORIA) BorderStroke(1.dp, Ouro) else null,
                            modifier = Modifier.fillMaxWidth().clickable {
                                vm.definirModoLeitura(AppViewModel.ModoLeitura.HISTORIA)
                                dialogoAberto = null
                            }
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("📖 Modo História (Recomendado)", fontWeight = FontWeight.Bold)
                                Text("Leitura contínua em formato de livro e novela visual com diálogo fluido.", style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                            }
                        }
                        Surface(
                            color = if (vm.modoLeitura == AppViewModel.ModoLeitura.RPG) Ouro.copy(alpha = 0.2f) else corCard,
                            shape = RoundedCornerShape(12.dp),
                            border = if (vm.modoLeitura == AppViewModel.ModoLeitura.RPG) BorderStroke(1.dp, Ouro) else null,
                            modifier = Modifier.fillMaxWidth().clickable {
                                vm.definirModoLeitura(AppViewModel.ModoLeitura.RPG)
                                dialogoAberto = null
                            }
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("🎲 Modo RPG", fontWeight = FontWeight.Bold)
                                Text("Chat com balões, inventário detalhado, pontos de vida e escolhas numeradas.", style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("Fechar") }
                }
            )
        }

        DialogoConfig.ReportarErro -> {
            var relato by remember { mutableStateOf("") }
            var enviado by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Reportar Erro") },
                text = {
                    if (enviado) {
                        Text("Obrigado pelo relato! A equipe do LivennIA analisará a situação.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Descreva o que aconteceu de inesperado:")
                            OutlinedTextField(
                                value = relato,
                                onValueChange = { relato = it },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                placeholder = { Text("Ex.: a rolagem travou, resposta vazia...") }
                            )
                        }
                    }
                },
                confirmButton = {
                    if (enviado) {
                        TextButton(onClick = { dialogoAberto = null }) { Text("OK") }
                    } else {
                        Button(
                            onClick = { enviado = true },
                            enabled = relato.isNotBlank()
                        ) { Text("Enviar") }
                    }
                },
                dismissButton = {
                    if (!enviado) {
                        TextButton(onClick = { dialogoAberto = null }) { Text("Cancelar") }
                    }
                }
            )
        }

        DialogoConfig.SolicitarRecurso -> {
            var ideia by remember { mutableStateOf("") }
            var enviado by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Solicitar Recurso") },
                text = {
                    if (enviado) {
                        Text("Ideia registrada com sucesso! Ela entrará nas próximas atualizações.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Qual funcionalidade você gostaria de ver no LivennIA?")
                            OutlinedTextField(
                                value = ideia,
                                onValueChange = { ideia = it },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                placeholder = { Text("Ex.: criação de histórias em grupo, mais vozes...") }
                            )
                        }
                    }
                },
                confirmButton = {
                    if (enviado) {
                        TextButton(onClick = { dialogoAberto = null }) { Text("OK") }
                    } else {
                        Button(
                            onClick = { enviado = true },
                            enabled = ideia.isNotBlank()
                        ) { Text("Enviar Sugestão") }
                    }
                },
                dismissButton = {
                    if (!enviado) {
                        TextButton(onClick = { dialogoAberto = null }) { Text("Cancelar") }
                    }
                }
            )
        }

        DialogoConfig.Pesquisa -> {
            var nota by remember { mutableIntStateOf(5) }
            var feedback by remember { mutableStateOf("") }
            var avaliado by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Pesquisa de Satisfação") },
                text = {
                    if (avaliado) {
                        Text("Obrigado pela sua avaliação! Sua opinião ajuda o LivennIA a evoluir.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Como está sendo sua experiência com o aplicativo?")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                (1..5).forEach { i ->
                                    Text(
                                        text = if (i <= nota) "⭐" else "☆",
                                        fontSize = 28.sp,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clickable { nota = i }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = feedback,
                                onValueChange = { feedback = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Comentários (opcional)") }
                            )
                        }
                    }
                },
                confirmButton = {
                    if (avaliado) {
                        TextButton(onClick = { dialogoAberto = null }) { Text("Fechar") }
                    } else {
                        Button(onClick = { avaliado = true }) { Text("Enviar") }
                    }
                },
                dismissButton = {
                    if (!avaliado) {
                        TextButton(onClick = { dialogoAberto = null }) { Text("Depois") }
                    }
                }
            )
        }

        DialogoConfig.Comunidade -> {
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Comunidade Oficial") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Conecte-se com outros autores, mestres e criadores de histórias do LivennIA.")
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = corCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💬 Servidor Oficial da Comunidade LivennIA",
                                fontWeight = FontWeight.Bold,
                                color = Ouro,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("OK") }
                }
            )
        }

        DialogoConfig.SairConta -> {
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Sair da Conta") },
                text = { Text("Tem certeza de que deseja encerrar a sessão neste dispositivo?") },
                confirmButton = {
                    Button(
                        onClick = {
                            dialogoAberto = null
                            vm.sair()
                        }
                    ) { Text("Sair") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("Cancelar") }
                }
            )
        }

        DialogoConfig.Sobre -> {
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Sobre o LivennIA") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "LivennIA v1.3.4",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ouro
                        )
                        Text(
                            "Plataforma inteligente de histórias interativas, RPG de mesa e conexão com personagens imersivos com inteligência artificial.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        CardAtualizacaoGitHub(vm)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("Fechar") }
                }
            )
        }

        DialogoConfig.DiagnosticoRede -> {
            var testando by remember { mutableStateOf(true) }
            var latenciaMs by remember { mutableLongStateOf(-1L) }
            var statusConexao by remember { mutableStateOf("Testando conexão...") }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val inicio = System.currentTimeMillis()
                    try {
                        val base = vm.servidor.trimEnd('/')
                        val url = URL("$base/saude")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connectTimeout = 5000
                        conn.readTimeout = 5000
                        conn.requestMethod = "GET"
                        val codigo = conn.responseCode
                        val fim = System.currentTimeMillis()
                        latenciaMs = fim - inicio
                        statusConexao = if (codigo == 200) "Servidor Online • $latenciaMs ms" else "Resposta: HTTP $codigo"
                    } catch (e: Exception) {
                        latenciaMs = -1L
                        statusConexao = "Falha ao conectar: ${e.message ?: "Timeout"}"
                    } finally {
                        testando = false
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Diagnóstico de Rede") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Servidor configurado: ${vm.servidor}", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                        Surface(
                            color = corCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                if (testando) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Ouro)
                                        Spacer(Modifier.width(10.dp))
                                        Text("Medindo latência...", style = MaterialTheme.typography.bodyMedium)
                                    }
                                } else {
                                    Text(
                                        text = statusConexao,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (latenciaMs in 0..800) Color(0xFF81C784) else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("OK") }
                }
            )
        }

        DialogoConfig.Servidor -> {
            var urlServidor by remember { mutableStateOf(vm.servidor) }
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Endereço do Servidor") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Defina o endereço da API do LivennIA:")
                        OutlinedTextField(
                            value = urlServidor,
                            onValueChange = { urlServidor = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.alterarServidor(urlServidor.trim())
                            dialogoAberto = null
                        }
                    ) { Text("Salvar") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("Cancelar") }
                }
            )
        }

        DialogoConfig.ApagarConta -> {
            var confirmouTexto by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { dialogoAberto = null },
                title = { Text("Apagar Conta Definitivamente", color = MaterialTheme.colorScheme.error) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Esta ação é irreversível. Todos os seus dados, histórias e personagens salvos serão permanentemente excluídos.")
                        Spacer(Modifier.height(4.dp))
                        Text("Digite 'APAGAR' para confirmar:")
                        OutlinedTextField(
                            value = confirmouTexto,
                            onValueChange = { confirmouTexto = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            dialogoAberto = null
                            vm.apagarConta()
                        },
                        enabled = confirmouTexto.trim().equals("APAGAR", ignoreCase = true),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Excluir Minha Conta") }
                },
                dismissButton = {
                    TextButton(onClick = { dialogoAberto = null }) { Text("Cancelar") }
                }
            )
        }

        null -> Unit
    }
}

// Linha de configuração padrão com chevron à direita estilo Emochi
@Composable
private fun ItemConfigLinha(
    titulo: String,
    subtexto: String? = null,
    corTitulo: Color = Color.White,
    aoClicar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { aoClicar() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Normal,
                color = corTitulo
            )
            if (subtexto != null) {
                Text(
                    text = subtexto,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8E93)
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF5E5E62),
            modifier = Modifier.size(18.dp)
        )
    }
}

// Linha com Switch moderno estilo Emochi (fundo amarelo/ouro ativo)
@Composable
private fun ItemConfigSwitchLinha(
    titulo: String,
    checked: Boolean,
    corAtiva: Color = Color(0xFFF5C842),
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = corAtiva,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2C2C2E),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// Divisor fino discreto entre itens dentro do mesmo card
@Composable
private fun DivisorConfig() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.04f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
