package com.sheenhill.windowlink.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val DarkColorPalette = darkColors(
    primary = MColor.BG1E,
    primaryVariant = PurpleGrey80,
    secondary = Pink80,
    background = MColor.Text333,
    surface = MColor.White
)

private val LightColorPalette = lightColors(
    primary = Purple40,
    primaryVariant = PurpleGrey40,
    secondary = Pink40,
    background = MColor.White,
    surface = MColor.Text333
)


@Composable
fun WindowLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colors.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}