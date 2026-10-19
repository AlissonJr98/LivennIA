package com.livennia.app

import android.content.Context
import org.json.JSONObject

/**
 * Filtro de palavras sensíveis do LivennIA.
 * Carrega a lista de palavras sensíveis em Português e Inglês de
 * `assets/sensitive_words/chat_block_keywords.json` e verifica textos localmente
 * (ignorando espaços intercalados e maiúsculas/minúsculas).
 */
object FiltroPalavras {
    @Volatile
    private var padroesCompilados: List<Pair<String, Regex>>? = null

    private fun garantirCarregado(context: Context): List<Pair<String, Regex>> {
        padroesCompilados?.let { return it }
        synchronized(this) {
            padroesCompilados?.let { return it }
            val termos = linkedSetOf<String>()
            try {
                val jsonTexto = context.assets
                    .open("sensitive_words/chat_block_keywords.json")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
                val raiz = JSONObject(jsonTexto)
                for (lang in listOf("pt", "en")) {
                    val arr = raiz.optJSONArray(lang) ?: continue
                    for (i in 0 until arr.length()) {
                        val palavra = arr.optString(i).trim().lowercase()
                        if (palavra.length >= 3) {
                            termos.add(palavra)
                        }
                    }
                }
            } catch (_: Exception) {
                termos.addAll(listOf("pornografia", "estupro", "pedofilia"))
            }
            val compilados = termos
                .sortedByDescending { it.length }
                .mapNotNull { original ->
                    val compacto = original.filter { !it.isWhitespace() }
                    if (compacto.length < 3) return@mapNotNull null
                    val partes = compacto.map { Regex.escape(it.toString()) }
                    val padrao = "(?<![a-zA-ZÀ-ÿ0-9])" + partes.joinToString("\\s*") + "(?![a-zA-ZÀ-ÿ0-9])"
                    runCatching { original to Regex(padrao, RegexOption.IGNORE_CASE) }.getOrNull()
                }
            padroesCompilados = compilados
            return compilados
        }
    }

    /**
     * Retorna a primeira palavra sensível encontrada no [texto], ou `null` se estiver limpo.
     */
    fun verificar(context: Context, texto: String): String? {
        if (texto.isBlank()) return null
        val lista = garantirCarregado(context)
        for ((original, regex) in lista) {
            if (regex.containsMatchIn(texto)) {
                return original
            }
        }
        return null
    }
}
