package com.pequecare.util

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.roundToInt

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
    val bmiStatus: String
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

    fun evaluate(
        gender: String, // "male" or "female"
        ageMonths: Int,
        weightKg: Double,
        heightCm: Double
    ): WhoEvaluation {
        val isMale = gender.lowercase() == "male" || gender.lowercase() == "niño" || gender.lowercase() == "m"

        // 1. Calcular IMC
        val heightM = heightCm / 100.0
        val rawBmi = if (heightM > 0) weightKg / (heightM * heightM) else 0.0
        val bmi = (rawBmi * 10).roundToInt() / 10.0

        // 2. Medias y desviaciones según OMS
        val (wMedian, wSd) = getWeightStats(ageMonths, isMale)
        val (hMedian, hSd) = getHeightStats(ageMonths, isMale)
        val (bmiMedian, bmiSd) = getBmiStats(ageMonths, isMale)

        val weightZ = ((weightKg - wMedian) / wSd)
        val heightZ = ((heightCm - hMedian) / hSd)
        val bmiZ = ((bmi - bmiMedian) / bmiSd)

        val weightP = zScoreToPercentile(weightZ)
        val heightP = zScoreToPercentile(heightZ)
        val bmiP = zScoreToPercentile(bmiZ)

        val weightStatus = when {
            weightZ < -2.0 -> "Bajo Peso OMS"
            weightZ > 2.0 -> "Sobrepeso OMS"
            else -> "Peso Adecuado OMS"
        }

        val heightStatus = when {
            heightZ < -2.0 -> "Talla Baja OMS"
            heightZ > 2.0 -> "Talla Alta OMS"
            else -> "Talla Adecuada OMS"
        }

        val bmiStatus = when {
            bmiZ < -2.0 -> "Bajo Peso"
            bmiZ > 2.0 -> "Sobrepeso"
            else -> "Normal OMS"
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
            bmiStatus = bmiStatus
        )
    }

    private fun zScoreToPercentile(z: Double): Int {
        // Aproximación logística de la CDF normal
        val p = 1.0 / (1.0 + exp(-1.654 * z))
        val percentile = (p * 100).roundToInt()
        return percentile.coerceIn(1, 99)
    }

    private fun getWeightStats(months: Int, isMale: Boolean): Pair<Double, Double> {
        val m = months.coerceIn(0, 60)
        return if (isMale) {
            val median = 3.3 + 0.7 * m.coerceAtMost(6) + 0.3 * (m - 6).coerceAtLeast(0).coerceAtMost(18) + 0.17 * (m - 24).coerceAtLeast(0)
            val sd = 0.4 + 0.035 * m
            median to sd
        } else {
            val median = 3.2 + 0.65 * m.coerceAtMost(6) + 0.28 * (m - 6).coerceAtLeast(0).coerceAtMost(18) + 0.17 * (m - 24).coerceAtLeast(0)
            val sd = 0.4 + 0.035 * m
            median to sd
        }
    }

    private fun getHeightStats(months: Int, isMale: Boolean): Pair<Double, Double> {
        val m = months.coerceIn(0, 60)
        return if (isMale) {
            val median = 49.9 + 2.9 * m.coerceAtMost(6) + 1.35 * (m - 6).coerceAtLeast(0).coerceAtMost(18) + 0.65 * (m - 24).coerceAtLeast(0)
            val sd = 1.9 + 0.05 * m
            median to sd
        } else {
            val median = 49.1 + 2.7 * m.coerceAtMost(6) + 1.35 * (m - 6).coerceAtLeast(0).coerceAtMost(18) + 0.65 * (m - 24).coerceAtLeast(0)
            val sd = 1.9 + 0.05 * m
            median to sd
        }
    }

    private fun getBmiStats(months: Int, isMale: Boolean): Pair<Double, Double> {
        val m = months.coerceIn(0, 60)
        val median = if (isMale) 13.4 + (m * 0.06).coerceAtMost(3.0) - ((m - 12).coerceAtLeast(0) * 0.04)
        else 13.2 + (m * 0.06).coerceAtMost(3.0) - ((m - 12).coerceAtLeast(0) * 0.04)
        val sd = 1.2
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
}
