/**
 * Curvas y Estándares de Crecimiento Infantil de la OMS (Organización Mundial de la Salud)
 * Patrones de referencia para Niñas de 36 a 72 meses (3 a 6 años).
 */

export interface WHODataPoint {
  month: number;
  sdNeg2: number; // -2 SD (aprox Percentil 2.3 / 3)
  sdNeg1: number; // -1 SD (aprox Percentil 15)
  median: number; //  0 SD (Mediana, Percentil 50)
  sdPos1: number; // +1 SD (aprox Percentil 85)
  sdPos2: number; // +2 SD (aprox Percentil 97.7 / 97)
}

// OMS Niñas: Peso para la edad (kg)
export const WHO_GIRLS_WEIGHT: WHODataPoint[] = [
  { month: 36, sdNeg2: 10.8, sdNeg1: 12.2, median: 13.9, sdPos1: 16.0, sdPos2: 18.7 },
  { month: 40, sdNeg2: 11.3, sdNeg1: 12.8, median: 14.6, sdPos1: 16.9, sdPos2: 19.9 },
  { month: 42, sdNeg2: 11.6, sdNeg1: 13.1, median: 15.0, sdPos1: 17.4, sdPos2: 20.5 },
  { month: 44, sdNeg2: 11.8, sdNeg1: 13.4, median: 15.4, sdPos1: 17.9, sdPos2: 21.1 },
  { month: 48, sdNeg2: 12.3, sdNeg1: 14.0, median: 16.1, sdPos1: 18.8, sdPos2: 22.4 },
  { month: 52, sdNeg2: 12.8, sdNeg1: 14.6, median: 16.8, sdPos1: 19.8, sdPos2: 23.7 },
  { month: 54, sdNeg2: 13.0, sdNeg1: 14.9, median: 17.2, sdPos1: 20.3, sdPos2: 24.3 },
  { month: 56, sdNeg2: 13.2, sdNeg1: 15.2, median: 17.5, sdPos1: 20.8, sdPos2: 24.9 },
  { month: 60, sdNeg2: 13.7, sdNeg1: 15.8, median: 18.2, sdPos1: 21.7, sdPos2: 26.2 },
  { month: 66, sdNeg2: 14.4, sdNeg1: 16.8, median: 19.4, sdPos1: 23.3, sdPos2: 28.3 },
  { month: 72, sdNeg2: 15.3, sdNeg1: 17.8, median: 20.7, sdPos1: 25.0, sdPos2: 30.6 },
];

// OMS Niñas: Talla para la edad (cm)
export const WHO_GIRLS_HEIGHT: WHODataPoint[] = [
  { month: 36, sdNeg2: 88.7, sdNeg1: 92.0, median: 95.1, sdPos1: 98.4, sdPos2: 101.6 },
  { month: 40, sdNeg2: 91.8, sdNeg1: 95.1, median: 98.4, sdPos1: 101.8, sdPos2: 105.1 },
  { month: 42, sdNeg2: 93.3, sdNeg1: 96.7, median: 100.1, sdPos1: 103.5, sdPos2: 106.9 },
  { month: 44, sdNeg2: 94.7, sdNeg1: 98.2, median: 101.6, sdPos1: 105.1, sdPos2: 108.6 },
  { month: 48, sdNeg2: 97.6, sdNeg1: 101.2, median: 104.7, sdPos1: 108.4, sdPos2: 112.0 },
  { month: 52, sdNeg2: 100.3, sdNeg1: 104.0, median: 107.6, sdPos1: 111.5, sdPos2: 115.2 },
  { month: 54, sdNeg2: 101.6, sdNeg1: 105.4, median: 109.1, sdPos1: 113.0, sdPos2: 116.8 },
  { month: 56, sdNeg2: 102.9, sdNeg1: 106.8, median: 110.5, sdPos1: 114.5, sdPos2: 118.4 },
  { month: 60, sdNeg2: 105.3, sdNeg1: 109.4, median: 113.4, sdPos1: 117.4, sdPos2: 121.4 },
  { month: 66, sdNeg2: 109.0, sdNeg1: 113.3, median: 117.5, sdPos1: 121.7, sdPos2: 126.0 },
  { month: 72, sdNeg2: 112.5, sdNeg1: 117.0, median: 121.5, sdPos1: 126.0, sdPos2: 130.5 },
];

