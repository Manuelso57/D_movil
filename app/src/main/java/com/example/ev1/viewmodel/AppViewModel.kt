package com.example.ev1.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ev1.data.local.AlmacenFotos
import com.example.ev1.data.local.AlmacenLocal
import com.example.ev1.data.model.Accion
import com.example.ev1.data.model.Alerta
import com.example.ev1.data.model.Desviacion
import com.example.ev1.data.model.Estado
import com.example.ev1.data.model.EstadoDesviacion
import com.example.ev1.data.model.Granja
import com.example.ev1.data.model.Medicion
import com.example.ev1.data.model.PuntoMedicion
import com.example.ev1.data.model.Usuario
import com.example.ev1.data.model.calcularEstado
import com.example.ev1.data.remote.ApiCloro
import com.example.ev1.data.remote.FuenteDatos
import com.example.ev1.nativos.Vibracion
import com.example.ev1.notificaciones.Notificaciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DatosServidor(
    val puntos: List<PuntoMedicion>,
    val mediciones: List<Medicion>,
    val alertas: List<Alerta>,
    val desviaciones: List<Desviacion>,
    val acciones: List<Accion>
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val fuente = FuenteDatos.desdeRaw(application)
    private val almacen = AlmacenLocal(application)
    private val api = ApiCloro()

    private val usuarios: List<Usuario> = fuente.obtenerUsuarios()
    private val rangos = fuente.obtenerRangos()
    val granjas: List<Granja> = fuente.obtenerGranjas()

    private val _puntos = MutableStateFlow(fuente.obtenerPuntos())
    val puntos: StateFlow<List<PuntoMedicion>> = _puntos.asStateFlow()

    private val _mediciones = MutableStateFlow(fuente.obtenerMediciones())
    val mediciones: StateFlow<List<Medicion>> = _mediciones.asStateFlow()

    private val _alertas = MutableStateFlow(fuente.obtenerAlertas())
    val alertas: StateFlow<List<Alerta>> = _alertas.asStateFlow()

    private val _desviaciones = MutableStateFlow(fuente.obtenerDesviaciones())
    val desviaciones: StateFlow<List<Desviacion>> = _desviaciones.asStateFlow()

    private val _acciones = MutableStateFlow(fuente.obtenerAcciones())
    val acciones: StateFlow<List<Accion>> = _acciones.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _enLinea = MutableStateFlow<Boolean?>(null)
    val enLinea: StateFlow<Boolean?> = _enLinea.asStateFlow()

    private val _ultimaSync = MutableStateFlow<String?>(null)
    val ultimaSync: StateFlow<String?> = _ultimaSync.asStateFlow()

    private val _ultimoError = MutableStateFlow<String?>(null)
    val ultimoError: StateFlow<String?> = _ultimoError.asStateFlow()

    private var sincronizando = false

    init {
        viewModelScope.launch {
            cargarLocal()
            sincronizar()
        }
    }

    private fun ahora(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

    private suspend fun cargarLocal() {
        try {
            val texto = almacen.cargarTexto() ?: return
            val guardado = FuenteDatos(texto)
            _puntos.value = guardado.obtenerPuntos()
            _mediciones.value = guardado.obtenerMediciones()
            _alertas.value = guardado.obtenerAlertas()
            if (guardado.tiene("desviaciones")) _desviaciones.value = guardado.obtenerDesviaciones()
            if (guardado.tiene("acciones")) _acciones.value = guardado.obtenerAcciones()
        } catch (e: Exception) {
        }
    }

    fun actualizar() {
        viewModelScope.launch { sincronizar() }
    }

    private suspend fun sincronizar() {
        if (sincronizando) return
        sincronizando = true
        _enLinea.value = null
        try {
            val datos = withContext(Dispatchers.IO) {
                enviarPendientes()
                val servidor = FuenteDatos(api.obtenerTodo())
                DatosServidor(
                    puntos = servidor.obtenerPuntos(),
                    mediciones = servidor.obtenerMediciones(),
                    alertas = servidor.obtenerAlertas(),
                    desviaciones = servidor.obtenerDesviaciones(),
                    acciones = servidor.obtenerAcciones()
                )
            }
            _puntos.value = datos.puntos
            _mediciones.value = datos.mediciones
            _alertas.value = datos.alertas
            _desviaciones.value = datos.desviaciones
            _acciones.value = datos.acciones
            _enLinea.value = true
            _ultimoError.value = null
            _ultimaSync.value = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            guardarTodo()
        } catch (e: Exception) {
            Log.e("SyncCloro", "Error al sincronizar", e)
            val hora = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            _ultimoError.value = "[$hora] ${e.javaClass.simpleName}: ${e.message}"
            _enLinea.value = false
            guardarTodo()
        } finally {
            sincronizando = false
        }
    }

    private fun enviarPendientes() {
        val pendientes = _mediciones.value
            .filter { !it.sincronizada }
            .sortedBy { it.fechaHora }

        for (m in pendientes) {
            val idServidor = api.publicarMedicion(m)

            val punto = _puntos.value.firstOrNull { it.id == m.puntoId }
            if (punto != null) {
                api.actualizarPunto(
                    punto.copy(
                        nivelActual = m.nivelCloro,
                        estadoActual = m.estado,
                        ultimaActualizacion = m.fechaHora
                    )
                )
            }

            api.desactivarAlertasDelPunto(m.puntoId)
            val alerta = _alertas.value.firstOrNull { it.medicionId == m.id }
            if (alerta != null) api.publicarAlerta(alerta, idServidor)

            _mediciones.update { lista ->
                lista.map { if (it.id == m.id) it.copy(sincronizada = true) else it }
            }
        }

        for (d in _desviaciones.value.filter { !it.sincronizada }) {
            api.guardarDesviacion(d)
            if (d.estado == EstadoDesviacion.RESUELTA) {
                api.desactivarAlertasDelPunto(d.puntoId)
            }
            _desviaciones.update { lista ->
                lista.map { if (it == d) it.copy(sincronizada = true) else it }
            }
        }

        for (a in _acciones.value.filter { !it.sincronizada }.sortedBy { it.fechaHora }) {
            api.guardarAccion(a)
            _acciones.update { lista ->
                lista.map { if (it == a) it.copy(sincronizada = true) else it }
            }
        }
    }

    fun iniciarSesion(nombreUsuario: String, clave: String): Boolean {
        val encontrado = usuarios.firstOrNull {
            it.usuario == nombreUsuario.trim() && it.clave == clave
        }
        _usuarioActual.value = encontrado

        val activas = _alertas.value.count { it.activa }
        if (encontrado != null && activas > 0) {
            Notificaciones.mostrar(
                getApplication<Application>(),
                0,
                "Alertas de cloro activas",
                "Hay $activas alerta(s) por revisar"
            )
        }
        return encontrado != null
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    fun nombreUsuario(id: Int?): String =
        usuarios.firstOrNull { it.id == id }?.nombre ?: "Sin asignar"

    fun registrarMedicion(puntoId: Int, nivel: Double, observacion: String?): String {
        val ahora = ahora()
        val estado = calcularEstado(nivel, rangos)

        val idMedicion = (_mediciones.value.maxOfOrNull { it.id } ?: 0) + 1
        val medicion = Medicion(
            id = idMedicion,
            puntoId = puntoId,
            nivelCloro = nivel,
            fechaHora = ahora,
            estado = estado,
            origen = "MANUAL",
            observacion = observacion?.takeIf { it.isNotBlank() },
            sincronizada = false
        )
        _mediciones.value = _mediciones.value + medicion

        _puntos.value = _puntos.value.map {
            if (it.id == puntoId) {
                it.copy(nivelActual = nivel, estadoActual = estado, ultimaActualizacion = ahora)
            } else it
        }

        var alertasNuevas = _alertas.value.map {
            if (it.puntoId == puntoId && it.activa) it.copy(activa = false) else it
        }
        if (estado != Estado.NORMAL) {
            val mensaje = when {
                estado == Estado.CRITICO -> "Nivel crítico de cloro libre ($nivel ppm)"
                nivel < rangos.normalMin -> "Cloro libre bajo el rango normal ($nivel ppm)"
                else -> "Cloro libre sobre el rango normal ($nivel ppm)"
            }
            val idAlerta = (alertasNuevas.maxOfOrNull { it.id } ?: 0) + 1
            alertasNuevas = alertasNuevas + Alerta(
                id = idAlerta,
                puntoId = puntoId,
                medicionId = idMedicion,
                estado = estado,
                mensaje = mensaje,
                fechaHora = ahora,
                activa = true
            )

            val nombrePunto = _puntos.value.firstOrNull { it.id == puntoId }?.nombre
                ?: "Punto $puntoId"
            val titulo = if (estado == Estado.CRITICO) "Alerta crítica de cloro" else "Advertencia de cloro"
            Notificaciones.mostrar(
                getApplication<Application>(),
                idAlerta,
                titulo,
                "$nombrePunto: $mensaje"
            )
            Vibracion.vibrar(getApplication<Application>(), estado == Estado.CRITICO)
        }
        _alertas.value = alertasNuevas

        guardarTodo()
        actualizar()
        return estado
    }

    fun crearDesviacion(alertaId: Int): Int? {
        val alerta = _alertas.value.firstOrNull { it.id == alertaId } ?: return null

        val existente = _desviaciones.value.firstOrNull {
            it.alertaId == alertaId && it.estado != EstadoDesviacion.RESUELTA
        }
        if (existente != null) return existente.id

        val nombrePunto = _puntos.value.firstOrNull { it.id == alerta.puntoId }?.nombre
            ?: "Punto ${alerta.puntoId}"
        val nueva = Desviacion(
            id = (_desviaciones.value.maxOfOrNull { it.id } ?: 0) + 1,
            alertaId = alertaId,
            puntoId = alerta.puntoId,
            descripcion = "$nombrePunto: ${alerta.mensaje}",
            estado = EstadoDesviacion.PENDIENTE,
            fechaDeteccion = alerta.fechaHora,
            responsableId = null,
            sincronizada = false
        )
        _desviaciones.value = _desviaciones.value + nueva

        guardarTodo()
        actualizar()
        return nueva.id
    }

    fun cambiarEstadoDesviacion(desviacionId: Int, nuevoEstado: String) {
        val actual = _desviaciones.value.firstOrNull { it.id == desviacionId } ?: return

        _desviaciones.update { lista ->
            lista.map {
                if (it.id == desviacionId) {
                    it.copy(
                        estado = nuevoEstado,
                        responsableId = if (nuevoEstado == EstadoDesviacion.EN_GESTION) {
                            _usuarioActual.value?.id ?: it.responsableId
                        } else it.responsableId,
                        sincronizada = false
                    )
                } else it
            }
        }

        if (nuevoEstado == EstadoDesviacion.RESUELTA) {
            _alertas.update { lista ->
                lista.map {
                    if (it.puntoId == actual.puntoId && it.activa) it.copy(activa = false) else it
                }
            }
        }

        guardarTodo()
        actualizar()
    }

    fun registrarAccion(desviacionId: Int, descripcion: String, foto: Bitmap? = null) {
        val usuario = _usuarioActual.value ?: return
        if (descripcion.isBlank()) return

        val fecha = ahora()

        viewModelScope.launch {
            val ruta = if (foto != null) {
                withContext(Dispatchers.IO) {
                    AlmacenFotos.guardar(
                        getApplication<Application>(),
                        foto,
                        "accion_${System.currentTimeMillis()}"
                    )
                }
            } else {
                null
            }

            _acciones.update { lista ->
                val id = (lista.maxOfOrNull { it.id } ?: 0) + 1
                lista + Accion(
                    id = id,
                    desviacionId = desviacionId,
                    usuarioId = usuario.id,
                    descripcion = descripcion.trim(),
                    fechaHora = fecha,
                    sincronizada = false,
                    fotoRuta = ruta
                )
            }

            guardarTodo()
            actualizar()
        }
    }

    private fun guardarTodo() {
        viewModelScope.launch {
            try {
                almacen.guardar(
                    _puntos.value,
                    _mediciones.value,
                    _alertas.value,
                    _desviaciones.value,
                    _acciones.value
                )
            } catch (e: Exception) {
            }
        }
    }
}