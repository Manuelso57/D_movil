package com.example.ev1.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.ev1.data.model.Accion
import com.example.ev1.data.model.Desviacion
import com.example.ev1.data.repository.RepositorioCloro
import kotlinx.coroutines.flow.StateFlow

class DesviacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = RepositorioCloro.obtener(application)

    val desviaciones: StateFlow<List<Desviacion>> = repositorio.desviaciones
    val acciones: StateFlow<List<Accion>> = repositorio.acciones

    fun crearDesviacion(alertaId: Int): Int? = repositorio.crearDesviacion(alertaId)

    fun cambiarEstado(desviacionId: Int, nuevoEstado: String, usuarioId: Int?) {
        repositorio.cambiarEstadoDesviacion(desviacionId, nuevoEstado, usuarioId)
    }

    fun registrarAccion(desviacionId: Int, descripcion: String, foto: Bitmap?, usuarioId: Int) {
        repositorio.registrarAccion(desviacionId, descripcion, foto, usuarioId)
    }
}