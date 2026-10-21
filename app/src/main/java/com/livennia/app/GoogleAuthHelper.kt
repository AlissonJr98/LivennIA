package com.livennia.app

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object GoogleAuthHelper {
    const val WEB_CLIENT_ID = "846486022098-st00tvi51dgl2pd72gbggv0cvmbn768i.apps.googleusercontent.com"

    fun iniciarLogin(
        contexto: Context,
        escopo: CoroutineScope,
        vm: AppViewModel,
        codigoConvite: String? = null,
        onFallbackLegacy: () -> Unit
    ) {
        escopo.launch {
            try {
                val cm = CredentialManager.create(contexto)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Mostra todas as contas Google configuradas no aparelho
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false) // Garante a exibição da janela de seleção de conta
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val resultado = cm.getCredential(request = request, context = contexto)
                val credencial = resultado.credential

                if (credencial is CustomCredential && credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCred = GoogleIdTokenCredential.createFrom(credencial.data)
                    val idToken = googleCred.idToken
                    vm.loginComGoogle(idToken, codigoConvite)
                } else {
                    onFallbackLegacy()
                }
            } catch (e: GetCredentialCancellationException) {
                // Usuário fechou ou cancelou o seletor de contas, não exibir erro
            } catch (e: Exception) {
                // Em caso de falha no Credential Manager (Play Services antigo, etc.), aciona fallback do Google Sign-In
                onFallbackLegacy()
            }
        }
    }
}
