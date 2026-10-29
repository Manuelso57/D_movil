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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ev1.data.model.Alerta
import com.example.ev1.data.model.Desviacion
import com.example.ev1.data.model.Estado
import com.example.ev1.data.model.EstadoDesviacion
import com.example.ev1.data.model.PuntoMedicion

@Composable
fun AlertasScreen(
    alertas: List<Alerta>,
    puntos: List<PuntoMedicion>,
    desviaciones: List<Desviacion>,
    puedeCrearDesviacion: Boolean,
    onAlertaClick: (puntoId: Int) -> Unit,
    onCrearDesviacion: (alertaId: Int) -> Unit,
    onVerDesviacion: (desviacionId: Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activas = alertas
        .filter { it.activa }
        .sortedWith(
            compareByDescending<Alerta> { it.estado == Estado.CRITICO }
                .thenByDescending { it.fechaHora }
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Alertas activas",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "${activas.size} alerta(s) por revisar",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (activas.isEmpty()) {
            EstadoVacio(
                mensaje = "No hay alertas activas. Todos los puntos están dentro del rango.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(activas, key = { it.id }) { alerta ->
                    val punto = puntos.firstOrNull { it.id == alerta.puntoId }
                    val desviacion = desviaciones.firstOrNull {
                        it.alertaId == alerta.id && it.estado != EstadoDesviacion.RESUELTA
                    }
                    Card(
                        onClick = { onAlertaClick(alerta.puntoId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = punto?.nombre ?: "Punto ${alerta.puntoId}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = alerta.mensaje,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = alerta.fechaHora.replace("T", " "),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Toca la tarjeta para ver el historial del punto",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                EtiquetaEstado(estado = alerta.estado)
                            }

                            if (desviacion != null) {
                                TextButton(onClick = { onVerDesviacion(desviacion.id) }) {
                                    Text("Ver desviación")
                                }
                            } else if (puedeCrearDesviacion) {
                                TextButton(onClick = { onCrearDesviacion(alerta.id) }) {
                                    Text("Crear desviación")
                                }
                            }
                        }
                    }
                }
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