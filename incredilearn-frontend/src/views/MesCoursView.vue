<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { RouterLink } from 'vue-router'

interface Course {
  id: number
  titre: string
  description: string
  domain: string
  enseignantNom: string
  enseignantId: number
  dureeHeures: number
  niveau: string
  nombreEtudiants: number
  imageUrl: string | null
}

const authStore = useAuthStore()
const courses = ref<Course[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  await loadCourses()
})

async function loadCourses() {
  if (!authStore.user?.id) {
    error.value = 'Utilisateur non connecté'
    loading.value = false
    return
  }

  try {
    const response = await fetch(`http://localhost:8080/api/courses/user/${authStore.user.id}`)
    if (response.ok) {
      courses.value = await response.json()
      loadProgress(courses.value.map(c => c.id))
    } else {
      error.value = 'Erreur lors du chargement des cours'
    }
  } catch (err) {
    error.value = 'Erreur de connexion au serveur'
    console.error(err)
  } finally {
    loading.value = false
  }
}

function getNiveauColor(niveau: string) {
  switch (niveau?.toLowerCase()) {
    case 'débutant': return '#10B981'
    case 'intermédiaire': return '#F59E0B'
    case 'avancé': return '#EF4444'
    default: return '#6B7280'
  }
}

const KEYWORD_IMAGES: Record<string, string> = {
  'python':       'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=420&h=200&fit=crop',
  'react':        'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=420&h=200&fit=crop',
  'vue':          'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=420&h=200&fit=crop',
  'javascript':   'https://images.unsplash.com/photo-1627398242454-45a1465c2479?w=420&h=200&fit=crop',
  'spring':       'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=420&h=200&fit=crop',
  'node':         'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=420&h=200&fit=crop',
  'docker':       'https://images.unsplash.com/photo-1605745341112-85968b19335b?w=420&h=200&fit=crop',
  'sql':          'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=420&h=200&fit=crop',
  'git':          'https://images.unsplash.com/photo-1556075798-4825dfaaf498?w=420&h=200&fit=crop',
  'mobile':       'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=420&h=200&fit=crop',
  'html':         'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=420&h=200&fit=crop',
  'design':       'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=420&h=200&fit=crop',
  'figma':        'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=420&h=200&fit=crop',
  'photoshop':    'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=420&h=200&fit=crop',
  'video':        'https://images.unsplash.com/photo-1492619375914-88005aa9e8fb?w=420&h=200&fit=crop',
  'marketing':    'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=420&h=200&fit=crop',
  'seo':          'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=420&h=200&fit=crop',
  'excel':        'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=420&h=200&fit=crop',
  'intelligence': 'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=420&h=200&fit=crop',
  'machine':      'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=420&h=200&fit=crop',
  'photographie': 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=420&h=200&fit=crop',
  'photo':        'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=420&h=200&fit=crop',
  'cyber':        'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=420&h=200&fit=crop',
  'securit':      'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=420&h=200&fit=crop',
  'finance':      'https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=420&h=200&fit=crop',
  'agile':        'https://images.unsplash.com/photo-1552664730-d307ca884978?w=420&h=200&fit=crop',
  'default':      'https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=420&h=200&fit=crop',
}

function getCourseImage(titre: string, domain: string): string {
  const text = ((titre || '') + ' ' + (domain || '')).toLowerCase()
  const key = Object.keys(KEYWORD_IMAGES).find(k => k !== 'default' && text.includes(k)) || 'default'
  return KEYWORD_IMAGES[key] as string
}

const unenrolling = ref<Record<number, boolean>>({})
const progressMap = ref<Record<number, number>>({})

function progressKey(courseId: number) {
  return `incredilearn_progress_${authStore.user?.id}_${courseId}`
}

function loadProgress(courseIds: number[]) {
  const map: Record<number, number> = {}
  for (const id of courseIds) {
    const stored = localStorage.getItem(progressKey(id))
    map[id] = stored !== null ? parseInt(stored, 10) : 0
  }
  progressMap.value = map
}

function getProgressLabel(pct: number) {
  if (pct === 0) return 'Non commencé'
  if (pct === 100) return 'Terminé'
  return `${pct}% complété`
}

function getProgressColor(pct: number) {
  if (pct === 100) return '#10B981'
  if (pct >= 50) return '#F59E0B'
  return '#6366F1'
}

async function unenroll(courseId: number) {
  if (!authStore.user?.id || unenrolling.value[courseId]) return
  if (!confirm('Se désinscrire de ce cours ?')) return
  unenrolling.value[courseId] = true
  try {
    const res = await fetch(
      `http://localhost:8080/api/courses/enroll/${authStore.user.id}/${courseId}`,
      { method: 'DELETE' }
    )
    if (res.ok) {
      courses.value = courses.value.filter(c => c.id !== courseId)
    }
  } catch (err) {
    console.error(err)
  } finally {
    unenrolling.value[courseId] = false
  }
}
</script>

