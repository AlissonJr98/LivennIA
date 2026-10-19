package com.livennia.app

import com.google.gson.JsonParser
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

// As rotas do servidor, uma função para cada.
interface LivennIAApi {
    @POST("registrar")
    suspend fun registrar(@Body corpo: RegistroRequest): RegistroResponse

    @POST("auth/firebase")
    suspend fun authFirebase(@Body corpo: FirebaseAuthIn): RegistroResponse

    @GET("conta")
    suspend fun conta(): ContaResponse

    @DELETE("conta")
    suspend fun apagarConta(): Response<Unit>

    @GET("jogos")
    suspend fun jogos(): List<JogoResumo>

    @GET("generos")
    suspend fun generos(): List<GeneroInfo>

    @GET("catalogo")
    suspend fun catalogo(): List<CenarioDto>

    @GET("cerebros")
    suspend fun cerebros(): List<CerebroDto>

    @POST("catalogo/{id}/capa")
    suspend fun gerarCapa(@Path("id") id: String): CapaResponse

    @POST("jogos")
    suspend fun criarJogo(@Body corpo: NovoJogoRequest): JogoResponse

    @GET("jogos/{id}")
    suspend fun jogo(@Path("id") id: String): JogoResponse

    @POST("jogos/{id}/turno")
    suspend fun turno(@Path("id") id: String, @Body corpo: TurnoRequest): JogoResponse

    @DELETE("jogos/{id}")
    suspend fun apagarJogo(@Path("id") id: String): Response<Unit>

    @POST("jogos/{id}/retrato")
    suspend fun gerarRetrato(@Path("id") id: String): RetratoResponse

    @POST("jogos/{id}/avatar")
    suspend fun sortearAvatar(@Path("id") id: String): AvatarResponse

    @POST("feedback")
    suspend fun feedback(@Body corpo: FeedbackRequest): Response<Unit>

    @POST("denuncias")
    suspend fun denuncia(@Body corpo: DenunciaRequest): Response<Unit>
}

object ApiClient {
    // Token do jogador: o interceptor abaixo o coloca em toda requisição.
    // (Nunca registramos o token em log.)
    @Volatile
    var token: String? = null

    private val cliente: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        // O servidor pode levar até ~80 s numa jogada quando a IA está instável.
        .readTimeout(100, TimeUnit.SECONDS)
        .callTimeout(110, TimeUnit.SECONDS)
        .addInterceptor { cadeia ->
            val t = token
            val pedido = if (t.isNullOrEmpty()) {
                cadeia.request()
            } else {
                cadeia.request().newBuilder().header("Authorization", "Bearer $t").build()
            }
            cadeia.proceed(pedido)
        }
        .build()

    private var urlAtual: String? = null
    private var apiAtual: LivennIAApi? = null

    // Endereço base atual (o app monta a URL dos retratos com ele).
    fun baseUrlAtual(): String? = urlAtual

    // Cabeçalho de autenticação (usado ao carregar as imagens dos retratos).
    fun cabecalhoAutorizacao(): String? = token?.let { "Bearer $it" }

    @Synchronized
    fun obter(baseUrl: String): LivennIAApi {
        val existente = apiAtual
        if (existente != null && urlAtual == baseUrl) return existente
        val nova = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LivennIAApi::class.java)
        urlAtual = baseUrl
        apiAtual = nova
        return nova
    }
}

// Aceita "192.168.0.15:8000" ou "http://192.168.0.15:8000" e devolve "http://192.168.0.15:8000/".
// Devolve null se o endereço for inválido.
fun normalizarServidor(texto: String): String? {
    var s = texto.trim()
    if (s.isEmpty()) return null
    if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://$s"
    if (!s.endsWith("/")) s += "/"
    return s.toHttpUrlOrNull()?.toString()
}

class ErroApp(val mensagem: String, val sessaoInvalida: Boolean = false)

// Transforma qualquer falha numa mensagem em português para o jogador.
fun Throwable.paraErroApp(): ErroApp {
    return when (this) {
        is HttpException -> {
            val detalhe = lerDetalhe(response()?.errorBody()?.string())
            when (code()) {
                401 -> ErroApp("Sua sessão não é mais válida. Entre novamente.", sessaoInvalida = true)
                403 -> ErroApp(detalhe ?: "Acesso negado.")
                404 -> ErroApp(detalhe ?: "Não encontrado.")
                409 -> ErroApp(detalhe ?: "Não foi possível fazer isso agora.")
                413 -> ErroApp("Texto grande demais.")
                422 -> ErroApp(detalhe ?: "Dados inválidos. Confira o que foi digitado.")
                429 -> ErroApp(detalhe ?: "Muitas tentativas. Aguarde um pouco.")
                503 -> ErroApp(detalhe ?: "O servidor está indisponível agora. Tente de novo em instantes.")
                else -> ErroApp(detalhe ?: "Erro ${code()} no servidor.")
            }
        }
        is SocketTimeoutException ->
            ErroApp("O servidor demorou demais para responder. Tente de novo.")
        is IOException ->
            ErroApp(
                "Sem conexão com o servidor. Confira o Wi-Fi e o endereço do servidor.\n" +
                        "(Detalhe técnico: ${this.javaClass.simpleName}: ${this.message})"
            )
        else ->
            ErroApp("Algo deu errado. Tente de novo.")
    }
}

// O servidor responde erros como {"detail": "mensagem"}.
private fun lerDetalhe(corpo: String?): String? {
    if (corpo.isNullOrBlank()) return null
    return try {
        val detalhe = JsonParser.parseString(corpo).asJsonObject.get("detail")
        if (detalhe != null && detalhe.isJsonPrimitive) detalhe.asString else null
    } catch (e: Exception) {
        null
    }
}

// Para as rotas que devolvem Response<Unit> (sem corpo): falha se o status não for 2xx.
fun Response<Unit>.garantir() {
    if (!isSuccessful) throw HttpException(this)
}