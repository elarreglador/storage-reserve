# SPEC 01 — Reserva de espacio de seguridad en Android

> **Estado:** Aprobado
>
> **Depende de:** (ninguna)
>
> **Fecha:** 2026-10-11
>
> **Objetivo:** Una app Android (Kotlin + Compose) que preasigna 1 GB por defecto en almacenamiento interno como archivo sacrificial; el usuario lo elimina con un botón para recuperar espacio instantáneo cuando el disco se llena.

## Alcance

**En:**

- **Reserva el espacio en almacenamiento interno** — `app/src/main/java/eu/elarreglador/storage/reserve/ReserveManager.kt` — crea `reserve.bin` de 1 GB (configurable) en `filesDir` al primer arranque si hay espacio suficiente.
- **Libera la reserva con un botón** — `app/src/main/java/eu/elarreglador/storage/reserve/MainScreen.kt` — elimina `reserve.bin` y actualiza el estado; muestra espacio libre de `/data` en tiempo real.
- **Permite configurar el tamaño** — `app/src/main/java/eu/elarreglador/storage/reserve/SettingsScreen.kt` — presets 500 MB / 1 GB / 2 GB + personalizado; persiste en `DataStore Preferences`.
- **Muestra estado visual claro** — `MainScreen.kt` — indicador verde "Protegido" / ámbar "Sin reserva"; botón principal "Liberar X GB" o "Reservar"; deshabilitado si no hay espacio para crear.
- **Re-armado manual** — `MainScreen.kt` — tras liberar, el botón pasa a "Reservar"; no hay WorkManager ni proceso en segundo plano.

**Fuera del alcance (para futuras especificaciones):**

- Re-armado automático con WorkManager.
- Notificaciones de espacio bajo / tile de Ajustes rápidos.
- Exportar/borrar desde ADB o gestor de archivos (el archivo está en almacenamiento interno, no accesible externamente).
- Soporte multi-usuario / perfiles de trabajo.
- Tests de instrumentación / UI automatizados.
- Localización / i18n (solo español por ahora).

## Modelo de datos

```kotlin
// app/src/main/java/eu/elarreglador/storage/reserve/data/ReserveState.kt
data class ReserveState(
    val isReserved: Boolean = false,
    val reservedSizeBytes: Long = 1_073_741_824L, // 1 GB por defecto
    val lastUpdated: Long = System.currentTimeMillis()
)

// Claves DataStore
object ReserveKeys {
    val IS_RESERVED = booleanPreferencesKey("is_reserved")
    val RESERVED_SIZE_BYTES = longPreferencesKey("reserved_size_bytes")
}

// Presets de tamaño (bytes)
object ReservePresets {
    val PRESETS = listOf(
        536_870_912L,   // 512 MB
        1_073_741_824L, // 1 GB
        2_147_483_648L  // 2 GB
    )
    val DEFAULT = PRESETS[1] // 1 GB
}
```

## Plan de implementación

1. **Crea el proyecto base** — `build.gradle.kts`, `settings.gradle.kts`, `AndroidManifest.xml`, `build.gradle.kts (app)` con dependencias: Compose BOM, Material3, DataStore Preferences, Activity Compose, Lifecycle Runtime KTX.
2. **Implementa `ReserveManager`** — escritura real en bloques de 4 MB con `FileOutputStream` y barra de progreso (corrutinas + `withContext(Dispatchers.IO)`); expone `createReserve(size)`, `deleteReserve()`, `getReserveFile()`, `getFreeSpaceBytes()` (usa `StatFs` en `/data`).
3. **Implementa `ReserveRepository` (DataStore)** — guarda/lee `isReserved` y `reservedSizeBytes`; expone `Flow<ReserveState>`.
4. **Pantalla principal (`MainScreen`)** — `Scaffold` + `Column` centrada; indicador de estado (Icon + Text verde/ámbar), texto "Espacio libre en /data: X GB", botón principal "Liberar X GB" / "Reservar" que llama a `ReserveManager` y actualiza repo; `LaunchedEffect` para auto-crear en primer arranque si `isReserved && !file.exists()`.
5. **Pantalla de ajustes (`SettingsScreen`)** — lista de presets con `RadioButton` + campo personalizado (número + selector MB/GB); guarda en DataStore; botón "Aplicar" que si cambia tamaño y hay reserva, ofrece "Recrear ahora".
6. **Navegación** — `NavHost` con `MainScreen` (start) + `SettingsScreen`; `TopAppBar` con ícono ajustes.
7. **Tema y estilos** — `Theme.kt` Material3, colores semánticos para estados (verde `ContainerSuccess`, ámbar `ContainerWarning`), tipografía estándar.
8. **Icono y metadatos** — `ic_launcher` vectorial simple (disco + escudo), `app_name="Reserva de espacio"`, package `eu.elarreglador.storage.reserve`.

