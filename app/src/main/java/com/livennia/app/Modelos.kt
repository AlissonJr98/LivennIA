package com.livennia.app



import com.google.gson.annotations.SerializedName



// Estes modelos espelham exatamente o JSON que o servidor envia e recebe.



data class RegistroRequest(

    @SerializedName("codigo_convite") val codigoConvite: String,

    val apelido: String

)



data class FirebaseAuthIn(
    @SerializedName("id_token") val idToken: String,
    @SerializedName("codigo_convite") val codigoConvite: String? = null
)



data class RegistroResponse(

    @SerializedName("jogador_id") val jogadorId: Int,

    val apelido: String,

    val token: String,

    // true quando o apelido já existia e a conta antiga (com as aventuras) foi recuperada

    val recuperado: Boolean = false

)



data class NovoJogoRequest(

    @SerializedName("nome_personagem") val nomePersonagem: String,

    val modo: String = "rpg",

    val genero: String = "Aventura",

    val premissa: String? = null,

    val aparencia: String? = null,

    val descricao: String? = null,

    val idade: String? = null,

    val interesses: String? = null,

    // Do que o personagem tem medo / não gosta (entra no prompt do narrador).

    val aversoes: String? = null,

    val tags: List<String>? = null,

    // Cenário oficial escolhido na vitrine: o servidor injeta o "prompt secreto" dele.

    @SerializedName("cenario_id") val cenarioId: String? = null,

    @SerializedName("cenario_nome") val cenarioNome: String? = null,

    @SerializedName("cenario_prologo") val cenarioPrologo: String? = null,

    // "Cérebro" de IA escolhido (veja /cerebros). Nulo = o padrão do servidor.

    @SerializedName("modelo_id") val modeloId: String? = null,

    val temperatura: Float? = null,

    @SerializedName("persona_id") val personaId: Int? = null

)



// ---- Vitrine (aba Explorar) ----

// O "prompt secreto" de cada cenário fica só no servidor: aqui chega o que aparece.

data class CenarioDto(
    val id: String,
    val nome: String,
    val subtitulo: String? = "",
    val emoji: String? = "",
    val genero: String? = "Aventura",
    val tags: List<String>? = emptyList(),
    val sinopse: String? = "",
    val abertura: String? = "",
    val capa: Boolean = false,
    val criadoPorUsuario: Boolean = false,
    val autorId: String? = null,
    val autorNome: String? = "Criado por IA",
    val autorAvatar: String? = "✨",
    val chats: Int = 0,
    val saves: Int = 0,
    val modoJogo: String? = "historia" // "historia" (só leitura / interativa) ou "rpg" (combates e dados)
) {
    val modoSeguro: String get() = modoJogo?.takeIf { it.isNotBlank() } ?: "historia"
    val autorNomeSeguro: String get() = autorNome?.takeIf { it.isNotBlank() } ?: "Criado por IA"
    val autorAvatarSeguro: String get() = autorAvatar?.takeIf { it.isNotBlank() } ?: "✨"
    val subtituloSeguro: String get() = subtitulo ?: ""
    val sinopseSegura: String get() = sinopse ?: ""
    val aberturaSegura: String get() = abertura ?: ""
    val emojiSeguro: String get() = emoji?.takeIf { it.isNotBlank() } ?: "📖"
    val generoSeguro: String get() = genero?.takeIf { it.isNotBlank() } ?: "Aventura"
    val tagsSeguras: List<String> get() = tags ?: emptyList()
}

data class NovoCenarioRequest(
    val id: String? = null,
    val nome: String,
    val subtitulo: String? = "",
    val emoji: String? = "🎭",
    val genero: String? = "Aventura",
    val tags: List<String> = emptyList(),
    val sinopse: String? = "",
    val abertura: String? = "",
    @SerializedName("prompt_secreto") val promptSecreto: String? = "",
    @SerializedName("estilo_imagem") val estiloImagem: String? = "semi_realistic",
    @SerializedName("modo_jogo") val modoJogo: String? = "historia",
    @SerializedName("autor_nome") val autorNome: String? = null,
    @SerializedName("autor_avatar") val autorAvatar: String? = null
)



// Motores de IA LivennIA com custo em Livens e categoria.

