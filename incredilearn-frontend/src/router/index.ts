import { createRouter, createWebHistory } from 'vue-router'
import { authService } from '@/services/authService'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: false }, // Page publique
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: { requiresAuth: false }, // Page publique
    },
    {
      path: '/inscription',
      name: 'inscription',
      component: () => import('../views/InscriptionView.vue'),
      meta: { requiresAuth: false, guestOnly: true }, // Seulement pour non-connectés
    },
    {
      path: '/connexion',
      name: 'connexion',
      component: () => import('../views/ConnexionView.vue'),
      meta: { requiresAuth: false, guestOnly: true }, // Seulement pour non-connectés
    },
    {
      path: '/cours',
      name: 'cours-liste',
      component: () => import('../views/CoursesListView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/cours/:id',
      name: 'cours',
      component: () => import('../views/CourseView.vue'),
      meta: { requiresAuth: true }, // Page protégée
    },
    {
      path: '/profil',
      name: 'profil',
      component: () => import('../views/ProfilView.vue'),
      meta: { requiresAuth: true }, // Page protégée
    },
    {
      path: '/mes-cours',
      name: 'mes-cours',
      component: () => import('../views/MesCoursView.vue'),
      meta: { requiresAuth: true }, // Page protégée
    },
    {
      path: '/tuteurs',
      name: 'tuteurs',
      component: () => import('../views/HomeView.vue'), // TODO: créer TuteursView
      meta: { requiresAuth: false }, // Page publique
    },
    {
      path: '/creer-cours',
      name: 'creer-cours',
      component: () => import('../views/CreateCourseView.vue'),
    },
    {
      path: '/mes-cours-enseignant',
      name: 'mes-cours-enseignant',
      component: () => import('../views/MesCoursEnseignantView.vue'),
      meta: { requiresAuth: true, enseignantOnly: true },
    },
    {
      path: '/messagerie',
      name: 'messagerie',
      component: () => import('../views/MessagerieView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminDashboardView.vue'),
      meta: { requiresAuth: true, adminOnly: true },
    },
  ],
})

// Navigation guard pour protéger les routes
router.beforeEach((to, from, next) => {
  const isAuthenticated = authService.isAuthenticated()
  const currentUser = authService.getCurrentUser()
  const requiresAuth = to.meta.requiresAuth
  const guestOnly = to.meta.guestOnly
  const adminOnly = to.meta.adminOnly

  const enseignantOnly = to.meta.enseignantOnly

  if (adminOnly && currentUser?.role !== 'admin') {
    next({ name: 'home' })
  } else if (enseignantOnly && currentUser?.role !== 'enseignant' && currentUser?.role !== 'admin') {
    next({ name: 'home' })
  } else if (requiresAuth && !isAuthenticated) {
    next({
      name: 'connexion',
      query: { redirect: to.fullPath }, // Sauvegarder la page cible pour redirection après connexion
    })
  }
  // Si la route est réservée aux invités (connexion/inscription) et l'utilisateur est déjà connecté
  else if (guestOnly && isAuthenticated) {
    next({ name: 'home' })
  }
  // Sinon, autoriser l'accès
  else {
    next()
  }
})

export default router
