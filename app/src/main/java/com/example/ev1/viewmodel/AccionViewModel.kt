package com.example.ev1.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.ev1.validacion.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AccionUiState(
    val texto: String = "",
    val foto: Bitmap? = null,
    val errorTexto: String? = null,
    val permisoDenegado: Boolean = false
)

class AccionViewModel : ViewModel() {

    private val _estado = MutableStateFlow(AccionUiState())
    val estado: StateFlow<AccionUiState> = _estado.asStateFlow()

    fun cambiarTexto(valor: String) {
        _estado.update {
            it.copy(
                texto = valor,
                errorTexto = if (valor.isBlank()) null else Validadores.accion(valor)
            )
        }
    }

    fun asignarFoto(foto: Bitmap) {
        _estado.update { it.copy(foto = foto) }
    }

    fun quitarFoto() {
        _estado.update { it.copy(foto = null) }
    }

    fun registrarPermiso(concedido: Boolean) {
        _estado.update { it.copy(permisoDenegado = !concedido) }
    }

    fun enviar(registrar: (String, Bitmap?) -> Unit) {
        val actual = _estado.value
        val error = Validadores.accion(actual.texto)

        if (error != null) {
            _estado.update { it.copy(errorTexto = error) }
            return
        }

        registrar(actual.texto.trim(), actual.foto)
        _estado.value = AccionUiState()
    }
}