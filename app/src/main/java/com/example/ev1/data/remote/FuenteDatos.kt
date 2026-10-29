package com.example.ev1.data.remote

import android.content.Context
import com.example.ev1.R
import com.example.ev1.data.model.Accion
import com.example.ev1.data.model.Alerta
import com.example.ev1.data.model.Desviacion
import com.example.ev1.data.model.Granja
import com.example.ev1.data.model.Medicion
import com.example.ev1.data.model.PuntoMedicion
import com.example.ev1.data.model.Rangos
import com.example.ev1.data.model.Usuario
import org.json.JSONObject

class FuenteDatos(private val texto: String) {

    companion object {
        fun desdeRaw(context: Context): FuenteDatos {
            val contenido = context.resources.openRawResource(R.raw.db)
                .bufferedReader()
                .use { it.readText() }
            return FuenteDatos(contenido)
        }
    }

    private val raiz: JSONObject by lazy { JSONObject(texto) }

    private fun <T> lista(nombre: String, convertir: (JSONObject) -> T): List<T> {
        val arreglo = raiz.optJSONArray(nombre) ?: return emptyList()
        return List(arreglo.length()) { convertir(arreglo.getJSONObject(it)) }
    }

    private fun JSONObject.textoONulo(clave: String): String? =
        if (isNull(clave)) null else getString(clave)

    private fun JSONObject.enteroONulo(clave: String): Int? =
        if (isNull(clave)) null else getInt(clave)

    fun tiene(nombre: String): Boolean = raiz.has(nombre)

    fun obtenerRangos(): Rangos {
        val r = raiz.getJSONObject("rangos")
        return Rangos(
            criticoMin = r.getDouble("criticoMin"),
            advertenciaMin = r.getDouble("advertenciaMin"),
            normalMin = r.getDouble("normalMin"),
            normalMax = r.getDouble("normalMax"),
            advertenciaMax = r.getDouble("advertenciaMax"),
            unidad = r.getString("unidad")
        )
    }

    fun obtenerUsuarios(): List<Usuario> = lista("usuarios") {
        Usuario(
            id = it.getInt("id"),
            nombre = it.getString("nombre"),
            rol = it.getString("rol"),
            usuario = it.getString("usuario"),
            clave = it.getString("clave")
        )
    }

    fun obtenerGranjas(): List<Granja> = lista("granjas") {
        Granja(
            id = it.getInt("id"),
            nombre = it.getString("nombre"),
            ubicacion = it.getString("ubicacion")
        )
    }

    fun obtenerPuntos(): List<PuntoMedicion> = lista("puntos") {
        PuntoMedicion(
            id = it.getInt("id"),
            granjaId = it.getInt("granjaId"),
            nombre = it.getString("nombre"),
            tipo = it.getString("tipo"),
            estadoActual = it.getString("estadoActual"),
            nivelActual = it.getDouble("nivelActual"),
            ultimaActualizacion = it.getString("ultimaActualizacion")
        )
    }

    fun obtenerMediciones(): List<Medicion> = lista("mediciones") {
        Medicion(
            id = it.getInt("id"),
            puntoId = it.getInt("puntoId"),
            nivelCloro = it.getDouble("nivelCloro"),
            fechaHora = it.getString("fechaHora"),
            estado = it.getString("estado"),
            origen = it.getString("origen"),
            observacion = it.textoONulo("observacion"),
            sincronizada = it.optBoolean("sincronizada", true)
        )
    }

    fun obtenerAlertas(): List<Alerta> = lista("alertas") {
        Alerta(
            id = it.getInt("id"),
            puntoId = it.getInt("puntoId"),
            medicionId = it.getInt("medicionId"),
            estado = it.getString("estado"),
            mensaje = it.getString("mensaje"),
            fechaHora = it.getString("fechaHora"),
            activa = it.getBoolean("activa")
        )
    }

    fun obtenerDesviaciones(): List<Desviacion> = lista("desviaciones") {
        Desviacion(
            id = it.getInt("id"),
            alertaId = it.getInt("alertaId"),
            puntoId = it.getInt("puntoId"),
            descripcion = it.getString("descripcion"),
            estado = it.getString("estado"),
            fechaDeteccion = it.getString("fechaDeteccion"),
            responsableId = it.enteroONulo("responsableId"),
            sincronizada = it.optBoolean("sincronizada", true)
        )
    }

    fun obtenerAcciones(): List<Accion> = lista("acciones") {
        Accion(
            id = it.getInt("id"),
            desviacionId = it.getInt("desviacionId"),
            usuarioId = it.getInt("usuarioId"),
            descripcion = it.getString("descripcion"),
            fechaHora = it.getString("fechaHora"),
            sincronizada = it.optBoolean("sincronizada", true),
            fotoRuta = it.textoONulo("fotoRuta")
        )
    }
}