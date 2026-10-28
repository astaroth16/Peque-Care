import React from 'react';
import { Trash2, AlertTriangle, X } from 'lucide-react';

interface ConfirmDeleteModalProps {
  isOpen: boolean;
  title: string;
  description: string;
  count?: number;
  onConfirm: () => void;
  onCancel: () => void;
}

export const ConfirmDeleteModal: React.FC<ConfirmDeleteModalProps> = ({
  isOpen,
  title,
  description,
  count = 1,
  onConfirm,
  onCancel,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="w-full max-w-sm rounded-3xl bg-white p-5 sm:p-6 shadow-2xl border border-rose-100 space-y-4 animate-in zoom-in-95 duration-150 text-slate-800">
        <div className="w-14 h-14 rounded-2xl bg-rose-100 text-rose-600 flex items-center justify-center mx-auto shadow-inner">
          <Trash2 className="w-7 h-7" />
        </div>

        <div className="text-center space-y-1">
          <h3 className="text-base font-black text-slate-900 tracking-tight">
            {title}
          </h3>
          <p className="text-xs text-slate-600 leading-relaxed">
            {description}
          </p>
          {count > 1 && (
            <span className="inline-block px-2.5 py-0.5 rounded-full bg-rose-50 border border-rose-200 text-rose-700 text-[11px] font-extrabold mt-1">
              {count} elementos seleccionados
            </span>
          )}
        </div>

        <div className="flex items-center gap-2.5 pt-2">
          <button
            type="button"
            onClick={onCancel}
            className="flex-1 py-2.5 px-4 rounded-xl border border-slate-200 text-slate-700 font-semibold text-xs hover:bg-slate-50 transition"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="flex-1 py-2.5 px-4 rounded-xl bg-rose-600 hover:bg-rose-700 active:scale-95 text-white font-bold text-xs shadow-xs transition"
          >
            {count > 1 ? `Eliminar (${count})` : 'Sí, Eliminar'}
          </button>
        </div>
      </div>
    </div>
  );
};
