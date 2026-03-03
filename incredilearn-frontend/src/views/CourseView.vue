<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'

const route = useRoute()
const courseId = Number(route.params.id)

/* ── Image mapping by keyword ── */
const KEYWORD_IMAGES: Record<string, string> = {
  'python':        'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=900&h=500&fit=crop&q=80',
  'react':         'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=900&h=500&fit=crop&q=80',
  'vue':           'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=900&h=500&fit=crop&q=80',
  'javascript':    'https://images.unsplash.com/photo-1627398242454-45a1465c2479?w=900&h=500&fit=crop&q=80',
  'spring':        'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=900&h=500&fit=crop&q=80',
  'java':          'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=900&h=500&fit=crop&q=80',
  'design':        'https://images.unsplash.com/photo-1561070791-2526d30994b5?w=900&h=500&fit=crop&q=80',
  'marketing':     'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=900&h=500&fit=crop&q=80',
  'excel':         'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=900&h=500&fit=crop&q=80',
  'bureautique':   'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=900&h=500&fit=crop&q=80',
  'business':      'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=900&h=500&fit=crop&q=80',
  'intelligence':  'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=900&h=500&fit=crop&q=80',
  'tensorflow':    'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=900&h=500&fit=crop&q=80',
  'machine':       'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?w=900&h=500&fit=crop&q=80',
  'photo':         'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=900&h=500&fit=crop&q=80',
  'cyber':         'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=900&h=500&fit=crop&q=80',
  'securit':       'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=900&h=500&fit=crop&q=80',
  'docker':        'https://images.unsplash.com/photo-1605745341112-85968b19335b?w=900&h=500&fit=crop&q=80',
  'sql':           'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=900&h=500&fit=crop&q=80',
  'programmation': 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=900&h=500&fit=crop&q=80',
  'default':       'https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=900&h=500&fit=crop&q=80',
}

function getCourseImage(titre: string, domain: string): string {
  const text = ((titre || '') + ' ' + (domain || '')).toLowerCase()
  const key = Object.keys(KEYWORD_IMAGES).find(k => k !== 'default' && text.includes(k)) || 'default'
  return KEYWORD_IMAGES[key] as string
}

