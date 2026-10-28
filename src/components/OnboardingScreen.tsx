import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { calculateAgeMonths, formatAgeString } from '../utils/whoStandards';
import {
  Heart,
  Sparkles,
  Calendar,
  User,
  ShieldAlert,
  Stethoscope,
  ArrowRight,
  Baby,
  Smile
} from 'lucide-react';
import confetti from 'canvas-confetti';

interface OnboardingScreenProps {
  onComplete: () => void;
}

export const OnboardingScreen: React.FC<OnboardingScreenProps> = ({ onComplete }) => {
  const { updateProfile, resetToSampleData } = useApp();

  const [name, setName] = useState('');
  const [lastName, setLastName] = useState('');
  const [birthDate, setBirthDate] = useState('2023-04-15');
  const [gender, setGender] = useState<'female' | 'male'>('female');
  const [bloodType, setBloodType] = useState('A+');
  const [allergies, setAllergies] = useState('');
  const [pediatricianName, setPediatricianName] = useState('');
  const [clinicName, setClinicName] = useState('');

  // Calculate live age preview
  const todayStr = new Date().toISOString().split('T')[0];
  const ageMonths = calculateAgeMonths(birthDate, todayStr);
  const ageText = formatAgeString(ageMonths);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    updateProfile({
      name: `${name.trim()} ${lastName.trim()}`.trim(),
      lastName: lastName.trim(),
      birthDate,
      gender,
      bloodType,
      allergies: allergies
        ? allergies.split(',').map(s => s.trim()).filter(Boolean)
        : [],
      pediatricianName: pediatricianName.trim() || 'Dr./Dra. de Cabecera',
      clinicName: clinicName.trim() || 'Centro de Salud',
    });

    try {
      confetti({
        particleCount: 50,
        spread: 60,
        origin: { y: 0.7 },
        colors: ['#F43F5E', '#FB7185', '#F59E0B', '#10B981', '#6366F1'],
      });
    } catch {
      // ignore
    }

    onComplete();
  };

  const handleUseSample = () => {
    resetToSampleData();
    onComplete();
  };

  return (
    <div className="min-h-full flex flex-col justify-center py-4 px-2 sm:px-4">
      <div className="bg-white/95 backdrop-blur-md rounded-3xl shadow-xl border border-rose-100 overflow-hidden max-w-md mx-auto w-full animate-in fade-in duration-300">
        {/* Maternal Header */}
        <div className="relative bg-gradient-to-br from-rose-400 via-rose-500 to-amber-400 p-6 text-white text-center overflow-hidden">
          {/* Subtle background circles */}
          <div className="absolute -top-10 -right-10 w-32 h-32 rounded-full bg-white/10 blur-sm pointer-events-none"></div>
          <div className="absolute -bottom-10 -left-10 w-28 h-28 rounded-full bg-white/10 blur-sm pointer-events-none"></div>

          <div className="relative z-10 flex flex-col items-center">
            <div className="w-16 h-16 rounded-full bg-white/20 border-2 border-white/60 flex items-center justify-center text-3xl shadow-inner mb-3">
              👶
            </div>
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/20 text-xs font-semibold tracking-wide backdrop-blur-xs mb-1.5">
              <Heart className="w-3.5 h-3.5 fill-rose-200 text-rose-200" />
              <span>Cuidado y Amor Pediátrico</span>
            </div>
            <h1 className="text-2xl font-black tracking-tight text-white">
              Bienvenida a PequeCare
            </h1>
            <p className="text-xs text-rose-100 mt-1 max-w-xs leading-relaxed">
              Registra los datos de tu pequeña para comenzar su seguimiento de crecimiento con curvas de la OMS, salud y rutinas diarias.
            </p>
          </div>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-5 sm:p-6 space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1">
                <User className="w-3.5 h-3.5 text-rose-500" />
                Nombre *
              </label>
              <input
                type="text"
                required
                value={name}
                onChange={e => setName(e.target.value)}
                placeholder="Ej. Sofía"
                className="w-full rounded-2xl border border-rose-200 px-3.5 py-2.5 text-sm bg-rose-50/20 focus:bg-white focus:border-rose-500 focus:ring-2 focus:ring-rose-200 outline-none transition"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Apellido *
              </label>
              <input
                type="text"
                required
                value={lastName}
                onChange={e => setLastName(e.target.value)}
                placeholder="Ej. Morales"
                className="w-full rounded-2xl border border-rose-200 px-3.5 py-2.5 text-sm bg-rose-50/20 focus:bg-white focus:border-rose-500 focus:ring-2 focus:ring-rose-200 outline-none transition"
              />
            </div>
          </div>

          {/* Birth Date & Age live preview */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1 flex items-center justify-between">
              <span className="flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-rose-500" />
                Fecha de Nacimiento *
              </span>
              <span className="text-[11px] text-rose-700 font-extrabold normal-case bg-rose-50 px-2 py-0.5 rounded-full border border-rose-200">
                {ageText} ({ageMonths}m)
              </span>
            </label>
            <input
              type="date"
              required
              value={birthDate}
              onChange={e => setBirthDate(e.target.value)}
              className="w-full rounded-2xl border border-rose-200 px-3.5 py-2.5 text-sm bg-rose-50/20 focus:bg-white focus:border-rose-500 focus:ring-2 focus:ring-rose-200 outline-none transition"
            />
            {ageMonths < 36 && (
              <p className="text-[11px] text-amber-700 mt-1 flex items-center gap-1 font-medium">
                <span>💡</span> La app está optimizada para niñas mayores de 3 años, pero calculará el crecimiento de cualquier edad.
              </p>
            )}
          </div>

          {/* Gender and Blood Type */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Género
              </label>
              <div className="grid grid-cols-2 gap-1.5 p-1 bg-slate-100 rounded-2xl">
                <button
                  type="button"
                  onClick={() => setGender('female')}
                  className={`py-1.5 text-xs font-bold rounded-xl transition ${
                    gender === 'female'
                      ? 'bg-white text-rose-600 shadow-2xs'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  👧 Niña
                </button>
                <button
                  type="button"
                  onClick={() => setGender('male')}
                  className={`py-1.5 text-xs font-bold rounded-xl transition ${
                    gender === 'male'
                      ? 'bg-white text-blue-600 shadow-2xs'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  👦 Niño
                </button>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Grupo Sanguíneo
              </label>
              <select
                value={bloodType}
                onChange={e => setBloodType(e.target.value)}
                className="w-full rounded-2xl border border-rose-200 px-3 py-2 text-sm bg-white focus:border-rose-500 focus:ring-2 focus:ring-rose-200 outline-none transition"
              >
                <option value="A+">A Positivo (A+)</option>
                <option value="A-">A Negativo (A-)</option>
                <option value="B+">B Positivo (B+)</option>
                <option value="B-">B Negativo (B-)</option>
                <option value="AB+">AB Positivo (AB+)</option>
                <option value="AB-">AB Negativo (AB-)</option>
                <option value="O+">O Positivo (O+)</option>
                <option value="O-">O Negativo (O-)</option>
              </select>
            </div>
          </div>

          {/* Allergies */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1">
              <ShieldAlert className="w-3.5 h-3.5 text-amber-500" />
              Alergias o Condiciones (Opcional)
            </label>
            <input
              type="text"
              value={allergies}
              onChange={e => setAllergies(e.target.value)}
              placeholder="Ej. Penicilina, polen, ninguna..."
              className="w-full rounded-2xl border border-slate-200 px-3.5 py-2 text-xs focus:border-rose-400 focus:ring-2 focus:ring-rose-100 outline-none"
            />
          </div>

          {/* Pediatrician and Clinic (Optional) */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Stethoscope className="w-3.5 h-3.5 text-indigo-500" />
                Pediatra
              </label>
              <input
                type="text"
                value={pediatricianName}
                onChange={e => setPediatricianName(e.target.value)}
                placeholder="Ej. Dra. Mariana Gómez"
                className="w-full rounded-2xl border border-slate-200 px-3 py-2 text-xs outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Clínica / Hospital
              </label>
              <input
                type="text"
                value={clinicName}
                onChange={e => setClinicName(e.target.value)}
                placeholder="Ej. San Lucas"
                className="w-full rounded-2xl border border-slate-200 px-3 py-2 text-xs outline-none"
              />
            </div>
          </div>

          {/* Submit Action Buttons */}
          <div className="pt-2 space-y-2">
            <button
              type="submit"
              className="w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-rose-500 via-rose-600 to-amber-500 hover:from-rose-600 hover:to-amber-600 text-white font-extrabold text-sm shadow-md hover:shadow-lg transition-all active:scale-[0.98] flex items-center justify-center gap-2"
            >
              <span>Comenzar Seguimiento de Mi Hija</span>
              <ArrowRight className="w-4 h-4" />
            </button>

            <button
              type="button"
              onClick={handleUseSample}
              className="w-full py-2.5 px-4 rounded-2xl border border-slate-200 hover:bg-slate-50 text-slate-600 text-xs font-semibold transition flex items-center justify-center gap-1.5"
            >
              <Sparkles className="w-3.5 h-3.5 text-amber-500" />
              <span>O continuar con datos de muestra (Sofía, 3 años y medio)</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
