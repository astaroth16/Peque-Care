import React from 'react';
import { useApp } from '../context/AppContext';
import { Bell, Pill, Calendar, AlertTriangle, X, Check } from 'lucide-react';

export const InAppNotificationBanner: React.FC = () => {
  const { notifications, dismissNotification } = useApp();
  const latestNotification = notifications.find(n => !n.isRead);

  if (!latestNotification) return null;

  const iconMap = {
    medicamento: <Pill className="w-5 h-5 text-amber-600" />,
    consulta: <Calendar className="w-5 h-5 text-indigo-600" />,
    estudio: <Calendar className="w-5 h-5 text-cyan-600" />,
    actividad: <Bell className="w-5 h-5 text-emerald-600" />,
    agua: <Bell className="w-5 h-5 text-blue-600" />,
  };

  return (
    <div className="fixed top-12 left-1/2 -translate-x-1/2 z-50 w-11/12 max-w-md animate-in slide-in-from-top-4 duration-300">
      <div className={`p-3.5 rounded-2xl shadow-xl border backdrop-blur-md flex items-start gap-3 ${
        latestNotification.urgent
          ? 'bg-rose-50/95 border-rose-300 text-rose-900 shadow-rose-200/50'
          : 'bg-white/95 border-indigo-100 text-slate-800 shadow-slate-300/40'
      }`}>
        <div className="p-2 rounded-xl bg-white shadow-xs shrink-0">
          {latestNotification.urgent ? (
            <AlertTriangle className="w-5 h-5 text-rose-600 animate-bounce" />
          ) : (
            iconMap[latestNotification.type] || <Bell className="w-5 h-5 text-indigo-600" />
          )}
        </div>

        <div className="flex-1 min-w-0 pr-1">
          <div className="flex items-center justify-between gap-1">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Notificación Android
            </h4>
            <span className="text-[10px] text-slate-400">Ahora</span>
          </div>
          <p className="text-sm font-bold mt-0.5 truncate text-slate-900">
            {latestNotification.title}
          </p>
          <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">
            {latestNotification.message}
          </p>
        </div>

        <button
          onClick={() => dismissNotification(latestNotification.id)}
          className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 shrink-0 transition"
          aria-label="Cerrar notificación"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
