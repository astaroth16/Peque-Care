import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { useLongPress } from '../../hooks/useLongPress';
import { ConfirmDeleteModal } from '../modals/ConfirmDeleteModal';
import { SleepRecord, MealRecord, ActivityRecord } from '../../types';
import {
  Moon,
  Utensils,
  Bike,
  Droplets,
  Plus,
  Trash2,
  Clock,
  Bell,
  Sparkles,
  CheckCircle2,
  CheckSquare,
  Square,
  X
} from 'lucide-react';

/* ---------------- Sleep Card Item with Long Press ---------------- */
interface SleepItemProps {
  record: SleepRecord;
  isSelected: boolean;
  isSelectionMode: boolean;
  onToggleSelect: (id: string) => void;
  onRequestDelete: () => void;
}

const SleepItem: React.FC<SleepItemProps> = ({
  record,
  isSelected,
  isSelectionMode,
  onToggleSelect,
  onRequestDelete,
}) => {
  const longPressEvents = useLongPress(
    () => onToggleSelect(record.id),
    () => {
      if (isSelectionMode) onToggleSelect(record.id);
    },
    { delay: 450 }
  );

  const hours = Math.floor(record.durationMinutes / 60);
  const mins = record.durationMinutes % 60;

  return (
    <div
      {...longPressEvents}
      className={`p-4 flex items-start justify-between gap-3 transition select-none ${
        isSelected ? 'bg-rose-50/80' : 'hover:bg-slate-50/70'
      }`}
    >
      <div className="flex items-start gap-3">
        {isSelectionMode ? (
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onToggleSelect(record.id);
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
          <div className="p-2.5 rounded-xl bg-violet-100 text-violet-700">
            <Moon className="w-5 h-5" />
          </div>
        )}

        <div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-slate-900">
              {hours}h {mins > 0 ? `${mins}m` : ''} de sueño
            </span>
            <span className="text-[10px] px-2 py-0.2 rounded-full font-bold uppercase bg-violet-100 text-violet-800">
              {record.type === 'noche' ? 'Sueño Nocturno' : 'Siesta'}
            </span>
          </div>
          <p className="text-xs text-slate-600 mt-0.5">
            {record.date} • De {record.startTime} hs a {record.endTime} hs
          </p>
          <p className="text-xs text-slate-500 mt-1">
            Calidad: <strong className="text-slate-700">{record.quality}</strong>
            {record.notes ? ` — ${record.notes}` : ''}
          </p>
        </div>
      </div>

      <button
        type="button"
        onClick={(e) => {
          e.stopPropagation();
          onRequestDelete();
        }}
        className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
        title="Eliminar registro"
      >
        <Trash2 className="w-4 h-4" />
      </button>
    </div>
  );
};

/* ---------------- Meal Card Item with Long Press ---------------- */
interface MealItemProps {
  record: MealRecord;
  isSelected: boolean;
  isSelectionMode: boolean;
  onToggleSelect: (id: string) => void;
  onRequestDelete: () => void;
}

const MealItem: React.FC<MealItemProps> = ({
  record,
  isSelected,
  isSelectionMode,
  onToggleSelect,
  onRequestDelete,
}) => {
  const longPressEvents = useLongPress(
    () => onToggleSelect(record.id),
    () => {
      if (isSelectionMode) onToggleSelect(record.id);
    },
    { delay: 450 }
  );

  return (
    <div
      {...longPressEvents}
      className={`p-4 flex items-start justify-between gap-3 transition select-none ${
        isSelected ? 'bg-rose-50/80' : 'hover:bg-slate-50/70'
      }`}
    >
      <div className="flex items-start gap-3">
        {isSelectionMode ? (
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onToggleSelect(record.id);
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
          <div className="p-2.5 rounded-xl bg-emerald-100 text-emerald-700">
            <Utensils className="w-5 h-5" />
          </div>
        )}

        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold uppercase tracking-wider text-emerald-800">
              {record.mealType}
            </span>
            <span className="text-xs text-slate-400">{record.date} • {record.time} hs</span>
            <span className="text-[10px] bg-slate-100 text-slate-600 px-2 py-0.2 rounded-full font-semibold">
              Apetito: {record.appetite}
            </span>
          </div>

          <div className="flex flex-wrap gap-1.5 mt-1.5">
            {record.foods.map((food, idx) => (
              <span
                key={idx}
                className="text-xs bg-emerald-50 text-emerald-900 border border-emerald-200/60 px-2 py-0.5 rounded-md font-medium"
              >
                {food}
              </span>
            ))}
          </div>

          {record.notes && (
            <p className="text-xs text-slate-500 mt-1 italic">{record.notes}</p>
          )}
        </div>
      </div>

      <button
        type="button"
        onClick={(e) => {
          e.stopPropagation();
          onRequestDelete();
        }}
        className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
        title="Eliminar registro"
      >
        <Trash2 className="w-4 h-4" />
      </button>
    </div>
  );
};

