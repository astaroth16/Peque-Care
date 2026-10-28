/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { AndroidFrame } from './components/AndroidFrame';
import { OnboardingScreen } from './components/OnboardingScreen';
import { DashboardTab } from './components/tabs/DashboardTab';
import { GrowthTab } from './components/tabs/GrowthTab';
import { HealthTab } from './components/tabs/HealthTab';
import { RoutineTab } from './components/tabs/RoutineTab';
import { SuggestionsTab } from './components/tabs/SuggestionsTab';
import { ReportTab } from './components/tabs/ReportTab';
import { SettingsTab } from './components/tabs/SettingsTab';

import { GrowthModal } from './components/modals/GrowthModal';
import { MedicationModal } from './components/modals/MedicationModal';
import { TemperatureModal } from './components/modals/TemperatureModal';
import { SleepModal } from './components/modals/SleepModal';
import { MealModal } from './components/modals/MealModal';
import { ActivityModal } from './components/modals/ActivityModal';
import { MedicalVisitModal } from './components/modals/MedicalVisitModal';

function AppContent() {
  const { isOnboarded, setIsOnboarded, activeTab, activeModal, setActiveModal } = useApp();

  return (
    <AndroidFrame>
      {/* If first time or registering child: show Onboarding screen */}
      {!isOnboarded ? (
        <OnboardingScreen onComplete={() => setIsOnboarded(true)} />
      ) : (
        <>
          {/* Active Tab View */}
          {activeTab === 'resumen' && <DashboardTab />}
          {activeTab === 'crecimiento' && <GrowthTab />}
          {activeTab === 'salud' && <HealthTab />}
          {activeTab === 'rutina' && <RoutineTab />}
          {activeTab === 'sugerencias' && <SuggestionsTab />}
          {activeTab === 'informe' && <ReportTab />}
          {activeTab === 'configuracion' && <SettingsTab />}

          {/* Global Modals for Quick Data Registration */}
          {activeModal === 'growth' && (
            <GrowthModal onClose={() => setActiveModal(null)} />
          )}
          {activeModal === 'medication' && (
            <MedicationModal onClose={() => setActiveModal(null)} />
          )}
          {activeModal === 'temperature' && (
            <TemperatureModal onClose={() => setActiveModal(null)} />
          )}
          {activeModal === 'sleep' && (
            <SleepModal onClose={() => setActiveModal(null)} />
          )}
          {activeModal === 'meal' && (
            <MealModal onClose={() => setActiveModal(null)} />
          )}
          {activeModal === 'activity' && (
            <ActivityModal onClose={() => setActiveModal(null)} />
          )}
          {activeModal === 'visit' && (
            <MedicalVisitModal onClose={() => setActiveModal(null)} />
          )}
        </>
      )}
    </AndroidFrame>
  );
}

export default function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}
