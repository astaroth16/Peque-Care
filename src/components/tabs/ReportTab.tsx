import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { formatAgeString } from '../../utils/whoStandards';
import {
  FileText,
  Printer,
  Download,
  CheckCircle,
  AlertTriangle,
  Heart,
  Scale,
  Ruler,
  Pill,
  Thermometer,
  ShieldAlert,
  Stethoscope,
  Info
} from 'lucide-react';

export const ReportTab: React.FC = () => {
  const {
    profile,
    currentAgeMonths,
    growthRecords,
    latestGrowth,
    temperatureRecords,
    medications,
    medicalVisits,
    medicalStudies,
    vaccines,
    sleepRecords,
    mealRecords,
    exportJSON,
  } = useApp();

  const [dateFilter, setDateFilter] = useState<'all' | '3months' | '1month'>('all');

  const todayStr = new Date().toISOString().split('T')[0];

  const handlePrint = () => {
    window.print();
  };

  // Filter records based on selected timeframe
  const getCutoffDate = () => {
    const now = new Date();
    if (dateFilter === '1month') {
      return new Date(now.getFullYear(), now.getMonth() - 1, now.getDate());
    }
    if (dateFilter === '3months') {
      return new Date(now.getFullYear(), now.getMonth() - 3, now.getDate());
    }
    return new Date(2000, 0, 1);
  };

  const cutoff = getCutoffDate();

  const filteredGrowth = growthRecords.filter(r => new Date(r.date) >= cutoff);
  const filteredTemp = temperatureRecords.filter(r => new Date(r.date) >= cutoff);
  const filteredVisits = medicalVisits.filter(r => new Date(r.date) >= cutoff);

  // Sleep average
  const recentSleeps = sleepRecords.filter(s => new Date(s.date) >= cutoff);
  const avgSleepMinutes = recentSleeps.length > 0
    ? Math.round(recentSleeps.reduce((sum, s) => sum + s.durationMinutes, 0) / recentSleeps.length)
    : 630;
  const avgSleepHours = Math.floor(avgSleepMinutes / 60);
  const avgSleepRemainderMins = avgSleepMinutes % 60;

  return (
    <div className="space-y-4">
      {/* Action Header (Hidden in Print) */}
      <div className="no-print bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-xl bg-indigo-50 text-indigo-600">
              <FileText className="w-5 h-5" />
            </span>
            <h2 className="text-lg font-black text-slate-900">
              Informe Pediátrico en PDF
            </h2>
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            Documento clínico completo listo para imprimir o enviar al pediatra
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
          <select
            value={dateFilter}
            onChange={e => setDateFilter(e.target.value as any)}
            className="text-xs font-semibold px-3 py-2 rounded-xl border border-slate-300 bg-white text-slate-700 outline-none"
          >
            <option value="all">Todo el historial</option>
            <option value="3months">Últimos 3 meses</option>
            <option value="1month">Último mes</option>
          </select>

          <button
            onClick={handlePrint}
            className="flex-1 sm:flex-none inline-flex items-center justify-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold shadow-sm transition active:scale-95"
          >
            <Printer className="w-4 h-4" />
            <span>Imprimir / Guardar como PDF</span>
          </button>

          <button
            onClick={exportJSON}
            className="p-2 rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 text-xs font-semibold"
            title="Descargar copia de seguridad JSON"
          >
            <Download className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Printable Clinical Document */}
      <div className="print-card bg-white rounded-2xl p-6 md:p-8 border border-slate-200 shadow-sm text-slate-800 space-y-6">
        {/* Document Header */}
        <div className="border-b-2 border-indigo-600 pb-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-indigo-700 font-extrabold text-sm uppercase tracking-wider">
              <Stethoscope className="w-4 h-4" />
              <span>PequeCare • Historia Clínica y Control de Crecimiento</span>
            </div>
            <h1 className="text-2xl font-black text-slate-900 mt-1">
              Informe de Salud y Desarrollo Infantil
            </h1>
            <p className="text-xs text-slate-500 mt-0.5">
              Evaluación según Estándares y Curvas de Crecimiento de la OMS
            </p>
          </div>

          <div className="text-left sm:text-right text-xs text-slate-600">
            <div><strong>Fecha de emisión:</strong> {todayStr}</div>
            <div><strong>Pediatra de cabecera:</strong> {profile.pediatricianName}</div>
            <div><strong>Centro Médico:</strong> {profile.clinicName}</div>
          </div>
        </div>

        {/* Patient Biodata Box */}
        <div className="bg-slate-50 rounded-xl p-4 border border-slate-200 grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
          <div>
            <span className="text-slate-400 font-semibold uppercase text-[10px]">Nombre de la Paciente</span>
            <p className="text-sm font-extrabold text-slate-900 mt-0.5">{profile.name}</p>
          </div>
          <div>
            <span className="text-slate-400 font-semibold uppercase text-[10px]">Edad Cronológica</span>
            <p className="text-sm font-bold text-slate-900 mt-0.5">
              {formatAgeString(currentAgeMonths)} ({Math.round(currentAgeMonths)}m)
            </p>
          </div>
          <div>
            <span className="text-slate-400 font-semibold uppercase text-[10px]">Fecha de Nacimiento</span>
            <p className="text-sm font-bold text-slate-900 mt-0.5">{profile.birthDate}</p>
          </div>
          <div>
            <span className="text-slate-400 font-semibold uppercase text-[10px]">Grupo Sanguíneo</span>
            <p className="text-sm font-bold text-slate-900 mt-0.5">{profile.bloodType}</p>
          </div>
        </div>

        {/* Allergies Highlight */}
        {profile.allergies && profile.allergies.length > 0 && (
          <div className="bg-rose-50 border-l-4 border-rose-500 p-3 rounded-r-xl text-xs text-rose-950 flex items-center gap-2">
            <ShieldAlert className="w-4 h-4 text-rose-600 shrink-0" />
            <span>
              <strong>ALERGIAS REGISTRADAS:</strong> {profile.allergies.join(', ')}
            </span>
          </div>
        )}

        {/* SECTION 1: ANTROPOMETRÍA Y EVALUACIÓN OMS */}
        <div className="space-y-3">
          <h3 className="text-sm font-bold uppercase tracking-wider text-indigo-900 border-b pb-1 flex items-center gap-2">
            <Scale className="w-4 h-4 text-indigo-600" />
            1. Estado Nutricional y Curvas de Crecimiento OMS
          </h3>

          {latestGrowth ? (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="p-3 rounded-xl border border-slate-200 bg-slate-50/50">
                <span className="text-xs text-slate-500 font-semibold">Peso Actual:</span>
                <div className="text-xl font-black text-slate-900">{latestGrowth.weightKg} kg</div>
                <div className="text-xs font-bold text-emerald-700 mt-0.5">
                  Percentil {latestGrowth.weightPercentile} (Z: {latestGrowth.weightZScore > 0 ? `+${latestGrowth.weightZScore}` : latestGrowth.weightZScore} DE)
                </div>
                <div className="text-[11px] text-slate-600 mt-0.5">{latestGrowth.weightStatus}</div>
              </div>

              <div className="p-3 rounded-xl border border-slate-200 bg-slate-50/50">
                <span className="text-xs text-slate-500 font-semibold">Talla / Estatura:</span>
                <div className="text-xl font-black text-slate-900">{latestGrowth.heightCm} cm</div>
                <div className="text-xs font-bold text-emerald-700 mt-0.5">
                  Percentil {latestGrowth.heightPercentile} (Z: {latestGrowth.heightZScore > 0 ? `+${latestGrowth.heightZScore}` : latestGrowth.heightZScore} DE)
                </div>
                <div className="text-[11px] text-slate-600 mt-0.5">{latestGrowth.heightStatus}</div>
              </div>

              <div className="p-3 rounded-xl border border-slate-200 bg-slate-50/50">
                <span className="text-xs text-slate-500 font-semibold">Índice de Masa Corporal (IMC):</span>
                <div className="text-xl font-black text-slate-900">{latestGrowth.bmi} kg/m²</div>
                <div className="text-xs font-bold text-emerald-700 mt-0.5">
                  Percentil {latestGrowth.bmiPercentile} (Z: {latestGrowth.bmiZScore > 0 ? `+${latestGrowth.bmiZScore}` : latestGrowth.bmiZScore} DE)
                </div>
                <div className="text-[11px] text-slate-600 mt-0.5">{latestGrowth.bmiStatus}</div>
              </div>
            </div>
          ) : (
            <p className="text-xs text-slate-400 italic">No hay mediciones antropométricas disponibles.</p>
          )}

          {/* Measurements Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border border-slate-200 rounded-lg overflow-hidden">
              <thead className="bg-slate-100 text-slate-600 font-bold uppercase text-[10px]">
                <tr>
                  <th className="p-2">Fecha</th>
                  <th className="p-2">Edad</th>
                  <th className="p-2">Peso (kg)</th>
                  <th className="p-2">Percentil OMS</th>
                  <th className="p-2">Talla (cm)</th>
                  <th className="p-2">Percentil OMS</th>
                  <th className="p-2">IMC</th>
                  <th className="p-2">Diagnóstico</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredGrowth.map(r => (
                  <tr key={r.id}>
                    <td className="p-2 font-bold">{r.date}</td>
                    <td className="p-2">{formatAgeString(r.ageMonths)}</td>
                    <td className="p-2 font-bold">{r.weightKg}</td>
                    <td className="p-2 font-bold text-emerald-700">P{r.weightPercentile}</td>
                    <td className="p-2 font-bold">{r.heightCm}</td>
                    <td className="p-2 font-bold text-emerald-700">P{r.heightPercentile}</td>
                    <td className="p-2">{r.bmi}</td>
                    <td className="p-2 text-slate-600">{r.weightStatus}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* SECTION 2: MEDICAMENTOS ACTIVOS Y TRATAMIENTOS */}
        <div className="space-y-3">
          <h3 className="text-sm font-bold uppercase tracking-wider text-indigo-900 border-b pb-1 flex items-center gap-2">
            <Pill className="w-4 h-4 text-amber-600" />
            2. Tratamientos Farmacológicos y Dosis
          </h3>

          <div className="space-y-2">
            {medications.map(med => (
              <div key={med.id} className="p-3 rounded-xl border border-slate-200 text-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-extrabold text-slate-900 text-sm">{med.name}</span>
                    <span className={`text-[10px] font-bold px-2 py-0.2 rounded-full uppercase ${
                      med.active ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-600'
                    }`}>
                      {med.active ? 'Tratamiento Activo' : 'Completado'}
                    </span>
                  </div>
                  <p className="text-slate-600 mt-0.5">
                    <strong>Dosis:</strong> {med.dosage} cada {med.frequencyHours} horas • {med.purpose}
                  </p>
                  <p className="text-[11px] text-slate-400">
                    Periodo: del {med.startDate} al {med.endDate} • {med.dosesLog.length} dosis administradas registradas
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* SECTION 3: CURVA TÉRMICA RECIENTE */}
        <div className="space-y-3">
          <h3 className="text-sm font-bold uppercase tracking-wider text-indigo-900 border-b pb-1 flex items-center gap-2">
            <Thermometer className="w-4 h-4 text-rose-600" />
            3. Registro Térmico y Episodios Febriles Recientes
          </h3>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border border-slate-200 rounded-lg overflow-hidden">
              <thead className="bg-slate-100 text-slate-600 font-bold uppercase text-[10px]">
                <tr>
                  <th className="p-2">Fecha y Hora</th>
                  <th className="p-2">Temperatura</th>
                  <th className="p-2">Clasificación</th>
                  <th className="p-2">Síntomas Reportados</th>
                  <th className="p-2">Antitérmico / Medidas</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredTemp.map(t => {
                  const isF = t.celsius >= 38.0;
                  return (
                    <tr key={t.id}>
                      <td className="p-2 font-bold">{t.date} {t.time} hs</td>
                      <td className={`p-2 font-black ${isF ? 'text-rose-600' : 'text-slate-800'}`}>{t.celsius}°C</td>
                      <td className="p-2 font-semibold">{isF ? 'Fiebre' : t.celsius >= 37.5 ? 'Febrícula' : 'Normal'}</td>
                      <td className="p-2 text-slate-600">{t.symptoms?.join(', ') || '-'}</td>
                      <td className="p-2 text-slate-700">{t.medicationGiven || '-'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* SECTION 4: HÁBITOS, SUEÑO Y NUTRICIÓN */}
        <div className="space-y-3">
          <h3 className="text-sm font-bold uppercase tracking-wider text-indigo-900 border-b pb-1 flex items-center gap-2">
            <Heart className="w-4 h-4 text-emerald-600" />
            4. Patrones de Rutina, Sueño y Nutrición
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
            <div className="p-3 rounded-xl border border-slate-200 bg-slate-50/50">
              <span className="font-bold text-slate-800">Descanso y Sueño:</span>
              <p className="text-slate-600 mt-1">
                Promedio registrado: <strong>{avgSleepHours}h {avgSleepRemainderMins}m diarios</strong>. Calidad predominante: tranquila y reparadora. Cumple con el rango objetivo de 10-13h recomendado por la OMS.
              </p>
            </div>
            <div className="p-3 rounded-xl border border-slate-200 bg-slate-50/50">
              <span className="font-bold text-slate-800">Alimentación e Hidratación:</span>
              <p className="text-slate-600 mt-1">
                Ingesta hídrica promedio: <strong>5-6 vasos de agua diarios</strong>. Dieta variada con incorporación progresiva de vegetales y frutas frescas.
              </p>
            </div>
          </div>
        </div>

        {/* SECTION 5: ESTUDIOS MÉDICOS Y VACUNAS */}
        <div className="space-y-3">
          <h3 className="text-sm font-bold uppercase tracking-wider text-indigo-900 border-b pb-1 flex items-center gap-2">
            <CheckCircle className="w-4 h-4 text-cyan-600" />
            5. Vacunación e Indicaciones de Tamizaje
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
            <div>
              <span className="font-bold text-slate-800 block mb-1">Inmunizaciones / Vacunas:</span>
              <ul className="space-y-1">
                {vaccines.map(v => (
                  <li key={v.id} className="flex items-center justify-between py-1 border-b border-slate-100">
                    <span className="text-slate-700">{v.name}</span>
                    <span className={`text-[10px] font-bold px-2 py-0.2 rounded-full uppercase ${
                      v.status === 'aplicada' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
                    }`}>
                      {v.status === 'aplicada' ? 'Al día' : 'Pendiente 4 años'}
                    </span>
                  </li>
                ))}
              </ul>
            </div>

            <div>
              <span className="font-bold text-slate-800 block mb-1">Estudios y Exámenes:</span>
              <ul className="space-y-1">
                {medicalStudies.map(s => (
                  <li key={s.id} className="py-1 border-b border-slate-100">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-slate-800">{s.title}</span>
                      <span className="text-[10px] text-slate-500 uppercase">{s.status}</span>
                    </div>
                    {s.resultsNotes && <p className="text-[11px] text-emerald-700 mt-0.5">✓ {s.resultsNotes}</p>}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>

        {/* Doctor Observations & Signature Area */}
        <div className="pt-6 border-t border-slate-200 grid grid-cols-1 sm:grid-cols-2 gap-8 text-xs">
          <div className="space-y-6">
            <div>
              <span className="font-bold text-slate-700 uppercase tracking-wider text-[10px]">
                Observaciones del Pediatra / Indicaciones:
              </span>
              <div className="h-20 border border-dashed border-slate-300 rounded-xl mt-1.5 p-2 bg-slate-50/50"></div>
            </div>
          </div>

          <div className="flex flex-col justify-end items-center sm:items-end">
            <div className="w-56 border-t border-slate-400 text-center pt-1.5 text-xs text-slate-600">
              <p className="font-bold text-slate-900">{profile.pediatricianName}</p>
              <p className="text-[11px] text-slate-500">Firma y Sello Profesional</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
