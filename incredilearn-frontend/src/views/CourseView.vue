<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ConfirmModal from '@/components/ConfirmModal.vue'

// ── Custom modal state ──
const modalVisible = ref(false)
const modalMessage = ref('')
const modalTitle = ref('')
const modalVariant = ref<'danger' | 'warning' | 'info'>('info')
const modalType = ref<'confirm' | 'alert'>('confirm')
const modalConfirmText = ref('Confirmer')
const modalCancelText = ref('Annuler')
let modalResolve: ((v: boolean) => void) | null = null

function showModal(opts: {
  message: string
  title?: string
  variant?: 'danger' | 'warning' | 'info'
  type?: 'confirm' | 'alert'
  confirmText?: string
  cancelText?: string
}): Promise<boolean> {
  modalMessage.value = opts.message
  modalTitle.value = opts.title ?? ''
  modalVariant.value = opts.variant ?? 'info'
  modalType.value = opts.type ?? 'confirm'
  modalConfirmText.value = opts.confirmText ?? 'Confirmer'
  modalCancelText.value = opts.cancelText ?? 'Annuler'
  modalVisible.value = true
  return new Promise(resolve => { modalResolve = resolve })
}
function onModalConfirm() {
  modalVisible.value = false
  modalResolve?.(true)
  modalResolve = null
}
function onModalCancel() {
  modalVisible.value = false
  modalResolve?.(false)
  modalResolve = null
}

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
  instructorId: number
  category: string
  description: string
  niveau: string
  dureeHeures: number
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
      instructorId: data.enseignantId,
      category: data.domain || '',
      description: data.description || '',
      niveau: data.niveau || '',
      dureeHeures: data.dureeHeures || 0,
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

const authStore = useAuthStore()
const isEnrolled   = ref(false)
const enrollLoading = ref(false)
const enrollMsg     = ref('')

// ── Star rating ──────────────────────────────────────────
const userRating  = ref(0)   // 0 = not rated yet
const hoverRating = ref(0)

function setRating(stars: number) {
  userRating.value = stars
  const userId = authStore.user?.id
  if (!userId) return
  localStorage.setItem(`incredilearn_rating_${userId}_${courseId}`, String(stars))
}
// ─────────────────────────────────────────────────────────

onMounted(async () => {
  if (!authStore.user?.id) return
  // Restore completed lessons from localStorage
  const saved = localStorage.getItem(`incredilearn_lessons_${authStore.user.id}_${courseId}`)
  if (saved) completedLessons.value = JSON.parse(saved)
  // Restore saved rating
  const savedRating = localStorage.getItem(`incredilearn_rating_${authStore.user.id}_${courseId}`)
  if (savedRating) userRating.value = Number(savedRating)
  // Load questions
  loadQuestions()
  try {
    const res = await fetch(`http://localhost:8080/api/courses/user/${authStore.user.id}`)
    if (res.ok) {
      const enrolled: { id: number }[] = await res.json()
      isEnrolled.value = enrolled.some(c => c.id === courseId)
    }
  } catch { /* silently ignore */ }
})

async function enroll() {
  if (!authStore.user?.id || isEnrolled.value || enrollLoading.value) return
  enrollLoading.value = true
  try {
    const res = await fetch(
      `http://localhost:8080/api/courses/enroll/${authStore.user.id}/${courseId}`,
      { method: 'POST' }
    )
    if (res.ok) {
      isEnrolled.value = true
      enrollMsg.value = 'Inscrit avec succès !'
      setTimeout(() => { enrollMsg.value = '' }, 3000)
    } else {
      enrollMsg.value = 'Erreur lors de l\'inscription'
      setTimeout(() => { enrollMsg.value = '' }, 3000)
    }
  } catch {
    enrollMsg.value = 'Erreur de connexion'
    setTimeout(() => { enrollMsg.value = '' }, 3000)
  } finally {
    enrollLoading.value = false
  }
}

async function unenroll() {
  if (!authStore.user?.id || !isEnrolled.value || enrollLoading.value) return
  const ok = await showModal({ message: 'Se désinscrire de ce cours ?', title: 'Désinscription', variant: 'warning', confirmText: 'Se désinscrire' })
  if (!ok) return
  enrollLoading.value = true
  try {
    const res = await fetch(
      `http://localhost:8080/api/courses/enroll/${authStore.user.id}/${courseId}`,
      { method: 'DELETE' }
    )
    if (res.ok) {
      isEnrolled.value = false
      enrollMsg.value = 'Désinscription effectuée'
      setTimeout(() => { enrollMsg.value = '' }, 3000)
    } else {
      enrollMsg.value = 'Erreur lors de la désinscription'
      setTimeout(() => { enrollMsg.value = '' }, 3000)
    }
  } catch {
    enrollMsg.value = 'Erreur de connexion'
    setTimeout(() => { enrollMsg.value = '' }, 3000)
  } finally {
    enrollLoading.value = false
  }
}

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
const activeTab     = ref<'resources' | 'summary' | 'discussion' | 'live' | 'quiz'>('resources')
const selectedSections = ref<number[]>([])
const generatedSummary = ref('')
const isGenerating = ref(false)
const summaryError = ref('')

/* ── Quiz state ── */
interface QuizOptionI { id: number; texte: string; correct?: boolean }
interface QuizQuestionI { id: number; texte: string; sortOrder: number; options: QuizOptionI[] }
interface QuizI { id: number; titre: string; description: string; courseId: number; nombreQuestions: number; createdAt: string; questions?: QuizQuestionI[] }
interface QuizResult { total: number; correct: number; score: number; results: { questionId: number; correct: boolean; correctOptionId: number; selectedOptionId: number }[] }
interface QuizAttemptI { id: number; quizId: number; quizTitre: string; userId: number; userName: string; totalQuestions: number; correctAnswers: number; score: number; completedAt: string }

const quizzes = ref<QuizI[]>([])
const activeQuiz = ref<QuizI | null>(null)
const quizAnswers = ref<Record<number, number>>({})
const quizResult = ref<QuizResult | null>(null)
const quizLoading = ref(false)
const quizSubmitting = ref(false)

// Tentatives existantes (quizId → attempt)
const quizAttempts = ref<Record<number, QuizAttemptI>>({})

// Vue notes enseignant
const showGrades = ref(false)
const gradesQuiz = ref<QuizI | null>(null)
const gradesData = ref<QuizAttemptI[]>([])
const gradesLoading = ref(false)

// Créer un quiz (enseignant)
const showCreateQuiz = ref(false)
const newQuiz = ref({ titre: '', description: '', questions: [{ texte: '', options: [{ texte: '', correct: false }, { texte: '', correct: false }] }] as { texte: string; options: { texte: string; correct: boolean }[] }[] })
const creatingQuiz = ref(false)

// Modifier un quiz (enseignant)
const editingQuiz = ref(false)
const editQuizData = ref({ titre: '', description: '', questions: [] as { texte: string; options: { texte: string; correct: boolean }[] }[] })
const savingQuiz = ref(false)

async function loadQuizzes() {
  try {
    const res = await fetch(`http://localhost:8080/api/quizzes/course/${courseId}`)
    if (res.ok) {
      quizzes.value = await res.json()
      // Charger les tentatives de l'étudiant connecté
      if (authStore.user?.id && authStore.userRole !== 'enseignant') {
        const attempts: Record<number, QuizAttemptI> = {}
        for (const q of quizzes.value) {
          try {
            const aRes = await fetch(`http://localhost:8080/api/quizzes/${q.id}/attempt/${authStore.user.id}`)
            if (aRes.ok) {
              attempts[q.id] = await aRes.json()
            }
          } catch { /* ignore */ }
        }
        quizAttempts.value = attempts
      }
    }
  } catch { /* ignore */ }
}

async function openQuiz(quiz: QuizI) {
  quizLoading.value = true
  quizResult.value = null
  quizAnswers.value = {}

  // Si l'étudiant a déjà répondu → mode review (lecture seule avec corrections)
  if (!isOwner.value && quizAttempts.value[quiz.id] && authStore.user?.id) {
    try {
      const res = await fetch(`http://localhost:8080/api/quizzes/${quiz.id}/review/${authStore.user.id}`)
      if (res.ok) {
        const review = await res.json()
        activeQuiz.value = review.quiz
        quizResult.value = {
          total: review.attempt.totalQuestions,
          correct: review.attempt.correctAnswers,
          score: review.attempt.score,
          results: review.answers,
        }
        const answers: Record<number, number> = {}
        for (const a of review.answers) {
          answers[a.questionId] = a.selectedOptionId
        }
        quizAnswers.value = answers
      }
    } catch { /* ignore */ }
    quizLoading.value = false
    return
  }

  try {
    const url = isOwner.value
      ? `http://localhost:8080/api/quizzes/${quiz.id}/teacher`
      : `http://localhost:8080/api/quizzes/${quiz.id}`
    const res = await fetch(url)
    if (res.ok) activeQuiz.value = await res.json()
  } catch { /* ignore */ }
  quizLoading.value = false
}

function backToQuizList() {
  activeQuiz.value = null
  quizResult.value = null
  quizAnswers.value = {}
  showGrades.value = false
  gradesQuiz.value = null
  editingQuiz.value = false
}

