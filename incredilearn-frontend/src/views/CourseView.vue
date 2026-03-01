<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'

const route = useRoute()
const courseId = Number(route.params.id)

/* ── Course data (same as HomeView) ── */
const allCourses = [
  {
    id: 1,
    title: 'Introduction à la programmation Python',
    instructor: 'Dr. Marie Dupont',
    category: 'Programmation',
    image: 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=900&h=500&fit=crop&q=80',
    lessons: [
      { id: 1, title: 'Introduction et installation', duration: '15:32' },
      { id: 2, title: 'Variables et types de données',  duration: '22:15' },
      { id: 3, title: 'Structures conditionnelles',     duration: '18:45' },
    ],
    resources: [
      { name: 'Introduction à Python - Guide complet', type: 'PDF', size: '2.4 MB' },
    ],
  },
  {
    id: 2,
    title: 'Design UI/UX Moderne',
    instructor: 'Sophie Martin',
    category: 'Design',
    image: 'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=900&h=500&fit=crop&q=80',
    lessons: [
      { id: 1, title: 'Introduction au Design Thinking',  duration: '12:00' },
      { id: 2, title: 'Principes de UI',                  duration: '18:30' },
      { id: 3, title: 'Prototypage avec Figma',           duration: '25:10' },
    ],
    resources: [
      { name: 'UI Kit Complet', type: 'PDF', size: '5.1 MB' },
    ],
  },
  {
    id: 3,
    title: 'Marketing Digital Avancé',
    instructor: 'Thomas Bernard',
    category: 'Marketing',
    image: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=900&h=500&fit=crop&q=80',
    lessons: [
      { id: 1, title: 'Stratégie de contenu',   duration: '14:00' },
      { id: 2, title: 'SEO & SEM',              duration: '20:00' },
      { id: 3, title: 'Réseaux sociaux',        duration: '16:45' },
    ],
    resources: [
      { name: 'Guide Marketing Digital', type: 'PDF', size: '3.2 MB' },
    ],
  },
  {
    id: 4,
    title: 'Excel pour Professionnels',
    instructor: 'Marc Dubois',
    category: 'Bureautique',
    image: 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=900&h=500&fit=crop&q=80',
    lessons: [
      { id: 1, title: 'Formules avancées',         duration: '18:00' },
      { id: 2, title: 'Tableaux croisés dynamiques', duration: '22:00' },
      { id: 3, title: 'Macros & VBA',              duration: '30:00' },
    ],
    resources: [
      { name: 'Fichiers Excel exercices', type: 'PDF', size: '1.8 MB' },
    ],
  },
  {
    id: 10,
    title: 'Intelligence Artificielle avec TensorFlow',
    instructor: 'Dr. Jean Leclerc',
    category: 'Programmation',
    image: 'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=900&h=500&fit=crop&q=80',
    lessons: [
      { id: 1, title: 'Introduction au Machine Learning', duration: '20:00' },
      { id: 2, title: 'Réseaux de neurones',              duration: '25:00' },
      { id: 3, title: 'Entraîner un modèle',              duration: '30:00' },
    ],
    resources: [
      { name: 'Guide TensorFlow', type: 'PDF', size: '4.0 MB' },
    ],
  },
  {
    id: 11,
    title: 'Développement Web React',
    instructor: 'Claire Rousseau',
    category: 'Programmation',
    image: 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=900&h=500&fit=crop&q=80',
    lessons: [
      { id: 1, title: 'Composants & Props',  duration: '15:00' },
      { id: 2, title: 'State & Hooks',       duration: '20:00' },
      { id: 3, title: 'React Router',        duration: '18:00' },
    ],
    resources: [
      { name: 'React Cheat Sheet', type: 'PDF', size: '1.5 MB' },
    ],
  },
  {
    id: 12,
    title: 'Photographie Créative',
    instructor: 'Émilie Laurent',
    category: 'Art',
    image: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=900&h=500&fit=crop&q=80',
    lessons: [
      { id: 1, title: 'Composition & Cadrage',  duration: '12:00' },
      { id: 2, title: 'Lumière & Exposition',   duration: '14:00' },
      { id: 3, title: 'Retouche Lightroom',     duration: '20:00' },
    ],
    resources: [
      { name: 'Guide Photo Créative', type: 'PDF', size: '2.9 MB' },
    ],
  },
  {
    id: 13,
    title: 'Cybersécurité Fondamentale',
    instructor: 'Dr. Antoine Moreau',
    category: 'Sécurité',
    image: 'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=900&h=500&fit=crop&q=80',
    lessons: [
      { id: 1, title: 'Introduction à la cybersécurité', duration: '16:00' },
      { id: 2, title: 'Cryptographie',                   duration: '22:00' },
      { id: 3, title: 'Ethical Hacking',                 duration: '28:00' },
    ],
    resources: [
      { name: 'Guide Cybersécurité', type: 'PDF', size: '3.7 MB' },
    ],
  },
]

