package com.example.ev1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun MensajeError(mensaje: String?) {
    var ultimo by remember { mutableStateOf("") }
    if (mensaje != null) ultimo = mensaje

    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn(tween(250)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
    ) {
        Text(
            text = ultimo,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun AvisoError(mensaje: String?, modifier: Modifier = Modifier) {
    var ultimo by remember { mutableStateOf("") }
    if (mensaje != null) ultimo = mensaje

    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn(tween(250)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(150)),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = ultimo,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun CampoFormulario(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    mostrarExito: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val iconoFinal: (@Composable () -> Unit)? = when {
        error != null -> {
            {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error en el campo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        mostrarExito && valor.isNotBlank() -> {
            {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Campo válido",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        else -> null
    }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(imageVector = icono, contentDescription = null) },
        trailingIcon = iconoFinal,
        isError = error != null,
        supportingText = { MensajeError(error) },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth()
    )
}