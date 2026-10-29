package com.example.ev1.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ev1.validacion.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegistroUiState(
    val nivel: String = "",
    val observacion: String = "",
    val errorNivel: String? = null,
    val errorObservacion: String? = null,
    val estadoGuardado: String? = null,
    val nivelGuardado: String = ""
)

class RegistroViewModel : ViewModel() {

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    fun cambiarNivel(valor: String) {
        _estado.update {
            it.copy(
                nivel = valor,
                errorNivel = if (valor.isBlank()) null else Validadores.nivelCloro(valor)
            )
        }
    }

    fun cambiarObservacion(valor: String) {
        _estado.update {
            it.copy(observacion = valor, errorObservacion = Validadores.observacion(valor))
        }
    }

    fun guardar(registrar: (Double, String) -> String) {
        val actual = _estado.value
        val errorNivel = Validadores.nivelCloro(actual.nivel)
        val errorObservacion = Validadores.observacion(actual.observacion)

        if (errorNivel != null || errorObservacion != null) {
            _estado.update {
                it.copy(errorNivel = errorNivel, errorObservacion = errorObservacion)
            }
            return
        }

        val nivel = actual.nivel.trim().replace(',', '.').toDouble()
        val estadoCalculado = registrar(nivel, actual.observacion)
        _estado.update {
            it.copy(estadoGuardado = estadoCalculado, nivelGuardado = actual.nivel.trim())
        }
    }
}