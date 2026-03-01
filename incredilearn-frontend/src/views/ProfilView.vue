<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>

<template>
  <div class="profil-page">
    <div class="profil-container">
      <!-- Header -->
      <div class="profil-header">
        <div class="avatar-large">
          {{ authStore.userFullName.charAt(0).toUpperCase() }}
        </div>
        <h1 class="profil-title">{{ authStore.userFullName }}</h1>
        <p class="profil-role">
          {{ authStore.userRole === 'etudiant' ? 'Étudiant' : 'Enseignant' }}
        </p>
      </div>

      <!-- Informations -->
      <div class="profil-card">
        <h2 class="card-title">Informations personnelles</h2>
        
        <div class="info-grid">
          <div class="info-item">
            <label class="info-label">Nom complet</label>
            <p class="info-value">{{ authStore.userFullName }}</p>
          </div>

          <div class="info-item">
            <label class="info-label">Email</label>
            <p class="info-value">{{ authStore.user?.email }}</p>
          </div>

          <div class="info-item">
            <label class="info-label">Rôle</label>
            <p class="info-value">{{ authStore.userRole === 'etudiant' ? 'Étudiant' : 'Enseignant' }}</p>
          </div>

          <div v-if="authStore.user?.domain" class="info-item">
            <label class="info-label">Domaine d'expertise</label>
            <p class="info-value">{{ authStore.user?.domain }}</p>
          </div>

          <div class="info-item">
            <label class="info-label">Statut</label>
            <p class="info-value">
              <span v-if="authStore.user?.isVerified" class="badge badge-success">✓ Vérifié</span>
              <span v-else class="badge badge-warning">En attente de vérification</span>
            </p>
          </div>

          <div class="info-item">
            <label class="info-label">Membre depuis</label>
            <p class="info-value">{{ new Date().toLocaleDateString('fr-FR') }}</p>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="profil-actions">
        <button class="btn-primary">Modifier le profil</button>
        <button class="btn-secondary">Changer le mot de passe</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profil-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #F0FDF4 0%, #F0F9FF 100%);
  padding: 3rem 1rem;
}

.profil-container {
  max-width: 800px;
  margin: 0 auto;
}

/* Header */
.profil-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.avatar-large {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: #10B981;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 auto 1rem;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.3);
}

.profil-title {
  font-size: 2rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.5rem;
}

.profil-role {
  font-size: 1rem;
  color: #6B7280;
}

/* Card */
.profil-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  margin-bottom: 1.5rem;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 1.5rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-label {
  font-size: 0.85rem;
  font-weight: 500;
  color: #6B7280;
}

.info-value {
  font-size: 1rem;
  color: #111827;
  font-weight: 500;
}

.badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.badge-success {
  background: #D1FAE5;
  color: #059669;
}

.badge-warning {
  background: #FEF3C7;
  color: #D97706;
}

/* Actions */
.profil-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-primary,
.btn-secondary {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-primary {
  background: #10B981;
  color: #ffffff;
}

.btn-primary:hover {
  background: #059669;
}

.btn-secondary {
  background: #ffffff;
  color: #374151;
  border: 1.5px solid #E5E7EB;
}

.btn-secondary:hover {
  background: #F9FAFB;
  border-color: #10B981;
}
</style>
