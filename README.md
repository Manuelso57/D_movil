# Medición de Cloro en Línea

Aplicación móvil Android para consultar y registrar los niveles de cloro libre del agua en las granjas de una empresa avícola y ganadera, con alertas y seguimiento de desviaciones. Usa datos ficticios.

Hecha con Kotlin y Jetpack Compose.

**Autor:** Manuel Duran

## Funcionalidades

- Inicio de sesión con tres perfiles: operario, supervisor y jefatura.
- Consulta de granjas y puntos de medición, con estado normal, advertencia o crítico.
- Historial de mediciones por punto.
- Registro de mediciones con validación de campos.
- Alertas activas con notificación y vibración.
- Desviaciones con estados y acciones correctivas, con foto de evidencia.
- Resumen general de la calidad del agua.
- Funciona sin conexión y se sincroniza con un servidor simulado.

## Cómo ejecutar

1. Abre el proyecto en Android Studio y espera a que termine la sincronización de Gradle.
2. Presiona **Run** en un emulador o en un celular.

La app funciona sin servidor: muestra "Sin conexión" y usa los datos de ejemplo.

Para conectarla al servidor simulado:

1. Instala Node.js.
2. En la carpeta `servidor`, ejecuta:

```
npx json-server@0.17.4 --watch db.json --host 0.0.0.0 --port 3000
```

3. Abre la app y toca **Actualizar**.

El emulador usa la dirección `http://10.0.2.2:3000`, definida en `ApiCloro.kt`. En un celular real, cámbiala por la IP del computador.

## Usuarios de prueba

| Usuario | Clave | Perfil |
|---|---|---|
| operario | 1234 | Operario |
| supervisor | 1234 | Supervisor |
| jefatura | 1234 | Jefatura |

## Notas

- `targetSdk` está en 36 porque Android 17 bloquea el acceso a la red local de las apps que apuntan a la 37.
- La app declara permisos de internet, notificaciones, vibración y cámara.