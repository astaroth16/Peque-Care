import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { WHOGrowthChart } from '../WHOGrowthChart';
import { formatAgeString } from '../../utils/whoStandards';
import { useLongPress } from '../../hooks/useLongPress';
import { ConfirmDeleteModal } from '../modals/ConfirmDeleteModal';
import { GrowthRecord } from '../../types';
import {
  Scale,
  Ruler,
  TrendingUp,
  Plus,
  Trash2,
  Info,
  Sparkles,
  HelpCircle,
  CheckCircle2,
  CheckSquare,
  Square,
  X
} from 'lucide-react';

interface GrowthRowProps {
  record: GrowthRecord;
  isSelected: boolean;
  isSelectionMode: boolean;
  onToggleSelect: (id: string) => void;
  onRequestDelete: (record: GrowthRecord) => void;
}

const GrowthRow: React.FC<GrowthRowProps> = ({
  record,
  isSelected,
  isSelectionMode,
  onToggleSelect,
  onRequestDelete,
}) => {
  const longPressEvents = useLongPress(
    () => {
      onToggleSelect(record.id);
    },
    () => {
      if (isSelectionMode) {
        onToggleSelect(record.id);
      }
    },
    { delay: 450 }
  );

  return (
    <tr
      {...longPressEvents}
      className={`cursor-pointer transition select-none ${
        isSelected
          ? 'bg-rose-50/80 hover:bg-rose-100/60'
          : 'hover:bg-slate-50/70'
      }`}
    >
      <td className="py-3 px-3.5 whitespace-nowrap">
        <div className="flex items-center gap-2">
          {isSelectionMode ? (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onToggleSelect(record.id);
              }}
              className="text-rose-600 focus:outline-hidden"
            >
              {isSelected ? (
                <CheckSquare className="w-4 h-4 fill-rose-600 text-white" />
              ) : (
                <Square className="w-4 h-4 text-slate-300" />
              )}
            </button>
          ) : null}
          <span className="font-bold text-slate-900">{record.date}</span>
        </div>
      </td>
      <td className="py-3 px-3 text-slate-600 whitespace-nowrap">
        {formatAgeString(record.ageMonths)}
      </td>
      <td className="py-3 px-3">
        <span className="font-extrabold text-slate-900">{record.weightKg} kg</span>
        <span className="block text-[10px] text-emerald-700 font-medium">
          P{record.weightPercentile} (Z: {record.weightZScore > 0 ? `+${record.weightZScore}` : record.weightZScore})
        </span>
      </td>
      <td className="py-3 px-3">
        <span className="font-extrabold text-slate-900">{record.heightCm} cm</span>
        <span className="block text-[10px] text-emerald-700 font-medium">
          P{record.heightPercentile} (Z: {record.heightZScore > 0 ? `+${record.heightZScore}` : record.heightZScore})
        </span>
      </td>
      <td className="py-3 px-3">
        <span className="font-bold text-slate-900">{record.bmi}</span>
        <span className="block text-[10px] text-slate-500">
          P{record.bmiPercentile}
        </span>
      </td>
      <td className="py-3 px-3 text-slate-600">
        {record.headCircumferenceCm ? `${record.headCircumferenceCm} cm` : '-'}
      </td>
      <td className="py-3 px-3 text-slate-600 max-w-[200px] truncate" title={record.notes}>
        {record.notes || '-'}
      </td>
      <td className="py-3 px-3 text-right">
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onRequestDelete(record);
          }}
          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition"
          title="Eliminar este registro"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </td>
    </tr>
  );
};

