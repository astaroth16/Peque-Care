import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { DIETARY_SUGGESTIONS, ACTIVITY_SUGGESTIONS } from '../../data/pediatricKnowledge';
import { formatAgeString } from '../../utils/whoStandards';
import {
  Sparkles,
  Utensils,
  Smile,
  CheckCircle2,
  Clock,
  BookOpen,
  Award,
  ChevronRight,
  Flame,
  Heart
} from 'lucide-react';

export const SuggestionsTab: React.FC = () => {
  const { profile, currentAgeMonths, milestones, toggleMilestone } = useApp();
  const [activeCategory, setActiveCategory] = useState<'dietary' | 'activities' | 'milestones'>('dietary');
  const [selectedArea, setSelectedArea] = useState<string>('todos');

  // Milestone statistics
  const achievedMilestones = milestones.filter(m => m.achieved).length;
  const totalMilestones = milestones.length;
  const milestonePercent = Math.round((achievedMilestones / totalMilestones) * 100);

  // Filtered milestones
  const filteredMilestones = selectedArea === 'todos'
    ? milestones
    : milestones.filter(m => m.category === selectedArea);

  // Filtered activities
  const filteredActivities = selectedArea === 'todos'
    ? ACTIVITY_SUGGESTIONS
    : ACTIVITY_SUGGESTIONS.filter(a => a.area === selectedArea);

  return (
    <div className="space-y-4">
      {/* Category selector */}
      <div className="bg-white rounded-2xl p-1.5 border border-slate-200/80 shadow-2xs flex overflow-x-auto gap-1">
        <button
          onClick={() => { setActiveCategory('dietary'); setSelectedArea('todos'); }}
          className={`flex-1 min-w-[130px] py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            activeCategory === 'dietary'
              ? 'bg-emerald-600 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Utensils className="w-3.5 h-3.5" />
          <span>Sugerencias Dietéticas</span>
        </button>

        <button
          onClick={() => { setActiveCategory('activities'); setSelectedArea('todos'); }}
          className={`flex-1 min-w-[130px] py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            activeCategory === 'activities'
              ? 'bg-indigo-600 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Smile className="w-3.5 h-3.5" />
          <span>Actividades & Juegos</span>
        </button>

        <button
          onClick={() => { setActiveCategory('milestones'); setSelectedArea('todos'); }}
          className={`flex-1 min-w-[130px] py-2.5 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${
            activeCategory === 'milestones'
              ? 'bg-amber-500 text-white shadow-xs'
              : 'text-slate-600 hover:bg-slate-100'
          }`}
        >
          <Award className="w-3.5 h-3.5" />
          <span>Hitos del Desarrollo ({achievedMilestones}/{totalMilestones})</span>
        </button>
      </div>

      {/* Header Context for Age */}
      <div className="bg-gradient-to-r from-slate-900 to-indigo-950 rounded-2xl p-4 text-white flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-1 rounded-lg bg-indigo-500/30 text-indigo-300">
              <Sparkles className="w-4 h-4" />
            </span>
            <h3 className="font-bold text-sm">
              Personalizado para {profile.name} ({formatAgeString(currentAgeMonths)})
            </h3>
          </div>
          <p className="text-xs text-slate-300 mt-0.5">
            Etapa preescolar (3 a 5 años): Consolidación del lenguaje, autonomía motriz y nutrición equilibrada
          </p>
        </div>

        {activeCategory === 'milestones' && (
          <div className="bg-white/10 px-3 py-1.5 rounded-xl border border-white/20 text-right">
            <div className="text-xs font-bold text-amber-300">{milestonePercent}% Completado</div>
            <div className="w-24 bg-white/20 h-1.5 rounded-full mt-1 overflow-hidden">
              <div className="bg-amber-400 h-1.5 rounded-full" style={{ width: `${milestonePercent}%` }}></div>
            </div>
          </div>
        )}
      </div>

      {/* 1. DIETARY SUGGESTIONS */}
      {activeCategory === 'dietary' && (
        <div className="space-y-3.5">
          {DIETARY_SUGGESTIONS.map(sug => (
            <div
              key={sug.id}
              className="bg-white rounded-2xl p-4 md:p-5 border border-slate-200/80 shadow-2xs space-y-3 hover:border-emerald-300 transition"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-2.5">
                  <span className="p-2 rounded-xl bg-emerald-50 text-emerald-600">
                    <Utensils className="w-5 h-5" />
                  </span>
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md">
                      {sug.ageGroup}
                    </span>
                    <h4 className="text-base font-bold text-slate-900 mt-1">
                      {sug.title}
                    </h4>
                  </div>
                </div>
              </div>

              <p className="text-xs text-slate-600 font-medium">
                {sug.summary}
              </p>

              <div className="bg-slate-50 rounded-xl p-3 border border-slate-100 space-y-1.5 text-xs">
                <span className="font-bold text-slate-700 text-[11px] uppercase tracking-wider block">
                  Recomendaciones Clave del Pediatra:
                </span>
                {sug.tips.map((tip, i) => (
                  <div key={i} className="flex items-start gap-2 text-slate-700">
                    <span className="text-emerald-500 font-bold">•</span>
                    <span>{tip}</span>
                  </div>
                ))}
              </div>

              {sug.recipeOrIdea && (
                <div className="bg-emerald-50/70 p-3 rounded-xl border border-emerald-200/70 text-xs text-emerald-950">
                  <strong className="text-emerald-900 font-bold block mb-0.5">
                    💡 Idea Práctica / Receta Rápida:
                  </strong>
                  <span>{sug.recipeOrIdea}</span>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 2. RECREATIONAL & STIMULATION ACTIVITIES */}
      {activeCategory === 'activities' && (
        <div className="space-y-3.5">
          {/* Filter Pills */}
          <div className="flex flex-wrap gap-1.5 text-xs">
            {['todos', 'motora_gruesa', 'motora_fina', 'lenguaje', 'cognitiva', 'socioemocional', 'creatividad'].map(area => (
              <button
                key={area}
                onClick={() => setSelectedArea(area)}
                className={`px-3 py-1.5 rounded-xl font-bold capitalize transition ${
                  selectedArea === area
                    ? 'bg-indigo-600 text-white shadow-2xs'
                    : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                {area.replace('_', ' ')}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
            {filteredActivities.map(act => (
              <div
                key={act.id}
                className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-2xs space-y-2.5 flex flex-col justify-between hover:border-indigo-300 transition"
              >
                <div>
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded-md">
                      {act.area.replace('_', ' ')}
                    </span>
                    <span className="flex items-center gap-1 text-[11px] text-slate-500 font-medium">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      {act.duration}
                    </span>
                  </div>

                  <h4 className="text-sm font-bold text-slate-900 mt-1.5">
                    {act.title}
                  </h4>

                  <div className="mt-2 text-xs text-slate-600 leading-relaxed">
                    <strong className="text-slate-800">¿Cómo jugarlo?: </strong>
                    {act.instructions}
                  </div>
                </div>

                <div className="space-y-2 pt-2 border-t border-slate-100">
                  <div className="text-[11px] text-slate-500">
                    <strong>Materiales: </strong>
                    {act.materials.join(', ')}
                  </div>
                  <div className="text-[11px] text-emerald-800 bg-emerald-50 p-2 rounded-lg border border-emerald-100">
                    <strong>Habilidad desarrollada: </strong>
                    {act.benefits}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 3. DEVELOPMENT MILESTONES (OMS & CDC) */}
      {activeCategory === 'milestones' && (
        <div className="space-y-3.5">
          {/* Filter Pills */}
          <div className="flex flex-wrap gap-1.5 text-xs">
            {['todos', 'motora_gruesa', 'motora_fina', 'lenguaje', 'cognitiva', 'socioemocional'].map(area => (
              <button
                key={area}
                onClick={() => setSelectedArea(area)}
                className={`px-3 py-1.5 rounded-xl font-bold capitalize transition ${
                  selectedArea === area
                    ? 'bg-amber-600 text-white shadow-2xs'
                    : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                {area.replace('_', ' ')}
              </button>
            ))}
          </div>

          <div className="space-y-2.5">
            {filteredMilestones.map(ms => (
              <div
                key={ms.id}
                onClick={() => toggleMilestone(ms.id)}
                className={`bg-white rounded-2xl p-4 border transition cursor-pointer flex items-start gap-3.5 ${
                  ms.achieved
                    ? 'border-emerald-200 bg-emerald-50/30 shadow-2xs'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <button
                  type="button"
                  className={`w-6 h-6 rounded-lg flex items-center justify-center font-bold text-xs shrink-0 mt-0.5 transition ${
                    ms.achieved
                      ? 'bg-emerald-600 text-white shadow-2xs'
                      : 'border-2 border-slate-300 text-transparent hover:border-indigo-500'
                  }`}
                >
                  ✓
                </button>

                <div className="flex-1 min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 bg-slate-100 px-2 py-0.2 rounded-md">
                      {ms.category.replace('_', ' ')}
                    </span>
                    <span className="text-xs text-slate-400 font-medium">
                      {ms.ageRange}
                    </span>
                    {ms.achieved && (
                      <span className="text-[10px] bg-emerald-100 text-emerald-800 px-2 py-0.2 rounded-full font-bold ml-auto">
                        Alcanzado el {ms.achievedDate || 'Recientemente'}
                      </span>
                    )}
                  </div>

                  <h4 className={`text-sm font-bold mt-1 ${ms.achieved ? 'text-slate-900 line-through text-slate-500' : 'text-slate-900'}`}>
                    {ms.title}
                  </h4>
                  <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">
                    {ms.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
