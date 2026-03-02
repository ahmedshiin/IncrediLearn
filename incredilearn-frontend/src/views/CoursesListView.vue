<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'

const searchQuery = ref('')
const selectedLevel = ref('Tous les niveaux')
const selectedDomain = ref('Tous')
const isLoading = ref(true)

const levels  = ['Tous les niveaux', 'Débutant', 'Intermédiaire', 'Avancé']
const domains = ['Tous', 'Informatique & Programmation', 'Design', 'Marketing', 'Bureautique', 'Business', 'Intelligence Artificielle', 'Cybersécurité', 'Photographie']

interface CourseItem {
  id: number
  title: string
  instructor: string
  description: string
  duration: string
  students: string
  badge: string
  badgeClass: string
  image: string
  domain: string
}

const allCourses = ref<CourseItem[]>([])

const KEYWORD_IMAGES: Record<string, string> = {
  'python':       'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=420&h=220&fit=crop',
  'react':        'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=420&h=220&fit=crop',
  'vue':          'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=420&h=220&fit=crop',
  'javascript':   'https://images.unsplash.com/photo-1627398242454-45a1465c2479?w=420&h=220&fit=crop',
  'spring':       'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=420&h=220&fit=crop',
  'java':         'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=420&h=220&fit=crop',
  'node':         'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=420&h=220&fit=crop',
  'docker':       'https://images.unsplash.com/photo-1605745341112-85968b19335b?w=420&h=220&fit=crop',
  'sql':          'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=420&h=220&fit=crop',
  'git':          'https://images.unsplash.com/photo-1556075798-4825dfaaf498?w=420&h=220&fit=crop',
  'mobile':       'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=420&h=220&fit=crop',
  'html':         'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=420&h=220&fit=crop',
  'css':          'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=420&h=220&fit=crop',
  'design':       'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=420&h=220&fit=crop',
  'figma':        'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=420&h=220&fit=crop',
  'photoshop':    'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=420&h=220&fit=crop',
  'video':        'https://images.unsplash.com/photo-1492619375914-88005aa9e8fb?w=420&h=220&fit=crop',
  'marketing':    'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=420&h=220&fit=crop',
  'seo':          'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=420&h=220&fit=crop',
  'excel':        'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=420&h=220&fit=crop',
  'bureautique':  'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=420&h=220&fit=crop',
  'intelligence': 'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=420&h=220&fit=crop',
  'machine':      'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=420&h=220&fit=crop',
  'photographie': 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=420&h=220&fit=crop',
  'photo':        'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=420&h=220&fit=crop',
  'cyber':        'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=420&h=220&fit=crop',
  'securit':      'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=420&h=220&fit=crop',
  'finance':      'https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=420&h=220&fit=crop',
  'agile':        'https://images.unsplash.com/photo-1552664730-d307ca884978?w=420&h=220&fit=crop',
  'scrum':        'https://images.unsplash.com/photo-1552664730-d307ca884978?w=420&h=220&fit=crop',
  'default':      'https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=420&h=220&fit=crop',
}

function getCourseImage(titre: string, domain: string): string {
  const text = ((titre || '') + ' ' + (domain || '')).toLowerCase()
  const key = Object.keys(KEYWORD_IMAGES).find(k => k !== 'default' && text.includes(k)) || 'default'
  return KEYWORD_IMAGES[key] as string
}

function getBadgeClass(niveau: string): string {
  if (niveau === 'Débutant') return 'badge-green'
  if (niveau === 'Avancé') return 'badge-orange'
  return 'badge-blue'
}

onMounted(async () => {
  try {
    const res = await fetch('http://localhost:8080/api/courses')
    const data = await res.json()
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    allCourses.value = data.map((c: any) => ({
      id: c.id,
      title: c.titre,
      instructor: c.enseignantNom || 'Instructeur',
      description: c.description || '',
      duration: c.dureeHeures ? `${c.dureeHeures}h` : '-',
      students: c.nombreEtudiants != null ? String(c.nombreEtudiants) : '0',
      badge: c.niveau || 'Débutant',
      badgeClass: getBadgeClass(c.niveau),
      image: getCourseImage(c.titre, c.domain),
      domain: c.domain || '',
    }))
  } catch (e) {
    console.error('Erreur chargement cours', e)
  } finally {
    isLoading.value = false
  }
})

const filteredCourses = computed(() => {
  const q = searchQuery.value.toLowerCase()
  return allCourses.value.filter(c => {
    const matchSearch = !q ||
      c.title.toLowerCase().includes(q) ||
      c.instructor.toLowerCase().includes(q) ||
      c.description.toLowerCase().includes(q)
    const matchLevel  = selectedLevel.value === 'Tous les niveaux' || c.badge === selectedLevel.value
    const matchDomain = selectedDomain.value === 'Tous' || c.domain === selectedDomain.value
    return matchSearch && matchLevel && matchDomain
  })
})
</script>