<template>
  <div class="cours-page">
    <div class="cours-container">
      <!-- Header -->
      <div class="page-header">
        <h1 class="page-title">Mes cours</h1>
        <p class="page-subtitle">Les cours auxquels vous êtes inscrit</p>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>Chargement de vos cours...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="error-banner">
        {{ error }}
      </div>

      <!-- Empty state -->
      <div v-else-if="courses.length === 0" class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="1.5">
          <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
          <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
        </svg>
        <h3>Aucun cours pour le moment</h3>
        <p>Vous n'êtes inscrit à aucun cours. Explorez notre catalogue pour commencer à apprendre !</p>
        <RouterLink to="/" class="btn-primary">Explorer les cours</RouterLink>
      </div>

      <!-- Courses Grid -->
      <div v-else class="cours-grid">
        <div v-for="course in courses" :key="course.id" class="course-card">
          <!-- Image -->
          <div class="course-image">
            <img :src="course.imageUrl ? `http://localhost:8080${course.imageUrl}` : getCourseImage(course.titre, course.domain)" :alt="course.titre" loading="lazy" />
          </div>

          <div class="course-body">
          <div class="course-header">
            <span class="course-domain">{{ course.domain || 'Général' }}</span>
            <span class="course-niveau" :style="{ backgroundColor: getNiveauColor(course.niveau) + '20', color: getNiveauColor(course.niveau) }">
              {{ course.niveau || 'Tous niveaux' }}
            </span>
          </div>

          <h3 class="course-title">{{ course.titre }}</h3>
          <p class="course-description">{{ course.description }}</p>

          <div class="course-meta">
            <div class="meta-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              {{ course.enseignantNom }}
            </div>
            <div class="meta-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              {{ course.dureeHeures }}h
            </div>
            <div class="meta-item">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
              {{ course.nombreEtudiants }} étudiants
            </div>
          </div>

          <!-- Progress bar -->
          <div class="course-progress">
            <div class="progress-header">
              <span class="progress-label">{{ getProgressLabel(progressMap[course.id] ?? 0) }}</span>
              <span v-if="(progressMap[course.id] ?? 0) > 0" class="progress-pct">{{ progressMap[course.id] }}%</span>
            </div>
            <div class="progress-track">
              <div
                class="progress-fill"
                :style="{ width: (progressMap[course.id] ?? 0) + '%', backgroundColor: getProgressColor(progressMap[course.id] ?? 0) }"
              ></div>
            </div>
          </div>

          <div class="course-actions">
            <RouterLink :to="`/cours/${course.id}`" class="course-link">
              Accéder au cours
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </RouterLink>
            <button class="unenroll-btn" :disabled="unenrolling[course.id]" @click="unenroll(course.id)">
              <svg v-if="unenrolling[course.id]" width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
              <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <line x1="22" y1="18" x2="16" y2="18"/>
              </svg>
              {{ unenrolling[course.id] ? 'En cours…' : 'Se désinscrire' }}
            </button>
          </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cours-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #F0FDF4 0%, #F0F9FF 100%);
  padding: 3rem 1rem;
}

.cours-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Header */
.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #6B7280;
}

/* Loading */
.loading {
  text-align: center;
  padding: 3rem;
  color: #6B7280;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #E5E7EB;
  border-top-color: #10B981;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Error */
.error-banner {
  background: #FEE2E2;
  border: 1px solid #FCA5A5;
  color: #DC2626;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.empty-state svg {
  margin-bottom: 1.5rem;
}

.empty-state h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.75rem;
}

.empty-state p {
  font-size: 1rem;
  color: #6B7280;
  margin-bottom: 2rem;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

/* Courses Grid */
.cours-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.5rem;
}

.course-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: transform 0.15s, box-shadow 0.15s;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* Course image */
.course-image {
  height: 180px;
  overflow: hidden;
  flex-shrink: 0;
}
.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.course-card:hover .course-image img {
  transform: scale(1.04);
}

/* Body wrapper for padded content */
.course-body {
  padding: 1.25rem 1.5rem 1.5rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.course-header {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.course-domain,
.course-niveau {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.course-domain {
  background: #E0F2FE;
  color: #0284C7;
}

.course-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.75rem;
}

.course-description {
  font-size: 0.95rem;
  color: #6B7280;
  line-height: 1.5;
  margin-bottom: 1.5rem;
  flex-grow: 1;
}

.course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem 0;
  border-top: 1px solid #E5E7EB;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #6B7280;
}

.course-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #10B981;
  color: #ffffff;
  font-weight: 600;
  border-radius: 8px;
  transition: background 0.15s;
}

.course-link:hover {
  background: #059669;
}

.course-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.unenroll-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.6rem;
  background: #FEE2E2;
  color: #DC2626;
  border: 1.5px solid #FECACA;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.unenroll-btn:hover:not(:disabled) {
  background: #FCA5A5;
  border-color: #F87171;
}

.unenroll-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Progress bar */
.course-progress {
  margin-bottom: 1rem;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.4rem;
}

.progress-label {
  font-size: 0.78rem;
  font-weight: 500;
  color: #6B7280;
}

.progress-pct {
  font-size: 0.78rem;
  font-weight: 600;
  color: #374151;
}

.progress-track {
  width: 100%;
  height: 7px;
  background: #E5E7EB;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.3s ease;
  min-width: 0;
}

.btn-primary {
  display: inline-block;
  background: #10B981;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  transition: background 0.15s;
}

.btn-primary:hover {
  background: #059669;
}
</style>
