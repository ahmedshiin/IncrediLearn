<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminService, type AdminUser, type AdminStats, type DocMeta } from '@/services/adminService'
import { useAuthStore } from '@/stores/auth'
import ConfirmModal from '@/components/ConfirmModal.vue'

// ── Custom modal state ──
const modalVisible = ref(false)
const modalMessage = ref('')
const modalTitle = ref('')
const modalVariant = ref<'danger' | 'warning' | 'info'>('danger')
let modalResolve: ((v: boolean) => void) | null = null

function showConfirm(message: string, title = '', variant: 'danger' | 'warning' | 'info' = 'danger'): Promise<boolean> {
  modalMessage.value = message
  modalTitle.value = title
  modalVariant.value = variant
  modalVisible.value = true
  return new Promise(resolve => { modalResolve = resolve })
}
function onModalConfirm() { modalVisible.value = false; modalResolve?.(true); modalResolve = null }
function onModalCancel()  { modalVisible.value = false; modalResolve?.(false); modalResolve = null }

interface CourseItem {
  id: number
  titre: string
  description: string
  domain: string
  enseignantNom: string
  dureeHeures: number
  niveau: string
  nombreEtudiants: number
}

const router = useRouter()
const authStore = useAuthStore()

const activeSection = ref<'dashboard' | 'utilisateurs' | 'cours' | 'moderation'>('dashboard')
const stats = ref<AdminStats>({ totalUsers: 0, activeCourses: 0, pendingValidations: 0, blockedUsers: 0 })
const users = ref<AdminUser[]>([])
const allCourses = ref<CourseItem[]>([])
const loading = ref(true)
const searchQuery = ref('')
const roleFilter = ref<'all' | 'etudiant' | 'enseignant'>('all')
const showFilterDropdown = ref(false)

// Docs modal
const selectedTeacher = ref<AdminUser | null>(null)
const teacherDocs = ref<DocMeta[]>([])
const docsLoading = ref(false)

const pendingTeachers = computed(() => users.value.filter(u => u.role?.toLowerCase() === 'enseignant' && !u.isVerified))

const filteredUsers = computed(() => {
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u => {
    if (u.role?.toLowerCase() === 'admin') return false
    const matchesSearch = u.nom.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      u.role.toLowerCase().includes(q)
    const matchesRole = roleFilter.value === 'all' ||
      u.role?.toLowerCase() === roleFilter.value
    return matchesSearch && matchesRole
  })
})

const recentUsers = computed(() => filteredUsers.value.slice(0, 10))

onMounted(async () => {
  try {
    const [s, u, c] = await Promise.all([
      adminService.getStats(),
      adminService.getUsers(),
      fetch('http://localhost:8080/api/courses').then(r => r.json())
    ])
    stats.value = s
    users.value = u
    allCourses.value = c
  } catch (e) {
    console.error('Erreur chargement admin', e)
  } finally {
    loading.value = false
  }
})

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
}