## Criterios de aceptación

- [ ] La app compila e instala en Android 16 (SDK 36) y arranca sin crashes.
- [ ] Al primer arranque con ≥ 1 GB libre en `/data`, se crea `reserve.bin` de 1 GB real (verificado con `adb shell ls -lh /data/data/eu.elarreglador.storage.reserve/files/reserve.bin` → tamaño exacto).
- [ ] El botón "Liberar 1 GB" elimina el archivo y el espacio libre en `/data` aumenta ~1 GB (verificado con `df -h /data` antes/después).
- [ ] Tras liberar, el estado pasa a ámbar "Sin reserva" y el botón muestra "Reservar".
- [ ] Pulsar "Reservar" vuelve a crear el archivo del tamaño configurado.
- [ ] Si no hay espacio suficiente para crear, el botón "Reservar" está deshabilitado y muestra "Espacio insuficiente (máx. X GB)".
- [ ] La pantalla de ajustes permite cambiar a 512 MB / 1 GB / 2 GB / personalizado; el cambio persiste tras cerrar/abrir la app.
- [ ] La UI muestra espacio libre de `/data` actualizado en tiempo real (refresco ≤ 2 s).

## Decisiones

- **Sí:** Almacenamiento interno (`filesDir`) para la reserva. Más fiable al límite de disco; no requiere permisos; borrado instantáneo por la propia app.
- **No:** Almacenamiento externo específico de la app (`getExternalFilesDir`). Aunque permite borrado externo (ADB/gestor), pasa por FUSE y en F2FS puede no reservar bloques reales con `setLength`.
- **Sí:** Escritura real de bytes en chunks de 4 MB. Garantiza que el FS (F2FS/ext4) asigne bloques físicos; `setLength`/`fallocate` puede dejar archivo disperso.
- **Sí:** `DataStore Preferences` para persistencia. Ligero, transaccional, API Flow reactiva; suficiente para dos claves.
- **Sí:** Re-armado manual tras liberar. KISS, sin `WorkManager`, evita sorpresas de espacio.
- **No:** Auto-creación silenciosa en background. El usuario decide cuándo sacrificar espacio.
- **Sí:** `minSdk 26`, `targetSdk 36`. Cubre el dispositivo objetivo (Android 16) y mantiene compatibilidad amplia.
- **Sí:** Kotlin + Jetpack Compose (Material3). Código conciso, estándar actual, una sola Activity.

## Riesgos

| Riesgo | Mitigación |
| -------- | ---------- |
| Disco tan lleno que la app no arranca (ART/Dalvik sin espacio) | Android reserva ~5 % de `/data` para sistema; apps instaladas suelen arrancar. Documentar en README: "Si la app no abre, libere espacio vía ADB borrando `reserve.bin`". |
| `setLength` no ocupa bloques reales en F2FS | Se escribe dato real (ceros) en bloques de 4 MB; verificado con `du -sh` tras creación. |
| Borrado no libera espacio instantáneo (F2FS GC diferido) | En pruebas, `rm` en F2FS libera al instante; si no, `sync` forzado tras borrar. |
| Usuario configura tamaño > espacio libre | Botón deshabilitado + mensaje con máximo creable calculado con `StatFs`. |
| Multi-usuario: reserva solo en usuario actual | Fuera de alcance; app instala por usuario. Documentar. |

## Lo que **no** está en esta especificación

- Re-armado automático con WorkManager.
- Notificaciones de espacio bajo / tile de Ajustes rápidos.
- Borrado desde ADB/gestor de archivos (archivo en almacenamiento interno).
- Soporte multi-usuario / perfiles de trabajo.
- Tests de instrumentación / UI automatizados.
- Localización / i18n (solo español).