/* ---------------- Activity Card Item with Long Press ---------------- */
interface ActivityItemProps {
  record: ActivityRecord;
  isSelected: boolean;
  isSelectionMode: boolean;
  onToggleSelect: (id: string) => void;
  onRequestDelete: () => void;
}

const ActivityItem: React.FC<ActivityItemProps> = ({
  record,
  isSelected,
  isSelectionMode,
  onToggleSelect,
  onRequestDelete,
}) => {
  const longPressEvents = useLongPress(
    () => onToggleSelect(record.id),
    () => {
      if (isSelectionMode) onToggleSelect(record.id);
    },
    { delay: 450 }
  );

  return (
    <div
      {...longPressEvents}
      className={`p-4 flex items-start justify-between gap-3 transition select-none ${
        isSelected ? 'bg-rose-50/80' : 'hover:bg-slate-50/70'
      }`}
    >
      <div className="flex items-start gap-3">
        {isSelectionMode ? (
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onToggleSelect(record.id);
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
          <div className="p-2.5 rounded-xl bg-cyan-100 text-cyan-700">
            <Bike className="w-5 h-5" />
          </div>
        )}

        <div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-slate-900">{record.title}</span>
            <span className="text-[10px] px-2 py-0.2 rounded-full font-bold uppercase bg-cyan-100 text-cyan-800">
              {record.category}
            </span>
          </div>
          <p className="text-xs text-slate-600 mt-0.5">
            {record.date} • {record.time} hs • Duración: {record.durationMinutes} minutos
          </p>
          {record.reminderEnabled && record.reminderTime && (
            <div className="flex items-center gap-1.5 text-xs text-cyan-700 font-semibold mt-1">
              <Bell className="w-3.5 h-3.5" />
              <span>Alarma activa a las {record.reminderTime} hs</span>
            </div>
          )}
          {record.notes && (
            <p className="text-xs text-slate-500 mt-1 italic">{record.notes}</p>
          )}
        </div>
      </div>

      <button
        type="button"
        onClick={(e) => {
          e.stopPropagation();
          onRequestDelete();
        }}
        className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
        title="Eliminar actividad"
      >
        <Trash2 className="w-4 h-4" />
      </button>
    </div>
  );
};

