import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { X, Thermometer, AlertTriangle, Check } from 'lucide-react';

interface TemperatureModalProps {
  onClose: () => void;
}

const COMMON_SYMPTOMS = [
  'Fiebre',
  'Decaimiento / Apatía',
  'Congestión nasal',
  'Garganta irritada / dolor al tragar',
  'Tos',
  'Mejillas calientes / rojas',
  'Dolor de panza',
  'Inapetencia',
  'Escalofríos',
];

export const TemperatureModal: React.FC<TemperatureModalProps> = ({ onClose }) => {
  const { addTemperatureRecord, medications } = useApp();

  const todayStr = new Date().toISOString().split('T')[0];
  const now = new Date();
  const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

  const [date, setDate] = useState(todayStr);
  const [time, setTime] = useState(timeStr);
  const [celsius, setCelsius] = useState<number>(37.5);
  const [selectedSymptoms, setSelectedSymptoms] = useState<string[]>(['Fiebre']);
  const [medicationGiven, setMedicationGiven] = useState('');
  const [notes, setNotes] = useState('');

  const toggleSymptom = (sym: string) => {
    setSelectedSymptoms(prev =>
      prev.includes(sym) ? prev.filter(s => s !== sym) : [...prev, sym]
    );
  };

  const isFever = celsius >= 38.0;
  const isSubFever = celsius >= 37.5 && celsius < 38.0;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!celsius) return;

    addTemperatureRecord({
      date,
      time,
      celsius: Number(celsius),
      symptoms: selectedSymptoms,
      medicationGiven: medicationGiven.trim() || undefined,
      notes: notes.trim() || undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className={`p-4 text-white flex items-center justify-between ${
          isFever
            ? 'bg-gradient-to-r from-rose-600 to-rose-700'
            : isSubFever
            ? 'bg-gradient-to-r from-amber-500 to-amber-600'
            : 'bg-gradient-to-r from-teal-600 to-teal-700'
        }`}>
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-white/20">
              <Thermometer className="w-5 h-5 text-white" />
            </span>
            <div>
              <h3 className="font-bold text-base">Registrar Temperatura Corporal</h3>
              <p className="text-xs text-white/90">Curva térmica y síntomas asociados</p>
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
          {/* Temperature Numeric Slider & Input */}
          <div className="rounded-xl p-4 bg-slate-50 border border-slate-200 text-center">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Valor del Termómetro
            </span>
            <div className="flex items-baseline justify-center gap-1 my-1">
              <input
                type="number"
                step="0.1"
                min="35.0"
                max="42.0"
                value={celsius}
                onChange={e => setCelsius(parseFloat(e.target.value) || 0)}
                className="text-4xl font-extrabold w-36 text-center text-slate-900 bg-transparent border-b-2 border-indigo-500 outline-none"
                required
              />
              <span className="text-xl font-bold text-slate-500">°C</span>
            </div>

            <div className="mt-2">
              <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold ${
                isFever
                  ? 'bg-rose-100 text-rose-800'
                  : isSubFever
                  ? 'bg-amber-100 text-amber-800'
                  : 'bg-emerald-100 text-emerald-800'
              }`}>
                {isFever && <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />}
                {isFever
                  ? 'Fiebre (> 38.0°C) - Requiere atención y control'
                  : isSubFever
                  ? 'Febrícula (37.5°C - 37.9°C)'
                  : 'Apirético / Temperatura Normal (< 37.5°C)'}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Fecha
              </label>
              <input
                type="date"
                value={date}
                onChange={e => setDate(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Hora
              </label>
              <input
                type="time"
                value={time}
                onChange={e => setTime(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none"
                required
              />
            </div>
          </div>

          {/* Symptoms Checklist */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Síntomas Observados
            </label>
            <div className="flex flex-wrap gap-1.5">
              {COMMON_SYMPTOMS.map(sym => {
                const active = selectedSymptoms.includes(sym);
                return (
                  <button
                    key={sym}
                    type="button"
                    onClick={() => toggleSymptom(sym)}
                    className={`px-2.5 py-1 rounded-lg text-xs font-medium border transition-all ${
                      active
                        ? 'bg-rose-50 text-rose-700 border-rose-300 shadow-2xs font-semibold'
                        : 'bg-white text-slate-600 border-slate-200 hover:border-slate-300'
                    }`}
                  >
                    {active ? '✓ ' : '+ '} {sym}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Medication given */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Medicamento o Antitérmico Administrado
            </label>
            <input
              type="text"
              value={medicationGiven}
              onChange={e => setMedicationGiven(e.target.value)}
              placeholder="Ej. Ibuprofeno 6.5 ml, Paracetamol 120mg..."
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Notas Adicionales
            </label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value)}
              rows={2}
              placeholder="Ej. Se le pusieron paños tibios, tomó medio vaso de agua..."
              className="w-full rounded-xl border border-slate-300 p-2.5 text-sm outline-none"
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
              className={`px-5 py-2.5 text-xs font-bold rounded-xl text-white shadow-sm transition ${
                isFever ? 'bg-rose-600 hover:bg-rose-700' : 'bg-teal-600 hover:bg-teal-700'
              }`}
            >
              Guardar Registro Térmico
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
