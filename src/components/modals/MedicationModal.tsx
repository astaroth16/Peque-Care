import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { X, Pill, Clock, AlertCircle } from 'lucide-react';

interface MedicationModalProps {
  onClose: () => void;
}

export const MedicationModal: React.FC<MedicationModalProps> = ({ onClose }) => {
  const { addMedication } = useApp();

  const todayStr = new Date().toISOString().split('T')[0];
  const now = new Date();
  const nextHourIso = new Date(now.getTime() + 60 * 60 * 1000).toISOString().slice(0, 16);

  const [name, setName] = useState('');
  const [purpose, setPurpose] = useState('');
  const [dosage, setDosage] = useState('');
  const [unit, setUnit] = useState('ml');
  const [frequencyHours, setFrequencyHours] = useState(8);
  const [startDate, setStartDate] = useState(todayStr);
  const [endDate, setEndDate] = useState(
    new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  );
  const [nextDoseTime, setNextDoseTime] = useState(nextHourIso);
  const [notes, setNotes] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !dosage) return;

    addMedication({
      name: name.trim(),
      purpose: purpose.trim() || 'Tratamiento pediátrico indicado',
      dosage: `${dosage} ${unit}`.trim(),
      unit,
      frequencyHours: Number(frequencyHours),
      startDate,
      endDate,
      nextDoseTime,
      active: true,
      notes: notes.trim() || undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="bg-gradient-to-r from-amber-500 to-amber-600 p-4 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-white/20">
              <Pill className="w-5 h-5 text-white" />
            </span>
            <div>
              <h3 className="font-bold text-base">Nuevo Medicamento / Tratamiento</h3>
              <p className="text-xs text-amber-100">Dosis exacta y recordatorio de suministro</p>
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
              Nombre del Medicamento *
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="Ej. Amoxicilina con Ácido Clavulánico 250mg, Ibuprofeno..."
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Motivo o Síntoma a Tratar
            </label>
            <input
              type="text"
              value={purpose}
              onChange={e => setPurpose(e.target.value)}
              placeholder="Ej. Infección respiratoria, fiebre > 38°C, alergia estacional..."
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Dosis *
              </label>
              <input
                type="text"
                value={dosage}
                onChange={e => setDosage(e.target.value)}
                placeholder="Ej. 5, 250, 4"
                className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Unidad
              </label>
              <select
                value={unit}
                onChange={e => setUnit(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm bg-white focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
              >
                <option value="ml">ml (mililitros en jarabe/suspensión)</option>
                <option value="mg">mg (miligramos)</option>
                <option value="gotas">gotas</option>
                <option value="comprimido">comprimido / masticable</option>
                <option value="puff">disparo / puff inhalador</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Frecuencia
              </label>
              <select
                value={frequencyHours}
                onChange={e => setFrequencyHours(Number(e.target.value))}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm bg-white focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
              >
                <option value={4}>Cada 4 horas</option>
                <option value={6}>Cada 6 horas</option>
                <option value={8}>Cada 8 horas (3 veces/día)</option>
                <option value={12}>Cada 12 horas (2 veces/día)</option>
                <option value={24}>Cada 24 horas (1 vez/día)</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-amber-600" />
                Próxima Dosis
              </label>
              <input
                type="datetime-local"
                value={nextDoseTime}
                onChange={e => setNextDoseTime(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Fecha de Inicio
              </label>
              <input
                type="date"
                value={startDate}
                onChange={e => setStartDate(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs outline-none"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Fecha de Fin
              </label>
              <input
                type="date"
                value={endDate}
                onChange={e => setEndDate(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Instrucciones / Indicaciones Especiales
            </label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value)}
              rows={2}
              placeholder="Ej. Agitar bien antes de usar, administrar con alimentos para proteger el estómago..."
              className="w-full rounded-xl border border-slate-300 p-2.5 text-sm focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
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
              className="px-5 py-2.5 text-xs font-bold rounded-xl bg-amber-600 text-white hover:bg-amber-700 shadow-sm transition"
            >
              Guardar y Activar Recordatorio
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
