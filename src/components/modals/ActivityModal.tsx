import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { X, Bike, Clock, Bell, Sparkles } from 'lucide-react';

interface ActivityModalProps {
  onClose: () => void;
}

export const ActivityModal: React.FC<ActivityModalProps> = ({ onClose }) => {
  const { addActivityRecord } = useApp();

  const todayStr = new Date().toISOString().split('T')[0];
  const now = new Date();
  const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

  const [date, setDate] = useState(todayStr);
  const [time, setTime] = useState(timeStr);
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState<'al aire libre' | 'educativa' | 'motricidad' | 'lectura' | 'jardín' | 'pantalla' | 'recreativa'>('al aire libre');
  const [durationMinutes, setDurationMinutes] = useState(45);
  const [reminderEnabled, setReminderEnabled] = useState(true);
  const [reminderTime, setReminderTime] = useState('16:00');
  const [notes, setNotes] = useState('');

  const quickActivityIdeas = [
    { title: 'Juego en la plaza y hamacas', cat: 'al aire libre' as const, min: 60 },
    { title: 'Pintura y motricidad con masa', cat: 'motricidad' as const, min: 30 },
    { title: 'Lectura de cuentos interactivos', cat: 'lectura' as const, min: 20 },
    { title: 'Armado de rompecabezas / lego', cat: 'educativa' as const, min: 35 },
    { title: 'Paseo en triciclo o monopatín', cat: 'al aire libre' as const, min: 40 },
    { title: 'Tiempo de pantalla (dibujos)', cat: 'pantalla' as const, min: 30 },
  ];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    addActivityRecord({
      date,
      time,
      title: title.trim(),
      category,
      durationMinutes: Number(durationMinutes),
      reminderEnabled,
      reminderTime: reminderEnabled ? reminderTime : undefined,
      notes: notes.trim() || undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="bg-gradient-to-r from-cyan-600 to-blue-700 p-4 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-white/20">
              <Bike className="w-5 h-5 text-white" />
            </span>
            <div>
              <h3 className="font-bold text-base">Registrar Actividad Diaria</h3>
              <p className="text-xs text-cyan-100">Juego, estimulación y recordatorios programados</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg text-white/80 hover:text-white transition">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4 max-h-[80vh] overflow-y-auto">
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Nombre de la Actividad *
            </label>
            <input
              type="text"
              value={title}
              onChange={e => setTitle(e.target.value)}
              placeholder="Ej. Bicicleta con rueditas en la plaza, recorte y collage..."
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 outline-none"
              required
            />

            {/* Quick Chips */}
            <div className="mt-2 text-[11px] text-slate-500">
              Ideas frecuentes:
              <div className="flex flex-wrap gap-1 mt-1">
                {quickActivityIdeas.map((idea, idx) => (
                  <button
                    key={idx}
                    type="button"
                    onClick={() => {
                      setTitle(idea.title);
                      setCategory(idea.cat);
                      setDurationMinutes(idea.min);
                    }}
                    className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 text-[10px] hover:bg-slate-200 transition"
                  >
                    + {idea.title}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Categoría
              </label>
              <select
                value={category}
                onChange={e => setCategory(e.target.value as any)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs bg-white outline-none capitalize"
              >
                <option value="al aire libre">Al aire libre / Parque</option>
                <option value="motricidad">Motricidad Fina/Gruesa</option>
                <option value="educativa">Educativa / Lógica</option>
                <option value="lectura">Lectura / Cuentos</option>
                <option value="recreativa">Juego Simbólico / Libre</option>
                <option value="jardín">Jardín de Infantes</option>
                <option value="pantalla">Pantalla / TV (controlada)</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Duración (minutos)
              </label>
              <input
                type="number"
                min="5"
                max="300"
                step="5"
                value={durationMinutes}
                onChange={e => setDurationMinutes(Number(e.target.value))}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs outline-none"
                required
              />
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
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs outline-none"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Hora Inicio
              </label>
              <input
                type="time"
                value={time}
                onChange={e => setTime(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs outline-none"
                required
              />
            </div>
          </div>

          {/* Reminder / Alarm option */}
          <div className="rounded-xl p-3.5 bg-cyan-50 border border-cyan-200">
            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={reminderEnabled}
                  onChange={e => setReminderEnabled(e.target.checked)}
                  className="w-4 h-4 text-cyan-600 rounded-md focus:ring-cyan-500"
                />
                <span className="text-xs font-bold text-cyan-900 flex items-center gap-1.5">
                  <Bell className="w-4 h-4 text-cyan-600" />
                  Activar Recordatorio / Alarma
                </span>
              </label>
            </div>
            {reminderEnabled && (
              <div className="mt-2.5 flex items-center gap-2">
                <span className="text-xs text-slate-600">Avisarme a las:</span>
                <input
                  type="time"
                  value={reminderTime}
                  onChange={e => setReminderTime(e.target.value)}
                  className="rounded-lg border border-slate-300 px-2.5 py-1 text-xs bg-white outline-none"
                />
              </div>
            )}
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Observaciones / Logros
            </label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value)}
              rows={2}
              placeholder="Ej. Compartió sus juguetes con otros niños, corrió sin cansarse..."
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
              className="px-5 py-2.5 text-xs font-bold rounded-xl bg-cyan-600 text-white hover:bg-cyan-700 shadow-sm transition"
            >
              Guardar Actividad
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
