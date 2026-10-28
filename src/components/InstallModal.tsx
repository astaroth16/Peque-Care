import React, { useState, useEffect } from 'react';
import QRCode from 'qrcode';
import {
  Smartphone,
  QrCode,
  Copy,
  Check,
  X,
  Sparkles,
  ArrowRight,
  ExternalLink,
  Share2,
  PackageCheck,
  Download,
  ShieldCheck,
  Info
} from 'lucide-react';

interface InstallModalProps {
  onClose: () => void;
}

export const InstallModal: React.FC<InstallModalProps> = ({ onClose }) => {
  const [qrDataUrl, setQrDataUrl] = useState<string>('');
  const [copied, setCopied] = useState(false);
  const [platformTab, setPlatformTab] = useState<'android' | 'apk' | 'ios'>('android');

  const appUrl =
    typeof window !== 'undefined'
      ? window.location.origin
      : 'https://ais-pre-w2mgju7clqhoze76vaij3r-343223524806.us-east5.run.app';

  const pwaBuilderUrl = `https://www.pwabuilder.com?url=${encodeURIComponent(appUrl)}`;

  useEffect(() => {
    QRCode.toDataURL(appUrl, {
      width: 240,
      margin: 1.5,
      color: {
        dark: '#1e1b4b',
        light: '#ffffff',
      },
    })
      .then(url => setQrDataUrl(url))
      .catch(err => console.error(err));
  }, [appUrl]);

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(appUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    } catch {
      // Fallback
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="w-full max-w-lg rounded-3xl bg-white shadow-2xl overflow-hidden border border-rose-100 flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="bg-gradient-to-r from-rose-500 via-rose-600 to-amber-500 p-5 text-white flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-white/20 border border-white/40 flex items-center justify-center text-xl shadow-inner">
              📱
            </div>
            <div>
              <h3 className="font-black text-base tracking-tight">
                Instalar PequeCare en tu Móvil
              </h3>
              <p className="text-xs text-rose-100">
                Opciones de instalación directa o paquete APK
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-white/80 hover:text-white hover:bg-white/10 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-5 overflow-y-auto space-y-4 text-xs text-slate-700">
          {/* QR Code Section */}
          <div className="bg-gradient-to-br from-rose-50/60 to-amber-50/60 rounded-2xl p-4 border border-rose-100/80 flex flex-col sm:flex-row items-center gap-4 text-center sm:text-left">
            <div className="bg-white p-2.5 rounded-2xl border border-rose-200/80 shadow-xs shrink-0">
              {qrDataUrl ? (
                <img
                  src={qrDataUrl}
                  alt="Código QR para abrir PequeCare en el móvil"
                  className="w-28 h-28 object-contain rounded-xl"
                />
              ) : (
                <div className="w-28 h-28 flex items-center justify-center text-slate-400">
                  <QrCode className="w-10 h-10 animate-pulse" />
                </div>
              )}
            </div>

            <div className="flex-1 space-y-2">
              <span className="inline-flex items-center gap-1 text-[11px] font-extrabold uppercase tracking-wider text-rose-700 bg-rose-100/80 px-2.5 py-0.5 rounded-full">
                <Sparkles className="w-3 h-3 text-rose-600" />
                Paso más rápido
              </span>
              <h4 className="text-sm font-bold text-slate-900">
                Apunta la cámara de tu teléfono a este código QR
              </h4>
              <p className="text-slate-600 text-[11px] leading-relaxed">
                Escanea el código con la cámara de tu móvil para abrir PequeCare directamente en el navegador de tu teléfono.
              </p>

              {/* Copy URL input */}
              <div className="flex items-center gap-1.5 pt-1">
                <input
                  type="text"
                  readOnly
                  value={appUrl}
                  className="flex-1 rounded-xl border border-slate-300 px-2.5 py-1.5 text-[11px] bg-white text-slate-600 select-all outline-none"
                />
                <button
                  type="button"
                  onClick={handleCopyLink}
                  className={`px-3 py-1.5 rounded-xl font-bold text-xs transition flex items-center gap-1 shrink-0 ${
                    copied
                      ? 'bg-emerald-600 text-white'
                      : 'bg-rose-600 hover:bg-rose-700 text-white shadow-xs'
                  }`}
                >
                  {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copied ? '¡Copiado!' : 'Copiar'}</span>
                </button>
              </div>
            </div>
          </div>

          {/* Platform Tab Selector */}
          <div>
            <div className="grid grid-cols-3 gap-1 p-1 bg-slate-100 rounded-2xl mb-3">
              <button
                type="button"
                onClick={() => setPlatformTab('android')}
                className={`py-2 px-1 text-xs font-bold rounded-xl transition flex items-center justify-center gap-1 ${
                  platformTab === 'android'
                    ? 'bg-white text-rose-600 shadow-2xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <span>🤖 Android (Directo)</span>
              </button>

              <button
                type="button"
                onClick={() => setPlatformTab('apk')}
                className={`py-2 px-1 text-xs font-bold rounded-xl transition flex items-center justify-center gap-1 ${
                  platformTab === 'apk'
                    ? 'bg-white text-indigo-600 shadow-2xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <span>📦 Generar APK</span>
              </button>

              <button
                type="button"
                onClick={() => setPlatformTab('ios')}
                className={`py-2 px-1 text-xs font-bold rounded-xl transition flex items-center justify-center gap-1 ${
                  platformTab === 'ios'
                    ? 'bg-white text-rose-600 shadow-2xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <span>🍎 iPhone/iPad</span>
              </button>
            </div>

            {/* TAB 1: Android PWA (WebAPK automático) */}
            {platformTab === 'android' && (
              <div className="bg-slate-50 rounded-2xl p-4 border border-slate-200 space-y-3 animate-in fade-in duration-150">
                <div className="p-2.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-900 flex items-start gap-2">
                  <ShieldCheck className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                  <p className="text-[11px] leading-relaxed">
                    <strong>¡El método oficial y más seguro!</strong> Cuando instalas desde Google Chrome en Android, el sistema genera automáticamente un <strong>WebAPK nativo firmado</strong>. No necesitas descargar archivos APK sueltos ni desactivar protecciones de seguridad.
                  </p>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-full bg-rose-600 text-white font-extrabold flex items-center justify-center text-xs shrink-0">
                    1
                  </div>
                  <div>
                    <h5 className="font-bold text-slate-900 text-xs">
                      Abre el enlace en Google Chrome en tu móvil
                    </h5>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      Usa el enlace copiado o escanea el código QR de arriba.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-full bg-rose-600 text-white font-extrabold flex items-center justify-center text-xs shrink-0">
                    2
                  </div>
                  <div>
                    <h5 className="font-bold text-slate-900 text-xs">
                      Pulsa "Instalar aplicación"
                    </h5>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      Toca el botón flotante en la barra superior o los <strong>3 puntos verticales (⋮)</strong> de Chrome &gt; <strong>"Instalar aplicación"</strong>.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-full bg-rose-600 text-white font-extrabold flex items-center justify-center text-xs shrink-0">
                    3
                  </div>
                  <div>
                    <h5 className="font-bold text-slate-900 text-xs">
                      ¡Listo! Funciona como cualquier app de Play Store
                    </h5>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      Aparecerá en tu lista de aplicaciones, se abrirá a pantalla completa y admitirá notificaciones y Google Drive.
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* TAB 2: Generar APK (.apk descargable) */}
            {platformTab === 'apk' && (
              <div className="bg-slate-50 rounded-2xl p-4 border border-slate-200 space-y-3.5 animate-in fade-in duration-150">
                <div className="p-3 rounded-xl bg-indigo-50 border border-indigo-200 text-indigo-950 flex items-start gap-2.5">
                  <PackageCheck className="w-5 h-5 text-indigo-600 shrink-0 mt-0.5" />
                  <div>
                    <h5 className="font-bold text-xs">¿Cómo crear el archivo .APK instalable?</h5>
                    <p className="text-[11px] text-slate-600 mt-0.5 leading-relaxed">
                      La app ya cuenta con todos los estándares técnicos oficiales (Service Worker, Manifest y metadatos). Puedes empaquetarla en un instalador <strong>.APK</strong> o <strong>.AAB</strong> usando <strong>PWABuilder</strong> (herramienta gratuita de Microsoft y Google).
                    </p>
                  </div>
                </div>

                <div className="space-y-2">
                  <h6 className="font-bold text-slate-900 text-xs">Pasos para generar tu APK:</h6>
                  <ol className="list-decimal pl-4 space-y-1.5 text-[11px] text-slate-600">
                    <li>
                      Pulsa el botón de abajo para abrir la herramienta <strong>PWABuilder</strong> con la dirección de esta app ya configurada.
                    </li>
                    <li>
                      En la sección <strong>"Android"</strong>, pulsa <strong>"Package for Stores"</strong> o <strong>"Generate APK"</strong>.
                    </li>
                    <li>
                      Descarga el archivo <code>PequeCare.apk</code> resultante a tu móvil o computadora.
                    </li>
                    <li>
                      Al abrir el <code>.apk</code> en tu Android, pulsa <em>"Instalar"</em> (si te lo solicita, autoriza la instalación de apps desde el navegador o explorador de archivos).
                    </li>
                  </ol>
                </div>

                <a
                  href={pwaBuilderUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-full py-3 px-4 rounded-2xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-xs transition flex items-center justify-center gap-2"
                >
                  <Download className="w-4 h-4" />
                  <span>Abrir Generador de APK (PWABuilder)</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>

                <p className="text-[10px] text-slate-400 text-center italic">
                  Tip: Si prefieres no descargar archivos externos, la opción 1 (Instalación Directa con Chrome) instala un WebAPK nativo en 5 segundos sin descargas manuales.
                </p>
              </div>
            )}

            {/* TAB 3: iOS Safari */}
            {platformTab === 'ios' && (
              <div className="bg-slate-50 rounded-2xl p-4 border border-slate-200 space-y-3 animate-in fade-in duration-150">
                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-full bg-rose-600 text-white font-extrabold flex items-center justify-center text-xs shrink-0">
                    1
                  </div>
                  <div>
                    <h5 className="font-bold text-slate-900 text-xs">
                      Abre el enlace en Safari
                    </h5>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      En dispositivos Apple, la instalación requiere el navegador <strong>Safari</strong>.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-full bg-rose-600 text-white font-extrabold flex items-center justify-center text-xs shrink-0">
                    2
                  </div>
                  <div>
                    <h5 className="font-bold text-slate-900 text-xs flex items-center gap-1.5">
                      <Share2 className="w-3.5 h-3.5 text-blue-600" />
                      Presiona el botón "Compartir"
                    </h5>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      Es el icono de un cuadro con flecha hacia arriba en la barra inferior.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-6 h-6 rounded-full bg-rose-600 text-white font-extrabold flex items-center justify-center text-xs shrink-0">
                    3
                  </div>
                  <div>
                    <h5 className="font-bold text-slate-900 text-xs">
                      Toca "Agregar a la pantalla de inicio"
                    </h5>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      Baja en el menú y selecciona <strong>"Agregar al inicio"</strong> y confirma pulsando "Agregar".
                    </p>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-end">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2.5 rounded-2xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs shadow-xs transition"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
};
