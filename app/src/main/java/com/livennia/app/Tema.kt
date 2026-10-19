package com.livennia.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow

// ---------------------------------------------------------------
// Dois estilos visuais, escolhidos nas configurações:
//   LIVRO — pergaminho, ouro envelhecido e títulos serifados (clima de livro antigo).
//   NEON  — fundo preto, UMA cor viva de destaque e fonte sem serifa (app moderno).
// ---------------------------------------------------------------
enum class EstiloTema { LIVRO, NEON }

// No estilo Neon a regra é "fundo neutro + uma única cor de destaque": tudo que era
// amarelo (botões, chips, barra de progresso) passa a usar a cor escolhida aqui.
// Cada cor tem duas versões: a viva (para o fundo preto) e uma mais funda, legível
// sobre fundo claro. É a mesma cor, só escurecida o bastante para dar contraste.
enum class CorNeon(val rotulo: String, val cor: Color, val corClara: Color) {
    AMARELO("Amarelo", Color(0xFFF5D90A), Color(0xFF8F6B00)),
    CIANO("Ciano", Color(0xFF22D3EE), Color(0xFF0E7490)),
    ROXO("Roxo", Color(0xFFA855F7), Color(0xFF7E22CE)),
    VERMELHO("Vermelho", Color(0xFFFF4D4D), Color(0xFFC02626))
}

// Paleta interna: cada estilo tem a sua. As telas usam os atalhos abaixo
// (Fundo, Superficie, Ouro...) e recebem a cor certa conforme o tema ativo.
class Paleta(
    val fundo: Color,
    val superficie: Color,
    val superficieAlta: Color,
    val textoPrincipal: Color,
    val destaque: Color,
    val acento: Color,
    val textoSuave: Color,
    val contorno: Color,
    val gradienteTopo: Color,
    val erro: Color,
    val escuro: Boolean
)

private val PaletaLivro = Paleta(
    fundo = Color(0xFF0D0B0A),
    superficie = Color(0xFF181310),
    superficieAlta = Color(0xFF241C17),
    textoPrincipal = Color(0xFFE8DCC8),
    destaque = Color(0xFFC9A36A),
    acento = Color(0xFF9E2B2B),
    textoSuave = Color(0xFFA89A86),
    contorno = Color(0xFF5A4A3C),
    gradienteTopo = Color(0xFF1A1410),
    erro = Color(0xFFE57373),
    escuro = true
)

private val PaletaLivroClara = Paleta(
    fundo = Color(0xFFF5EFE6),
    superficie = Color(0xFFFBF7F0),
    superficieAlta = Color(0xFFE8DCC8),
    textoPrincipal = Color(0xFF1A120B),
    destaque = Color(0xFF8B6914),
    acento = Color(0xFF9E2B2B),
    textoSuave = Color(0xFF5A4A3C),
    contorno = Color(0xFF8B7355),
    gradienteTopo = Color(0xFFEDE4D5),
    erro = Color(0xFFB00020),
    escuro = false
)

// O Neon fala sempre a mesma língua (fundo neutro + UMA cor viva), mas tem as duas
// versões: no escuro a cor é a viva; no claro, o tom fundo da mesma cor.
private fun paletaNeon(corDestaque: CorNeon, escuro: Boolean): Paleta = if (escuro) {
    Paleta(
        fundo = Color(0xFF0A0A0A),
        superficie = Color(0xFF161616),
        superficieAlta = Color(0xFF212121),
        textoPrincipal = Color(0xFFFFFFFF),
        destaque = corDestaque.cor,
        acento = Color(0xFFFF4D4D),
        textoSuave = Color(0xFF9C9C9C),
        contorno = Color(0xFF323232),
        gradienteTopo = Color(0xFF141414),
        erro = Color(0xFFFF6B6B),
        escuro = true
    )
} else {
    Paleta(
        fundo = Color(0xFFF6F6F8),
        superficie = Color(0xFFFFFFFF),
        superficieAlta = Color(0xFFEDEDF1),
        textoPrincipal = Color(0xFF0B0B0C),
        destaque = corDestaque.corClara,
        acento = Color(0xFFC02626),
        textoSuave = Color(0xFF6A6A72),
        contorno = Color(0xFFDCDCE0),
        gradienteTopo = Color(0xFFE8E8EC),
        erro = Color(0xFFC62828),
        escuro = false
    )
}

