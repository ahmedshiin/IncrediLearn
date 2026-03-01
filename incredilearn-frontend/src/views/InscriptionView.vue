<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

const role        = ref<'etudiant' | 'enseignant'>('etudiant')
const nom         = ref('')
const email       = ref('')
const password    = ref('')
const showPass    = ref(false)
const dragging    = ref(false)
const fichiers    = ref<File[]>([])

function onDrop(e: DragEvent) {
  dragging.value = false
  const files = e.dataTransfer?.files
  if (files) fichiers.value = [...fichiers.value, ...Array.from(files)]
}
function onFileInput(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (files) fichiers.value = [...fichiers.value, ...Array.from(files)]
}
</script>

<template>
  <!-- Full-page gradient background -->
  <div class="auth-bg">
    <div class="auth-card">

      <!-- Logo -->
      <div class="auth-logo">
        <div class="logo-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" fill="white"/>
            <path d="M2 12l10 5 10-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            <path d="M2 17l10 5 10-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          </svg>
        </div>
        <span class="logo-text">
          <span class="logo-incredi">Incredi</span><span class="logo-learn">Learn</span>
        </span>
      </div>

      <!-- Tabs -->
      <div class="auth-tabs">
        <RouterLink to="/connexion" class="tab">Connexion</RouterLink>
        <RouterLink to="/inscription" class="tab tab--active">Inscription</RouterLink>
      </div>

      <!-- Role selector -->
      <p class="role-label">Je suis…</p>
      <div class="role-grid">
        <button
          class="role-btn"
          :class="{ 'role-btn--active': role === 'etudiant' }"
          @click="role = 'etudiant'"
        >
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none"
            :stroke="role === 'etudiant' ? '#10B981' : '#6B7280'"
            stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          <span>Étudiant</span>
        </button>
        <button
          class="role-btn"
          :class="{ 'role-btn--active': role === 'enseignant' }"
          @click="role = 'enseignant'"
        >
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none"
            :stroke="role === 'enseignant' ? '#10B981' : '#6B7280'"
            stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
          <span>Enseignant</span>
        </button>
      </div>

      <!-- Form -->
      <form @submit.prevent class="auth-form">
        <!-- Nom complet -->
        <div class="field">
          <label class="field-label">Nom complet</label>
          <input v-model="nom" type="text" class="field-input" placeholder="Votre nom" />
        </div>

        <!-- Email -->
        <div class="field">
          <label class="field-label">Adresse email</label>
          <input v-model="email" type="email" class="field-input" placeholder="votre@email.com" />
        </div>

        <!-- Mot de passe -->
        <div class="field">
          <label class="field-label">Mot de passe</label>
          <div class="pass-wrap">
            <input
              v-model="password"
              :type="showPass ? 'text' : 'password'"
              class="field-input pass-input"
              placeholder="••••••••"
            />
            <button type="button" class="pass-toggle" @click="showPass = !showPass" aria-label="Afficher/masquer">
              <!-- Eye open -->
              <svg v-if="!showPass" width="18" height="18" viewBox="0 0 24 24" fill="none"
                stroke="#9CA3AF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <!-- Eye closed -->
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none"
                stroke="#9CA3AF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Justificatifs – enseignant uniquement -->
        <div v-if="role === 'enseignant'" class="field">
          <label class="field-label">Justificatifs (diplôme, certificat)</label>
          <div
            class="dropzone"
            :class="{ 'dropzone--over': dragging }"
            @dragover.prevent="dragging = true"
            @dragleave.prevent="dragging = false"
            @drop.prevent="onDrop"
            @click="($refs.fileInput as HTMLInputElement).click()"
          >
            <input ref="fileInput" type="file" multiple accept=".pdf,.jpg,.jpeg,.png" hidden @change="onFileInput" />
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none"
              stroke="#9CA3AF" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="16 16 12 12 8 16"/>
              <line x1="12" y1="12" x2="12" y2="21"/>
              <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/>
            </svg>
            <p class="dz-main">Glissez-déposez vos fichiers ici</p>
            <p class="dz-sub">ou cliquez pour parcourir (PDF, JPG, PNG)</p>
            <ul v-if="fichiers.length" class="dz-files">
              <li v-for="f in fichiers" :key="f.name">{{ f.name }}</li>
            </ul>
          </div>
        </div>

        <!-- Submit -->
        <button type="submit" class="btn-submit">Créer mon compte</button>
      </form>

      <!-- Social divider -->
      <div class="divider"><span>ou continuer avec</span></div>

      <!-- Social buttons -->
      <div class="social-btns">
        <button class="btn-social btn-google">
          <svg width="18" height="18" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          Google
        </button>
        <button class="btn-social btn-facebook">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="white">
            <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/>
          </svg>
          Facebook
        </button>
      </div>

      <!-- Legal -->
      <p class="auth-legal">
        En continuant, vous acceptez nos
        <a href="#">Conditions d'utilisation</a> et notre
        <a href="#">Politique de confidentialité</a>.
      </p>
    </div>
  </div>
