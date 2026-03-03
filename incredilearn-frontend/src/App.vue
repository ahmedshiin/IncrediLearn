<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showNavbar = computed(() => !['inscription','connexion'].includes(String(route.name)))
const showHeader = computed(() => !['creer-cours'].includes(String(route.name)))
const showUserMenu = ref(false)

function handleLogout() {
  authStore.logout()
  showUserMenu.value = false
  router.push('/connexion')
}

// Fermer le menu quand on clique à l'extérieur
function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!target.closest('.user-menu-wrapper')) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div id="app">
    <!-- ── Navbar ── -->
    <header v-if="showHeader" class="navbar">
      <div class="navbar-inner">
        <!-- Logo -->
        <RouterLink to="/" class="nav-logo">
          <div class="logo-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7l10 5 10-5-10-5z" fill="white"/>
              <path d="M2 12l10 5 10-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
              <path d="M2 17l10 5 10-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            </svg>
          </div>
          <span class="logo-text">
            <span class="logo-incredi">Incredi</span><span class="logo-learn">Learn</span>
          </span>
        </RouterLink>

        <!-- Spacer -->
        <div class="nav-spacer"></div>

        <!-- Icon nav actions -->
        <div v-if="showNavbar" class="nav-icons">
          <!-- Explorer les cours -->
          <RouterLink to="/cours" class="nav-icon-btn" title="Explorer les cours"
            :class="{ active: route.path === '/cours' || route.path === '/' }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <span class="nav-icon-label">Explorer</span>
          </RouterLink>

          <!-- Mes cours (étudiant uniquement) -->
          <RouterLink v-if="authStore.isAuthenticated && authStore.userRole === 'etudiant'" to="/mes-cours" class="nav-icon-btn" title="Mes cours"
            :class="{ active: route.path === '/mes-cours' }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
            </svg>
            <span class="nav-icon-label">Mes cours</span>
          </RouterLink>

          <!-- Mes cours enseignant -->
          <RouterLink v-if="(authStore.userRole === 'enseignant' || authStore.userRole === 'admin') && authStore.isAuthenticated"
            to="/mes-cours-enseignant" class="nav-icon-btn" title="Gérer mes cours"
            :class="{ active: route.path === '/mes-cours-enseignant' }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 20h9"/>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
            </svg>
            <span class="nav-icon-label">Enseigner</span>
          </RouterLink>

          <!-- Messagerie -->
          <RouterLink v-if="authStore.isAuthenticated" to="/messagerie" class="nav-icon-btn" title="Messagerie"
            :class="{ active: route.path === '/messagerie' }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span class="nav-icon-label">Messages</span>
          </RouterLink>

          <!-- Admin -->
          <RouterLink v-if="authStore.userRole === 'admin'" to="/admin" class="nav-icon-btn nav-icon-btn--admin" title="Administration"
            :class="{ active: route.path === '/admin' }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            <span class="nav-icon-label">Admin</span>
          </RouterLink>
        </div>

        <!-- Auth - Non connecté -->
        <div v-if="!authStore.isAuthenticated" class="nav-auth">
          <RouterLink to="/connexion" class="btn-connexion">Connexion</RouterLink>
          <RouterLink to="/inscription" class="btn-inscrire">S'inscrire</RouterLink>
        </div>

        <!-- User Menu - Connecté -->
        <div v-else class="nav-auth">
          <!-- Créer un cours (enseignant) -->
          <RouterLink v-if="authStore.userRole === 'enseignant' && authStore.user?.verified"
            to="/creer-cours" class="btn-creer" title="Créer un cours">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            <span>Créer</span>
          </RouterLink>

          <!-- User avatar dropdown -->
          <div class="user-menu-wrapper">
            <button @click="showUserMenu = !showUserMenu" class="user-menu-btn">
              <div class="user-avatar">
                {{ authStore.userFullName.charAt(0).toUpperCase() }}
              </div>
              <svg class="chevron-icon" :class="{ open: showUserMenu }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>

            <!-- Dropdown menu -->
            <div v-if="showUserMenu" class="user-dropdown">
              <div class="dropdown-header">
                <div class="dropdown-avatar">{{ authStore.userFullName.charAt(0).toUpperCase() }}</div>
                <div>
                  <div class="dropdown-name">{{ authStore.userFullName }}</div>
                  <div class="dropdown-role">{{ authStore.userRole === 'etudiant' ? 'Étudiant' : authStore.userRole === 'admin' ? 'Administrateur' : 'Enseignant' }}</div>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <RouterLink to="/profil" class="dropdown-item" @click="showUserMenu = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                Mon profil
              </RouterLink>
              <RouterLink to="/about" class="dropdown-item" @click="showUserMenu = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="16" x2="12" y2="12"/>
                  <line x1="12" y1="8" x2="12.01" y2="8"/>
                </svg>
                À propos
              </RouterLink>
              <div class="dropdown-divider"></div>
              <button @click="handleLogout" class="dropdown-item dropdown-logout">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                  <polyline points="16 17 21 12 16 7"/>
                  <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                Déconnexion
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <RouterView />
  </div>
