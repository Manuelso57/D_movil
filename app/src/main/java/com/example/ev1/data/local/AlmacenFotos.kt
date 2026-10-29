package com.example.ev1.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object AlmacenFotos {

    fun guardar(context: Context, foto: Bitmap, nombre: String): String? {
        return try {
            val carpeta = File(context.filesDir, "evidencias")
            if (!carpeta.exists()) carpeta.mkdirs()
            val archivo = File(carpeta, "$nombre.jpg")
            FileOutputStream(archivo).use { salida ->
                foto.compress(Bitmap.CompressFormat.JPEG, 90, salida)
            }
            archivo.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun cargar(ruta: String?): Bitmap? {
        if (ruta.isNullOrBlank()) return null
        return try {
            BitmapFactory.decodeFile(ruta)
        } catch (e: Exception) {
            null
        }
    }
}