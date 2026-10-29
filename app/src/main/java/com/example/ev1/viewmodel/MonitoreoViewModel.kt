package com.example.ev1.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.ev1.data.model.Alerta
import com.example.ev1.data.model.Estado
import com.example.ev1.data.model.Granja
import com.example.ev1.data.model.Medicion
import com.example.ev1.data.model.PuntoMedicion
import com.example.ev1.data.repository.RepositorioCloro
import com.example.ev1.nativos.Vibracion
import com.example.ev1.notificaciones.Notificaciones
import kotlinx.coroutines.flow.StateFlow

class MonitoreoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = RepositorioCloro.obtener(application)

    val granjas: List<Granja> = repositorio.granjas
    val puntos: StateFlow<List<PuntoMedicion>> = repositorio.puntos
    val mediciones: StateFlow<List<Medicion>> = repositorio.mediciones
    val alertas: StateFlow<List<Alerta>> = repositorio.alertas
    val enLinea: StateFlow<Boolean?> = repositorio.enLinea
    val ultimaSync: StateFlow<String?> = repositorio.ultimaSync
    val ultimoError: StateFlow<String?> = repositorio.ultimoError

    fun actualizar() {
        repositorio.actualizar()
    }

    fun registrarMedicion(puntoId: Int, nivel: Double, observacion: String?): String {
        val resultado = repositorio.registrarMedicion(puntoId, nivel, observacion)
        val alerta = resultado.alertaNueva

        if (alerta != null) {
            val critica = resultado.estado == Estado.CRITICO
            val titulo = if (critica) "Alerta crítica de cloro" else "Advertencia de cloro"
            Notificaciones.mostrar(
                getApplication<Application>(),
                alerta.id,
                titulo,
                "${resultado.nombrePunto}: ${alerta.mensaje}"
            )
            Vibracion.vibrar(getApplication<Application>(), critica)
        }
        return resultado.estado
    }
}