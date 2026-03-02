<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink } from 'vue-router'
import StarPolygon from '@/components/StarPolygon.vue'

const searchQuery  = ref('')
const selectedCat  = ref('Tous')
const selectedLvl  = ref('Tous les niveaux')

const categories = ['Tous', 'Programmation', 'Design', 'Marketing', 'Bureautique', 'Business']
const levels     = ['Tous les niveaux', 'Débutant', 'Intermédiaire', 'Avancé']
const minRating = ref(0)
const starDropdownOpen = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

const starOptions = [0, 3, 3.5, 4, 4.5, 5]

function selectRating(value: number) {
  minRating.value = value
  starDropdownOpen.value = false
}

function getRatingLabel(rating: number) {
  return rating === 0 ? "Toutes les notes" : `${rating} étoiles`
}

function handleClickOutside(event: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    starDropdownOpen.value = false
  }
}

function resetFilters() {
  searchQuery.value = ''
  selectedCat.value = 'Tous'
  selectedLvl.value = 'Tous les niveaux'
  minRating.value = 0
  starDropdownOpen.value = false
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

interface CourseItem {
  id: number
  title: string
  instructor: string
  description: string
  rating: number
  duration: string
  students: string
  badge: string
  badgeClass: string
  image: string
  domain: string
}

const allCourses = ref<CourseItem[]>([])

const DOMAIN_IMAGES: Record<string, string> = {
  'informatique': 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=420&h=220&fit=crop&q=80',
  'programmation': 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=420&h=220&fit=crop&q=80',
  'design': 'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=420&h=220&fit=crop&q=80',
  'marketing': 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=420&h=220&fit=crop&q=80',
  'bureautique': 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=420&h=220&fit=crop&q=80',
  'business': 'https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=420&h=220&fit=crop&q=80',
  'mathematiques': 'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=420&h=220&fit=crop&q=80',
  'physique': 'https://images.unsplash.com/photo-1636466497217-26a8cbeaf0aa?w=420&h=220&fit=crop&q=80',
  'langues': 'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=420&h=220&fit=crop&q=80',
  'default': 'https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=420&h=220&fit=crop&q=80',
}

function getCourseImage(domain: string): string {
  const key = (domain || '').toLowerCase().split(' ')[0].split('&')[0].trim()
  return DOMAIN_IMAGES[key] || DOMAIN_IMAGES['default']
}

function getBadgeClass(niveau: string): string {
  if (niveau === 'Débutant') return 'badge-green'
  if (niveau === 'Avancé') return 'badge-orange'
  return 'badge-blue'
}

function mapCourse(c: any): CourseItem {
  return {
    id: c.id,
    title: c.titre,
    instructor: c.enseignantNom || 'Instructeur',
    description: c.description || '',
    rating: 4.7,
    duration: c.dureeHeures ? `${c.dureeHeures}h` : '-',
    students: c.nombreEtudiants != null ? String(c.nombreEtudiants) : '0',
    badge: c.niveau || 'Débutant',
    badgeClass: getBadgeClass(c.niveau),
    image: getCourseImage(c.domain),
    domain: c.domain || '',
  }
}

function normalize(text: string): string {
  return text
    .toLowerCase()
    .normalize("NFD")                 // Sépare les accents
    .replace(/[\u0300-\u036f]/g, "")  // Supprime les accents
    .trim()
}

onMounted(async () => {
  try {
    const res = await fetch('http://localhost:8080/api/courses')
    const data = await res.json()
    allCourses.value = data.map(mapCourse)
    data.forEach(c => {
      console.log(
        `Titre: ${c.titre} | Auteur: ${c.enseignantNom} | Domaine: ${c.domain}`
      )
    })
  } catch (e) {
    console.error('Erreur chargement cours', e)
  }
})

const filteredCourses = computed(() => {

  const q = normalize(searchQuery.value)

  return allCourses.value.filter(c => {

    // Texte searchable combiné
    const searchableText = normalize(
      [
        c.title,
        c.instructor,
        c.description,
        c.domain
      ].join(" ")
    )

    // Recherche multi-mots (AND logique)
    const matchesSearch = !q || (() => {
      const words = q.split(/\s+/).filter(Boolean)
      return words.every(word =>
        searchableText.includes(word)
      )
    })()

    // Niveau
    const matchesLvl =
      selectedLvl.value === 'Tous les niveaux' ||
      c.badge === selectedLvl.value

    // Catégorie
    const matchesCat =
      selectedCat.value === 'Tous' ||
      normalize(c.domain).includes(normalize(selectedCat.value))

    // Note minimale
    const matchesRating =
      c.rating >= minRating.value

    return matchesSearch && matchesLvl && matchesCat && matchesRating
  })
})

const courses = computed(() => filteredCourses.value)
const newCourses = computed(() => filteredCourses.value.slice(0, 4))
const recommendedCourses = computed(() => filteredCourses.value.slice(0, 4))
</script>

<template>
  <main>
    <!-- ══════════════════ HERO ══════════════════ -->
    <section class="hero">
      <div class="hero-inner">
        <h1 class="hero-title">
          Explorez nos <span class="hero-accent">cours</span>
        </h1>
        <p class="hero-sub">
          Découvrez des centaines de cours créés par des experts pour vous aider à atteindre
          vos objectifs.
        </p>

        <!-- Search bar -->
        <div class="search-bar">
          <!-- Text input -->
          <div class="search-input-wrap">
            <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none"
              stroke="#9CA3AF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
            <input
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="Rechercher par titre, enseignant ou mots-clés..."
            />
          </div>

          <!-- Category -->
          <div class="search-divider"></div>
          <div class="select-wrap">
            <select v-model="selectedCat" class="search-select">
              <option v-for="c in categories" :key="c">{{ c }}</option>
            </select>
            <svg class="select-chevron" width="14" height="14" viewBox="0 0 24 24" fill="none"
              stroke="#6B7280" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>

          <!-- Level -->
          <div class="search-divider"></div>
          <div class="select-wrap select-wrap-level">
            <select v-model="selectedLvl" class="search-select">
              <option v-for="l in levels" :key="l">{{ l }}</option>
            </select>
            <svg class="select-chevron" width="14" height="14" viewBox="0 0 24 24" fill="none"
              stroke="#6B7280" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>

          <!-- Rating -->
           <div class="search-divider"></div>
          <div class="select-wrap rating-select" ref="dropdownRef">
          
          <div class="star-dropdown" @click="starDropdownOpen = !starDropdownOpen">
            {{ getRatingLabel(minRating) }}
            <svg class="select-chevron" width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="#6B7280" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>

          
          <div v-if="starDropdownOpen" class="star-options">
            <div v-for="value in starOptions" :key="value" class="option" @click="selectRating(value)">
              <div class="stars-wrapper" style="display:flex; gap:2px;">
                <StarPolygon
                  v-for="i in 5"
                  :key="i"
                  :rating="Math.min(Math.max(value - i + 1, 0), 1)"
                />
              </div>
            </div>
          </div>
        </div>

          <!-- Filter button -->
          <div class="search-divider"></div>
          <button class="filter-btn" aria-label="Filtres avancés" @click="resetFilters">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </div>
    </section>

    <!-- ══════════════════ POPULAR COURSES ══════════════════ -->
    <section class="courses-section">
      <div class="section-inner">
        <!-- Section header -->
        <div class="section-header">
          <div class="section-title-group">
            <div class="section-icon">
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none"
                stroke="#10B981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
            </div>
            <div>
              <h2 class="section-title">Cours populaires</h2>
              <p class="section-sub">Les cours les plus appréciés par notre communauté</p>
            </div>
          </div>
          <a href="#" class="voir-tout">Voir tout &rsaquo;</a>
        </div>

        <!-- Cards grid -->
        <div class="courses-grid">
          <RouterLink
            v-for="course in courses"
            :key="course.id"
            :to="'/cours/' + course.id"
            class="card-link"
          >
          <article class="card">
            <div class="card-img-wrap">
              <img :src="course.image" :alt="course.title" class="card-img" />
              <span class="card-badge" :class="course.badgeClass">{{ course.badge }}</span>
            </div>

            <div class="card-body">
              <h3 class="card-title">{{ course.title }}</h3>
              <p class="card-instructor">{{ course.instructor }}</p>
              <p class="card-desc">{{ course.description }}</p>

              <div class="card-meta">
                <!-- Rating -->
                <span class="meta-rating">
                  <svg width="13" height="13" viewBox="0 0 24 24"
                    fill="#F59E0B" stroke="#F59E0B" stroke-width="0.5">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                  {{ course.rating }}
                </span>
                <span class="meta-dot">•</span>
                <!-- Duration -->
                <span class="meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                    stroke="#6B7280" stroke-width="2" stroke-linecap="round">
                    <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                  </svg>
                  {{ course.duration }}
                </span>
                <span class="meta-dot">•</span>
                <!-- Students -->
                <span class="meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                    stroke="#6B7280" stroke-width="2" stroke-linecap="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                  {{ course.students }}
                </span>
              </div>
            </div>
          </article>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- ══════════════════ NOUVEAUTÉS ══════════════════ -->
    <section class="courses-section courses-section--white">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-title-group">
            <div class="section-icon section-icon--teal">
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none"
                stroke="#10B981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z"/>
              </svg>
            </div>
            <div>
              <h2 class="section-title">Nouveautés</h2>
              <p class="section-sub">Découvrez les derniers cours ajoutés</p>
            </div>
          </div>
          <a href="#" class="voir-tout">Voir tout &rsaquo;</a>
        </div>
        <div class="courses-grid">
          <RouterLink v-for="course in newCourses" :key="course.id" :to="'/cours/' + course.id" class="card-link">
            <article class="card">
            <div class="card-img-wrap">
              <img :src="course.image" :alt="course.title" class="card-img" />
              <span class="card-badge" :class="course.badgeClass">{{ course.badge }}</span>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ course.title }}</h3>
              <p class="card-instructor">{{ course.instructor }}</p>
              <p class="card-desc">{{ course.description }}</p>
              <div class="card-meta">
                <span class="meta-rating">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="#F59E0B" stroke="#F59E0B" stroke-width="0.5">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                  {{ course.rating }}
                </span>
                <span class="meta-dot">•</span>
                <span class="meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="2" stroke-linecap="round">
                    <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                  </svg>
                  {{ course.duration }}
                </span>
                <span class="meta-dot">•</span>
                <span class="meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="2" stroke-linecap="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                  {{ course.students }}
                </span>
              </div>
            </div>
            </article>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- ══════════════════ RECOMMANDÉS ══════════════════ -->
    <section class="courses-section">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-title-group">
            <div class="section-icon section-icon--cyan">
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none"
                stroke="#10B981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
                <line x1="9" y1="9" x2="9.01" y2="9"/>
                <line x1="15" y1="9" x2="15.01" y2="9"/>
              </svg>
            </div>
            <div>
              <h2 class="section-title">Recommandés pour vous</h2>
              <p class="section-sub">Basés sur les meilleures notes</p>
            </div>
          </div>
          <a href="#" class="voir-tout">Voir tout &rsaquo;</a>
        </div>
        <div class="courses-grid">
          <RouterLink v-for="course in recommendedCourses" :key="course.id" :to="'/cours/' + course.id" class="card-link">
            <article class="card">
            <div class="card-img-wrap">
              <img :src="course.image" :alt="course.title" class="card-img" />
              <span class="card-badge" :class="course.badgeClass">{{ course.badge }}</span>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ course.title }}</h3>
              <p class="card-instructor">{{ course.instructor }}</p>
              <p class="card-desc">{{ course.description }}</p>
              <div class="card-meta">
                <span class="meta-rating">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="#F59E0B" stroke="#F59E0B" stroke-width="0.5">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                  {{ course.rating }}
                </span>
                <span class="meta-dot">•</span>
                <span class="meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="2" stroke-linecap="round">
                    <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                  </svg>
                  {{ course.duration }}
                </span>
                <span class="meta-dot">•</span>
                <span class="meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="2" stroke-linecap="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                  {{ course.students }}
                </span>
              </div>
            </div>
            </article>
          </RouterLink>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