/* ── Hardcoded lessons/resources lookup for known courses ── */
const hardcodedLessons: Record<number, { lessons: { id: number; title: string; duration: string; description: string }[]; resources: { name: string; type: string; size: string }[] }> = {
  1: {
    lessons: [
      { id: 1, title: 'Introduction et installation',    duration: '15:32', description: "Python est un langage de programmation interprété, de haut niveau et polyvalent. Il met l'accent sur la lisibilité du code avec son utilisation notable de l'indentation significative." },
      { id: 2, title: 'Variables et types de données',   duration: '22:15', description: "En Python, les variables sont créées lors de l'assignation d'une valeur. Les types principaux incluent : int (entiers), float (décimaux), str (chaînes de caractères), bool (booléens), list, tuple, dict." },
      { id: 3, title: 'Structures conditionnelles',      duration: '18:45', description: "Les structures conditionnelles (if, elif, else) permettent d'exécuter différents blocs de code selon les conditions. Python utilise l'indentation pour délimiter ces blocs." },
    ],
    resources: [
      { name: 'Introduction à Python - Guide complet', type: 'PDF', size: '2.4 MB' },
    ],
  },
  2: {
    lessons: [
      { id: 1, title: 'Introduction au Design Thinking',  duration: '12:00', description: "Le Design Thinking est une approche centrée sur l'utilisateur qui résout des problèmes complexes en suivant 5 étapes : empathie, définition, idéation, prototypage et test." },
      { id: 2, title: 'Principes de UI',                  duration: '18:30', description: "Les principes fondamentaux de l'interface utilisateur incluent la hiérarchie visuelle, la cohérence, le contraste, l'alignement et l'espace négatif pour créer des designs lisibles." },
      { id: 3, title: 'Prototypage avec Figma',           duration: '25:10', description: "Figma est un outil de design collaboratif. Cette leçon couvre la création de composants réutilisables, les auto-layouts et le prototypage interactif." },
    ],
    resources: [
      { name: 'UI Kit Complet', type: 'PDF', size: '5.1 MB' },
    ],
  },
  3: {
    lessons: [
      { id: 1, title: 'Stratégie de contenu',   duration: '14:00', description: "Une stratégie de contenu efficace définit les objectifs, le public cible, les types de contenu et les canaux de distribution pour maximiser l'engagement et la conversion." },
      { id: 2, title: 'SEO & SEM',              duration: '20:00', description: "Le SEO (référencement naturel) améliore la visibilité organique tandis que le SEM (référencement payant) utilise les annonces Google Ads pour attirer du trafic qualifié." },
      { id: 3, title: 'Réseaux sociaux',        duration: '16:45', description: "Maîtrisez les algorithmes des plateformes sociales, la création de contenus viraux, la gestion de communauté et l'analyse des métriques de performance." },
    ],
    resources: [
      { name: 'Guide Marketing Digital', type: 'PDF', size: '3.2 MB' },
    ],
  },
  4: {
    lessons: [
      { id: 1, title: 'Formules avancées',           duration: '18:00', description: "Apprenez les formules complexes : RECHERCHEV, INDEX/EQUIV, SI.CONDITIONS, SOMME.SI.ENS. Ces fonctions automatisent les calculs et transforment vos feuilles de calcul en outils puissants." },
      { id: 2, title: 'Tableaux croisés dynamiques', duration: '22:00', description: "Les TCD permettent d'analyser rapidement de grandes quantités de données. Apprenez à les créer, les filtrer, les grouper et à créer des graphiques croisés dynamiques." },
      { id: 3, title: 'Macros & VBA',                duration: '30:00', description: "Le VBA (Visual Basic for Applications) permet d'automatiser des tâches répétitives dans Excel. Cette leçon couvre l'enregistrement de macros et les bases de la programmation VBA." },
    ],
    resources: [
      { name: 'Fichiers Excel exercices', type: 'PDF', size: '1.8 MB' },
    ],
  },
  10: {
    lessons: [
      { id: 1, title: 'Introduction au Machine Learning', duration: '20:00', description: "Le Machine Learning permet aux ordinateurs d'apprendre sans être explicitement programmés. Découvrez les types d'apprentissage : supervisé, non supervisé et par renforcement." },
      { id: 2, title: 'Réseaux de neurones',              duration: '25:00', description: "Les réseaux de neurones artificiels s'inspirent du cerveau humain. Apprenez les couches (input, hidden, output), les fonctions d'activation et la rétropropagation." },
      { id: 3, title: 'Entraîner un modèle',              duration: '30:00', description: "L'entraînement d'un modèle TensorFlow implique la préparation des données, la définition de l'architecture, la compilation avec un optimiseur et l'évaluation des performances." },
    ],
    resources: [
      { name: 'Guide TensorFlow', type: 'PDF', size: '4.0 MB' },
    ],
  },
  11: {
    lessons: [
      { id: 1, title: 'Composants & Props',  duration: '15:00', description: "React est basé sur des composants réutilisables. Les props permettent de passer des données du composant parent vers l'enfant, favorisant la composition et la réutilisabilité." },
      { id: 2, title: 'State & Hooks',       duration: '20:00', description: "Les hooks React (useState, useEffect, useContext) permettent d'ajouter des fonctionnalités comme l'état local et les effets de bord dans les composants fonctionnels." },
      { id: 3, title: 'React Router',        duration: '18:00', description: "React Router gère la navigation côté client. Apprenez à configurer les routes, utiliser les paramètres d'URL et protéger les routes avec l'authentification." },
    ],
    resources: [
      { name: 'React Cheat Sheet', type: 'PDF', size: '1.5 MB' },
    ],
  },
  12: {
    lessons: [
      { id: 1, title: 'Composition & Cadrage',  duration: '12:00', description: "Maîtrisez la règle des tiers, les lignes directrices, le cadrage naturel et la symétrie pour créer des compositions visuellement équilibrées et attractives." },
      { id: 2, title: 'Lumière & Exposition',   duration: '14:00', description: "Comprenez le triangle d'exposition (ISO, vitesse, ouverture) et apprenez à exploiter la lumière naturelle et artificielle pour obtenir des photos parfaitement exposées." },
      { id: 3, title: 'Retouche Lightroom',     duration: '20:00', description: "Adobe Lightroom est l'outil de référence pour la retouche photo. Découvrez les corrections de base, les courbes de tonalité, la colorimétrie et l'export professionnel." },
    ],
    resources: [
      { name: 'Guide Photo Créative', type: 'PDF', size: '2.9 MB' },
    ],
  },
  13: {
    lessons: [
      { id: 1, title: 'Introduction à la cybersécurité', duration: '16:00', description: "La cybersécurité protège les systèmes informatiques contre les cybermenaces. Découvrez les types d'attaques (phishing, malware, DDoS) et les principes de défense en profondeur." },
      { id: 2, title: 'Cryptographie',                   duration: '22:00', description: "La cryptographie garantit la confidentialité et l'intégrité des données. Apprenez le chiffrement symétrique (AES), asymétrique (RSA) et les fonctions de hachage (SHA-256)." },
      { id: 3, title: 'Ethical Hacking',                 duration: '28:00', description: "Le test de pénétration éthique identifie les vulnérabilités avant les attaquants. Cette leçon couvre la reconnaissance, l'exploitation et le rapport des failles de sécurité." },
    ],
    resources: [
      { name: 'Guide Cybersécurité', type: 'PDF', size: '3.7 MB' },
    ],
  },
}