async function submitQuiz() {
  if (!activeQuiz.value || quizSubmitting.value || !authStore.user?.id) return
  quizSubmitting.value = true
  try {
    const answers = Object.entries(quizAnswers.value).map(([qId, optId]) => ({
      questionId: Number(qId),
      selectedOptionId: optId,
    }))
    const res = await fetch(`http://localhost:8080/api/quizzes/${activeQuiz.value.id}/submit/${authStore.user.id}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(answers),
    })
    if (res.ok) {
      quizResult.value = await res.json()
      // Marquer comme tenté pour bloquer la relance
      if (activeQuiz.value) {
        quizAttempts.value[activeQuiz.value.id] = {
          id: 0,
          quizId: activeQuiz.value.id,
          quizTitre: activeQuiz.value.titre,
          userId: authStore.user.id,
          userName: authStore.userFullName,
          totalQuestions: quizResult.value!.total,
          correctAnswers: quizResult.value!.correct,
          score: quizResult.value!.score,
          completedAt: new Date().toISOString(),
        }
      }
    } else if (res.status === 409) {
      await showModal({ message: 'Vous avez déjà répondu à ce quiz.', title: 'Quiz déjà complété', type: 'alert', variant: 'warning', confirmText: 'OK' })
      backToQuizList()
      await loadQuizzes()
    }
  } catch { /* ignore */ }
  quizSubmitting.value = false
}

function addQuestion() {
  newQuiz.value.questions.push({ texte: '', options: [{ texte: '', correct: false }, { texte: '', correct: false }] })
}

function removeQuestion(qi: number) {
  if (newQuiz.value.questions.length <= 1) return
  newQuiz.value.questions.splice(qi, 1)
}

function addOption(qi: number) {
  const q = newQuiz.value.questions[qi]
  if (!q || q.options.length >= 6) return
  q.options.push({ texte: '', correct: false })
}

function removeOption(qi: number, oi: number) {
  const q = newQuiz.value.questions[qi]
  if (!q || q.options.length <= 2) return
  q.options.splice(oi, 1)
}

async function createQuiz() {
  if (creatingQuiz.value || !newQuiz.value.titre.trim()) return
  creatingQuiz.value = true
  try {
    const body = {
      titre: newQuiz.value.titre,
      description: newQuiz.value.description,
      courseId: courseId,
      questions: newQuiz.value.questions,
    }
    const res = await fetch('http://localhost:8080/api/quizzes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (res.ok) {
      showCreateQuiz.value = false
      newQuiz.value = { titre: '', description: '', questions: [{ texte: '', options: [{ texte: '', correct: false }, { texte: '', correct: false }] }] }
      await loadQuizzes()
    }
  } catch { /* ignore */ }
  creatingQuiz.value = false
}

async function deleteQuiz(quizId: number) {
  const ok = await showModal({ message: 'Supprimer ce quiz ? Cette action est irréversible.', title: 'Supprimer le quiz', variant: 'danger', confirmText: 'Supprimer' })
  if (!ok) return
  try {
    await fetch(`http://localhost:8080/api/quizzes/${quizId}`, { method: 'DELETE' })
    await loadQuizzes()
  } catch { /* ignore */ }
}

// ── Notes enseignant ──
async function openGrades(quiz: QuizI) {
  gradesQuiz.value = quiz
  showGrades.value = true
  gradesLoading.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/quizzes/${quiz.id}/attempts`)
    if (res.ok) gradesData.value = await res.json()
  } catch { /* ignore */ }
  gradesLoading.value = false
}

function closeGrades() {
  showGrades.value = false
  gradesQuiz.value = null
  gradesData.value = []
}

// ── Modifier un quiz ──
function startEditQuiz() {
  if (!activeQuiz.value) return
  editQuizData.value = {
    titre: activeQuiz.value.titre,
    description: activeQuiz.value.description || '',
    questions: (activeQuiz.value.questions || []).map(q => ({
      texte: q.texte,
      options: q.options.map(o => ({ texte: o.texte, correct: !!o.correct })),
    })),
  }
  editingQuiz.value = true
}

function cancelEditQuiz() {
  editingQuiz.value = false
}

function addEditQuestion() {
  editQuizData.value.questions.push({ texte: '', options: [{ texte: '', correct: false }, { texte: '', correct: false }] })
}

function removeEditQuestion(qi: number) {
  if (editQuizData.value.questions.length <= 1) return
  editQuizData.value.questions.splice(qi, 1)
}

function addEditOption(qi: number) {
  const q = editQuizData.value.questions[qi]
  if (!q || q.options.length >= 6) return
  q.options.push({ texte: '', correct: false })
}

function removeEditOption(qi: number, oi: number) {
  const q = editQuizData.value.questions[qi]
  if (!q || q.options.length <= 2) return
  q.options.splice(oi, 1)
}

async function saveEditQuiz() {
  if (!activeQuiz.value || savingQuiz.value || !editQuizData.value.titre.trim()) return
  savingQuiz.value = true
  try {
    const body = {
      titre: editQuizData.value.titre,
      description: editQuizData.value.description,
      courseId: courseId,
      questions: editQuizData.value.questions,
    }
    const res = await fetch(`http://localhost:8080/api/quizzes/${activeQuiz.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (res.ok) {
      const updated = await res.json()
      activeQuiz.value = updated
      editingQuiz.value = false
      await loadQuizzes()
    }
  } catch { /* ignore */ }
  savingQuiz.value = false
}

function toggleSection(idx: number) {
  const i = selectedSections.value.indexOf(idx)
  if (i === -1) selectedSections.value.push(idx)
  else selectedSections.value.splice(i, 1)
// Fichiers documents uploadés pour ce cours (PDF, DOCX, etc.)
const summarizableContents = computed(() =>
  uploadedContents.value.filter(c => c.contentType === 'Document')
)

function toggleContent(id: number) {
  const i = selectedContentIds.value.indexOf(id)
  if (i === -1) selectedContentIds.value.push(id)
  else selectedContentIds.value.splice(i, 1)
}
function selectAll() {
  if (!course.value) return
  if (summarizableContents.value.length > 0) {
    // Mode fichiers uploadés
    const allIds = summarizableContents.value.map(c => c.id)
    if (selectedContentIds.value.length === allIds.length)
      selectedContentIds.value = []
    else
      selectedContentIds.value = [...allIds]
  }
}
async function generateSummary() {
  if (selectedContentIds.value.length === 0) return
  isGenerating.value = true
  generatedSummary.value = ''
  summaryError.value = ''
  try {
    const res = await fetch('http://localhost:8080/api/ai/summarize', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ courseId, contentIds: selectedContentIds.value, lessonDescriptions: [] }),
    })
    if (!res.ok) throw new Error(`Erreur serveur : ${res.status}`)
    const data = await res.json()
    generatedSummary.value = data.summary ?? 'Aucune réponse reçue.'
  } catch (e: any) {
    summaryError.value = e?.message ?? 'Erreur inconnue.'
  } finally {
    isGenerating.value = false
  }
}
function resetSummary() {
  generatedSummary.value = ''
  summaryError.value = ''
  selectedContentIds.value = []
}
const completedLessons = ref<number[]>([])

// ── Questions / Discussion ──────────────────────────────────
interface Reply {
  id: number
  userId: number
  userName: string
  role: string
  text: string
  timestamp: string
  edited?: boolean
}

interface Question {
  id: number
  userId: number
  userName: string
  role: string
  text: string
  timestamp: string
  edited?: boolean
  replies: Reply[]
}

const questions = ref<Question[]>([])
const newQuestion = ref('')
const replyingTo = ref<number | null>(null)
const replyText = ref('')
const editingReplyId = ref<number | null>(null)
const editReplyText = ref('')

async function loadQuestions() {
  try {
    const res = await fetch(`http://localhost:8080/api/questions/course/${courseId}`)
    if (res.ok) questions.value = await res.json()
  } catch { /* ignore */ }
}

async function submitQuestion() {
  const text = newQuestion.value.trim()
  if (!text || (!isEnrolled.value && !isOwner.value) || !authStore.user?.id) return
  try {
    const res = await fetch(`http://localhost:8080/api/questions/course/${courseId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: authStore.user.id, text }),
    })
    if (res.ok) {
      const q = await res.json()
      questions.value.unshift(q)
      newQuestion.value = ''
    }
  } catch { /* ignore */ }
}

async function deleteQuestion(id: number) {
  const ok = await showModal({ message: 'Supprimer cette question et toutes ses réponses ?', title: 'Supprimer', variant: 'danger', confirmText: 'Supprimer' })
  if (!ok) return
  try {
    await fetch(`http://localhost:8080/api/questions/${id}`, { method: 'DELETE' })
    questions.value = questions.value.filter(q => q.id !== id)
    if (editingId.value === id) { editingId.value = null; editText.value = '' }
  } catch { /* ignore */ }
}

const editingId = ref<number | null>(null)
const editText  = ref('')

function startEdit(q: Question) {
  editingId.value = q.id
  editText.value  = q.text
}
function cancelQuestionEdit() {
  editingId.value = null
  editText.value  = ''
}
async function saveEdit(id: number) {
  const text = editText.value.trim()
  if (!text) return
  try {
    const res = await fetch(`http://localhost:8080/api/questions/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text }),
    })
    if (res.ok) {
      const q = questions.value.find(q => q.id === id)
      if (q) { q.text = text; q.edited = true }
    }
  } catch { /* ignore */ }
  cancelQuestionEdit()
}

// ── Replies ──
function startReply(questionId: number) {
  replyingTo.value = questionId
  replyText.value = ''
}
function cancelReply() {
  replyingTo.value = null
  replyText.value = ''
}
async function submitReply(questionId: number) {
  const text = replyText.value.trim()
  if (!text || !authStore.user?.id) return
  try {
    const res = await fetch(`http://localhost:8080/api/questions/${questionId}/replies`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: authStore.user.id, text }),
    })
    if (res.ok) {
      const reply = await res.json()
      const q = questions.value.find(q => q.id === questionId)
      if (q) q.replies.push(reply)
      cancelReply()
    }
  } catch { /* ignore */ }
}

function startEditReply(r: Reply) {
  editingReplyId.value = r.id
  editReplyText.value = r.text
}
function cancelEditReply() {
  editingReplyId.value = null
  editReplyText.value = ''
}
async function saveEditReply(questionId: number, replyId: number) {
  const text = editReplyText.value.trim()
  if (!text) return
  try {
    const res = await fetch(`http://localhost:8080/api/questions/replies/${replyId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text }),
    })
    if (res.ok) {
      const q = questions.value.find(q => q.id === questionId)
      const r = q?.replies.find(r => r.id === replyId)
      if (r) { r.text = text; r.edited = true }
    }
  } catch { /* ignore */ }
  cancelEditReply()
}

async function deleteReply(questionId: number, replyId: number) {
  const ok = await showModal({ message: 'Supprimer cette réponse ?', title: 'Supprimer', variant: 'danger', confirmText: 'Supprimer' })
  if (!ok) return
  try {
    await fetch(`http://localhost:8080/api/questions/replies/${replyId}`, { method: 'DELETE' })
    const q = questions.value.find(q => q.id === questionId)
    if (q) q.replies = q.replies.filter(r => r.id !== replyId)
  } catch { /* ignore */ }
}

function getRoleLabel(role: string) {
  if (role === 'enseignant') return 'Professeur'
  if (role === 'admin') return 'Admin'
  return 'Étudiant'
}

function getRoleClass(role: string) {
  if (role === 'enseignant') return 'badge-role--prof'
  if (role === 'admin') return 'badge-role--admin'
  return 'badge-role--student'
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })
}
// ────────────────────────────────────────────────────────────