</template>

<style scoped>
/* ── Background ── */
.auth-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #d1fae5 0%, #e0f2fe 50%, #ede9fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

/* ── Card ── */
.auth-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.10);
  padding: 2.25rem 2.5rem 1.75rem;
  width: 100%;
  max-width: 440px;
}

/* ── Logo ── */
.auth-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1.6rem;
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
.logo-text   { font-size: 1.25rem; font-weight: 700; }
.logo-incredi{ color: #111827; }
.logo-learn  { color: #10B981; }

/* ── Tabs ── */
.auth-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: #F3F4F6;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 1.6rem;
}
.tab {
  text-align: center;
  padding: 0.55rem 0;
  font-size: 0.9rem;
  font-weight: 500;
  color: #6B7280;
  border-radius: 7px;
  transition: background 0.15s, color 0.15s;
}
.tab--active {
  background: #ffffff;
  color: #111827;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

/* ── Role selector ── */
.role-label {
  font-size: 0.85rem;
  color: #6B7280;
  margin-bottom: 0.6rem;
}
.role-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  margin-bottom: 1.4rem;
}
.role-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.45rem;
  padding: 0.95rem 0.5rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 10px;
  background: #ffffff;
  cursor: pointer;
  font-size: 0.88rem;
  font-weight: 500;
  color: #374151;
  transition: border-color 0.15s, color 0.15s;
}
.role-btn--active {
  border-color: #10B981;
  color: #10B981;
}

/* ── Form ── */
.auth-form { display: flex; flex-direction: column; gap: 1rem; }

.field { display: flex; flex-direction: column; gap: 0.35rem; }
.field-label {
  font-size: 0.85rem;
  font-weight: 500;
  color: #374151;
}
.field-input {
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  padding: 0.65rem 0.9rem;
  font-size: 0.9rem;
  color: #111827;
  outline: none;
  transition: border-color 0.15s;
  width: 100%;
}
.field-input::placeholder { color: #9CA3AF; }
.field-input:focus { border-color: #10B981; }

/* Password */
.pass-wrap { position: relative; }
.pass-input { padding-right: 2.8rem; }
.pass-toggle {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 0;
}

/* Submit */
/* Dropzone */
.dropzone {
  border: 1.5px dashed #D1D5DB;
  border-radius: 10px;
  padding: 1.5rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
  background: #ffffff;
}
.dropzone:hover,
.dropzone--over {
  border-color: #10B981;
  background: #f0fdf4;
}
.dz-main {
  font-size: 0.85rem;
  font-weight: 500;
  color: #374151;
}
.dz-sub {
  font-size: 0.75rem;
  color: #9CA3AF;
}
.dz-files {
  list-style: none;
  margin-top: 0.4rem;
  font-size: 0.75rem;
  color: #10B981;
  text-align: center;
}

.btn-submit {
  margin-top: 0.25rem;
  background: #10B981;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  width: 100%;
  transition: background 0.15s;
}
.btn-submit:hover { background: #059669; }

/* Divider */
.divider {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 1.25rem 0 1rem;
  color: #9CA3AF;
  font-size: 0.8rem;
}
.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #E5E7EB;
}

/* Social */
.social-btns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
}
.btn-social {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem 0;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s;
}
.btn-social:hover { opacity: 0.85; }
.btn-google {
  background: #ffffff;
  border: 1.5px solid #E5E7EB;
  color: #374151;
}
.btn-facebook {
  background: #1877F2;
  border: none;
  color: #ffffff;
}

/* Legal */
.auth-legal {
  font-size: 0.75rem;
  color: #9CA3AF;
  text-align: center;
  line-height: 1.5;
}
.auth-legal a {
  color: #10B981;
  text-decoration: underline;
}
</style>
