package com.example.ev1.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ev1.data.model.Accion
import com.example.ev1.data.model.Alerta
import com.example.ev1.data.model.Desviacion
import com.example.ev1.data.model.Medicion
import com.example.ev1.data.model.PuntoMedicion
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "cloro_datos")

class AlmacenLocal(private val context: Context) {

    private val claveDatos = stringPreferencesKey("datos_json")

    suspend fun cargarTexto(): String? = context.dataStore.data.first()[claveDatos]

    suspend fun guardar(
        puntos: List<PuntoMedicion>,
        mediciones: List<Medicion>,
        alertas: List<Alerta>,
        desviaciones: List<Desviacion>,
        acciones: List<Accion>
    ) {
        val texto = aJson(puntos, mediciones, alertas, desviaciones, acciones)
        context.dataStore.edit { it[claveDatos] = texto }
    }

    private fun aJson(
        puntos: List<PuntoMedicion>,
        mediciones: List<Medicion>,
        alertas: List<Alerta>,
        desviaciones: List<Desviacion>,
        acciones: List<Accion>
    ): String {
        val raiz = JSONObject()

        raiz.put("puntos", JSONArray().apply {
            puntos.forEach { p ->
                put(JSONObject().apply {
                    put("id", p.id)
                    put("granjaId", p.granjaId)
                    put("nombre", p.nombre)
                    put("tipo", p.tipo)
                    put("estadoActual", p.estadoActual)
                    put("nivelActual", p.nivelActual)
                    put("ultimaActualizacion", p.ultimaActualizacion)
                })
            }
        })

        raiz.put("mediciones", JSONArray().apply {
            mediciones.forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id)
                    put("puntoId", m.puntoId)
                    put("nivelCloro", m.nivelCloro)
                    put("fechaHora", m.fechaHora)
                    put("estado", m.estado)
                    put("origen", m.origen)
                    put("observacion", m.observacion ?: JSONObject.NULL)
                    put("sincronizada", m.sincronizada)
                })
            }
        })

        raiz.put("alertas", JSONArray().apply {
            alertas.forEach { a ->
                put(JSONObject().apply {
                    put("id", a.id)
                    put("puntoId", a.puntoId)
                    put("medicionId", a.medicionId)
                    put("estado", a.estado)
                    put("mensaje", a.mensaje)
                    put("fechaHora", a.fechaHora)
                    put("activa", a.activa)
                })
            }
        })

        raiz.put("desviaciones", JSONArray().apply {
            desviaciones.forEach { d ->
                put(JSONObject().apply {
                    put("id", d.id)
                    put("alertaId", d.alertaId)
                    put("puntoId", d.puntoId)
                    put("descripcion", d.descripcion)
                    put("estado", d.estado)
                    put("fechaDeteccion", d.fechaDeteccion)
                    put("responsableId", d.responsableId ?: JSONObject.NULL)
                    put("sincronizada", d.sincronizada)
                })
            }
        })

        raiz.put("acciones", JSONArray().apply {
            acciones.forEach { a ->
                put(JSONObject().apply {
                    put("id", a.id)
                    put("desviacionId", a.desviacionId)
                    put("usuarioId", a.usuarioId)
                    put("descripcion", a.descripcion)
                    put("fechaHora", a.fechaHora)
                    put("sincronizada", a.sincronizada)
                    put("fotoRuta", a.fotoRuta ?: JSONObject.NULL)
                })
            }
        })

        return raiz.toString()
    }
}