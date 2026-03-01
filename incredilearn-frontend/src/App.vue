<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, RouterLink, useRoute } from 'vue-router'

const route = useRoute()
const showNavbar = computed(() => !['inscription','connexion'].includes(String(route.name)))
const showHeader = computed(() => !['creer-cours'].includes(String(route.name)))
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
        <nav v-if="showNavbar" class="nav-links">
          <RouterLink to="/" class="nav-link">Cours</RouterLink>
          <RouterLink to="/tuteurs" class="nav-link">Tuteurs</RouterLink>
          <RouterLink to="/about" class="nav-link">À propos</RouterLink>
        </nav>

        <!-- Auth -->
        <div class="nav-auth">
          <RouterLink to="/creer-cours" class="btn-creer">Créer un cours</RouterLink>
          <RouterLink to="/connexion" class="btn-connexion">Connexion</RouterLink>
          <RouterLink to="/inscription" class="btn-inscrire">S'inscrire</RouterLink>
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
