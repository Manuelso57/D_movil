package com.example.ev1.nativos

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Vibracion {

    @Suppress("DEPRECATION")
    fun vibrar(context: Context, critica: Boolean) {
        val patron = if (critica) {
            longArrayOf(0, 400, 150, 400, 150, 400)
        } else {
            longArrayOf(0, 250)
        }

        try {
            val vibrador: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (!vibrador.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrador.vibrate(VibrationEffect.createWaveform(patron, -1))
            } else {
                vibrador.vibrate(patron, -1)
            }
        } catch (e: SecurityException) {
            return
        }
    }
}