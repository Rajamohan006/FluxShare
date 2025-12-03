package com.rajamohan.fluxshare.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object FluxShareColors {
    // Primary Colors
    val Primary = Color(0xFF0B60FF)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFD8E2FF)
    val OnPrimaryContainer = Color(0xFF001849)

    // Secondary Colors
    val Secondary = Color(0xFF00BFA6)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFA7F3E5)
    val OnSecondaryContainer = Color(0xFF002019)

    // Background & Surface
    val Background = Color(0xFFFAFBFF)
    val OnBackground = Color(0xFF1A1C1E)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1A1C1E)
    val SurfaceVariant = Color(0xFFE1E2EC)
    val OnSurfaceVariant = Color(0xFF44474F)

    // Error
    val Error = Color(0xFFD93025)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFDE7E5)
    val OnErrorContainer = Color(0xFF410E0B)

    // Success
    val Success = Color(0xFF16A34A)
    val OnSuccess: Color = Color(0xFFFFFFFF)
    val SuccessContainer: Color = Color(0xFFBBF7D0)
    val OnSuccessContainer: Color = Color(0xFF052E16)

    // Warning
    val Warning: Color = Color(0xFFF59E0B)
    val OnWarning: Color = Color(0xFFFFFFFF)
    val WarningContainer: Color = Color(0xFFFEF3C7)
    val OnWarningContainer: Color = Color(0xFF78350F)

    // Muted Text
    val MutedText: Color = Color(0xFF6B7280)

    // Dark Theme Colors
    val DarkBackground = Color(0xFF0F1724)
    val DarkSurface = Color(0xFF1A1F2E)
    val DarkOnBackground = Color(0xFFE2E8F0)
    val DarkOnSurface = Color(0xFFE2E8F0)
    val DarkSurfaceVariant = Color(0xFF2D3748)
    val DarkPrimary = Color(0xFF4C9AFF)
}

// ========== Light Color Scheme ==========
val LightColorScheme = lightColorScheme(
    primary = FluxShareColors.Primary,
    onPrimary = FluxShareColors.OnPrimary,
    primaryContainer = FluxShareColors.PrimaryContainer,
    onPrimaryContainer = FluxShareColors.OnPrimaryContainer,

    secondary = FluxShareColors.Secondary,
    onSecondary = FluxShareColors.OnSecondary,
    secondaryContainer = FluxShareColors.SecondaryContainer,
    onSecondaryContainer = FluxShareColors.OnSecondaryContainer,

    background = FluxShareColors.Background,
    onBackground = FluxShareColors.OnBackground,
    surface = FluxShareColors.Surface,
    onSurface = FluxShareColors.OnSurface,
    surfaceVariant = FluxShareColors.SurfaceVariant,
    onSurfaceVariant = FluxShareColors.OnSurfaceVariant,

    error = FluxShareColors.Error,
    onError = FluxShareColors.OnError,
    errorContainer = FluxShareColors.ErrorContainer,
    onErrorContainer = FluxShareColors.OnErrorContainer
)

// ========== Dark Color Scheme ==========
val DarkColorScheme = darkColorScheme(
    primary = FluxShareColors.DarkPrimary,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = FluxShareColors.PrimaryContainer,

    secondary = FluxShareColors.Secondary,
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = FluxShareColors.SecondaryContainer,

    background = FluxShareColors.DarkBackground,
    onBackground = FluxShareColors.DarkOnBackground,
    surface = FluxShareColors.DarkSurface,
    onSurface = FluxShareColors.DarkOnSurface,
    surfaceVariant = FluxShareColors.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = Color(0xFFFFDAD6)
)