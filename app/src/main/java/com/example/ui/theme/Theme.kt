package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun buildColorScheme(isDark: Boolean, themeId: String): ColorScheme {
    return when (themeId.uppercase()) {
        "SKY" -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF38BDF8),
                onPrimary = Color(0xFF082F49),
                primaryContainer = SkyContainer,
                onPrimaryContainer = Color(0xFFE0F2FE),
                secondary = Color(0xFF818CF8),
                onSecondary = Color(0xFF1E1B4B),
                background = SlateDarkBg,
                surface = SlateDarkSurface,
                surfaceVariant = SlateDarkSurfaceVariant,
                onBackground = Color(0xFFF1F5F9),
                onSurface = Color(0xFFF8FAFC)
            )
        } else {
            lightColorScheme(
                primary = SkyPrimary,
                onPrimary = Color.White,
                primaryContainer = SkyLightContainer,
                onPrimaryContainer = Color(0xFF0369A1),
                secondary = Color(0xFF4F46E5),
                onSecondary = Color.White,
                background = SlateLightBg,
                surface = SlateLightSurface,
                surfaceVariant = SlateLightSurfaceVariant,
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A)
            )
        }
        "VIOLET" -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFA78BFA),
                onPrimary = Color(0xFF2E1065),
                primaryContainer = VioletContainer,
                onPrimaryContainer = Color(0xFFEDE9FE),
                secondary = Color(0xFFC084FC),
                onSecondary = Color(0xFF3B0764),
                background = Color(0xFF0B0914),
                surface = Color(0xFF161224),
                surfaceVariant = Color(0xFF241D38),
                onBackground = Color(0xFFF5F3FF),
                onSurface = Color(0xFFFAF5FF)
            )
        } else {
            lightColorScheme(
                primary = VioletPrimary,
                onPrimary = Color.White,
                primaryContainer = VioletLightContainer,
                onPrimaryContainer = Color(0xFF5B21B6),
                secondary = Color(0xFF9333EA),
                onSecondary = Color.White,
                background = SlateLightBg,
                surface = SlateLightSurface,
                surfaceVariant = Color(0xFFF5F3FF),
                onBackground = Color(0xFF1E1B4B),
                onSurface = Color(0xFF1E1B4B)
            )
        }
        "AMBER" -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFFBBF24),
                onPrimary = Color(0xFF451A03),
                primaryContainer = AmberContainer,
                onPrimaryContainer = Color(0xFFFEF3C7),
                secondary = Color(0xFFFB923C),
                onSecondary = Color(0xFF431407),
                background = Color(0xFF0D0B08),
                surface = Color(0xFF1A1612),
                surfaceVariant = Color(0xFF2B221B),
                onBackground = Color(0xFFFFFBEB),
                onSurface = Color(0xFFFFFDF5)
            )
        } else {
            lightColorScheme(
                primary = AmberPrimaryDark,
                onPrimary = Color.White,
                primaryContainer = AmberLightContainer,
                onPrimaryContainer = Color(0xFF92400E),
                secondary = Color(0xFFEA580C),
                onSecondary = Color.White,
                background = SlateLightBg,
                surface = SlateLightSurface,
                surfaceVariant = Color(0xFFFFFBEB),
                onBackground = Color(0xFF451A03),
                onSurface = Color(0xFF451A03)
            )
        }
        "ROSE" -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFFB7185),
                onPrimary = Color(0xFF4C0519),
                primaryContainer = RoseContainer,
                onPrimaryContainer = Color(0xFFFFE4E6),
                secondary = Color(0xFFF43F5E),
                onSecondary = Color(0xFF4C0519),
                background = Color(0xFF0E080A),
                surface = Color(0xFF1C1014),
                surfaceVariant = Color(0xFF2E1920),
                onBackground = Color(0xFFFFF1F2),
                onSurface = Color(0xFFFFF1F2)
            )
        } else {
            lightColorScheme(
                primary = RosePrimary,
                onPrimary = Color.White,
                primaryContainer = RoseLightContainer,
                onPrimaryContainer = Color(0xFF9F1239),
                secondary = Color(0xFFBE123C),
                onSecondary = Color.White,
                background = SlateLightBg,
                surface = SlateLightSurface,
                surfaceVariant = Color(0xFFFFF1F2),
                onBackground = Color(0xFF4C0519),
                onSurface = Color(0xFF4C0519)
            )
        }
        "MONOCHROME" -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFE2E8F0),
                onPrimary = Color(0xFF0F172A),
                primaryContainer = MonoContainer,
                onPrimaryContainer = Color(0xFFF1F5F9),
                secondary = Color(0xFFCBD5E1),
                onSecondary = Color(0xFF0F172A),
                background = Color(0xFF020617),
                surface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFF1E293B),
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF8FAFC)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF1E293B),
                onPrimary = Color.White,
                primaryContainer = MonoLightContainer,
                onPrimaryContainer = Color(0xFF0F172A),
                secondary = Color(0xFF475569),
                onSecondary = Color.White,
                background = Color(0xFFF8FAFC),
                surface = Color.White,
                surfaceVariant = Color(0xFFE2E8F0),
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A)
            )
        }
        else -> { // EMERALD (Default)
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFF34D399),
                    onPrimary = Color(0xFF022C22),
                    primaryContainer = EmeraldContainer,
                    onPrimaryContainer = Color(0xFFD1FAE5),
                    secondary = Color(0xFF6EE7B7),
                    onSecondary = Color(0xFF064E3B),
                    background = SlateDarkBg,
                    surface = SlateDarkSurface,
                    surfaceVariant = SlateDarkSurfaceVariant,
                    onBackground = Color(0xFFF1F5F9),
                    onSurface = Color(0xFFF8FAFC)
                )
            } else {
                lightColorScheme(
                    primary = EmeraldPrimary,
                    onPrimary = Color.White,
                    primaryContainer = EmeraldLightContainer,
                    onPrimaryContainer = Color(0xFF065F46),
                    secondary = EmeraldPrimaryDark,
                    onSecondary = Color.White,
                    background = SlateLightBg,
                    surface = SlateLightSurface,
                    surfaceVariant = SlateLightSurfaceVariant,
                    onBackground = Color(0xFF0F172A),
                    onSurface = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorThemeId: String = "EMERALD",
    content: @Composable () -> Unit
) {
    val colorScheme = buildColorScheme(darkTheme, colorThemeId)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