private val LocalPaleta = staticCompositionLocalOf { paletaNeon(CorNeon.AMARELO, escuro = true) }

// Atalhos usados nas telas. São lidos dentro de composables e resolvem
// para a cor do tema ativo.
val Fundo: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.fundo

val Superficie: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.superficie

val SuperficieAlta: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.superficieAlta

val Pergaminho: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.textoPrincipal

val Ouro: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.destaque

val Sangue: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.acento

val TextoSuave: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.textoSuave

val Contorno: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.contorno

val GradienteTopo: Color
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.gradienteTopo

val TemaEscuroAtivo: Boolean
    @Composable @ReadOnlyComposable get() = LocalPaleta.current.escuro

// ---------------------------------------------------------------
// Cantos. No app de referência quase nada é "quadrado": tudo é pílula ou canto bem
// arredondado, e a separação entre os blocos vem do tom e da sombra, não de bordas.
// Usar sempre estes valores mantém o app com a mesma linguagem em todas as telas.
// ---------------------------------------------------------------
object Cantos {
    /** Cartões e blocos de conteúdo. */
    val cartao = RoundedCornerShape(24.dp)

    /** Cartões grandes, modais e folhas. */
    val grande = RoundedCornerShape(30.dp)

    /** Campos de texto e caixas de seleção. */
    val campo = RoundedCornerShape(20.dp)

    /** Imagens e capas. */
    val imagem = RoundedCornerShape(24.dp)

    /** Botões, chips, barras e o que deve virar pílula (metade da altura). */
    val pilula = RoundedCornerShape(50)
}

// Títulos com fonte serifada (clima de livro antigo); o resto usa a fonte padrão do sistema.
private val TipografiaLivro = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        letterSpacing = 4.sp,
        lineHeight = 48.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 2.sp,
        lineHeight = 38.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp)
)

// Fonte sem serifa, títulos mais fortes (estilo app moderno).
private val TipografiaNeon = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, lineHeight = 15.sp)
)

private fun esquemaDe(p: Paleta) = if (p.escuro) {
    darkColorScheme(
        primary = p.destaque,
        onPrimary = if (p.destaque.luminance() > 0.5f) Color(0xFF101010) else p.fundo,
        secondary = p.acento,
        onSecondary = p.textoPrincipal,
        background = p.fundo,
        onBackground = p.textoPrincipal,
        surface = p.superficie,
        onSurface = p.textoPrincipal,
        surfaceVariant = p.superficieAlta,
        onSurfaceVariant = p.textoSuave,
        error = p.erro,
        onError = p.fundo,
        outline = p.contorno
    )
} else {
    lightColorScheme(
        primary = p.destaque,
        onPrimary = if (p.destaque.luminance() > 0.5f) Color(0xFF101010) else p.fundo,
        secondary = p.acento,
        onSecondary = p.textoPrincipal,
        background = p.fundo,
        onBackground = p.textoPrincipal,
        surface = p.superficie,
        onSurface = p.textoPrincipal,
        surfaceVariant = p.superficieAlta,
        onSurfaceVariant = p.textoSuave,
        error = p.erro,
        onError = p.fundo,
        outline = p.contorno
    )
}

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

private fun paletaDe(estilo: EstiloTema, escuro: Boolean, corNeon: CorNeon): Paleta = when (estilo) {
    EstiloTema.NEON -> paletaNeon(corNeon, escuro)
    EstiloTema.LIVRO -> if (escuro) PaletaLivro else PaletaLivroClara
}

@Composable
fun TemaLivennIA(
    estilo: EstiloTema = EstiloTema.NEON,
    escuro: Boolean = true,
    corNeon: CorNeon = CorNeon.AMARELO,
    conteudo: @Composable () -> Unit
) {
    val paleta = paletaDe(estilo, escuro, corNeon)
    val tipografia = if (estilo == EstiloTema.LIVRO) TipografiaLivro else TipografiaNeon
    CompositionLocalProvider(LocalPaleta provides paleta) {
        MaterialTheme(
            colorScheme = esquemaDe(paleta),
            typography = tipografia,
            content = conteudo
        )
    }
}