</template>

<style scoped>
.navbar {
  background: #ffffff;
  border-bottom: 1px solid #F0F0F0;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.92);
}

.navbar-inner {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1.5rem;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* Logo */
.nav-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
  text-decoration: none;
}

.logo-icon {
  width: 34px;
  height: 34px;
  background: linear-gradient(135deg, #10B981, #059669);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.25);
}

.logo-text {
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.logo-incredi { color: #111827; }
.logo-learn   { color: #10B981; }

/* Spacer */
.nav-spacer {
  flex: 1;
}

/* Icon nav buttons */
.nav-icons {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.nav-icon-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.45rem 0.85rem;
  border-radius: 10px;
  color: #6B7280;
  font-size: 0.82rem;
  font-weight: 500;
  transition: all 0.2s ease;
  text-decoration: none;
  position: relative;
}

.nav-icon-btn:hover {
  color: #10B981;
  background: #F0FDF4;
}

.nav-icon-btn.active {
  color: #10B981;
  background: #ECFDF5;
  font-weight: 600;
}

.nav-icon-btn svg {
  flex-shrink: 0;
}

.nav-icon-label {
  white-space: nowrap;
}

/* Admin icon variant */
.nav-icon-btn--admin {
  color: #7C3AED;
}

.nav-icon-btn--admin:hover {
  color: #6D28D9;
  background: #F5F3FF;
}

.nav-icon-btn--admin.active {
  color: #6D28D9;
  background: #EDE9FE;
}

/* Auth */
.nav-auth {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.btn-creer {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.82rem;
  font-weight: 600;
  color: #ffffff;
  padding: 0.4rem 0.9rem;
  border-radius: 9px;
  background: linear-gradient(135deg, #10B981, #059669);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.25);
  transition: all 0.2s ease;
  text-decoration: none;
}
.btn-creer:hover {
  box-shadow: 0 4px 14px rgba(16, 185, 129, 0.35);
  transform: translateY(-1px);
}

.btn-connexion {
  font-size: 0.9rem;
  font-weight: 500;
  color: #374151;
  padding: 0.4rem 0.85rem;
  border-radius: 8px;
  transition: color 0.15s;
  text-decoration: none;
}

.btn-connexion:hover { color: #10B981; }

.btn-inscrire {
  background: linear-gradient(135deg, #10B981, #059669);
  color: #ffffff;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 0.5rem 1.2rem;
  border-radius: 9px;
  transition: all 0.2s ease;
  text-decoration: none;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.25);
}

.btn-inscrire:hover {
  box-shadow: 0 4px 14px rgba(16, 185, 129, 0.35);
  transform: translateY(-1px);
}

/* User Menu */
.user-menu-wrapper {
  position: relative;
}

.user-menu-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.3rem;
  padding-right: 0.5rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 50px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-menu-btn:hover {
  border-color: #D1D5DB;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10B981, #059669);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.85rem;
}

.chevron-icon {
  color: #9CA3AF;
  transition: transform 0.2s ease;
}

.chevron-icon.open {
  transform: rotate(180deg);
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  width: 240px;
  background: #ffffff;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.03);
  padding: 0.5rem;
  z-index: 1000;
  animation: dropdown-in 0.15s ease;
}

@keyframes dropdown-in {
  from { opacity: 0; transform: translateY(-6px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.65rem 0.75rem;
}

.dropdown-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10B981, #059669);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.dropdown-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #111827;
}

.dropdown-role {
  font-size: 0.75rem;
  color: #9CA3AF;
  margin-top: 0.1rem;
}

.dropdown-divider {
  height: 1px;
  background: #F3F4F6;
  margin: 0.35rem 0.5rem;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  width: 100%;
  padding: 0.55rem 0.75rem;
  border: none;
  background: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  color: #374151;
  transition: all 0.15s ease;
  text-align: left;
  text-decoration: none;
}

.dropdown-item:hover {
  background: #F9FAFB;
  color: #111827;
}

.dropdown-item svg {
  flex-shrink: 0;
  color: #9CA3AF;
}

.dropdown-item:hover svg {
  color: #6B7280;
}

.dropdown-logout {
  color: #EF4444;
}

.dropdown-logout svg {
  color: #EF4444;
}

.dropdown-logout:hover {
  background: #FEF2F2;
  color: #DC2626;
}

.dropdown-logout:hover svg {
  color: #DC2626;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .nav-icon-label {
    display: none;
  }

  .nav-icon-btn {
    padding: 0.5rem;
    border-radius: 50%;
  }

  .btn-creer span {
    display: none;
  }

  .btn-creer {
    padding: 0.45rem;
    border-radius: 50%;
  }
}
</style>
