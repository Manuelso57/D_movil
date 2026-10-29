package com.example.ev1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ev1.data.model.Estado
import com.example.ev1.data.model.PuntoMedicion
import com.example.ev1.viewmodel.RegistroUiState

@Composable
fun RegistroScreen(
    punto: PuntoMedicion?,
    estado: RegistroUiState,
    onNivelChange: (String) -> Unit,
    onObservacionChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Registrar medición",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = punto?.nombre ?: "Punto de medición",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(visible = estado.estadoGuardado == null) {
                Column {
                    CampoFormulario(
                        valor = estado.nivel,
                        onValorChange = onNivelChange,
                        etiqueta = "Cloro libre (ppm)",
                        icono = Icons.Default.Info,
                        error = estado.errorNivel,
                        mostrarExito = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    CampoFormulario(
                        valor = estado.observacion,
                        onValorChange = onObservacionChange,
                        etiqueta = "Observaciones (opcional)",
                        icono = Icons.Default.Edit,
                        error = estado.errorObservacion,
                        singleLine = false,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Button(
                        onClick = onGuardar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Guardar medición")
                    }
                }
            }

            AnimatedVisibility(
                visible = estado.estadoGuardado != null,
                enter = fadeIn(tween(400)) + scaleIn(tween(400))
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Medición guardada",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(text = "${estado.nivelGuardado} ppm")
                        }
                        EtiquetaEstado(estado = estado.estadoGuardado ?: Estado.NORMAL)
                    }
                }
            }

        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(if (estado.estadoGuardado == null) "Cancelar" else "Volver")
        }
    }
}