package com.livennia.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EntradaTela(vm: AppViewModel) {
    // O prefixo "LIV-" é fixo; o usuário digita apenas os 8 caracteres restantes.
    // O traço é inserido automaticamente: LIV-XXXX-XXXX
    var codigoDigitado by rememberSaveable { mutableStateOf("") }
    val codigo = "LIV-" + codigoDigitado.let { d ->
        if (d.length <= 4) d else d.take(4) + "-" + d.drop(4).take(4)
    }
    var apelidoDigitado by rememberSaveable { mutableStateOf(vm.apelido) }

    val codigoValido = codigo.matches(Regex("^LIV-[A-Z0-9]{4}-[A-Z0-9]{4}$"))
    val podeEntrar = !vm.ocupado && codigoValido && apelidoDigitado.trim().length >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("LivennIA", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text(
            "Uma aventura sombria, escrita por uma IA, só para você.",
            color = TextoSuave
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = codigoDigitado,
            onValueChange = { entrada ->
                val limpo = entrada.uppercase().filter { it.isLetterOrDigit() }.take(8)
                codigoDigitado = limpo
            },
            shape = Cantos.campo,
            label = { Text("Código de convite") },
            prefix = { Text("LIV-", color = MaterialTheme.colorScheme.primary) },
            placeholder = { Text("XXXX-XXXX") },
            supportingText = {
                Text(if (codigoDigitado.length < 8) "Digite os 8 caracteres do código" else "Código completo")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        // Pré-visualização do código formatado
        if (codigoDigitado.isNotEmpty()) {
            Text(
                text = "Código: $codigo",
                color = if (codigoValido) Color(0xFF4CAF50) else TextoSuave,
                style = MaterialTheme.typography.labelMedium
            )
        }

        OutlinedTextField(
            value = apelidoDigitado,
            onValueChange = { apelidoDigitado = it.take(20) },
            shape = Cantos.campo,
            label = { Text("Seu apelido") },
            supportingText = {
                Text("Já tinha uma conta? Digite o MESMO apelido para voltar às suas aventuras.")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.servidor,
            onValueChange = { vm.alterarServidor(it) },
            shape = Cantos.campo,
            label = { Text("Endereço do servidor (teste)") },
            placeholder = { Text("http://192.168.15.20:8000") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        vm.erro?.let { AvisoCard(it, ehErro = true, onFechar = { vm.limparErro() }) }
        vm.aviso?.let { AvisoCard(it, ehErro = false, onFechar = { vm.limparAviso() }) }
        if (vm.ocupado) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(20.dp))
        }

        Button(
            onClick = { vm.entrar(codigo, apelidoDigitado) },
            enabled = podeEntrar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar com convite")
        }

        TextButton(onClick = { vm.navegarPara(Tela.Login) }) {
            Text("Já tem conta? Entrar / Criar conta")
        }
    }
}
