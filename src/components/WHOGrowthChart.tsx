import React, { useState } from 'react';
import {
  WHO_GIRLS_WEIGHT,
  WHO_GIRLS_HEIGHT,
  WHO_GIRLS_BMI,
  formatAgeString,
  WHODataPoint
} from '../utils/whoStandards';
import { GrowthRecord } from '../types';
import { Activity, Info, TrendingUp } from 'lucide-react';

interface WHOGrowthChartProps {
  records: GrowthRecord[];
  childName: string;
}

export const WHOGrowthChart: React.FC<WHOGrowthChartProps> = ({ records, childName }) => {
  const [metric, setMetric] = useState<'weight' | 'height' | 'bmi'>('weight');
  const [selectedPoint, setSelectedPoint] = useState<GrowthRecord | null>(null);

  // Configuration by metric
  const config = {
    weight: {
      title: 'Peso para la Edad (Niñas 3 a 6 años)',
      unit: 'kg',
      whoData: WHO_GIRLS_WEIGHT,
      yMin: 9,
      yMax: 32,
      yStep: 3,
      getValue: (r: GrowthRecord) => r.weightKg,
      getPercentile: (r: GrowthRecord) => r.weightPercentile,
      getZScore: (r: GrowthRecord) => r.weightZScore,
      getStatus: (r: GrowthRecord) => r.weightStatus,
    },
    height: {
      title: 'Talla para la Edad (Niñas 3 a 6 años)',
      unit: 'cm',
      whoData: WHO_GIRLS_HEIGHT,
      yMin: 85,
      yMax: 135,
      yStep: 10,
      getValue: (r: GrowthRecord) => r.heightCm,
      getPercentile: (r: GrowthRecord) => r.heightPercentile,
      getZScore: (r: GrowthRecord) => r.heightZScore,
      getStatus: (r: GrowthRecord) => r.heightStatus,
    },
    bmi: {
      title: 'Índice de Masa Corporal (IMC) para la Edad',
      unit: 'kg/m²',
      whoData: WHO_GIRLS_BMI,
      yMin: 12,
      yMax: 20,
      yStep: 1,
      getValue: (r: GrowthRecord) => r.bmi,
      getPercentile: (r: GrowthRecord) => r.bmiPercentile,
      getZScore: (r: GrowthRecord) => r.bmiZScore,
      getStatus: (r: GrowthRecord) => r.bmiStatus,
    },
  }[metric];

  // SVG Chart Geometry
  const width = 640;
  const height = 360;
  const padding = { top: 25, right: 35, bottom: 45, left: 50 };
  const innerWidth = width - padding.left - padding.right;
  const innerHeight = height - padding.top - padding.bottom;

  const minMonth = 36;
  const maxMonth = 72;

  // Scale functions
  const scaleX = (month: number) => {
    const clamped = Math.min(Math.max(month, minMonth), maxMonth);
    return padding.left + ((clamped - minMonth) / (maxMonth - minMonth)) * innerWidth;
  };

  const scaleY = (val: number) => {
    const clamped = Math.min(Math.max(val, config.yMin), config.yMax);
    return padding.top + innerHeight - ((clamped - config.yMin) / (config.yMax - config.yMin)) * innerHeight;
  };

  // Generate SVG path for a line through data points
  const generatePath = (data: WHODataPoint[], key: keyof WHODataPoint) => {
    return data
      .map((d, i) => {
        const x = scaleX(d.month);
        const y = scaleY(d[key] as number);
        return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
      })
      .join(' ');
  };

  // Generate shaded area path between -2SD and +2SD (Normal WHO band)
  const generateBandPath = (data: WHODataPoint[]) => {
    const topPoints = data.map(d => `${scaleX(d.month).toFixed(1)},${scaleY(d.sdPos2).toFixed(1)}`);
    const bottomPoints = [...data].reverse().map(d => `${scaleX(d.month).toFixed(1)},${scaleY(d.sdNeg2).toFixed(1)}`);
    return `M ${topPoints.join(' L ')} L ${bottomPoints.join(' L ')} Z`;
  };

  // Inner shaded area between -1SD and +1SD (Optimal Median band)
  const generateOptimalBandPath = (data: WHODataPoint[]) => {
    const topPoints = data.map(d => `${scaleX(d.month).toFixed(1)},${scaleY(d.sdPos1).toFixed(1)}`);
    const bottomPoints = [...data].reverse().map(d => `${scaleX(d.month).toFixed(1)},${scaleY(d.sdNeg1).toFixed(1)}`);
    return `M ${topPoints.join(' L ')} L ${bottomPoints.join(' L ')} Z`;
  };

  // Y-axis grid ticks
  const yTicks: number[] = [];
  for (let val = config.yMin; val <= config.yMax; val += config.yStep) {
    yTicks.push(val);
  }

  // X-axis ticks (months)
  const xTicks = [36, 42, 48, 54, 60, 66, 72];

  // Child data points sorted by age
  const sortedRecords = [...records].sort((a, b) => a.ageMonths - b.ageMonths);
  const childPointsPath = sortedRecords
    .map((r, i) => {
      const x = scaleX(r.ageMonths);
      const y = scaleY(config.getValue(r));
      return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(' ');

  const latestRecord = sortedRecords.length > 0 ? sortedRecords[sortedRecords.length - 1] : null;

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-4 md:p-5">
      {/* Metric Selector Tabs */}
      <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-indigo-50 text-indigo-600">
              <TrendingUp className="w-5 h-5" />
            </span>
            <h3 className="font-bold text-slate-800 text-base md:text-lg">
              Curvas de Crecimiento Oficiales OMS
            </h3>
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            Comparativa automática de {childName} con los estándares de la OMS
          </p>
        </div>

        <div className="inline-flex rounded-xl bg-slate-100 p-1 border border-slate-200">
          <button
            onClick={() => { setMetric('weight'); setSelectedPoint(null); }}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-all ${
              metric === 'weight'
                ? 'bg-white text-indigo-600 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Peso (kg)
          </button>
          <button
            onClick={() => { setMetric('height'); setSelectedPoint(null); }}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-all ${
              metric === 'height'
                ? 'bg-white text-indigo-600 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Talla (cm)
          </button>
          <button
            onClick={() => { setMetric('bmi'); setSelectedPoint(null); }}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-all ${
              metric === 'bmi'
                ? 'bg-white text-indigo-600 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            IMC (kg/m²)
          </button>
        </div>
      </div>

      {/* SVG Chart Container */}
      <div className="relative w-full overflow-x-auto bg-slate-50/60 rounded-xl p-2 border border-slate-100">
        <svg
          viewBox={`0 0 ${width} ${height}`}
          className="w-full h-auto min-w-[540px] select-none"
        >
          <defs>
            <linearGradient id="normalBandGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#10B981" stopOpacity="0.16" />
              <stop offset="50%" stopColor="#06B6D4" stopOpacity="0.10" />
              <stop offset="100%" stopColor="#10B981" stopOpacity="0.16" />
            </linearGradient>
            <linearGradient id="optimalBandGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#10B981" stopOpacity="0.22" />
              <stop offset="100%" stopColor="#10B981" stopOpacity="0.22" />
            </linearGradient>
            <linearGradient id="childLineGrad" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor="#6366F1" />
              <stop offset="100%" stopColor="#4F46E5" />
            </linearGradient>
            <filter id="dotGlow" x="-50%" y="-50%" width="200%" height="200%">
              <feDropShadow dx="0" dy="2" stdDeviation="3" floodColor="#4F46E5" floodOpacity="0.4" />
            </filter>
          </defs>

          {/* Grid lines: Y */}
          {yTicks.map(val => {
            const y = scaleY(val);
            return (
              <g key={`y-${val}`}>
                <line
                  x1={padding.left}
                  y1={y}
                  x2={width - padding.right}
                  y2={y}
                  stroke="#E2E8F0"
                  strokeWidth="1"
                  strokeDasharray="2 3"
                />
                <text
                  x={padding.left - 8}
                  y={y + 4}
                  textAnchor="end"
                  fontSize="11"
                  fill="#64748B"
                  fontWeight="500"
                >
                  {val}
                </text>
              </g>
            );
          })}

          {/* Grid lines: X */}
          {xTicks.map(month => {
            const x = scaleX(month);
            const years = month / 12;
            return (
              <g key={`x-${month}`}>
                <line
                  x1={x}
                  y1={padding.top}
                  x2={x}
                  y2={height - padding.bottom}
                  stroke="#E2E8F0"
                  strokeWidth="1"
                  strokeDasharray="2 3"
                />
                <text
                  x={x}
                  y={height - padding.bottom + 16}
                  textAnchor="middle"
                  fontSize="11"
                  fill="#64748B"
                  fontWeight="600"
                >
                  {month}m
                </text>
                <text
                  x={x}
                  y={height - padding.bottom + 29}
                  textAnchor="middle"
                  fontSize="10"
                  fill="#94A3B8"
                >
                  ({years}a)
                </text>
              </g>
            );
          })}

          {/* WHO Shaded Area: Normal Range -2SD to +2SD (P3 to P97) */}
          <path
            d={generateBandPath(config.whoData)}
            fill="url(#normalBandGrad)"
          />

          {/* WHO Shaded Area: Optimal Range -1SD to +1SD (P15 to P85) */}
          <path
            d={generateOptimalBandPath(config.whoData)}
            fill="url(#optimalBandGrad)"
          />

          {/* WHO Curves */}
          {/* +2 SD (P97) */}
          <path
            d={generatePath(config.whoData, 'sdPos2')}
            fill="none"
            stroke="#94A3B8"
            strokeWidth="1.5"
            strokeDasharray="3 3"
          />
          <text
            x={scaleX(72) + 5}
            y={scaleY(config.whoData[config.whoData.length - 1].sdPos2) + 3}
            fontSize="9"
            fill="#64748B"
            fontWeight="600"
          >
            +2 DE (P97)
          </text>

          {/* +1 SD (P85) */}
          <path
            d={generatePath(config.whoData, 'sdPos1')}
            fill="none"
            stroke="#CBD5E1"
            strokeWidth="1.2"
          />

          {/* Mediana OMS (P50) - Centro */}
          <path
            d={generatePath(config.whoData, 'median')}
            fill="none"
            stroke="#10B981"
            strokeWidth="2.2"
          />
          <text
            x={scaleX(72) + 5}
            y={scaleY(config.whoData[config.whoData.length - 1].median) + 3}
            fontSize="10"
            fill="#059669"
            fontWeight="bold"
          >
            OMS P50
          </text>

          {/* -1 SD (P15) */}
          <path
            d={generatePath(config.whoData, 'sdNeg1')}
            fill="none"
            stroke="#CBD5E1"
            strokeWidth="1.2"
          />

          {/* -2 SD (P3) */}
          <path
            d={generatePath(config.whoData, 'sdNeg2')}
            fill="none"
            stroke="#94A3B8"
            strokeWidth="1.5"
            strokeDasharray="3 3"
          />
          <text
            x={scaleX(72) + 5}
            y={scaleY(config.whoData[config.whoData.length - 1].sdNeg2) + 3}
            fontSize="9"
            fill="#64748B"
            fontWeight="600"
          >
            -2 DE (P3)
          </text>

          {/* Child's Trajectory Line */}
          {sortedRecords.length > 1 && (
            <path
              d={childPointsPath}
              fill="none"
              stroke="url(#childLineGrad)"
              strokeWidth="3.2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          )}

          {/* Child's Data Points */}
          {sortedRecords.map((rec, index) => {
            const x = scaleX(rec.ageMonths);
            const y = scaleY(config.getValue(rec));
            const isLatest = index === sortedRecords.length - 1;
            const isSelected = selectedPoint?.id === rec.id;

            return (
              <g
                key={rec.id}
                className="cursor-pointer transition-transform hover:scale-125"
                onClick={() => setSelectedPoint(rec)}
              >
                {/* Glow ring for latest measurement */}
                {isLatest && (
                  <circle
                    cx={x}
                    cy={y}
                    r="12"
                    fill="#6366F1"
                    fillOpacity="0.2"
                    className="animate-pulse"
                  />
                )}
                {/* Outer halo */}
                <circle
                  cx={x}
                  cy={y}
                  r={isSelected ? 9 : 7}
                  fill="white"
                  stroke="#4F46E5"
                  strokeWidth={isSelected ? 3.5 : 2.5}
                  filter="url(#dotGlow)"
                />
                {/* Inner center */}
                <circle
                  cx={x}
                  cy={y}
                  r="3.5"
                  fill="#4F46E5"
                />
              </g>
            );
          })}
        </svg>
      </div>

      {/* Chart Legend */}
      <div className="flex flex-wrap items-center justify-between text-xs gap-3 mt-3 pt-3 border-t border-slate-100 text-slate-600">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-1.5">
            <span className="w-3.5 h-1.5 bg-emerald-500 rounded-full"></span>
            <span className="font-medium text-slate-700">Mediana OMS (P50)</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3.5 h-3 bg-emerald-100 border border-emerald-300 rounded-sm"></span>
            <span>Rango Normal OMS (-2 a +2 DE)</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded-full bg-indigo-600 border border-white shadow-xs"></span>
            <span className="font-semibold text-indigo-700">Mediciones de {childName}</span>
          </div>
        </div>
        <div className="text-slate-400 text-[11px] italic">
          Toca cualquier punto para ver el diagnóstico
        </div>
      </div>

      {/* Detail Card for Selected Point or Latest */}
      {(() => {
        const activeRec = selectedPoint || latestRecord;
        if (!activeRec) return null;

        const val = config.getValue(activeRec);
        const p = config.getPercentile(activeRec);
        const z = config.getZScore(activeRec);
        const status = config.getStatus(activeRec);
        const isNormal = Math.abs(z) <= 2;

        return (
          <div className="mt-4 p-3.5 rounded-xl bg-gradient-to-r from-slate-50 to-indigo-50/40 border border-slate-200 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div className="flex items-start gap-3">
              <div className={`p-2 rounded-xl mt-0.5 ${isNormal ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}`}>
                <Activity className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                    {selectedPoint ? 'Medición seleccionada' : 'Última medición registrada'}
                  </span>
                  <span className="text-xs text-slate-400">({activeRec.date})</span>
                </div>
                <div className="flex items-baseline gap-2 mt-0.5">
                  <span className="text-xl font-bold text-slate-900">
                    {val} {config.unit}
                  </span>
                  <span className="text-xs text-slate-600">
                    a los {formatAgeString(activeRec.ageMonths)}
                  </span>
                </div>
                <div className="flex flex-wrap items-center gap-2 mt-1.5">
                  <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-bold border ${
                    isNormal ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-amber-50 text-amber-700 border-amber-200'
                  }`}>
                    Percentil {p} (Z-Score: {z > 0 ? `+${z}` : z})
                  </span>
                  <span className="text-xs font-medium text-slate-700">
                    {status}
                  </span>
                </div>
              </div>
            </div>

            <div className="text-xs text-slate-500 sm:text-right max-w-xs">
              <div className="flex items-center sm:justify-end gap-1 text-indigo-700 font-medium">
                <Info className="w-3.5 h-3.5" />
                <span>Dictamen OMS:</span>
              </div>
              <p className="mt-0.5 text-slate-600">
                {isNormal
                  ? 'El crecimiento se encuentra dentro del rango de normalidad y desarrollo saludable esperado por la OMS.'
                  : 'Se aconseja comentar la curva con el pediatra en la próxima consulta.'}
              </p>
            </div>
          </div>
        );
      })()}
    </div>
  );
};
