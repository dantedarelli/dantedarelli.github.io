package br.com.viajarelegal.airbnb.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta transposta do dashboard web "Viajar é Legal" (styles/base.css),
 * para que o aplicativo e o painel de desktop tenham a mesma identidade.
 */

// Marca
val Brand = Color(0xFFFF5A5F)
val BrandDark = Color(0xFFE0484D)
val Brand2 = Color(0xFF00A699)
val Brand2Dark = Color(0xFF00857B)
val Accent = Color(0xFFFFB400)

// Semântica
val Ok = Color(0xFF34C77B)
val Warn = Color(0xFFFFB400)
val Danger = Color(0xFFFF5A5F)
val Info = Color(0xFF4AA3FF)

// Superfícies — tema escuro
val DarkBg = Color(0xFF0E1420)
val DarkBgSoft = Color(0xFF131C2B)
val DarkPanel = Color(0xFF161F2F)
val DarkPanel2 = Color(0xFF1C2739)
val DarkLine = Color(0xFF26334A)
val DarkTxt = Color(0xFFE8EEF7)
val DarkMuted = Color(0xFF93A3BB)
val DarkFaint = Color(0xFF67758E)

// Superfícies — tema claro
val LightBg = Color(0xFFEEF2F7)
val LightBgSoft = Color(0xFFE4EAF3)
val LightPanel = Color(0xFFFFFFFF)
val LightPanel2 = Color(0xFFF5F8FC)
val LightLine = Color(0xFFD5DFEC)
val LightTxt = Color(0xFF172032)
val LightMuted = Color(0xFF5B6B83)
val LightFaint = Color(0xFF8595AD)

/** Escala sequencial de 7 classes usada no heatmap de bairros (fria → quente). */
val SequentialScale = listOf(
    Color(0xFF2B4B8F),
    Color(0xFF2F7FB0),
    Color(0xFF40A9A0),
    Color(0xFF8FC46A),
    Color(0xFFF2C14A),
    Color(0xFFF08A3C),
    Color(0xFFE0453F),
)

/** Cores categóricas atribuídas por índice (tipo de acomodação, cidade, etc.). */
val CategoryScale = listOf(
    Color(0xFF4AA3FF),
    Color(0xFF00A699),
    Color(0xFFFFB400),
    Color(0xFFC77DFF),
    Color(0xFFFF5A5F),
    Color(0xFF34C77B),
)

fun categoryColor(index: Int): Color = CategoryScale[((index % CategoryScale.size) + CategoryScale.size) % CategoryScale.size]
