import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { X, Ruler, Scale, Sparkles, AlertCircle } from 'lucide-react';
import { calculateBMI, evaluateGrowthMetric, calculateAgeMonths } from '../../utils/whoStandards';

interface GrowthModalProps {
  onClose: () => void;
}

export const GrowthModal: React.FC<GrowthModalProps> = ({ onClose }) => {
  const { profile, addGrowthRecord } = useApp();

  const todayStr = new Date().toISOString().split('T')[0];
  const [date, setDate] = useState(todayStr);
  const [weightKg, setWeightKg] = useState<number>(15.5);
  const [heightCm, setHeightCm] = useState<number>(101.5);
  const [headCircumferenceCm, setHeadCircumferenceCm] = useState<number>(50.2);
  const [notes, setNotes] = useState('');

  // Live WHO Preview
  const ageMonths = calculateAgeMonths(profile.birthDate, date);
  const bmi = calculateBMI(weightKg, heightCm);
  const weightEval = evaluateGrowthMetric('weight', ageMonths, weightKg);
  const heightEval = evaluateGrowthMetric('height', ageMonths, heightCm);
  const bmiEval = evaluateGrowthMetric('bmi', ageMonths, bmi);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!weightKg || !heightCm) return;

    addGrowthRecord({
      date,
      weightKg: Number(weightKg),
      heightCm: Number(heightCm),
      headCircumferenceCm: headCircumferenceCm ? Number(headCircumferenceCm) : undefined,
      notes: notes.trim() || undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="bg-gradient-to-r from-indigo-600 to-indigo-700 p-4 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-white/20">
              <Scale className="w-5 h-5 text-white" />
            </span>
            <div>
              <h3 className="font-bold text-base">Registrar Crecimiento</h3>
              <p className="text-xs text-indigo-100">Cálculo automático de Curvas OMS</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-white/80 hover:text-white hover:bg-white/10 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4 max-h-[80vh] overflow-y-auto">
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Fecha de la Medición
            </label>
            <input
              type="date"
              value={date}
              onChange={e => setDate(e.target.value)}
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none"
              required
            />
            <p className="text-[11px] text-slate-500 mt-1">
              Edad a esta fecha: <strong>{ageMonths} meses</strong>
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Scale className="w-3.5 h-3.5 text-indigo-600" />
                Peso (kg) *
              </label>
              <input
                type="number"
                step="0.05"
                min="5"
                max="40"
                value={weightKg}
                onChange={e => setWeightKg(parseFloat(e.target.value) || 0)}
                placeholder="Ej. 15.3"
                className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm font-semibold text-slate-900 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Ruler className="w-3.5 h-3.5 text-indigo-600" />
                Talla / Altura (cm) *
              </label>
              <input
                type="number"
                step="0.1"
                min="60"
                max="150"
                value={heightCm}
                onChange={e => setHeightCm(parseFloat(e.target.value) || 0)}
                placeholder="Ej. 101.5"
                className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm font-semibold text-slate-900 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none"
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Perímetro Cefálico (cm, opcional)
            </label>
            <input
              type="number"
              step="0.1"
              min="35"
              max="60"
              value={headCircumferenceCm}
              onChange={e => setHeadCircumferenceCm(parseFloat(e.target.value) || 0)}
              placeholder="Ej. 50.0"
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none"
            />
          </div>

          {/* Live OMS Classification Preview Box */}
          <div className="rounded-xl p-3.5 bg-slate-50 border border-slate-200 space-y-2">
            <div className="flex items-center gap-1.5 text-xs font-bold text-indigo-900">
              <Sparkles className="w-4 h-4 text-indigo-600" />
              <span>Diagnóstico Automático OMS en Tiempo Real</span>
            </div>

            <div className="grid grid-cols-3 gap-2 text-center pt-1">
              <div className="p-2 rounded-lg bg-white border border-slate-100 shadow-xs">
                <span className="text-[10px] text-slate-400 font-bold uppercase">Percentil Peso</span>
                <p className="text-base font-extrabold text-indigo-600">P{weightEval.percentile}</p>
                <span className="text-[9px] text-slate-500 leading-tight block truncate">{weightEval.status}</span>
              </div>
              <div className="p-2 rounded-lg bg-white border border-slate-100 shadow-xs">
                <span className="text-[10px] text-slate-400 font-bold uppercase">Percentil Talla</span>
                <p className="text-base font-extrabold text-indigo-600">P{heightEval.percentile}</p>
                <span className="text-[9px] text-slate-500 leading-tight block truncate">{heightEval.status}</span>
              </div>
              <div className="p-2 rounded-lg bg-white border border-slate-100 shadow-xs">
                <span className="text-[10px] text-slate-400 font-bold uppercase">IMC Calculado</span>
                <p className="text-base font-extrabold text-emerald-600">{bmi}</p>
                <span className="text-[9px] text-slate-500 leading-tight block truncate">P{bmiEval.percentile} ({bmiEval.status})</span>
              </div>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Observaciones / Notas del Pediatra
            </label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value)}
              rows={2}
              placeholder="Ej. Control de 3 años y medio en consultorio, buena ganancia de peso..."
              className="w-full rounded-xl border border-slate-300 p-2.5 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none"
            />
          </div>

          <div className="pt-2 flex items-center justify-end gap-2.5">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold rounded-xl text-slate-600 hover:bg-slate-100 transition"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 text-xs font-bold rounded-xl bg-indigo-600 text-white hover:bg-indigo-700 shadow-sm transition"
            >
              Guardar en Historial OMS
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
