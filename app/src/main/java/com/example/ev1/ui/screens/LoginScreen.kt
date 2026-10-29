package com.example.ev1.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ev1.viewmodel.LoginUiState
import kotlin.math.roundToInt

@Composable
fun LoginScreen(
    estado: LoginUiState,
    onUsuarioChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onIngresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val desplazamiento = remember { Animatable(0f) }

    LaunchedEffect(estado.intentosFallidos) {
        if (estado.intentosFallidos > 0) {
            repeat(3) {
                desplazamiento.animateTo(14f, tween(60))
                desplazamiento.animateTo(-14f, tween(60))
            }
            desplazamiento.animateTo(0f, tween(60))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Medición de cloro",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(desplazamiento.value.roundToInt(), 0) }
            ) {
                CampoFormulario(
                    valor = estado.usuario,
                    onValorChange = onUsuarioChange,
                    etiqueta = "Usuario",
                    icono = Icons.Default.Person,
                    error = estado.errorUsuario
                )
                CampoFormulario(
                    valor = estado.clave,
                    onValorChange = onClaveChange,
                    etiqueta = "Clave",
                    icono = Icons.Default.Lock,
                    error = estado.errorClave,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.padding(top = 4.dp)
                )

                AvisoError(
                    mensaje = estado.errorGeneral,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Button(
                    onClick = onIngresar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Ingresar")
                }
            }

            Text(
                text = "Prueba con: operario, supervisor o jefatura (clave 1234)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}