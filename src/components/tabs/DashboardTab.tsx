import React from 'react';
import { useApp } from '../../context/AppContext';
import { formatAgeString } from '../../utils/whoStandards';
import {
  Heart,
  Scale,
  Ruler,
  Thermometer,
  Pill,
  Moon,
  Utensils,
  Droplets,
  Calendar,
  AlertTriangle,
  Plus,
  ArrowRight,
  ShieldAlert,
  Clock,
  Sparkles,
  ChevronRight
} from 'lucide-react';

export const DashboardTab: React.FC = () => {
  const {
    profile,
    currentAgeMonths,
    latestGrowth,
    latestTemperature,
    medications,
    recordDoseAdministered,
    todaySleepMinutes,
    todayWaterGlasses,
    incrementWater,
    decrementWater,
    mealRecords,
    medicalVisits,
    medicalStudies,
    setActiveTab,
    setActiveModal,
  } = useApp();

  const todayStr = new Date().toISOString().split('T')[0];
  const activeMeds = medications.filter(m => m.active);

  // Next upcoming medication
  const nextMed = activeMeds.length > 0
    ? [...activeMeds].sort((a, b) => new Date(a.nextDoseTime).getTime() - new Date(b.nextDoseTime).getTime())[0]
    : null;

  // Upcoming medical visit
  const upcomingVisit = medicalVisits
    .filter(v => v.status === 'programada' && new Date(v.date) >= new Date(todayStr))
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())[0];

  // Pending studies
  const pendingStudies = medicalStudies.filter(s => s.status === 'pendiente' || s.status === 'programado');

  const sleepHours = Math.floor(todaySleepMinutes / 60);
  const sleepRemainderMins = todaySleepMinutes % 60;
  // Sleep goal for 3+ years is ~11 hours (660 mins)
  const sleepGoalMinutes = 660;
  const sleepProgressPercent = Math.min(100, Math.round((todaySleepMinutes / sleepGoalMinutes) * 100));

  // Water goal: 5 glasses (approx 1L - 1.2L)
  const waterGoal = 5;

  const isFever = latestTemperature && latestTemperature.celsius >= 38.0;

  return (
    <div className="space-y-4">
      {/* Child Profile Welcome Card - Maternal Theme */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-rose-500 via-rose-600 to-amber-500 p-5 text-white shadow-md">
        {/* Subtle decorative heart watermark */}
        <div className="absolute -right-6 -bottom-6 w-36 h-36 opacity-10 pointer-events-none">
          <Heart className="w-full h-full fill-white" />
        </div>

        <div className="relative z-10 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3.5">
            {/* Avatar */}
            <div className="relative w-14 h-14 rounded-2xl bg-white/20 border-2 border-white/50 flex items-center justify-center text-3xl shadow-inner shrink-0">
              {profile.gender === 'male' ? '👦' : '👧'}
              <span className="absolute -bottom-1 -right-1 w-4 h-4 rounded-full bg-emerald-400 border-2 border-rose-600"></span>
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-xl font-black tracking-tight">
                  {profile.name}
                </h2>
                <span className="text-xs bg-white/20 px-2 py-0.5 rounded-full font-bold">
                  {profile.bloodType}
                </span>
              </div>
              <p className="text-xs text-rose-100 font-medium mt-0.5">
                {formatAgeString(currentAgeMonths)} ({Math.round(currentAgeMonths)} meses)
              </p>
              <div className="flex items-center gap-2 text-[11px] text-rose-200 mt-1">
                <span>Pediatra: {profile.pediatricianName}</span>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <button
              onClick={() => setActiveTab('crecimiento')}
              className="text-xs font-semibold px-3 py-1.5 rounded-xl bg-white/20 hover:bg-white/30 backdrop-blur-xs text-white transition flex items-center gap-1.5"
            >
              <span>Curva OMS</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
            <button
              onClick={() => setActiveTab('configuracion')}
              className="text-xs font-bold px-3 py-1.5 rounded-xl bg-white text-rose-800 hover:bg-rose-50 shadow-xs transition"
            >
              Nube / Drive
            </button>
          </div>
        </div>

        {/* Allergies Warning if present */}
        {profile.allergies && profile.allergies.length > 0 && (
          <div className="mt-3.5 pt-3 border-t border-white/20 flex items-center gap-2 text-xs text-amber-100 font-medium">
            <ShieldAlert className="w-4 h-4 text-amber-200 shrink-0" />
            <span>
              <strong>Alergias conocidas:</strong> {profile.allergies.join(', ')}
            </span>
          </div>
        )}
      </div>

      {/* Vital Metrics Snapshot Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
        {/* Weight */}
        <div
          onClick={() => setActiveTab('crecimiento')}
          className="bg-white rounded-2xl p-3.5 border border-slate-200/80 shadow-2xs cursor-pointer hover:border-indigo-300 transition"
        >
          <div className="flex items-center justify-between text-slate-500 mb-1">
            <span className="text-[11px] font-bold uppercase tracking-wider">Peso OMS</span>
            <Scale className="w-4 h-4 text-indigo-600" />
          </div>
          <div className="text-xl font-black text-slate-900">
            {latestGrowth ? `${latestGrowth.weightKg} kg` : '--'}
          </div>
          <div className="text-[11px] font-semibold text-emerald-600 mt-0.5 truncate">
            {latestGrowth ? `Percentil ${latestGrowth.weightPercentile} (Normal)` : 'Sin datos'}
          </div>
        </div>

        {/* Height */}
        <div
          onClick={() => setActiveTab('crecimiento')}
          className="bg-white rounded-2xl p-3.5 border border-slate-200/80 shadow-2xs cursor-pointer hover:border-indigo-300 transition"
        >
          <div className="flex items-center justify-between text-slate-500 mb-1">
            <span className="text-[11px] font-bold uppercase tracking-wider">Talla OMS</span>
            <Ruler className="w-4 h-4 text-indigo-600" />
          </div>
          <div className="text-xl font-black text-slate-900">
            {latestGrowth ? `${latestGrowth.heightCm} cm` : '--'}
          </div>
          <div className="text-[11px] font-semibold text-emerald-600 mt-0.5 truncate">
            {latestGrowth ? `Percentil ${latestGrowth.heightPercentile} (Normal)` : 'Sin datos'}
          </div>
        </div>

        {/* Temperature */}
        <div
          onClick={() => setActiveModal('temperature')}
          className={`rounded-2xl p-3.5 border shadow-2xs cursor-pointer transition ${
            isFever
              ? 'bg-rose-50 border-rose-300 text-rose-900'
              : 'bg-white border-slate-200/80 text-slate-900 hover:border-teal-300'
          }`}
        >
          <div className="flex items-center justify-between text-slate-500 mb-1">
            <span className="text-[11px] font-bold uppercase tracking-wider">Temperatura</span>
            <Thermometer className={`w-4 h-4 ${isFever ? 'text-rose-600 animate-pulse' : 'text-teal-600'}`} />
          </div>
          <div className={`text-xl font-black ${isFever ? 'text-rose-600' : 'text-slate-900'}`}>
            {latestTemperature ? `${latestTemperature.celsius}°C` : '36.8°C'}
          </div>
          <div className={`text-[11px] font-semibold mt-0.5 truncate ${isFever ? 'text-rose-700 font-bold' : 'text-teal-600'}`}>
            {latestTemperature
              ? isFever
                ? 'Fiebre activa'
                : latestTemperature.celsius >= 37.5
                ? 'Febrícula'
                : 'Normal / Apirética'
              : 'Normal'}
          </div>
        </div>

        {/* BMI */}
        <div
          onClick={() => setActiveTab('crecimiento')}
          className="bg-white rounded-2xl p-3.5 border border-slate-200/80 shadow-2xs cursor-pointer hover:border-indigo-300 transition"
        >
          <div className="flex items-center justify-between text-slate-500 mb-1">
            <span className="text-[11px] font-bold uppercase tracking-wider">IMC Infantil</span>
            <Sparkles className="w-4 h-4 text-violet-600" />
          </div>
          <div className="text-xl font-black text-slate-900">
            {latestGrowth ? latestGrowth.bmi : '--'}
          </div>
          <div className="text-[11px] font-semibold text-indigo-600 mt-0.5 truncate">
            {latestGrowth ? `P${latestGrowth.bmiPercentile} (Saludable)` : 'OMS'}
          </div>
        </div>
      </div>

      {/* Active Medication Alert / Next Dose */}
      {nextMed && (
        <div className="rounded-2xl p-4 bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 shadow-2xs">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div className="flex items-start gap-3">
              <div className="p-2 rounded-xl bg-amber-500 text-white shadow-xs">
                <Pill className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-[11px] font-extrabold uppercase tracking-wider text-amber-800">
                    Tratamiento Activo • Próxima Dosis
                  </span>
                  <span className="inline-flex items-center gap-1 text-[10px] bg-amber-200 text-amber-900 px-2 py-0.5 rounded-full font-bold">
                    <Clock className="w-3 h-3" />
                    {nextMed.nextDoseTime.replace('T', ' ')}
                  </span>
                </div>
                <h4 className="text-base font-bold text-slate-900 mt-0.5">
                  {nextMed.name} ({nextMed.dosage})
                </h4>
                <p className="text-xs text-slate-600">
                  Frecuencia: Cada {nextMed.frequencyHours} horas • {nextMed.purpose}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2 w-full sm:w-auto">
              <button
                onClick={() => recordDoseAdministered(nextMed.id)}
                className="flex-1 sm:flex-none px-4 py-2 rounded-xl bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold shadow-xs active:scale-95 transition"
              >
                ✓ Administrar Dosis
              </button>
              <button
                onClick={() => setActiveTab('salud')}
                className="px-3 py-2 rounded-xl bg-white border border-amber-300 text-amber-900 text-xs font-semibold hover:bg-amber-100 transition"
              >
                Detalles
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Routine Trackers: Sleep & Water Intake */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        {/* Sleep Progress */}
        <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-violet-100 text-violet-700">
                <Moon className="w-4 h-4" />
              </div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                Horas de Sueño Hoy
              </h3>
            </div>
            <button
              onClick={() => setActiveModal('sleep')}
              className="text-xs font-semibold text-violet-600 hover:text-violet-800"
            >
              + Registrar
            </button>
          </div>

          <div className="flex items-baseline justify-between">
            <div className="text-2xl font-black text-slate-900">
              {sleepHours}h {sleepRemainderMins > 0 ? `${sleepRemainderMins}m` : ''}
            </div>
            <span className="text-xs text-slate-500 font-medium">
              Meta OMS: 10 - 13h
            </span>
          </div>

          <div className="w-full bg-slate-100 rounded-full h-2.5 mt-2 overflow-hidden">
            <div
              className="bg-violet-600 h-2.5 rounded-full transition-all duration-500"
              style={{ width: `${sleepProgressPercent}%` }}
            ></div>
          </div>
          <p className="text-[11px] text-slate-500 mt-1.5 flex items-center justify-between">
            <span>{sleepProgressPercent}% de la meta recomendada</span>
            <span className="font-semibold text-violet-700">Sueño de calidad</span>
          </p>
        </div>

        {/* Water Intake */}
        <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-blue-100 text-blue-700">
                <Droplets className="w-4 h-4" />
              </div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                Hidratación (Agua)
              </h3>
            </div>
            <span className="text-xs text-slate-500 font-medium">
              Meta: 5-6 vasos (1.2L)
            </span>
          </div>

          <div className="flex items-center justify-between mt-1">
            <div className="flex items-baseline gap-1">
              <span className="text-2xl font-black text-slate-900">{todayWaterGlasses}</span>
              <span className="text-xs text-slate-500">/ {waterGoal} vasos ({todayWaterGlasses * 200} ml)</span>
            </div>

            <div className="flex items-center gap-1.5">
              <button
                onClick={decrementWater}
                className="w-8 h-8 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold flex items-center justify-center transition"
              >
                -
              </button>
              <button
                onClick={incrementWater}
                className="w-8 h-8 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold flex items-center justify-center shadow-xs transition"
              >
                +
              </button>
            </div>
          </div>

          {/* Visual Glasses Icons */}
          <div className="flex gap-1.5 mt-2.5">
            {[1, 2, 3, 4, 5, 6].map(g => (
              <div
                key={g}
                className={`flex-1 h-3 rounded-full transition-all ${
                  g <= todayWaterGlasses
                    ? 'bg-blue-500'
                    : 'bg-slate-200'
                }`}
              ></div>
            ))}
          </div>
        </div>
      </div>

      {/* Upcoming Medical Checkups / Studies banner */}
      {(upcomingVisit || pendingStudies.length > 0) && (
        <div className="rounded-2xl p-4 bg-white border border-slate-200/80 shadow-2xs">
          <div className="flex items-center justify-between mb-2.5">
            <div className="flex items-center gap-2">
              <Calendar className="w-4 h-4 text-indigo-600" />
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                Próximos Controles y Estudios Médicos
              </h3>
            </div>
            <button
              onClick={() => setActiveTab('salud')}
              className="text-xs font-semibold text-indigo-600 hover:underline"
            >
              Ver todos
            </button>
          </div>

          <div className="space-y-2">
            {upcomingVisit && (
              <div className="p-3 rounded-xl bg-indigo-50/70 border border-indigo-100 flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-bold uppercase bg-indigo-200/80 text-indigo-800 px-2 py-0.5 rounded-full">
                    Turno {upcomingVisit.date} • {upcomingVisit.time} hs
                  </span>
                  <p className="text-xs font-bold text-slate-900 mt-1">
                    {upcomingVisit.doctorName} ({upcomingVisit.specialty})
                  </p>
                  <p className="text-[11px] text-slate-600">{upcomingVisit.reason} • {upcomingVisit.clinic}</p>
                </div>
                <button
                  onClick={() => setActiveTab('salud')}
                  className="p-1.5 rounded-lg text-indigo-600 hover:bg-indigo-100"
                >
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            )}

            {pendingStudies.slice(0, 1).map(study => (
              <div key={study.id} className="p-3 rounded-xl bg-cyan-50/70 border border-cyan-100 flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-bold uppercase bg-cyan-200/80 text-cyan-800 px-2 py-0.5 rounded-full">
                    Estudio Pediátrico: {study.category}
                  </span>
                  <p className="text-xs font-bold text-slate-900 mt-1">
                    {study.title}
                  </p>
                  <p className="text-[11px] text-slate-600">{study.doctorNotes || 'Pendiente de realización'}</p>
                </div>
                <button
                  onClick={() => setActiveTab('salud')}
                  className="p-1.5 rounded-lg text-cyan-600 hover:bg-cyan-100"
                >
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Quick Actions Shortcuts */}
      <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs">
        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">
          Accesos Rápidos de Registro
        </h3>
        <div className="grid grid-cols-4 sm:grid-cols-7 gap-2 text-center">
          <button
            onClick={() => setActiveModal('growth')}
            className="flex flex-col items-center p-2.5 rounded-xl hover:bg-indigo-50 group transition"
          >
            <div className="w-10 h-10 rounded-xl bg-indigo-100 text-indigo-600 flex items-center justify-center group-hover:scale-105 transition shadow-2xs">
              <Scale className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-semibold text-slate-700 mt-1.5">Crecimiento</span>
          </button>

          <button
            onClick={() => setActiveModal('medication')}
            className="flex flex-col items-center p-2.5 rounded-xl hover:bg-amber-50 group transition"
          >
            <div className="w-10 h-10 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center group-hover:scale-105 transition shadow-2xs">
              <Pill className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-semibold text-slate-700 mt-1.5">Medicamento</span>
          </button>

          <button
            onClick={() => setActiveModal('temperature')}
            className="flex flex-col items-center p-2.5 rounded-xl hover:bg-rose-50 group transition"
          >
            <div className="w-10 h-10 rounded-xl bg-rose-100 text-rose-600 flex items-center justify-center group-hover:scale-105 transition shadow-2xs">
              <Thermometer className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-semibold text-slate-700 mt-1.5">Temperatura</span>
          </button>

          <button
            onClick={() => setActiveModal('sleep')}
            className="flex flex-col items-center p-2.5 rounded-xl hover:bg-violet-50 group transition"
          >
            <div className="w-10 h-10 rounded-xl bg-violet-100 text-violet-600 flex items-center justify-center group-hover:scale-105 transition shadow-2xs">
              <Moon className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-semibold text-slate-700 mt-1.5">Sueño</span>
          </button>

          <button
            onClick={() => setActiveModal('meal')}
            className="flex flex-col items-center p-2.5 rounded-xl hover:bg-emerald-50 group transition"
          >
            <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center group-hover:scale-105 transition shadow-2xs">
              <Utensils className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-semibold text-slate-700 mt-1.5">Comida</span>
          </button>

          <button
            onClick={() => setActiveModal('activity')}
            className="flex flex-col items-center p-2.5 rounded-xl hover:bg-cyan-50 group transition"
          >
            <div className="w-10 h-10 rounded-xl bg-cyan-100 text-cyan-600 flex items-center justify-center group-hover:scale-105 transition shadow-2xs">
              <Sparkles className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-semibold text-slate-700 mt-1.5">Actividad</span>
          </button>

          <button
            onClick={() => setActiveModal('visit')}
            className="flex flex-col items-center p-2.5 rounded-xl hover:bg-blue-50 group transition"
          >
            <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center group-hover:scale-105 transition shadow-2xs">
              <Calendar className="w-5 h-5" />
            </div>
            <span className="text-[11px] font-semibold text-slate-700 mt-1.5">Consulta</span>
          </button>
        </div>
      </div>
    </div>
  );
};