watch(completedLessons, (val) => {
  const userId = authStore.user?.id
  if (!userId || !course.value) return
  const pct = Math.round((val.length / course.value.lessons.length) * 100)
  localStorage.setItem(`incredilearn_lessons_${userId}_${courseId}`, JSON.stringify(val))
  localStorage.setItem(`incredilearn_progress_${userId}_${courseId}`, String(pct))
}, { deep: true })

const progressPct = computed(() =>
  course.value ? Math.round((completedLessons.value.length / course.value.lessons.length) * 100) : 0
)

function selectLesson(idx: number) {
  activeLesson.value = idx
}
function toggleComplete(idx: number) {
  if (!isEnrolled.value) return   // pas d'inscription → pas de progression
  const i = completedLessons.value.indexOf(idx)
  if (i === -1) completedLessons.value.push(idx)
  else completedLessons.value.splice(i, 1)
}

/* ── Edit mode ── */
const isOwner = computed(() => {
  return authStore.user?.id != null && course.value?.instructorId === authStore.user.id
})

const editMode = ref(false)
const editForm = ref({ titre: '', description: '', domain: '', niveau: '', dureeHeures: 0 })
const savingInfo = ref(false)
const uploadingFile = ref(false)
const deletingContentId = ref<number | null>(null)

function enterEditMode() {
  if (!course.value) return
  editForm.value = {
    titre: course.value.title,
    description: course.value.description,
    domain: course.value.category,
    niveau: course.value.niveau,
    dureeHeures: course.value.dureeHeures,
  }
  editMode.value = true
}

function cancelEdit() {
  editMode.value = false
}

async function saveInfo() {
  if (!course.value || !authStore.user?.id || savingInfo.value) return
  savingInfo.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/courses/${course.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...editForm.value, enseignantId: authStore.user.id }),
    })
    if (res.ok) {
      const updated = await res.json()
      course.value.title = updated.titre
      course.value.description = updated.description || ''
      course.value.category = updated.domain || ''
      course.value.niveau = updated.niveau || ''
      course.value.dureeHeures = updated.dureeHeures || 0
    }
  } catch (err) {
    console.error(err)
  } finally {
    savingInfo.value = false
  }
}

async function uploadNewContent(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length || !course.value || uploadingFile.value) return
  uploadingFile.value = true

  for (const file of Array.from(input.files)) {
    const isVideo = file.type.startsWith('video/')
    const contentType = isVideo ? 'Video' : 'Document'

    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', contentType)
    formData.append('order', '0')

    // Extract video duration if applicable
    if (isVideo) {
      const duration = await getVideoDuration(file)
      if (duration > 0) formData.append('duration', String(duration))
    }

    try {
      const res = await fetch(`http://localhost:8080/api/courses/content/upload/${course.value.id}`, {
        method: 'POST',
        body: formData,
      })
      if (res.ok) {
        const dto = await res.json()
        uploadedContents.value.push(dto)
        // Update local lists
        if (contentType === 'Video') {
          course.value.lessons.push({
            id: dto.id,
            title: dto.fileName.replace(/\.[^.]+$/, ''),
            duration: '-',
            description: `Vidéo du cours : ${dto.fileName}`,
          })
        } else {
          course.value.resources.push({
            name: dto.fileName,
            type: dto.mimeType?.includes('pdf') ? 'PDF' : 'Document',
            size: formatFileSize(dto.fileSize),
            downloadUrl: dto.downloadUrl,
          })
        }
      }
    } catch (err) {
      console.error(err)
    }
  }

  input.value = ''
  uploadingFile.value = false
}

function getVideoDuration(file: File): Promise<number> {
  return new Promise((resolve) => {
    const video = document.createElement('video')
    video.preload = 'metadata'
    video.onloadedmetadata = () => {
      resolve(video.duration)
      URL.revokeObjectURL(video.src)
    }
    video.onerror = () => resolve(0)
    video.src = URL.createObjectURL(file)
  })
}

async function deleteContent(contentId: number) {
  if (deletingContentId.value !== null) return
  deletingContentId.value = contentId
  try {
    const res = await fetch(`http://localhost:8080/api/courses/content/delete/${contentId}`, { method: 'DELETE' })
    if (res.ok) {
      uploadedContents.value = uploadedContents.value.filter(c => c.id !== contentId)
      if (course.value) {
        course.value.lessons = course.value.lessons.filter(l => l.id !== contentId)
        course.value.resources = course.value.resources.filter(r => {
          const uc = uploadedContents.value.find(u => u.downloadUrl && r.downloadUrl === u.downloadUrl)
          return r.downloadUrl ? true : true // Keep non-uploaded resources; uploaded ones already filtered
        })
        // Also remove from resources by matching downloadUrl
        const deletedContent = uploadedContents.value.find(c => c.id === contentId)
        if (!deletedContent) {
          course.value.resources = course.value.resources.filter(r => {
            if (!r.downloadUrl) return true
            return !r.downloadUrl.includes(`/download/${contentId}`)
          })
        }
      }
    }
  } catch (err) {
    console.error(err)
  } finally {
    deletingContentId.value = null
  }
}