<template>
  <div class="courses-page">

    <!-- Hero -->
    <section class="hero">
      <div class="hero-content">
        <h1>Tous les cours</h1>
        <p>Explorez notre catalogue complet de {{ allCourses.length }} formations</p>
      </div>
    </section>

    <!-- Filters -->
    <section class="filters-bar">
      <div class="filters-inner">
        <div class="search-wrap">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input v-model="searchQuery" type="text" placeholder="Rechercher un cours ou un instructeur…" />
        </div>
        <select v-model="selectedDomain">
          <option v-for="d in domains" :key="d">{{ d }}</option>
        </select>
        <select v-model="selectedLevel">
          <option v-for="l in levels" :key="l">{{ l }}</option>
        </select>
        <span class="result-count">{{ filteredCourses.length }} résultat{{ filteredCourses.length !== 1 ? 's' : '' }}</span>
      </div>
    </section>

    <!-- Grid -->
    <section class="courses-grid-section">
      <div v-if="isLoading" class="loading">Chargement des cours…</div>

      <div v-else-if="filteredCourses.length === 0" class="empty">
        <p>Aucun cours ne correspond à votre recherche.</p>
        <button @click="searchQuery = ''; selectedLevel = 'Tous les niveaux'; selectedDomain = 'Tous'">
          Réinitialiser les filtres
        </button>
      </div>

      <div v-else class="courses-grid">
        <RouterLink
          v-for="course in filteredCourses"
          :key="course.id"
          :to="`/cours/${course.id}`"
          class="card"
        >
          <div class="card-image">
            <img :src="course.image" :alt="course.title" loading="lazy" />
            <span :class="['badge', course.badgeClass]">{{ course.badge }}</span>
          </div>
          <div class="card-body">
            <div class="card-domain">{{ course.domain }}</div>
            <h3 class="card-title">{{ course.title }}</h3>
            <p class="card-desc">{{ course.description }}</p>
            <div class="card-instructor">
              <div class="avatar">{{ course.instructor.charAt(0) }}</div>
              <span>{{ course.instructor }}</span>
            </div>
            <div class="card-meta">
              <span>⏱ {{ course.duration }}</span>
              <span>👥 {{ course.students }} étudiant{{ Number(course.students) !== 1 ? 's' : '' }}</span>
            </div>
          </div>
        </RouterLink>
      </div>
    </section>

  </div>
</template>

<style scoped>
/* ── Layout ─────────────────────────────────────────────── */
.courses-page { background: #f8fafc; min-height: 100vh; }

/* ── Hero ────────────────────────────────────────────────── */
.hero {
  background: linear-gradient(135deg, #6c46ff 0%, #a855f7 100%);
  color: white;
  padding: 60px 24px 48px;
  text-align: center;
}
.hero h1 { font-size: 2.4rem; font-weight: 800; margin: 0 0 8px; }
.hero p  { font-size: 1.1rem; opacity: .85; margin: 0; }

/* ── Filters ─────────────────────────────────────────────── */
.filters-bar {
  background: white;
  border-bottom: 1px solid #e8ecf0;
  padding: 16px 24px;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.filters-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.search-wrap {
  flex: 1;
  min-width: 220px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f1f5f9;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 0 14px;
}
.search-wrap svg { color: #94a3b8; flex-shrink: 0; }
.search-wrap input {
  border: none;
  background: transparent;
  outline: none;
  width: 100%;
  font-size: .95rem;
  padding: 10px 0;
  color: #1e293b;
}
.filters-inner select {
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 14px;
  font-size: .9rem;
  background: white;
  color: #475569;
  outline: none;
  cursor: pointer;
}
.filters-inner select:focus { border-color: #6c46ff; }
.result-count { font-size: .85rem; color: #94a3b8; white-space: nowrap; margin-left: 4px; }

/* ── Grid ────────────────────────────────────────────────── */
.courses-grid-section { max-width: 1200px; margin: 0 auto; padding: 40px 24px 60px; }
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

/* ── Card ────────────────────────────────────────────────── */
.card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,.07);
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: transform .2s, box-shadow .2s;
  display: flex;
  flex-direction: column;
}
.card:hover { transform: translateY(-4px); box-shadow: 0 8px 28px rgba(108,70,255,.15); }

.card-image { position: relative; height: 180px; overflow: hidden; }
.card-image img { width: 100%; height: 100%; object-fit: cover; transition: transform .3s; }
.card:hover .card-image img { transform: scale(1.05); }

.badge {
  position: absolute;
  top: 12px; left: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: .75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .04em;
}
.badge-green  { background: #dcfce7; color: #16a34a; }
.badge-blue   { background: #dbeafe; color: #2563eb; }
.badge-orange { background: #fef3c7; color: #d97706; }

.card-body { padding: 16px 18px 20px; flex: 1; display: flex; flex-direction: column; gap: 6px; }
.card-domain { font-size: .75rem; font-weight: 600; color: #6c46ff; text-transform: uppercase; letter-spacing: .06em; }
.card-title  { font-size: 1rem; font-weight: 700; color: #1e293b; margin: 0; line-height: 1.3; }
.card-desc   { font-size: .85rem; color: #64748b; line-height: 1.45; margin: 0; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.card-instructor { display: flex; align-items: center; gap: 8px; margin-top: auto; padding-top: 8px; }
.avatar {
  width: 30px; height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6c46ff, #a855f7);
  color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: .8rem; font-weight: 700;
  flex-shrink: 0;
}
.card-instructor span { font-size: .82rem; color: #475569; font-weight: 500; }

.card-meta { display: flex; gap: 14px; font-size: .8rem; color: #94a3b8; border-top: 1px solid #f1f5f9; padding-top: 10px; }

/* ── States ──────────────────────────────────────────────── */
.loading, .empty {
  text-align: center;
  padding: 80px 24px;
  color: #94a3b8;
  font-size: 1.1rem;
}
.empty button {
  margin-top: 16px;
  padding: 10px 24px;
  background: #6c46ff;
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: .95rem;
}
.empty button:hover { background: #5a35e8; }

/* ── Responsive ──────────────────────────────────────────── */
@media (max-width: 640px) {
  .hero h1 { font-size: 1.7rem; }
  .courses-grid { grid-template-columns: 1fr; }
  .filters-inner { flex-direction: column; align-items: stretch; }
  .search-wrap { min-width: unset; }
}
</style>
