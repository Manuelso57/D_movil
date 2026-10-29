package com.example.ev1.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.ev1.data.model.Usuario
import com.example.ev1.data.repository.RepositorioCloro
import com.example.ev1.notificaciones.Notificaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = RepositorioCloro.obtener(application)

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun iniciarSesion(nombreUsuario: String, clave: String): Boolean {
        val encontrado = repositorio.autenticar(nombreUsuario, clave)
        _usuarioActual.value = encontrado

        val activas = repositorio.alertas.value.count { it.activa }
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

    fun nombreUsuario(id: Int?): String = repositorio.nombreUsuario(id)
}