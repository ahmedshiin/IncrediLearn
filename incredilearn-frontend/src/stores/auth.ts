import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authService, type AuthResponse } from '@/services/authService'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthResponse | null>(null)

  const isAuthenticated = computed(() => !!user.value?.id)
  
  const userFullName = computed(() => user.value?.nom || '')
  
  const userRole = computed(() => user.value?.role || '')

  function initAuth() {
    const savedUser = authService.getCurrentUser()
    if (savedUser) {
      user.value = savedUser
    }
  }

  function setUser(userData: AuthResponse) {
    user.value = userData
  }

  function logout() {
    authService.logout()
    user.value = null
  }

  // Initialiser au démarrage
  initAuth()

  return {
    user,
    isAuthenticated,
    userFullName,
    userRole,
    setUser,
    logout,
    initAuth,
  }
})
