package br.com.viajarelegal.airbnb.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Cores que o Material 3 não cobre bem (painéis, linhas, texto tênue)
 * ficam neste objeto para que os gráficos e cartões fiquem consistentes.
 */
data class AppPalette(
    val panel: Color,
    val panel2: Color,
    val line: Color,
    val muted: Color,
    val faint: Color,
    val ok: Color,
    val warn: Color,
    val danger: Color,
    val info: Color,
    val isDark: Boolean,
)

val LocalAppPalette = staticCompositionLocalOf {
    AppPalette(
        panel = DarkPanel,
        panel2 = DarkPanel2,
        line = DarkLine,
        muted = DarkMuted,
        faint = DarkFaint,
        ok = Ok,
        warn = Warn,
        danger = Danger,
        info = Info,
        isDark = true,
    )
}

private val DarkScheme = darkColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandDark,
    onPrimaryContainer = Color.White,
    secondary = Brand2,
    onSecondary = Color.White,
    secondaryContainer = Brand2Dark,
    onSecondaryContainer = Color.White,
    tertiary = Accent,
    onTertiary = Color(0xFF241A00),
    background = DarkBg,
    onBackground = DarkTxt,
    surface = DarkPanel,
    onSurface = DarkTxt,
    surfaceVariant = DarkPanel2,
    onSurfaceVariant = DarkMuted,
    surfaceContainer = DarkBgSoft,
    surfaceContainerHigh = DarkPanel2,
    outline = DarkLine,
    outlineVariant = Color(0xFF1F2A3D),
    error = Danger,
    onError = Color.White,
)

private val LightScheme = lightColorScheme(
    primary = BrandDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD9),
    onPrimaryContainer = Color(0xFF410004),
    secondary = Brand2Dark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8EFE9),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Color(0xFF8A6100),
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTxt,
    surface = LightPanel,
    onSurface = LightTxt,
    surfaceVariant = LightPanel2,
    onSurfaceVariant = LightMuted,
    surfaceContainer = LightBgSoft,
    surfaceContainerHigh = LightPanel2,
    outline = LightLine,
    outlineVariant = Color(0xFFE4EBF4),
    error = Color(0xFFC0393D),
    onError = Color.White,
)

@Composable
fun ViajarELegalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val palette = if (darkTheme) {
        AppPalette(DarkPanel, DarkPanel2, DarkLine, DarkMuted, DarkFaint, Ok, Warn, Danger, Info, true)
    } else {
        AppPalette(LightPanel, LightPanel2, LightLine, LightMuted, LightFaint, Color(0xFF1E9E5E), Color(0xFFB07800), Color(0xFFC0393D), Color(0xFF2D7FD1), false)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = scheme.background.toArgb()
            window.navigationBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content,
        )
    }
}
