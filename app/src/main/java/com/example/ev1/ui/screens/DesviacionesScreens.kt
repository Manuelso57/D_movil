package com.example.ev1.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ev1.data.local.AlmacenFotos
import com.example.ev1.data.model.Accion
import com.example.ev1.data.model.Desviacion
import com.example.ev1.data.model.EstadoDesviacion
import com.example.ev1.data.model.PuntoMedicion
import com.example.ev1.viewmodel.AccionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun colorDesviacion(estado: String): Color = when (estado) {
    EstadoDesviacion.RESUELTA -> Color(0xFF2E7D32)
    EstadoDesviacion.EN_GESTION -> Color(0xFF1565C0)
    else -> Color(0xFFE65100)
}

private fun textoDesviacion(estado: String): String = when (estado) {
    EstadoDesviacion.RESUELTA -> "Resuelta"
    EstadoDesviacion.EN_GESTION -> "En gestión"
    else -> "Pendiente"
}

@Composable
private fun EtiquetaDesviacion(estado: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = colorDesviacion(estado),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = textoDesviacion(estado),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun DesviacionesScreen(
    desviaciones: List<Desviacion>,
    puntos: List<PuntoMedicion>,
    onDesviacionClick: (Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ordenadas = desviaciones.sortedWith(
        compareBy<Desviacion> { it.estado == EstadoDesviacion.RESUELTA }
            .thenByDescending { it.fechaDeteccion }
    )
    val abiertas = desviaciones.count { it.estado != EstadoDesviacion.RESUELTA }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Desviaciones",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "$abiertas abierta(s) de ${desviaciones.size}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (ordenadas.isEmpty()) {
            EstadoVacio(
                mensaje = "No hay desviaciones registradas.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(ordenadas, key = { it.id }) { desviacion ->
                    val punto = puntos.firstOrNull { it.id == desviacion.puntoId }
                    Card(
                        onClick = { onDesviacionClick(desviacion.id) },
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
                                    text = punto?.nombre ?: "Punto ${desviacion.puntoId}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = desviacion.descripcion,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "Detectada: ${desviacion.fechaDeteccion.replace("T", " ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            EtiquetaDesviacion(estado = desviacion.estado)
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

@Composable
fun DetalleDesviacionScreen(
    desviacion: Desviacion?,
    punto: PuntoMedicion?,
    acciones: List<Accion>,
    formulario: AccionUiState,
    nombreUsuario: (Int?) -> String,
    puedeGestionar: Boolean,
    puedeRegistrarAccion: Boolean,
    onCambiarEstado: (String) -> Unit,
    onTextoChange: (String) -> Unit,
    onFotoCapturada: (Bitmap) -> Unit,
    onQuitarFoto: () -> Unit,
    onPermisoCamara: (Boolean) -> Unit,
    onRegistrarAccion: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { imagen ->
        if (imagen != null) onFotoCapturada(imagen)
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        onPermisoCamara(concedido)
        if (concedido) camara.launch(null)
    }

    if (desviacion == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Esta desviación no está disponible.",
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
        return
    }

    val ordenadas = acciones.sortedBy { it.fechaHora }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
    ) {
        Text(
            text = "Desviación #${desviacion.id}",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = punto?.nombre ?: "Punto ${desviacion.puntoId}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Estado",
                                style = MaterialTheme.typography.titleMedium
                            )
                            EtiquetaDesviacion(estado = desviacion.estado)
                        }
                        Text(
                            text = desviacion.descripcion,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Detectada: ${desviacion.fechaDeteccion.replace("T", " ")}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Responsable: ${nombreUsuario(desviacion.responsableId)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Acciones registradas (${ordenadas.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (ordenadas.isEmpty()) {
                item { Text("Aún no hay acciones registradas.") }
            }

            items(ordenadas, key = { it.id }) { accion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = accion.descripcion)
                        Text(
                            text = "${nombreUsuario(accion.usuarioId)} · ${accion.fechaHora.replace("T", " ")}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        MiniaturaEvidencia(ruta = accion.fotoRuta)
                    }
                }
            }
        }

        if (puedeGestionar) {
            when (desviacion.estado) {
                EstadoDesviacion.PENDIENTE -> Button(
                    onClick = { onCambiarEstado(EstadoDesviacion.EN_GESTION) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) { Text("Poner en gestión") }

                EstadoDesviacion.EN_GESTION -> Button(
                    onClick = { onCambiarEstado(EstadoDesviacion.RESUELTA) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) { Text("Marcar como resuelta") }

                else -> OutlinedButton(
                    onClick = { onCambiarEstado(EstadoDesviacion.EN_GESTION) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) { Text("Reabrir") }
            }
        }

        if (puedeRegistrarAccion && desviacion.estado != EstadoDesviacion.RESUELTA) {
            CampoFormulario(
                valor = formulario.texto,
                onValorChange = onTextoChange,
                etiqueta = "Nueva acción correctiva",
                icono = Icons.Default.Edit,
                error = formulario.errorTexto,
                mostrarExito = true,
                singleLine = false,
                modifier = Modifier.padding(top = 8.dp)
            )

            formulario.foto?.let { imagen ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        bitmap = imagen.asImageBitmap(),
                        contentDescription = "Foto de evidencia",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(72.dp)
                            .weight(1f)
                    )
                    TextButton(onClick = onQuitarFoto) {
                        Text("Quitar foto")
                    }
                }
            }

            AvisoError(
                mensaje = if (formulario.permisoDenegado) {
                    "Permiso de cámara denegado. Actívalo en los ajustes de la app para adjuntar fotos."
                } else {
                    null
                },
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val concedido = ContextCompat.checkSelfPermission(
                            contexto,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (concedido) {
                            camara.launch(null)
                        } else {
                            pedirPermiso.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tomar foto")
                }
                Button(
                    onClick = onRegistrarAccion,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Registrar")
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Volver")
        }
    }
}

@Composable
private fun MiniaturaEvidencia(ruta: String?) {
    if (!ruta.isNullOrBlank()) {
        val imagen by produceState<Bitmap?>(null, ruta) {
            value = withContext(Dispatchers.IO) { AlmacenFotos.cargar(ruta) }
        }
        imagen?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Foto de evidencia",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }
    }
}