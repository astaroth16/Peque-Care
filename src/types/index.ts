export type Gender = 'female' | 'male';

export interface ChildProfile {
  name: string;
  lastName?: string;
  birthDate: string; // YYYY-MM-DD
  gender: Gender;
  bloodType: string;
  allergies: string[];
  pediatricianName: string;
  pediatricianPhone: string;
  clinicName: string;
  insuranceNumber?: string;
  notes?: string;
  avatarUrl?: string;
}

export interface GoogleSyncState {
  isConnected: boolean;
  userEmail: string | null;
  userName: string | null;
  userPhoto: string | null;
  lastSyncTime: string | null;
  autoSync: boolean;
  isSyncing: boolean;
  syncError: string | null;
  driveFileId: string | null;
}

export type GrowthMetricType = 'weight' | 'height' | 'bmi';

export interface GrowthRecord {
  id: string;
  date: string; // YYYY-MM-DD
  ageMonths: number;
  weightKg: number;
  heightCm: number;
  headCircumferenceCm?: number;
  bmi: number;
  weightPercentile: number;
  heightPercentile: number;
  bmiPercentile: number;
  weightZScore: number;
  heightZScore: number;
  bmiZScore: number;
  weightStatus: string;
  heightStatus: string;
  bmiStatus: string;
  notes?: string;
}

export interface SleepRecord {
  id: string;
  date: string; // YYYY-MM-DD
  startTime: string; // HH:mm
  endTime: string; // HH:mm
  durationMinutes: number;
  type: 'noche' | 'siesta';
  quality: 'tranquilo' | 'inquieto' | 'con pesadillas' | 'despertares frecuentes';
  notes?: string;
}

export interface MealRecord {
  id: string;
  date: string; // YYYY-MM-DD
  time: string; // HH:mm
  mealType: 'desayuno' | 'media mañana' | 'almuerzo' | 'merienda' | 'cena' | 'colación';
  foods: string[];
  waterGlasses: number; // 200ml per glass
  appetite: 'excelente' | 'bueno' | 'regular' | 'poco' | 'rechazó';
  notes?: string;
}

export interface ActivityRecord {
  id: string;
  date: string; // YYYY-MM-DD
  time: string; // HH:mm
  title: string;
  category: 'al aire libre' | 'educativa' | 'motricidad' | 'lectura' | 'jardín' | 'pantalla' | 'recreativa';
  durationMinutes: number;
  reminderEnabled: boolean;
  reminderTime?: string; // HH:mm
  notes?: string;
}

export interface DoseLog {
  id: string;
  timestamp: string; // ISO string
  dosageGiven: string;
  status: 'administrado' | 'omitido';
  notes?: string;
}

export interface Medication {
  id: string;
  name: string;
  purpose: string;
  dosage: string; // e.g. "5 ml", "250 mg"
  unit: string;
  frequencyHours: number; // e.g. 8 for every 8 hours
  startDate: string; // YYYY-MM-DD
  endDate: string; // YYYY-MM-DD
  nextDoseTime: string; // YYYY-MM-DDTHH:mm
  active: boolean;
  notes?: string;
  dosesLog: DoseLog[];
}

export interface TemperatureRecord {
  id: string;
  date: string; // YYYY-MM-DD
  time: string; // HH:mm
  celsius: number;
  symptoms: string[];
  medicationGiven?: string;
  notes?: string;
}

export interface MedicalVisit {
  id: string;
  date: string; // YYYY-MM-DD
  time: string; // HH:mm
  doctorName: string;
  specialty: string;
  clinic: string;
  reason: string;
  diagnosis: string;
  indications: string;
  prescriptions: string[];
  studiesRequested: string[];
  nextFollowUpDate?: string;
  status: 'completada' | 'programada';
  notes?: string;
}

export interface MedicalStudy {
  id: string;
  title: string;
  category: 'análisis clínico' | 'visión' | 'audiometría' | 'odontopediatría' | 'vacuna' | 'radiografía' | 'otro';
  prescribedDate: string;
  scheduledDate?: string;
  status: 'pendiente' | 'programado' | 'realizado';
  resultsNotes?: string;
  doctorNotes?: string;
}

export interface VaccineItem {
  id: string;
  name: string;
  diseaseTarget: string;
  targetAgeDescription: string;
  targetAgeMonths: number;
  status: 'aplicada' | 'pendiente' | 'programada';
  administeredDate?: string;
  batchNumber?: string;
  notes?: string;
}

export interface MilestoneItem {
  id: string;
  title: string;
  category: 'motora_gruesa' | 'motora_fina' | 'lenguaje' | 'cognitiva' | 'socioemocional';
  ageRange: string;
  description: string;
  achieved: boolean;
  achievedDate?: string;
}

export interface DietarySuggestion {
  id: string;
  title: string;
  category: 'plato_balanceado' | 'colaciones' | 'hidratacion' | 'selectividad' | 'nutrientes_clave';
  ageGroup: string;
  summary: string;
  tips: string[];
  recipeOrIdea?: string;
}

export interface ActivitySuggestion {
  id: string;
  title: string;
  area: 'motora_gruesa' | 'motora_fina' | 'lenguaje' | 'cognitiva' | 'socioemocional' | 'creatividad';
  duration: string;
  materials: string[];
  instructions: string;
  benefits: string;
}

export interface InAppNotification {
  id: string;
  type: 'medicamento' | 'consulta' | 'estudio' | 'actividad' | 'agua';
  title: string;
  message: string;
  timestamp: string;
  isRead: boolean;
  urgent?: boolean;
}
