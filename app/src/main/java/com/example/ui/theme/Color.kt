package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Define the theme colors data class for custom presets
data class AppColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val onPrimary: Color,
    val onBackground: Color,
    val grayMuted: Color,
    val success: Color
)

// Preset 1: Google's Material 3 Expressive (Default)
val ExpressiveThemeColors = AppColors(
    primary = Color(0xFF6366F1),      // Vibrant Indigo
    secondary = Color(0xFFF97316),    // Vibrant Energetic Orange
    background = Color(0xFF0F0E17),   // Deep dark midnight
    surface = Color(0xFF1E1B2E),      // Deep expressive purple-grey
    onPrimary = Color.White,
    onBackground = Color(0xFFF1F5F9), // Slidely off-white / slate-100
    grayMuted = Color(0xFF94A3B8),    // Cool gray
    success = Color(0xFF10B981)       // Vivid mint
)

// Preset 2: Cosmic Noir (Classic Sci-Fi Minimalist)
val CosmicNoirThemeColors = AppColors(
    primary = Color(0xFFFF3344),      // Crimson Neon
    secondary = Color(0xFFFFB300),    // Amber Glow
    background = Color(0xFF0A0A0A),   // Deep Black
    surface = Color(0xFF161616),      // Charcoal
    onPrimary = Color.White,
    onBackground = Color(0xFFE5E5E5), // OffWhite
    grayMuted = Color(0xFF6B6B6B),    // GrayMuted
    success = Color(0xFF10B981)       // GreenSuccess
)

// Preset 3: Midnight Teal
val MidnightTealThemeColors = AppColors(
    primary = Color(0xFF06B6D4),      // Neon Cyan
    secondary = Color(0xFFF43F5E),    // Vibrant Rose
    background = Color(0xFF051111),   // Ultra-dark teal-black
    surface = Color(0xFF0E2222),      // Teal charcoal
    onPrimary = Color.White,
    onBackground = Color(0xFFE2E8F0), 
    grayMuted = Color(0xFF64748B),
    success = Color(0xFF10B981)
)

// Preset 4: Sunset Rose
val SunsetRoseThemeColors = AppColors(
    primary = Color(0xFFEC4899),      // Deep Pink/Rose
    secondary = Color(0xFFFBBF24),    // Amber-Gold Glow
    background = Color(0xFF0F0A0E),   // Deep Blackberry
    surface = Color(0xFF21151E),      // Plum-charcoal
    onPrimary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    grayMuted = Color(0xFF8E8A94),
    success = Color(0xFF10B981)
)

// Preset 5: Golden Oasis
val GoldenOasisThemeColors = AppColors(
    primary = Color(0xFFD4AF37),      // Classic Gold
    secondary = Color(0xFFF59E0B),    // Amber Light
    background = Color(0xFF0D0C06),   // Deep Dark Gold Hue
    surface = Color(0xFF1E1C12),      // Dark Antique Gold Surface
    onPrimary = Color.Black,
    onBackground = Color(0xFFF3F4F6),
    grayMuted = Color(0xFF9CA3AF),
    success = Color(0xFF10B981)
)

// Preset 6: Ice Emerald
val IceEmeraldThemeColors = AppColors(
    primary = Color(0xFF10B981),      // Mint Emerald
    secondary = Color(0xFF34D399),    // Jade Bright
    background = Color(0xFF050B08),   // Dark Green Midnight
    surface = Color(0xFF0D1C14),      // Deep Green Surface
    onPrimary = Color.White,
    onBackground = Color(0xFFE6F4EA),
    grayMuted = Color(0xFF81C784),
    success = Color(0xFF2E7D32)
)

// Preset 7: Pacific Sea
val PacificSeaThemeColors = AppColors(
    primary = Color(0xFF3B82F6),      // Vivid Blue
    secondary = Color(0xFF60A5FA),    // Light Sky Blue
    background = Color(0xFF040814),   // Deep Ocean Trench
    surface = Color(0xFF0D1730),      // Submarine Steel
    onPrimary = Color.White,
    onBackground = Color(0xFFE0F2FE),
    grayMuted = Color(0xFF7DD3FC),
    success = Color(0xFF34D399)
)

// Preset 8: Velvet Amethyst
val VelvetAmethystThemeColors = AppColors(
    primary = Color(0xFF8B5CF6),      // Rich Violet
    secondary = Color(0xFFD8B4FE),    // Pale Lavender
    background = Color(0xFF0C0816),   // Mystic Dark Velvet
    surface = Color(0xFF1E1435),      // Purple Charcoal
    onPrimary = Color.White,
    onBackground = Color(0xFFF3E8FF),
    grayMuted = Color(0xFFC084FC),
    success = Color(0xFF10B981)
)

// Preset 9: Solar Flare
val SolarFlareThemeColors = AppColors(
    primary = Color(0xFFEF4444),      // Neon Red
    secondary = Color(0xFFF97316),    // Bright Orange
    background = Color(0xFF0F0505),   // Volcano Black
    surface = Color(0xFF261010),      // Obsidian embers
    onPrimary = Color.White,
    onBackground = Color(0xFFFEE2E2),
    grayMuted = Color(0xFFFCA5A5),
    success = Color(0xFF10B981)
)