/* ---------------- Main RoutineTab Component ---------------- */
export const RoutineTab: React.FC = () => {
  const {
    sleepRecords,
    deleteSleepRecord,
    deleteSleepRecords,
    mealRecords,
    deleteMealRecord,
    deleteMealRecords,
    activityRecords,
    deleteActivityRecord,
    deleteActivityRecords,
    todayWaterGlasses,
    incrementWater,
    decrementWater,
    setActiveModal,
  } = useApp();

  const [routineSection, setRoutineSection] = useState<'sleep' | 'meals' | 'activities'>('sleep');

  // Multi-selection state
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [deleteDialog, setDeleteDialog] = useState<{
    isOpen: boolean;
    ids: string[];
    title: string;
    description: string;
    section: 'sleep' | 'meals' | 'activities';
  }>({
    isOpen: false,
    ids: [],
    title: '',
    description: '',
    section: 'sleep',
  });

  const isSelectionMode = selectedIds.length > 0;

  const handleSectionChange = (sec: 'sleep' | 'meals' | 'activities') => {
    setSelectedIds([]);
    setRoutineSection(sec);
  };

  const handleToggleSelect = (id: string) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const handleSelectAll = () => {
    let allIds: string[] = [];
    if (routineSection === 'sleep') allIds = sleepRecords.map((s) => s.id);
    else if (routineSection === 'meals') allIds = mealRecords.map((m) => m.id);
    else if (routineSection === 'activities') allIds = activityRecords.map((a) => a.id);

    if (selectedIds.length === allIds.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(allIds);
    }
  };

  const handleClearSelection = () => {
    setSelectedIds([]);
  };

  const handleRequestBatchDelete = () => {
    if (selectedIds.length === 0) return;
    const labels = {
      sleep: 'registros de sueño',
      meals: 'registros de comida',
      activities: 'actividades',
    };
    setDeleteDialog({
      isOpen: true,
      ids: selectedIds,
      title: `¿Eliminar ${selectedIds.length} ${labels[routineSection]}?`,
      description: `Se eliminarán permanentemente los registros seleccionados. Esta acción no se puede deshacer.`,
      section: routineSection,
    });
  };

  const handleConfirmDelete = () => {
    const { section, ids } = deleteDialog;
    if (section === 'sleep') {
      if (ids.length === 1) deleteSleepRecord(ids[0]);
      else deleteSleepRecords(ids);
    } else if (section === 'meals') {
      if (ids.length === 1) deleteMealRecord(ids[0]);
      else deleteMealRecords(ids);
    } else if (section === 'activities') {
      if (ids.length === 1) deleteActivityRecord(ids[0]);
      else deleteActivityRecords(ids);
    }

    setSelectedIds((prev) => prev.filter((id) => !ids.includes(id)));
    setDeleteDialog((prev) => ({ ...prev, isOpen: false, ids: [] }));
  };

  return (
    <div className="space-y-4">
      {/* Top Section Navigator */}
      <div className="bg-white rounded-2xl p-1.5 border border-slate-200/80 shadow-2xs flex overflow-x-auto gap-1">
        <button
          onClick={() => handleSectionChange('sleep')}
          className={`flex-1 py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            routineSection === 'sleep'
              ? 'bg-violet-600 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Moon className="w-3.5 h-3.5" />
          <span>Sueño y Descanso</span>
        </button>

        <button
          onClick={() => handleSectionChange('meals')}
          className={`flex-1 py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            routineSection === 'meals'
              ? 'bg-emerald-600 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Utensils className="w-3.5 h-3.5" />
          <span>Alimentos & Agua</span>
        </button>

        <button
          onClick={() => handleSectionChange('activities')}
          className={`flex-1 py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            routineSection === 'activities'
              ? 'bg-cyan-600 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Bike className="w-3.5 h-3.5" />
          <span>Actividades & Alarmas</span>
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

      {/* SECTION 1: SLEEP */}
      {routineSection === 'sleep' && (
        <div className="space-y-3">
          <div className="bg-gradient-to-r from-violet-600 to-indigo-700 rounded-2xl p-4 text-white shadow-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div>
              <div className="flex items-center gap-2">
                <Moon className="w-5 h-5" />
                <h3 className="font-extrabold text-base">Higiene del Sueño Infantil</h3>
              </div>
              <p className="text-xs text-violet-100 mt-0.5">
                La OMS recomienda entre 10 y 13 horas de sueño diario total (incluida siesta de 1 a 2 horas)
              </p>
              <p className="text-[11px] text-violet-200 mt-1">
                💡 Mantén presionado un registro para activar selección múltiple
              </p>
            </div>
            <button
              onClick={() => setActiveModal('sleep')}
              className="px-4 py-2 bg-white text-violet-900 rounded-xl text-xs font-bold hover:bg-violet-50 transition shadow-xs"
            >
              + Registrar Sueño
            </button>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200/80 shadow-2xs divide-y divide-slate-100 overflow-hidden">
            {sleepRecords.length === 0 ? (
              <div className="p-8 text-center text-slate-400 text-xs">
                No hay registros de sueño ingresados
              </div>
            ) : (
              sleepRecords.map((s) => (
                <SleepItem
                  key={s.id}
                  record={s}
                  isSelected={selectedIds.includes(s.id)}
                  isSelectionMode={isSelectionMode}
                  onToggleSelect={handleToggleSelect}
                  onRequestDelete={() =>
                    setDeleteDialog({
                      isOpen: true,
                      ids: [s.id],
                      title: '¿Eliminar registro de sueño?',
                      description: `Se eliminará el registro de descanso del ${s.date} (${s.startTime} a ${s.endTime} hs).`,
                      section: 'sleep',
                    })
                  }
                />
              ))
            )}
          </div>
        </div>
      )}

      {/* SECTION 2: MEALS & WATER */}
      {routineSection === 'meals' && (
        <div className="space-y-3">
          {/* Water quick banner */}
          <div className="bg-gradient-to-r from-blue-500 to-cyan-600 rounded-2xl p-4 text-white shadow-xs flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-white/20">
                <Droplets className="w-5 h-5 text-white" />
              </div>
              <div>
                <h3 className="font-extrabold text-sm">Consumo de Agua Hoy</h3>
                <p className="text-xs text-blue-100">
                  {todayWaterGlasses} vasos bebidos (aprox {todayWaterGlasses * 200} ml)
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={decrementWater}
                className="w-8 h-8 rounded-xl bg-white/20 hover:bg-white/30 text-white font-bold text-sm"
              >
                -
              </button>
              <button
                onClick={incrementWater}
                className="w-8 h-8 rounded-xl bg-white text-blue-800 hover:bg-blue-50 font-bold text-sm shadow-xs"
              >
                +
              </button>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pt-1">
            <div>
              <h3 className="text-sm font-bold text-slate-800">Diario de Comidas e Ingesta</h3>
              <p className="text-xs text-slate-500">
                💡 Mantén presionado un registro para selección múltiple
              </p>
            </div>
            <button
              onClick={() => setActiveModal('meal')}
              className="px-3 py-2 bg-emerald-600 text-white rounded-xl text-xs font-bold hover:bg-emerald-700 transition"
            >
              + Registrar Comida
            </button>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200/80 shadow-2xs divide-y divide-slate-100 overflow-hidden">
            {mealRecords.length === 0 ? (
              <div className="p-8 text-center text-slate-400 text-xs">
                No hay comidas registradas aún
              </div>
            ) : (
              mealRecords.map((m) => (
                <MealItem
                  key={m.id}
                  record={m}
                  isSelected={selectedIds.includes(m.id)}
                  isSelectionMode={isSelectionMode}
                  onToggleSelect={handleToggleSelect}
                  onRequestDelete={() =>
                    setDeleteDialog({
                      isOpen: true,
                      ids: [m.id],
                      title: `¿Eliminar comida (${m.mealType})?`,
                      description: `Se eliminará el registro del ${m.date} a las ${m.time} hs con ${m.foods.join(', ')}.`,
                      section: 'meals',
                    })
                  }
                />
              ))
            )}
          </div>
        </div>
      )}

      {/* SECTION 3: ACTIVITIES & REMINDERS */}
      {routineSection === 'activities' && (
        <div className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h3 className="text-sm font-bold text-slate-800">Actividades y Recordatorios Diarios</h3>
              <p className="text-xs text-slate-500">
                💡 Mantén presionada una actividad para selección múltiple
              </p>
            </div>
            <button
              onClick={() => setActiveModal('activity')}
              className="px-3 py-2 bg-cyan-600 text-white rounded-xl text-xs font-bold hover:bg-cyan-700 transition"
            >
              + Registrar Actividad
            </button>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200/80 shadow-2xs divide-y divide-slate-100 overflow-hidden">
            {activityRecords.length === 0 ? (
              <div className="p-8 text-center text-slate-400 text-xs">
                No hay actividades registradas
              </div>
            ) : (
              activityRecords.map((act) => (
                <ActivityItem
                  key={act.id}
                  record={act}
                  isSelected={selectedIds.includes(act.id)}
                  isSelectionMode={isSelectionMode}
                  onToggleSelect={handleToggleSelect}
                  onRequestDelete={() =>
                    setDeleteDialog({
                      isOpen: true,
                      ids: [act.id],
                      title: `¿Eliminar actividad "${act.title}"?`,
                      description: `Se eliminará la actividad del ${act.date} y cualquier alarma vinculada.`,
                      section: 'activities',
                    })
                  }
                />
              ))
            )}
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
