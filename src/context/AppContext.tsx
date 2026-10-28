import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import confetti from 'canvas-confetti';
import {
  ChildProfile,
  GoogleSyncState,
  GrowthRecord,
  SleepRecord,
  MealRecord,
  ActivityRecord,
  Medication,
  TemperatureRecord,
  MedicalVisit,
  MedicalStudy,
  VaccineItem,
  MilestoneItem,
  InAppNotification,
} from '../types';
import {
  INITIAL_CHILD_PROFILE,
  INITIAL_GROWTH_RECORDS,
  INITIAL_MEDICATIONS,
  INITIAL_TEMPERATURE_LOG,
  INITIAL_MEDICAL_VISITS,
  INITIAL_MEDICAL_STUDIES,
  INITIAL_VACCINES,
  INITIAL_SLEEP_RECORDS,
  INITIAL_MEAL_RECORDS,
  INITIAL_ACTIVITY_RECORDS,
  MILESTONES_CATALOG,
} from '../data/pediatricKnowledge';
import {
  calculateAgeMonths,
  calculateBMI,
  evaluateGrowthMetric
} from '../utils/whoStandards';
import {
  initAuth,
  googleSignIn,
  logoutGoogle,
  getAccessToken
} from '../services/authService';
import {
  syncDataToDrive,
  findDriveBackup,
  loadDataFromDrive
} from '../services/googleDriveService';

export type ActiveTabType = 'resumen' | 'crecimiento' | 'salud' | 'rutina' | 'sugerencias' | 'informe' | 'configuracion';

interface AppContextType {
  // Onboarding
  isOnboarded: boolean;
  setIsOnboarded: (val: boolean) => void;

  // Child Profile
  profile: ChildProfile;
  updateProfile: (profile: Partial<ChildProfile>) => void;
  currentAgeMonths: number;

  // Growth & WHO
  growthRecords: GrowthRecord[];
  addGrowthRecord: (record: Omit<GrowthRecord, 'id' | 'ageMonths' | 'bmi' | 'weightPercentile' | 'heightPercentile' | 'bmiPercentile' | 'weightZScore' | 'heightZScore' | 'bmiZScore' | 'weightStatus' | 'heightStatus' | 'bmiStatus'>) => void;
  deleteGrowthRecord: (id: string) => void;
  deleteGrowthRecords: (ids: string[]) => void;
  latestGrowth: GrowthRecord | undefined;

  // Sleep
  sleepRecords: SleepRecord[];
  addSleepRecord: (record: Omit<SleepRecord, 'id'>) => void;
  deleteSleepRecord: (id: string) => void;
  deleteSleepRecords: (ids: string[]) => void;
  todaySleepMinutes: number;

  // Meals & Water
  mealRecords: MealRecord[];
  addMealRecord: (record: Omit<MealRecord, 'id'>) => void;
  deleteMealRecord: (id: string) => void;
  deleteMealRecords: (ids: string[]) => void;
  todayWaterGlasses: number;
  incrementWater: () => void;
  decrementWater: () => void;

  // Activities
  activityRecords: ActivityRecord[];
  addActivityRecord: (record: Omit<ActivityRecord, 'id'>) => void;
  deleteActivityRecord: (id: string) => void;
  deleteActivityRecords: (ids: string[]) => void;

  // Medications
  medications: Medication[];
  addMedication: (med: Omit<Medication, 'id' | 'dosesLog'>) => void;
  updateMedication: (id: string, med: Partial<Medication>) => void;
  recordDoseAdministered: (medId: string, notes?: string) => void;
  deleteMedication: (id: string) => void;
  deleteMedications: (ids: string[]) => void;
  activeMedicationsCount: number;

  // Temperature
  temperatureRecords: TemperatureRecord[];
  addTemperatureRecord: (rec: Omit<TemperatureRecord, 'id'>) => void;
  deleteTemperatureRecord: (id: string) => void;
  deleteTemperatureRecords: (ids: string[]) => void;
  latestTemperature: TemperatureRecord | undefined;

  // Visits & Studies
  medicalVisits: MedicalVisit[];
  addMedicalVisit: (visit: Omit<MedicalVisit, 'id'>) => void;
  updateMedicalVisit: (id: string, visit: Partial<MedicalVisit>) => void;
  deleteMedicalVisit: (id: string) => void;
  deleteMedicalVisits: (ids: string[]) => void;

