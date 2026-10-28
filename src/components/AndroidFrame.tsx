import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { AndroidStatusBar } from './AndroidStatusBar';
import { AndroidNavBar } from './AndroidNavBar';
import { InAppNotificationBanner } from './InAppNotificationBanner';
import { PWAInstallButton } from './PWAInstallButton';
import {
  Smartphone,
  Maximize2,
  Bell,
  RefreshCw,
  User,
  Cloud,
  Check,
  Settings,
  Heart
} from 'lucide-react';

export const AndroidFrame: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const {
    profile,
    googleSync,
    activeTab,
    setActiveTab,
    isMobileDeviceFrame,
    setIsMobileDeviceFrame,
    resetToSampleData,
    requestWebNotificationPermission,
    isOnboarded,
  } = useApp();

  return (
    <div className="min-h-screen bg-slate-900 text-slate-900 flex flex-col items-center justify-start antialiased selection:bg-rose-500 selection:text-white">
      {/* Top Application Global Header */}
      <header className="no-print w-full bg-slate-950/90 backdrop-blur-md border-b border-rose-950/40 text-white px-3 sm:px-4 py-2 flex flex-wrap items-center justify-between gap-2.5 z-40">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-2xl bg-gradient-to-tr from-rose-500 via-rose-600 to-amber-400 p-0.5 flex items-center justify-center shadow-xs">
            <div className="w-full h-full bg-slate-950 rounded-[14px] flex items-center justify-center text-sm font-bold">
              👶
            </div>
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-extrabold tracking-tight text-white text-base">PequeCare</span>
              <span className="text-[10px] bg-rose-500/20 text-rose-300 border border-rose-500/30 px-2 py-0.2 rounded-full font-bold uppercase">
                OMS • Android
              </span>
            </div>
            <p className="text-[11px] text-rose-200/70 hidden sm:block">
              Seguimiento pediátrico amoroso y curvas de crecimiento OMS
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-1.5 sm:gap-2">
          {/* PWA Install Button */}
          <PWAInstallButton />

          {/* Google Drive Status pill */}
          <button
            onClick={() => setActiveTab('configuracion')}
            className={`px-2.5 py-1 rounded-xl text-xs font-bold transition flex items-center gap-1.5 border ${
              googleSync.isConnected
                ? 'bg-indigo-950/60 text-indigo-300 border-indigo-700/50 hover:bg-indigo-900/60'
                : 'bg-slate-800 text-slate-300 border-slate-700 hover:bg-slate-700'
            }`}
            title={googleSync.isConnected ? `Drive sincronizado: ${googleSync.userEmail}` : 'Conectar con Google Drive'}
          >
            <Cloud className={`w-3.5 h-3.5 ${googleSync.isConnected ? 'text-indigo-400' : 'text-slate-400'}`} />
            <span className="hidden sm:inline">
              {googleSync.isConnected ? 'Drive Conectado' : 'Google Drive'}
            </span>
          </button>

          {/* Web Notifications Toggle */}
          <button
            onClick={requestWebNotificationPermission}
            className="p-1.5 rounded-xl text-slate-300 hover:text-white hover:bg-slate-800 transition"
            title="Activar notificaciones del sistema"
          >
            <Bell className="w-4 h-4" />
          </button>

          {/* Configuration menu trigger */}
          <button
            onClick={() => setActiveTab('configuracion')}
            className={`p-1.5 rounded-xl transition ${
              activeTab === 'configuracion'
                ? 'bg-rose-600 text-white shadow-xs'
                : 'text-slate-300 hover:text-white hover:bg-slate-800'
            }`}
            title="Configuración y Google Drive"
          >
            <Settings className="w-4 h-4" />
          </button>

          {/* Device Frame View Mode Switcher */}
          <button
            onClick={() => setIsMobileDeviceFrame(!isMobileDeviceFrame)}
            className="inline-flex items-center gap-1.5 px-2.5 sm:px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-200 border border-slate-700 transition"
            title="Alternar entre marco de teléfono Android y vista ampliada"
          >
            {isMobileDeviceFrame ? (
              <>
                <Maximize2 className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Vista Completa</span>
              </>
            ) : (
              <>
                <Smartphone className="w-3.5 h-3.5 text-rose-400" />
                <span className="hidden sm:inline">Marco Android</span>
              </>
            )}
          </button>
        </div>
      </header>

      {/* Main Container Area */}
      <main className="w-full flex-1 flex items-center justify-center p-0 sm:p-3 md:p-5 overflow-hidden">
        {isMobileDeviceFrame ? (
          /* ANDROID SMARTPHONE MOCKUP */
          <div className="relative w-full max-w-[420px] h-[92vh] max-h-[890px] bg-slate-950 rounded-[44px] shadow-[0_25px_60px_-15px_rgba(0,0,0,0.7)] border-[10px] border-slate-900 flex flex-col overflow-hidden ring-1 ring-slate-800">
            {/* Top speaker grill */}
            <div className="absolute top-2 left-1/2 -translate-x-1/2 w-20 h-1 bg-slate-800 rounded-full z-40"></div>

            {/* Android Punch Hole Front Camera */}
            <div className="absolute top-3.5 left-1/2 -translate-x-1/2 w-3.5 h-3.5 bg-black rounded-full border border-slate-800 z-40 flex items-center justify-center">
              <div className="w-1.5 h-1.5 rounded-full bg-slate-900"></div>
            </div>

            {/* Android Status Bar */}
            <AndroidStatusBar />

            {/* Heads-up notification overlay */}
            <InAppNotificationBanner />

            {/* Scrollable Screen Content */}
            <div className="flex-1 overflow-y-auto bg-slate-100 p-3.5 scroll-smooth relative">
              {children}
            </div>

            {/* Android Bottom Navigation */}
            {isOnboarded && <AndroidNavBar />}

            {/* Android Gesture Navigation Line */}
            <div className="no-print w-full bg-white flex justify-center py-1.5">
              <div className="w-32 h-1 bg-slate-300 rounded-full"></div>
            </div>
          </div>
        ) : (
          /* WIDE DASHBOARD VIEW */
          <div className="w-full max-w-5xl h-full flex flex-col bg-slate-100 rounded-3xl shadow-xl overflow-hidden border border-slate-200 min-h-[85vh]">
            {/* Heads-up notification overlay */}
            <InAppNotificationBanner />

            {/* Content Area */}
            <div className="flex-1 p-4 md:p-6 overflow-y-auto">
              {children}
            </div>

            {/* Bottom Nav Bar */}
            {isOnboarded && <AndroidNavBar />}
          </div>
        )}
      </main>
    </div>
  );
};