/* ── Default lessons/resources for courses without hardcoded content ── */
function getDefaultContent(title: string) {
  return {
    lessons: [
      { id: 1, title: 'Introduction au cours',     duration: '15:00', description: `Découvrez les objectifs, le plan et les prérequis de ce cours : ${title}.` },
      { id: 2, title: 'Concepts fondamentaux',       duration: '20:00', description: 'Exploration des notions essentielles et des principes de base du domaine.' },
      { id: 3, title: 'Mise en pratique',           duration: '25:00', description: 'Application concrète des concepts à travers des exercices et des projets pratiques.' },
    ],
    resources: [
      { name: 'Support de cours', type: 'PDF', size: '1.0 MB' },
    ],
  }
}

/* ── Course state (fetched from API) ── */
interface CourseData {
  id: number
  title: string
  instructor: string
  category: string
  image: string
  lessons: { id: number; title: string; duration: string; description: string }[]
  resources: { name: string; type: string; size: string; downloadUrl?: string }[]
}

interface ContentFile {
  id: number
  fileName: string
  contentType: string // "Video" | "Document"
  mimeType: string
  fileSize: number
  sortOrder: number
  downloadUrl: string
}

const course = ref<CourseData | null>(null)
const loading = ref(true)
const uploadedContents = ref<ContentFile[]>([])

function formatFileSize(bytes: number): string {
  if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  if (bytes >= 1024) return Math.round(bytes / 1024) + ' KB'
  return bytes + ' B'
}

