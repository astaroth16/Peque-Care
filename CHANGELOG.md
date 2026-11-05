# Registro de Cambios (Changelog) - PequeCare

Todos los cambios notables en este proyecto serán documentados en este archivo.
El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) y este proyecto sigue el versionado semántico [Semantic Versioning](https://semver.org/lang/es/).

---

## [1.4.0] - 2026-11-04
### Añadido
- **Sistema de Versionado y Changelog:** Seguimiento integrado de versiones y visor interactivo de registro de cambios dentro de la app (Ajustes > Acerca de PequeCare > Novedades y Changelog).
- **Compatibilidad Dual SQLite & JSON en Google Drive:** Detección automática por encabezado magic bytes de respaldos binarios nativos (`SQLite format 3`) y paquetes JSON estructurados (`FamilyBackupPackage` / `ChildBackupPackage`), permitiendo restaurar sin pérdidas cualquier copia previa o moderna.
- **Gestión Avanzada de Perfiles:**
  - Edición en caliente de información del niño (nombre, apellido, pediatra, clínica, teléfono, seguro) sin reiniciar ni vaciar la base de datos.
  - Opción de clonación de perfil para facilitar el registro de gemelos/múltiples.
  - Eliminación profunda con borrado en cascada de todos los datos asociados (crecimiento, tomas, turnos, rutinas).

### Modificado
- **Optimización de Pantalla Resumen:**
  - Eliminación de redundancias en el saludo y tarjetas duplicadas de niños.
  - Transformación del botón de registro en *Crecimiento y Métricas* a un botón de acción rápida circular compacto con ícono `+`.
- **Nuevo Ícono de Aplicación:**
  - Ícono adaptativo y minimalista vectorizado que representa cuidado infantil y familia, optimizado para Material Design 3.

---

## [1.3.0] - 2026-11-03
### Añadido
- **Soporte Multi-Peque (Familia):**
  - Registro y gestión de múltiples niños en la misma instalación.
  - Selector superior y pestañas para alternar rápidamente entre perfiles conservando estados activos.
  - Respaldo selectivo en Google Drive: opción de subir/restaurar la base familiar completa o la ficha médica individual de un niño.
- **Sincronización en la Nube con Google Drive REST API:**
  - Vinculación segura de cuenta de Google.
  - Detección y listado interactivo de copias de seguridad en la nube.

### Corregido
- **Reorganización en Consultas Médicas:**
  - Reestructuración de campos en tarjetas de turnos médicos para evitar estiramientos o distorsión de filas en pantallas compactas y teléfonos plegables (Flip).

---

## [1.2.0] - 2026-11-02
### Añadido
- **Recordatorios y Notificaciones Pediátricas:**
  - Integración de `AlarmManager` con notificaciones exactas para tomas de medicamentos y turnos médicos programados.
- **Exportación de Informes Pediátricos:**
  - Generador de reportes médicos filtrados por fecha con exportación a PDF nativo.
  - Compartición directa a través de WhatsApp o Correo Electrónico.
- **Personalización Visual:**
  - Soporte completo para Modo Oscuro y Modo Claro adaptativo con paleta de colores pasteles relajantes.

---

## [1.1.0] - 2026-11-01
### Añadido
- **Métricas y Curvas de Crecimiento:**
  - Registro de peso, talla, perímetro cefálico e IMC con cálculo automático de percentiles y puntuaciones Z.
- **Módulo de Salud:**
  - Control de tratamientos farmacológicos, posología, horarios, duración y registro histórico de tomas administradas.
  - Registro de temperaturas corporales y síntomas febriles.
- **Rutina Diaria del Peque:**
  - Monitoreo de ciclos de sueño (siestas y noche).
  - Registro de tomas de lactancia, comidas y vasos de agua.
  - Seguimiento de actividades recreativas, educativas y motrices.

---

## [1.0.0] - 2026-10-31
### Añadido
- **Lanzamiento Inicial de PequeCare:**
  - Arquitectura moderna en Kotlin con Jetpack Compose y Material 3.
  - Base de datos local segura con Room SQLite.
  - Ficha médica base del niño/a.
