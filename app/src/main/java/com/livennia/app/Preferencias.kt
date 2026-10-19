package com.livennia.app

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Guarda dados simples no aparelho. O token é guardado criptografado (Android Keystore).
class Preferencias(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val p = EncryptedSharedPreferences.create(
        context,
        "livennia_prefs_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // IP padrão do PC na rede local (para o app no celular alcançar o servidor).
    val servidorPadrao = "192.168.15.20:8000"

    var token: String?
        get() = p.getString("token", null)
        set(valor) {
            p.edit().putString("token", valor).apply()
        }

    var apelido: String
        get() = p.getString("apelido", "") ?: ""
        set(valor) {
            p.edit().putString("apelido", valor).apply()
        }

    var servidor: String
        get() = p.getString("servidor", servidorPadrao) ?: servidorPadrao
        set(valor) {
            p.edit().putString("servidor", valor).apply()
        }

    fun limparSessao() {
        p.edit().remove("token").remove("apelido").apply()
    }

    var temaEscuro: Boolean
        get() = p.getBoolean("tema_escuro", true)
        set(valor) {
            p.edit().putBoolean("tema_escuro", valor).apply()
        }

    // Estilo visual: "NEON" (preto + uma cor viva) ou "LIVRO" (pergaminho + ouro).
    var estiloTema: String
        get() = p.getString("estilo_tema", EstiloTema.NEON.name) ?: EstiloTema.NEON.name
        set(valor) {
            p.edit().putString("estilo_tema", valor).apply()
        }

    // Cor de destaque do estilo Neon (Amarelo, Ciano, Roxo ou Vermelho).
    var corNeon: String
        get() = p.getString("cor_neon", CorNeon.ROXO.name) ?: CorNeon.ROXO.name
        set(valor) {
            p.edit().putString("cor_neon", valor).apply()
        }

    var onboardingVisto: Boolean
        get() = p.getBoolean("onboarding_visto", false)
        set(valor) {
            p.edit().putBoolean("onboarding_visto", valor).apply()
        }

    // Última aventura aberta: assim o app retoma de onde o jogador parou
    // ao ser fechado (o histórico fica guardado no servidor).
    var ultimoJogoId: String?
        get() = p.getString("ultimo_jogo_id", null)
        set(valor) {
            p.edit().putString("ultimo_jogo_id", valor).apply()
        }

    // Tamanho da letra do narrador (em sp). Travado entre 13 e 20 para
    // nunca ficar extravagante nem pequena demais.
    var tamanhoLetraNarrador: Float
        get() = p.getFloat("tamanho_letra_narrador", 16f)
        set(valor) {
            p.edit().putFloat("tamanho_letra_narrador", valor.coerceIn(13f, 20f)).apply()
        }

    // Modo de leitura: RPG (chat/bolhas) ou HISTORIA (estilo livro).
    var modoLeitura: String
        get() = p.getString("modo_leitura", "RPG") ?: "RPG"
        set(valor) {
            p.edit().putString("modo_leitura", valor).apply()
        }

    // Filtro local de palavras sensíveis do LivennIA.
    var filtroPalavrasSensiveis: Boolean
        get() = p.getBoolean("filtro_palavras_sensiveis", true)
        set(valor) {
            p.edit().putBoolean("filtro_palavras_sensiveis", valor).apply()
        }
}