/* ───────── CARD LINK ───────── */
.card-link {
  text-decoration: none;
  color: inherit;
  display: block;
}
.card-link:hover .card { box-shadow: 0 8px 24px rgba(0,0,0,0.12); transform: translateY(-3px); }

/* ───────── HERO ───────── */
.hero {
  background: linear-gradient(to bottom, #ecfdf5 0%, #f0fdf8 40%, #ffffff 100%);
  padding: 4.5rem 2rem 3.5rem;
  text-align: center;
}
.hero-inner {
  max-width: 860px;
  margin: 0 auto;
}
.hero-title {
  font-size: 2.6rem;
  font-weight: 800;
  color: #111827;
  line-height: 1.2;
  margin-bottom: 1rem;
}
.hero-accent { color: #10B981; }
.hero-sub {
  font-size: 1rem;
  color: #6B7280;
  max-width: 520px;
  margin: 0 auto 2.25rem;
  line-height: 1.65;
}

/* ── Search Bar ── */
.search-bar {
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1.5px solid #E5E7EB;
  border-radius: 12px;
  padding: 0.45rem 0.75rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
  gap: 0.5rem;
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
}
.search-icon { flex-shrink: 0; }
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 0.875rem;
  color: #374151;
  background: transparent;
  min-width: 0;
}
.search-input::placeholder { color: #9CA3AF; }

.search-divider {
  width: 1px;
  height: 28px;
  background: #E5E7EB;
  flex-shrink: 0;
}

.select-wrap {
  position: relative;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.select-wrap-level { min-width: 150px; }
.search-select {
  appearance: none;
  -webkit-appearance: none;
  border: none;
  outline: none;
  font-size: 0.875rem;
  color: #374151;
  background: transparent;
  cursor: pointer;
  padding: 0.25rem 1.4rem 0.25rem 0.5rem;
}
.select-chevron {
  position: absolute;
  right: 0;
  pointer-events: none;
}

.filter-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  padding: 0.3rem 0.35rem;
  border-radius: 6px;
  transition: background 0.15s;
  flex-shrink: 0;
}
.filter-btn:hover { background: #F3F4F6; }

/* ───────── COURSES SECTION ───────── */
.courses-section {
  background: #ffffff;
  padding: 3rem 2rem 4.5rem;
}
.courses-section--white {
  background: #ffffff;
}
.section-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 1.75rem;
}
.section-title-group {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}
.section-icon {
  width: 38px;
  height: 38px;
  background: #D1FAE5;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 3px;
}
.section-icon--teal  { background: #CCFBF1; }
.section-icon--cyan  { background: #CFFAFE; }
.section-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.2rem;
}
.section-sub {
  font-size: 0.82rem;
  color: #6B7280;
}
.voir-tout {
  color: #10B981;
  font-size: 0.88rem;
  font-weight: 600;
  padding-top: 5px;
  transition: opacity 0.15s;
}
.voir-tout:hover { opacity: 0.75; }

/* ───────── GRID ───────── */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.25rem;
}

/* ───────── CARD ───────── */
.card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
  transition: box-shadow 0.2s, transform 0.2s;
  cursor: pointer;
}
.card:hover {
  box-shadow: 0 8px 24px rgba(0,0,0,0.11);
  transform: translateY(-3px);
}
.card-img-wrap { position: relative; }
.card-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}
.card-badge {
  position: absolute;
  top: 0.6rem;
  left: 0.6rem;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.18rem 0.55rem;
  border-radius: 20px;
  letter-spacing: 0.02em;
}
.badge-green  { background: #10B981; color: #fff; }
.badge-blue   { background: #3B82F6; color: #fff; }
.badge-orange { background: #EA580C; color: #fff; }
.badge-red    { background: #DC2626; color: #fff; }

.card-body { padding: 1rem; }

.card-title {
  font-size: 0.93rem;
  font-weight: 700;
  color: #111827;
  line-height: 1.35;
  margin-bottom: 0.3rem;
}
.card-instructor {
  font-size: 0.8rem;
  color: #6B7280;
  margin-bottom: 0.45rem;
}
.card-desc {
  font-size: 0.8rem;
  color: #4B5563;
  line-height: 1.55;
  margin-bottom: 0.85rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.8rem;
  color: #6B7280;
  flex-wrap: wrap;
}
.meta-rating {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-weight: 600;
  color: #374151;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 0.2rem;
}
.meta-dot { color: #D1D5DB; }

.rating-select {
  min-width: 150px;
  position: relative;
}

.star-dropdown {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  padding: 0.35rem 0.75rem;
  font-size: 0.875rem;
  color: #374151;
}

.arrow {
  font-size: 0.7rem;
  color: #6B7280;
  margin-left: 0.25rem;
}

.star-options {
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  margin-top: 2px;
  z-index: 10;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  min-width: 100px;
  padding: 0.25rem 0;
}

.option {
  padding: 0.35rem 0.75rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.option:hover {
  background: #F3F4F6;
}

/* ── Responsive ── */
@media (max-width: 1100px) {
  .courses-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .hero-title { font-size: 1.9rem; }
  .courses-grid { grid-template-columns: 1fr; }
  .search-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }
  .search-divider { display: none; }
  .select-wrap { width: 100%; }
  .search-select { width: 100%; }
}
</style>
