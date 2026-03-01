import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/inscription',
      name: 'inscription',
      component: () => import('../views/InscriptionView.vue'),
    },
    {
      path: '/connexion',
      name: 'connexion',
      component: () => import('../views/ConnexionView.vue'),
    },
    {
      path: '/cours/:id',
      name: 'cours',
      component: () => import('../views/CourseView.vue'),
    },
    {
      path: '/creer-cours',
      name: 'creer-cours',
      component: () => import('../views/CreateCourseView.vue'),
    },
  ],
})

export default router
