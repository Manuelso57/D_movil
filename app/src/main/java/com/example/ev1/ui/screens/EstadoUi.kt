package com.example.ev1.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ev1.data.model.Estado

fun colorEstado(estado: String): Color = when (estado) {
    Estado.NORMAL -> Color(0xFF2E7D32)
    Estado.ADVERTENCIA -> Color(0xFFF9A825)
    else -> Color(0xFFC62828)
}

fun textoEstado(estado: String): String = when (estado) {
    Estado.NORMAL -> "Normal"
    Estado.ADVERTENCIA -> "Advertencia"
    else -> "Crítico"
}

@Composable
fun EtiquetaEstado(estado: String, modifier: Modifier = Modifier) {
    val colorFondo by animateColorAsState(
        targetValue = colorEstado(estado),
        animationSpec = tween(durationMillis = 500),
        label = "colorEstado"
    )
    val pulso = rememberInfiniteTransition(label = "pulsoEstado")
    val opacidad by pulso.animateFloat(
        initialValue = 1f,
        targetValue = if (estado == Estado.CRITICO) 0.55f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "opacidadEstado"
    )

    Box(
        modifier = modifier
            .alpha(opacidad)
            .background(
                color = colorFondo,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = textoEstado(estado),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}