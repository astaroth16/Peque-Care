import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { X, Moon, Clock } from 'lucide-react';

interface SleepModalProps {
  onClose: () => void;
}

export const SleepModal: React.FC<SleepModalProps> = ({ onClose }) => {
  const { addSleepRecord } = useApp();

  const todayStr = new Date().toISOString().split('T')[0];
  const [date, setDate] = useState(todayStr);
  const [startTime, setStartTime] = useState('21:30');
  const [endTime, setEndTime] = useState('07:30');
  const [type, setType] = useState<'noche' | 'siesta'>('noche');
  const [quality, setQuality] = useState<'tranquilo' | 'inquieto' | 'con pesadillas' | 'despertares frecuentes'>('tranquilo');
  const [notes, setNotes] = useState('');

  // Calculate duration in minutes
  const calculateDuration = () => {
    const [startH, startM] = startTime.split(':').map(Number);
    const [endH, endM] = endTime.split(':').map(Number);
    let startTotal = startH * 60 + startM;
    let endTotal = endH * 60 + endM;
    if (endTotal < startTotal) {
      endTotal += 24 * 60; // Crosses midnight
    }
    return Math.max(0, endTotal - startTotal);
  };

  const durationMin = calculateDuration();
  const hours = Math.floor(durationMin / 60);
  const minutes = durationMin % 60;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    addSleepRecord({
      date,
      startTime,
      endTime,
      durationMinutes: durationMin,
      type,
      quality,
      notes: notes.trim() || undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="bg-gradient-to-r from-violet-600 to-indigo-700 p-4 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-white/20">
              <Moon className="w-5 h-5 text-white" />
            </span>
            <div>
              <h3 className="font-bold text-base">Registrar Horas de Sueño</h3>
              <p className="text-xs text-violet-100">Meta recomendada OMS: 10 a 13 horas/día</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg text-white/80 hover:text-white transition">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4 max-h-[80vh] overflow-y-auto">
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
                Tipo de Sueño
              </label>
              <select
                value={type}
                onChange={e => setType(e.target.value as 'noche' | 'siesta')}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm bg-white outline-none"
              >
                <option value="noche">Sueño Nocturno</option>
                <option value="siesta">Siesta de la tarde</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Hora en que se Durmió
              </label>
              <input
                type="time"
                value={startTime}
                onChange={e => setStartTime(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Hora en que Despertó
              </label>
              <input
                type="time"
                value={endTime}
                onChange={e => setEndTime(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none"
                required
              />
            </div>
          </div>

          {/* Duration display badge */}
          <div className="rounded-xl p-3 bg-violet-50 border border-violet-200 text-center">
            <span className="text-xs text-violet-700 font-semibold">Duración Total Calculada:</span>
            <div className="text-2xl font-black text-violet-900 mt-0.5">
              {hours} h {minutes > 0 ? `${minutes} min` : ''}
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Calidad del Sueño
            </label>
            <select
              value={quality}
              onChange={e => setQuality(e.target.value as any)}
              className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm bg-white outline-none"
            >
              <option value="tranquilo">Tranquilo y reparador (sin despertares)</option>
              <option value="despertares frecuentes">Despertares frecuentes / inquieto</option>
              <option value="con pesadillas">Con pesadillas o terrores nocturnos</option>
              <option value="inquieto">Inquieto / le costó conciliar</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Observaciones (Cuento, rutina, etc.)
            </label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value)}
              rows={2}
              placeholder="Ej. Tomó un vaso de leche tibia antes, se durmió escuchando música clásica..."
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
              className="px-5 py-2.5 text-xs font-bold rounded-xl bg-violet-600 text-white hover:bg-violet-700 shadow-sm transition"
            >
              Guardar Sueño
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