// OMS Niñas: IMC para la edad (kg/m²)
export const WHO_GIRLS_BMI: WHODataPoint[] = [
  { month: 36, sdNeg2: 13.5, sdNeg1: 14.4, median: 15.4, sdPos1: 16.6, sdPos2: 18.0 },
  { month: 40, sdNeg2: 13.4, sdNeg1: 14.3, median: 15.3, sdPos1: 16.5, sdPos2: 17.9 },
  { month: 42, sdNeg2: 13.3, sdNeg1: 14.2, median: 15.3, sdPos1: 16.5, sdPos2: 17.9 },
  { month: 44, sdNeg2: 13.3, sdNeg1: 14.2, median: 15.2, sdPos1: 16.4, sdPos2: 17.9 },
  { month: 48, sdNeg2: 13.2, sdNeg1: 14.1, median: 15.2, sdPos1: 16.4, sdPos2: 17.9 },
  { month: 52, sdNeg2: 13.1, sdNeg1: 14.0, median: 15.2, sdPos1: 16.5, sdPos2: 18.0 },
  { month: 54, sdNeg2: 13.1, sdNeg1: 14.0, median: 15.2, sdPos1: 16.5, sdPos2: 18.0 },
  { month: 56, sdNeg2: 13.0, sdNeg1: 14.0, median: 15.2, sdPos1: 16.5, sdPos2: 18.1 },
  { month: 60, sdNeg2: 13.0, sdNeg1: 14.0, median: 15.2, sdPos1: 16.6, sdPos2: 18.2 },
  { month: 66, sdNeg2: 13.0, sdNeg1: 14.0, median: 15.3, sdPos1: 16.8, sdPos2: 18.5 },
  { month: 72, sdNeg2: 13.0, sdNeg1: 14.1, median: 15.4, sdPos1: 17.0, sdPos2: 18.9 },
];

/**
 * Calcula la edad en meses exactos entre la fecha de nacimiento y una fecha objetivo
 */
export function calculateAgeMonths(birthDateStr: string, targetDateStr: string): number {
  const birth = new Date(birthDateStr);
  const target = new Date(targetDateStr);
  
  const yearsDiff = target.getFullYear() - birth.getFullYear();
  const monthsDiff = target.getMonth() - birth.getMonth();
  const daysDiff = target.getDate() - birth.getDate();
  
  let totalMonths = yearsDiff * 12 + monthsDiff + (daysDiff / 30.4375);
  return Math.max(0, Math.round(totalMonths * 10) / 10);
}

/**
 * Convierte edad en meses a texto legible: "3 años y 6 meses"
 */
export function formatAgeString(ageInMonths: number): string {
  const years = Math.floor(ageInMonths / 12);
  const months = Math.floor(ageInMonths % 12);
  if (years === 0) return `${months} meses`;
  if (months === 0) return `${years} ${years === 1 ? 'año' : 'años'}`;
  return `${years} ${years === 1 ? 'año' : 'años'} y ${months} ${months === 1 ? 'mes' : 'meses'}`;
}

/**
 * Calcula el Índice de Masa Corporal (IMC): kg / (m^2)
 */
export function calculateBMI(weightKg: number, heightCm: number): number {
  if (heightCm <= 0) return 0;
  const heightM = heightCm / 100;
  const bmi = weightKg / (heightM * heightM);
  return Math.round(bmi * 10) / 10;
}

/**
 * Interpola linealmente los valores de referencia OMS para una edad en meses dada
 */
export function getInterpolatedWHOValues(
  data: WHODataPoint[],
  ageMonths: number
): { sdNeg2: number; sdNeg1: number; median: number; sdPos1: number; sdPos2: number } {
  // Clamp entre rango mínimo y máximo disponible
  const clampedAge = Math.min(Math.max(ageMonths, data[0].month), data[data.length - 1].month);
  
  // Encontrar el tramo de interpolación
  for (let i = 0; i < data.length - 1; i++) {
    const p1 = data[i];
    const p2 = data[i + 1];
    if (clampedAge >= p1.month && clampedAge <= p2.month) {
      const t = (clampedAge - p1.month) / (p2.month - p1.month);
      return {
        sdNeg2: p1.sdNeg2 + t * (p2.sdNeg2 - p1.sdNeg2),
        sdNeg1: p1.sdNeg1 + t * (p2.sdNeg1 - p1.sdNeg1),
        median: p1.median + t * (p2.median - p1.median),
        sdPos1: p1.sdPos1 + t * (p2.sdPos1 - p1.sdPos1),
        sdPos2: p1.sdPos2 + t * (p2.sdPos2 - p1.sdPos2),
      };
    }
  }
  return data[data.length - 1];
}

/**
 * Aproximación estándar de función de distribución acumulativa normal estándar (CDF)
 * Convierte un Z-Score en Percentil (0 a 100)
 */
function zScoreToPercentile(z: number): number {
  // Approximation of error function erf
  const t = 1 / (1 + 0.2316419 * Math.abs(z));
  const d = 0.3989423 * Math.exp((-z * z) / 2);
  let p = d * t * (0.3193815 + t * (-0.3565638 + t * (1.781478 + t * (-1.821256 + t * 1.330274))));
  if (z > 0) p = 1 - p;
  const percentile = (1 - p) * 100;
  return Math.min(99.9, Math.max(0.1, Math.round(percentile * 10) / 10));
}

/**
 * Evalúa una métrica respecto a los patrones de la OMS
 */