function getInitials(nom: string) {
  return nom.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

function getAvatarColor(nom: string) {
  const colors = ['#10b981', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4']
  const idx = nom.charCodeAt(0) % colors.length
  return colors[idx]
}

async function toggleVerify(user: AdminUser) {
  const updated = await adminService.verifyUser(user.id, !user.isVerified)
  const idx = users.value.findIndex(u => u.id === user.id)
  if (idx !== -1) users.value[idx] = updated
  const s = await adminService.getStats()
  stats.value = s
}

async function toggleBlock(user: AdminUser) {
  const updated = await adminService.blockUser(user.id, !user.isBlocked)
  const idx = users.value.findIndex(u => u.id === user.id)
  if (idx !== -1) users.value[idx] = updated
  const s = await adminService.getStats()
  stats.value = s
}

async function refuseTeacher(user: AdminUser) {
  const ok = await showConfirm(`Refuser et supprimer le compte de ${user.nom} ?`, 'Refuser l\'enseignant', 'danger')
  if (!ok) return
  await adminService.deleteUser(user.id)
  users.value = users.value.filter(u => u.id !== user.id)
  const s = await adminService.getStats()
  stats.value = s
}

async function openDocsModal(user: AdminUser) {
  selectedTeacher.value = user
  docsLoading.value = true
  teacherDocs.value = await adminService.getUserDocuments(user.id)
  docsLoading.value = false
}

function closeDocsModal() {
  selectedTeacher.value = null
  teacherDocs.value = []
}

function setRoleFilter(v: 'all' | 'etudiant' | 'enseignant') {
  roleFilter.value = v
  showFilterDropdown.value = false
}

function logout() {
  authStore.logout()
  router.push('/connexion')
}

// SVG Line chart data
const lineData = [1000, 1300, 1700, 2100, 2400, 2900, 3100, 3300, 3600, 3900, 4200, 4500]
const months = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc']

const chartWidth = 460
const chartHeight = 160
const maxVal = 6000

function linePoints() {
  return lineData.map((v, i) => {
    const x = 20 + (i / (lineData.length - 1)) * (chartWidth - 40)
    const y = chartHeight - 10 - ((v / maxVal) * (chartHeight - 20))
    return `${x},${y}`
  }).join(' ')
}

function dotCx(i: number) {
  return 20 + (i / (lineData.length - 1)) * (chartWidth - 40)
}
function dotCy(v: number) {
  return chartHeight - 10 - ((v / maxVal) * (chartHeight - 20))
}

// Bar chart — computed from real course data
const barData = computed(() => {
  const domainMap: Record<string, number> = {}
  for (const c of allCourses.value) {
    const d = c.domain || 'Autre'
    domainMap[d] = (domainMap[d] || 0) + (c.nombreEtudiants || 0)
  }
  return Object.entries(domainMap)
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 5)
})
const barMax = computed(() => Math.max(...barData.value.map(b => b.value), 1) * 1.25)
const barChartW = 280
const barChartH = 160
const barWidth = 36
const barGap = 20

function barX(i: number) {
  return 20 + i * (barWidth + barGap)
}
function barH(v: number) {
  return (v / barMax.value) * (barChartH - 20)
}
function barY(v: number) {
  return barChartH - 10 - barH(v)
}
</script>

<template>
  <div class="admin-layout">
    <!-- SIDEBAR -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">IL</div>
        <div class="logo-text">
          <span class="logo-name">IncrediLearn</span>
          <span class="logo-sub">Administration</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <button
          class="nav-item"
          :class="{ active: activeSection === 'dashboard' }"
          @click="activeSection = 'dashboard'"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          Tableau de bord
        </button>
        <button
          class="nav-item"
          :class="{ active: activeSection === 'utilisateurs' }"
          @click="activeSection = 'utilisateurs'"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          Utilisateurs
          <span v-if="pendingTeachers.length > 0" class="badge-nav">{{ pendingTeachers.length }}</span>
        </button>
        <button
          class="nav-item"
          :class="{ active: activeSection === 'cours' }"
          @click="activeSection = 'cours'"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
          Cours
        </button>
        <button
          class="nav-item"
          :class="{ active: activeSection === 'moderation' }"
          @click="activeSection = 'moderation'"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          Modération
          <span v-if="stats.pendingValidations > 0" class="badge-nav">{{ stats.pendingValidations }}</span>
        </button>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-avatar" style="background:#10b981">AD</div>
        <div class="admin-info">
          <span class="admin-name">{{ authStore.user?.nom || 'Admin User' }}</span>
          <span class="admin-email">{{ authStore.user?.email || 'admin@incredilearn.com' }}</span>
        </div>
        <button class="logout-btn" @click="logout" title="Déconnexion">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </aside>

    <!-- MAIN CONTENT -->
    <main class="main-content">
      <!-- TOP BAR -->
      <div class="topbar">
        <div>
          <h1 class="page-title">Tableau de bord</h1>
          <p class="page-sub">Vue d'ensemble de la plateforme</p>
        </div>
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input v-model="searchQuery" type="text" placeholder="Rechercher..." />
        </div>
      </div>

      <!-- LOADING -->
      <div v-if="loading" class="loading-center">Chargement...</div>

      <!-- DASHBOARD SECTION -->
      <template v-else-if="activeSection === 'dashboard'">
        <!-- Stat cards -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon stat-icon-blue">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </div>
            <div class="stat-trend positive">+12.5%</div>
            <div class="stat-label">Total Utilisateurs</div>
            <div class="stat-value">{{ stats.totalUsers.toLocaleString() }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon stat-icon-green">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
            </div>
            <div class="stat-trend positive">+8.2%</div>
            <div class="stat-label">Cours Actifs</div>
            <div class="stat-value">{{ stats.activeCourses }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon stat-icon-orange">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            </div>
            <div class="stat-trend neutral">+{{ stats.pendingValidations }}</div>
            <div class="stat-label">Validations en Attente</div>
            <div class="stat-value">{{ stats.pendingValidations }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon stat-icon-red">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            </div>
            <div class="stat-trend negative">-{{ stats.blockedUsers }}</div>
            <div class="stat-label">Comptes Bloqués</div>
            <div class="stat-value">{{ stats.blockedUsers }}</div>
          </div>
        </div>

        <!-- Charts row -->
        <div class="charts-row">
          <div class="chart-card">
            <div class="chart-header">
              <div>
                <div class="chart-title">Tendances d'Inscription</div>
                <div class="chart-sub">Nouveaux utilisateurs par mois</div>
              </div>
              <button class="chart-more">⋮</button>
            </div>
            <svg :width="chartWidth + 20" :height="chartHeight + 30" style="display:block;margin:0 auto">
              <!-- Grid lines -->
              <line v-for="i in 4" :key="i" :x1="20" :y1="chartHeight - 10 - ((i/4) * (chartHeight-20))" :x2="chartWidth" :y2="chartHeight - 10 - ((i/4) * (chartHeight-20))" stroke="#e5e7eb" stroke-width="1"/>
              <!-- Y labels -->
              <text v-for="i in 4" :key="'y'+i" :x="15" :y="chartHeight - 10 - ((i/4) * (chartHeight-20)) + 4" text-anchor="end" font-size="9" fill="#9ca3af">{{ Math.round((i/4)*maxVal/1000) }}k</text>
              <!-- Line -->
              <polyline :points="linePoints()" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round"/>
              <!-- Dots -->
              <circle v-for="(v, i) in lineData" :key="'d'+i" :cx="dotCx(i)" :cy="dotCy(v)" r="4" fill="#10b981" stroke="white" stroke-width="2"/>
              <!-- Month labels -->
              <text v-for="(m, i) in months" :key="'m'+i" :x="dotCx(i)" :y="chartHeight + 22" text-anchor="middle" font-size="9" fill="#9ca3af">{{ m }}</text>
            </svg>
          </div>

          <div class="chart-card">
            <div class="chart-header">
              <div>
                <div class="chart-title">Catégories Populaires</div>
                <div class="chart-sub">Top 5 par nombre d'inscrits</div>
              </div>
            </div>
            <svg :width="barChartW + 20" :height="barChartH + 40" style="display:block;margin:0 auto">
              <!-- Grid lines -->
              <line v-for="i in 3" :key="i" :x1="10" :y1="barChartH - 10 - ((i/3) * (barChartH-20))" :x2="barChartW + 10" :y2="barChartH - 10 - ((i/3)*(barChartH-20))" stroke="#e5e7eb" stroke-width="1"/>
              <!-- Y labels -->
              <text v-for="i in 3" :key="'y'+i" :x="8" :y="barChartH - 10 - ((i/3)*(barChartH-20)) + 4" text-anchor="end" font-size="9" fill="#9ca3af">{{ Math.round((i/3)*barMax.value) }}</text>
              <!-- Bars -->
              <rect v-for="(b, i) in barData" :key="'b'+i"
                :x="barX(i) + 10" :y="barY(b.value)" :width="barWidth" :height="barH(b.value)"
                fill="#3b82f6" rx="3"
              />
              <!-- Value on top of each bar -->
              <text v-for="(b, i) in barData" :key="'bv'+i"
                :x="barX(i) + 10 + barWidth/2" :y="barY(b.value) - 3"
                text-anchor="middle" font-size="8" fill="#374151" font-weight="600"
              >{{ b.value }}</text>
              <!-- Labels -->
              <text v-for="(b, i) in barData" :key="'bl'+i"
                :x="barX(i) + 10 + barWidth/2" :y="barChartH + 22"
                text-anchor="middle" font-size="8" fill="#9ca3af"
              >{{ b.label.length > 7 ? b.label.slice(0, 7) + '.' : b.label }}</text>
            </svg>
          </div>
        </div>

        <!-- Recent Users Table -->
        <div class="table-card">
          <div class="table-header">
            <div>
              <div class="table-title">Utilisateurs Récents</div>
              <div class="table-sub">Dernières inscriptions sur la plateforme</div>
            </div>
            <button class="filter-btn" @click="showFilterDropdown = !showFilterDropdown">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
              Filtrer{{ roleFilter !== 'all' ? ` (${roleFilter === 'etudiant' ? 'Étudiant' : 'Instructeur'})` : '' }}
            </button>
            <div v-if="showFilterDropdown" class="filter-dropdown">
              <button @click="setRoleFilter('all')" :class="{ active: roleFilter === 'all' }">Tous</button>
              <button @click="setRoleFilter('etudiant')" :class="{ active: roleFilter === 'etudiant' }">Étudiant</button>
              <button @click="setRoleFilter('enseignant')" :class="{ active: roleFilter === 'enseignant' }">Instructeur</button>
            </div>
          </div>
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>UTILISATEUR</th>
                  <th>EMAIL</th>
                  <th>RÔLE</th>
                  <th>STATUT</th>
                  <th>DATE D'INSCRIPTION</th>
                  <th>COURS</th>
                  <th>JUSTIFICATIFS</th>
                  <th>ACTIONS</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in recentUsers" :key="u.id">
                  <td>
                    <div class="user-cell">
                      <div class="user-avatar" :style="{ background: getAvatarColor(u.nom) }">{{ getInitials(u.nom) }}</div>
                      <span class="user-name">{{ u.nom }}</span>
                    </div>
                  </td>
                  <td class="email-cell">{{ u.email }}</td>
                  <td>
                    <span class="role-badge" :class="u.role?.toLowerCase()">{{ u.role?.toLowerCase() === 'enseignant' ? 'Instructeur' : u.role?.toLowerCase() === 'admin' ? 'Admin' : 'Étudiant' }}</span>
                  </td>
                  <td>
                    <span v-if="u.isBlocked" class="status-badge blocked">⊘ Bloqué</span>
                    <span v-else-if="u.role?.toLowerCase() === 'enseignant' && !u.isVerified" class="status-badge pending">En attente</span>
                    <span v-else class="status-badge active">✓ Actif</span>
                  </td>
                  <td class="date-cell">{{ formatDate(u.createdAt) }}</td>
                  <td class="courses-cell">{{ u.courseCount }}</td>
                  <td>
                    <button v-if="u.role?.toLowerCase() === 'enseignant'" class="btn-action btn-info" @click="openDocsModal(u)">Voir</button>
                    <span v-else class="text-muted">-</span>
                  </td>
                  <td>
                    <div class="action-buttons">
                      <button v-if="u.role?.toLowerCase() === 'enseignant'" class="btn-action" :class="u.isVerified ? 'btn-warn' : 'btn-success'" @click="toggleVerify(u)">
                        {{ u.isVerified ? 'Révoquer' : 'Valider' }}
                      </button>
                      <button class="btn-action" :class="u.isBlocked ? 'btn-success' : 'btn-danger'" @click="toggleBlock(u)">
                        {{ u.isBlocked ? 'Débloquer' : 'Bloquer' }}
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="recentUsers.length === 0">
                  <td colspan="8" class="empty-row">Aucun utilisateur</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>

      <!-- UTILISATEURS SECTION -->
      <template v-else-if="activeSection === 'utilisateurs'">
        <div class="table-card">
          <div class="table-header">
            <div>
              <div class="table-title">Tous les Utilisateurs</div>
              <div class="table-sub">Gestion complète des comptes</div>
            </div>
            <button class="filter-btn" @click="showFilterDropdown = !showFilterDropdown">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
              Filtrer{{ roleFilter !== 'all' ? ` (${roleFilter === 'etudiant' ? 'Étudiant' : 'Instructeur'})` : '' }}
            </button>
            <div v-if="showFilterDropdown" class="filter-dropdown">
              <button @click="setRoleFilter('all')" :class="{ active: roleFilter === 'all' }">Tous</button>
              <button @click="setRoleFilter('etudiant')" :class="{ active: roleFilter === 'etudiant' }">Étudiant</button>
              <button @click="setRoleFilter('enseignant')" :class="{ active: roleFilter === 'enseignant' }">Instructeur</button>
            </div>
          </div>
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>UTILISATEUR</th>
                  <th>EMAIL</th>
                  <th>RÔLE</th>
                  <th>DOMAINE</th>
                  <th>STATUT</th>
                  <th>DATE D'INSCRIPTION</th>
                  <th>COURS</th>
                  <th>JUSTIFICATIFS</th>
                  <th>ACTIONS</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in filteredUsers" :key="u.id">
                  <td>
                    <div class="user-cell">
                      <div class="user-avatar" :style="{ background: getAvatarColor(u.nom) }">{{ getInitials(u.nom) }}</div>
                      <span class="user-name">{{ u.nom }}</span>
                    </div>
                  </td>
                  <td class="email-cell">{{ u.email }}</td>
                  <td>
                    <span class="role-badge" :class="u.role?.toLowerCase()">{{ u.role?.toLowerCase() === 'enseignant' ? 'Instructeur' : u.role?.toLowerCase() === 'admin' ? 'Admin' : 'Étudiant' }}</span>
                  </td>
                  <td class="date-cell">{{ u.domain || '-' }}</td>
                  <td>
                    <span v-if="u.isBlocked" class="status-badge blocked">⊘ Bloqué</span>
                    <span v-else-if="u.role?.toLowerCase() === 'enseignant' && !u.isVerified" class="status-badge pending">En attente</span>
                    <span v-else class="status-badge active">✓ Actif</span>
                  </td>
                  <td class="date-cell">{{ formatDate(u.createdAt) }}</td>
                  <td class="courses-cell">{{ u.courseCount }}</td>
                  <td>
                    <button v-if="u.role?.toLowerCase() === 'enseignant'" class="btn-action btn-info" @click="openDocsModal(u)">Voir</button>
                    <span v-else class="text-muted">-</span>
                  </td>
                  <td>
                    <div class="action-buttons">
                      <button v-if="u.role?.toLowerCase() === 'enseignant'" class="btn-action" :class="u.isVerified ? 'btn-warn' : 'btn-success'" @click="toggleVerify(u)">
                        {{ u.isVerified ? 'Révoquer' : 'Valider' }}
                      </button>
                      <button class="btn-action" :class="u.isBlocked ? 'btn-success' : 'btn-danger'" @click="toggleBlock(u)">
                        {{ u.isBlocked ? 'Débloquer' : 'Bloquer' }}
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="filteredUsers.length === 0">
                  <td colspan="9" class="empty-row">Aucun utilisateur trouvé</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>

      <!-- COURS SECTION -->
      <template v-else-if="activeSection === 'cours'">
        <div class="table-card">
          <div class="table-header">
            <div>
              <div class="table-title">Gestion des Cours</div>
              <div class="table-sub">{{ allCourses.length }} cours actifs sur la plateforme</div>
            </div>
          </div>
          <div v-if="allCourses.length === 0" class="empty-section">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
            <p>Aucun cours disponible pour le moment</p>
          </div>
          <div v-else class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>TITRE</th>
                  <th>INSTRUCTEUR</th>
                  <th>DOMAINE</th>
                  <th>NIVEAU</th>
                  <th>DURÉE</th>
                  <th>ÉTUDIANTS</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in allCourses" :key="c.id">
                  <td>
                    <div class="course-title-cell">
                      <span class="course-title">{{ c.titre }}</span>
                      <span class="course-desc">{{ c.description?.slice(0, 60) }}{{ c.description?.length > 60 ? '...' : '' }}</span>
                    </div>
                  </td>
                  <td class="email-cell">{{ c.enseignantNom || '-' }}</td>
                  <td>{{ c.domain || '-' }}</td>
                  <td>
                    <span class="role-badge" :class="c.niveau === 'Débutant' ? 'etudiant' : c.niveau === 'Avancé' ? 'enseignant' : 'admin'">{{ c.niveau || '-' }}</span>
                  </td>
                  <td class="date-cell">{{ c.dureeHeures ? c.dureeHeures + 'h' : '-' }}</td>
                  <td class="courses-cell">{{ c.nombreEtudiants ?? 0 }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>

      <!-- MODÉRATION SECTION -->
      <template v-else-if="activeSection === 'moderation'">
        <div class="table-card">
          <div class="table-header">
            <div>
              <div class="table-title">Validation des Enseignants</div>
              <div class="table-sub">Demandes en attente de validation</div>
            </div>
            <span class="badge-count">{{ pendingTeachers.length }} en attente</span>
          </div>
          <div v-if="pendingTeachers.length === 0" class="empty-section">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            <p>Aucune validation en attente — tout est à jour !</p>
          </div>
          <div v-else class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ENSEIGNANT</th>
                  <th>EMAIL</th>
                  <th>DOMAINE</th>
                  <th>DATE D'INSCRIPTION</th>
                  <th>ACTIONS</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in pendingTeachers" :key="u.id">
                  <td>
                    <div class="user-cell">
                      <div class="user-avatar" :style="{ background: getAvatarColor(u.nom) }">{{ getInitials(u.nom) }}</div>
                      <span class="user-name">{{ u.nom }}</span>
                    </div>
                  </td>
                  <td class="email-cell">{{ u.email }}</td>
                  <td class="date-cell">{{ u.domain || '-' }}</td>
                  <td class="date-cell">{{ formatDate(u.createdAt) }}</td>
                  <td>
                    <div class="action-buttons">
                      <button class="btn-action btn-info" @click="openDocsModal(u)">Voir docs</button>
                      <button class="btn-action btn-success" @click="toggleVerify(u)">✓ Valider</button>
                      <button class="btn-action btn-danger" @click="refuseTeacher(u)">✗ Refuser</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>
    </main>

    <!-- Documents Modal -->
    <div v-if="selectedTeacher" class="modal-overlay" @click.self="closeDocsModal">
      <div class="modal-box">
        <div class="modal-header">
          <div>
            <div class="modal-title">Documents de {{ selectedTeacher.nom }}</div>
            <div class="modal-sub">{{ selectedTeacher.email }}</div>
          </div>
          <button class="modal-close" @click="closeDocsModal">×</button>
        </div>
        <div class="modal-body">
          <div v-if="docsLoading" class="docs-loading">… Chargement</div>
          <div v-else-if="teacherDocs.length === 0" class="docs-empty">Aucun document uploadé</div>
          <ul v-else class="doc-list">
            <li v-for="doc in teacherDocs" :key="doc.id" class="doc-item">
              <span class="doc-name">{{ doc.fileName }}</span>
              <a :href="`http://localhost:8080/api/documents/${doc.id}`" target="_blank" class="btn-view-doc">Voir</a>
            </li>
          </ul>
        </div>
        <div class="modal-footer">
          <button class="btn-action btn-success" @click="toggleVerify(selectedTeacher!); closeDocsModal()">✓ Valider</button>
          <button class="btn-action btn-danger" @click="refuseTeacher(selectedTeacher!); closeDocsModal()">Refuser</button>
          <button class="btn-action btn-warn" @click="closeDocsModal">Annuler</button>
        </div>
      </div>
    </div>
  </div>

  <ConfirmModal
    :visible="modalVisible"
    :message="modalMessage"
    :title="modalTitle"
    :variant="modalVariant"
    type="confirm"
    confirm-text="Confirmer"
    cancel-text="Annuler"
    @confirm="onModalConfirm"
    @cancel="onModalCancel"
  />
</template>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f9fafb;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* SIDEBAR */
.sidebar {
  width: 220px;
  min-width: 220px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  padding: 20px 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.logo-icon {
  background: #10b981;
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-name {
  font-weight: 700;
  font-size: 14px;
  color: #111827;
}

.logo-sub {
  font-size: 11px;
  color: #6b7280;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #6b7280;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  width: 100%;
  transition: all 0.15s;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #111827;
}

.nav-item.active {
  background: #ecfdf5;
  color: #10b981;
  font-weight: 600;
}

.badge-nav {
  margin-left: auto;
  background: #fef3c7;
  color: #d97706;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
}

.sidebar-footer {
  padding: 14px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.admin-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-name {
  font-size: 12px;
  font-weight: 600;
  color: #111827;
  truncate: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.admin-email {
  font-size: 10px;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logout-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  color: #9ca3af;
  padding: 4px;
  border-radius: 4px;
}
.logout-btn:hover { color: #ef4444; }

/* MAIN CONTENT */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.page-sub {
  font-size: 13px;
  color: #6b7280;
  margin: 2px 0 0;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px 14px;
  min-width: 220px;
}

.search-box input {
  border: none;
  outline: none;
  font-size: 13px;
  color: #374151;
  background: transparent;
  width: 100%;
}

/* STATS */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 18px;
  position: relative;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}
.stat-icon-blue { background: #eff6ff; color: #3b82f6; }
.stat-icon-green { background: #ecfdf5; color: #10b981; }
.stat-icon-orange { background: #fff7ed; color: #f97316; }
.stat-icon-red { background: #fef2f2; color: #ef4444; }

.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 11px;
  font-weight: 600;
}
.stat-trend.positive { color: #10b981; }
.stat-trend.negative { color: #ef4444; }
.stat-trend.neutral { color: #f97316; }

.stat-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #111827;
}

/* CHARTS */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.chart-sub {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 2px;
}

.chart-more {
  border: none;
  background: transparent;
  cursor: pointer;
  color: #9ca3af;
  font-size: 18px;
}

/* TABLE */
.table-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  overflow: hidden;
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

.table-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.table-sub {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 2px;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 7px 14px;
  border-radius: 8px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
}
.filter-btn:hover { background: #f9fafb; }

.table-header { position: relative; }
.filter-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 100;
  min-width: 140px;
  overflow: hidden;
}
.filter-dropdown button {
  display: block;
  width: 100%;
  padding: 9px 16px;
  text-align: left;
  background: none;
  border: none;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  transition: background 0.15s;
}
.filter-dropdown button:hover { background: #f3f4f6; }
.filter-dropdown button.active { background: #eff6ff; color: #3b82f6; font-weight: 600; }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}
.modal-box {
  background: #fff;
  border-radius: 14px;
  width: 520px;
  max-width: 94vw;
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
}
.modal-title { font-size: 16px; font-weight: 700; color: #111827; }
.modal-sub { font-size: 12px; color: #6b7280; margin-top: 2px; }
.modal-close {
  background: none;
  border: none;
  font-size: 22px;
  color: #9ca3af;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}
.modal-close:hover { color: #374151; }
.modal-body { padding: 20px 24px; min-height: 80px; }
.docs-loading, .docs-empty { color: #9ca3af; font-size: 13px; }
.doc-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 8px; }
.doc-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 12px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}
.doc-name { font-size: 13px; color: #374151; word-break: break-all; }
.btn-view-doc {
  padding: 4px 12px;
  background: #eff6ff;
  color: #3b82f6;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  text-decoration: none;
  white-space: nowrap;
  flex-shrink: 0;
  margin-left: 12px;
  transition: background 0.15s;
}
.btn-view-doc:hover { background: #dbeafe; }
.modal-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 14px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}
.btn-info { background: #eff6ff; color: #3b82f6; border: 1px solid #bfdbfe; }
.text-muted { color: #9ca3af; font-size: 13px; }
.course-title-cell { display: flex; flex-direction: column; gap: 2px; }
.course-title { font-size: 13px; font-weight: 600; color: #111827; }
.course-desc { font-size: 11px; color: #9ca3af; }

.badge-count {
  background: #fef3c7;
  color: #d97706;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead th {
  padding: 10px 16px;
  text-align: left;
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid #f3f4f6;
  white-space: nowrap;
}

tbody td {
  padding: 12px 16px;
  border-bottom: 1px solid #f9fafb;
  font-size: 13px;
  color: #374151;
  vertical-align: middle;
}

tbody tr:last-child td { border-bottom: none; }
tbody tr:hover { background: #fafafa; }

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-name { font-weight: 500; color: #111827; }
.email-cell { color: #6b7280; }
.date-cell { color: #6b7280; font-size: 12px; white-space: nowrap; }
.courses-cell { font-weight: 600; color: #374151; }

.role-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}
.role-badge.etudiant { background: #eff6ff; color: #3b82f6; }
.role-badge.enseignant { background: #ecfdf5; color: #10b981; }
.role-badge.admin { background: #fef3c7; color: #d97706; }

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}
.status-badge.active { background: #ecfdf5; color: #10b981; }
.status-badge.blocked { background: #fef2f2; color: #ef4444; }
.status-badge.pending { background: #fff7ed; color: #f97316; }

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.btn-action {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  white-space: nowrap;
  transition: opacity 0.15s;
}
.btn-action:hover { opacity: 0.8; }
.btn-success { background: #ecfdf5; color: #10b981; }
.btn-danger { background: #fef2f2; color: #ef4444; }
.btn-warn { background: #fef3c7; color: #d97706; }

.empty-row {
  text-align: center;
  color: #9ca3af;
  padding: 32px;
}

.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px;
  color: #9ca3af;
  font-size: 14px;
}

.loading-center {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #9ca3af;
  font-size: 14px;
}

@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}
</style>
