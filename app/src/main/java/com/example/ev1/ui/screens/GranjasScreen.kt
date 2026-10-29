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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ev1.data.model.Estado
import com.example.ev1.data.model.Granja
import com.example.ev1.data.model.PuntoMedicion
import com.example.ev1.data.model.Usuario

@Composable
fun GranjasScreen(
    usuario: Usuario,
    granjas: List<Granja>,
    puntos: List<PuntoMedicion>,
    alertasActivas: Int,
    desviacionesAbiertas: Int,
    enLinea: Boolean?,
    ultimaSync: String?,
    ultimoError: String?,
    onGranjaClick: (Int) -> Unit,
    onVerAlertas: () -> Unit,
    onVerDesviaciones: () -> Unit,
    onVerResumen: () -> Unit,
    onActualizar: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Hola, ${usuario.nombre}",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Perfil: ${usuario.rol}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = when (enLinea) {
                true -> "Conectado · actualizado a las ${ultimaSync ?: "--:--"}"
                false -> "Sin conexión · mostrando datos guardados"
                null -> "Conectando…"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (enLinea == false) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (enLinea == null) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
        if (enLinea == false && ultimoError != null) {
            Text(
                text = "Detalle: $ultimoError",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        OutlinedButton(
            onClick = onActualizar,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text("Actualizar")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(granjas, key = { it.id }) { granja ->
                val puntosGranja = puntos.filter { it.granjaId == granja.id }
                val normales = puntosGranja.count { it.estadoActual == Estado.NORMAL }
                val advertencia = puntosGranja.count { it.estadoActual == Estado.ADVERTENCIA }
                val criticos = puntosGranja.count { it.estadoActual == Estado.CRITICO }

                Card(
                    onClick = { onGranjaClick(granja.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = granja.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = granja.ubicacion,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Puntos: ${puntosGranja.size}",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Normal: $normales  |  Advertencia: $advertencia  |  Crítico: $criticos"
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onVerAlertas,
                modifier = Modifier.weight(1f)
            ) {
                Text("Alertas ($alertasActivas)")
            }
            Button(
                onClick = onVerDesviaciones,
                modifier = Modifier.weight(1f)
            ) {
                Text("Desviaciones ($desviacionesAbiertas)")
            }
        }

        OutlinedButton(
            onClick = onVerResumen,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Resumen general")
        }

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}