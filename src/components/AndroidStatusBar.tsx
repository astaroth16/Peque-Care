import React, { useState, useEffect } from 'react';
import { Wifi, Signal, Battery, BellRing } from 'lucide-react';
import { useApp } from '../context/AppContext';

export const AndroidStatusBar: React.FC = () => {
  const [time, setTime] = useState('');
  const { notifications } = useApp();
  const unreadCount = notifications.filter(n => !n.isRead).length;

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTime(now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false }));
    };
    updateTime();
    const timer = setInterval(updateTime, 10000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="w-full bg-slate-900/95 text-white px-5 py-2 flex items-center justify-between text-xs font-medium select-none z-30">
      {/* Left side: Time & Notification badge */}
      <div className="flex items-center gap-2">
        <span className="font-semibold tracking-wide">{time || '09:41'}</span>
        {unreadCount > 0 && (
          <span className="flex items-center gap-1 text-[10px] bg-rose-500/80 px-1.5 py-0.5 rounded-full text-white">
            <BellRing className="w-3 h-3" />
            {unreadCount}
          </span>
        )}
      </div>

      {/* Right side: Android system icons */}
      <div className="flex items-center gap-2 text-slate-300">
        <Wifi className="w-3.5 h-3.5" />
        <Signal className="w-3.5 h-3.5" />
        <div className="flex items-center gap-1">
          <span className="text-[11px] font-semibold text-white">88%</span>
          <Battery className="w-4 h-4 fill-white text-white" />
        </div>
      </div>
    </div>
  );
};