  medicalStudies: MedicalStudy[];
  addMedicalStudy: (study: Omit<MedicalStudy, 'id'>) => void;
  updateMedicalStudy: (id: string, study: Partial<MedicalStudy>) => void;
  deleteMedicalStudy: (id: string) => void;
  deleteMedicalStudies: (ids: string[]) => void;

  vaccines: VaccineItem[];
  toggleVaccineStatus: (id: string) => void;

  // Milestones
  milestones: MilestoneItem[];
  toggleMilestone: (id: string) => void;

  // Notifications
  notifications: InAppNotification[];
  dismissNotification: (id: string) => void;
  pushNotification: (notification: Omit<InAppNotification, 'id' | 'timestamp' | 'isRead'>) => void;
  requestWebNotificationPermission: () => Promise<boolean>;

  // Google Drive & Cloud Sync
  googleSync: GoogleSyncState;
  signInWithGoogle: () => Promise<void>;
  signOutGoogle: () => Promise<void>;
  triggerGoogleDriveSync: () => Promise<void>;
  restoreFromGoogleDrive: () => Promise<void>;
  toggleAutoSync: () => void;

  // View presentation state
  activeTab: ActiveTabType;
  setActiveTab: (tab: ActiveTabType) => void;
  isMobileDeviceFrame: boolean;
  setIsMobileDeviceFrame: (val: boolean | ((prev: boolean) => boolean)) => void;

  // Quick Action Modal states
  quickActionOpen: boolean;
  setQuickActionOpen: (open: boolean) => void;
  activeModal: 'growth' | 'medication' | 'temperature' | 'sleep' | 'meal' | 'activity' | 'visit' | null;
  setActiveModal: (modal: 'growth' | 'medication' | 'temperature' | 'sleep' | 'meal' | 'activity' | 'visit' | null) => void;

