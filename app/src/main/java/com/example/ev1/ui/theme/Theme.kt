package com.example.ev1.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulAgua,
    onPrimary = Color.White,
    secondary = VerdeAgua,
    onSecondary = Color.White,
    tertiary = GrisAzulado,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = FondoClaro,
    onSurface = TextoClaro,
    onSurfaceVariant = TextoSecundarioClaro,
    surfaceContainerHighest = TarjetaClara
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulAguaOscuro,
    onPrimary = Color(0xFF00344D),
    secondary = VerdeAguaOscuro,
    onSecondary = Color(0xFF00332E),
    tertiary = GrisAzuladoOscuro,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = FondoOscuro,
    onSurface = TextoOscuro,
    onSurfaceVariant = TextoSecundarioOscuro,
    surfaceContainerHighest = TarjetaOscura
)

@Composable
fun Ev1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
