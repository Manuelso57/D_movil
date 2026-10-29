package com.example.ev1.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ev1.data.model.Alerta
import com.example.ev1.data.model.Desviacion
import com.example.ev1.data.model.Estado
import com.example.ev1.data.model.EstadoDesviacion
import com.example.ev1.data.model.PuntoMedicion

@Composable
fun DashboardScreen(
    puntos: List<PuntoMedicion>,
    alertas: List<Alerta>,
    desviaciones: List<Desviacion>,
    onPuntoClick: (puntoId: Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var barraVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { barraVisible = true }
    val progresoBarra by animateFloatAsState(
        targetValue = if (barraVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "progresoBarra"
    )

    val normales = puntos.count { it.estadoActual == Estado.NORMAL }
    val advertencias = puntos.count { it.estadoActual == Estado.ADVERTENCIA }
    val criticos = puntos.count { it.estadoActual == Estado.CRITICO }

    val alertasActivas = alertas.count { it.activa }
    val pendientes = desviaciones.count { it.estado == EstadoDesviacion.PENDIENTE }
    val enGestion = desviaciones.count { it.estado == EstadoDesviacion.EN_GESTION }
    val abiertas = pendientes + enGestion

    val enAtencion = puntos
        .filter { it.estadoActual != Estado.NORMAL }
        .sortedByDescending { it.estadoActual == Estado.CRITICO }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Resumen general",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Calidad del agua en todas las granjas",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estado de los puntos (${puntos.size})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (puntos.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(progresoBarra)
                                    .padding(vertical = 12.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                if (normales > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(normales.toFloat())
                                            .fillMaxHeight()
                                            .background(colorEstado(Estado.NORMAL))
                                    )
                                }
                                if (advertencias > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(advertencias.toFloat())
                                            .fillMaxHeight()
                                            .background(colorEstado(Estado.ADVERTENCIA))
                                    )
                                }
                                if (criticos > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(criticos.toFloat())
                                            .fillMaxHeight()
                                            .background(colorEstado(Estado.CRITICO))
                                    )
                                }
                            }
                        }

                        FilaEstado(Estado.NORMAL, "Normal", normales)
                        FilaEstado(Estado.ADVERTENCIA, "Advertencia", advertencias)
                        FilaEstado(Estado.CRITICO, "Crítico", criticos)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AnimatedContent(targetState = alertasActivas, label = "contadorAlertas") { valor ->
                                Text(
                                    text = "$valor",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            Text(
                                text = "Alertas activas",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AnimatedContent(targetState = abiertas, label = "contadorDesviaciones") { valor ->
                                Text(
                                    text = "$valor",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            Text(
                                text = "Desviaciones abiertas",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Pendientes: $pendientes · En gestión: $enGestion",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Puntos que requieren atención",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (enAtencion.isEmpty()) {
                item { Text("Todos los puntos están dentro del rango.") }
            }

            items(enAtencion, key = { it.id }) { punto ->
                Card(
                    onClick = { onPuntoClick(punto.id) },
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
                                text = punto.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Cloro libre: ${punto.nivelActual} ppm",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        EtiquetaEstado(estado = punto.estadoActual)
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

@Composable
private fun FilaEstado(estado: String, nombre: String, cantidad: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(colorEstado(estado), CircleShape)
        )
        Text(
            text = "$nombre: $cantidad",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}