const course = computed(() => allCourses.find(c => c.id === courseId) ?? allCourses[0])

const activeLesson  = ref(0)
const activeTab     = ref<'resources' | 'discussion'>('resources')
const completedLessons = ref<number[]>([])

const progressPct = computed(() =>
  course.value ? Math.round((completedLessons.value.length / course.value.lessons.length) * 100) : 0
)

function selectLesson(idx: number) {
  activeLesson.value = idx
}
function toggleComplete(idx: number) {
  const i = completedLessons.value.indexOf(idx)
  if (i === -1) completedLessons.value.push(idx)
  else completedLessons.value.splice(i, 1)
}
</script>

<template>
  <div v-if="course" class="course-page">

    <!-- ── Top bar ── -->
    <div class="topbar">
      <RouterLink to="/" class="back-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
        Back to courses
      </RouterLink>
      <span class="online-badge">
        <span class="online-dot"></span> Online
      </span>
    </div>

    <!-- ── Main layout ── -->
    <div class="course-layout">

      <!-- ════ LEFT ════ -->
      <div class="course-left">

        <!-- Video player -->
        <div class="video-wrap">
          <img :src="course.image" :alt="course.title" class="video-thumb" />
          <button class="play-btn" aria-label="Lire la leçon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
              <polygon points="5 3 19 12 5 21 5 3"/>
            </svg>
          </button>
          <!-- Controls bar -->
          <div class="video-controls">
            <div class="controls-left">
              <button class="ctrl-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><polygon points="19 20 9 12 19 4 19 20"/><line x1="5" y1="19" x2="5" y2="5"/></svg>
              </button>
              <button class="ctrl-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="white"><polygon points="5 3 19 12 5 21 5 3"/></svg>
              </button>
              <button class="ctrl-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><polygon points="5 4 15 12 5 20 5 4"/><line x1="19" y1="5" x2="19" y2="19"/></svg>
              </button>
              <button class="ctrl-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14"/><path d="M15.54 8.46a5 5 0 0 1 0 7.07"/></svg>
              </button>
            </div>
            <div class="progress-bar-wrap">
              <div class="progbar"><div class="progbar-fill" style="width:0%"></div></div>
              <span class="time-label">0:00 / 0:10</span>
            </div>
            <div class="controls-right">
              <span class="lesson-label-ctrl">{{ course.lessons[activeLesson].title }}</span>
              <button class="ctrl-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"/></svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Lesson title / meta -->
        <div class="lesson-meta">
          <div>
            <h1 class="lesson-title">{{ course.lessons[activeLesson].title }}</h1>
            <p class="lesson-instructor">{{ course.instructor }} • {{ course.category }}</p>
          </div>
          <button class="fav-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
              stroke="#6B7280" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            Add to Favorites
          </button>
        </div>

        <!-- Tabs -->
        <div class="tabs-bar">
          <button
            class="tab-btn"
            :class="{ 'tab-btn--active': activeTab === 'resources' }"
            @click="activeTab = 'resources'"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            Resources ({{ course.resources.length }})
          </button>
          <button
            class="tab-btn"
            :class="{ 'tab-btn--active': activeTab === 'discussion' }"
            @click="activeTab = 'discussion'"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            Discussion
          </button>
        </div>

        <!-- Resources list -->
        <div v-if="activeTab === 'resources'" class="resources-list">
          <div v-for="res in course.resources" :key="res.name" class="resource-row">
            <div class="resource-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                stroke="#DC2626" stroke-width="2" stroke-linecap="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="resource-info">
              <p class="resource-name">{{ res.name }}</p>
              <p class="resource-meta">{{ res.type }} • {{ res.size }}</p>
            </div>
            <button class="dl-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="7 10 12 15 17 10"/>
                <line x1="12" y1="15" x2="12" y2="3"/>
              </svg>
              Télécharger
            </button>
          </div>
        </div>

        <!-- Discussion (placeholder) -->
        <div v-else class="discussion-empty">
          <p>Aucun commentaire pour le moment. Soyez le premier à participer !</p>
        </div>

      </div>

      <!-- ════ RIGHT sidebar ════ -->
      <aside class="course-sidebar">

        <!-- Progress -->
        <div class="sidebar-card">
          <div class="progress-header">
            <span class="progress-title">Your Progress</span>
            <span class="progress-status">{{ completedLessons.length === 0 ? 'Not Started' : progressPct === 100 ? 'Completed' : 'In Progress' }}</span>
          </div>
          <p class="progress-sub">{{ completedLessons.length }} of {{ course.lessons.length }} lessons</p>
          <div class="progress-bar">
            <div class="progress-bar-fill" :style="{ width: progressPct + '%' }"></div>
          </div>
          <p class="progress-pct">{{ progressPct }}%</p>
        </div>

        <!-- Course content -->
        <div class="sidebar-card sidebar-card--lessons">
          <h3 class="sidebar-section-title">Course Content</h3>
          <ul class="lesson-list">
            <li
              v-for="(lesson, idx) in course.lessons"
              :key="lesson.id"
              class="lesson-item"
              :class="{ 'lesson-item--active': activeLesson === idx, 'lesson-item--done': completedLessons.includes(idx) }"
              @click="selectLesson(idx)"
            >
              <div class="lesson-play">
                <svg v-if="activeLesson === idx" width="12" height="12" viewBox="0 0 24 24" fill="#10B981">
                  <polygon points="5 3 19 12 5 21 5 3"/>
                </svg>
                <span v-else class="lesson-num">{{ idx + 1 }}</span>
              </div>
              <div class="lesson-info">
                <p class="lesson-name">{{ lesson.title }}</p>
                <p class="lesson-dur">{{ lesson.duration }}</p>
              </div>
              <input
                type="checkbox"
                class="lesson-check"
                :checked="completedLessons.includes(idx)"
                @click.stop="toggleComplete(idx)"
              />
            </li>
          </ul>
        </div>

        <!-- Download offline -->
        <button class="dl-offline-btn">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          Download for Offline
        </button>

      </aside>
    </div>
  </div>