onMounted(async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/courses/${courseId}`)
    if (!res.ok) throw new Error('Cours introuvable')
    const data = await res.json()

    // Fetch uploaded course contents from API
    let apiContents: ContentFile[] = []
    try {
      const contentRes = await fetch(`http://localhost:8080/api/courses/content/${courseId}`)
      if (contentRes.ok) {
        apiContents = await contentRes.json()
        uploadedContents.value = apiContents
      }
    } catch { /* ignore */ }

    // Build lessons: use hardcoded if available, or uploaded videos, or defaults
    const hardcoded = hardcodedLessons[courseId]
    let lessons: CourseData['lessons']
    let resources: CourseData['resources']

    if (hardcoded) {
      lessons = hardcoded.lessons
      resources = hardcoded.resources
    } else {
      // Use uploaded videos as lessons
      const uploadedVideos = apiContents.filter(c => c.contentType === 'Video')
      const uploadedDocs = apiContents.filter(c => c.contentType === 'Document')

      if (uploadedVideos.length > 0) {
        lessons = uploadedVideos.map((v, i) => ({
          id: v.id,
          title: v.fileName.replace(/\.[^.]+$/, ''),
          duration: '-',
          description: `Vidéo du cours : ${v.fileName}`,
        }))
      } else {
        lessons = getDefaultContent(data.titre).lessons
      }

      if (uploadedDocs.length > 0) {
        resources = uploadedDocs.map(d => ({
          name: d.fileName,
          type: d.mimeType?.includes('pdf') ? 'PDF' : 'Document',
          size: formatFileSize(d.fileSize),
          downloadUrl: d.downloadUrl,
        }))
      } else if (!hardcoded) {
        resources = getDefaultContent(data.titre).resources
      } else {
        resources = []
      }
    }

    // If there are uploaded docs, add them to resources even for hardcoded courses
    if (hardcoded && apiContents.length > 0) {
      const uploadedDocs = apiContents.filter(c => c.contentType === 'Document')
      const uploadedVideos = apiContents.filter(c => c.contentType === 'Video')
      for (const d of uploadedDocs) {
        resources.push({
          name: d.fileName,
          type: d.mimeType?.includes('pdf') ? 'PDF' : 'Document',
          size: formatFileSize(d.fileSize),
          downloadUrl: d.downloadUrl,
        })
      }
      for (const v of uploadedVideos) {
        lessons.push({
          id: v.id,
          title: v.fileName.replace(/\.[^.]+$/, ''),
          duration: '-',
          description: `Vidéo du cours : ${v.fileName}`,
        })
      }
    }

    course.value = {
      id: data.id,
      title: data.titre,
      instructor: data.enseignantNom || 'Instructeur',
      category: data.domain || '',
      image: data.imageUrl ? 'http://localhost:8080' + data.imageUrl : getCourseImage(data.titre, data.domain),
      lessons,
      resources,
    }
  } catch (e) {
    console.error('Erreur chargement cours', e)
  } finally {
    loading.value = false
  }
})

const activeLesson  = ref(0)

/* ── Compute video URL for active lesson (if uploaded video) ── */
const activeLessonVideoUrl = computed(() => {
  if (!course.value) return null
  const lesson = course.value.lessons[activeLesson.value]
  if (!lesson) return null
  // Check if this lesson id matches an uploaded video
  const vid = uploadedContents.value.find(c => c.contentType === 'Video' && c.id === lesson.id)
  if (vid) return 'http://localhost:8080' + vid.downloadUrl
  return null
})
const activeTab     = ref<'resources' | 'summary' | 'discussion' | 'live'>('resources')
const selectedSections = ref<number[]>([])
const generatedSummary = ref('')
const isGenerating = ref(false)

