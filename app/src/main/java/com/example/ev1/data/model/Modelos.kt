package com.example.ev1.data.model

object Estado {
    const val NORMAL = "NORMAL"
    const val ADVERTENCIA = "ADVERTENCIA"
    const val CRITICO = "CRITICO"
}

object Rol {
    const val OPERARIO = "OPERARIO"
    const val SUPERVISOR = "SUPERVISOR"
    const val JEFATURA = "JEFATURA"
}

data class Usuario(
    val id: Int,
    val nombre: String,
    val rol: String,
    val usuario: String,
    val clave: String
)

data class Granja(
    val id: Int,
    val nombre: String,
    val ubicacion: String
)

data class PuntoMedicion(
    val id: Int,
    val granjaId: Int,
    val nombre: String,
    val tipo: String,
    val estadoActual: String,
    val nivelActual: Double,
    val ultimaActualizacion: String
)

data class Medicion(
    val id: Int = 0,
    val puntoId: Int,
    val nivelCloro: Double,
    val fechaHora: String,
    val estado: String,
    val origen: String,
    val observacion: String? = null,
    val sincronizada: Boolean = true
)

data class Alerta(
    val id: Int,
    val puntoId: Int,
    val medicionId: Int,
    val estado: String,
    val mensaje: String,
    val fechaHora: String,
    val activa: Boolean
)

data class Desviacion(
    val id: Int,
    val alertaId: Int,
    val puntoId: Int,
    val descripcion: String,
    val estado: String,
    val fechaDeteccion: String,
    val responsableId: Int?,
    val sincronizada: Boolean = true
)

data class Accion(
    val id: Int = 0,
    val desviacionId: Int,
    val usuarioId: Int,
    val descripcion: String,
    val fechaHora: String,
    val sincronizada: Boolean = true,
    val fotoRuta: String? = null
)

data class Rangos(
    val criticoMin: Double,
    val advertenciaMin: Double,
    val normalMin: Double,
    val normalMax: Double,
    val advertenciaMax: Double,
    val unidad: String
)

fun calcularEstado(nivel: Double, r: Rangos): String = when {
    nivel in r.normalMin..r.normalMax -> Estado.NORMAL
    nivel in r.criticoMin..r.normalMin || nivel in r.normalMax..r.advertenciaMax -> Estado.ADVERTENCIA
    else -> Estado.CRITICO
}

object EstadoDesviacion {
    const val PENDIENTE = "PENDIENTE"
    const val EN_GESTION = "EN_GESTION"
    const val RESUELTA = "RESUELTA"
}