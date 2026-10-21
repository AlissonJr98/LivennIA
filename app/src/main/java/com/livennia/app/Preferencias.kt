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
    private val pConfig = context.getSharedPreferences("livennia_config", Context.MODE_PRIVATE)

    // IP padrão do PC na rede local (para o app no celular alcançar o servidor).
    val servidorPadrao = "https://functionality-jumping-hood-man.trycloudflare.com"

    var token: String?
        get() {
            val salvo = pConfig.getString("token_auth", null)
            if (!salvo.isNullOrBlank()) return salvo
            return try { p.getString("token", null) } catch (_: Exception) { null }
        }
        set(valor) {
            pConfig.edit().putString("token_auth", valor).commit()
            try { p.edit().putString("token", valor).commit() } catch (_: Exception) {}
        }

    var apelido: String
        get() {
            val salvo = pConfig.getString("apelido_user", null)
            if (!salvo.isNullOrBlank()) return salvo
            return try { p.getString("apelido", "") ?: "" } catch (_: Exception) { "" }
        }
        set(valor) {
            pConfig.edit().putString("apelido_user", valor).commit()
            try { p.edit().putString("apelido", valor).commit() } catch (_: Exception) {}
        }

    var servidor: String
        get() {
            val salvo = pConfig.getString("servidor_custom", null)
            if (!salvo.isNullOrBlank()) return salvo
            val legacy = try { p.getString("servidor", null) } catch (_: Exception) { null }
            if (!legacy.isNullOrBlank()) return legacy
            return servidorPadrao
        }
        set(valor) {
            val limpo = valor.trim().trimEnd('/')
            pConfig.edit().putString("servidor_custom", limpo).commit()
            try { p.edit().putString("servidor", limpo).commit() } catch (_: Exception) {}
        }

    fun limparSessao() {
        pConfig.edit().remove("token_auth").remove("apelido_user").commit()
        try { p.edit().remove("token").remove("apelido").commit() } catch (_: Exception) {}
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

    // ---- Minha Persona (Perfil do Protagonista que interage com a IA) ----
    var personaNome: String
        get() = p.getString("persona_nome", "") ?: ""
        set(valor) { p.edit().putString("persona_nome", valor).apply() }

    var personaGenero: String
        get() = p.getString("persona_genero", "Masculino") ?: "Masculino"
        set(valor) { p.edit().putString("persona_genero", valor).apply() }

    var personaDescricao: String
        get() = p.getString("persona_descricao", "") ?: ""
        set(valor) { p.edit().putString("persona_descricao", valor).apply() }

    var personaIdade: String
        get() = p.getString("persona_idade", "") ?: ""
        set(valor) { p.edit().putString("persona_idade", valor).apply() }

    var personaAparencia: String
        get() = p.getString("persona_aparencia", "") ?: ""
        set(valor) { p.edit().putString("persona_aparencia", valor).apply() }

    var personaInteresses: String
        get() = p.getString("persona_interesses", "") ?: ""
        set(valor) { p.edit().putString("persona_interesses", valor).apply() }

    var personaAversoes: String
        get() = p.getString("persona_aversoes", "") ?: ""
        set(valor) { p.edit().putString("persona_aversoes", valor).apply() }

    var personaTags: String
        get() = p.getString("persona_tags", "") ?: ""
        set(valor) { p.edit().putString("persona_tags", valor).apply() }

    var personaAvatar: String
        get() = p.getString("persona_avatar", "👤") ?: "👤"
        set(valor) { p.edit().putString("persona_avatar", valor).apply() }

    var personaTemRetrato: Boolean
        get() = pConfig.getBoolean("persona_tem_retrato", false)
        set(valor) { pConfig.edit().putBoolean("persona_tem_retrato", valor).commit() }
}