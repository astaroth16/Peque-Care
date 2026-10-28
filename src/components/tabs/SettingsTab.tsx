import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { InstallModal } from '../InstallModal';
import {
  Cloud,
  CloudCheck,
  RefreshCw,
  LogOut,
  User,
  ShieldCheck,
  Calendar,
  Download,
  Upload,
  Smartphone,
  Maximize2,
  Bell,
  Heart,
  AlertTriangle,
  CheckCircle2,
  Lock,
  QrCode
} from 'lucide-react';

export const SettingsTab: React.FC = () => {
  const {
    profile,
    updateProfile,
    googleSync,
    signInWithGoogle,
    signOutGoogle,
    triggerGoogleDriveSync,
    restoreFromGoogleDrive,
    toggleAutoSync,
    isMobileDeviceFrame,
    setIsMobileDeviceFrame,
    requestWebNotificationPermission,
    resetToSampleData,
    exportJSON,
    setIsOnboarded,
  } = useApp();

  const [confirmRestoreOpen, setConfirmRestoreOpen] = useState(false);
  const [profileEditSuccess, setProfileEditSuccess] = useState(false);
  const [showInstallModal, setShowInstallModal] = useState(false);

  // Editable Profile fields
  const [name, setName] = useState(profile.name);
  const [birthDate, setBirthDate] = useState(profile.birthDate);
  const [bloodType, setBloodType] = useState(profile.bloodType);
  const [allergies, setAllergies] = useState(profile.allergies.join(', '));
  const [pediatricianName, setPediatricianName] = useState(profile.pediatricianName);
  const [clinicName, setClinicName] = useState(profile.clinicName);

  const handleSaveProfile = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfile({
      name: name.trim(),
      birthDate,
      bloodType: bloodType.trim(),
      allergies: allergies.split(',').map(s => s.trim()).filter(Boolean),
      pediatricianName: pediatricianName.trim(),
      clinicName: clinicName.trim(),
    });
    setProfileEditSuccess(true);
    setTimeout(() => setProfileEditSuccess(false), 3000);
  };

  const handleConfirmRestore = async () => {
    setConfirmRestoreOpen(false);
    await restoreFromGoogleDrive();
  };

  return (
    <div className="space-y-4 max-w-2xl mx-auto">
      {/* Title */}
      <div className="bg-white rounded-3xl p-5 border border-rose-100/80 shadow-2xs">
        <h2 className="text-lg font-black text-slate-900 flex items-center gap-2">
          <span>⚙️</span>
          <span>Configuración y Sincronización</span>
        </h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Respaldo seguro en Google Drive, perfil de tu hija y preferencias
        </p>
      </div>

      {/* 1. GOOGLE DRIVE SYNC & AUTH */}
      <div className="bg-white rounded-3xl p-5 border border-rose-100 shadow-2xs space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="p-2.5 rounded-2xl bg-gradient-to-tr from-blue-500 to-indigo-600 text-white shadow-xs">
              <Cloud className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-900">
                Copia de Seguridad en Google Drive
              </h3>
              <p className="text-xs text-slate-500">
                Sincronización frecuente y segura en tu cuenta personal
              </p>
            </div>
          </div>

          {googleSync.isConnected && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              Conectado
            </span>
          )}
        </div>

        {/* Connection State */}
        {!googleSync.isConnected ? (
          <div className="bg-slate-50 rounded-2xl p-4 border border-slate-200 text-center space-y-3">
            <p className="text-xs text-slate-600 leading-relaxed max-w-sm mx-auto">
              Inicia sesión con tu cuenta de Google para guardar automáticamente las curvas de crecimiento, historial térmico, tratamientos y vacunas en tu Google Drive.
            </p>

            {/* Google Sign In Official Style Button */}
            <button
              onClick={signInWithGoogle}
              className="inline-flex items-center justify-center gap-3 px-5 py-3 rounded-2xl bg-white border border-slate-300 text-slate-700 font-bold text-xs shadow-xs hover:bg-slate-50 hover:shadow-md transition active:scale-95"
            >
              <svg className="w-4 h-4" viewBox="0 0 48 48">
                <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"></path>
                <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"></path>
                <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"></path>
                <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"></path>
              </svg>
              <span>Iniciar sesión con cuenta Google</span>
            </button>
          </div>
        ) : (
          <div className="space-y-3">
            {/* User Connected Card */}
            <div className="flex items-center justify-between p-3.5 rounded-2xl bg-indigo-50/60 border border-indigo-100">
              <div className="flex items-center gap-3">
                {googleSync.userPhoto ? (
                  <img
                    src={googleSync.userPhoto}
                    alt={googleSync.userName || 'Usuario'}
                    className="w-10 h-10 rounded-full border border-indigo-200"
                  />
                ) : (
                  <div className="w-10 h-10 rounded-full bg-indigo-600 text-white font-bold flex items-center justify-center text-sm">
                    {googleSync.userName ? googleSync.userName[0] : 'G'}
                  </div>
                )}
                <div>
                  <h4 className="text-xs font-bold text-slate-900">
                    {googleSync.userName || 'Cuenta Google'}
                  </h4>
                  <p className="text-[11px] text-slate-500">
                    {googleSync.userEmail}
                  </p>
                </div>
              </div>

              <button
                onClick={signOutGoogle}
                className="px-3 py-1.5 rounded-xl border border-rose-200 text-rose-700 hover:bg-rose-50 text-xs font-semibold transition flex items-center gap-1.5"
                title="Cerrar sesión"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span>Desconectar</span>
              </button>
            </div>

            {/* Sync Controls */}
            <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <span className="text-xs font-bold text-slate-800 block">
                    Sincronización Automática Frecuente
                  </span>
                  <span className="text-[11px] text-slate-500">
                    Guarda cambios cada vez que registras datos
                  </span>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={googleSync.autoSync}
                    onChange={toggleAutoSync}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-indigo-600"></div>
                </label>
              </div>

              <div className="pt-2 border-t border-slate-200 flex flex-wrap items-center justify-between gap-2 text-xs">
                <div>
                  <span className="text-slate-500">Última sincronización: </span>
                  <strong className="text-slate-800">
                    {googleSync.lastSyncTime
                      ? new Date(googleSync.lastSyncTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })
                      : 'Pendiente'}
                  </strong>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={triggerGoogleDriveSync}
                    disabled={googleSync.isSyncing}
                    className="px-3.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-xs transition flex items-center gap-1.5 disabled:opacity-50"
                  >
                    <RefreshCw className={`w-3.5 h-3.5 ${googleSync.isSyncing ? 'animate-spin' : ''}`} />
                    <span>{googleSync.isSyncing ? 'Sincronizando...' : 'Sincronizar a Drive'}</span>
                  </button>

                  <button
                    onClick={() => setConfirmRestoreOpen(true)}
                    className="px-3 py-2 rounded-xl bg-white border border-slate-300 text-slate-700 hover:bg-slate-100 font-semibold text-xs transition flex items-center gap-1.5"
                    title="Restaurar datos desde la copia en Drive"
                  >
                    <Upload className="w-3.5 h-3.5 text-indigo-600" />
                    <span>Restaurar de Drive</span>
                  </button>
                </div>
              </div>

              {googleSync.syncError && (
                <div className="p-2.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 shrink-0 text-rose-600" />
                  <span>{googleSync.syncError}</span>
                </div>
              )}
            </div>

            <p className="text-[11px] text-slate-400 flex items-center gap-1">
              <Lock className="w-3 h-3 text-slate-400" />
              <span>
                Archivo en Drive: <strong>PequeCare_Respaldo_Salud_{profile.name.replace(/\s+/g, '_')}.json</strong>
              </span>
            </p>
          </div>
        )}
      </div>

      {/* 2. EDIT CHILD PROFILE */}
      <div className="bg-white rounded-3xl p-5 border border-rose-100 shadow-2xs space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="p-2.5 rounded-2xl bg-rose-100 text-rose-700">
              <User className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-900">
                Perfil de {profile.name}
              </h3>
              <p className="text-xs text-slate-500">
                Información básica, pediatra y antecedentes médicos
              </p>
            </div>
          </div>

          <button
            onClick={() => setIsOnboarded(false)}
            className="text-xs text-rose-600 hover:underline font-semibold"
          >
            Volver a pantalla inicial
          </button>
        </div>

        <form onSubmit={handleSaveProfile} className="space-y-3 text-xs">
          <div>
            <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1">
              Nombre de la Menor
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              className="w-full rounded-2xl border border-slate-300 p-2.5 text-xs outline-none focus:border-rose-500"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1">
                Fecha de Nacimiento
              </label>
              <input
                type="date"
                value={birthDate}
                onChange={e => setBirthDate(e.target.value)}
                className="w-full rounded-2xl border border-slate-300 p-2 text-xs outline-none"
                required
              />
            </div>
            <div>
              <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1">
                Grupo Sanguíneo
              </label>
              <input
                type="text"
                value={bloodType}
                onChange={e => setBloodType(e.target.value)}
                className="w-full rounded-2xl border border-slate-300 p-2 text-xs outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1">
              Alergias Conocidas
            </label>
            <input
              type="text"
              value={allergies}
              onChange={e => setAllergies(e.target.value)}
              placeholder="Ej. Penicilina, picadura de avispa..."
              className="w-full rounded-2xl border border-slate-300 p-2.5 text-xs outline-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1">
                Pediatra de Cabecera
              </label>
              <input
                type="text"
                value={pediatricianName}
                onChange={e => setPediatricianName(e.target.value)}
                className="w-full rounded-2xl border border-slate-300 p-2 text-xs outline-none"
              />
            </div>
            <div>
              <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1">
                Centro Pediátrico
              </label>
              <input
                type="text"
                value={clinicName}
                onChange={e => setClinicName(e.target.value)}
                className="w-full rounded-2xl border border-slate-300 p-2 text-xs outline-none"
              />
            </div>
          </div>

          <div className="flex items-center justify-between pt-2">
            {profileEditSuccess ? (
              <span className="text-xs text-emerald-700 font-bold flex items-center gap-1">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                ¡Cambios guardados con éxito!
              </span>
            ) : <span></span>}

            <button
              type="submit"
              className="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl shadow-xs transition"
            >
              Guardar Perfil
            </button>
          </div>
        </form>
      </div>

      {/* 3. PREFERENCES & DEVICE FRAME */}
      <div className="bg-white rounded-3xl p-5 border border-rose-100 shadow-2xs space-y-3">
        <h3 className="text-sm font-bold text-slate-900">
          Preferencias de Visualización y Sistema
        </h3>

        <div className="flex items-center justify-between py-2 border-b border-slate-100 text-xs">
          <div>
            <span className="font-bold text-slate-800 block">Instalar en tu Teléfono Móvil</span>
            <span className="text-slate-500">Ver código QR e instrucciones para Android e iPhone</span>
          </div>
          <button
            onClick={() => setShowInstallModal(true)}
            className="px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-rose-500 to-amber-500 hover:from-rose-600 hover:to-amber-600 text-white font-bold flex items-center gap-1.5 shadow-xs"
          >
            <QrCode className="w-3.5 h-3.5" />
            <span>Código QR / Guía</span>
          </button>
        </div>

        <div className="flex items-center justify-between py-2 border-b border-slate-100 text-xs">
          <div>
            <span className="font-bold text-slate-800 block">Modo de Pantalla</span>
            <span className="text-slate-500">Alternar marco de teléfono móvil Android o escritorio</span>
          </div>
          <button
            onClick={() => setIsMobileDeviceFrame(!isMobileDeviceFrame)}
            className="px-3 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold flex items-center gap-1.5"
          >
            {isMobileDeviceFrame ? <Maximize2 className="w-3.5 h-3.5" /> : <Smartphone className="w-3.5 h-3.5 text-indigo-600" />}
            <span>{isMobileDeviceFrame ? 'Ver Pantalla Completa' : 'Ver Marco Móvil'}</span>
          </button>
        </div>

        <div className="flex items-center justify-between py-2 border-b border-slate-100 text-xs">
          <div>
            <span className="font-bold text-slate-800 block">Notificaciones del Sistema</span>
            <span className="text-slate-500">Alertas de medicamentos, visitas y fiebre en tu dispositivo</span>
          </div>
          <button
            onClick={requestWebNotificationPermission}
            className="px-3 py-1.5 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold flex items-center gap-1.5"
          >
            <Bell className="w-3.5 h-3.5" />
            <span>Activar Alertas</span>
          </button>
        </div>

        <div className="flex items-center justify-between py-2 text-xs">
          <div>
            <span className="font-bold text-slate-800 block">Restablecer Datos de Demostración</span>
            <span className="text-slate-500">Carga nuevamente los datos iniciales de prueba</span>
          </div>
          <button
            onClick={() => {
              if (window.confirm('¿Deseas restablecer los datos de demostración?')) {
                resetToSampleData();
              }
            }}
            className="px-3 py-1.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-600 font-semibold"
          >
            Restablecer
          </button>
        </div>
      </div>

      {/* Confirmation Modal for Google Drive Restore (Required Destructive Operation Confirmation) */}
      {confirmRestoreOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
          <div className="w-full max-w-sm rounded-3xl bg-white p-5 shadow-2xl space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-amber-100 text-amber-700 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-slate-900 text-center">
              ¿Restaurar copia desde Google Drive?
            </h3>
            <p className="text-xs text-slate-600 leading-relaxed text-center">
              Esta acción reemplazará los registros actuales de tu dispositivo con la versión guardada en tu Google Drive. ¿Deseas continuar?
            </p>
            <div className="flex items-center gap-2 pt-2">
              <button
                onClick={() => setConfirmRestoreOpen(false)}
                className="flex-1 py-2.5 rounded-xl border border-slate-200 text-slate-700 font-semibold text-xs hover:bg-slate-50"
              >
                Cancelar
              </button>
              <button
                onClick={handleConfirmRestore}
                className="flex-1 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-xs"
              >
                Sí, Restaurar
              </button>
            </div>
          </div>
        </div>
      )}
      {/* Install on Mobile Modal */}
      {showInstallModal && (
        <InstallModal onClose={() => setShowInstallModal(false)} />
      )}
    </div>
  );
};