</template>

<style scoped>
.course-page {
  min-height: 100vh;
  background: #F9FAFB;
  font-family: var(--il-font, 'Inter', sans-serif);
}

/* ── Topbar ── */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 2rem;
  background: #ffffff;
  border-bottom: 1px solid #E5E7EB;
}
.back-link {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  transition: color 0.15s;
}
.back-link:hover { color: #10B981; }
.online-badge {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: #6B7280;
}
.online-dot {
  width: 8px;
  height: 8px;
  background: #10B981;
  border-radius: 50%;
}

/* ── Layout ── */
.course-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 1.5rem;
  max-width: 1280px;
  margin: 0 auto;
  padding: 1.25rem 2rem 3rem;
}

/* ════ VIDEO ════ */
.video-wrap {
  position: relative;
  background: #000;
  border-radius: 10px;
  overflow: hidden;
  aspect-ratio: 16/9;
}
.video-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.85;
  display: block;
}
.play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: #10B981;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s;
  padding-left: 4px;
}
.play-btn:hover { background: #059669; transform: translate(-50%, -50%) scale(1.07); }

/* Controls bar */
.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, transparent 100%);
  padding: 0.6rem 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.controls-left  { display: flex; gap: 0.4rem; }
.controls-right { display: flex; align-items: center; gap: 0.5rem; margin-left: auto; }
.ctrl-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.2rem;
  display: flex;
  align-items: center;
  opacity: 0.85;
  transition: opacity 0.15s;
}
.ctrl-btn:hover { opacity: 1; }
.progress-bar-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.progbar {
  flex: 1;
  height: 3px;
  background: rgba(255,255,255,0.35);
  border-radius: 2px;
  overflow: hidden;
}
.progbar-fill {
  height: 100%;
  background: #10B981;
  border-radius: 2px;
}
.time-label {
  font-size: 0.72rem;
  color: white;
  white-space: nowrap;
}
.lesson-label-ctrl {
  font-size: 0.72rem;
  color: #10B981;
  font-weight: 500;
  white-space: nowrap;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── Lesson meta ── */
.lesson-meta {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin: 1.1rem 0 1rem;
  gap: 1rem;
}
.lesson-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.2rem;
}
.lesson-instructor {
  font-size: 0.82rem;
  color: #6B7280;
}
.fav-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  background: none;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  padding: 0.45rem 0.85rem;
  font-size: 0.82rem;
  font-weight: 500;
  color: #6B7280;
  cursor: pointer;
  white-space: nowrap;
  transition: border-color 0.15s, color 0.15s;
  flex-shrink: 0;
}
.fav-btn:hover { border-color: #10B981; color: #10B981; }

/* ── Tabs ── */
.tabs-bar {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #E5E7EB;
  margin-bottom: 1rem;
}
.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  background: none;
  border: none;
  padding: 0.6rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #6B7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: color 0.15s, border-color 0.15s;
}
.tab-btn--active {
  color: #10B981;
  border-bottom-color: #10B981;
}

