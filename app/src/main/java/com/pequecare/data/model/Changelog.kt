package com.pequecare.data.model

data class ChangelogEntry(
    val version: String,
    val date: String,
    val isLatest: Boolean = false,
    val highlights: String,
    val changes: List<ChangeItem>
)

data class ChangeItem(
    val type: ChangeType,
    val description: String
)

enum class ChangeType(val label: String, val emoji: String) {
    FEATURE("Novedad", "✨"),
    IMPROVEMENT("Mejora", "⚡"),
    FIX("Corrección", "🐛"),
    DESIGN("Diseño", "🎨"),
    SECURITY("Seguridad", "🔒")
}

object ChangelogProvider {
    val entries: List<ChangelogEntry> = listOf(
        ChangelogEntry(
            version = "1.4.0",
            date = "04 de Noviembre, 2026",
            isLatest = true,
            highlights = "Compatibilidad dual en respaldos de Google Drive, clonación y edición de perfiles, y nuevo visor de registro de cambios.",
            changes = listOf(
                ChangeItem(ChangeType.FEATURE, "Sistema de Versionado y Changelog integrado en la aplicación."),
                ChangeItem(ChangeType.FEATURE, "Compatibilidad dual en Google Drive: reconoce bases de datos SQLite (.db) y respaldos JSON."),
                ChangeItem(ChangeType.FEATURE, "Edición de perfil del niño sin reinicios ni pérdida de base de datos."),
                ChangeItem(ChangeType.FEATURE, "Opción para clonar perfil (gemelos/múltiples) y eliminación profunda con cascada."),
                ChangeItem(ChangeType.DESIGN, "Optimización de pantalla Resumen: limpieza de redundancias y botón '+' compacto."),
                ChangeItem(ChangeType.DESIGN, "Nuevo ícono de la aplicación minimalista y moderno.")
            )
        ),
        ChangelogEntry(
            version = "1.3.0",
            date = "03 de Noviembre, 2026",
            highlights = "Soporte Multi-Peque con pestañas de cambio de perfil y respaldo selectivo en la nube.",
            changes = listOf(
                ChangeItem(ChangeType.FEATURE, "Soporte Multi-Peque: gestiona múltiples niños en una sola app."),
                ChangeItem(ChangeType.FEATURE, "Pestañas y selector de peques con transición fluida sin pérdida de estado."),
                ChangeItem(ChangeType.FEATURE, "Respaldo en Google Drive individual por niño o base completa familiar."),
                ChangeItem(ChangeType.FIX, "Reorganización de campos en Consultas Médicas para evitar distorsión en pantallas compactas.")
            )
        ),
        ChangelogEntry(
            version = "1.2.0",
            date = "02 de Noviembre, 2026",
            highlights = "Alarmas de tomas y citas, informes pediátricos para WhatsApp/PDF y personalización visual.",
            changes = listOf(
                ChangeItem(ChangeType.FEATURE, "Recordatorios y notificaciones precisas con AlarmManager para dosis y citas."),
                ChangeItem(ChangeType.FEATURE, "Generador de Informes Pediátricos con exportación a PDF y texto para WhatsApp."),
                ChangeItem(ChangeType.DESIGN, "Soporte completo para Modo Oscuro y Modo Claro con tonos pasteles.")
            )
        ),
        ChangelogEntry(
            version = "1.1.0",
            date = "01 de Noviembre, 2026",
            highlights = "Métricas de percentiles pediátricos, tratamientos farmacológicos y rutinas diarias.",
            changes = listOf(
                ChangeItem(ChangeType.FEATURE, "Curvas de crecimiento con percentiles pediátricos de peso, talla e IMC."),
                ChangeItem(ChangeType.FEATURE, "Módulo de salud con tomas de medicamentos, dosis y registro de fiebre."),
                ChangeItem(ChangeType.FEATURE, "Registro de rutinas: sueño, comidas y actividades.")
            )
        ),
        ChangelogEntry(
            version = "1.0.0",
            date = "31 de Octubre, 2026",
            highlights = "Lanzamiento oficial de PequeCare.",
            changes = listOf(
                ChangeItem(ChangeType.FEATURE, "Lanzamiento inicial con Jetpack Compose Material 3 y Room SQLite local.")
            )
        )
    )
}