data class CerebroDto(

    val id: String,

    val nome: String,

    val emoji: String = "",

    val descricao: String = "",

    val premium: Boolean = false,

    @SerializedName("custo_livens", alternate = ["custo_mochi"]) val custoLivens: Int = 0,

    val categoria: String = "Geral",

    @SerializedName("temperatura_padrao") val temperaturaPadrao: Float = 0.85f,

    // false = este modelo é premium e a conta do jogador ainda não tem acesso/Livens.

    val liberado: Boolean = true

) {

    val custoMochi: Int get() = custoLivens

}



data class PersonaRetratoRequest(
    val nome: String? = null,
    val genero: String? = null,
    val aparencia: String? = null,
    val descricao: String? = null,
    val idade: String? = null,
    val interesses: String = "",
    val aversoes: String = "",
    val tags: List<String> = emptyList(),
    @SerializedName("estilo_imagem") val estiloImagem: String = "semi_realistic",
    @SerializedName("detalhes_imagem") val detalhesImagem: String = ""
)

data class CandidatoRetratoDto(
    val indice: Int = 0,
    val seed: Long = 0L,
    val url: String = "",
    @SerializedName("url_direta") val urlDireta: String = ""
)

data class CandidatosRetratoResponse(
    @SerializedName("lote_id") val loteId: String = "",
    val candidatos: List<CandidatoRetratoDto> = emptyList()
)

data class ConfirmarCandidatoRequest(
    @SerializedName("lote_id") val loteId: String,
    val indice: Int,
    val seed: Long? = null
)

data class UploadRetratoRequest(
    @SerializedName("imagem_base64") val imagemBase64: String
)

data class TrocarPersonaJogoRequest(
    @SerializedName("persona_id") val personaId: Int
)

data class PersonaIn(
    val nome: String,
    val genero: String = "Feminino",
    val aparencia: String = "",
    @SerializedName("personalidade") val personalidade: String = "",
    @SerializedName("descricao") val descricao: String = "",
    val idade: String = "",
    val interesses: String = "",
    @SerializedName("medos") val medos: String = "",
    @SerializedName("aversoes") val aversoes: String = "",
    val tags: List<String> = emptyList(),
    @SerializedName("estilo_imagem") val estiloImagem: String = "semi_realistic",
    @SerializedName("detalhes_imagem") val detalhesImagem: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val padrao: Boolean = false
)

data class PersonaResponse(
    val id: Int = 0,
    @SerializedName("jogador_id") val jogadorId: String = "",
    val nome: String = "",
    val genero: String = "",
    val aparencia: String = "",
    @SerializedName("personalidade", alternate = ["descricao"]) val descricao: String = "", // Personalidade
    val idade: String = "",
    val interesses: String = "",
    @SerializedName("medos", alternate = ["aversoes"]) val aversoes: String = "",
    val tags: List<String> = emptyList(),
    @SerializedName("estilo_imagem") val estiloImagem: String = "semi_realistic",
    @SerializedName("detalhes_imagem") val detalhesImagem: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val padrao: Boolean = false,
    @SerializedName("tem_retrato") val temRetrato: Boolean = false,
    @SerializedName("versao_retrato", alternate = ["retrato_versao"]) val versaoRetrato: Int = 1,
    @SerializedName("criado_em") val criadoEm: String = "",
    @SerializedName("atualizado_em") val atualizadoEm: String = ""
)

data class CapaResponse(val capa: Boolean = true)



// Versão enxuta do cenário, como vem dentro de um jogo (sem gênero/sinopse).

data class CenarioDoJogoDto(

    val id: String,

    val nome: String,

    val subtitulo: String = "",

    val emoji: String = "",

    val tags: List<String> = emptyList(),

    val capa: Boolean = false,

    val autorNome: String = "LivennIA",

    val sinopse: String = ""

)



data class CerebroDoJogoDto(

    val id: String,

    val nome: String,

    val emoji: String = "",

    @SerializedName("custo_livens", alternate = ["custo_mochi"]) val custoLivens: Int = 0,

    val categoria: String = "Geral"

) {

    val custoMochi: Int get() = custoLivens

}



data class GeneroInfo(

    val nome: String,

    val emoji: String,

    val descricao: String,

    val ideias: List<String> = emptyList()

)



// Envie "opcao" OU "acao" (o campo nulo não é enviado).