async function uploadCoverImage(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length || !course.value || uploadingFile.value) return
  uploadingFile.value = true
  const file = input.files[0]!
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', 'Cover')
  formData.append('order', '0')

  try {
    const res = await fetch(`http://localhost:8080/api/courses/content/upload/${course.value.id}`, {
      method: 'POST',
      body: formData,
    })
    if (res.ok) {
      const dto = await res.json()
      course.value.image = 'http://localhost:8080' + dto.downloadUrl
    }
  } catch (err) {
    console.error(err)
  } finally {
    input.value = ''
    uploadingFile.value = false
  }
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
      <div class="topbar-right">
        <button v-if="isOwner && !editMode" class="edit-toggle-btn" @click="enterEditMode">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
          </svg>
          Mode édition
        </button>
        <button v-if="editMode" class="edit-toggle-btn edit-toggle-btn--active" @click="cancelEdit">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
          Quitter l'édition
        </button>
        <span class="online-badge">
          <span class="online-dot"></span> Online
        </span>
      </div>
    </div>

    <!-- ── Body: layout + optional edit sidebar ── -->
    <div class="course-body" :class="{ 'course-body--editing': editMode }">

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
            <h1 class="lesson-title">{{ course.lessons[activeLesson]?.title }}</h1>
            <p class="lesson-instructor">{{ course.instructor }} • {{ course.category }}</p>
          </div>
          <div class="enroll-wrap">
            <template v-if="authStore.userRole === 'etudiant'">
            <span v-if="enrollMsg" class="enroll-msg" :class="{ 'enroll-msg--success': isEnrolled || enrollMsg.startsWith('D') }">{{ enrollMsg }}</span>
            <button
              v-if="!isEnrolled"
              class="enroll-btn"
              :disabled="enrollLoading"
              @click="enroll"
            >
              <svg v-if="enrollLoading" width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <line x1="19" y1="8" x2="19" y2="14"/>
                <line x1="22" y1="11" x2="16" y2="11"/>
              </svg>
              {{ enrollLoading ? 'Inscription…' : "S'inscrire" }}
            </button>
            <button
              v-else
              class="enroll-btn enroll-btn--done"
              :disabled="enrollLoading"
              @click="unenroll"
            >
              <svg v-if="enrollLoading" width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              {{ enrollLoading ? 'En cours…' : 'Déjà inscrit — Se désinscrire' }}
            </button>
            </template>

            <!-- ── Star rating (visible only when enrolled) ── -->
            <div v-if="isEnrolled" class="course-rating">
              <span class="rating-label">{{ userRating === 0 ? 'Notez ce cours :' : 'Votre note :' }}</span>
              <div class="stars-row">
                <button
                  v-for="star in 5"
                  :key="star"
                  class="star-btn"
                  @mouseenter="hoverRating = star"
                  @mouseleave="hoverRating = 0"
                  @click="setRating(star)"
                  :aria-label="`${star} étoile${star > 1 ? 's' : ''}`"
                >
                  <svg width="22" height="22" viewBox="0 0 24 24"
                    :fill="star <= (hoverRating || userRating) ? '#F59E0B' : 'none'"
                    :stroke="star <= (hoverRating || userRating) ? '#F59E0B' : '#D1D5DB'"
                    stroke-width="1.5">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                </button>
              </div>
              <span v-if="userRating > 0" class="rating-value">{{ userRating }}/5</span>
            </div>
          </div>

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
            :class="{ 'tab-btn--active': activeTab === 'quiz' }"
            @click="activeTab = 'quiz'; loadQuizzes()"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11l3 3L22 4"/>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
            Quiz
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

          <div v-if="summaryError" class="summary-error">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <span>{{ summaryError }}</span>
          </div>

          <div v-if="generatedSummary" class="summary-result">
            <p>{{ generatedSummary }}</p>
            <button class="summary-reset-btn" @click="resetSummary">Nouveau résumé</button>
          </div>
          <template v-else>

            <!-- Aucun fichier uploadé -->
            <div v-if="summarizableContents.length === 0" class="summary-no-content">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/>
                <polyline points="13 2 13 9 20 9"/>
              </svg>
              <p>Aucun document uploadé pour ce cours.<br/>Le professeur doit d'abord ajouter des fichiers PDF ou DOCX.</p>
            </div>

            <!-- Fichiers disponibles -->
            <template v-else>
              <div class="summary-select-header">
                <span class="summary-select-label">Sélectionnez les documents à résumer</span>
                <button class="summary-select-all" @click="selectAll">
                  {{ selectedContentIds.length === summarizableContents.length ? 'Tout désélectionner' : 'Tout sélectionner' }}
                </button>
              </div>

              <div class="summary-items">
                <label
                  v-for="content in summarizableContents"
                  :key="content.id"
                  class="summary-item"
                  :class="{ 'summary-item--checked': selectedContentIds.includes(content.id) }"
                >
                  <input
                    type="checkbox"
                    class="summary-checkbox"
                    :checked="selectedContentIds.includes(content.id)"
                    @change="toggleContent(content.id)"
                  />
                  <span class="summary-item-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                      <polyline points="14 2 14 8 20 8"/>
                    </svg>
                  </span>
                  <div class="summary-item-body">
                    <p class="summary-item-title">{{ content.fileName }}</p>
                    <p class="summary-item-desc">
                      <span class="summary-item-badge badge-doc">Document</span>
                      {{ content.mimeType?.split('/')[1]?.toUpperCase() ?? '' }} &bull; {{ Math.round(content.fileSize / 1024) }} KB
                    </p>
                  </div>
                </label>
              </div>

              <p class="summary-count">{{ selectedContentIds.length }} fichier(s) sélectionné(s)</p>
            </template>
          </template>

          <button
            class="summary-generate-btn"
            :disabled="selectedContentIds.length === 0 || isGenerating || summarizableContents.length === 0"
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
        <div v-else-if="activeTab === 'discussion'" class="discussion-panel">

          <!-- Zone de saisie -->
          <div v-if="isEnrolled || isOwner" class="discussion-form">
            <div class="discussion-form-header">
              <span class="discussion-avatar">{{ (authStore.user?.nom || 'U').charAt(0).toUpperCase() }}</span>
              <div class="discussion-form-meta">
                <span class="discussion-form-name">{{ authStore.user?.nom }}</span>
                <span class="badge-role" :class="getRoleClass(authStore.user?.role || '')">
                  {{ getRoleLabel(authStore.user?.role || '') }}
                </span>
              </div>
            </div>
            <textarea
              v-model="newQuestion"
              class="discussion-textarea"
              placeholder="Posez votre question ou partagez un commentaire..."
              rows="3"
              @keydown.ctrl.enter="submitQuestion"
            />
            <div class="discussion-form-footer">
              <span class="discussion-hint">Ctrl+Entrée pour envoyer</span>
              <button class="discussion-submit" :disabled="!newQuestion.trim()" @click="submitQuestion">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="22" y1="2" x2="11" y2="13"/>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                </svg>
                Envoyer
              </button>
            </div>
          </div>

          <!-- Message si non inscrit -->
          <div v-else class="discussion-locked">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <p>Inscrivez-vous à ce cours pour poser des questions et participer.</p>
          </div>

          <!-- Liste des questions -->
          <div class="discussion-list">
            <div v-if="questions.length === 0" class="discussion-empty">
              <p>Aucune question pour le moment. {{ (isEnrolled || isOwner) ? 'Soyez le premier à participer !' : '' }}</p>
            </div>
            <div v-for="q in questions" :key="q.id" class="discussion-item">
              <span class="discussion-avatar">{{ (q.userName || 'U').charAt(0).toUpperCase() }}</span>
              <div class="discussion-item-body">
                <div class="discussion-item-header">
                  <span class="discussion-item-name">{{ q.userName }}</span>
                  <span class="badge-role" :class="getRoleClass(q.role)">{{ getRoleLabel(q.role) }}</span>
                  <span class="discussion-item-date">{{ formatDate(q.timestamp) }}<span v-if="q.edited" class="discussion-edited"> (modifié)</span></span>
                  <template v-if="authStore.user?.id === q.userId && editingId !== q.id">
                    <button class="discussion-action-btn" title="Modifier" @click="startEdit(q)">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                      </svg>
                    </button>
                    <button class="discussion-action-btn discussion-action-btn--delete" title="Supprimer" @click="deleteQuestion(q.id)">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                    </button>
                  </template>
                  <!-- Owner (prof) can also delete any question -->
                  <template v-else-if="isOwner && authStore.user?.id !== q.userId && editingId !== q.id">
                    <button class="discussion-action-btn discussion-action-btn--delete" title="Supprimer" @click="deleteQuestion(q.id)">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                    </button>
                  </template>
                </div>
                <!-- Mode édition -->
                <template v-if="editingId === q.id">
                  <textarea
                    v-model="editText"
                    class="discussion-textarea discussion-textarea--edit"
                    rows="3"
                    @keydown.ctrl.enter="saveEdit(q.id)"
                    @keydown.esc="cancelQuestionEdit"
                  />
                  <div class="discussion-edit-actions">
                    <span class="discussion-hint">Ctrl+Entrée pour sauvegarder · Échap pour annuler</span>
                    <div style="display:flex;gap:6px">
                      <button class="discussion-cancel-btn" @click="cancelQuestionEdit">Annuler</button>
                      <button class="discussion-submit" :disabled="!editText.trim()" @click="saveEdit(q.id)">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                          <polyline points="20 6 9 17 4 12"/>
                        </svg>
                        Sauvegarder
                      </button>
                    </div>
                  </div>
                </template>
                <!-- Mode lecture -->
                <p v-else class="discussion-item-text">{{ q.text }}</p>

                <!-- Replies section -->
                <div class="discussion-replies" v-if="q.replies && q.replies.length > 0">
                  <div v-for="r in q.replies" :key="r.id" class="discussion-reply">
                    <span class="discussion-avatar discussion-avatar--sm">{{ (r.userName || 'U').charAt(0).toUpperCase() }}</span>
                    <div class="discussion-reply-body">
                      <div class="discussion-item-header">
                        <span class="discussion-item-name">{{ r.userName }}</span>
                        <span class="badge-role" :class="getRoleClass(r.role)">{{ getRoleLabel(r.role) }}</span>
                        <span class="discussion-item-date">{{ formatDate(r.timestamp) }}<span v-if="r.edited" class="discussion-edited"> (modifié)</span></span>
                        <template v-if="authStore.user?.id === r.userId && editingReplyId !== r.id">
                          <button class="discussion-action-btn" title="Modifier" @click="startEditReply(r)">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                            </svg>
                          </button>
                          <button class="discussion-action-btn discussion-action-btn--delete" title="Supprimer" @click="deleteReply(q.id, r.id)">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                            </svg>
                          </button>
                        </template>
                        <!-- Owner (prof) can also delete any reply -->
                        <template v-else-if="isOwner && authStore.user?.id !== r.userId && editingReplyId !== r.id">
                          <button class="discussion-action-btn discussion-action-btn--delete" title="Supprimer" @click="deleteReply(q.id, r.id)">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                            </svg>
                          </button>
                        </template>
                      </div>
                      <!-- Reply edit mode -->
                      <template v-if="editingReplyId === r.id">
                        <textarea
                          v-model="editReplyText"
                          class="discussion-textarea discussion-textarea--edit"
                          rows="2"
                          @keydown.ctrl.enter="saveEditReply(q.id, r.id)"
                          @keydown.esc="cancelEditReply"
                        />
                        <div class="discussion-edit-actions">
                          <span class="discussion-hint">Ctrl+Entrée · Échap</span>
                          <div style="display:flex;gap:6px">
                            <button class="discussion-cancel-btn" @click="cancelEditReply">Annuler</button>
                            <button class="discussion-submit" :disabled="!editReplyText.trim()" @click="saveEditReply(q.id, r.id)">Sauvegarder</button>
                          </div>
                        </div>
                      </template>
                      <p v-else class="discussion-item-text">{{ r.text }}</p>
                    </div>
                  </div>
                </div>

                <!-- Reply button & form -->
                <div class="discussion-reply-zone">
                  <button v-if="(isEnrolled || isOwner) && replyingTo !== q.id" class="discussion-reply-btn" @click="startReply(q.id)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="9 17 4 12 9 7"/><path d="M20 18v-2a4 4 0 0 0-4-4H4"/>
                    </svg>
                    Répondre
                  </button>
                  <div v-if="replyingTo === q.id" class="discussion-reply-form">
                    <textarea
                      v-model="replyText"
                      class="discussion-textarea discussion-textarea--reply"
                      placeholder="Votre réponse..."
                      rows="2"
                      @keydown.ctrl.enter="submitReply(q.id)"
                      @keydown.esc="cancelReply"
                    />
                    <div class="discussion-edit-actions">
                      <span class="discussion-hint">Ctrl+Entrée pour envoyer · Échap pour annuler</span>
                      <div style="display:flex;gap:6px">
                        <button class="discussion-cancel-btn" @click="cancelReply">Annuler</button>
                        <button class="discussion-submit" :disabled="!replyText.trim()" @click="submitReply(q.id)">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="22" y1="2" x2="11" y2="13"/>
                            <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                          </svg>
                          Répondre
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

              </div>
            </div>
          </div>
        </div>

        <!-- Quiz -->
        <div v-else-if="activeTab === 'quiz'" class="quiz-panel">

          <!-- Bouton créer (enseignant) -->
          <div v-if="isOwner" class="quiz-teacher-bar">
            <button class="quiz-create-btn" @click="showCreateQuiz = !showCreateQuiz">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              {{ showCreateQuiz ? 'Annuler' : 'Créer un quiz' }}
            </button>
          </div>

          <!-- Formulaire de création -->
          <div v-if="showCreateQuiz && isOwner" class="quiz-create-form">
            <h3 class="quiz-create-title">Nouveau Quiz</h3>
            <input v-model="newQuiz.titre" placeholder="Titre du quiz" class="quiz-input" />
            <input v-model="newQuiz.description" placeholder="Description (optionnel)" class="quiz-input" />

            <div v-for="(q, qi) in newQuiz.questions" :key="qi" class="quiz-question-form">
              <div class="quiz-question-header">
                <span class="quiz-question-num">Question {{ qi + 1 }}</span>
                <button v-if="newQuiz.questions.length > 1" class="quiz-remove-btn" @click="removeQuestion(qi)" title="Supprimer">&times;</button>
              </div>
              <textarea v-model="q.texte" placeholder="Texte de la question" class="quiz-textarea" rows="2"></textarea>
              <div v-for="(opt, oi) in q.options" :key="oi" class="quiz-option-form">
                <label class="quiz-option-label" :class="{ 'quiz-option-label--correct': opt.correct }">
                  <input type="radio" :name="'correct-' + qi" :checked="opt.correct" @change="q.options.forEach((o,i) => o.correct = i === oi)" />
                  <input v-model="opt.texte" :placeholder="'Option ' + (oi + 1)" class="quiz-option-input" />
                  <button v-if="q.options.length > 2" class="quiz-remove-opt" @click="removeOption(qi, oi)">&times;</button>
                </label>
              </div>
              <button v-if="q.options.length < 6" class="quiz-add-opt-btn" @click="addOption(qi)">+ Ajouter une option</button>
            </div>

            <button class="quiz-add-q-btn" @click="addQuestion">+ Ajouter une question</button>
            <button class="quiz-submit-btn" :disabled="creatingQuiz || !newQuiz.titre.trim()" @click="createQuiz">
              {{ creatingQuiz ? 'Création…' : 'Enregistrer le quiz' }}
            </button>
          </div>

          <!-- Vue notes enseignant -->
          <div v-if="showGrades && isOwner" class="quiz-grades-panel">
            <button class="quiz-back-btn" @click="closeGrades">&larr; Retour aux quiz</button>
            <h3 class="quiz-grades-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
              Notes — {{ gradesQuiz?.titre }}
            </h3>
            <div v-if="gradesLoading" class="quiz-grades-loading">Chargement…</div>
            <div v-else-if="gradesData.length === 0" class="quiz-grades-empty">
              <p>Aucun étudiant n'a encore répondu à ce quiz.</p>
            </div>
            <div v-else class="quiz-grades-table-wrap">
              <table class="quiz-grades-table">
                <thead>
                  <tr>
                    <th>Étudiant</th>
                    <th>Score</th>
                    <th>Bonnes réponses</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="a in gradesData" :key="a.id">
                    <td class="quiz-grades-name">{{ a.userName }}</td>
                    <td>
                      <span class="quiz-grades-score" :class="{ 'quiz-grades-score--good': a.score >= 70, 'quiz-grades-score--bad': a.score < 50 }">
                        {{ a.score }}%
                      </span>
                    </td>
                    <td>{{ a.correctAnswers }} / {{ a.totalQuestions }}</td>
                    <td class="quiz-grades-date">{{ new Date(a.completedAt).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }) }}</td>
                  </tr>
                </tbody>
              </table>
              <div class="quiz-grades-avg">
                Moyenne : <strong>{{ (gradesData.reduce((s, a) => s + a.score, 0) / gradesData.length).toFixed(1) }}%</strong>
                — {{ gradesData.length }} étudiant{{ gradesData.length > 1 ? 's' : '' }}
              </div>
            </div>
          </div>

          <!-- Passer un quiz -->
          <div v-else-if="activeQuiz" class="quiz-take">
            <button class="quiz-back-btn" @click="backToQuizList">&larr; Retour à la liste</button>
            <h3 class="quiz-take-title">{{ activeQuiz.titre }}</h3>
            <p v-if="activeQuiz.description" class="quiz-take-desc">{{ activeQuiz.description }}</p>

            <!-- Avertissement essai unique -->
            <div v-if="!quizResult && !isOwner" class="quiz-warning">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <span>Attention : vous n'avez <strong>qu'un seul essai</strong> pour ce quiz. Une fois soumis, vous ne pourrez plus modifier vos réponses.</span>
            </div>

            <!-- Bannière enseignant : mode aperçu -->
            <div v-if="isOwner && !editingQuiz" class="quiz-teacher-preview">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              <span>Aperçu enseignant — les bonnes réponses sont mises en évidence en vert.</span>
              <button class="quiz-edit-btn" @click="startEditQuiz">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                Modifier
              </button>
            </div>

            <!-- Formulaire de modification -->
            <div v-if="editingQuiz && isOwner" class="quiz-create-form">
              <h3 class="quiz-create-title">Modifier le quiz</h3>
              <input v-model="editQuizData.titre" placeholder="Titre du quiz" class="quiz-input" />
              <input v-model="editQuizData.description" placeholder="Description (optionnel)" class="quiz-input" />

              <div v-for="(q, qi) in editQuizData.questions" :key="qi" class="quiz-question-form">
                <div class="quiz-question-header">
                  <span class="quiz-question-num">Question {{ qi + 1 }}</span>
                  <button v-if="editQuizData.questions.length > 1" class="quiz-remove-btn" @click="removeEditQuestion(qi)" title="Supprimer">&times;</button>
                </div>
                <textarea v-model="q.texte" placeholder="Texte de la question" class="quiz-textarea" rows="2"></textarea>
                <div v-for="(opt, oi) in q.options" :key="oi" class="quiz-option-form">
                  <label class="quiz-option-label" :class="{ 'quiz-option-label--correct': opt.correct }">
                    <input type="radio" :name="'edit-correct-' + qi" :checked="opt.correct" @change="q.options.forEach((_o: { texte: string; correct: boolean },i: number) => _o.correct = i === oi)" />
                    <input v-model="opt.texte" :placeholder="'Option ' + (oi + 1)" class="quiz-option-input" />
                    <button v-if="q.options.length > 2" class="quiz-remove-opt" @click="removeEditOption(qi, oi)">&times;</button>
                  </label>
                </div>
                <button v-if="q.options.length < 6" class="quiz-add-opt-btn" @click="addEditOption(qi)">+ Ajouter une option</button>
              </div>

              <button class="quiz-add-q-btn" @click="addEditQuestion">+ Ajouter une question</button>
              <div class="quiz-edit-actions">
                <button class="quiz-submit-btn" :disabled="savingQuiz || !editQuizData.titre.trim()" @click="saveEditQuiz">
                  {{ savingQuiz ? 'Enregistrement…' : 'Enregistrer les modifications' }}
                </button>
                <button class="quiz-cancel-edit-btn" @click="cancelEditQuiz">Annuler</button>
              </div>
            </div>

            <!-- Résultat -->
            <div v-if="quizResult && !editingQuiz" class="quiz-result-card">
              <div class="quiz-result-score" :class="{ 'quiz-result-score--good': quizResult.score >= 70 }">
                {{ quizResult.score }}%
              </div>
              <p class="quiz-result-text">{{ quizResult.correct }} / {{ quizResult.total }} réponses correctes</p>
              <p class="quiz-result-locked">Ce quiz est terminé. Vous ne pouvez pas le repasser.</p>
            </div>

            <!-- Questions -->
            <div v-if="!editingQuiz" class="quiz-questions">
              <div v-for="(q, qi) in activeQuiz.questions" :key="q.id" class="quiz-q-card" :class="{ 'quiz-q-card--correct': quizResult && (quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.correct, 'quiz-q-card--wrong': quizResult && !(quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.correct }">
                <div class="quiz-q-header">
                  <p class="quiz-q-text"><strong>{{ qi + 1 }}.</strong> {{ q.texte }}</p>
                  <span v-if="quizResult" class="quiz-q-badge" :class="(quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.correct ? 'quiz-q-badge--correct' : 'quiz-q-badge--wrong'">
                    {{ (quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.correct ? '✓ Correct' : '✗ Incorrect' }}
                  </span>
                </div>
                <div class="quiz-q-options">
                  <label
                    v-for="opt in q.options"
                    :key="opt.id"
                    class="quiz-q-option"
                    :class="{
                      'quiz-q-option--selected': !quizResult && !isOwner && quizAnswers[q.id] === opt.id,
                      'quiz-q-option--correct': (quizResult && (quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.correctOptionId === opt.id) || (isOwner && !quizResult && opt.correct),
                      'quiz-q-option--wrong': quizResult && (quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.selectedOptionId === opt.id && (quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.correctOptionId !== opt.id,
                      'quiz-q-option--disabled': !!quizResult || isOwner,
                    }"
                  >
                    <input type="radio" :name="'quiz-q-' + q.id" :value="opt.id" v-model="quizAnswers[q.id]" :disabled="!!quizResult || isOwner" />
                    <span>{{ opt.texte }}</span>
                    <svg v-if="quizResult && (quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.correctOptionId === opt.id" class="quiz-q-icon quiz-q-icon--correct" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                    <svg v-if="quizResult && (quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.selectedOptionId === opt.id && (quizResult as QuizResult).results.find((r: QuizResult['results'][0]) => r.questionId === q.id)?.correctOptionId !== opt.id" class="quiz-q-icon quiz-q-icon--wrong" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  </label>
                </div>
              </div>
              <button
                v-if="!quizResult && !isOwner"
                class="quiz-submit-answers-btn"
                :disabled="quizSubmitting || Object.keys(quizAnswers).length < (activeQuiz.questions?.length || 0)"
                @click="submitQuiz"
              >
                {{ quizSubmitting ? 'Envoi…' : 'Soumettre mes réponses' }}
              </button>
            </div>
          </div>

          <!-- Liste des quiz -->
          <div v-else-if="quizzes.length > 0" class="quiz-list">
            <div v-for="q in quizzes" :key="q.id" class="quiz-card" :class="!isOwner && quizAttempts[q.id] ? (quizAttempts[q.id]!.score < 50 ? 'quiz-card--fail' : quizAttempts[q.id]!.score <= 60 ? 'quiz-card--avg' : 'quiz-card--pass') : ''" @click="openQuiz(q)">
              <div class="quiz-card-body">
                <h4 class="quiz-card-title">{{ q.titre }}</h4>
                <p v-if="q.description" class="quiz-card-desc">{{ q.description }}</p>
                <span class="quiz-card-meta">{{ q.nombreQuestions }} question{{ q.nombreQuestions > 1 ? 's' : '' }}</span>
                <!-- Badge tentative étudiant -->
                <div v-if="!isOwner && quizAttempts[q.id]" class="quiz-card-attempted" :class="quizAttempts[q.id]!.score < 50 ? 'quiz-card-attempted--fail' : quizAttempts[q.id]!.score <= 60 ? 'quiz-card-attempted--avg' : 'quiz-card-attempted--pass'">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  Répondu — <strong>{{ quizAttempts[q.id]?.score }}%</strong> · Cliquez pour revoir
                </div>
              </div>
              <div class="quiz-card-actions">
                <button v-if="isOwner" class="quiz-grades-btn" @click.stop="openGrades(q)" title="Voir les notes">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </button>
                <button v-if="isOwner" class="quiz-delete-btn" @click.stop="deleteQuiz(q.id)" title="Supprimer">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                    <path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                  </svg>
                </button>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
            </div>
          </div>

          <div v-else class="quiz-empty">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
            <p>Aucun quiz disponible pour ce cours.</p>
            <p v-if="isOwner" class="quiz-empty-hint">Cliquez sur « Créer un quiz » pour commencer.</p>
          </div>
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
                :class="{ 'lesson-check--locked': !isEnrolled }"
                :checked="completedLessons.includes(idx)"
                :disabled="!isEnrolled"
                :title="!isEnrolled ? 'Inscrivez-vous pour suivre votre progression' : ''"
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

    <!-- ── Edit panel sidebar (owner only) ── -->
    <aside v-if="editMode" class="edit-panel">
      <div class="edit-panel-inner">
        <!-- Course info editing -->
        <div class="edit-section">
          <h3 class="edit-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
            </svg>
            Informations du cours
          </h3>
          <div class="edit-fields">
            <label class="edit-field">
              <span>Titre</span>
              <input v-model="editForm.titre" type="text" />
            </label>
            <label class="edit-field">
              <span>Description</span>
              <textarea v-model="editForm.description" rows="3"></textarea>
            </label>
            <label class="edit-field">
              <span>Domaine</span>
              <select v-model="editForm.domain">
                <option value="" disabled>Choisir un domaine</option>
                <option value="Programmation">Programmation</option>
                <option value="Design">Design</option>
                <option value="Marketing">Marketing</option>
                <option value="Bureautique">Bureautique</option>
                <option value="Business">Business</option>
                <option value="Intelligence Artificielle">Intelligence Artificielle</option>
              </select>
            </label>
            <label class="edit-field">
              <span>Niveau</span>
              <select v-model="editForm.niveau">
                <option value="">Tous niveaux</option>
                <option value="Débutant">Débutant</option>
                <option value="Intermédiaire">Intermédiaire</option>
                <option value="Avancé">Avancé</option>
              </select>
            </label>
            <label class="edit-field">
              <span>Durée (h)</span>
              <input v-model.number="editForm.dureeHeures" type="number" min="0" />
            </label>
            <button class="edit-save-btn" :disabled="savingInfo" @click="saveInfo">
              {{ savingInfo ? 'Enregistrement...' : 'Enregistrer' }}
            </button>
          </div>
        </div>

        <!-- Cover image -->
        <div class="edit-section">
          <h3 class="edit-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            Image de couverture
          </h3>
          <div class="edit-cover-col">
            <img :src="course.image" alt="cover" class="edit-cover-preview" />
            <label class="edit-upload-btn">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
              Changer l'image
              <input type="file" accept="image/*" hidden @change="uploadCoverImage" />
            </label>
          </div>
        </div>

        <!-- Content management -->
        <div class="edit-section">
          <h3 class="edit-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            Contenu du cours
          </h3>

          <div class="edit-content-list">
            <div v-for="content in uploadedContents.filter(c => c.contentType !== 'Cover')" :key="content.id" class="edit-content-item">
              <div class="edit-content-icon" :class="content.contentType === 'Video' ? 'edit-content-icon--video' : ''">
                <svg v-if="content.contentType === 'Video'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="5 3 19 12 5 21 5 3"/>
                </svg>
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
              </div>
              <div class="edit-content-info">
                <p class="edit-content-name">{{ content.fileName }}</p>
                <p class="edit-content-meta">{{ content.contentType }} • {{ formatFileSize(content.fileSize) }}</p>
              </div>
              <button
                class="edit-content-delete"
                :disabled="deletingContentId === content.id"
                @click="deleteContent(content.id)"
              >
                <svg v-if="deletingContentId === content.id" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                </svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
              </button>
            </div>
            <div v-if="uploadedContents.filter(c => c.contentType !== 'Cover').length === 0" class="edit-content-empty">
              Aucun contenu uploadé pour ce cours.
            </div>
          </div>

          <label class="edit-upload-btn edit-upload-btn--full">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            {{ uploadingFile ? 'Upload en cours...' : 'Ajouter des fichiers' }}
            <input type="file" multiple accept="video/*,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt" hidden @change="uploadNewContent" :disabled="uploadingFile" />
          </label>
        </div>
      </div>
    </aside>

    </div><!-- /course-body -->
  </div>

  <ConfirmModal
    :visible="modalVisible"
    :message="modalMessage"
    :title="modalTitle"
    :variant="modalVariant"
    :type="modalType"
    :confirm-text="modalConfirmText"
    :cancel-text="modalCancelText"
    @confirm="onModalConfirm"
    @cancel="onModalCancel"
  />
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

/* ── Body wrapper (layout + edit sidebar) ── */
.course-body {
  display: flex;
  gap: 0;
  transition: all 0.3s ease;
}
.course-body--editing {
  gap: 0;
}

/* ── Layout ── */
.course-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 1.5rem;
  flex: 1;
  min-width: 0;
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

/* ── Enroll button ── */
.enroll-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}
.enroll-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  background: #10B981;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  font-size: 0.88rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s, opacity 0.15s;
}
.enroll-btn:hover:not(:disabled) { background: #059669; }
.enroll-btn:disabled { opacity: 0.75; cursor: default; }
.enroll-btn--done { background: #FEE2E2; color: #DC2626; border-color: #FECACA; }
.enroll-btn--done:hover { background: #FCA5A5; border-color: #F87171; }

/* ── Star rating ── */
.course-rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding: 10px 14px;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  border-radius: 10px;
}
.rating-label { font-size: .85rem; color: #92400E; font-weight: 500; white-space: nowrap; }
.stars-row { display: flex; gap: 2px; }
.star-btn {
  background: none;
  border: none;
  padding: 2px;
  cursor: pointer;
  transition: transform .15s;
  line-height: 0;
}
.star-btn:hover { transform: scale(1.2); }
.rating-value { font-size: .85rem; font-weight: 700; color: #D97706; }
.enroll-msg {
  font-size: 0.78rem;
  color: #EF4444;
  font-weight: 500;
}
.enroll-msg--success { color: #10B981; }

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
.summary-item-icon { flex-shrink: 0; margin-top: 2px; }
.summary-item-title { font-size: 0.85rem; font-weight: 600; color: #111827; margin: 0 0 4px; word-break: break-all; }
.summary-item-desc  { display: flex; align-items: center; gap: 0.4rem; font-size: 0.73rem; color: #6B7280; margin: 0; line-height: 1.4; }
.summary-item-badge { display: inline-block; padding: 1px 7px; border-radius: 20px; font-size: 0.68rem; font-weight: 700; text-transform: uppercase; }
.badge-video { background: #EEF2FF; color: #6366F1; }
.badge-doc   { background: #ECFDF5; color: #059669; }
.summary-count { font-size: 0.78rem; color: #6B7280; margin: 0; }
.summary-no-content {
  display: flex; flex-direction: column; align-items: center; gap: 0.5rem;
  padding: 1.5rem; text-align: center; color: #9CA3AF; font-size: 0.82rem; line-height: 1.5;
}
.summary-result {
  background: #ECFDF5; border: 1.5px solid #A7F3D0; border-radius: 10px;
  padding: 1rem 1.1rem;
}
.summary-result p { font-size: 0.875rem; color: #064E3B; line-height: 1.6; margin: 0; white-space: pre-wrap; }
.summary-reset-btn {
  margin-top: 0.75rem; background: none; border: 1.5px solid #10B981;
  color: #10B981; border-radius: 8px; padding: 0.4rem 0.9rem;
  font-size: 0.8rem; font-weight: 600; cursor: pointer; font-family: inherit;
  transition: background 0.15s, color 0.15s;
}
.summary-reset-btn:hover { background: #10B981; color: #fff; }
.summary-error {
  display: flex; align-items: center; gap: 0.5rem;
  background: #FEF2F2; border: 1.5px solid #FCA5A5; border-radius: 10px;
  padding: 0.75rem 1rem; font-size: 0.82rem; color: #991B1B;
}
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

/* ════ DISCUSSION ════ */
.discussion-panel { display: flex; flex-direction: column; gap: 1rem; }

.discussion-form {
  border: 1.5px solid #E5E7EB;
  border-radius: 12px;
  padding: 1rem;
  background: #FAFAFA;
}
.discussion-form-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.discussion-form-meta { display: flex; align-items: center; gap: 8px; }
.discussion-form-name { font-weight: 600; font-size: .9rem; color: #1e293b; }
.discussion-textarea {
  width: 100%;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: .9rem;
  color: #1e293b;
  resize: vertical;
  font-family: inherit;
  transition: border-color .2s;
  box-sizing: border-box;
  background: #fff;
}
.discussion-textarea:focus { outline: none; border-color: #10B981; }
.discussion-form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.discussion-hint { font-size: .75rem; color: #94a3b8; }
.discussion-submit {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #10B981;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 7px 14px;
  font-size: .85rem;
  font-weight: 600;
  cursor: pointer;
  transition: background .2s;
}
.discussion-submit:hover { background: #059669; }
.discussion-submit:disabled { background: #D1FAE5; color: #6EE7B7; cursor: not-allowed; }

.discussion-locked {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: #F8FAFC;
  border: 1.5px dashed #CBD5E1;
  border-radius: 10px;
  color: #64748b;
  font-size: .875rem;
}
.discussion-locked p { margin: 0; }

.discussion-list { display: flex; flex-direction: column; gap: 10px; }
.discussion-empty {
  padding: 2rem;
  text-align: center;
  color: #9CA3AF;
  font-size: 0.875rem;
  border: 1.5px dashed #E5E7EB;
  border-radius: 10px;
}

.discussion-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10B981, #059669);
  color: white;
  font-weight: 700;
  font-size: .9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.discussion-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  border: 1.5px solid #F1F5F9;
  border-radius: 10px;
  background: #fff;
}
.discussion-item-body { flex: 1; min-width: 0; }
.discussion-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.discussion-item-name { font-weight: 600; font-size: .875rem; color: #1e293b; }
.discussion-item-date { font-size: .75rem; color: #94a3b8; margin-left: auto; }
.discussion-item-text { font-size: .875rem; color: #334155; margin: 0; line-height: 1.5; white-space: pre-wrap; }
.discussion-action-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #94a3b8;
  padding: 3px 5px;
  line-height: 0;
  border-radius: 4px;
  transition: color .15s, background .15s;
}
.discussion-action-btn:hover { color: #10B981; background: #D1FAE5; }
.discussion-action-btn--delete:hover { color: #EF4444; background: #FEE2E2; }

.discussion-textarea--edit { margin-top: 6px; }
.discussion-edit-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
  flex-wrap: wrap;
  gap: 6px;
}
.discussion-cancel-btn {
  background: #F1F5F9;
  border: 1.5px solid #E2E8F0;
  color: #475569;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: .85rem;
  font-weight: 600;
  cursor: pointer;
  transition: background .15s;
}
.discussion-cancel-btn:hover { background: #E2E8F0; }
.discussion-edited { font-style: italic; color: #94a3b8; font-size: .72rem; }

/* Replies */
.discussion-replies {
  margin-top: 12px;
  padding-left: 8px;
  border-left: 2.5px solid #E2E8F0;
}
.discussion-reply {
  display: flex;
  gap: 10px;
  padding: 10px 0 6px;
}
.discussion-reply + .discussion-reply {
  border-top: 1px solid #F1F5F9;
}
.discussion-avatar--sm {
  width: 28px;
  height: 28px;
  font-size: .7rem;
  flex-shrink: 0;
}
.discussion-reply-body {
  flex: 1;
  min-width: 0;
}
.discussion-reply-zone {
  margin-top: 8px;
}
.discussion-reply-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  color: #6366F1;
  font-size: .8rem;
  font-weight: 600;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background .15s;
}
.discussion-reply-btn:hover {
  background: #EEF2FF;
}
.discussion-reply-form {
  margin-top: 6px;
}
.discussion-textarea--reply {
  font-size: .85rem;
}


/* Role badges */
.badge-role {
  font-size: .7rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: .04em;
}
.badge-role--student { background: #DBEAFE; color: #1D4ED8; }
.badge-role--prof    { background: #D1FAE5; color: #065F46; }
.badge-role--admin   { background: #EDE9FE; color: #5B21B6; }


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
.lesson-check--locked {
  cursor: not-allowed;
  opacity: 0.4;
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
@media (max-width: 1200px) {
  .course-body { flex-direction: column; }
  .edit-panel { width: 100% !important; max-height: 50vh; border-left: none; border-top: 2px solid #BFDBFE; }
}
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

/* ── Topbar right ── */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

/* ── Edit toggle button ── */
.edit-toggle-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.9rem;
  background: #EFF6FF;
  color: #2563EB;
  border: 1.5px solid #BFDBFE;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.edit-toggle-btn:hover {
  background: #DBEAFE;
  border-color: #93C5FD;
}
.edit-toggle-btn--active {
  background: #FEF2F2;
  color: #DC2626;
  border-color: #FECACA;
}
.edit-toggle-btn--active:hover {
  background: #FEE2E2;
  border-color: #FCA5A5;
}

/* ── Edit panel (sidebar) ── */
.edit-panel {
  width: 380px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #EFF6FF 0%, #F8FAFC 40%, #ECFDF5 100%);
  border-left: 2px solid #BFDBFE;
  overflow-y: auto;
  max-height: calc(100vh - 54px);
  position: sticky;
  top: 54px;
}
.edit-panel-inner {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.25rem;
}
.edit-section {
  background: #fff;
  border-radius: 12px;
  padding: 1rem;
  border: 1.5px solid #E5E7EB;
}
.edit-section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 1rem;
}
.edit-fields {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.edit-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.edit-field span {
  font-size: 0.82rem;
  font-weight: 500;
  color: #374151;
}
.edit-field input,
.edit-field textarea,
.edit-field select {
  padding: 0.55rem 0.7rem;
  border: 1.5px solid #D1D5DB;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #111827;
  background: #fff;
  transition: border-color 0.15s;
  font-family: inherit;
}
.edit-field input:focus,
.edit-field textarea:focus,
.edit-field select:focus {
  outline: none;
  border-color: #10B981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.12);
}
.edit-field textarea {
  resize: vertical;
}
.edit-save-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.6rem 1rem;
  background: #10B981;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  align-self: flex-start;
}
.edit-save-btn:hover:not(:disabled) {
  background: #059669;
}
.edit-save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Cover editing */
.edit-cover-col {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.edit-cover-preview {
  width: 100%;
  max-width: 240px;
  aspect-ratio: 16/9;
  object-fit: cover;
  border-radius: 8px;
  border: 1.5px solid #E5E7EB;
}
.edit-upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  background: #F3F4F6;
  color: #374151;
  border: 1.5px solid #D1D5DB;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.edit-upload-btn:hover {
  background: #E5E7EB;
  border-color: #9CA3AF;
}
.edit-upload-btn--full {
  width: 100%;
  justify-content: center;
  margin-top: 0.75rem;
  background: #ECFDF5;
  color: #059669;
  border-color: #A7F3D0;
}
.edit-upload-btn--full:hover {
  background: #D1FAE5;
  border-color: #6EE7B7;
}

/* Content list in edit mode */
.edit-content-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.edit-content-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0.8rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  background: #FAFAFA;
}
.edit-content-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: #FEE2E2;
  color: #DC2626;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.edit-content-icon--video {
  background: #EDE9FE;
  color: #7C3AED;
}
.edit-content-info {
  flex: 1;
  min-width: 0;
}
.edit-content-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.edit-content-meta {
  font-size: 0.72rem;
  color: #9CA3AF;
}
.edit-content-delete {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  background: #FEF2F2;
  color: #DC2626;
  border: 1.5px solid #FECACA;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s;
}
.edit-content-delete:hover:not(:disabled) {
  background: #FEE2E2;
}
.edit-content-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.edit-content-empty {
  text-align: center;
  padding: 1rem;
  font-size: 0.85rem;
  color: #9CA3AF;
  border: 1.5px dashed #E5E7EB;
  border-radius: 8px;
}

/* ════════ QUIZ ════════ */
.quiz-panel {
  padding: 0.5rem 0;
}

/* Teacher bar */
.quiz-teacher-bar {
  margin-bottom: 1rem;
}
.quiz-create-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #10B981, #059669);
  color: #fff;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.quiz-create-btn:hover {
  box-shadow: 0 4px 14px rgba(16,185,129,0.3);
  transform: translateY(-1px);
}

/* Create form */
.quiz-create-form {
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  padding: 1.25rem;
  margin-bottom: 1.25rem;
}
.quiz-create-title {
  font-size: 1rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.75rem;
}
.quiz-input {
  width: 100%;
  padding: 0.55rem 0.75rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  font-size: 0.85rem;
  margin-bottom: 0.6rem;
  font-family: inherit;
  transition: border-color 0.15s;
}
.quiz-input:focus {
  outline: none;
  border-color: #10B981;
  box-shadow: 0 0 0 3px rgba(16,185,129,0.1);
}
.quiz-textarea {
  width: 100%;
  padding: 0.55rem 0.75rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  font-size: 0.85rem;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.15s;
}
.quiz-textarea:focus {
  outline: none;
  border-color: #10B981;
  box-shadow: 0 0 0 3px rgba(16,185,129,0.1);
}
.quiz-question-form {
  background: #ffffff;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  padding: 1rem;
  margin-bottom: 0.75rem;
}
.quiz-question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}
.quiz-question-num {
  font-size: 0.8rem;
  font-weight: 700;
  color: #10B981;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.quiz-remove-btn {
  background: none;
  border: none;
  color: #EF4444;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0 0.3rem;
  line-height: 1;
}
.quiz-option-form {
  margin-top: 0.4rem;
}
.quiz-option-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.5rem;
  border-radius: 6px;
  transition: background 0.15s;
}
.quiz-option-label--correct {
  background: #ECFDF5;
}
.quiz-option-label input[type="radio"] {
  accent-color: #10B981;
}
.quiz-option-input {
  flex: 1;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  padding: 0.35rem 0.6rem;
  font-size: 0.83rem;
  font-family: inherit;
}
.quiz-option-input:focus {
  outline: none;
  border-color: #10B981;
}
.quiz-remove-opt {
  background: none;
  border: none;
  color: #9CA3AF;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 0 0.2rem;
}
.quiz-remove-opt:hover { color: #EF4444; }
.quiz-add-opt-btn {
  display: inline-block;
  margin-top: 0.4rem;
  font-size: 0.78rem;
  color: #6B7280;
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 500;
}
.quiz-add-opt-btn:hover { color: #10B981; }
.quiz-add-q-btn {
  display: block;
  width: 100%;
  padding: 0.55rem;
  border: 1.5px dashed #D1D5DB;
  border-radius: 8px;
  background: none;
  color: #6B7280;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  margin-bottom: 0.75rem;
  transition: all 0.15s;
}
.quiz-add-q-btn:hover {
  border-color: #10B981;
  color: #10B981;
}
.quiz-submit-btn {
  display: block;
  width: 100%;
  padding: 0.6rem;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #10B981, #059669);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.quiz-submit-btn:hover:not(:disabled) { box-shadow: 0 4px 14px rgba(16,185,129,0.3); }
.quiz-submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Quiz list cards */
.quiz-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}
.quiz-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.85rem 1rem;
  background: #ffffff;
  border: 1.5px solid #E5E7EB;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.quiz-card:hover {
  border-color: #10B981;
  box-shadow: 0 2px 12px rgba(16,185,129,0.1);
}
.quiz-card-body { flex: 1; min-width: 0; }
.quiz-card-title {
  font-size: 0.92rem;
  font-weight: 600;
  color: #111827;
  margin: 0;
}
.quiz-card-desc {
  font-size: 0.8rem;
  color: #6B7280;
  margin: 0.2rem 0 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.quiz-card-meta {
  font-size: 0.75rem;
  color: #9CA3AF;
  margin-top: 0.15rem;
  display: inline-block;
}
.quiz-card-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #D1D5DB;
  flex-shrink: 0;
}
.quiz-delete-btn {
  background: none;
  border: none;
  color: #D1D5DB;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 6px;
  transition: all 0.15s;
}
.quiz-delete-btn:hover {
  color: #EF4444;
  background: #FEF2F2;
}

/* Quiz empty */
.quiz-empty {
  text-align: center;
  padding: 2.5rem 1rem;
  color: #9CA3AF;
  font-size: 0.88rem;
}
.quiz-empty svg { margin-bottom: 0.75rem; }
.quiz-empty-hint { font-size: 0.8rem; margin-top: 0.3rem; }

/* Taking a quiz */
.quiz-take { }
.quiz-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  background: none;
  border: none;
  color: #6B7280;
  font-size: 0.82rem;
  cursor: pointer;
  margin-bottom: 0.75rem;
  padding: 0;
  font-weight: 500;
}
.quiz-back-btn:hover { color: #10B981; }
.quiz-take-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 0.25rem;
}
.quiz-take-desc {
  font-size: 0.85rem;
  color: #6B7280;
  margin: 0 0 1rem;
}
.quiz-questions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.quiz-q-card {
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  padding: 1rem;
}
.quiz-q-card--correct {
  border-color: #A7F3D0;
  background: #F0FDF4;
}
.quiz-q-card--wrong {
  border-color: #FECACA;
  background: #FEF2F2;
}
.quiz-q-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.5rem;
}
.quiz-q-text {
  font-size: 0.9rem;
  color: #111827;
  margin: 0 0 0.65rem;
  line-height: 1.5;
  flex: 1;
}
.quiz-q-badge {
  flex-shrink: 0;
  font-size: 0.72rem;
  font-weight: 600;
  padding: 0.2rem 0.55rem;
  border-radius: 20px;
  white-space: nowrap;
}
.quiz-q-badge--correct {
  background: #D1FAE5;
  color: #065F46;
}
.quiz-q-badge--wrong {
  background: #FEE2E2;
  color: #991B1B;
}
.quiz-q-options {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}
.quiz-q-option {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  padding: 0.5rem 0.75rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 0.85rem;
  color: #374151;
  background: #fff;
}
.quiz-q-option:hover {
  border-color: #D1D5DB;
  background: #F9FAFB;
}
.quiz-q-option--selected {
  border-color: #10B981;
  background: #F0FDF4;
  color: #065F46;
}
.quiz-q-option--correct {
  border-color: #10B981 !important;
  background: #ECFDF5 !important;
}
.quiz-q-option--wrong {
  border-color: #EF4444 !important;
  background: #FEF2F2 !important;
  color: #991B1B !important;
}
.quiz-q-option input[type="radio"] {
  accent-color: #10B981;
}
.quiz-q-option--disabled {
  cursor: default;
  pointer-events: none;
}
.quiz-q-icon {
  flex-shrink: 0;
  margin-left: auto;
}
.quiz-q-option--correct .quiz-q-icon--correct {
  animation: quiz-pop 0.3s ease;
}
@keyframes quiz-pop {
  0% { transform: scale(0); }
  60% { transform: scale(1.3); }
  100% { transform: scale(1); }
}
.quiz-submit-answers-btn {
  display: block;
  width: 100%;
  margin-top: 1rem;
  padding: 0.65rem;
  border: none;
  border-radius: 9px;
  background: linear-gradient(135deg, #10B981, #059669);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.quiz-submit-answers-btn:hover:not(:disabled) { box-shadow: 0 4px 14px rgba(16,185,129,0.3); }
.quiz-submit-answers-btn:disabled { opacity: 0.45; cursor: not-allowed; }

/* Quiz warning */
.quiz-warning {
  display: flex;
  align-items: flex-start;
  gap: 0.6rem;
  padding: 0.75rem 1rem;
  background: #FFFBEB;
  border: 1.5px solid #FDE68A;
  border-radius: 10px;
  margin-bottom: 1.2rem;
  font-size: 0.82rem;
  color: #92400E;
  line-height: 1.45;
}
.quiz-warning svg {
  flex-shrink: 0;
  margin-top: 1px;
  color: #F59E0B;
}

/* Teacher preview banner */
.quiz-teacher-preview {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.65rem 1rem;
  background: #EFF6FF;
  border: 1.5px solid #BFDBFE;
  border-radius: 10px;
  margin-bottom: 1.2rem;
  font-size: 0.82rem;
  color: #1E40AF;
  line-height: 1.45;
}
.quiz-teacher-preview svg {
  flex-shrink: 0;
  color: #3B82F6;
}
.quiz-teacher-preview span {
  flex: 1;
}

/* Edit quiz button */
.quiz-edit-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.75rem;
  border: 1.5px solid #BFDBFE;
  border-radius: 7px;
  background: #fff;
  color: #2563EB;
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
  flex-shrink: 0;
}
.quiz-edit-btn:hover {
  background: #DBEAFE;
  border-color: #93C5FD;
}

/* Edit form actions */
.quiz-edit-actions {
  display: flex;
  gap: 0.6rem;
  margin-top: 0.5rem;
}
.quiz-cancel-edit-btn {
  padding: 0.5rem 1.2rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  background: #fff;
  color: #6B7280;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.quiz-cancel-edit-btn:hover {
  border-color: #D1D5DB;
  background: #F9FAFB;
}

/* Quiz result */
.quiz-result-card {
  text-align: center;
  padding: 2rem 1.5rem;
  background: #F9FAFB;
  border: 1.5px solid #E5E7EB;
  border-radius: 12px;
  margin-bottom: 1.5rem;
}
.quiz-result-score {
  font-size: 3rem;
  font-weight: 800;
  color: #EF4444;
  margin-bottom: 0.3rem;
}
.quiz-result-score--good {
  color: #10B981;
}
.quiz-result-text {
  font-size: 0.9rem;
  color: #6B7280;
  margin: 0 0 1rem;
}
.quiz-result-locked {
  font-size: 0.8rem;
  color: #9CA3AF;
  margin-top: 0.5rem;
  font-style: italic;
}

/* Quiz card attempted — score colors */
.quiz-card--pass {
  border-color: #D1FAE5 !important;
  background: #F0FDF4;
}
.quiz-card--pass:hover { border-color: #A7F3D0 !important; }

.quiz-card--avg {
  border-color: #FDE68A !important;
  background: #FFFBEB;
}
.quiz-card--avg:hover { border-color: #FCD34D !important; }

.quiz-card--fail {
  border-color: #FECACA !important;
  background: #FEF2F2;
}
.quiz-card--fail:hover { border-color: #FCA5A5 !important; }

.quiz-card-attempted {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  margin-top: 0.35rem;
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 6px;
  width: fit-content;
}
.quiz-card-attempted--pass {
  color: #065F46;
  background: #D1FAE5;
}
.quiz-card-attempted--pass strong { color: #10B981; }
.quiz-card-attempted--avg {
  color: #92400E;
  background: #FEF3C7;
}
.quiz-card-attempted--avg strong { color: #D97706; }
.quiz-card-attempted--fail {
  color: #991B1B;
  background: #FEE2E2;
}
.quiz-card-attempted--fail strong { color: #EF4444; }

/* Grades button on quiz card */
.quiz-grades-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #EFF6FF;
  border-radius: 6px;
  color: #3B82F6;
  cursor: pointer;
  transition: all 0.15s;
}
.quiz-grades-btn:hover {
  background: #DBEAFE;
  color: #2563EB;
}

/* Teacher grades panel */
.quiz-grades-panel {
  padding: 0;
}
.quiz-grades-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0.5rem 0 1.2rem;
}
.quiz-grades-loading {
  text-align: center;
  color: #9CA3AF;
  padding: 2rem;
  font-size: 0.9rem;
}
.quiz-grades-empty {
  text-align: center;
  color: #9CA3AF;
  padding: 2rem;
  font-size: 0.9rem;
  background: #F9FAFB;
  border: 1.5px dashed #E5E7EB;
  border-radius: 10px;
}
.quiz-grades-table-wrap {
  overflow-x: auto;
}
.quiz-grades-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}
.quiz-grades-table thead {
  background: #F3F4F6;
}
.quiz-grades-table th {
  text-align: left;
  padding: 0.6rem 0.75rem;
  font-weight: 600;
  color: #374151;
  border-bottom: 1.5px solid #E5E7EB;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.quiz-grades-table td {
  padding: 0.55rem 0.75rem;
  border-bottom: 1px solid #F3F4F6;
  color: #4B5563;
}
.quiz-grades-table tbody tr:hover {
  background: #F9FAFB;
}
.quiz-grades-name {
  font-weight: 500;
  color: #111827;
}
.quiz-grades-score {
  display: inline-block;
  padding: 0.15rem 0.5rem;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.8rem;
  background: #FEF3C7;
  color: #92400E;
}
.quiz-grades-score--good {
  background: #D1FAE5;
  color: #065F46;
}
.quiz-grades-score--bad {
  background: #FEE2E2;
  color: #991B1B;
}
.quiz-grades-date {
  font-size: 0.78rem;
  color: #9CA3AF;
}
.quiz-grades-avg {
  margin-top: 0.75rem;
  padding: 0.6rem 0.75rem;
  background: #F3F4F6;
  border-radius: 8px;
  font-size: 0.85rem;
  color: #4B5563;
}
.quiz-grades-avg strong {
  color: #111827;
}
</style>
