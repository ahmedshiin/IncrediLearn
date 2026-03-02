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

        <!-- Nav links -->
        <nav v-if="showNavbar && authStore.userRole !== 'admin'" class="nav-links">
          <RouterLink to="/" class="nav-link">Cours</RouterLink>
          <RouterLink to="/tuteurs" class="nav-link">Tuteurs</RouterLink>
          <RouterLink to="/about" class="nav-link">À propos</RouterLink>
        </nav>

        <!-- Auth - Non connecté -->
        <div v-if="!authStore.isAuthenticated" class="nav-auth">
          <RouterLink to="/connexion" class="btn-connexion">Connexion</RouterLink>
          <RouterLink to="/inscription" class="btn-inscrire">S'inscrire</RouterLink>
        </div>

        <!-- User Menu - Connecté -->
        <div v-else class="nav-auth">
          <div v-if="authStore.userRole === 'enseignant' && authStore.user?.verified" class="field">
            <RouterLink to="/creer-cours" class="btn-creer">Créer un cours</RouterLink>
          </div>
          <div class="user-menu-wrapper">
            <button @click="showUserMenu = !showUserMenu" class="user-menu-btn">
              <div class="user-avatar">
                {{ authStore.userFullName.charAt(0).toUpperCase() }}
              </div>
              <span class="user-name">{{ authStore.userFullName }}</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>

            <!-- Dropdown menu -->
            <div v-if="showUserMenu" class="user-dropdown">
              <div class="dropdown-header">
                <div class="dropdown-name">{{ authStore.userFullName }}</div>
                <div class="dropdown-role">{{ authStore.userRole === 'etudiant' ? 'Étudiant' : 'Enseignant' }}</div>
              </div>
              <div class="dropdown-divider"></div>
              <RouterLink to="/profil" class="dropdown-item" @click="showUserMenu = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                Mon profil
              </RouterLink>
              <RouterLink to="/mes-cours" class="dropdown-item" @click="showUserMenu = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
                  <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
                </svg>
                Mes cours
              </RouterLink>
              <RouterLink to="/messagerie" class="dropdown-item" @click="showUserMenu = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                Messagerie
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
  border-bottom: 1px solid #E5E7EB;
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-inner {
  width: 100%;
  padding: 0 2rem;
  height: 64px;
  display: flex;
  align-items: center;
}

/* Logo */
.nav-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: #10B981;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 1.2rem;
  font-weight: 700;
}

.logo-incredi { color: #111827; }
.logo-learn   { color: #10B981; }

/* Nav links */
.nav-links {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex: 1;
  justify-content: center;
}

.nav-link {
  padding: 0.35rem 0.85rem;
  font-size: 0.95rem;
  font-weight: 500;
  color: #374151;
  border-radius: 6px;
  transition: color 0.15s;
}

.nav-link:hover {
  color: #10B981;
}

/* Auth */
.nav-auth {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
  margin-left: auto;
}

.btn-creer {
  font-size: 0.85rem;
  font-weight: 600;
  color: #10B981;
  padding: 0.4rem 0.85rem;
  border-radius: 7px;
  border: 1.5px solid #D1FAE5;
  background: #F0FDF4;
  transition: background 0.15s, border-color 0.15s;
}
.btn-creer:hover { background: #D1FAE5; border-color: #A7F3D0; }

.btn-connexion {
  font-size: 0.95rem;
  font-weight: 500;
  color: #374151;
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  transition: color 0.15s;
}

.btn-connexion:hover { color: #10B981; }

.btn-inscrire {
  background: #10B981;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 0.5rem 1.2rem;
  border-radius: 8px;
  transition: background 0.15s;
}

.btn-inscrire:hover { background: #059669; }

/* User Menu */
.user-menu-wrapper {
  position: relative;
}

.user-menu-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.75rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  background: #ffffff;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.user-menu-btn:hover {
  border-color: #10B981;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.1);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #10B981;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
}

.user-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #111827;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  width: 240px;
  background: #ffffff;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  padding: 0.5rem;
  z-index: 1000;
}

.dropdown-header {
  padding: 0.75rem;
}

.dropdown-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #111827;
}

.dropdown-role {
  font-size: 0.8rem;
  color: #6B7280;
  margin-top: 0.15rem;
}

.dropdown-divider {
  height: 1px;
  background: #E5E7EB;
  margin: 0.5rem 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.65rem 0.75rem;
  border: none;
  background: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #374151;
  transition: background 0.15s;
  text-align: left;
}

.dropdown-item:hover {
  background: #F3F4F6;
}

.dropdown-item svg {
  flex-shrink: 0;
}

.dropdown-logout {
  color: #DC2626;
}

.dropdown-logout:hover {
  background: #FEE2E2;
}
</style>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

nav {
  width: 100%;
  font-size: 12px;
  text-align: center;
  margin-top: 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;

    padding: 1rem 0;
    margin-top: 1rem;
  }
}
</style>
