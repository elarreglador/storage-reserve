# SPEC 02 — Internacionalización (i18n) multi-idioma

> **Estado:** Aprobado
> **Depende de:** SPEC 01
> **Fecha:** 2026-10-11
> **Objetivo:** Añadir soporte para 8 idiomas usando el locale del sistema Android, con archivos de strings por locale y claves en inglés como base.

## Alcance

**En:**
- Crear directorios `values-<locale>/strings.xml` para: `en` (default), `ja`, `de`, `ko`, `fr`, `es`, `pt-rBR`, `zh-rTW` (zh-Hant)
- Migrar `values/strings.xml` actual a `values-en/strings.xml` con claves en inglés
- Mantener `values/strings.xml` como fallback (inglés)
- Actualizar `app_name` en cada locale
- Verificar que `stringResource()` funciona correctamente en Compose con los nuevos locales

**Fuera del alcance:**
- Selector de idioma en UI (la app respeta `Locale.getDefault()`)
- Plurales complejos / ICU MessageFormat (no necesarios en strings actuales)
- RTL (ninguno de los 8 idiomas lo requiere)
- Tests de instrumentación por locale

## Modelo de datos — Directorios de recursos

```
app/src/main/res/
├── values/              ← fallback (inglés)
├── values-en/           ← inglés explícito
├── values-ja/           ← japonés
├── values-de/           ← alemán
├── values-ko/           ← coreano
├── values-fr/           ← francés
├── values-es/           ← español (castellano)
├── values-pt-rBR/       ← portugués (Brasil)
└── values-zh-rTW/       ← chino tradicional (Taiwán/HK)
```

**Locale codes Android:**

| Idioma | Directorio | Código ISO |
|--------|------------|------------|
| English | `values-en` | `en` |
| 日本語 | `values-ja` | `ja` |
| Deutsch | `values-de` | `de` |
| 한국어 | `values-ko` | `ko` |
| Français | `values-fr` | `fr` |
| Castellano | `values-es` | `es` |
| Português (BR) | `values-pt-rBR` | `pt-rBR` |
| 繁體中文 | `values-zh-rTW` | `zh-rTW` |

## Strings base (inglés) — `values-en/strings.xml`

```xml
<resources>
    <string name="app_name">Storage Reserve</string>
    <string name="settings">Settings</string>
    <string name="protected_status">Protected</string>
    <string name="no_reserve_status">No reserve</string>
    <string name="free_space_label">Free space in /data: %s</string>
    <string name="release_button">Release %s</string>
    <string name="reserve_button">Reserve</string>
    <string name="insufficient_space">Insufficient space (max. %s)</string>
    <string name="reserve_size">Reserve size</string>
    <string name="preset_512mb">512 MB</string>
    <string name="preset_1gb">1 GB</string>
    <string name="preset_2gb">2 GB</string>
    <string name="custom_size">Custom</string>
    <string name="custom_size_hint">Size</string>
    <string name="unit_mb">MB</string>
    <string name="unit_gb">GB</string>
    <string name="apply_button">Apply</string>
    <string name="recreate_now">Recreate now</string>
    <string name="creating_reserve">Creating reserve…</string>
    <string name="deleting_reserve">Deleting reserve…</string>
    <string name="app_description">This app creates a large file; when the phone/tablet fills up and becomes unstable, open the app or uninstall it to reclaim that disk space and delete other useless apps</string>
</resources>
```

## Plan de implementación

1. **Crear `values-en/strings.xml`** con claves en inglés (copia traducida del actual `values/strings.xml`)
2. **Mover `values/strings.xml` actual → `values-es/strings.xml`** (español)
3. **Crear `values/strings.xml` fallback** (idéntico a `values-en`)
4. **Crear 6 directorios restantes** con placeholders (claves en inglés, valores vacíos o "TODO")
5. **Verificar build** y que `stringResource(R.string.xxx)` resuelve correctamente
6. **Opcional:** Añadir `resConfig` en `build.gradle.kts` para limitar APK a estos 8 locales

## Criterios de aceptación

- [ ] La app compila sin errores de recursos faltantes
- [ ] Cambiar idioma del sistema a cualquiera de los 8 muestra textos en ese idioma (fallback a inglés si falta traducción)
- [ ] `app_name` aparece traducido en el launcher
- [ ] No hay crashes por `Resources$NotFoundException`
- [ ] Tamaño APK no aumenta desmesuradamente (solo ~8 × strings.xml)

## Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Falta traducción en algún locale → muestra inglés | Fallback automático a `values/` (inglés) |
| `pt-rBR` vs `pt-BR` confusion | Usar `pt-rBR` (notación Android legacy) |
| `zh-rTW` no cubre HK en algunos dispositivos | `zh-rTW` es estándar para tradicional; Android cae a `zh-Hant` si existe |

## Decisiones

- **Sí:** Solo locale del sistema. KISS, evita UI extra y persistencia de preferencia de idioma.
- **Sí:** Claves en inglés como base. Estándar de la industria; facilita revisión y contribuciones.
- **Sí:** `pt-rBR` y `zh-rTW` notación legacy. Compatibles con todos los SDK; Gradle mapea a BCP 47.
- **No:** `resConfigs` por ahora. Dejar que Gradle empaquete todos; optimizar en release si necesario.

## Lo que **no** está en esta especificación

- Selector de idioma en SettingsScreen
- Plurales / ICU MessageFormat
- Soporte RTL (árabe, hebreo, etc.)
- Tests automatizados por locale