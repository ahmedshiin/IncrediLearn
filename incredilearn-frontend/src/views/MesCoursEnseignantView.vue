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
  nombreLecons: number
  dureeTotaleSecondes: number
  createdAt: string
}

const authStore = useAuthStore()
const courses = ref<Course[]>([])
const loading = ref(true)
const error = ref('')

// Delete modal state
const deleteModal = ref(false)
const courseToDelete = ref<Course | null>(null)
const deleting = ref(false)

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
    const response = await fetch(`http://localhost:8080/api/courses/enseignant/${authStore.user.id}`)
    if (response.ok) {
      courses.value = await response.json()
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
  'default':      'https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=420&h=200&fit=crop',
}

function getCourseImage(course: Course): string {
  if (course.imageUrl) return `http://localhost:8080${course.imageUrl}`
  const text = ((course.titre || '') + ' ' + (course.domain || '')).toLowerCase()
  const key = Object.keys(KEYWORD_IMAGES).find(k => k !== 'default' && text.includes(k)) || 'default'
  return KEYWORD_IMAGES[key] as string
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
}

function formatDuration(seconds: number): string {
  if (!seconds || seconds <= 0) return '0min'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (h > 0 && m > 0) return `${h}h${String(m).padStart(2, '0')}`
  if (h > 0) return `${h}h`
  return `${m}min`
}

// Delete
function openDelete(course: Course) {
  courseToDelete.value = course
  deleteModal.value = true
}

