import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { X, Utensils, Droplets, Plus } from 'lucide-react';

interface MealModalProps {
  onClose: () => void;
}

const COMMON_FOOD_SUGGESTIONS = [
  'Frutas frescas (manzana, banana, pera)',
  'Avena / Cereales integrales',
  'Leche / Yogur natural',
  'Huevo duro / revuelto',
  'Pollo / Carne magra',
  'Pescado',
  'Lentejas / Garbanzos',
  'Zanahoria / Calabaza',
  'Brócoli / Espinaca',
  'Fideos / Arroz',
  'Pan integral con queso',
];

export const MealModal: React.FC<MealModalProps> = ({ onClose }) => {
  const { addMealRecord } = useApp();

  const todayStr = new Date().toISOString().split('T')[0];
  const now = new Date();
  const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

  const [date, setDate] = useState(todayStr);
  const [time, setTime] = useState(timeStr);
  const [mealType, setMealType] = useState<'desayuno' | 'media mañana' | 'almuerzo' | 'merienda' | 'cena' | 'colación'>('almuerzo');
  const [foodInput, setFoodInput] = useState('');
  const [foodsList, setFoodsList] = useState<string[]>(['Fideos con salsa de tomate', 'Zanahoria rallada']);
  const [waterGlasses, setWaterGlasses] = useState(1);
  const [appetite, setAppetite] = useState<'excelente' | 'bueno' | 'regular' | 'poco' | 'rechazó'>('bueno');
  const [notes, setNotes] = useState('');

  const addFoodItem = (item: string) => {
    if (!item.trim() || foodsList.includes(item.trim())) return;
    setFoodsList([...foodsList, item.trim()]);
    setFoodInput('');
  };

  const removeFoodItem = (idx: number) => {
    setFoodsList(foodsList.filter((_, i) => i !== idx));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    addMealRecord({
      date,
      time,
      mealType,
      foods: foodsList.length > 0 ? foodsList : ['Comida variada'],
      waterGlasses: Number(waterGlasses),
      appetite,
      notes: notes.trim() || undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="bg-gradient-to-r from-emerald-600 to-teal-700 p-4 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-white/20">
              <Utensils className="w-5 h-5 text-white" />
            </span>
            <div>
              <h3 className="font-bold text-base">Registrar Comida y Nutrición</h3>
              <p className="text-xs text-emerald-100">Alimentos ingeridos, agua y apetito</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg text-white/80 hover:text-white transition">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4 max-h-[80vh] overflow-y-auto">
          <div className="grid grid-cols-3 gap-2">
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
                Hora
              </label>
              <input
                type="time"
                value={time}
                onChange={e => setTime(e.target.value)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs outline-none"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Comida
              </label>
              <select
                value={mealType}
                onChange={e => setMealType(e.target.value as any)}
                className="w-full rounded-xl border border-slate-300 px-2 py-2 text-xs bg-white outline-none capitalize"
              >
                <option value="desayuno">Desayuno</option>
                <option value="media mañana">Media Mañana</option>
                <option value="almuerzo">Almuerzo</option>
                <option value="merienda">Merienda</option>
                <option value="cena">Cena</option>
                <option value="colación">Colación / Snack</option>
              </select>
            </div>
          </div>

          {/* Foods list */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Alimentos Ingeridos
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={foodInput}
                onChange={e => setFoodInput(e.target.value)}
                onKeyDown={e => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    addFoodItem(foodInput);
                  }
                }}
                placeholder="Escribe un alimento y presiona enter..."
                className="flex-1 rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none"
              />
              <button
                type="button"
                onClick={() => addFoodItem(foodInput)}
                className="px-3 py-2 bg-emerald-600 text-white rounded-xl text-xs font-bold hover:bg-emerald-700 transition"
              >
                <Plus className="w-4 h-4" />
              </button>
            </div>

            {/* Chips of added foods */}
            <div className="flex flex-wrap gap-1.5 mt-2">
              {foodsList.map((f, i) => (
                <span
                  key={i}
                  className="inline-flex items-center gap-1.5 bg-emerald-50 text-emerald-800 border border-emerald-200 px-2.5 py-1 rounded-lg text-xs font-semibold"
                >
                  {f}
                  <button
                    type="button"
                    onClick={() => removeFoodItem(i)}
                    className="text-emerald-500 hover:text-rose-600"
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>

            {/* Quick suggested chips */}
            <div className="mt-2 text-[11px] text-slate-500">
              Sugerencias rápidas:
              <div className="flex flex-wrap gap-1 mt-1">
                {COMMON_FOOD_SUGGESTIONS.slice(0, 5).map(s => (
                  <button
                    key={s}
                    type="button"
                    onClick={() => addFoodItem(s)}
                    className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 text-[10px] hover:bg-slate-200 transition"
                  >
                    + {s}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {/* Water Glasses */}
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Droplets className="w-3.5 h-3.5 text-blue-500" />
                Vasos de Agua (200ml)
              </label>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setWaterGlasses(Math.max(0, waterGlasses - 1))}
                  className="w-9 h-9 rounded-xl border border-slate-200 text-slate-600 font-bold hover:bg-slate-100"
                >
                  -
                </button>
                <span className="text-base font-extrabold text-slate-900 w-8 text-center">
                  {waterGlasses}
                </span>
                <button
                  type="button"
                  onClick={() => setWaterGlasses(waterGlasses + 1)}
                  className="w-9 h-9 rounded-xl border border-slate-200 text-slate-600 font-bold hover:bg-slate-100"
                >
                  +
                </button>
              </div>
            </div>

            {/* Appetite */}
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Nivel de Apetito
              </label>
              <select
                value={appetite}
                onChange={e => setAppetite(e.target.value as any)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs bg-white outline-none"
              >
                <option value="excelente">Excelente (comió todo)</option>
                <option value="bueno">Bueno (mayor parte)</option>
                <option value="regular">Regular (la mitad)</option>
                <option value="poco">Poco apetito</option>
                <option value="rechazó">Rechazó el plato</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Notas o Reacciones
            </label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value)}
              rows={2}
              placeholder="Ej. Probó brócoli por primera vez y le gustó, pidió repetir postre..."
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
              className="px-5 py-2.5 text-xs font-bold rounded-xl bg-emerald-600 text-white hover:bg-emerald-700 shadow-sm transition"
            >
              Guardar Registro de Comida
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
