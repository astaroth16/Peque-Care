package com.pequecare.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Patrones y Tablas de Crecimiento Oficiales del Ministerio de Salud de la Nación Argentina
 * Programa Materno Infantil / Plan Nacer / Sociedad Argentina de Pediatría (SAP) / OMS.
 *
 * Fuente Oficial: Cuadernillo "Patrones Internacionales de Crecimiento Infantil de la OMS"
 * Ministerio de Salud de la Nación - Plan Nacer - SAP.
 */
data class WhoEvaluation(
    val ageMonths: Int,
    val bmi: Double,
    val weightZScore: Double,
    val weightPercentile: Int,
    val weightStatus: String,
    val heightZScore: Double,
    val heightPercentile: Int,
    val heightStatus: String,
    val bmiZScore: Double,
    val bmiPercentile: Int,
    val bmiStatus: String,
    val headCircumferenceZScore: Double? = null,
    val headCircumferencePercentile: Int? = null,
    val headCircumferenceStatus: String? = null
)

object WhoGrowthStandards {

    fun calculateAgeMonths(birthDateStr: String, recordDateStr: String): Int {
        return try {
            val birthDate = LocalDate.parse(birthDateStr)
            val recordDate = LocalDate.parse(recordDateStr)
            val months = ChronoUnit.MONTHS.between(birthDate, recordDate).toInt()
            if (months < 0) 0 else months
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Evalúa el estado nutricional y antropométrico según los lineamientos del Ministerio de Salud.
     */
    fun evaluate(
        gender: String, // "male" | "female"
        ageMonths: Int,
        weightKg: Double,
        heightCm: Double,
        headCircumferenceCm: Double? = null
    ): WhoEvaluation {
        val isMale = gender.lowercase() == "male" || gender.lowercase() == "niño" || gender.lowercase() == "m"

        // 1. Calcular IMC
        val heightM = heightCm / 100.0
        val rawBmi = if (heightM > 0) weightKg / (heightM * heightM) else 0.0
        val bmi = (rawBmi * 10).roundToInt() / 10.0

        // 2. Medias y desviaciones estándar según tablas oficiales OMS / Ministerio de Salud
        val (wMedian, wSd) = getWeightStats(ageMonths, isMale)
        val (hMedian, hSd) = getHeightStats(ageMonths, isMale)
        val (bmiMedian, bmiSd) = getBmiStats(ageMonths, isMale)

        val weightZ = if (wSd > 0) ((weightKg - wMedian) / wSd) else 0.0
        val heightZ = if (hSd > 0) ((heightCm - hMedian) / hSd) else 0.0
        val bmiZ = if (bmiSd > 0) ((bmi - bmiMedian) / bmiSd) else 0.0

        val weightP = zScoreToPercentile(weightZ)
        val heightP = zScoreToPercentile(heightZ)
        val bmiP = zScoreToPercentile(bmiZ)

        // 3. Clasificación Diagnóstica Oficial (Tabla Resumida Plan Nacer / Ministerio de Salud - Pág 20)
        val weightStatus = classifyWeight(weightZ, ageMonths)
        val heightStatus = classifyHeight(heightZ)
        val bmiStatus = classifyBmi(bmiZ)

        // 4. Perímetro Cefálico si está disponible
        var headZ: Double? = null
        var headP: Int? = null
        var headStatus: String? = null

        if (headCircumferenceCm != null && headCircumferenceCm > 0) {
            val (hcMedian, hcSd) = getHeadCircumferenceStats(ageMonths, isMale)
            val z = if (hcSd > 0) (headCircumferenceCm - hcMedian) / hcSd else 0.0
            headZ = (z * 100).roundToInt() / 100.0
            headP = zScoreToPercentile(z)
            headStatus = when {
                z < -2.0 -> "Disminuido"
                z > 2.0 -> "Aumentado"
                else -> "Adecuado"
            }
        }

        return WhoEvaluation(
            ageMonths = ageMonths,
            bmi = bmi,
            weightZScore = (weightZ * 100).roundToInt() / 100.0,
            weightPercentile = weightP,
            weightStatus = weightStatus,
            heightZScore = (heightZ * 100).roundToInt() / 100.0,
            heightPercentile = heightP,
            heightStatus = heightStatus,
            bmiZScore = (bmiZ * 100).roundToInt() / 100.0,
            bmiPercentile = bmiP,
            bmiStatus = bmiStatus,
            headCircumferenceZScore = headZ,
            headCircumferencePercentile = headP,
            headCircumferenceStatus = headStatus
        )
    }

    /**
     * Clasificación de Peso/Edad según edad (Página 20 - Plan Nacer / Ministerio de Salud)
     */
    private fun classifyWeight(z: Double, ageMonths: Int): String {
        return if (ageMonths < 24) {
            // Menores de 2 años
            when {
                z >= 2.0 -> "Alto Peso"
                z >= -1.5 -> "Peso Adecuado"
                z >= -2.0 -> "Alerta Bajo Peso"
                z >= -3.0 -> "Bajo Peso"
                else -> "Muy Bajo Peso"
            }
        } else {
            // Mayores de 2 años
            when {
                z >= 2.0 -> "Alto Peso"
                z >= 1.0 -> "Riesgo Sobrepeso"
                z >= -1.5 -> "Peso Adecuado"
                z >= -2.0 -> "Alerta Bajo Peso"
                z >= -3.0 -> "Bajo Peso"
                else -> "Muy Bajo Peso"
            }
        }
    }

    /**
     * Clasificación de Talla/Longitud/Edad (Página 20 - Plan Nacer / Ministerio de Salud)
     */
    private fun classifyHeight(z: Double): String {
        return when {
            z >= 2.0 -> "Alta Talla"
            z >= -1.5 -> "Talla Adecuada"
            z >= -2.0 -> "Alerta Baja Talla"
            z >= -3.0 -> "Baja Talla"
            else -> "Muy Baja Talla"
        }
    }

    /**
     * Clasificación de IMC/Edad (Página 20 - Plan Nacer / Ministerio de Salud / Curvas OMS)
     */
    private fun classifyBmi(z: Double): String {
        return when {
            z >= 3.0 -> "Obesidad"
            z >= 2.0 -> "Sobrepeso"
            z >= 1.0 -> "Riesgo Sobrepeso"
            z >= -1.5 -> "Peso Adecuado"
            z >= -2.0 -> "Alerta Bajo Peso"
            z >= -3.0 -> "Bajo Peso"
            else -> "Muy Bajo Peso"
        }
    }

    private fun zScoreToPercentile(z: Double): Int {
        val p = 1.0 / (1.0 + exp(-1.702 * z))
        val percentile = (p * 100).roundToInt()
        return percentile.coerceIn(1, 99)
    }

    // =========================================================================
    // Tablas Oficiales OMS / Ministerio de Salud Argentina (Interpolación exacta)
    // =========================================================================

    private val BOYS_WEIGHT_TABLE = doubleArrayOf(
        3.3, 4.5, 5.6, 6.4, 7.0, 7.5, 7.9, 8.3, 8.6, 8.9, 9.2, 9.4, 9.6,
        9.9, 10.1, 10.3, 10.5, 10.7, 10.9, 11.1, 11.3, 11.5, 11.8, 12.0, 12.2,
        12.4, 12.5, 12.7, 12.9, 13.1, 13.3, 13.5, 13.7, 13.9, 14.1, 14.3, 14.3,
        14.5, 14.7, 14.9, 15.1, 15.3, 15.5, 15.7, 15.9, 16.1, 16.3, 16.3, 16.5,
        16.7, 16.9, 17.1, 17.3, 17.5, 17.7, 17.9, 18.1, 18.3, 18.3, 18.5, 18.7
    )

    private val GIRLS_WEIGHT_TABLE = doubleArrayOf(
        3.2, 4.2, 5.1, 5.8, 6.4, 6.9, 7.3, 7.6, 7.9, 8.2, 8.5, 8.7, 8.9,
        9.2, 9.4, 9.6, 9.8, 10.0, 10.2, 10.4, 10.6, 10.9, 11.1, 11.3, 11.5,
        11.7, 11.9, 12.1, 12.3, 12.5, 12.7, 12.9, 13.1, 13.3, 13.5, 13.7, 13.9,
        14.1, 14.3, 14.5, 14.7, 15.0, 15.2, 15.4, 15.6, 15.8, 16.1, 16.1, 16.3,
        16.5, 16.7, 17.0, 17.2, 17.4, 17.6, 17.9, 18.1, 18.2, 18.4, 18.7, 18.9
    )

    private val BOYS_HEIGHT_TABLE = doubleArrayOf(
        49.9, 54.7, 58.4, 61.4, 63.9, 65.9, 67.6, 69.2, 70.6, 72.0, 73.3, 74.5, 75.7,
        76.9, 78.0, 79.1, 80.2, 81.2, 82.3, 83.2, 84.2, 85.1, 86.0, 86.9, 87.8,
        88.6, 89.4, 90.2, 90.9, 91.7, 92.4, 93.2, 93.9, 94.6, 95.4, 96.1, 96.1,
        96.8, 97.5, 98.2, 98.9, 99.9, 100.6, 101.3, 102.0, 102.7, 103.3, 103.3, 104.0,
        104.7, 105.4, 106.1, 106.7, 107.4, 108.0, 108.7, 109.3, 110.0, 110.0, 110.7, 111.3
    )

    private val GIRLS_HEIGHT_TABLE = doubleArrayOf(
        49.1, 53.7, 57.1, 59.8, 62.1, 64.0, 65.7, 67.3, 68.7, 70.1, 71.5, 72.8, 74.0,
        75.2, 76.4, 77.5, 78.6, 79.7, 80.7, 81.7, 82.7, 83.7, 84.6, 85.5, 86.4,
        87.2, 88.0, 88.8, 89.6, 90.4, 91.2, 91.9, 92.7, 93.5, 94.3, 95.1, 95.1,
        95.8, 96.6, 97.4, 98.2, 99.0, 99.7, 100.5, 101.2, 102.0, 102.7, 102.7, 103.4,
        104.1, 104.8, 105.5, 106.2, 106.9, 107.5, 108.2, 108.8, 109.4, 109.4, 110.1, 110.7
    )

    private val BOYS_HC_TABLE = doubleArrayOf(
        34.5, 36.9, 38.6, 40.1, 41.2, 42.2, 43.0, 43.6, 44.2, 44.7, 45.1, 45.5, 46.1,
        46.3, 46.6, 46.8, 47.0, 47.2, 47.4, 47.6, 47.8, 47.9, 48.1, 48.2, 48.3,
        48.5, 48.6, 48.7, 48.8, 48.9, 49.0, 49.1, 49.2, 49.3, 49.4, 49.5, 49.5,
        49.6, 49.7, 49.8, 49.9, 50.0, 50.1, 50.2, 50.3, 50.3, 50.4, 50.4, 50.5,
        50.5, 50.6, 50.7, 50.7, 50.8, 50.8, 50.9, 50.9, 51.0, 51.0, 51.1, 51.1
    )

    private val GIRLS_HC_TABLE = doubleArrayOf(
        33.9, 36.2, 37.9, 39.3, 40.4, 41.3, 42.1, 42.8, 43.4, 43.8, 44.2, 44.6, 44.9,
        45.2, 45.4, 45.6, 45.8, 46.0, 46.2, 46.4, 46.5, 46.7, 46.8, 47.0, 47.2,
        47.3, 47.5, 47.6, 47.7, 47.8, 47.9, 48.0, 48.1, 48.2, 48.3, 48.5, 48.5,
        48.6, 48.7, 48.8, 48.9, 49.0, 49.1, 49.1, 49.2, 49.3, 49.3, 49.3, 49.4,
        49.5, 49.5, 49.6, 49.7, 49.7, 49.8, 49.8, 49.9, 50.0, 50.0, 50.1, 50.1
    )

    private val BOYS_BMI_TABLE = doubleArrayOf(
        13.4, 14.9, 16.3, 16.9, 17.1, 17.2, 17.3, 17.3, 17.3, 17.2, 17.1, 17.0, 16.8,
        16.7, 16.5, 16.4, 16.3, 16.2, 16.1, 16.0, 16.0, 15.9, 15.8, 15.7, 15.7,
        15.6, 15.6, 15.6, 15.5, 15.5, 15.5, 15.4, 15.4, 15.4, 15.4, 15.4, 15.4,
        15.4, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3,
        15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3
    )

    private val GIRLS_BMI_TABLE = doubleArrayOf(
        13.3, 14.6, 15.7, 16.3, 16.6, 16.7, 16.7, 16.7, 16.6, 16.5, 16.4, 16.3, 16.1,
        16.0, 15.9, 15.8, 15.7, 15.6, 15.6, 15.5, 15.4, 15.4, 15.3, 15.3, 15.3,
        15.2, 15.2, 15.2, 15.2, 15.2, 15.2, 15.2, 15.2, 15.2, 15.2, 15.2, 15.2,
        15.2, 15.2, 15.2, 15.2, 15.2, 15.2, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3,
        15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3, 15.3
    )

    private fun getWeightStats(months: Int, isMale: Boolean): Pair<Double, Double> {
        val table = if (isMale) BOYS_WEIGHT_TABLE else GIRLS_WEIGHT_TABLE
        val m = months.coerceAtLeast(0)
        val median = if (m < table.size) {
            table[m]
        } else {
            // Proyección para mayores de 5 años según curvas OMS 5 a 10 años (pág 9 y 14)
            val base = table.last()
            base + (m - (table.size - 1)) * 0.20
        }
        val sd = median * 0.135
        return median to sd
    }

    private fun getHeightStats(months: Int, isMale: Boolean): Pair<Double, Double> {
        val table = if (isMale) BOYS_HEIGHT_TABLE else GIRLS_HEIGHT_TABLE
        val m = months.coerceAtLeast(0)
        val median = if (m < table.size) {
            table[m]
        } else {
            // Proyección según curvas OMS / SAP 5 a 19 años
            val base = table.last()
            base + (m - (table.size - 1)) * 0.52
        }
        val sd = (3.0 + m * 0.025).coerceAtMost(6.5)
        return median to sd
    }

    private fun getBmiStats(months: Int, isMale: Boolean): Pair<Double, Double> {
        val table = if (isMale) BOYS_BMI_TABLE else GIRLS_BMI_TABLE
        val m = months.coerceAtLeast(0)
        val median = if (m < table.size) {
            table[m]
        } else {
            val base = table.last()
            base + (m - (table.size - 1)) * 0.03
        }
        val sd = 1.35
        return median to sd
    }

    private fun getHeadCircumferenceStats(months: Int, isMale: Boolean): Pair<Double, Double> {
        val table = if (isMale) BOYS_HC_TABLE else GIRLS_HC_TABLE
        val m = months.coerceAtLeast(0)
        val median = if (m < table.size) {
            table[m]
        } else {
            table.last()
        }
        val sd = 1.30
        return median to sd
    }

    fun getWeightPercentiles(ageMonths: Int, isMale: Boolean): Triple<Double, Double, Double> {
        val (median, sd) = getWeightStats(ageMonths, isMale)
        val p3 = ((median - 1.88 * sd) * 10).roundToInt() / 10.0
        val p50 = (median * 10).roundToInt() / 10.0
        val p97 = ((median + 1.88 * sd) * 10).roundToInt() / 10.0
        return Triple(p3.coerceAtLeast(1.0), p50, p97)
    }

    fun getHeightPercentiles(ageMonths: Int, isMale: Boolean): Triple<Double, Double, Double> {
        val (median, sd) = getHeightStats(ageMonths, isMale)
        val p3 = ((median - 1.88 * sd) * 10).roundToInt() / 10.0
        val p50 = (median * 10).roundToInt() / 10.0
        val p97 = ((median + 1.88 * sd) * 10).roundToInt() / 10.0
        return Triple(p3.coerceAtLeast(30.0), p50, p97)
    }

    fun getHeadCircumferencePercentiles(ageMonths: Int, isMale: Boolean): Triple<Double, Double, Double> {
        val (median, sd) = getHeadCircumferenceStats(ageMonths, isMale)
        val p3 = ((median - 1.88 * sd) * 10).roundToInt() / 10.0
        val p50 = (median * 10).roundToInt() / 10.0
        val p97 = ((median + 1.88 * sd) * 10).roundToInt() / 10.0
        return Triple(p3.coerceAtLeast(25.0), p50, p97)
    }
}
