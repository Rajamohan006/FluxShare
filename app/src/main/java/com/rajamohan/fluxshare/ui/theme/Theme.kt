package com.rajamohan.fluxshare.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
@Composable
fun FluxShareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FluxShareTypography,
        shapes = FluxShareShapes,
        content = content
    )
}

// ========== Extension Properties for Custom Colors ==========
val ColorScheme.success: androidx.compose.ui.graphics.Color
    @Composable get() = if (isSystemInDarkTheme()) FluxShareColors.Success else FluxShareColors.Success

val ColorScheme.onSuccess: Color
    @Composable get() = FluxShareColors.OnSuccess

val ColorScheme.successContainer: Color
    @Composable get() = FluxShareColors.SuccessContainer

val ColorScheme.onSuccessContainer: Color
    @Composable get() = FluxShareColors.OnSuccessContainer

val ColorScheme.warning: Color
    @Composable get() = FluxShareColors.Warning

val ColorScheme.onWarning: Color
    @Composable get() = FluxShareColors.OnWarning

val ColorScheme.warningContainer: Color
    @Composable get() = FluxShareColors.WarningContainer

val ColorScheme.onWarningContainer: Color
    @Composable get() = FluxShareColors.OnWarningContainer

val ColorScheme.mutedText: Color
    @Composable get() = FluxShareColors.MutedText
