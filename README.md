# Reserva de Espacio — Android

App minimalista (Kotlin + Jetpack Compose) que preasigna un archivo sacrificial en el almacenamiento interno. Cuando el disco se llena, abres la app y pulsas **"Liberar X GB"** para recuperar espacio al instante y que el dispositivo vuelva a ser funcional.

**Paquete:** `eu.elarreglador.storage.reserve`  
**minSdk:** 26 | **targetSdk:** 36

---

## ¿Para qué sirve?

En dispositivos con `/data` casi lleno (p. ej. 99 % usado), Android empieza a fallar: no se pueden instalar apps, actualizar, ni siquiera borrar caché con `pm clear` (timeouts). Esta app reserva **1 GB por defecto** (configurable: 512 MB / 1 GB / 2 GB) *mientras hay espacio*. Si luego el disco se llena, el archivo ya existe y **borrarlo libera 1 GB real de golpe** —sin esperar a que `pm clear` termine, sin necesitar ADB, sin cambiar de usuario.

---

## Características

- **Reserva real**: escribe ceros en bloques de 4 MB → el FS (F2FS/ext4) asigna bloques físicos.
- **Un botón**: "Liberar 1 GB" / "Reservar" — acción inmediata.
- **Estado visual**: verde "Protegido" / ámbar "Sin reserva".
- **Espacio libre en `/data`** en tiempo real (refresco ≤ 2 s).
- **Tamaño configurable** con presets + personalizado (persiste en DataStore).
- **Re-armado manual**: tras liberar, decides cuándo volver a reservar.
- **Sin permisos especiales**: usa almacenamiento interno (`filesDir`), privado y fiable.

---

## Compilar e instalar

```bash
# Requisitos: JDK 17+, Android SDK (cmdline-tools o Android Studio)
./gradlew assembleDebug

# Instalar en dispositivo conectado (adb)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O abre el proyecto en **Android Studio** → *Run* ▶.

---

## Uso

1. Abre la app. Si es el primer arranque y hay ≥ 1 GB libre en `/data`, verás **"Protegido"** y el botón **"Liberar 1 GB"**.
2. Cuando el dispositivo se quede sin espacio, abre la app y pulsa **"Liberar 1 GB"**.
3. El archivo `reserve.bin` se borra → recuperas ~1 GB al instante.
4. El estado cambia a **"Sin reserva"** (ámbar) y el botón pasa a **"Reservar"**.
5. Cuando quieras volver a proteger el dispositivo, pulsa **"Reservar"** (requiere espacio libre ≥ tamaño configurado).

> **Ajustes** (⚙ en la barra superior): cambia el tamaño (512 MB / 1 GB / 2 GB / personalizado). Si ya hay reserva activa y cambias el tamaño, la app te ofrece "Recrear ahora".

---

## Notas técnicas

| Aspecto | Decisión |
|---------|----------|
| Ubicación del archivo | `/data/data/eu.elarreglador.storage.reserve/files/reserve.bin` (interno, sin permisos) |
| Escritura | Bloques de 4 MB con `FileOutputStream` (corrutina `Dispatchers.IO`) |
| Persistencia | `DataStore Preferences` (`is_reserved`, `reserved_size_bytes`) |
| Espacio libre | `StatFs` en `/data` |
| Auto-creación | Solo en **primer arranque** si hay espacio; tras liberar, es manual |

---

## Solución de problemas

| Problema | Qué hacer |
|----------|-----------|
| La app no abre (disco 100 % lleno) | Android reserva ~5 % para sistema; si aun así falla: `adb shell rm /data/data/eu.elarreglador.storage.reserve/files/reserve.bin` |
| Botón "Reservar" deshabilitado | Espacio libre en `/data` < tamaño configurado. Libera espacio manualmente o reduce el preset en Ajustes. |
| El espacio no sube tras borrar | En F2FS el GC puede tardar segundos. Ejecuta `sync` por ADB o espera unos instantes. |

---

## Licencia

MIT — úsalo, modifícalo, distribúyelo libremente.

---

## Créditos

Desarrollado siguiendo la especificación `SPEC 01` (ver `specs/01-storage-reserve.md`).