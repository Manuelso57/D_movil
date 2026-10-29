package com.example.ev1.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ev1.data.model.EstadoDesviacion
import com.example.ev1.data.model.Rol
import com.example.ev1.ui.screens.AlertasScreen
import com.example.ev1.ui.screens.BarraSuperior
import com.example.ev1.ui.screens.DashboardScreen
import com.example.ev1.ui.screens.DesviacionesScreen
import com.example.ev1.ui.screens.DetalleDesviacionScreen
import com.example.ev1.ui.screens.GranjasScreen
import com.example.ev1.ui.screens.HistorialScreen
import com.example.ev1.ui.screens.LoginScreen
import com.example.ev1.ui.screens.PuntosScreen
import com.example.ev1.ui.screens.RegistroScreen
import com.example.ev1.viewmodel.AccionViewModel
import com.example.ev1.viewmodel.DesviacionesViewModel
import com.example.ev1.viewmodel.LoginViewModel
import com.example.ev1.viewmodel.MonitoreoViewModel
import com.example.ev1.viewmodel.RegistroViewModel
import com.example.ev1.viewmodel.SesionViewModel

object Rutas {
    const val LOGIN = "login"
    const val GRANJAS = "granjas"
    const val PUNTOS = "puntos/{granjaId}"
    const val ALERTAS = "alertas"
    const val HISTORIAL = "historial/{puntoId}"
    const val REGISTRO = "registro/{puntoId}"
    const val DESVIACIONES = "desviaciones"
    const val RESUMEN = "resumen"
    const val DESVIACION = "desviacion/{desviacionId}"

    fun puntos(granjaId: Int) = "puntos/$granjaId"
    fun historial(puntoId: Int) = "historial/$puntoId"
    fun registro(puntoId: Int) = "registro/$puntoId"
    fun desviacion(desviacionId: Int) = "desviacion/$desviacionId"
}

