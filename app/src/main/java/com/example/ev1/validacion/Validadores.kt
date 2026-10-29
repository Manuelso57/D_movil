package com.example.ev1.validacion

object Validadores {

    fun usuario(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu usuario"
        valor.trim().length < 3 -> "El usuario debe tener al menos 3 caracteres"
        else -> null
    }

    fun clave(valor: String): String? = when {
        valor.isEmpty() -> "Ingresa tu clave"
        valor.length < 4 -> "La clave debe tener al menos 4 caracteres"
        else -> null
    }

    fun nivelCloro(valor: String): String? {
        if (valor.isBlank()) return "Ingresa el nivel de cloro libre"
        val numero = valor.trim().replace(',', '.').toDoubleOrNull()
            ?: return "Ingresa un número válido, por ejemplo 0.8"
        return when {
            numero < 0.0 -> "El nivel no puede ser negativo"
            numero > 10.0 -> "El nivel no puede superar los 10 ppm"
            else -> null
        }
    }

    fun observacion(valor: String): String? =
        if (valor.length > 200) "Máximo 200 caracteres" else null

    fun accion(valor: String): String? = when {
        valor.isBlank() -> "Describe la acción realizada"
        valor.trim().length < 5 -> "Describe la acción con al menos 5 caracteres"
        valor.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }
}