/* ── Resources ── */
.resources-list { display: flex; flex-direction: column; gap: 0.75rem; }
.resource-row {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 10px;
  padding: 0.85rem 1rem;
  background: #fff;
}
.resource-icon {
  width: 36px;
  height: 36px;
  background: #FEE2E2;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.resource-info { flex: 1; min-width: 0; }
.resource-name {
  font-size: 0.88rem;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.resource-meta { font-size: 0.75rem; color: #9CA3AF; }
.dl-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  background: none;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  padding: 0.4rem 0.85rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  white-space: nowrap;
  transition: border-color 0.15s, color 0.15s;
  flex-shrink: 0;
}
.dl-btn:hover { border-color: #10B981; color: #10B981; }

/* Discussion placeholder */
.discussion-empty {
  padding: 2rem;
  text-align: center;
  color: #9CA3AF;
  font-size: 0.875rem;
  border: 1.5px dashed #E5E7EB;
  border-radius: 10px;
}

/* ════ SIDEBAR ════ */
.sidebar-card {
  background: #ffffff;
  border: 1.5px solid #E5E7EB;
  border-radius: 12px;
  padding: 1.1rem 1.25rem;
  margin-bottom: 1rem;
}

/* Progress card */
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}
.progress-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #111827;
}
.progress-status {
  font-size: 0.75rem;
  color: #9CA3AF;
}
.progress-sub {
  font-size: 0.8rem;
  color: #6B7280;
  margin-bottom: 0.6rem;
}
.progress-bar {
  height: 6px;
  background: #E5E7EB;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 0.35rem;
}
.progress-bar-fill {
  height: 100%;
  background: #10B981;
  border-radius: 3px;
  transition: width 0.3s;
}
.progress-pct {
  font-size: 0.85rem;
  font-weight: 700;
  color: #10B981;
  text-align: right;
}

/* Course content card */
.sidebar-card--lessons { padding: 1.1rem 0; }
.sidebar-section-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #111827;
  padding: 0 1.25rem;
  margin-bottom: 0.75rem;
}
.lesson-list { list-style: none; }
.lesson-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 1.25rem;
  cursor: pointer;
  transition: background 0.15s;
  border-radius: 0;
}
.lesson-item:hover { background: #F9FAFB; }
.lesson-item--active { background: #D1FAE5; }
.lesson-item--active:hover { background: #A7F3D0; }

.lesson-play {
  width: 26px;
  height: 26px;
  background: #E5E7EB;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.lesson-item--active .lesson-play { background: #10B981; }
.lesson-num {
  font-size: 0.72rem;
  font-weight: 600;
  color: #6B7280;
}

.lesson-info { flex: 1; min-width: 0; }
.lesson-name {
  font-size: 0.82rem;
  font-weight: 500;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.lesson-dur { font-size: 0.75rem; color: #9CA3AF; }

.lesson-check {
  accent-color: #10B981;
  width: 15px;
  height: 15px;
  flex-shrink: 0;
  cursor: pointer;
}

/* Download offline */
.dl-offline-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background: #ffffff;
  border: 1.5px solid #E5E7EB;
  border-radius: 10px;
  padding: 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}
.dl-offline-btn:hover { border-color: #10B981; color: #10B981; }

/* Responsive */
@media (max-width: 900px) {
  .course-layout { grid-template-columns: 1fr; }
}
</style>
