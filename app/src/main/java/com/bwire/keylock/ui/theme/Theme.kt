package com.bwire.keylock.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * KeyLock Pro Theme
 * Implements the darkest green color scheme with neon green accents
 * Professional, expert-focused appearance for cryptographic operations
 */

private val KeyLockDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DarkestGreen,
    primaryContainer = DarkGreen,
    onPrimaryContainer = BrightNeonGreen,
    
    secondary = MediumGreen,
    onSecondary = TextPrimary,
    secondaryContainer = DarkGreen,
    onSecondaryContainer = TextSecondary,
    
    tertiary = MutedGold,
    onTertiary = DarkestGreen,
    tertiaryContainer = WarningGold,
    onTertiaryContainer = TextPrimary,
    
    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = DestructiveRed,
    onErrorContainer = TextPrimary,
    
    background = DarkestGreen,
    onBackground = TextPrimary,
    
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMedium,
    onSurfaceVariant = TextSecondary,
    
    outline = MediumGreen,
    outlineVariant = DarkGreen,
    
    inverseSurface = TextPrimary,
    inverseOnSurface = DarkestGreen,
    inversePrimary = DarkGreen,
    
    surfaceTint = NeonGreen,
    
    scrim = Color(0x99000000)
)

@Composable
fun KeyLockTheme(
    darkTheme: Boolean = true, // Always dark theme for professional crypto application
    content: @Composable () -> Unit
) {
    val colorScheme = KeyLockDarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = KeyLockTypography,
        content = content
    )
}

/**
 * Extension object to access custom theme attributes
 */
object KeyLockTheme {
    val colorScheme: androidx.compose.material3.ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme
    
    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}