function toggleSection(idx: number) {
  const i = selectedSections.value.indexOf(idx)
  if (i === -1) selectedSections.value.push(idx)
  else selectedSections.value.splice(i, 1)
}
function selectAll() {
  if (!course.value) return
  if (selectedSections.value.length === course.value.lessons.length)
    selectedSections.value = []
  else
    selectedSections.value = course.value.lessons.map((_, i) => i)
}
function generateSummary() {
  if (selectedSections.value.length === 0) return
  isGenerating.value = true
  generatedSummary.value = ''
  setTimeout(() => {
    isGenerating.value = false
    generatedSummary.value = `Ce résumé couvre ${selectedSections.value.length} section(s) du cours. Les leçons sélectionnées abordent les concepts fondamentaux et avancés du domaine, en progressant du général au spécifique. Chaque section renforce les notions précédentes et prépare aux applications pratiques.`
  }, 1800)
}
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
  <!-- Loading -->
  <div v-if="loading" class="course-loading">
    <div class="loading-spinner"></div>
    <p>Chargement du cours...</p>
  </div>

  <!-- Course not found -->
  <div v-else-if="!course" class="course-loading">
    <p>Ce cours est introuvable.</p>
    <RouterLink to="/" class="back-home-link">Retour à l'accueil</RouterLink>
  </div>

  <div v-else class="course-page">

    <!-- ── Top bar ── -->
    <div class="topbar">
      <RouterLink to="/" class="back-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
        Back to courses
      </RouterLink>
      <h2 class="topbar-course-name">{{ course.title }}</h2>
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
          <!-- Uploaded video player -->
          <video
            v-if="activeLessonVideoUrl"
            class="video-thumb"
            :src="activeLessonVideoUrl"
            controls
            style="object-fit: contain; background: #000;"
          />
          <!-- Fallback image thumbnail -->
          <template v-else>
            <img :src="course.image" :alt="course.title" class="video-thumb" />
            <button class="play-btn" aria-label="Lire la leçon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
                <polygon points="5 3 19 12 5 21 5 3"/>
              </svg>
            </button>
          </template>
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
            :class="{ 'tab-btn--active': activeTab === 'summary' }"
            @click="activeTab = 'summary'"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            Résumé IA
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
            Questions
          </button>
          <button
            class="tab-btn"
            :class="{ 'tab-btn--active': activeTab === 'live' }"
            @click="activeTab = 'live'"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="2"/>
              <path d="M16.24 7.76a6 6 0 0 1 0 8.49"/>
              <path d="M7.76 7.76a6 6 0 0 0 0 8.49"/>
              <path d="M19.07 4.93a10 10 0 0 1 0 14.14"/>
              <path d="M4.93 4.93a10 10 0 0 0 0 14.14"/>
            </svg>
            Session Live
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
            <a
              v-if="res.downloadUrl"
              :href="'http://localhost:8080' + res.downloadUrl"
              class="dl-btn"
              target="_blank"
              download
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="7 10 12 15 17 10"/>
                <line x1="12" y1="15" x2="12" y2="3"/>
              </svg>
              Télécharger
            </a>
            <button v-else class="dl-btn">
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

        <!-- Résumé IA -->
        <div v-else-if="activeTab === 'summary'" class="summary-panel">
          <div class="summary-header">
            <div class="summary-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="#10B981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
            </div>
            <div>
              <p class="summary-title">Résumé IA</p>
              <p class="summary-sub">Générez un résumé synthétique du cours</p>
            </div>
          </div>

          <div v-if="generatedSummary" class="summary-result">
            <p>{{ generatedSummary }}</p>
          </div>
          <template v-else>
            <div class="summary-select-header">
              <span class="summary-select-label">Sélectionnez le contenu à résumer</span>
              <button class="summary-select-all" @click="selectAll">
                {{ selectedSections.length === course.lessons.length ? 'Tout désélectionner' : 'Tout sélectionner' }}
              </button>
            </div>

            <div class="summary-items">
              <label
                v-for="(lesson, idx) in course.lessons"
                :key="lesson.id"
                class="summary-item"
                :class="{ 'summary-item--checked': selectedSections.includes(idx) }"
              >
                <input
                  type="checkbox"
                  class="summary-checkbox"
                  :checked="selectedSections.includes(idx)"
                  @change="toggleSection(idx)"
                />
                <div class="summary-item-body">
                  <p class="summary-item-title">{{ lesson.title }}</p>
                  <p class="summary-item-desc">{{ lesson.description ?? 'Contenu de la leçon ' + (idx + 1) }}</p>
                </div>
              </label>
            </div>

            <p class="summary-count">{{ selectedSections.length }} section(s) sélectionnée(s)</p>
          </template>

          <button
            class="summary-generate-btn"
            :disabled="selectedSections.length === 0 || isGenerating"
            @click="generateSummary"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
              :class="{ 'spin': isGenerating }">
              <path d="M12 2a10 10 0 0 1 10 10"/>
              <polyline points="22 12 18 16 14 12"/>
            </svg>
            {{ isGenerating ? 'Génération en cours...' : 'Générer le résumé' }}
          </button>
        </div>

        <!-- Discussion -->
        <div v-else-if="activeTab === 'discussion'" class="discussion-empty">
          <p>Aucun commentaire pour le moment. Soyez le premier à participer !</p>
        </div>

        <!-- Session Live -->
        <div v-else-if="activeTab === 'live'" class="live-panel">
          <div class="live-banner">
            <div class="live-dot"></div>
            <span>Aucune session live programmée pour le moment</span>
          </div>
          <p class="live-desc">Les sessions live vous permettent d'interagir en temps réel avec votre instructeur. Revenez bientôt !</p>
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
.topbar-course-name {
  flex: 1;
  text-align: center;
  font-size: 1.35rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.01em;
}
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

