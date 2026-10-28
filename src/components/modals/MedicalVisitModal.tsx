import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { X, Stethoscope, Plus, Calendar } from 'lucide-react';

interface MedicalVisitModalProps {
  onClose: () => void;
}

export const MedicalVisitModal: React.FC<MedicalVisitModalProps> = ({ onClose }) => {
  const { profile, addMedicalVisit } = useApp();

  const todayStr = new Date().toISOString().split('T')[0];
  const [date, setDate] = useState(todayStr);
  const [time, setTime] = useState('16:00');
  const [doctorName, setDoctorName] = useState(profile.pediatricianName || 'Dra. Mariana Gómez Valenzuela');
  const [specialty, setSpecialty] = useState('Pediatría General');
  const [clinic, setClinic] = useState(profile.clinicName || 'Centro Pediátrico San Lucas');
  const [reason, setReason] = useState('');
  const [diagnosis, setDiagnosis] = useState('');
  const [indications, setIndications] = useState('');
  const [nextFollowUpDate, setNextFollowUpDate] = useState('');
  const [status, setStatus] = useState<'completada' | 'programada'>('completada');
  const [notes, setNotes] = useState('');

  const [prescriptionInput, setPrescriptionInput] = useState('');
  const [prescriptions, setPrescriptions] = useState<string[]>([]);

  const [studyInput, setStudyInput] = useState('');
  const [studiesRequested, setStudiesRequested] = useState<string[]>([]);

  const addPrescription = () => {
    if (prescriptionInput.trim()) {
      setPrescriptions([...prescriptions, prescriptionInput.trim()]);
      setPrescriptionInput('');
    }
  };

  const addStudy = () => {
    if (studyInput.trim()) {
      setStudiesRequested([...studiesRequested, studyInput.trim()]);
      setStudyInput('');
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!doctorName || !reason) return;

    addMedicalVisit({
      date,
      time,
      doctorName: doctorName.trim(),
      specialty: specialty.trim(),
      clinic: clinic.trim(),
      reason: reason.trim(),
      diagnosis: diagnosis.trim() || 'Control de rutina',
      indications: indications.trim(),
      prescriptions,
      studiesRequested,
      nextFollowUpDate: nextFollowUpDate || undefined,
      status,
      notes: notes.trim() || undefined,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="bg-gradient-to-r from-blue-600 to-indigo-700 p-4 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-white/20">
              <Stethoscope className="w-5 h-5 text-white" />
            </span>
            <div>
              <h3 className="font-bold text-base">Registrar Consulta o Visita Médica</h3>
              <p className="text-xs text-blue-100">Pediatría, especialistas, diagnósticos y estudios</p>
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
                Estado de la Consulta
              </label>
              <select
                value={status}
                onChange={e => setStatus(e.target.value as any)}
                className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs bg-white outline-none"
              >
                <option value="completada">Completada / Ya realizada</option>
                <option value="programada">Programada / Turno futuro</option>
              </select>
            </div>
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
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Médico / Especialista *
              </label>
              <input
                type="text"
                value={doctorName}
                onChange={e => setDoctorName(e.target.value)}
                placeholder="Ej. Dra. Gómez Valenzuela"
                className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs outline-none"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                Especialidad
              </label>
              <input
                type="text"
                value={specialty}
                onChange={e => setSpecialty(e.target.value)}
                placeholder="Ej. Pediatría, Oftalmología, Otorrino..."
                className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Clínica / Sanatorio / Consultorio
            </label>
            <input
              type="text"
              value={clinic}
              onChange={e => setClinic(e.target.value)}
              placeholder="Ej. Centro Pediátrico San Lucas..."
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Motivo de Consulta *
            </label>
            <input
              type="text"
              value={reason}
              onChange={e => setReason(e.target.value)}
              placeholder="Ej. Control sano semestral, fiebre persistente de 24hs..."
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs outline-none"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Diagnóstico Médico
            </label>
            <input
              type="text"
              value={diagnosis}
              onChange={e => setDiagnosis(e.target.value)}
              placeholder="Ej. Rinofaringitis aguda leve, desarrollo neurocognitivo normal..."
              className="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-xs outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Indicaciones y Recomendaciones
            </label>
            <textarea
              value={indications}
              onChange={e => setIndications(e.target.value)}
              rows={2}
              placeholder="Ej. Mantener buena hidratación, control de temperatura, baños tibios..."
              className="w-full rounded-xl border border-slate-300 p-2.5 text-xs outline-none"
            />
          </div>

          {/* Prescriptions input */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Recetas / Medicamentos Indicados
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={prescriptionInput}
                onChange={e => setPrescriptionInput(e.target.value)}
                placeholder="Ej. Ibuprofeno 6.5ml cada 8hs x 3 días"
                className="flex-1 rounded-xl border border-slate-300 px-3 py-1.5 text-xs outline-none"
              />
              <button
                type="button"
                onClick={addPrescription}
                className="px-3 py-1.5 bg-blue-600 text-white rounded-xl text-xs font-bold hover:bg-blue-700"
              >
                +
              </button>
            </div>
            {prescriptions.length > 0 && (
              <ul className="mt-1.5 space-y-1">
                {prescriptions.map((p, i) => (
                  <li key={i} className="text-xs text-slate-700 bg-blue-50 px-2 py-1 rounded-md flex justify-between">
                    <span>💊 {p}</span>
                    <button type="button" onClick={() => setPrescriptions(prescriptions.filter((_, idx) => idx !== i))} className="text-slate-400 hover:text-rose-600">×</button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Studies input */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Estudios Médicos Solicitados
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={studyInput}
                onChange={e => setStudyInput(e.target.value)}
                placeholder="Ej. Control de vista, Hemograma completo, Urocultivo"
                className="flex-1 rounded-xl border border-slate-300 px-3 py-1.5 text-xs outline-none"
              />
              <button
                type="button"
                onClick={addStudy}
                className="px-3 py-1.5 bg-blue-600 text-white rounded-xl text-xs font-bold hover:bg-blue-700"
              >
                +
              </button>
            </div>
            {studiesRequested.length > 0 && (
              <ul className="mt-1.5 space-y-1">
                {studiesRequested.map((s, i) => (
                  <li key={i} className="text-xs text-slate-700 bg-indigo-50 px-2 py-1 rounded-md flex justify-between">
                    <span>🔬 {s}</span>
                    <button type="button" onClick={() => setStudiesRequested(studiesRequested.filter((_, idx) => idx !== i))} className="text-slate-400 hover:text-rose-600">×</button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Próximo Control / Seguimiento
            </label>
            <input
              type="date"
              value={nextFollowUpDate}
              onChange={e => setNextFollowUpDate(e.target.value)}
              className="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs outline-none"
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
              className="px-5 py-2.5 text-xs font-bold rounded-xl bg-blue-600 text-white hover:bg-blue-700 shadow-sm transition"
            >
              Guardar Consulta Médica
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
