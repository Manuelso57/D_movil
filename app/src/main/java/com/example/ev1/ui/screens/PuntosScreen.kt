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
import com.example.ev1.data.model.Granja
import com.example.ev1.data.model.PuntoMedicion

@Composable
fun PuntosScreen(
    granja: Granja?,
    puntos: List<PuntoMedicion>,
    onPuntoClick: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = granja?.nombre ?: "Granja",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Puntos de medición",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(puntos, key = { it.id }) { punto ->
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
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Actualizado: ${punto.ultimaActualizacion.replace("T", " ")}",
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