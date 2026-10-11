package eu.elarreglador.storage.reserve.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Colores semánticos para estados
private val SuccessGreen = Color(0xFF34A853)
private val WarningAmber = Color(0xFFFBBC04)
private val SuccessGreenDark = Color(0xFF4ADE80)
private val WarningAmberDark = Color(0xFFFFD600)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4285F4),
    secondary = Color(0xFF34A853),
    tertiary = Color(0xFFFBBC04),
    surface = Color.White,
    background = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSurface = Color.Black,
    onBackground = Color.Black,
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFFFFF8E1),
    onTertiaryContainer = Color(0xFFF57F17),
    outline = Color(0xFF757575)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    secondary = Color(0xFF66BB6A),
    tertiary = Color(0xFFFFD600),
    surface = Color(0xFF1E1E1E),
    background = Color(0xFF121212),
    onPrimary = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White,
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFE8F0FE),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFE8F5E9),
    tertiaryContainer = Color(0xFFF57F17),
    onTertiaryContainer = Color(0xFFFFF8E1),
    outline = Color(0xFFB0B0B0)
)

private val LocalIsDark = compositionLocalOf { false }

@Composable
fun ReserveTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(LocalIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = androidx.compose.material3.Typography(),
            content = content
        )
    }
}

@Composable
fun SuccessColor(): Color {
    val isDark = LocalIsDark.current
    return if (isDark) SuccessGreenDark else SuccessGreen
}

@Composable
fun WarningColor(): Color {
    val isDark = LocalIsDark.current
    return if (isDark) WarningAmberDark else WarningAmber
}