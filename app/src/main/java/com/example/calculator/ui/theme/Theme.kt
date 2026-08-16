package com.example.calculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C83FD),
    secondary = Color(0xFFFE8D6F),
    tertiary = Color(0xFFB0BEC5),
    background = Color(0xFF1B1B2F),
    surface = Color(0xFF23233C),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF1B1B2F),
    onBackground = Color(0xFFECECF4),
    onSurface = Color(0xFFECECF4)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3D4CFF),
    secondary = Color(0xFFFB6D48),
    tertiary = Color(0xFF546E7A),
    background = Color(0xFFF7F7FB),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1B1B2F),
    onSurface = Color(0xFF1B1B2F)
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}