package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ObsidianColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = TextPrimary,
    primaryContainer = SoftIndigoContainer,
    onPrimaryContainer = VoidCanvas,
    secondary = CyberViolet,
    onSecondary = TextPrimary,
    secondaryContainer = DeepVioletContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = PhotonBlue,
    onTertiary = TextPrimary,
    background = VoidCanvas,
    onBackground = TextPrimary,
    surface = SurfaceElevation1,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevation2,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceElevation1,
    surfaceContainerHigh = SurfaceElevation2,
    surfaceContainerHighest = SurfaceElevation3,
    outline = TranslucentBorder,
    outlineVariant = SubtleBorder,
    error = StatusError,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark-first computational environment
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = VoidCanvas.toArgb()
                window.navigationBarColor = VoidCanvas.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = AppTypography,
        content = content
    )
}
