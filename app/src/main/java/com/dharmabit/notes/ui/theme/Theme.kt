package com.dharmabit.notes.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Gray900,
    onPrimary = PureWhite,
    primaryContainer = Gray200,
    onPrimaryContainer = Gray900,

    secondary = AccentSteel,
    onSecondary = PureWhite,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray900,

    tertiary = AccentCharcoal,
    onTertiary = PureWhite,

    background = PrimaryWhite,
    onBackground = Gray900,

    surface = PureWhite,
    onSurface = Gray900,
    surfaceVariant = Gray50,
    onSurfaceVariant = Gray700,

    surfaceContainerHighest = Gray100,
    surfaceContainerHigh = Gray50,
    surfaceContainer = PureWhite,
    surfaceContainerLow = Gray50,
    surfaceContainerLowest = PureWhite,

    outline = Gray300,
    outlineVariant = Gray200,

    error = ErrorLight,
    onError = PureWhite,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),

    scrim = OverlayLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Gray100,
    onPrimary = Gray900,
    primaryContainer = DarkGray50,
    onPrimaryContainer = Gray100,

    secondary = AccentSilver,
    onSecondary = Gray900,
    secondaryContainer = DarkGray100,
    onSecondaryContainer = Gray200,

    tertiary = Gray400,
    onTertiary = Gray900,

    background = PrimaryBlack,
    onBackground = Gray100,

    surface = DarkGray300,
    onSurface = Gray100,
    surfaceVariant = DarkGray200,
    onSurfaceVariant = Gray300,

    surfaceContainerHighest = DarkGray50,
    surfaceContainerHigh = DarkGray100,
    surfaceContainer = DarkGray200,
    surfaceContainerLow = DarkGray300,
    surfaceContainerLowest = PrimaryBlack,

    outline = DarkGray50,
    outlineVariant = DarkGray100,

    error = ErrorDark,
    onError = Gray900,
    errorContainer = Color(0xFF5D1F1F),
    onErrorContainer = Color(0xFFFFCDD2),

    scrim = OverlayDark
)

@Composable
fun NotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}