data class TurnoRequest(

    val opcao: Int? = null,

    val acao: String? = null,

    // Identifica a jogada. Se uma tentativa se repetir (rede instável), o servidor

    // percebe que é o mesmo pedido e NÃO cria um turno novo.

    @SerializedName("pedido_id") val pedidoId: String? = null

)



data class ConfigModeloRequest(

    @SerializedName("modelo_id") val modeloId: String? = null,

    val temperatura: Float? = null,

    @SerializedName("persona_id") val personaId: Int? = null

)



data class TrocarVersaoRequest(

    val indice: Int,

    @SerializedName("versao_idx") val versaoIdx: Int

)



data class EditarMensagemRequest(

    val indice: Int,

    val narrativa: String? = null,

    val acao: String? = null

)



data class MemoriaRequest(

    val memoria: String? = null,

    @SerializedName("auto_resumir") val autoResumir: Boolean = false

)



data class SavepointRequest(

    val nome: String? = null

)



data class EventoDto(

    val tipo: String,

    val texto: String

)



data class MensagemDto(

    val acao: String,

    // O gesto do jogador em cena (a IA escreve quando ele toca numa opção). O chat mostra

    // isto no lugar do texto da opção; sem ele, mostra a "acao" mesmo.

    val situacao: String? = null,

    val narrativa: String,

    val versoes: List<String> = emptyList(),

    @SerializedName("versao_ativa") val versaoAtiva: Int = 0,

    val eventos: List<EventoDto> = emptyList(),

    // Horários (epoch em ms) do envio do jogador e da resposta do narrador.

    @SerializedName("em_jogador") val emJogador: Long? = null,

    @SerializedName("em_narrador") val emNarrador: Long? = null

)



data class DiarioDto(

    val id: String,

    val titulo: String,

    val conteudo: String,

    val humor: String = "Reflexivo",

    val turno: Int = 1,

    @SerializedName("criado_em") val criadoEm: Long? = null,

    val lido: Boolean = false

)



data class CartaDto(

    val id: String,

    val remetente: String,

    val titulo: String,

    val conteudo: String,

    val turno: Int = 1,

    @SerializedName("criada_em") val criadaEm: Long? = null,

    val lida: Boolean = false

)



data class SavepointDto(

    val id: String,

    val nome: String,

    val turno: Int,

    val local: String,

    val resumo: String = "",

    @SerializedName("criado_em") val criadoEm: Long? = null

)



data class AutoRespostaResponse(

    val sugestoes: List<String> = emptyList()

)



data class MemoriaResponse(

    val memoria: String = ""

)



data class ResgateLivenResponse(

    val ganho: Int = 0,

    @SerializedName("livens", alternate = ["mochis"]) val livens: Int = 0,

    @SerializedName("ja_resgatado") val jaResgatado: Boolean = false

) {

    val mochis: Int get() = livens

}







data class PersonagemDto(

    val nome: String,

    val vida: Int,

    @SerializedName("vida_max") val vidaMax: Int,

    val inventario: List<String>,

    val local: String

)



data class CenaDto(
    val narrativa: String,
    val opcoes: List<String>,
    @SerializedName("imagem_url") val imagemUrl: String? = null
)

data class IlustracaoCenaResponse(
    @SerializedName("imagem_url") val imagemUrl: String
)



data class JogoResponse(

    @SerializedName("jogo_id") val jogoId: String,

    val modo: String = "rpg",

    val turno: Int,

    @SerializedName("avatar_seed") val avatarSeed: Int = 0,

    val genero: String = "Aventura",

    val personagem: PersonagemDto,

    val cena: CenaDto,

    val historico: List<MensagemDto>? = null,

    @SerializedName("fim_de_jogo") val fimDeJogo: Boolean,

    @SerializedName("jogadas_restantes_hoje") val jogadasRestantesHoje: Int,

    val retrato: Boolean = false,

    // Preenchidos só quando a história começou por um cenário da vitrine.

    val cenario: CenarioDoJogoDto? = null,

    // O "cérebro" de IA que está escrevendo esta aventura.

    val modelo: CerebroDoJogoDto? = null,

    val temperatura: Float = 0.85f,

    val memoria: String = "",

    val diarios: List<DiarioDto> = emptyList(),

    val cartas: List<CartaDto> = emptyList(),

    val savepoints: List<SavepointDto> = emptyList(),

    val party: List<MembroPartyDto> = emptyList(),

    @SerializedName("livens", alternate = ["mochis"]) val livens: Int? = null

) {

    val mochis: Int? get() = livens

}



