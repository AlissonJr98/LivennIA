package com.livennia.app

import com.google.gson.annotations.SerializedName

// Estes modelos espelham exatamente o JSON que o servidor envia e recebe.

data class RegistroRequest(
    @SerializedName("codigo_convite") val codigoConvite: String,
    val apelido: String
)

data class FirebaseAuthIn(
    @SerializedName("id_token") val idToken: String
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
    // "Cérebro" de IA escolhido (veja /cerebros). Nulo = o padrão do servidor.
    @SerializedName("modelo_id") val modeloId: String? = null,
    val temperatura: Float? = null
)

// ---- Vitrine (aba Explorar) ----
// O "prompt secreto" de cada cenário fica só no servidor: aqui chega o que aparece.
data class CenarioDto(
    val id: String,
    val nome: String,
    val subtitulo: String = "",
    val emoji: String = "",
    val genero: String = "Aventura",
    val tags: List<String> = emptyList(),
    val sinopse: String = "",
    val abertura: String = "",
    // true quando a arte de capa já foi gerada e está salva no servidor.
    val capa: Boolean = false
)

// Os "cérebros"/sabores de IA (inspirados no Emochi), com custo em Mochis e categoria.
data class CerebroDto(
    val id: String,
    val nome: String,
    val emoji: String = "",
    val descricao: String = "",
    val premium: Boolean = false,
    @SerializedName("custo_mochi") val custoMochi: Int = 0,
    val categoria: String = "Geral",
    @SerializedName("temperatura_padrao") val temperaturaPadrao: Float = 0.85f,
    // false = este modelo é premium e a conta do jogador ainda não tem acesso/Mochis.
    val liberado: Boolean = true
)

data class CapaResponse(val capa: Boolean = true)

// Versão enxuta do cenário, como vem dentro de um jogo (sem gênero/sinopse).
data class CenarioDoJogoDto(
    val id: String,
    val nome: String,
    val subtitulo: String = "",
    val emoji: String = "",
    val tags: List<String> = emptyList(),
    val capa: Boolean = false
)

data class CerebroDoJogoDto(
    val id: String,
    val nome: String,
    val emoji: String = "",
    @SerializedName("custo_mochi") val custoMochi: Int = 0,
    val categoria: String = "Geral"
)

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
    val temperatura: Float? = null
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

data class ResgateMochiResponse(
    val ganho: Int = 0,
    val mochis: Int = 0,
    @SerializedName("ja_resgatado") val jaResgatado: Boolean = false
)

data class PersonagemDto(
    val nome: String,
    val vida: Int,
    @SerializedName("vida_max") val vidaMax: Int,
    val inventario: List<String>,
    val local: String
)

data class CenaDto(
    val narrativa: String,
    val opcoes: List<String>
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
    val mochis: Int? = null
)

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
    val mochis: Int = 100,
    @SerializedName("resgate_diario_disponivel") val resgateDiarioDisponivel: Boolean = true
)

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