@Composable
fun AppNav(
    modifier: Modifier = Modifier,
    sesionVm: SesionViewModel = viewModel(),
    monitoreoVm: MonitoreoViewModel = viewModel(),
    desviacionesVm: DesviacionesViewModel = viewModel()
) {
    val navController = rememberNavController()
    val usuario by sesionVm.usuarioActual.collectAsStateWithLifecycle()
    val puntos by monitoreoVm.puntos.collectAsStateWithLifecycle()
    val mediciones by monitoreoVm.mediciones.collectAsStateWithLifecycle()
    val alertas by monitoreoVm.alertas.collectAsStateWithLifecycle()
    val desviaciones by desviacionesVm.desviaciones.collectAsStateWithLifecycle()
    val acciones by desviacionesVm.acciones.collectAsStateWithLifecycle()
    val enLinea by monitoreoVm.enLinea.collectAsStateWithLifecycle()
    val ultimaSync by monitoreoVm.ultimaSync.collectAsStateWithLifecycle()
    val ultimoError by monitoreoVm.ultimoError.collectAsStateWithLifecycle()

    val entradaActual by navController.currentBackStackEntryAsState()
    val ruta = entradaActual?.destination?.route
    val titulo = when (ruta) {
        Rutas.GRANJAS -> "Granjas"
        Rutas.PUNTOS -> "Puntos de medición"
        Rutas.ALERTAS -> "Alertas"
        Rutas.HISTORIAL -> "Historial"
        Rutas.REGISTRO -> "Nueva medición"
        Rutas.RESUMEN -> "Resumen general"
        Rutas.DESVIACIONES -> "Desviaciones"
        Rutas.DESVIACION -> "Detalle de desviación"
        else -> "Medición de cloro"
    }
    val mostrarVolver = ruta != null && ruta != Rutas.LOGIN && ruta != Rutas.GRANJAS

    Column(modifier = modifier.fillMaxSize()) {
        BarraSuperior(
            titulo = titulo,
            mostrarVolver = mostrarVolver,
            onVolver = { navController.popBackStack() }
        )

        NavHost(
            navController = navController,
            startDestination = Rutas.LOGIN,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350)) +
                        fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350)) +
                        fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350)) +
                        fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350)) +
                        fadeOut(tween(350))
            }
        ) {
            composable(Rutas.LOGIN) {
                val loginVm: LoginViewModel = viewModel()
                val estadoLogin by loginVm.estado.collectAsStateWithLifecycle()

                LoginScreen(
                    estado = estadoLogin,
                    onUsuarioChange = loginVm::cambiarUsuario,
                    onClaveChange = loginVm::cambiarClave,
                    onIngresar = {
                        val correcto = loginVm.ingresar { nombreUsuario, clave ->
                            sesionVm.iniciarSesion(nombreUsuario, clave)
                        }
                        if (correcto) {
                            navController.navigate(Rutas.GRANJAS) {
                                popUpTo(Rutas.LOGIN) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Rutas.GRANJAS) {
                val u = usuario
                if (u != null) {
                    GranjasScreen(
                        usuario = u,
                        granjas = monitoreoVm.granjas,
                        puntos = puntos,
                        alertasActivas = alertas.count { it.activa },
                        desviacionesAbiertas = desviaciones.count {
                            it.estado != EstadoDesviacion.RESUELTA
                        },
                        enLinea = enLinea,
                        ultimaSync = ultimaSync,
                        ultimoError = ultimoError,
                        onGranjaClick = { id -> navController.navigate(Rutas.puntos(id)) },
                        onVerAlertas = { navController.navigate(Rutas.ALERTAS) },
                        onVerDesviaciones = { navController.navigate(Rutas.DESVIACIONES) },
                        onVerResumen = { navController.navigate(Rutas.RESUMEN) },
                        onActualizar = { monitoreoVm.actualizar() },
                        onCerrarSesion = {
                            sesionVm.cerrarSesion()
                            navController.navigate(Rutas.LOGIN) {
                                popUpTo(Rutas.GRANJAS) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(
                route = Rutas.PUNTOS,
                arguments = listOf(navArgument("granjaId") { type = NavType.IntType })
            ) { entrada ->
                val granjaId = entrada.arguments?.getInt("granjaId") ?: 0
                PuntosScreen(
                    granja = monitoreoVm.granjas.firstOrNull { it.id == granjaId },
                    puntos = puntos.filter { it.granjaId == granjaId },
                    onPuntoClick = { id -> navController.navigate(Rutas.historial(id)) },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.ALERTAS) {
                AlertasScreen(
                    alertas = alertas,
                    puntos = puntos,
                    desviaciones = desviaciones,
                    puedeCrearDesviacion = usuario?.rol == Rol.SUPERVISOR,
                    onAlertaClick = { puntoId -> navController.navigate(Rutas.historial(puntoId)) },
                    onCrearDesviacion = { alertaId ->
                        val id = desviacionesVm.crearDesviacion(alertaId)
                        if (id != null) navController.navigate(Rutas.desviacion(id))
                    },
                    onVerDesviacion = { id -> navController.navigate(Rutas.desviacion(id)) },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(
                route = Rutas.HISTORIAL,
                arguments = listOf(navArgument("puntoId") { type = NavType.IntType })
            ) { entrada ->
                val puntoId = entrada.arguments?.getInt("puntoId") ?: 0
                HistorialScreen(
                    punto = puntos.firstOrNull { it.id == puntoId },
                    mediciones = mediciones.filter { it.puntoId == puntoId },
                    puedeRegistrar = usuario?.rol != Rol.JEFATURA,
                    onRegistrar = { navController.navigate(Rutas.registro(puntoId)) },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(
                route = Rutas.REGISTRO,
                arguments = listOf(navArgument("puntoId") { type = NavType.IntType })
            ) { entrada ->
                val puntoId = entrada.arguments?.getInt("puntoId") ?: 0
                val registroVm: RegistroViewModel = viewModel()
                val estadoRegistro by registroVm.estado.collectAsStateWithLifecycle()

                RegistroScreen(
                    punto = puntos.firstOrNull { it.id == puntoId },
                    estado = estadoRegistro,
                    onNivelChange = registroVm::cambiarNivel,
                    onObservacionChange = registroVm::cambiarObservacion,
                    onGuardar = {
                        registroVm.guardar { nivel, observacion ->
                            monitoreoVm.registrarMedicion(puntoId, nivel, observacion)
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.RESUMEN) {
                DashboardScreen(
                    puntos = puntos,
                    alertas = alertas,
                    desviaciones = desviaciones,
                    onPuntoClick = { id -> navController.navigate(Rutas.historial(id)) },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.DESVIACIONES) {
                DesviacionesScreen(
                    desviaciones = desviaciones,
                    puntos = puntos,
                    onDesviacionClick = { id -> navController.navigate(Rutas.desviacion(id)) },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(
                route = Rutas.DESVIACION,
                arguments = listOf(navArgument("desviacionId") { type = NavType.IntType })
            ) { entrada ->
                val desviacionId = entrada.arguments?.getInt("desviacionId") ?: 0
                val desviacion = desviaciones.firstOrNull { it.id == desviacionId }
                val accionVm: AccionViewModel = viewModel()
                val estadoAccion by accionVm.estado.collectAsStateWithLifecycle()

                DetalleDesviacionScreen(
                    desviacion = desviacion,
                    punto = puntos.firstOrNull { it.id == desviacion?.puntoId },
                    acciones = acciones.filter { it.desviacionId == desviacionId },
                    formulario = estadoAccion,
                    nombreUsuario = { id -> sesionVm.nombreUsuario(id) },
                    puedeGestionar = usuario?.rol == Rol.SUPERVISOR,
                    puedeRegistrarAccion = usuario?.rol != Rol.JEFATURA,
                    onCambiarEstado = { estado ->
                        desviacionesVm.cambiarEstado(desviacionId, estado, usuario?.id)
                    },
                    onTextoChange = accionVm::cambiarTexto,
                    onFotoCapturada = accionVm::asignarFoto,
                    onQuitarFoto = accionVm::quitarFoto,
                    onPermisoCamara = accionVm::registrarPermiso,
                    onRegistrarAccion = {
                        accionVm.enviar { texto, foto ->
                            usuario?.let { u ->
                                desviacionesVm.registrarAccion(desviacionId, texto, foto, u.id)
                            }
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}