package com.livennia.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class InfoAtualizacao(
    val versaoCodigo: Int,
    val versaoNome: String,
    val notas: String,
    val apkUrl: String,
    val paginaUrl: String = "https://github.com/${AtualizadorGitHub.DONO_REPO}/${AtualizadorGitHub.NOME_REPO}"
)

data class ResultadoAtualizacao(
    val disponivel: Boolean,
    val info: InfoAtualizacao?,
    val versaoInstaladaNome: String,
    val versaoInstaladaCodigo: Int
)

object AtualizadorGitHub {
    const val DONO_REPO = "AlissonJr98"
    const val NOME_REPO = "LivennIA"
    const val URL_VERSAO_JSON = "https://raw.githubusercontent.com/$DONO_REPO/$NOME_REPO/main/atualizacao/versao.json"
    const val URL_RELEASES_API = "https://api.github.com/repos/$DONO_REPO/$NOME_REPO/releases/latest"
    const val URL_APK_PADRAO = "https://raw.githubusercontent.com/$DONO_REPO/$NOME_REPO/main/atualizacao/livennia.apk"

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun obterVersaoInstalada(context: Context): Pair<Int, String> {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val codigo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
            val nome = pInfo.versionName ?: "1.0"
            codigo to nome
        } catch (_: Throwable) {
            1 to "1.0"
        }
    }

    suspend fun verificar(context: Context): ResultadoAtualizacao = withContext(Dispatchers.IO) {
        val (codigoLocal, nomeLocal) = obterVersaoInstalada(context)
        val infoJson = runCatching { buscarPorVersaoJson() }.getOrNull()
        val infoRelease = runCatching { buscarPorGitHubRelease() }.getOrNull()

        val melhorInfo = when {
            infoJson != null && infoRelease != null -> {
                if (ehMaisNova(infoRelease.versaoCodigo, infoRelease.versaoNome, infoJson.versaoCodigo, infoJson.versaoNome)) {
                    infoRelease
                } else {
                    infoJson
                }
            }
            infoJson != null -> infoJson
            else -> infoRelease
        }

        val temNova = melhorInfo != null && ehMaisNova(
            melhorInfo.versaoCodigo,
            melhorInfo.versaoNome,
            codigoLocal,
            nomeLocal
        )

        ResultadoAtualizacao(
            disponivel = temNova,
            info = melhorInfo,
            versaoInstaladaNome = nomeLocal,
            versaoInstaladaCodigo = codigoLocal
        )
    }

    private fun buscarPorVersaoJson(): InfoAtualizacao? {
        val req = Request.Builder()
            .url("$URL_VERSAO_JSON?t=${System.currentTimeMillis()}")
            .header("Cache-Control", "no-cache")
            .header("User-Agent", "LivennIA-Android-Updater")
            .build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val obj = JsonParser.parseString(body).asJsonObject
            val codigo = obj.get("versaoCodigo")?.asInt ?: 1
            val nome = obj.get("versaoNome")?.asString?.removePrefix("v")?.trim() ?: "1.0"
            val notas = obj.get("notas")?.asString?.trim() ?: "Nova atualização disponível no GitHub."
            val apkUrl = obj.get("apkUrl")?.asString?.trim().takeUnless { it.isNullOrBlank() } ?: URL_APK_PADRAO
            return InfoAtualizacao(
                versaoCodigo = codigo,
                versaoNome = nome,
                notas = notas,
                apkUrl = apkUrl
            )
        }
    }

    private fun buscarPorGitHubRelease(): InfoAtualizacao? {
        val req = Request.Builder()
            .url(URL_RELEASES_API)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "LivennIA-Android-Updater")
            .build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val obj = JsonParser.parseString(body).asJsonObject
            val tag = obj.get("tag_name")?.asString?.removePrefix("v")?.trim() ?: return null
            val notas = obj.get("body")?.asString?.trim().takeUnless { it.isNullOrBlank() }
                ?: obj.get("name")?.asString?.trim()
                ?: "Nova versão publicada no GitHub."
            val htmlUrl = obj.get("html_url")?.asString ?: "https://github.com/$DONO_REPO/$NOME_REPO/releases"
            val assets = obj.getAsJsonArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (el in assets) {
                    val asset = el.asJsonObject
                    val nomeArquivo = asset.get("name")?.asString ?: ""
                    val url = asset.get("browser_download_url")?.asString
                    if (nomeArquivo.endsWith(".apk", ignoreCase = true) && !url.isNullOrBlank()) {
                        apkUrl = url
                        break
                    }
                }
            }
            if (apkUrl.isNullOrBlank()) return null
            val codigoExtraido = extrairCodigoDeTag(tag)
            return InfoAtualizacao(
                versaoCodigo = codigoExtraido,
                versaoNome = tag,
                notas = notas,
                apkUrl = apkUrl,
                paginaUrl = htmlUrl
            )
        }
    }

    private fun extrairCodigoDeTag(tag: String): Int {
        // Ex.: "1.2" -> 2, "1.0.3" -> 3, ou se tiver "-b4" -> 4
        val limpo = tag.removePrefix("v").trim()
        val partes = limpo.split(".", "-", "_").mapNotNull { it.toIntOrNull() }
        if (partes.size >= 3) return partes[0] * 100 + partes[1] * 10 + partes[2]
        if (partes.size == 2 && partes[0] == 1) return partes[1] + 1
        return partes.lastOrNull() ?: 1
    }

    fun ehMaisNova(
        codigoRemoto: Int,
        nomeRemoto: String,
        codigoLocal: Int,
        nomeLocal: String
    ): Boolean {
        if (codigoRemoto > codigoLocal) return true
        val cmp = compararSemver(nomeRemoto, nomeLocal)
        return cmp > 0
    }

    private fun compararSemver(a: String, b: String): Int {
        val pa = a.removePrefix("v").trim().split(".", "-").map { it.toIntOrNull() ?: 0 }
        val pb = b.removePrefix("v").trim().split(".", "-").map { it.toIntOrNull() ?: 0 }
        val max = maxOf(pa.size, pb.size)
        for (i in 0 until max) {
            val va = pa.getOrElse(i) { 0 }
            val vb = pb.getOrElse(i) { 0 }
            if (va != vb) return va.compareTo(vb)
        }
        return 0
    }

    suspend fun baixarApk(
        context: Context,
        apkUrl: String,
        onProgresso: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val pasta = File(context.cacheDir, "updates").apply { mkdirs() }
        val destino = File(pasta, "LivennIA-update.apk")
        if (destino.exists()) destino.delete()

        val urlComCacheBust = if (apkUrl.contains("?")) "$apkUrl&t=${System.currentTimeMillis()}"
        else "$apkUrl?t=${System.currentTimeMillis()}"

        val req = Request.Builder()
            .url(urlComCacheBust)
            .header("User-Agent", "LivennIA-Android-Updater")
            .header("Accept", "application/octet-stream, application/vnd.android.package-archive, */*")
            .build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw IllegalStateException("Não foi possível baixar o APK no GitHub (HTTP ${resp.code}).")
            }
            val body = resp.body ?: throw IllegalStateException("Resposta vazia ao baixar o APK.")
            val total = body.contentLength()
            body.byteStream().use { input ->
                FileOutputStream(destino).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var baixado = 0L
                    var lidos: Int
                    while (input.read(buffer).also { lidos = it } != -1) {
                        output.write(buffer, 0, lidos)
                        baixado += lidos
                        if (total > 0) {
                            onProgresso((baixado.toFloat() / total.toFloat()).coerceIn(0f, 1f))
                        }
                    }
                    output.flush()
                }
            }
        }

        // Verifica se é realmente um arquivo APK (ZIP começa com 'P' 'K')
        if (destino.length() < 50_000L) {
            destino.delete()
            throw IllegalStateException("O arquivo baixado do GitHub é inválido ou ainda não foi publicado.")
        }
        val cabecalho = destino.inputStream().use {
            val b = ByteArray(2)
            it.read(b)
            b
        }
        if (cabecalho[0] != 'P'.code.toByte() || cabecalho[1] != 'K'.code.toByte()) {
            destino.delete()
            throw IllegalStateException("O arquivo baixado não é um pacote APK válido.")
        }

        onProgresso(1f)
        destino
    }

    fun podeInstalarPacotes(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun abrirConfiguracaoFontesDesconhecidas(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun instalarApk(context: Context, arquivoApk: File): Boolean {
        if (!podeInstalarPacotes(context)) {
            abrirConfiguracaoFontesDesconhecidas(context)
            return false
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivoApk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }
}

@Composable
fun DialogoAtualizacaoGitHub(
    info: InfoAtualizacao,
    versaoInstalada: String,
    baixando: Boolean,
    progresso: Float,
    apkPronto: Boolean,
    erro: String?,
    onBaixarOuInstalar: () -> Unit,
    onFechar: () -> Unit
) {
    Dialog(onDismissRequest = { if (!baixando) onFechar() }) {
        Surface(
            color = SuperficieAlta,
            shape = Cantos.cartao,
            border = BorderStroke(1.dp, Ouro.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚀", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Atualização via GitHub", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Instalada: v$versaoInstalada  →  GitHub: v${info.versaoNome}",
                            color = Ouro,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Surface(
                    color = Fundo.copy(alpha = 0.65f),
                    shape = Cantos.campo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "NOVIDADES DA VERSÃO",
                            color = Ouro,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            info.notas,
                            color = TextoSuave,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (baixando) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val pct = (progresso * 100).toInt()
                        Text(
                            "Baixando atualização do GitHub... $pct%",
                            color = Ouro,
                            style = MaterialTheme.typography.labelMedium
                        )
                        LinearProgressIndicator(
                            progress = { progresso.coerceIn(0f, 1f) },
                            color = Ouro,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (!erro.isNullOrBlank()) {
                    Text(
                        erro,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Button(
                    onClick = onBaixarOuInstalar,
                    enabled = !baixando,
                    shape = Cantos.campo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            baixando -> "Baixando APK..."
                            apkPronto -> "📦 Instalar atualização agora"
                            else -> "⬇ Baixar e atualizar pelo GitHub"
                        }
                    )
                }

                if (!baixando) {
                    TextButton(
                        onClick = onFechar,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Agora não", color = TextoSuave)
                    }
                }
            }
        }
    }
}

@Composable
fun CardAtualizacaoGitHub(vm: AppViewModel) {
    Surface(
        color = SuperficieAlta,
        shape = Cantos.cartao,
        border = BorderStroke(
            1.dp,
            if (vm.temNovaAtualizacao) Ouro.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Atualização do App (GitHub)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Versão instalada: v${vm.versaoInstaladaNome} (build ${vm.versaoInstaladaCodigo})",
                        color = TextoSuave,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "Repositório: github.com/${AtualizadorGitHub.DONO_REPO}/${AtualizadorGitHub.NOME_REPO}",
                        color = Ouro.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (vm.temNovaAtualizacao) {
                    ChipTag("Nova v${vm.infoAtualizacao?.versaoNome}", destaque = true)
                }
            }

            vm.statusAtualizacao?.let { msg ->
                Text(
                    msg,
                    color = if (vm.temNovaAtualizacao) Ouro else TextoSuave,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (vm.baixandoAtualizacao) {
                val pct = (vm.progressoAtualizacao * 100).toInt()
                Text("Baixando APK: $pct%", color = Ouro, style = MaterialTheme.typography.labelSmall)
                LinearProgressIndicator(
                    progress = { vm.progressoAtualizacao.coerceIn(0f, 1f) },
                    color = Ouro,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { vm.verificarAtualizacaoGitHub(manual = true) },
                    enabled = !vm.verificandoAtualizacao && !vm.baixandoAtualizacao,
                    modifier = Modifier.weight(1f)
                ) {
                    if (vm.verificandoAtualizacao) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Ouro
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Buscando...")
                    } else {
                        Text("🔄 Verificar no GitHub")
                    }
                }

                if (vm.infoAtualizacao != null) {
                    Button(
                        onClick = { vm.iniciarDownloadOuInstalacaoGitHub() },
                        enabled = !vm.baixandoAtualizacao,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            when {
                                vm.apkAtualizacaoBaixado != null -> "📦 Instalar"
                                vm.temNovaAtualizacao -> "⬇ Atualizar"
                                else -> "⬇ Reinstalar"
                            }
                        )
                    }
                }
            }
        }
    }
}