async function confirmDelete() {
  if (!courseToDelete.value || !authStore.user?.id || deleting.value) return
  deleting.value = true
  try {
    const res = await fetch(
      `http://localhost:8080/api/courses/${courseToDelete.value.id}?enseignantId=${authStore.user.id}`,
      { method: 'DELETE' },
    )
    if (res.ok) {
      courses.value = courses.value.filter(c => c.id !== courseToDelete.value!.id)
      deleteModal.value = false
    }
  } catch (err) {
    console.error(err)
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="container">
      <!-- Header -->
      <div class="page-header">
        <div>
          <h1 class="page-title">Mes cours créés</h1>
          <p class="page-subtitle">Gérez les cours que vous avez créés en tant qu'enseignant</p>
        </div>
        <RouterLink to="/creer-cours" class="btn-create">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          Créer un cours
        </RouterLink>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>Chargement de vos cours...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="error-banner">{{ error }}</div>

      <!-- Empty state -->
      <div v-else-if="courses.length === 0" class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="1.5">
          <path d="M12 20h9" /><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
        </svg>
        <h3>Vous n'avez encore créé aucun cours</h3>
        <p>Partagez vos connaissances avec le monde en créant votre premier cours !</p>
        <RouterLink to="/creer-cours" class="btn-primary">Créer mon premier cours</RouterLink>
      </div>

      <!-- Courses Grid -->
      <div v-else class="cours-grid">
        <div v-for="course in courses" :key="course.id" class="course-card">
          <div class="course-image">
            <img :src="getCourseImage(course)" :alt="course.titre" loading="lazy" />
            <div class="image-overlay">
              <span class="student-badge">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
                {{ course.nombreEtudiants }} inscrit{{ course.nombreEtudiants !== 1 ? 's' : '' }}
              </span>
            </div>
          </div>

          <div class="course-body">
            <div class="course-header">
              <span class="course-domain">{{ course.domain || 'Général' }}</span>
              <span class="course-niveau"
                :style="{ backgroundColor: getNiveauColor(course.niveau) + '20', color: getNiveauColor(course.niveau) }">
                {{ course.niveau || 'Tous niveaux' }}
              </span>
            </div>

            <h3 class="course-title">{{ course.titre }}</h3>
            <p class="course-description">{{ course.description }}</p>

            <div class="course-stats">
              <div class="stat">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
                </svg>
                <span>{{ course.nombreLecons || 0 }} leçon{{ (course.nombreLecons || 0) !== 1 ? 's' : '' }}</span>
              </div>
              <div class="stat">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
                <span>{{ formatDuration(course.dureeTotaleSecondes) }}</span>
              </div>
              <div class="stat">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                  <line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                <span>{{ formatDate(course.createdAt) }}</span>
              </div>
            </div>

            <div class="course-actions">
              <RouterLink :to="`/cours/${course.id}`" class="btn-view">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" />
                </svg>
                Voir / Modifier
              </RouterLink>
              <button class="btn-delete" @click="openDelete(course)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                </svg>
                Supprimer
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <Teleport to="body">
      <div v-if="deleteModal" class="modal-overlay" @click.self="deleteModal = false">
        <div class="modal modal-sm">
          <div class="modal-header">
            <h2>Supprimer le cours</h2>
            <button class="modal-close" @click="deleteModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <p class="delete-warning">
              Êtes-vous sûr de vouloir supprimer le cours
              <strong>« {{ courseToDelete?.titre }} »</strong> ?
              Cette action est irréversible. Tous les contenus et inscriptions seront supprimés.
            </p>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="deleteModal = false">Annuler</button>
            <button class="btn-delete-confirm" :disabled="deleting" @click="confirmDelete">
              {{ deleting ? 'Suppression...' : 'Supprimer définitivement' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #F0FDF4 0%, #F0F9FF 100%);
  padding: 3rem 1rem;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.25rem;
}

.page-subtitle {
  font-size: 1.05rem;
  color: #6B7280;
}

.btn-create {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1.4rem;
  background: #10B981;
  color: #fff;
  font-weight: 600;
  font-size: 0.95rem;
  border-radius: 10px;
  transition: background 0.15s;
  text-decoration: none;
}
.btn-create:hover {
  background: #059669;
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
@keyframes spin { to { transform: rotate(360deg); } }

/* Error */
.error-banner {
  background: #FEE2E2;
  border: 1px solid #FCA5A5;
  color: #DC2626;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
}

/* Empty */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.empty-state svg { margin-bottom: 1.5rem; }
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
.btn-primary {
  display: inline-block;
  background: #10B981;
  color: #fff;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  transition: background 0.15s;
  text-decoration: none;
}
.btn-primary:hover { background: #059669; }

/* Grid */
.cours-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.5rem;
}

.course-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transition: transform 0.15s, box-shadow 0.15s;
  display: flex;
  flex-direction: column;
}
.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

/* Image */
.course-image {
  height: 180px;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
}
.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.course-card:hover .course-image img { transform: scale(1.04); }

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 0.5rem 0.75rem;
  background: linear-gradient(transparent, rgba(0,0,0,0.55));
  display: flex;
  justify-content: flex-end;
}

.student-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  background: rgba(255,255,255,0.9);
  color: #374151;
  font-size: 0.78rem;
  font-weight: 600;
  padding: 0.25rem 0.6rem;
  border-radius: 6px;
}

/* Body */
.course-body {
  padding: 1.25rem 1.5rem 1.5rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.course-header {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.course-domain, .course-niveau {
  padding: 0.2rem 0.65rem;
  border-radius: 12px;
  font-size: 0.78rem;
  font-weight: 500;
}
.course-domain {
  background: #E0F2FE;
  color: #0284C7;
}

.course-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.5rem;
}

.course-description {
  font-size: 0.9rem;
  color: #6B7280;
  line-height: 1.5;
  margin-bottom: 1rem;
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 0.75rem 0;
  border-top: 1px solid #E5E7EB;
  margin-bottom: 1rem;
}
.stat {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.82rem;
  color: #6B7280;
}

/* Actions */
.course-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-view, .btn-delete {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.55rem 0.85rem;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  text-decoration: none;
  border: 1.5px solid transparent;
}

.btn-view {
  background: #10B981;
  color: #fff;
  flex: 1;
  justify-content: center;
}
.btn-view:hover { background: #059669; }

.btn-delete {
  background: #FEF2F2;
  color: #DC2626;
  border-color: #FECACA;
}
.btn-delete:hover {
  background: #FEE2E2;
  border-color: #FCA5A5;
}

/* ── Modal ────────────────────────────── */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.modal {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 560px;
  box-shadow: 0 24px 48px rgba(0,0,0,0.18);
  overflow: hidden;
}
.modal-sm { max-width: 460px; }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #E5E7EB;
}
.modal-header h2 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #111827;
}
.modal-close {
  background: none;
  border: none;
  font-size: 1.6rem;
  color: #9CA3AF;
  cursor: pointer;
  line-height: 1;
}
.modal-close:hover { color: #374151; }

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid #E5E7EB;
}

.btn-cancel {
  padding: 0.6rem 1.2rem;
  background: #F3F4F6;
  color: #374151;
  border: 1.5px solid #D1D5DB;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-cancel:hover { background: #E5E7EB; }

.btn-delete-confirm {
  padding: 0.6rem 1.2rem;
  background: #DC2626;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-delete-confirm:hover { background: #B91C1C; }
.btn-delete-confirm:disabled { opacity: 0.6; cursor: not-allowed; }

.delete-warning {
  font-size: 0.95rem;
  color: #374151;
  line-height: 1.6;
}
.delete-warning strong { color: #111827; }
</style>
