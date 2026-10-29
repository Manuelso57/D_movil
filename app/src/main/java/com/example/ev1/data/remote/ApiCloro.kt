package com.example.ev1.data.remote

import com.example.ev1.data.model.Accion
import com.example.ev1.data.model.Alerta
import com.example.ev1.data.model.Desviacion
import com.example.ev1.data.model.Medicion
import com.example.ev1.data.model.PuntoMedicion
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object ConfigApi {
    const val URL_BASE = "http://10.0.2.2:3000"
}

class ApiCloro(private val urlBase: String = ConfigApi.URL_BASE) {

    private fun peticion(metodo: String, ruta: String, cuerpo: String? = null): String {
        val conexion = URL(urlBase + ruta).openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = metodo
            conexion.connectTimeout = 15000
            conexion.readTimeout = 15000
            conexion.setRequestProperty("Accept", "application/json")
            if (cuerpo != null) {
                conexion.doOutput = true
                conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conexion.outputStream.use { it.write(cuerpo.toByteArray(Charsets.UTF_8)) }
            }
            val codigo = conexion.responseCode
            val flujo = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
            val texto = flujo?.bufferedReader()?.use { it.readText() } ?: ""
            if (codigo !in 200..299) throw IOException("HTTP $codigo en $metodo $ruta")
            return texto
        } finally {
            conexion.disconnect()
        }
    }

    fun obtenerTodo(): String = peticion("GET", "/db")

    fun publicarMedicion(m: Medicion): Int {
        val cuerpo = JSONObject().apply {
            put("puntoId", m.puntoId)
            put("nivelCloro", m.nivelCloro)
            put("fechaHora", m.fechaHora)
            put("estado", m.estado)
            put("origen", m.origen)
            put("observacion", m.observacion ?: JSONObject.NULL)
            put("sincronizada", true)
        }
        val respuesta = peticion("POST", "/mediciones", cuerpo.toString())
        return JSONObject(respuesta).getInt("id")
    }

    fun actualizarPunto(p: PuntoMedicion) {
        val cuerpo = JSONObject().apply {
            put("id", p.id)
            put("granjaId", p.granjaId)
            put("nombre", p.nombre)
            put("tipo", p.tipo)
            put("estadoActual", p.estadoActual)
            put("nivelActual", p.nivelActual)
            put("ultimaActualizacion", p.ultimaActualizacion)
        }
        peticion("PUT", "/puntos/${p.id}", cuerpo.toString())
    }

    fun desactivarAlertasDelPunto(puntoId: Int) {
        val lista = JSONArray(peticion("GET", "/alertas?puntoId=$puntoId"))
        for (i in 0 until lista.length()) {
            val alerta = lista.getJSONObject(i)
            if (alerta.optBoolean("activa", false)) {
                alerta.put("activa", false)
                peticion("PUT", "/alertas/${alerta.getInt("id")}", alerta.toString())
            }
        }
    }

    fun publicarAlerta(a: Alerta, medicionIdServidor: Int) {
        val cuerpo = JSONObject().apply {
            put("puntoId", a.puntoId)
            put("medicionId", medicionIdServidor)
            put("estado", a.estado)
            put("mensaje", a.mensaje)
            put("fechaHora", a.fechaHora)
            put("activa", true)
        }
        peticion("POST", "/alertas", cuerpo.toString())
    }

    fun guardarDesviacion(d: Desviacion) {
        val cuerpo = JSONObject().apply {
            put("id", d.id)
            put("alertaId", d.alertaId)
            put("puntoId", d.puntoId)
            put("descripcion", d.descripcion)
            put("estado", d.estado)
            put("fechaDeteccion", d.fechaDeteccion)
            put("responsableId", d.responsableId ?: JSONObject.NULL)
            put("sincronizada", true)
        }
        guardarConId("/desviaciones", d.id, cuerpo.toString())
    }

    fun guardarAccion(a: Accion) {
        val cuerpo = JSONObject().apply {
            put("id", a.id)
            put("desviacionId", a.desviacionId)
            put("usuarioId", a.usuarioId)
            put("descripcion", a.descripcion)
            put("fechaHora", a.fechaHora)
            put("sincronizada", true)
            put("fotoRuta", a.fotoRuta ?: JSONObject.NULL)
        }
        guardarConId("/acciones", a.id, cuerpo.toString())
    }

    private fun guardarConId(coleccion: String, id: Int, cuerpo: String) {
        try {
            peticion("PUT", "$coleccion/$id", cuerpo)
        } catch (e: IOException) {
            if (e.message?.contains("HTTP 404") == true) {
                peticion("POST", coleccion, cuerpo)
            } else {
                throw e
            }
        }
    }
}