export const GrowthTab: React.FC = () => {
  const {
    profile,
    growthRecords,
    deleteGrowthRecord,
    deleteGrowthRecords,
    latestGrowth,
    setActiveModal,
  } = useApp();

  const [showWhoInfo, setShowWhoInfo] = useState(false);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [deleteDialog, setDeleteDialog] = useState<{
    isOpen: boolean;
    ids: string[];
    title: string;
    description: string;
  }>({
    isOpen: false,
    ids: [],
    title: '',
    description: '',
  });

  const isSelectionMode = selectedIds.length > 0;

  const handleToggleSelect = (id: string) => {
    setSelectedIds(prev =>
      prev.includes(id) ? prev.filter(item => item !== id) : [...prev, id]
    );
  };

  const handleSelectAll = () => {
    if (selectedIds.length === growthRecords.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(growthRecords.map(r => r.id));
    }
  };

  const handleClearSelection = () => {
    setSelectedIds([]);
  };

  const handleRequestSingleDelete = (record: GrowthRecord) => {
    setDeleteDialog({
      isOpen: true,
      ids: [record.id],
      title: '¿Eliminar medición antropométrica?',
      description: `Se eliminará el registro del ${record.date} (${record.weightKg} kg, ${record.heightCm} cm). Esta acción no se puede deshacer.`,
    });
  };

  const handleRequestBatchDelete = () => {
    if (selectedIds.length === 0) return;
    setDeleteDialog({
      isOpen: true,
      ids: selectedIds,
      title: `¿Eliminar ${selectedIds.length} registros seleccionados?`,
      description: `Se eliminarán permanentemente las mediciones seleccionadas del historial de ${profile.name}. Esta acción no se puede revertir.`,
    });
  };

  const handleConfirmDelete = () => {
    if (deleteDialog.ids.length === 1) {
      deleteGrowthRecord(deleteDialog.ids[0]);
    } else if (deleteDialog.ids.length > 1) {
      deleteGrowthRecords(deleteDialog.ids);
    }
    setSelectedIds(prev => prev.filter(id => !deleteDialog.ids.includes(id)));
    setDeleteDialog(prev => ({ ...prev, isOpen: false, ids: [] }));
  };

  return (
    <div className="space-y-4">
      {/* Header and Add Action */}
      <div className="bg-white rounded-2xl p-4 md:p-5 border border-slate-200/80 shadow-2xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-xl bg-indigo-50 text-indigo-600">
              <TrendingUp className="w-5 h-5" />
            </span>
            <h2 className="text-lg font-black text-slate-900">
              Crecimiento y Curvas OMS
            </h2>
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            Monitoreo antropométrico oficial para niñas mayores de 3 años
          </p>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <button
            onClick={() => setShowWhoInfo(!showWhoInfo)}
            className="p-2 rounded-xl text-slate-500 hover:text-indigo-600 hover:bg-slate-100 transition"
            title="¿Cómo interpretar las curvas OMS?"
          >
            <HelpCircle className="w-5 h-5" />
          </button>
          <button
            onClick={() => setActiveModal('growth')}
            className="flex-1 sm:flex-none inline-flex items-center justify-center gap-1.5 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold shadow-sm transition active:scale-95"
          >
            <Plus className="w-4 h-4" />
            <span>Registrar Peso y Talla</span>
          </button>
        </div>
      </div>

      {/* Explanatory banner if toggled */}
      {showWhoInfo && (
        <div className="rounded-2xl p-4 bg-indigo-50/80 border border-indigo-200 text-xs text-indigo-950 space-y-2 animate-in fade-in duration-200">
          <div className="flex items-center justify-between font-bold text-sm text-indigo-900">
            <span className="flex items-center gap-1.5">
              <Info className="w-4 h-4 text-indigo-600" />
              Guía de Interpretación de las Curvas OMS
            </span>
            <button onClick={() => setShowWhoInfo(false)} className="text-indigo-600 hover:text-indigo-900">×</button>
          </div>
          <p className="leading-relaxed">
            La <strong>Organización Mundial de la Salud (OMS)</strong> establece los patrones internacionales de crecimiento infantil para niñas sanas:
          </p>
          <ul className="list-disc pl-4 space-y-1 text-slate-700">
            <li><strong>Percentil 50 (Mediana / 0 DE):</strong> Es el promedio exacto de crecimiento de la población infantil de referencia saludable.</li>
            <li><strong>Rango Normal Saludable (-2 DE a +2 DE / Percentil 3 a 97):</strong> Más del 95% de los niños sanos crecen dentro de esta franja verde.</li>
            <li><strong>Z-Score (Desviación Estándar):</strong> Mide qué tan cerca o lejos está el niño de la mediana. Valores entre -1 y +1 representan un desarrollo nutricional óptimo.</li>
          </ul>
        </div>
      )}

      {/* Latest Metrics Cards */}
      {latestGrowth && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs">
            <div className="flex items-center justify-between text-slate-500 mb-1">
              <span className="text-xs font-bold uppercase tracking-wider">Peso Actual</span>
              <Scale className="w-4 h-4 text-indigo-600" />
            </div>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl font-black text-slate-900">{latestGrowth.weightKg} kg</span>
              <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
                Percentil {latestGrowth.weightPercentile}
              </span>
            </div>
            <p className="text-xs text-slate-600 mt-1">
              Z-Score: <strong>{latestGrowth.weightZScore > 0 ? `+${latestGrowth.weightZScore}` : latestGrowth.weightZScore} DE</strong> • {latestGrowth.weightStatus}
            </p>
          </div>

          <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs">
            <div className="flex items-center justify-between text-slate-500 mb-1">
              <span className="text-xs font-bold uppercase tracking-wider">Talla / Estatura</span>
              <Ruler className="w-4 h-4 text-indigo-600" />
            </div>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl font-black text-slate-900">{latestGrowth.heightCm} cm</span>
              <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
                Percentil {latestGrowth.heightPercentile}
              </span>
            </div>
            <p className="text-xs text-slate-600 mt-1">
              Z-Score: <strong>{latestGrowth.heightZScore > 0 ? `+${latestGrowth.heightZScore}` : latestGrowth.heightZScore} DE</strong> • {latestGrowth.heightStatus}
            </p>
          </div>

          <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs">
            <div className="flex items-center justify-between text-slate-500 mb-1">
              <span className="text-xs font-bold uppercase tracking-wider">IMC para la Edad</span>
              <Sparkles className="w-4 h-4 text-emerald-600" />
            </div>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl font-black text-slate-900">{latestGrowth.bmi}</span>
              <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
                Percentil {latestGrowth.bmiPercentile}
              </span>
            </div>
            <p className="text-xs text-slate-600 mt-1">
              {latestGrowth.bmiStatus}
            </p>
          </div>
        </div>
      )}

      {/* Interactive WHO Curves Chart Component */}
      <WHOGrowthChart
        records={growthRecords}
        childName={profile.name}
      />

      {/* Contextual Multi-Select Bar */}
      {isSelectionMode && (
        <div className="sticky top-2 z-30 bg-rose-900 text-white rounded-2xl p-3 shadow-lg flex items-center justify-between gap-3 animate-in fade-in slide-in-from-top-2">
          <div className="flex items-center gap-3">
            <span className="w-7 h-7 rounded-full bg-rose-800 flex items-center justify-center text-xs font-black">
              {selectedIds.length}
            </span>
            <span className="text-xs font-bold">
              {selectedIds.length === 1 ? '1 registro seleccionado' : `${selectedIds.length} registros seleccionados`}
            </span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleSelectAll}
              className="text-xs px-2.5 py-1.5 rounded-xl bg-rose-800/80 hover:bg-rose-800 text-rose-100 font-semibold"
            >
              {selectedIds.length === growthRecords.length ? 'Deseleccionar' : 'Todos'}
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

      {/* Historical Growth Log Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-2xs overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div>
            <h3 className="font-bold text-slate-900 text-sm">
              Historial Cronológico de Mediciones Antropométricas
            </h3>
            <p className="text-[11px] text-slate-400 mt-0.5">
              💡 Mantén presionado un registro para activar la selección múltiple
            </p>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-500">
              {growthRecords.length} {growthRecords.length === 1 ? 'registro' : 'registros'}
            </span>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-700">
            <thead className="bg-slate-50 text-slate-500 font-bold uppercase tracking-wider text-[10px] border-b border-slate-200">
              <tr>
                <th className="py-3 px-3.5">Fecha</th>
                <th className="py-3 px-3">Edad</th>
                <th className="py-3 px-3">Peso (kg) / OMS</th>
                <th className="py-3 px-3">Talla (cm) / OMS</th>
                <th className="py-3 px-3">IMC (kg/m²)</th>
                <th className="py-3 px-3">Perím. Cefálico</th>
                <th className="py-3 px-3">Observaciones</th>
                <th className="py-3 px-3 text-right">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {[...growthRecords]
                .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
                .map(r => (
                  <GrowthRow
                    key={r.id}
                    record={r}
                    isSelected={selectedIds.includes(r.id)}
                    isSelectionMode={isSelectionMode}
                    onToggleSelect={handleToggleSelect}
                    onRequestDelete={handleRequestSingleDelete}
                  />
                ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Confirmation Modal */}
      <ConfirmDeleteModal
        isOpen={deleteDialog.isOpen}
        title={deleteDialog.title}
        description={deleteDialog.description}
        count={deleteDialog.ids.length}
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleteDialog(prev => ({ ...prev, isOpen: false, ids: [] }))}
      />
    </div>
  );
};
