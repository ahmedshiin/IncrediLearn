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

          <RouterLink :to="`/cours/${course.id}`" class="course-link">
            Accéder au cours
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="5" y1="12" x2="19" y2="12"/>
              <polyline points="12 5 19 12 12 19"/>
            </svg>
          </RouterLink>
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
  padding: 1.5rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: transform 0.15s, box-shadow 0.15s;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
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
