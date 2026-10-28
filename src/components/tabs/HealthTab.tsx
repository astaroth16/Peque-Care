import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { useLongPress } from '../../hooks/useLongPress';
import { ConfirmDeleteModal } from '../modals/ConfirmDeleteModal';
import { Medication, TemperatureRecord, MedicalVisit, MedicalStudy } from '../../types';
import {
  Pill,
  Thermometer,
  Stethoscope,
  FileText,
  Plus,
  Clock,
  CheckCircle,
  AlertTriangle,
  Calendar,
  Trash2,
  Syringe,
  ChevronDown,
  ChevronUp,
  CheckSquare,
  Square,
  X
} from 'lucide-react';

/* ---------------- Medication Card Item with Long Press ---------------- */
interface MedCardProps {
  med: Medication;
  isSelected: boolean;
  isSelectionMode: boolean;
  isExpanded: boolean;
  onToggleSelect: (id: string) => void;
  onToggleExpand: () => void;
  onRecordDose: () => void;
  onRequestDelete: () => void;
}

const MedCard: React.FC<MedCardProps> = ({
  med,
  isSelected,
  isSelectionMode,
  isExpanded,
  onToggleSelect,
  onToggleExpand,
  onRecordDose,
  onRequestDelete,
}) => {
  const longPressEvents = useLongPress(
    () => onToggleSelect(med.id),
    () => {
      if (isSelectionMode) onToggleSelect(med.id);
    },
    { delay: 450 }
  );

  return (
    <div
      {...longPressEvents}
      className={`rounded-2xl border transition select-none overflow-hidden ${
        isSelected
          ? 'bg-rose-50/70 border-rose-400 shadow-sm'
          : med.active
          ? 'bg-white border-amber-200/80 shadow-2xs'
          : 'bg-slate-50 border-slate-200 opacity-75'
      }`}
    >
      <div className="p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="flex items-start gap-3">
          {isSelectionMode ? (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onToggleSelect(med.id);
              }}
              className="mt-1 text-rose-600 focus:outline-hidden"
            >
              {isSelected ? (
                <CheckSquare className="w-5 h-5 fill-rose-600 text-white" />
              ) : (
                <Square className="w-5 h-5 text-slate-300" />
              )}
            </button>
          ) : (
            <div
              className={`p-2.5 rounded-xl shrink-0 ${
                med.active ? 'bg-amber-100 text-amber-700' : 'bg-slate-200 text-slate-500'
              }`}
            >
              <Pill className="w-5 h-5" />
            </div>
          )}

          <div>
            <div className="flex items-center gap-2">
              <h4 className="text-base font-extrabold text-slate-900">{med.name}</h4>
              <span
                className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                  med.active ? 'bg-amber-100 text-amber-800' : 'bg-slate-200 text-slate-700'
                }`}
              >
                {med.active ? 'Activo' : 'Finalizado'}
              </span>
            </div>
            <p className="text-xs text-slate-600 font-medium mt-0.5">
              Dosis: <strong className="text-slate-800">{med.dosage}</strong> cada{' '}
              <strong className="text-slate-800">{med.frequencyHours} hs</strong> ({med.purpose})
            </p>
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-[11px] text-slate-400 mt-1">
              <span>Del {med.startDate} al {med.endDate}</span>
              <span>•</span>
              <span className="font-semibold text-slate-700">
                Próxima toma: {med.nextDoseTime.replace('T', ' ')}
              </span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
          {med.active && (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onRecordDose();
              }}
              className="px-3.5 py-2 rounded-xl bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold shadow-xs active:scale-95 transition"
            >
              ✓ Registrar Dosis
            </button>
          )}
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onToggleExpand();
            }}
            className="p-2 rounded-xl text-slate-500 hover:bg-slate-100"
            title="Ver historial de tomas"
          >
            {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onRequestDelete();
            }}
            className="p-2 rounded-xl text-slate-400 hover:text-rose-600 hover:bg-rose-50"
            title="Eliminar tratamiento"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {isExpanded && (
        <div className="bg-slate-50 p-4 border-t border-slate-100">
          <h5 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
            Historial de Dosis Administradas ({med.dosesLog.length})
          </h5>
          {med.dosesLog.length === 0 ? (
            <p className="text-xs text-slate-400 italic">No hay tomas registradas aún.</p>
          ) : (
            <div className="space-y-1.5">
              {med.dosesLog.map((log) => (
                <div
                  key={log.id}
                  className="bg-white p-2.5 rounded-xl border border-slate-200 text-xs flex items-center justify-between"
                >
                  <div className="flex items-center gap-2">
                    <CheckCircle className="w-3.5 h-3.5 text-emerald-600" />
                    <span className="font-bold text-slate-800">{log.dosageGiven}</span>
                    <span className="text-slate-500">
                      {new Date(log.timestamp).toLocaleString([], {
                        dateStyle: 'short',
                        timeStyle: 'short',
                      })}
                    </span>
                  </div>
                  {log.notes && (
                    <span className="text-[11px] text-slate-400 italic max-w-xs truncate">
                      {log.notes}
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
          {med.notes && (
            <div className="mt-3 text-xs text-amber-900 bg-amber-50 p-2 rounded-lg border border-amber-200">
              <strong>Nota pediátrica:</strong> {med.notes}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

/* ---------------- Temperature Item with Long Press ---------------- */
interface TempItemProps {
  rec: TemperatureRecord;
  isSelected: boolean;
  isSelectionMode: boolean;
  onToggleSelect: (id: string) => void;
  onRequestDelete: () => void;
}

const TempItem: React.FC<TempItemProps> = ({
  rec,
  isSelected,
  isSelectionMode,
  onToggleSelect,
  onRequestDelete,
}) => {
  const longPressEvents = useLongPress(
    () => onToggleSelect(rec.id),
    () => {
      if (isSelectionMode) onToggleSelect(rec.id);
    },
    { delay: 450 }
  );

  const isFever = rec.celsius >= 38.0;
  const isSub = rec.celsius >= 37.5 && rec.celsius < 38.0;

  return (
    <div
      {...longPressEvents}
      className={`p-3.5 flex items-start justify-between gap-3 transition select-none ${
        isSelected ? 'bg-rose-50/80' : 'hover:bg-slate-50/70'
      }`}
    >
      <div className="flex items-start gap-3">
        {isSelectionMode ? (
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onToggleSelect(rec.id);
            }}
            className="mt-1 text-rose-600 focus:outline-hidden"
          >
            {isSelected ? (
              <CheckSquare className="w-5 h-5 fill-rose-600 text-white" />
            ) : (
              <Square className="w-5 h-5 text-slate-300" />
            )}
          </button>
        ) : null}

        <div
          className={`p-2 rounded-xl font-black text-sm shrink-0 ${
            isFever
              ? 'bg-rose-100 text-rose-700'
              : isSub
              ? 'bg-amber-100 text-amber-700'
              : 'bg-teal-100 text-teal-700'
          }`}
        >
          {rec.celsius}°C
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-slate-900">{rec.date} • {rec.time} hs</span>
            <span
              className={`text-[10px] px-2 py-0.2 rounded-full font-bold ${
                isFever
                  ? 'bg-rose-100 text-rose-800'
                  : isSub
                  ? 'bg-amber-100 text-amber-800'
                  : 'bg-teal-50 text-teal-800'
              }`}
            >
              {isFever ? 'Fiebre' : isSub ? 'Febrícula' : 'Normal'}
            </span>
          </div>
          {rec.symptoms && rec.symptoms.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-1">
              {rec.symptoms.map((s, idx) => (
                <span
                  key={idx}
                  className="text-[10px] bg-slate-100 text-slate-600 px-2 py-0.5 rounded-md"
                >
                  {s}
                </span>
              ))}
            </div>
          )}
          {rec.medicationGiven && (
            <p className="text-xs text-amber-800 font-semibold mt-1">
              💊 Medicamento administrado: {rec.medicationGiven}
            </p>
          )}
          {rec.notes && <p className="text-xs text-slate-500 mt-0.5 italic">{rec.notes}</p>}
        </div>
      </div>

      <button
        type="button"
        onClick={(e) => {
          e.stopPropagation();
          onRequestDelete();
        }}
        className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition"
        title="Eliminar registro"
      >
        <Trash2 className="w-3.5 h-3.5" />
      </button>
    </div>
  );
};

/* ---------------- Visit Card with Long Press ---------------- */
interface VisitCardProps {
  visit: MedicalVisit;
  isSelected: boolean;
  isSelectionMode: boolean;
  onToggleSelect: (id: string) => void;
  onRequestDelete: () => void;
}

const VisitCard: React.FC<VisitCardProps> = ({
  visit,
  isSelected,
  isSelectionMode,
  onToggleSelect,
  onRequestDelete,
}) => {
  const longPressEvents = useLongPress(
    () => onToggleSelect(visit.id),
    () => {
      if (isSelectionMode) onToggleSelect(visit.id);
    },
    { delay: 450 }
  );

  return (
    <div
      {...longPressEvents}
      className={`rounded-2xl p-4 border transition select-none space-y-2.5 ${
        isSelected
          ? 'bg-rose-50/70 border-rose-400 shadow-sm'
          : 'bg-white border-slate-200/80 shadow-2xs'
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          {isSelectionMode ? (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onToggleSelect(visit.id);
              }}
              className="mt-1 text-rose-600 focus:outline-hidden"
            >
              {isSelected ? (
                <CheckSquare className="w-5 h-5 fill-rose-600 text-white" />
              ) : (
                <Square className="w-5 h-5 text-slate-300" />
              )}
            </button>
          ) : (
            <div
              className={`p-2 rounded-xl ${
                visit.status === 'programada'
                  ? 'bg-indigo-100 text-indigo-700'
                  : 'bg-emerald-100 text-emerald-700'
              }`}
            >
              <Stethoscope className="w-5 h-5" />
            </div>
          )}

          <div>
            <div className="flex items-center gap-2">
              <span
                className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                  visit.status === 'programada'
                    ? 'bg-indigo-100 text-indigo-800'
                    : 'bg-emerald-100 text-emerald-800'
                }`}
              >
                {visit.status === 'programada' ? 'Turno Programado' : 'Consulta Realizada'}
              </span>
              <span className="text-xs text-slate-400">
                {visit.date} a las {visit.time} hs
              </span>
            </div>
            <h4 className="text-base font-bold text-slate-900 mt-0.5">{visit.doctorName}</h4>
            <p className="text-xs text-slate-600 font-medium">
              {visit.specialty} • {visit.clinic}
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onRequestDelete();
          }}
          className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg"
          title="Eliminar consulta"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>

      <div className="bg-slate-50 p-3 rounded-xl border border-slate-100 space-y-1 text-xs">
        <div>
          <span className="font-bold text-slate-700">Motivo: </span>
          <span className="text-slate-900">{visit.reason}</span>
        </div>
        <div>
          <span className="font-bold text-slate-700">Diagnóstico: </span>
          <span className="text-slate-900">{visit.diagnosis}</span>
        </div>
        {visit.indications && (
          <div>
            <span className="font-bold text-slate-700">Indicaciones: </span>
            <span className="text-slate-900">{visit.indications}</span>
          </div>
        )}
        {visit.prescriptions.length > 0 && (
          <div className="pt-1">
            <span className="font-bold text-slate-700">Recetas: </span>
            <span className="text-amber-800 font-medium">{visit.prescriptions.join(', ')}</span>
          </div>
        )}
        {visit.studiesRequested.length > 0 && (
          <div className="pt-0.5">
            <span className="font-bold text-slate-700">Estudios pedidos: </span>
            <span className="text-indigo-800 font-medium">{visit.studiesRequested.join(', ')}</span>
          </div>
        )}
        {visit.nextFollowUpDate && (
          <div className="pt-1 text-[11px] text-slate-500">
            📅 Próximo control sugerido: <strong>{visit.nextFollowUpDate}</strong>
          </div>
        )}
      </div>
    </div>
  );
};

/* ---------------- Study Item with Long Press ---------------- */
interface StudyItemProps {
  study: MedicalStudy;
  isSelected: boolean;
  isSelectionMode: boolean;
  onToggleSelect: (id: string) => void;
  onToggleStatus: () => void;
  onRequestDelete: () => void;
}

const StudyItem: React.FC<StudyItemProps> = ({
  study,
  isSelected,
  isSelectionMode,
  onToggleSelect,
  onToggleStatus,
  onRequestDelete,
}) => {
  const longPressEvents = useLongPress(
    () => onToggleSelect(study.id),
    () => {
      if (isSelectionMode) onToggleSelect(study.id);
    },
    { delay: 450 }
  );

  return (
    <div
      {...longPressEvents}
      className={`p-3 rounded-xl border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 transition select-none ${
        isSelected
          ? 'bg-rose-50/70 border-rose-400'
          : 'border-slate-200 hover:bg-slate-50/70'
      }`}
    >
      <div className="flex items-start gap-3">
        {isSelectionMode ? (
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onToggleSelect(study.id);
            }}
            className="mt-1 text-rose-600 focus:outline-hidden"
          >
            {isSelected ? (
              <CheckSquare className="w-5 h-5 fill-rose-600 text-white" />
            ) : (
              <Square className="w-5 h-5 text-slate-300" />
            )}
          </button>
        ) : null}
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[10px] font-bold uppercase bg-slate-100 text-slate-700 px-2 py-0.5 rounded-md">
              {study.category}
            </span>
            <span
              className={`text-[10px] font-bold px-2 py-0.5 rounded-md ${
                study.status === 'realizado'
                  ? 'bg-emerald-100 text-emerald-800'
                  : study.status === 'programado'
                  ? 'bg-indigo-100 text-indigo-800'
                  : 'bg-amber-100 text-amber-800'
              }`}
            >
              {study.status === 'realizado'
                ? '✓ Realizado'
                : study.status === 'programado'
                ? 'Programado'
                : 'Pendiente'}
            </span>
          </div>
          <h4 className="text-xs font-bold text-slate-900 mt-1">{study.title}</h4>
          {study.doctorNotes && <p className="text-xs text-slate-500">{study.doctorNotes}</p>}
          {study.resultsNotes && (
            <p className="text-xs text-emerald-700 font-medium mt-0.5">
              Resultado: {study.resultsNotes}
            </p>
          )}
        </div>
      </div>

      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onToggleStatus();
          }}
          className={`px-2.5 py-1 text-xs font-bold rounded-lg transition ${
            study.status !== 'realizado'
              ? 'bg-emerald-600 text-white hover:bg-emerald-700'
              : 'text-slate-500 hover:text-slate-800 underline'
          }`}
        >
          {study.status !== 'realizado' ? '✓ Marcar Realizado' : 'Desmarcar'}
        </button>
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onRequestDelete();
          }}
          className="p-1 text-slate-400 hover:text-rose-600"
          title="Eliminar estudio"
        >
          <Trash2 className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
};

/* ---------------- Main HealthTab ---------------- */
export const HealthTab: React.FC = () => {
  const {
    profile,
    medications,
    recordDoseAdministered,
    deleteMedication,
    deleteMedications,
    temperatureRecords,
    deleteTemperatureRecord,
    deleteTemperatureRecords,
    medicalVisits,
    deleteMedicalVisit,
    deleteMedicalVisits,
    medicalStudies,
    updateMedicalStudy,
    deleteMedicalStudy,
    deleteMedicalStudies,
    vaccines,
    toggleVaccineStatus,
    setActiveModal,
  } = useApp();

  const [healthSubTab, setHealthSubTab] = useState<'meds' | 'temp' | 'visits' | 'studies'>('meds');
  const [expandedMedId, setExpandedMedId] = useState<string | null>(null);

  // Multi-selection state
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [deleteDialog, setDeleteDialog] = useState<{
    isOpen: boolean;
    ids: string[];
    title: string;
    description: string;
    category: 'meds' | 'temp' | 'visits' | 'studies';
  }>({
    isOpen: false,
    ids: [],
    title: '',
    description: '',
    category: 'meds',
  });

  const isSelectionMode = selectedIds.length > 0;

  const handleSubTabChange = (tab: 'meds' | 'temp' | 'visits' | 'studies') => {
    setSelectedIds([]);
    setHealthSubTab(tab);
  };

  const handleToggleSelect = (id: string) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const handleSelectAll = () => {
    let allIds: string[] = [];
    if (healthSubTab === 'meds') allIds = medications.map((m) => m.id);
    else if (healthSubTab === 'temp') allIds = temperatureRecords.map((t) => t.id);
    else if (healthSubTab === 'visits') allIds = medicalVisits.map((v) => v.id);
    else if (healthSubTab === 'studies') allIds = medicalStudies.map((s) => s.id);

    if (selectedIds.length === allIds.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(allIds);
    }
  };

  const handleClearSelection = () => {
    setSelectedIds([]);
  };

  // Delete handlers
  const handleRequestBatchDelete = () => {
    if (selectedIds.length === 0) return;
    const catLabels = {
      meds: 'medicamentos',
      temp: 'registros de temperatura',
      visits: 'consultas médicas',
      studies: 'estudios médicos',
    };
    setDeleteDialog({
      isOpen: true,
      ids: selectedIds,
      title: `¿Eliminar ${selectedIds.length} ${catLabels[healthSubTab]}?`,
      description: `Se eliminarán los registros seleccionados permanentemente. Esta acción no se puede deshacer.`,
      category: healthSubTab,
    });
  };

  const handleConfirmDelete = () => {
    const { category, ids } = deleteDialog;
    if (category === 'meds') {
      if (ids.length === 1) deleteMedication(ids[0]);
      else deleteMedications(ids);
    } else if (category === 'temp') {
      if (ids.length === 1) deleteTemperatureRecord(ids[0]);
      else deleteTemperatureRecords(ids);
    } else if (category === 'visits') {
      if (ids.length === 1) deleteMedicalVisit(ids[0]);
      else deleteMedicalVisits(ids);
    } else if (category === 'studies') {
      if (ids.length === 1) deleteMedicalStudy(ids[0]);
      else deleteMedicalStudies(ids);
    }

    setSelectedIds((prev) => prev.filter((id) => !ids.includes(id)));
    setDeleteDialog((prev) => ({ ...prev, isOpen: false, ids: [] }));
  };

  return (
    <div className="space-y-4">
      {/* Subtab Navigator */}
      <div className="bg-white rounded-2xl p-1.5 border border-slate-200/80 shadow-2xs flex overflow-x-auto gap-1">
        <button
          onClick={() => handleSubTabChange('meds')}
          className={`flex-1 min-w-[120px] py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            healthSubTab === 'meds'
              ? 'bg-amber-500 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Pill className="w-3.5 h-3.5" />
          <span>Medicamentos</span>
          {medications.filter((m) => m.active).length > 0 && (
            <span
              className={`text-[10px] px-1.5 py-0.2 rounded-full font-extrabold ${
                healthSubTab === 'meds' ? 'bg-amber-700 text-white' : 'bg-amber-100 text-amber-800'
              }`}
            >
              {medications.filter((m) => m.active).length}
            </span>
          )}
        </button>

        <button
          onClick={() => handleSubTabChange('temp')}
          className={`flex-1 min-w-[120px] py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            healthSubTab === 'temp'
              ? 'bg-rose-600 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Thermometer className="w-3.5 h-3.5" />
          <span>Curva Térmica</span>
        </button>

        <button
          onClick={() => handleSubTabChange('visits')}
          className={`flex-1 min-w-[120px] py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            healthSubTab === 'visits'
              ? 'bg-blue-600 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Stethoscope className="w-3.5 h-3.5" />
          <span>Consultas</span>
        </button>

        <button
          onClick={() => handleSubTabChange('studies')}
          className={`flex-1 min-w-[120px] py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            healthSubTab === 'studies'
              ? 'bg-cyan-600 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <FileText className="w-3.5 h-3.5" />
          <span>Estudios & Vacunas</span>
        </button>
      </div>

      {/* Contextual Multi-Select Bar */}
      {isSelectionMode && (
        <div className="sticky top-2 z-30 bg-rose-900 text-white rounded-2xl p-3 shadow-lg flex items-center justify-between gap-3 animate-in fade-in slide-in-from-top-2">
          <div className="flex items-center gap-3">
            <span className="w-7 h-7 rounded-full bg-rose-800 flex items-center justify-center text-xs font-black">
              {selectedIds.length}
            </span>
            <span className="text-xs font-bold">
              {selectedIds.length === 1
                ? '1 elemento seleccionado'
                : `${selectedIds.length} elementos seleccionados`}
            </span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleSelectAll}
              className="text-xs px-2.5 py-1.5 rounded-xl bg-rose-800/80 hover:bg-rose-800 text-rose-100 font-semibold"
            >
              Todos
            </button>
            <button
              onClick={handleRequestBatchDelete}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white text-rose-700 hover:bg-rose-50 text-xs font-black shadow-xs transition active:scale-95"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>Eliminar ({selectedIds.length})</span>
            </button>
            <button
              onClick={handleClearSelection}
              className="p-1.5 rounded-xl hover:bg-rose-800/80 text-rose-200"
              title="Cancelar selección"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* SUBTAB 1: MEDICAMENTOS */}
      {healthSubTab === 'meds' && (
        <div className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h3 className="text-sm font-bold text-slate-800">Tratamientos y Medicación Activa</h3>
              <p className="text-xs text-slate-500">
                💡 Mantén presionado un medicamento para seleccionar varios y eliminarlos
              </p>
            </div>
            <button
              onClick={() => setActiveModal('medication')}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold shadow-xs transition"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Nuevo Medicamento</span>
            </button>
          </div>

          <div className="space-y-3">
            {medications.length === 0 ? (
              <div className="bg-white rounded-2xl p-8 border border-slate-200 text-center text-slate-400 text-xs">
                No hay medicamentos registrados actualmente
              </div>
            ) : (
              medications.map((med) => (
                <MedCard
                  key={med.id}
                  med={med}
                  isSelected={selectedIds.includes(med.id)}
                  isSelectionMode={isSelectionMode}
                  isExpanded={expandedMedId === med.id}
                  onToggleSelect={handleToggleSelect}
                  onToggleExpand={() => setExpandedMedId(expandedMedId === med.id ? null : med.id)}
                  onRecordDose={() => recordDoseAdministered(med.id)}
                  onRequestDelete={() =>
                    setDeleteDialog({
                      isOpen: true,
                      ids: [med.id],
                      title: `¿Eliminar tratamiento de ${med.name}?`,
                      description: `Se eliminará el medicamento y todo su historial de tomas asociadas.`,
                      category: 'meds',
                    })
                  }
                />
              ))
            )}
          </div>
        </div>
      )}

      {/* SUBTAB 2: TEMPERATURA / FIEBRE */}
      {healthSubTab === 'temp' && (
        <div className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h3 className="text-sm font-bold text-slate-800">Control de Fiebre y Registro Térmico</h3>
              <p className="text-xs text-slate-500">
                💡 Mantén presionado un registro para selección múltiple
              </p>
            </div>
            <button
              onClick={() => setActiveModal('temperature')}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold shadow-xs transition"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Registrar Temperatura</span>
            </button>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200/80 shadow-2xs divide-y divide-slate-100 overflow-hidden">
            {temperatureRecords.length === 0 ? (
              <div className="p-8 text-center text-slate-400 text-xs">
                No hay registros térmicos cargados
              </div>
            ) : (
              temperatureRecords.map((rec) => (
                <TempItem
                  key={rec.id}
                  rec={rec}
                  isSelected={selectedIds.includes(rec.id)}
                  isSelectionMode={isSelectionMode}
                  onToggleSelect={handleToggleSelect}
                  onRequestDelete={() =>
                    setDeleteDialog({
                      isOpen: true,
                      ids: [rec.id],
                      title: '¿Eliminar registro de temperatura?',
                      description: `Se eliminará la medición de ${rec.celsius}°C del ${rec.date} a las ${rec.time} hs.`,
                      category: 'temp',
                    })
                  }
                />
              ))
            )}
          </div>
        </div>
      )}

      {/* SUBTAB 3: CONSULTAS MÉDICAS */}
      {healthSubTab === 'visits' && (
        <div className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h3 className="text-sm font-bold text-slate-800">Consultas y Visitas Pediátricas</h3>
              <p className="text-xs text-slate-500">
                💡 Mantén presionada una consulta para seleccionarla y eliminarla
              </p>
            </div>
            <button
              onClick={() => setActiveModal('visit')}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow-xs transition"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Registrar Consulta</span>
            </button>
          </div>

          <div className="space-y-3">
            {medicalVisits.length === 0 ? (
              <div className="bg-white rounded-2xl p-8 border border-slate-200 text-center text-slate-400 text-xs">
                No hay consultas médicas cargadas
              </div>
            ) : (
              medicalVisits.map((visit) => (
                <VisitCard
                  key={visit.id}
                  visit={visit}
                  isSelected={selectedIds.includes(visit.id)}
                  isSelectionMode={isSelectionMode}
                  onToggleSelect={handleToggleSelect}
                  onRequestDelete={() =>
                    setDeleteDialog({
                      isOpen: true,
                      ids: [visit.id],
                      title: '¿Eliminar consulta médica?',
                      description: `Se eliminará la visita con ${visit.doctorName} del ${visit.date}.`,
                      category: 'visits',
                    })
                  }
                />
              ))
            )}
          </div>
        </div>
      )}

      {/* SUBTAB 4: ESTUDIOS Y VACUNAS */}
      {healthSubTab === 'studies' && (
        <div className="space-y-4">
          {/* Studies List */}
          <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-sm font-bold text-slate-800">Estudios Médicos y Exámenes</h3>
                <p className="text-xs text-slate-500">
                  💡 Mantén presionado un estudio para selección múltiple
                </p>
              </div>
            </div>

            <div className="space-y-2">
              {medicalStudies.length === 0 ? (
                <p className="text-xs text-slate-400 text-center py-4">No hay estudios registrados</p>
              ) : (
                medicalStudies.map((study) => (
                  <StudyItem
                    key={study.id}
                    study={study}
                    isSelected={selectedIds.includes(study.id)}
                    isSelectionMode={isSelectionMode}
                    onToggleSelect={handleToggleSelect}
                    onToggleStatus={() => {
                      if (study.status !== 'realizado') {
                        updateMedicalStudy(study.id, {
                          status: 'realizado',
                          resultsNotes: 'Control satisfactorio y normal.',
                        });
                      } else {
                        updateMedicalStudy(study.id, { status: 'pendiente' });
                      }
                    }}
                    onRequestDelete={() =>
                      setDeleteDialog({
                        isOpen: true,
                        ids: [study.id],
                        title: '¿Eliminar estudio médico?',
                        description: `Se eliminará permanentemente el estudio "${study.title}".`,
                        category: 'studies',
                      })
                    }
                  />
                ))
              )}
            </div>
          </div>

          {/* Vaccines Card */}
          <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Syringe className="w-4 h-4 text-indigo-600" />
                <h3 className="text-sm font-bold text-slate-800">
                  Calendario Oficial de Vacunación (Preescolar)
                </h3>
              </div>
            </div>

            <div className="space-y-2">
              {vaccines.map((vac) => (
                <div
                  key={vac.id}
                  className="p-3 rounded-xl border border-slate-200 flex items-center justify-between gap-3 hover:bg-slate-50/70"
                >
                  <div className="flex items-center gap-3">
                    <button
                      onClick={() => toggleVaccineStatus(vac.id)}
                      className={`w-6 h-6 rounded-lg flex items-center justify-center font-bold text-xs transition ${
                        vac.status === 'aplicada'
                          ? 'bg-emerald-600 text-white'
                          : 'border-2 border-slate-300 text-transparent hover:border-indigo-500'
                      }`}
                    >
                      ✓
                    </button>
                    <div>
                      <h4 className="text-xs font-bold text-slate-900">{vac.name}</h4>
                      <p className="text-[11px] text-slate-500">
                        {vac.diseaseTarget} • Edad recomendada: {vac.targetAgeDescription}
                      </p>
                      {vac.administeredDate && (
                        <p className="text-[10px] text-emerald-700 font-semibold">
                          Aplicada el {vac.administeredDate}{' '}
                          {vac.batchNumber ? `(Lote: ${vac.batchNumber})` : ''}
                        </p>
                      )}
                    </div>
                  </div>

                  <span
                    className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                      vac.status === 'aplicada'
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {vac.status === 'aplicada' ? 'Al día' : 'Pendiente'}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Confirmation Modal */}
      <ConfirmDeleteModal
        isOpen={deleteDialog.isOpen}
        title={deleteDialog.title}
        description={deleteDialog.description}
        count={deleteDialog.ids.length}
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleteDialog((prev) => ({ ...prev, isOpen: false, ids: [] }))}
      />
    </div>
  );
};