  // Data management
  resetToSampleData: () => void;
  exportJSON: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

const STORAGE_KEYS = {
  ONBOARDED: 'pequecare_onboarded_v2',
  PROFILE: 'pequecare_profile_v2',
  GROWTH: 'pequecare_growth_v2',
  SLEEP: 'pequecare_sleep_v2',
  MEALS: 'pequecare_meals_v2',
  ACTIVITIES: 'pequecare_activities_v2',
  MEDS: 'pequecare_meds_v2',
  TEMP: 'pequecare_temp_v2',
  VISITS: 'pequecare_visits_v2',
  STUDIES: 'pequecare_studies_v2',
  VACCINES: 'pequecare_vaccines_v2',
  MILESTONES: 'pequecare_milestones_v2',
  NOTIFS: 'pequecare_notifs_v2',
  FRAME: 'pequecare_mobile_frame_v2',
  SYNC_SETTINGS: 'pequecare_sync_settings_v2',
};

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Onboarding state: defaults to false so user is immediately presented with registration screen
  const [isOnboarded, setIsOnboardedState] = useState<boolean>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.ONBOARDED);
    return saved === 'true';
  });

  const setIsOnboarded = (val: boolean) => {
    setIsOnboardedState(val);
    localStorage.setItem(STORAGE_KEYS.ONBOARDED, val ? 'true' : 'false');
  };

  // Profile
  const [profile, setProfile] = useState<ChildProfile>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.PROFILE);
    return saved ? JSON.parse(saved) : INITIAL_CHILD_PROFILE;
  });

  // Growth
  const [growthRecords, setGrowthRecords] = useState<GrowthRecord[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.GROWTH);
    return saved ? JSON.parse(saved) : INITIAL_GROWTH_RECORDS;
  });

  // Sleep
  const [sleepRecords, setSleepRecords] = useState<SleepRecord[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.SLEEP);
    return saved ? JSON.parse(saved) : INITIAL_SLEEP_RECORDS;
  });

  // Meals
  const [mealRecords, setMealRecords] = useState<MealRecord[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.MEALS);
    return saved ? JSON.parse(saved) : INITIAL_MEAL_RECORDS;
  });

  // Activities
  const [activityRecords, setActivityRecords] = useState<ActivityRecord[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.ACTIVITIES);
    return saved ? JSON.parse(saved) : INITIAL_ACTIVITY_RECORDS;
  });

  // Medications
  const [medications, setMedications] = useState<Medication[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.MEDS);
    return saved ? JSON.parse(saved) : INITIAL_MEDICATIONS;
  });

  // Temperature
  const [temperatureRecords, setTemperatureRecords] = useState<TemperatureRecord[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.TEMP);
    return saved ? JSON.parse(saved) : INITIAL_TEMPERATURE_LOG;
  });

  // Visits
  const [medicalVisits, setMedicalVisits] = useState<MedicalVisit[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.VISITS);
    return saved ? JSON.parse(saved) : INITIAL_MEDICAL_VISITS;
  });

  // Studies
  const [medicalStudies, setMedicalStudies] = useState<MedicalStudy[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.STUDIES);
    return saved ? JSON.parse(saved) : INITIAL_MEDICAL_STUDIES;
  });

  // Vaccines
  const [vaccines, setVaccines] = useState<VaccineItem[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.VACCINES);
    return saved ? JSON.parse(saved) : INITIAL_VACCINES;
  });

  // Milestones
  const [milestones, setMilestones] = useState<MilestoneItem[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.MILESTONES);
    return saved ? JSON.parse(saved) : MILESTONES_CATALOG;
  });

  // In-app notifications
  const [notifications, setNotifications] = useState<InAppNotification[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.NOTIFS);
    if (saved) return JSON.parse(saved);
    return [
      {
        id: 'notif-1',
        type: 'medicamento',
        title: 'Próxima dosis programada',
        message: 'Ibuprofeno Suspensión (6.5 ml) a las 14:30 hs.',
        timestamp: new Date().toISOString(),
        isRead: false,
        urgent: true,
      },
      {
        id: 'notif-2',
        type: 'consulta',
        title: 'Turno Médico Próximo',
        message: 'Tamizaje Visual Infantil en Clínica Santa Lucía el 10 de Noviembre.',
        timestamp: new Date().toISOString(),
        isRead: false,
      },
    ];
  });

  // Google Sync State
  const [googleSync, setGoogleSync] = useState<GoogleSyncState>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.SYNC_SETTINGS);
    const parsed = saved ? JSON.parse(saved) : {};
    return {
      isConnected: false,
      userEmail: null,
      userName: null,
      userPhoto: null,
      lastSyncTime: parsed.lastSyncTime || null,
      autoSync: parsed.autoSync !== undefined ? parsed.autoSync : true,
      isSyncing: false,
      syncError: null,
      driveFileId: parsed.driveFileId || null,
    };
  });

  // Navigation & Frame
  const [activeTab, setActiveTab] = useState<ActiveTabType>('resumen');
  const [isMobileDeviceFrame, setIsMobileDeviceFrame] = useState<boolean>(() => {
    const saved = localStorage.getItem(STORAGE_KEYS.FRAME);
    if (saved !== null) return JSON.parse(saved);
    return typeof window !== 'undefined' ? window.innerWidth > 768 : false;
  });

  // Quick Action Modal states
  const [quickActionOpen, setQuickActionOpen] = useState(false);
  const [activeModal, setActiveModal] = useState<'growth' | 'medication' | 'temperature' | 'sleep' | 'meal' | 'activity' | 'visit' | null>(null);

  // Persistence effects
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.PROFILE, JSON.stringify(profile)); }, [profile]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.GROWTH, JSON.stringify(growthRecords)); }, [growthRecords]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.SLEEP, JSON.stringify(sleepRecords)); }, [sleepRecords]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.MEALS, JSON.stringify(mealRecords)); }, [mealRecords]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.ACTIVITIES, JSON.stringify(activityRecords)); }, [activityRecords]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.MEDS, JSON.stringify(medications)); }, [medications]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.TEMP, JSON.stringify(temperatureRecords)); }, [temperatureRecords]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.VISITS, JSON.stringify(medicalVisits)); }, [medicalVisits]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.STUDIES, JSON.stringify(medicalStudies)); }, [medicalStudies]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.VACCINES, JSON.stringify(vaccines)); }, [vaccines]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.MILESTONES, JSON.stringify(milestones)); }, [milestones]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.NOTIFS, JSON.stringify(notifications)); }, [notifications]);
  useEffect(() => { localStorage.setItem(STORAGE_KEYS.FRAME, JSON.stringify(isMobileDeviceFrame)); }, [isMobileDeviceFrame]);
  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.SYNC_SETTINGS, JSON.stringify({
      autoSync: googleSync.autoSync,
      lastSyncTime: googleSync.lastSyncTime,
      driveFileId: googleSync.driveFileId,
    }));
  }, [googleSync.autoSync, googleSync.lastSyncTime, googleSync.driveFileId]);

  // Firebase Auth listener on mount
  useEffect(() => {
    const unsubscribe = initAuth(
      (user, token) => {
        setGoogleSync(prev => ({
          ...prev,
          isConnected: true,
          userEmail: user.email,
          userName: user.displayName,
          userPhoto: user.photoURL,
          syncError: null,
        }));
      },
      () => {
        setGoogleSync(prev => ({
          ...prev,
          isConnected: false,
          userEmail: null,
          userName: null,
          userPhoto: null,
        }));
      }
    );
    return () => unsubscribe();
  }, []);

  // Derived current age
  const todayStr = new Date().toISOString().split('T')[0];
  const currentAgeMonths = calculateAgeMonths(profile.birthDate, todayStr);

  // Latest records
  const latestGrowth = growthRecords.length > 0
    ? [...growthRecords].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())[0]
    : undefined;

  const latestTemperature = temperatureRecords.length > 0
    ? [...temperatureRecords].sort((a, b) => new Date(`${b.date}T${b.time}`).getTime() - new Date(`${a.date}T${a.time}`).getTime())[0]
    : undefined;

  // Today's sleep calculation
  const todaySleepMinutes = sleepRecords
    .filter(r => r.date === todayStr)
    .reduce((sum, r) => sum + r.durationMinutes, 0);

  // Today's water calculation
  const todayMeals = mealRecords.filter(r => r.date === todayStr);
  const todayWaterGlasses = todayMeals.reduce((sum, r) => sum + (r.waterGlasses || 0), 0);

  const incrementWater = () => {
    const existing = mealRecords.find(r => r.date === todayStr);
    if (existing) {
      setMealRecords(prev => prev.map(m => m.id === existing.id ? { ...m, waterGlasses: (m.waterGlasses || 0) + 1 } : m));
    } else {
      const now = new Date();
      const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
      const newMeal: MealRecord = {
        id: `meal-${Date.now()}`,
        date: todayStr,
        time: timeStr,
        mealType: 'colación',
        foods: ['Vaso de agua fresca (200 ml)'],
        waterGlasses: 1,
        appetite: 'bueno',
      };
      setMealRecords(prev => [newMeal, ...prev]);
    }
  };

  const decrementWater = () => {
    const existing = mealRecords.find(r => r.date === todayStr && (r.waterGlasses || 0) > 0);
    if (existing) {
      setMealRecords(prev => prev.map(m => m.id === existing.id ? { ...m, waterGlasses: Math.max(0, m.waterGlasses - 1) } : m));
    }
  };

  // Profile update
  const updateProfile = (data: Partial<ChildProfile>) => {
    setProfile(prev => ({ ...prev, ...data }));
  };

  // Add growth record with WHO standard calculations
  const addGrowthRecord = (rec: Omit<GrowthRecord, 'id' | 'ageMonths' | 'bmi' | 'weightPercentile' | 'heightPercentile' | 'bmiPercentile' | 'weightZScore' | 'heightZScore' | 'bmiZScore' | 'weightStatus' | 'heightStatus' | 'bmiStatus'>) => {
    const ageMonths = calculateAgeMonths(profile.birthDate, rec.date);
    const bmi = calculateBMI(rec.weightKg, rec.heightCm);

    const weightEval = evaluateGrowthMetric('weight', ageMonths, rec.weightKg);
    const heightEval = evaluateGrowthMetric('height', ageMonths, rec.heightCm);
    const bmiEval = evaluateGrowthMetric('bmi', ageMonths, bmi);

    const newRecord: GrowthRecord = {
      id: `g-${Date.now()}`,
      ...rec,
      ageMonths,
      bmi,
      weightPercentile: weightEval.percentile,
      heightPercentile: heightEval.percentile,
      bmiPercentile: bmiEval.percentile,
      weightZScore: weightEval.zScore,
      heightZScore: heightEval.zScore,
      bmiZScore: bmiEval.zScore,
      weightStatus: weightEval.status,
      heightStatus: heightEval.status,
      bmiStatus: bmiEval.status,
    };

    setGrowthRecords(prev => [newRecord, ...prev].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()));
    
    pushNotification({
      type: 'actividad',
      title: 'Nuevo registro de crecimiento',
      message: `Peso: ${rec.weightKg} kg (P${weightEval.percentile}), Talla: ${rec.heightCm} cm (P${heightEval.percentile}). Guardado según curvas OMS.`,
    });
  };

  const deleteGrowthRecord = (id: string) => {
    setGrowthRecords(prev => prev.filter(r => r.id !== id));
  };

  const deleteGrowthRecords = (ids: string[]) => {
    setGrowthRecords(prev => prev.filter(r => !ids.includes(r.id)));
  };

  // Sleep methods
  const addSleepRecord = (record: Omit<SleepRecord, 'id'>) => {
    const newRecord: SleepRecord = {
      id: `slp-${Date.now()}`,
      ...record,
    };
    setSleepRecords(prev => [newRecord, ...prev]);
  };

  const deleteSleepRecord = (id: string) => {
    setSleepRecords(prev => prev.filter(r => r.id !== id));
  };

  const deleteSleepRecords = (ids: string[]) => {
    setSleepRecords(prev => prev.filter(r => !ids.includes(r.id)));
  };

  // Meal methods
  const addMealRecord = (record: Omit<MealRecord, 'id'>) => {
    const newRecord: MealRecord = {
      id: `meal-${Date.now()}`,
      ...record,
    };
    setMealRecords(prev => [newRecord, ...prev]);
  };

  const deleteMealRecord = (id: string) => {
    setMealRecords(prev => prev.filter(r => r.id !== id));
  };

  const deleteMealRecords = (ids: string[]) => {
    setMealRecords(prev => prev.filter(r => !ids.includes(r.id)));
  };

  // Activity methods
  const addActivityRecord = (record: Omit<ActivityRecord, 'id'>) => {
    const newRecord: ActivityRecord = {
      id: `act-${Date.now()}`,
      ...record,
    };
    setActivityRecords(prev => [newRecord, ...prev]);
  };

  const deleteActivityRecord = (id: string) => {
    setActivityRecords(prev => prev.filter(r => r.id !== id));
  };

  const deleteActivityRecords = (ids: string[]) => {
    setActivityRecords(prev => prev.filter(r => !ids.includes(r.id)));
  };

  // Medications methods
  const addMedication = (med: Omit<Medication, 'id' | 'dosesLog'>) => {
    const newMed: Medication = {
      id: `med-${Date.now()}`,
      ...med,
      dosesLog: [],
    };
    setMedications(prev => [newMed, ...prev]);
    pushNotification({
      type: 'medicamento',
      title: 'Nuevo tratamiento registrado',
      message: `${newMed.name} (${newMed.dosage}) cada ${newMed.frequencyHours} horas.`,
    });
  };

  const updateMedication = (id: string, update: Partial<Medication>) => {
    setMedications(prev => prev.map(m => m.id === id ? { ...m, ...update } : m));
  };

  const recordDoseAdministered = (medId: string, notes?: string) => {
    const med = medications.find(m => m.id === medId);
    if (!med) return;

    const now = new Date();
    const newLog = {
      id: `dose-${Date.now()}`,
      timestamp: now.toISOString(),
      dosageGiven: med.dosage,
      status: 'administrado' as const,
      notes,
    };

    const nextDose = new Date(now.getTime() + med.frequencyHours * 60 * 60 * 1000);
    const nextDoseIso = nextDose.toISOString().slice(0, 16);

    setMedications(prev => prev.map(m => {
      if (m.id === medId) {
        return {
          ...m,
          nextDoseTime: nextDoseIso,
          dosesLog: [newLog, ...m.dosesLog],
        };
      }
      return m;
    }));

    pushNotification({
      type: 'medicamento',
      title: `Dosis administrada: ${med.name}`,
      message: `${med.dosage} registrado con éxito. Próxima dosis en ${med.frequencyHours} horas.`,
    });
  };

  const deleteMedication = (id: string) => {
    setMedications(prev => prev.filter(m => m.id !== id));
  };

  const deleteMedications = (ids: string[]) => {
    setMedications(prev => prev.filter(m => !ids.includes(m.id)));
  };

  const activeMedicationsCount = medications.filter(m => m.active).length;

  // Temperature
  const addTemperatureRecord = (rec: Omit<TemperatureRecord, 'id'>) => {
    const newRecord: TemperatureRecord = {
      id: `temp-${Date.now()}`,
      ...rec,
    };
    setTemperatureRecords(prev => [newRecord, ...prev]);
    if (rec.celsius >= 38.0) {
      pushNotification({
        type: 'medicamento',
        title: 'Alerta de Fiebre detectada',
        message: `${rec.celsius}°C registrado para ${profile.name}. Revisa antitérmico y medidas físicas.`,
        urgent: true,
      });
    }
  };

  const deleteTemperatureRecord = (id: string) => {
    setTemperatureRecords(prev => prev.filter(r => r.id !== id));
  };

  const deleteTemperatureRecords = (ids: string[]) => {
    setTemperatureRecords(prev => prev.filter(r => !ids.includes(r.id)));
  };

  // Visits
  const addMedicalVisit = (visit: Omit<MedicalVisit, 'id'>) => {
    const newVisit: MedicalVisit = {
      id: `vis-${Date.now()}`,
      ...visit,
    };
    setMedicalVisits(prev => [newVisit, ...prev]);
  };

  const updateMedicalVisit = (id: string, visit: Partial<MedicalVisit>) => {
    setMedicalVisits(prev => prev.map(v => v.id === id ? { ...v, ...visit } : v));
  };

  const deleteMedicalVisit = (id: string) => {
    setMedicalVisits(prev => prev.filter(v => v.id !== id));
  };

  const deleteMedicalVisits = (ids: string[]) => {
    setMedicalVisits(prev => prev.filter(v => !ids.includes(v.id)));
  };

  // Studies
  const addMedicalStudy = (study: Omit<MedicalStudy, 'id'>) => {
    const newStudy: MedicalStudy = {
      id: `stu-${Date.now()}`,
      ...study,
    };
    setMedicalStudies(prev => [newStudy, ...prev]);
  };

  const updateMedicalStudy = (id: string, study: Partial<MedicalStudy>) => {
    setMedicalStudies(prev => prev.map(s => s.id === id ? { ...s, ...study } : s));
  };

  const deleteMedicalStudy = (id: string) => {
    setMedicalStudies(prev => prev.filter(s => s.id !== id));
  };

  const deleteMedicalStudies = (ids: string[]) => {
    setMedicalStudies(prev => prev.filter(s => !ids.includes(s.id)));
  };

  // Vaccines
  const toggleVaccineStatus = (id: string) => {
    setVaccines(prev => prev.map(v => {
      if (v.id === id) {
        const nextStatus = v.status === 'aplicada' ? 'pendiente' : 'aplicada';
        return {
          ...v,
          status: nextStatus,
          administeredDate: nextStatus === 'aplicada' ? new Date().toISOString().split('T')[0] : undefined,
        };
      }
      return v;
    }));
  };

  // Milestones
  const toggleMilestone = (id: string) => {
    setMilestones(prev => prev.map(m => {
      if (m.id === id) {
        const nextState = !m.achieved;
        if (nextState) {
          try {
            confetti({
              particleCount: 60,
              spread: 70,
              origin: { y: 0.6 },
              colors: ['#F43F5E', '#06B6D4', '#10B981', '#F59E0B', '#6366F1'],
            });
          } catch {
            // ignore
          }
        }
        return {
          ...m,
          achieved: nextState,
          achievedDate: nextState ? new Date().toISOString().split('T')[0] : undefined,
        };
      }
      return m;
    }));
  };

  // Notifications
  const dismissNotification = (id: string) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  const pushNotification = (notif: Omit<InAppNotification, 'id' | 'timestamp' | 'isRead'>) => {
    const newNotif: InAppNotification = {
      id: `notif-${Date.now()}`,
      ...notif,
      timestamp: new Date().toISOString(),
      isRead: false,
    };
    setNotifications(prev => [newNotif, ...prev.slice(0, 9)]);

    if (typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'granted') {
      try {
        new Notification(notif.title, {
          body: notif.message,
          icon: '/icon.svg',
        });
      } catch {
        // Fallback
      }
    }
  };

  const requestWebNotificationPermission = async (): Promise<boolean> => {
    if (typeof window === 'undefined' || !('Notification' in window)) return false;
    try {
      const permission = await Notification.requestPermission();
      return permission === 'granted';
    } catch {
      return false;
    }
  };

  // GOOGLE DRIVE SYNC & AUTHENTICATION
  const signInWithGoogle = async () => {
    try {
      setGoogleSync(prev => ({ ...prev, isSyncing: true, syncError: null }));
      const result = await googleSignIn();
      if (result) {
        setGoogleSync(prev => ({
          ...prev,
          isConnected: true,
          userEmail: result.user.email,
          userName: result.user.displayName,
          userPhoto: result.user.photoURL,
          isSyncing: false,
        }));
        pushNotification({
          type: 'actividad',
          title: 'Google Drive Conectado',
          message: `Sesión iniciada como ${result.user.email}. Tu respaldo en la nube está activo.`,
        });
        // Initial auto-sync after sign in
        setTimeout(() => triggerGoogleDriveSync(), 1000);
      }
    } catch (err: any) {
      setGoogleSync(prev => ({
        ...prev,
        isSyncing: false,
        syncError: err?.message || 'Error al iniciar sesión con Google',
      }));
    }
  };

  const signOutGoogle = async () => {
    try {
      await logoutGoogle();
      setGoogleSync(prev => ({
        ...prev,
        isConnected: false,
        userEmail: null,
        userName: null,
        userPhoto: null,
      }));
    } catch (err) {
      console.error(err);
    }
  };

  const triggerGoogleDriveSync = async () => {
    const token = await getAccessToken();
    if (!token) {
      setGoogleSync(prev => ({ ...prev, syncError: 'Debes iniciar sesión con Google para sincronizar' }));
      return;
    }

    try {
      setGoogleSync(prev => ({ ...prev, isSyncing: true, syncError: null }));
      const fullData = {
        profile,
        growthRecords,
        sleepRecords,
        mealRecords,
        activityRecords,
        medications,
        temperatureRecords,
        medicalVisits,
        medicalStudies,
        vaccines,
        milestones,
        exportedAt: new Date().toISOString(),
      };

      const result = await syncDataToDrive(token, profile.name, fullData);

      setGoogleSync(prev => ({
        ...prev,
        isSyncing: false,
        lastSyncTime: new Date().toISOString(),
        driveFileId: result.id,
        syncError: null,
      }));

      pushNotification({
        type: 'actividad',
        title: 'Copia Guardada en Google Drive',
        message: `Los datos de ${profile.name} se han respaldado exitosamente en tu Google Drive.`,
      });
    } catch (err: any) {
      setGoogleSync(prev => ({
        ...prev,
        isSyncing: false,
        syncError: err?.message || 'Error al sincronizar con Google Drive',
      }));
    }
  };

  const restoreFromGoogleDrive = async () => {
    const token = await getAccessToken();
    if (!token) {
      setGoogleSync(prev => ({ ...prev, syncError: 'Inicia sesión con Google para restaurar' }));
      return;
    }

    try {
      setGoogleSync(prev => ({ ...prev, isSyncing: true, syncError: null }));
      const existing = await findDriveBackup(token, profile.name);
      if (!existing) {
        throw new Error('No se encontró ninguna copia previa en Google Drive para este menor.');
      }

      const remoteData = await loadDataFromDrive(token, existing.id);
      if (remoteData) {
        if (remoteData.profile) setProfile(remoteData.profile);
        if (remoteData.growthRecords) setGrowthRecords(remoteData.growthRecords);
        if (remoteData.sleepRecords) setSleepRecords(remoteData.sleepRecords);
        if (remoteData.mealRecords) setMealRecords(remoteData.mealRecords);
        if (remoteData.activityRecords) setActivityRecords(remoteData.activityRecords);
        if (remoteData.medications) setMedications(remoteData.medications);
        if (remoteData.temperatureRecords) setTemperatureRecords(remoteData.temperatureRecords);
        if (remoteData.medicalVisits) setMedicalVisits(remoteData.medicalVisits);
        if (remoteData.medicalStudies) setMedicalStudies(remoteData.medicalStudies);
        if (remoteData.vaccines) setVaccines(remoteData.vaccines);
        if (remoteData.milestones) setMilestones(remoteData.milestones);

        setGoogleSync(prev => ({
          ...prev,
          isSyncing: false,
          lastSyncTime: new Date().toISOString(),
          driveFileId: existing.id,
          syncError: null,
        }));

        pushNotification({
          type: 'actividad',
          title: 'Restauración Completa',
          message: 'Se cargaron exitosamente todos los datos desde Google Drive.',
        });
      }
    } catch (err: any) {
      setGoogleSync(prev => ({
        ...prev,
        isSyncing: false,
        syncError: err?.message || 'Error al restaurar desde Google Drive',
      }));
    }
  };

  const toggleAutoSync = () => {
    setGoogleSync(prev => ({ ...prev, autoSync: !prev.autoSync }));
  };

  // Periodic frequent synchronization (every 15 minutes if autoSync enabled and connected)
  useEffect(() => {
    if (!googleSync.autoSync || !googleSync.isConnected) return;
    const interval = setInterval(() => {
      triggerGoogleDriveSync();
    }, 15 * 60 * 1000);
    return () => clearInterval(interval);
  }, [googleSync.autoSync, googleSync.isConnected, profile, growthRecords, medications]);

  // Reset & Export
  const resetToSampleData = () => {
    setProfile(INITIAL_CHILD_PROFILE);
    setGrowthRecords(INITIAL_GROWTH_RECORDS);
    setMedications(INITIAL_MEDICATIONS);
    setTemperatureRecords(INITIAL_TEMPERATURE_LOG);
    setMedicalVisits(INITIAL_MEDICAL_VISITS);
    setMedicalStudies(INITIAL_MEDICAL_STUDIES);
    setVaccines(INITIAL_VACCINES);
    setSleepRecords(INITIAL_SLEEP_RECORDS);
    setMealRecords(INITIAL_MEAL_RECORDS);
    setActivityRecords(INITIAL_ACTIVITY_RECORDS);
    setMilestones(MILESTONES_CATALOG);
    setIsOnboarded(true);
  };

  const exportJSON = () => {
    const data = {
      profile,
      growthRecords,
      sleepRecords,
      mealRecords,
      activityRecords,
      medications,
      temperatureRecords,
      medicalVisits,
      medicalStudies,
      vaccines,
      milestones,
      exportedAt: new Date().toISOString(),
    };
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `PequeCare_${profile.name.replace(/\s+/g, '_')}_Datos.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <AppContext.Provider
      value={{
        isOnboarded,
        setIsOnboarded,
        profile,
        updateProfile,
        currentAgeMonths,
        growthRecords,
        addGrowthRecord,
        deleteGrowthRecord,
        deleteGrowthRecords,
        latestGrowth,
        sleepRecords,
        addSleepRecord,
        deleteSleepRecord,
        deleteSleepRecords,
        todaySleepMinutes,
        mealRecords,
        addMealRecord,
        deleteMealRecord,
        deleteMealRecords,
        todayWaterGlasses,
        incrementWater,
        decrementWater,
        activityRecords,
        addActivityRecord,
        deleteActivityRecord,
        deleteActivityRecords,
        medications,
        addMedication,
        updateMedication,
        recordDoseAdministered,
        deleteMedication,
        deleteMedications,
        activeMedicationsCount,
        temperatureRecords,
        addTemperatureRecord,
        deleteTemperatureRecord,
        deleteTemperatureRecords,
        latestTemperature,
        medicalVisits,
        addMedicalVisit,
        updateMedicalVisit,
        deleteMedicalVisit,
        deleteMedicalVisits,
        medicalStudies,
        addMedicalStudy,
        updateMedicalStudy,
        deleteMedicalStudy,
        deleteMedicalStudies,
        vaccines,
        toggleVaccineStatus,
        milestones,
        toggleMilestone,
        notifications,
        dismissNotification,
        pushNotification,
        requestWebNotificationPermission,
        googleSync,
        signInWithGoogle,
        signOutGoogle,
        triggerGoogleDriveSync,
        restoreFromGoogleDrive,
        toggleAutoSync,
        activeTab,
        setActiveTab,
        isMobileDeviceFrame,
        setIsMobileDeviceFrame,
        quickActionOpen,
        setQuickActionOpen,
        activeModal,
        setActiveModal,
        resetToSampleData,
        exportJSON,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
