package com.kindred.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Rose80,
    secondary = Plum80,
    tertiary = Sand80,
)

private val LightColors = lightColorScheme(
    primary = Rose40,
    secondary = Plum40,
    tertiary = Sand40,
)

@Composable
fun KindredTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
