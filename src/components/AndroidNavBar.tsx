import React from 'react';
import { useApp } from '../context/AppContext';
import {
  Home,
  TrendingUp,
  HeartPulse,
  Clock,
  Sparkles,
  FileText,
  Settings
} from 'lucide-react';

export const AndroidNavBar: React.FC = () => {
  const { activeTab, setActiveTab, medications, googleSync } = useApp();
  const activeMedsCount = medications.filter(m => m.active).length;

  const tabs = [
    { id: 'resumen' as const, label: 'Inicio', icon: Home },
    { id: 'crecimiento' as const, label: 'OMS', icon: TrendingUp },
    { id: 'salud' as const, label: 'Salud', icon: HeartPulse, badge: activeMedsCount },
    { id: 'rutina' as const, label: 'Rutina', icon: Clock },
    { id: 'sugerencias' as const, label: 'Hitos', icon: Sparkles },
    { id: 'informe' as const, label: 'Informe', icon: FileText },
    { id: 'configuracion' as const, label: 'Nube', icon: Settings },
  ];

  return (
    <div className="no-print w-full bg-white/95 backdrop-blur-md border-t border-slate-200/80 px-2 py-1.5 flex items-center justify-around z-30 select-none shadow-lg">
      {tabs.map(tab => {
        const Icon = tab.icon;
        const isActive = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`relative flex flex-col items-center justify-center flex-1 py-1 px-1 rounded-2xl transition-all duration-200 ${
              isActive
                ? 'text-indigo-600 font-bold scale-105'
                : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            {/* Pill active indicator for Android Material You style */}
            <div className={`relative px-3 py-1 rounded-full transition-all ${
              isActive ? 'bg-indigo-100 text-indigo-700' : 'bg-transparent'
            }`}>
              <Icon className="w-4 h-4" />
              {tab.badge !== undefined && tab.badge > 0 && (
                <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-amber-500 text-white text-[9px] font-black flex items-center justify-center shadow-xs">
                  {tab.badge}
                </span>
              )}
            </div>
            <span className="text-[10px] mt-0.5 tracking-tight font-medium">
              {tab.label}
            </span>
          </button>
        );
      })}
    </div>
  );
};