// Preset 10: Neon Sapphire
val NeonSapphireThemeColors = AppColors(
    primary = Color(0xFF2563EB),      // Bright Royal Blue
    secondary = Color(0xFFA5F3FC),    // Turquoise Flare
    background = Color(0xFF02040A),   // Electric Abyss
    surface = Color(0xFF0E1428),      // Deep Tech Surface
    onPrimary = Color.White,
    onBackground = Color(0xFFE0F2FE),
    grayMuted = Color(0xFF93C5FD),
    success = Color(0xFF10B981)
)

// Preset 11: Lavender Field
val LavenderFieldThemeColors = AppColors(
    primary = Color(0xFFA78BFA),      // Pastel Lavender
    secondary = Color(0xFFF472B6),    // Blossom Pink
    background = Color(0xFF0B0810),   // Sweet Dusk
    surface = Color(0xFF1A1326),      // Lavender Charcoal
    onPrimary = Color.Black,
    onBackground = Color(0xFFF2EAFA),
    grayMuted = Color(0xFFD6BCFA),
    success = Color(0xFF34D399)
)

// Preset 12: Polar Glacier
val PolarGlacierThemeColors = AppColors(
    primary = Color(0xFF06B6D4),      // Cyan Ice
    secondary = Color(0xFF99F6E4),    // Aquamarine
    background = Color(0xFF02090B),   // Glacial Abyss
    surface = Color(0xFF091D22),      // Frost Steel
    onPrimary = Color.White,
    onBackground = Color(0xFFE0F7FA),
    grayMuted = Color(0xFF26C6DA),
    success = Color(0xFF00E676)
)

// Preset 13: Pine Evergreen
val PineEvergreenThemeColors = AppColors(
    primary = Color(0xFF4ADE80),      // Emerald Glow
    secondary = Color(0xFFFACC15),    // Sun Amber
    background = Color(0xFF030A05),   // Forest Shadow
    surface = Color(0xFF0A1F0F),      // Pine Moss
    onPrimary = Color.Black,
    onBackground = Color(0xFFECFDF5),
    grayMuted = Color(0xFF6EE7B7),
    success = Color(0xFFA7F3D0)
)

// Preset 14: Crimson Moon
val CrimsonMoonThemeColors = AppColors(
    primary = Color(0xFFDC2626),      // Royal Blood Red
    secondary = Color(0xFF9CA3AF),    // Titanium Silver
    background = Color(0xFF0A0202),   // Lunar eclipse core
    surface = Color(0xFF1B0B0B),      // Crimson Coffin
    onPrimary = Color.White,
    onBackground = Color(0xFFFEE2E2),
    grayMuted = Color(0xFFEF4444),
    success = Color(0xFF10B981)
)

// Preset 15: Pure Slate
val PureSlateThemeColors = AppColors(
    primary = Color(0xFFE2E8F0),      // Clear Slate
    secondary = Color(0xFF94A3B8),    // Muted Lead
    background = Color(0xFF0B0C0E),   // Monochrome Obsidian
    surface = Color(0xFF1A1C1F),      // Steel Gray
    onPrimary = Color.Black,
    onBackground = Color(0xFFF8FAFC),
    grayMuted = Color(0xFF64748B),
    success = Color(0xFF10B981)
)

// Preset 16: Tokyo Sakura
val TokyoSakuraThemeColors = AppColors(
    primary = Color(0xFFFBCFE8),      // Soft Blossom Pink
    secondary = Color(0xFFFDA4AF),    // Spring Rose
    background = Color(0xFF0C0709),   // Night Hanami
    surface = Color(0xFF1F1216),      // Plumwood Tea House
    onPrimary = Color.Black,
    onBackground = Color(0xFFFFF1F2),
    grayMuted = Color(0xFFF472B6),
    success = Color(0xFF10B981)
)

// Preset 17: Nebula Spark
val NebulaSparkThemeColors = AppColors(
    primary = Color(0xFFC084FC),      // Nebula Purple
    secondary = Color(0xFF818CF8),    // Stellar Indigos
    background = Color(0xFF080512),   // Dark Cosmic Vacuum
    surface = Color(0xFF16102D),      // Pulsar Dust
    onPrimary = Color.White,
    onBackground = Color(0xFFFAF5FF),
    grayMuted = Color(0xFFA78BFA),
    success = Color(0xFF34D399)
)

// Preset 18: Sahara Nomad
val SaharaNomadThemeColors = AppColors(
    primary = Color(0xFFF59E0B),      // Desert Sand
    secondary = Color(0xFFD97706),    // Clay Glow
    background = Color(0xFF0A0704),   // Oasis Dusk
    surface = Color(0xFF1D140B),      // Nomad Tent Leather
    onPrimary = Color.Black,
    onBackground = Color(0xFFFEF3C7),
    grayMuted = Color(0xFFFBBF24),
    success = Color(0xFF10B981)
)

// Preset 19: Boreal Lights
val BorealLightsThemeColors = AppColors(
    primary = Color(0xFF2DD4BF),      // Aurora Teal
    secondary = Color(0xFFC084FC),    // Starry Violet
    background = Color(0xFF030A09),   // Arctic Solitude
    surface = Color(0xFF0C1F1C),      // Northern Canopy
    onPrimary = Color.Black,
    onBackground = Color(0xFFF0FDFA),
    grayMuted = Color(0xFF5EEAD4),
    success = Color(0xFF10B981)
)

val LocalAppColors = staticCompositionLocalOf { ExpressiveThemeColors }

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}
