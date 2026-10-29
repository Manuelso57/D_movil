package com.example.ev1.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ev1.validacion.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val errorUsuario: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,
    val intentosFallidos: Int = 0
)

class LoginViewModel : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun cambiarUsuario(valor: String) {
        _estado.update { it.copy(usuario = valor, errorUsuario = null, errorGeneral = null) }
    }

    fun cambiarClave(valor: String) {
        _estado.update { it.copy(clave = valor, errorClave = null, errorGeneral = null) }
    }

    fun ingresar(autenticar: (String, String) -> Boolean): Boolean {
        val actual = _estado.value
        val errorUsuario = Validadores.usuario(actual.usuario)
        val errorClave = Validadores.clave(actual.clave)

        if (errorUsuario != null || errorClave != null) {
            _estado.update {
                it.copy(
                    errorUsuario = errorUsuario,
                    errorClave = errorClave,
                    intentosFallidos = it.intentosFallidos + 1
                )
            }
            return false
        }

        val correcto = autenticar(actual.usuario.trim(), actual.clave)
        if (!correcto) {
            _estado.update {
                it.copy(
                    errorGeneral = "Usuario o clave incorrectos",
                    intentosFallidos = it.intentosFallidos + 1
                )
            }
        }
        return correcto
    }
}