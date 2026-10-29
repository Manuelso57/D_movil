package com.example.ev1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ev1.data.model.Medicion
import com.example.ev1.data.model.PuntoMedicion

@Composable
fun HistorialScreen(
    punto: PuntoMedicion?,
    mediciones: List<Medicion>,
    puedeRegistrar: Boolean,
    onRegistrar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ordenadas = mediciones.sortedByDescending { it.fechaHora }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = punto?.nombre ?: "Punto de medición",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Historial de mediciones",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (ordenadas.isEmpty()) {
            EstadoVacio(
                mensaje = "Este punto aún no tiene mediciones.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(ordenadas, key = { it.id }) { medicion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${medicion.nivelCloro} ppm",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = medicion.fechaHora.replace("T", " "),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = if (medicion.origen == "MANUAL") "Registro manual" else "Sistema de monitoreo",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (!medicion.observacion.isNullOrBlank()) {
                                    Text(
                                        text = "Obs.: ${medicion.observacion}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            EtiquetaEstado(estado = medicion.estado)
                        }
                    }
                }
            }
        }

        if (puedeRegistrar) {
            Button(
                onClick = onRegistrar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Registrar medición")
            }
        }

        Button(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Volver")
        }
    }
}