package com.livennia.app

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfiguracoesTela(vm: AppViewModel) {
    var servidor by rememberSaveable { mutableStateOf(vm.servidor) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(GradienteTopo, Fundo)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Configurações", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))

            // ---- Estilo visual ----
            TituloSecao("Estilo visual")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        CartaoEstilo(
                    nome = "Neon",
                    descricao = (if (vm.temaEscuro) "Preto e " else "Claro e ") + vm.corNeon.rotulo.lowercase(),
                    fundo = if (vm.temaEscuro) Color(0xFF0A0A0A) else Color(0xFFF6F6F8),
                    destaque = if (vm.temaEscuro) vm.corNeon.cor else vm.corNeon.corClara,
                    selecionado = vm.estiloTema == EstiloTema.NEON,
                    modifier = Modifier.weight(1f)
                ) { vm.definirEstiloTema(EstiloTema.NEON) }

                                        CartaoEstilo(
                    nome = "Livro",
                    descricao = "Pergaminho e ouro",
                    fundo = Color(0xFF0D0B0A),
                    destaque = Color(0xFFC9A36A),
                    selecionado = vm.estiloTema == EstiloTema.LIVRO,
                    modifier = Modifier.weight(1f)
                ) { vm.definirEstiloTema(EstiloTema.LIVRO) }
            }

            // ---- Cor de destaque (só no estilo Neon) ----
            if (vm.estiloTema == EstiloTema.NEON) {
                TituloSecao("Cor de destaque")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CorNeon.entries.forEach { opcao ->
                        val selecionada = opcao == vm.corNeon
                        // No claro a cor aparece no tom fundo — o mesmo que o app vai usar.
                        val corDaOpcao = if (vm.temaEscuro) opcao.cor else opcao.corClara
                        Surface(
                            color = if (selecionada) corDaOpcao.copy(alpha = 0.12f) else SuperficieAlta,
                            shape = Cantos.campo,
                            border = if (selecionada) BorderStroke(2.dp, corDaOpcao) else null,
                            modifier = Modifier.weight(1f).clickable { vm.definirCorNeon(opcao) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(corDaOpcao)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    opcao.rotulo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selecionada) corDaOpcao else TextoSuave
                                )
                            }
                        }
                    }
                }
                Text(
                    "A identidade do app é uma cor só: ela pinta botões, chips e a barra de vida.",
                    color = TextoSuave,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // O interruptor vale para os dois estilos: no Livro ele troca o pergaminho,
            // no Neon troca o fundo preto pelo fundo claro (a cor de destaque continua a mesma).
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Modo escuro", modifier = Modifier.weight(1f))
                Switch(
                    checked = vm.temaEscuro,
                    onCheckedChange = { vm.alternarTema() }
                )
            }
            Text(
                if (vm.estiloTema == EstiloTema.LIVRO) {
                    "No estilo Livro, o modo escuro troca o pergaminho claro pelo pergaminho escuro."
                } else {
                    "No estilo Neon, o modo escuro troca o fundo preto pelo fundo claro; " +
                        "a cor de destaque continua a mesma (só no tom mais fundo, para dar leitura)."
                },
                color = TextoSuave,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.height(4.dp))

            // ---- Leitura ----

            // Modo de leitura: chat (RPG) ou livro (Historia).
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Modo de leitura", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "RPG (chat com bolhas) ou Historia (leitura em livro)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSuave
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = if (vm.modoLeitura == AppViewModel.ModoLeitura.RPG) Ouro else SuperficieAlta,
                        shape = Cantos.pilula,
                        modifier = Modifier.clickable { vm.definirModoLeitura(AppViewModel.ModoLeitura.RPG) }
                    ) {
                        Text("RPG", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Surface(
                        color = if (vm.modoLeitura == AppViewModel.ModoLeitura.HISTORIA) Ouro else SuperficieAlta,
                        shape = Cantos.pilula,
                        modifier = Modifier.clickable { vm.definirModoLeitura(AppViewModel.ModoLeitura.HISTORIA) }
                    ) {
                        Text("Historia", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }

            TituloSecao("Leitura")
            Text(
                "Tamanho da letra do narrador: ${vm.tamanhoLetraNarrador.toInt()}",
                color = TextoSuave,
                style = MaterialTheme.typography.labelSmall
            )
            Slider(
                value = vm.tamanhoLetraNarrador,
                onValueChange = { vm.definirTamanhoLetra(it) },
                valueRange = 13f..20f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                color = SuperficieAlta,
                shape = Cantos.cartao,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "O narrador fala assim: a noite caiu sobre a cidade e algo se moveu nas sombras.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = vm.tamanhoLetraNarrador.sp,
                        lineHeight = (vm.tamanhoLetraNarrador * 1.45f).sp
                    ),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                "Entre 13 (bem discreto) e 20 (bem grande). Ideal: 16.",
                color = TextoSuave,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.height(4.dp))

            // ---- Segurança & Filtro de palavras sensíveis do LivennIA ----
            TituloSecao("Segurança do chat")
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Filtro de palavras sensíveis", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Bloqueia termos impróprios localmente antes do envio (base PT/EN)",
                        color = TextoSuave,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Switch(
                    checked = vm.filtroPalavrasSensiveis,
                    onCheckedChange = { vm.definirFiltroPalavrasSensiveis(it) }
                )
            }

            Spacer(Modifier.height(4.dp))

            // ---- Servidor ----
            TituloSecao("Servidor")
            OutlinedTextField(
                value = servidor,
                onValueChange = { servidor = it; vm.alterarServidor(it) },
                shape = Cantos.campo,
                label = { Text("Endereço do servidor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Ex.: http://192.168.15.20:8000",
                color = TextoSuave,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.height(4.dp))

            // ---- Conta ----
            TituloSecao("Conta")
            Text(
                "Apagar a conta remove seu cadastro e todos os seus personagens.",
                color = TextoSuave,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = { vm.apagarConta() }) {
                Text("Apagar minha conta", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(4.dp))

            // ---- Sobre & Atualizações ----
            TituloSecao("Sobre e Atualizações")
            CardAtualizacaoGitHub(vm)
            Text(
                "Um jogo de aventura e histórias interativas com IA.",
                color = TextoSuave,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { vm.navegarPara(Tela.Personagens) }) { Text("Voltar") }
        }
    }
}

// Cartão de escolha de estilo, com uma pequena prévia das cores.
@Composable
private fun CartaoEstilo(
    nome: String,
    descricao: String,
    fundo: Color,
    destaque: Color,
    selecionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (selecionado) destaque.copy(alpha = 0.14f) else SuperficieAlta,
        shape = Cantos.cartao,
        border = if (selecionado) BorderStroke(2.dp, destaque) else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(fundo)
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(destaque)
                            .align(Alignment.Center)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(nome, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            Text(descricao, color = TextoSuave, style = MaterialTheme.typography.labelSmall)
            if (selecionado) {
                Spacer(Modifier.height(4.dp))
                Text("Selecionado", color = destaque, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}