export function evaluateGrowthMetric(
  metric: 'weight' | 'height' | 'bmi',
  ageMonths: number,
  value: number
): {
  zScore: number;
  percentile: number;
  status: string;
  isNormal: boolean;
  colorClass: string;
  bgLightClass: string;
} {
  let dataset = WHO_GIRLS_WEIGHT;
  if (metric === 'height') dataset = WHO_GIRLS_HEIGHT;
  if (metric === 'bmi') dataset = WHO_GIRLS_BMI;

  const ref = getInterpolatedWHOValues(dataset, ageMonths);
  
  // Estimación del Z-score usando la distancia a la mediana
  let zScore = 0;
  if (value >= ref.median) {
    const sdUnit = (ref.sdPos1 - ref.median);
    zScore = (value - ref.median) / sdUnit;
  } else {
    const sdUnit = (ref.median - ref.sdNeg1);
    zScore = (value - ref.median) / sdUnit;
  }
  zScore = Math.round(zScore * 100) / 100;
  const percentile = zScoreToPercentile(zScore);

  let status = '';
  let isNormal = true;
  let colorClass = 'text-emerald-700 border-emerald-200 bg-emerald-50';
  let bgLightClass = 'bg-emerald-50 text-emerald-800 border-emerald-300';

  if (metric === 'weight') {
    if (zScore < -2) {
      status = 'Bajo peso para la edad (-2 DE)';
      isNormal = false;
      colorClass = 'text-rose-700 border-rose-200 bg-rose-50';
      bgLightClass = 'bg-rose-50 text-rose-800 border-rose-300';
    } else if (zScore < -1) {
      status = 'Riesgo de bajo peso';
      isNormal = true;
      colorClass = 'text-amber-700 border-amber-200 bg-amber-50';
      bgLightClass = 'bg-amber-50 text-amber-800 border-amber-300';
    } else if (zScore <= 1) {
      status = 'Adecuado para la edad (Normal OMS)';
      isNormal = true;
      colorClass = 'text-emerald-700 border-emerald-200 bg-emerald-50';
      bgLightClass = 'bg-emerald-50 text-emerald-800 border-emerald-300';
    } else if (zScore <= 2) {
      status = 'Riesgo de sobrepeso';
      isNormal = true;
      colorClass = 'text-amber-700 border-amber-200 bg-amber-50';
      bgLightClass = 'bg-amber-50 text-amber-800 border-amber-300';
    } else {
      status = 'Sobrepeso para la edad (+2 DE)';
      isNormal = false;
      colorClass = 'text-rose-700 border-rose-200 bg-rose-50';
      bgLightClass = 'bg-rose-50 text-rose-800 border-rose-300';
    }
  } else if (metric === 'height') {
    if (zScore < -2) {
      status = 'Talla baja para la edad (-2 DE)';
      isNormal = false;
      colorClass = 'text-rose-700 border-rose-200 bg-rose-50';
      bgLightClass = 'bg-rose-50 text-rose-800 border-rose-300';
    } else if (zScore < -1) {
      status = 'Riesgo de talla baja';
      isNormal = true;
      colorClass = 'text-amber-700 border-amber-200 bg-amber-50';
      bgLightClass = 'bg-amber-50 text-amber-800 border-amber-300';
    } else if (zScore <= 1) {
      status = 'Talla adecuada para la edad (Normal OMS)';
      isNormal = true;
      colorClass = 'text-emerald-700 border-emerald-200 bg-emerald-50';
      bgLightClass = 'bg-emerald-50 text-emerald-800 border-emerald-300';
    } else if (zScore <= 2) {
      status = 'Talla alta para la edad';
      isNormal = true;
      colorClass = 'text-teal-700 border-teal-200 bg-teal-50';
      bgLightClass = 'bg-teal-50 text-teal-800 border-teal-300';
    } else {
      status = 'Talla muy alta (+2 DE)';
      isNormal = true;
      colorClass = 'text-blue-700 border-blue-200 bg-blue-50';
      bgLightClass = 'bg-blue-50 text-blue-800 border-blue-300';
    }
  } else {
    // BMI
    if (zScore < -2) {
      status = 'Bajo IMC / Emaciación (-2 DE)';
      isNormal = false;
      colorClass = 'text-rose-700 border-rose-200 bg-rose-50';
      bgLightClass = 'bg-rose-50 text-rose-800 border-rose-300';
    } else if (zScore < -1) {
      status = 'Riesgo de bajo IMC';
      isNormal = true;
      colorClass = 'text-amber-700 border-amber-200 bg-amber-50';
      bgLightClass = 'bg-amber-50 text-amber-800 border-amber-300';
    } else if (zScore <= 1) {
      status = 'IMC Normal y saludable (OMS)';
      isNormal = true;
      colorClass = 'text-emerald-700 border-emerald-200 bg-emerald-50';
      bgLightClass = 'bg-emerald-50 text-emerald-800 border-emerald-300';
    } else if (zScore <= 2) {
      status = 'Sobrepeso (+1 a +2 DE)';
      isNormal = false;
      colorClass = 'text-amber-700 border-amber-200 bg-amber-50';
      bgLightClass = 'bg-amber-50 text-amber-800 border-amber-300';
    } else {
      status = 'Obesidad infantil (+2 DE)';
      isNormal = false;
      colorClass = 'text-rose-700 border-rose-200 bg-rose-50';
      bgLightClass = 'bg-rose-50 text-rose-800 border-rose-300';
    }
  }

  return {
    zScore,
    percentile,
    status,
    isNormal,
    colorClass,
    bgLightClass,
  };
}
