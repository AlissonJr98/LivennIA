package com.livennia.app

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.LinearProgressIndicator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// ---------- Login ----------
@Composable
fun LoginTela(vm: AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    val contexto = LocalContext.current

    val webClientId = remember {
        val id = contexto.resources.getIdentifier("default_web_client_id", "string", contexto.packageName)
        if (id != 0) contexto.getString(id) else null
    }
    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .apply { if (webClientId != null) requestIdToken(webClientId) }
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(contexto, gso) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val conta = task.getResult(ApiException::class.java)
            val idToken = conta.idToken
            if (idToken != null) vm.loginComGoogle(idToken)
            else vm.definirErro("Não foi possível obter o token do Google.")
        } catch (e: ApiException) {
            vm.definirErro("Login com Google cancelado ou falhou.")
        }
    }

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
        Text("Entre na sua conta para continuar.", color = TextoSuave)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            shape = Cantos.campo,
            label = { Text("Email") },
            singleLine = true,
            isError = email.isNotEmpty() && !emailValido(email),
            supportingText = {
                if (email.isNotEmpty() && !emailValido(email)) Text("Email inválido", color = Sangue)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            shape = Cantos.campo,
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        vm.erro?.let { AvisoCard(it, ehErro = true, onFechar = { vm.limparErro() }) }
        vm.aviso?.let { AvisoCard(it, ehErro = false, onFechar = { vm.limparAviso() }) }
        if (vm.ocupado) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(20.dp))
        }

        Button(
            onClick = { vm.loginComEmail(email, senha) },
            enabled = !vm.ocupado && email.isNotBlank() && senha.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        OutlinedButton(
            onClick = { launcher.launch(googleSignInClient.signInIntent) },
            enabled = !vm.ocupado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar com Google")
        }

        val contextoLocal = contexto
        if (suportaBiometria(contextoLocal)) {
            OutlinedButton(
                onClick = {
                    val activity = contextoLocal as? FragmentActivity
                    if (activity != null) {
                        solicitarBiometria(
                            activity = activity,
                            onSuccess = {
                                // Biometria OK — tenta login com credenciais salvas (se houver)
                                vm.definirAviso("Biometria confirmada. Entre com email/senha.")
                            },
                            onError = { vm.definirErro(it) }
                        )
                    }
                },
                enabled = !vm.ocupado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar com biometria")
            }
        }

        TextButton(onClick = { vm.navegarPara(Tela.Cadastro) }) {
            Text("Não tem conta? Criar conta")
        }
        TextButton(onClick = { vm.navegarPara(Tela.RecuperarSenha) }) {
            Text("Esqueci minha senha")
        }
        TextButton(onClick = { vm.navegarPara(Tela.Entrada) }) {
            Text("Voltar (entrar com convite)")
        }
    }
}

// ---------- Cadastro ----------
@Composable
fun CadastroTela(vm: AppViewModel) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var confirmarSenha by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Criar conta", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Preencha os dados para começar sua aventura.", color = TextoSuave)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it.take(30) },
            shape = Cantos.campo,
            label = { Text("Nome (apelido)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            shape = Cantos.campo,
            label = { Text("Email") },
            singleLine = true,
            isError = email.isNotEmpty() && !emailValido(email),
            supportingText = {
                if (email.isNotEmpty() && !emailValido(email)) Text("Email inválido", color = Sangue)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            shape = Cantos.campo,
            label = { Text("Senha (mín. 6 caracteres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        // Indicador de força da senha
        if (senha.isNotEmpty()) {
            val forca = when {
                senha.length >= 8 && senha.any { it.isDigit() } && senha.any { it.isUpperCase() } -> 3
                senha.length >= 6 -> 2
                else -> 1
            }
            val corForca = when (forca) {
                3 -> Color(0xFF4CAF50)
                2 -> Color(0xFFFFC107)
                else -> Sangue
            }
            val textoForca = when (forca) {
                3 -> "Senha forte"
                2 -> "Senha média"
                else -> "Senha fraca"
            }
            LinearProgressIndicator(
                progress = { forca / 3f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = corForca,
                trackColor = SuperficieAlta
            )
            Text(textoForca, color = corForca, style = MaterialTheme.typography.labelSmall)
        }
        OutlinedTextField(
            value = confirmarSenha,
            onValueChange = { confirmarSenha = it },
            shape = Cantos.campo,
            label = { Text("Confirmar senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        vm.erro?.let { AvisoCard(it, ehErro = true, onFechar = { vm.limparErro() }) }
        vm.aviso?.let { AvisoCard(it, ehErro = false, onFechar = { vm.limparAviso() }) }
        if (vm.ocupado) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(20.dp))
        }

        val senhasConferem = senha == confirmarSenha && senha.length >= 6
        Button(
            onClick = { vm.cadastrarComEmail(nome, email, senha) },
            enabled = !vm.ocupado && email.isNotBlank() && senhasConferem && nome.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar conta")
        }

        TextButton(onClick = { vm.navegarPara(Tela.Login) }) {
            Text("Já tem conta? Entrar")
        }
    }
}

// ---------- Recuperar senha ----------
@Composable
fun RecuperarSenhaTela(vm: AppViewModel) {
    var email by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Recuperar senha", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Informe seu email para receber o link de redefinição.", color = TextoSuave)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            shape = Cantos.campo,
            label = { Text("Email") },
            singleLine = true,
            isError = email.isNotEmpty() && !emailValido(email),
            supportingText = {
                if (email.isNotEmpty() && !emailValido(email)) Text("Email inválido", color = Sangue)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        vm.erro?.let { AvisoCard(it, ehErro = true, onFechar = { vm.limparErro() }) }
        vm.aviso?.let { AvisoCard(it, ehErro = false, onFechar = { vm.limparAviso() }) }
        if (vm.ocupado) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(20.dp))
        }

        Button(
            onClick = { vm.recuperarSenha(email) },
            enabled = !vm.ocupado && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar link")
        }

        TextButton(onClick = { vm.navegarPara(Tela.Login) }) {
            Text("Voltar ao login")
        }
    }
}

fun emailValido(email: String): Boolean =
    email.isNotBlank() && email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