/* ── Summary ── */
.summary-panel { display: flex; flex-direction: column; gap: 1rem; }
.summary-header { display: flex; align-items: flex-start; gap: 0.75rem; }
.summary-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: #ECFDF5; display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.summary-title { font-size: 0.9rem; font-weight: 700; color: #111827; margin: 0 0 2px; }
.summary-sub   { font-size: 0.78rem; color: #9CA3AF; margin: 0; }
.summary-select-header {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 0.82rem;
}
.summary-select-label { font-weight: 600; color: #374151; }
.summary-select-all {
  background: none; border: none; color: #10B981; font-size: 0.82rem;
  font-weight: 500; cursor: pointer; padding: 0;
}
.summary-select-all:hover { text-decoration: underline; }
.summary-items { display: flex; flex-direction: column; gap: 0.5rem; }
.summary-item {
  display: flex; align-items: flex-start; gap: 0.75rem;
  padding: 0.7rem 0.9rem;
  border: 1.5px solid #E5E7EB; border-radius: 10px;
  cursor: pointer; transition: border-color 0.15s, background 0.15s;
  background: #fff;
}
.summary-item--checked { border-color: #10B981; background: #ECFDF5; }
.summary-checkbox { margin-top: 2px; accent-color: #10B981; flex-shrink: 0; }
.summary-item-title { font-size: 0.85rem; font-weight: 600; color: #111827; margin: 0 0 2px; }
.summary-item-desc  { font-size: 0.75rem; color: #6B7280; margin: 0; line-height: 1.4; }
.summary-count { font-size: 0.78rem; color: #6B7280; margin: 0; }
.summary-result {
  background: #ECFDF5; border: 1.5px solid #A7F3D0; border-radius: 10px;
  padding: 1rem 1.1rem;
}
.summary-result p { font-size: 0.875rem; color: #064E3B; line-height: 1.6; margin: 0; }
.summary-generate-btn {
  display: flex; align-items: center; justify-content: center; gap: 0.5rem;
  width: 100%; padding: 0.8rem; border-radius: 10px;
  background: #10B981; color: #fff; border: none;
  font-size: 0.9rem; font-weight: 700; cursor: pointer;
  transition: background 0.15s, opacity 0.15s;
  font-family: inherit;
}
.summary-generate-btn:hover:not(:disabled) { background: #059669; }
.summary-generate-btn:disabled { opacity: 0.55; cursor: not-allowed; }
@keyframes spin-anim { to { transform: rotate(360deg); } }
.spin { animation: spin-anim 1s linear infinite; }

/* ── Live ── */
.live-panel { display: flex; flex-direction: column; gap: 0.85rem; padding: 0.5rem 0; }
.live-banner {
  display: flex; align-items: center; gap: 0.6rem;
  padding: 0.75rem 1rem; border-radius: 10px;
  background: #FFF7ED; border: 1.5px solid #FED7AA;
  font-size: 0.875rem; font-weight: 600; color: #92400E;
}
.live-dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: #F97316; flex-shrink: 0;
  box-shadow: 0 0 0 3px rgba(249,115,22,0.25);
}
.live-desc { font-size: 0.82rem; color: #6B7280; margin: 0; line-height: 1.5; }

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

/* Loading state */
.course-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 1rem;
  color: #6B7280;
  font-size: 0.95rem;
}
.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #E5E7EB;
  border-top-color: #10B981;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.back-home-link {
  color: #10B981;
  font-weight: 600;
  text-decoration: none;
}
.back-home-link:hover { text-decoration: underline; }
</style>