data class JogoResumo(

    @SerializedName("jogo_id") val jogoId: String,

    val modo: String = "rpg",

    @SerializedName("avatar_seed") val avatarSeed: Int = 0,

    val nome: String,

    val turno: Int,

    val local: String,

    val vida: Int,

    @SerializedName("vida_max") val vidaMax: Int,

    val encerrado: Boolean,

    val genero: String = "Aventura",

    @SerializedName("atualizado_em") val atualizadoEm: String,

    val retrato: Boolean = false,

    @SerializedName("cenario_id") val cenarioId: String? = null,

    // Nome do cenário oficial (quando a história veio da vitrine) e se já tem capa.

    @SerializedName("cenario_nome") val cenarioNome: String? = null,

    val capa: Boolean = false,

    @SerializedName("ultima_narrativa") val ultimaNarrativa: String? = null

)



data class ContaResponse(

    val apelido: String,

    @SerializedName("limite_diario") val limiteDiario: Int,

    @SerializedName("jogadas_hoje") val jogadasHoje: Int,

    @SerializedName("jogadas_restantes_hoje") val jogadasRestantesHoje: Int,

    @SerializedName("jogos_ativos") val jogosAtivos: Int,

    @SerializedName("max_jogos") val maxJogos: Int,

    val premium: Boolean = false,

    @SerializedName("livens", alternate = ["mochis"]) val livens: Int = 100,

    @SerializedName("resgate_diario_disponivel") val resgateDiarioDisponivel: Boolean = true,

    @SerializedName("persona_retrato") val personaRetrato: Boolean = false,
    @SerializedName("dono") val dono: Boolean = false
) {

    val mochis: Int get() = livens

}



data class FeedbackRequest(

    val nota: Int,

    val comentario: String? = null

)



data class DenunciaRequest(

    @SerializedName("jogo_id") val jogoId: String,

    val motivo: String,

    val detalhe: String? = null

)



// POST /jogos/{id}/retrato e POST /jogos/{id}/avatar

data class RetratoResponse(val retrato: Boolean = true)



data class AvatarResponse(@SerializedName("avatar_seed") val avatarSeed: Int)

data class ComentarioDto(
    val id: Long,
    @SerializedName("cenario_id") val cenarioId: String,
    @SerializedName("jogador_id") val jogadorId: Long,
    @SerializedName("autor_nome") val autorNome: String,
    @SerializedName("autor_avatar") val autorAvatar: String = "👤",
    val texto: String,
    val likes: Int = 0,
    @SerializedName("meu_like") val meuLike: Boolean = false,
    @SerializedName("meu_comentario") val meuComentario: Boolean = false,
    @SerializedName("criado_em") val criadoEm: String,
    @SerializedName("editado_em") val editadoEm: String? = null
)

data class NovoComentarioRequest(
    val texto: String
)

data class LikeComentarioResponse(
    val likes: Int,
    @SerializedName("meu_like") val meuLike: Boolean
)

data class PerfilCriadorDto(
    val nome: String,
    val avatar: String = "🎭",
    val uid: String,
    val chats: Int = 0,
    val saves: Int = 0,
    val seguidores: Int = 0,
    val seguindo: Int = 1,
    @SerializedName("esta_seguindo") val estaSeguindo: Boolean = false,
    @SerializedName("esta_bloqueado") val estaBloqueado: Boolean = false,
    val historias: List<CenarioDto> = emptyList()
)

data class SeguirResponse(
    @SerializedName("esta_seguindo") val estaSeguindo: Boolean,
    @SerializedName("total_seguidores") val totalSeguidores: Int
)

data class DenunciaCriadorRequest(
    val motivo: String,
    val trecho: String? = null
)

data class MembroPartyDto(
    val nome: String,
    val papel: String = "Companheiro",
    val emoji: String = "👤",
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val descricao: String = ""
)

data class DadoRequest(
    val lados: Int = 20,
    val modificador: Int = 0,
    val motivo: String? = null
)

data class DadoResponse(
    val lados: Int,
    val resultado: Int,
    val modificador: Int,
    val total: Int,
    val critico: Boolean,
    val desastre: Boolean,
    @SerializedName("texto_formatado") val textoFormatado: String
)