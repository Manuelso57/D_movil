package com.example.ev1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ev1.notificaciones.Notificaciones
import com.example.ev1.ui.navigation.AppNav
import com.example.ev1.ui.theme.Ev1Theme

class MainActivity : ComponentActivity() {

    private val pedirPermisoNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        Notificaciones.crearCanal(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            Ev1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}