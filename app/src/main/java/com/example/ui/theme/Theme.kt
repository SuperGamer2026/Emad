package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun MyApplicationTheme(
    themePreset: String = "EXPRESSIVE",
    content: @Composable () -> Unit,
) {
    val chosenColors = when (themePreset) {
        "COSMIC" -> CosmicNoirThemeColors
        "TEAL" -> MidnightTealThemeColors
        "ROSE" -> SunsetRoseThemeColors
        "GOLDEN" -> GoldenOasisThemeColors
        "EMERALD" -> IceEmeraldThemeColors
        "OCEAN" -> PacificSeaThemeColors
        "AMYTHIST" -> VelvetAmethystThemeColors
        "SUNFIRE" -> SolarFlareThemeColors
        "SAPPHIRE" -> NeonSapphireThemeColors
        "LAVENDER" -> LavenderFieldThemeColors
        "MINT" -> PolarGlacierThemeColors
        "FOREST" -> PineEvergreenThemeColors
        "BLOOD_MOON" -> CrimsonMoonThemeColors
        "MONOCHROME" -> PureSlateThemeColors
        "SAKURA" -> TokyoSakuraThemeColors
        "NEBULA" -> NebulaSparkThemeColors
        "DESERT" -> SaharaNomadThemeColors
        "AURORA" -> BorealLightsThemeColors
        else -> ExpressiveThemeColors
    }

    val materialDynamicScheme = darkColorScheme(
        primary = chosenColors.primary,
        secondary = chosenColors.secondary,
        tertiary = chosenColors.success,
        background = chosenColors.background,
        surface = chosenColors.surface,
        onPrimary = chosenColors.onPrimary,
        onBackground = chosenColors.onBackground,
        onSurface = chosenColors.onBackground
    )

    CompositionLocalProvider(LocalAppColors provides chosenColors) {
        MaterialTheme(
            colorScheme = materialDynamicScheme,
            typography = Typography,
            content = content
        )
    }
}
