<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

/* ── Form state ── */
const courseTitle   = ref('')
const description   = ref('')
const category      = ref('')
const level         = ref('')
const coverImage    = ref<File | null>(null)
const coverPreview  = ref('')
const draggingCover = ref(false)

const categories = ['Programmation', 'Design', 'Marketing', 'Bureautique', 'Business', 'Intelligence Artificielle']
const levels     = ['Débutant', 'Intermédiaire', 'Avancé']

/* ── Course content items ── */
interface ContentItem {
  id: number
  name: string
  type: 'Video' | 'Document'
  size: string
  file?: File
}
const contentItems = ref<ContentItem[]>([
  { id: 1, name: 'Introduction.mp4',    type: 'Video',    size: '45 MB' },
  { id: 2, name: 'Course Overview.pdf', type: 'Document', size: '2.3 MB' },
])
let nextId = 3

/* ── Cover image drop ── */
function onCoverDrop(e: DragEvent) {
  draggingCover.value = false
  const file = e.dataTransfer?.files[0]
  if (file && file.type.startsWith('image/')) setCover(file)
}
function onCoverInput(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) setCover(file)
}
function setCover(file: File) {
  coverImage.value = file
  coverPreview.value = URL.createObjectURL(file)
}

/* ── Add content ── */
const videoInput = ref<HTMLInputElement | null>(null)
const docInput   = ref<HTMLInputElement | null>(null)

function addVideo() { videoInput.value?.click() }
function addDocument() { docInput.value?.click() }

function onVideoAdded(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const sizeKB = file.size / 1024
  const sizeStr = sizeKB > 1024 ? `${(sizeKB/1024).toFixed(1)} MB` : `${Math.round(sizeKB)} KB`
  contentItems.value.push({ id: nextId++, name: file.name, type: 'Video', size: sizeStr, file })
  ;(e.target as HTMLInputElement).value = ''
}
function onDocAdded(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const sizeKB = file.size / 1024
  const sizeStr = sizeKB > 1024 ? `${(sizeKB/1024).toFixed(1)} MB` : `${Math.round(sizeKB)} KB`
  contentItems.value.push({ id: nextId++, name: file.name, type: 'Document', size: sizeStr, file })
  ;(e.target as HTMLInputElement).value = ''
}

function removeItem(id: number) {
  contentItems.value = contentItems.value.filter(i => i.id !== id)
}

/* ── Drag-to-reorder ── */
const draggedIdx = ref<number | null>(null)
function onItemDragStart(idx: number) { draggedIdx.value = idx }
function onItemDragOver(e: DragEvent, idx: number) {
  e.preventDefault()
  if (draggedIdx.value === null || draggedIdx.value === idx) return
  const items = [...contentItems.value]
  const [moved] = items.splice(draggedIdx.value, 1)
  if (!moved) return
  items.splice(idx, 0, moved)
  contentItems.value = items
  draggedIdx.value = idx
}
function onItemDragEnd() { draggedIdx.value = null }

/* ── Submit ── */
function createCourse() {
  if (!courseTitle.value.trim()) {
    alert('Veuillez saisir un titre de cours.')
    return
  }
  alert('Cours créé avec succès !')
  router.push('/')
}
</script>

<template>
  <div class="page">
    <!-- ── Top bar ── -->
    <header class="topbar">
      <div class="topbar-left">
        <button class="back-btn" @click="router.back()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
        </button>
        <div class="topbar-logo-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 10v6M2 10l10-5 10 5-10 5-10-5z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M6 12v5c0 1.657 2.686 3 6 3s6-1.343 6-3v-5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="topbar-title-group">
          <h1 class="topbar-title">Create New Course</h1>
          <p class="topbar-sub">Share your knowledge with the world</p>
        </div>
      </div>
      <span class="draft-badge">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
        </svg>
        Draft
      </span>
    </header>

    <!-- ── Scrollable form ── -->
    <main class="form-scroll">
      <div class="form-inner">

        <!-- ════ COURSE DETAILS ════ -->
        <div class="card-section">
          <div class="card-section-header">
            <div class="section-icon-box">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="#10B981" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7" rx="1"/>
                <rect x="14" y="3" width="7" height="7" rx="1"/>
                <rect x="3" y="14" width="7" height="7" rx="1"/>
                <rect x="14" y="14" width="7" height="7" rx="1"/>
              </svg>
            </div>
            <span class="card-section-title">Course Details</span>
          </div>

          <div class="form-group">
            <label class="form-label">Course Title <span class="required">*</span></label>
            <input
              v-model="courseTitle"
              class="form-input"
              type="text"
              placeholder="e.g., Complete Web Development Bootcamp 2024"
            />
          </div>

          <div class="form-group">
            <label class="form-label">Description</label>
            <textarea
              v-model="description"
              class="form-textarea"
              rows="5"
              placeholder="Describe what students will learn, the skills they'll gain, and why they should take this course..."
            />
          </div>

          <div class="form-row">
            <div class="form-group form-group--half">
              <label class="form-label">Category</label>
              <div class="select-wrap">
                <select v-model="category" class="form-select">
                  <option value="" disabled>Select a category</option>
                  <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                </select>
                <svg class="select-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none"
                  stroke="#9CA3AF" stroke-width="2.5" stroke-linecap="round"><polyline points="6 9 12 15 18 9"/></svg>
              </div>
            </div>
            <div class="form-group form-group--half">
              <label class="form-label">Level</label>
              <div class="select-wrap">
                <select v-model="level" class="form-select">
                  <option value="" disabled>Select level</option>
                  <option v-for="lvl in levels" :key="lvl" :value="lvl">{{ lvl }}</option>
                </select>
                <svg class="select-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none"
                  stroke="#9CA3AF" stroke-width="2.5" stroke-linecap="round"><polyline points="6 9 12 15 18 9"/></svg>
              </div>
            </div>
          </div>
        </div>

        <!-- ════ COVER IMAGE ════ -->
        <div class="card-section">
          <div class="card-section-header">
            <div class="section-icon-box">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="#10B981" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
            </div>
            <span class="card-section-title">Cover Image</span>
          </div>

          <div
            class="drop-zone"
            :class="{ 'drop-zone--active': draggingCover, 'drop-zone--filled': !!coverPreview }"
            @dragover.prevent="draggingCover = true"
            @dragleave="draggingCover = false"
            @drop.prevent="onCoverDrop"
            @click="($refs.coverInput as HTMLInputElement).click()"
          >
            <img v-if="coverPreview" :src="coverPreview" class="cover-preview" alt="Aperçu" />
            <template v-else>
              <div class="drop-icon-box">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none"
                  stroke="#10B981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="16 16 12 12 8 16"/>
                  <line x1="12" y1="12" x2="12" y2="21"/>
                  <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/>
                </svg>
              </div>
              <p class="drop-label">Drag &amp; drop your cover image</p>
              <p class="drop-sub">PNG, JPG or WebP — max 5 MB</p>
            </template>
            <input ref="coverInput" type="file" accept="image/*" class="hidden-input" @change="onCoverInput" />
          </div>
        </div>

        <!-- ════ COURSE CONTENT ════ -->
        <div class="card-section">
          <div class="card-section-header">
            <div class="section-icon-box">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="#10B981" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div>
              <span class="card-section-title">Course Content</span>
              <span class="content-count">{{ contentItems.length }} item{{ contentItems.length !== 1 ? 's' : '' }} added</span>
            </div>
          </div>

          <!-- File list -->
          <div class="content-list">
            <div
              v-for="(item, idx) in contentItems"
              :key="item.id"
              class="content-row"
              draggable="true"
              @dragstart="onItemDragStart(idx)"
              @dragover="onItemDragOver($event, idx)"
              @dragend="onItemDragEnd"
              :class="{ 'content-row--dragging': draggedIdx === idx }"
            >
              <!-- Drag handle -->
              <div class="drag-handle">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="2">
                  <line x1="8" y1="6" x2="8" y2="6"/><line x1="16" y1="6" x2="16" y2="6"/>
                  <line x1="8" y1="12" x2="8" y2="12"/><line x1="16" y1="12" x2="16" y2="12"/>
                  <line x1="8" y1="18" x2="8" y2="18"/><line x1="16" y1="18" x2="16" y2="18"/>
                  <circle cx="8" cy="6" r="1.5" fill="#D1D5DB"/><circle cx="16" cy="6" r="1.5" fill="#D1D5DB"/>
                  <circle cx="8" cy="12" r="1.5" fill="#D1D5DB"/><circle cx="16" cy="12" r="1.5" fill="#D1D5DB"/>
                  <circle cx="8" cy="18" r="1.5" fill="#D1D5DB"/><circle cx="16" cy="18" r="1.5" fill="#D1D5DB"/>
                </svg>
              </div>

              <!-- Icon -->
              <div class="content-icon" :class="item.type === 'Video' ? 'content-icon--video' : 'content-icon--doc'">
                <!-- Video icon -->
                <svg v-if="item.type === 'Video'" width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="2" y="3" width="15" height="15" rx="2"/>
                  <polygon points="22 8 22 16 17 12 22 8"/>
                </svg>
                <!-- Document icon -->
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </div>

              <!-- Info -->
              <div class="content-info">
                <span class="content-index">{{ String(idx + 1).padStart(2, '0') }}.</span>
                <span class="content-name">{{ item.name }}</span>
                <span class="content-type-badge" :class="item.type === 'Video' ? 'badge-video' : 'badge-doc'">
                  {{ item.type }}
                </span>
                <span class="content-size">{{ item.size }}</span>
              </div>

              <!-- Remove -->
              <button class="remove-btn" @click="removeItem(item.id)" title="Supprimer">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Add buttons -->
          <div class="add-buttons">
            <button class="add-btn" @click="addVideo">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="3" width="15" height="15" rx="2"/>
                <polygon points="22 8 22 16 17 12 22 8"/>
              </svg>
              Add Video
            </button>
            <button class="add-btn" @click="addDocument">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              Add Document
            </button>
          </div>

          <!-- Hidden file inputs -->
          <input ref="videoInput" type="file" accept="video/*" class="hidden-input" @change="onVideoAdded" />
          <input ref="docInput"   type="file" accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx" class="hidden-input" @change="onDocAdded" />
        </div>

        <!-- ════ ACTION BUTTONS ════ -->
        <div class="actions">
          <button class="btn-create" @click="createCourse">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            Create Course
          </button>
          <button class="btn-cancel" @click="router.back()">Cancel</button>
        </div>

      </div>
    </main>
  </div>
</template>

<style scoped>
/* ── Page layout ── */
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #d1fae5 0%, #e0f2fe 50%, #ede9fe 100%);
  display: flex;
  flex-direction: column;
  font-family: 'Inter', sans-serif;
}

/* ── Topbar ── */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
  background: #ffffff;
  border-bottom: 1px solid #F3F4F6;
  position: sticky;
  top: 0;
  z-index: 10;
}
.topbar-left { display: flex; align-items: center; gap: 0.85rem; }
.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  border: 1.5px solid #E5E7EB;
  background: #fff;
  color: #374151;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.back-btn:hover { background: #F9FAFB; border-color: #D1D5DB; }
.topbar-logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #10B981, #059669);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(16,185,129,0.35);
}
.topbar-title {
  font-size: 1.05rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}
.topbar-sub {
  font-size: 0.78rem;
  color: #9CA3AF;
  margin: 2px 0 0;
}
.draft-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0.3rem 0.75rem;
  border-radius: 20px;
  border: 1.5px solid #D1FAE5;
  background: #F0FDF4;
  color: #059669;
  font-size: 0.8rem;
  font-weight: 600;
}

/* ── Form scroll ── */
.form-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 2rem 1rem 3rem;
}
.form-inner {
  max-width: 680px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

/* ── Card section ── */
.card-section {
  background: #ffffff;
  border-radius: 14px;
  padding: 1.5rem 1.75rem;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}
.card-section-header {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 1.35rem;
}
.section-icon-box {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #F0FDF4;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-section-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #111827;
}
.content-count {
  font-size: 0.78rem;
  color: #9CA3AF;
  margin-left: 0.5rem;
}

/* ── Form elements ── */
.form-group { margin-bottom: 1.1rem; }
.form-group:last-child { margin-bottom: 0; }
.form-label {
  display: block;
  font-size: 0.82rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.4rem;
}
.required { color: #EF4444; }
.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 0.6rem 0.9rem;
  border: 1.5px solid #E5E7EB;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #111827;
  background: #ffffff;
  outline: none;
  transition: border-color 0.15s;
  box-sizing: border-box;
  font-family: inherit;
}
.form-input::placeholder,
.form-textarea::placeholder { color: #9CA3AF; }
.form-input:focus,
.form-textarea:focus,
.form-select:focus { border-color: #10B981; box-shadow: 0 0 0 3px rgba(16,185,129,0.1); }
.form-textarea { resize: vertical; min-height: 110px; }

.form-row { display: flex; gap: 1rem; }
.form-group--half { flex: 1; margin-bottom: 0; }

.select-wrap { position: relative; }
.form-select {
  appearance: none;
  -webkit-appearance: none;
  cursor: pointer;
  padding-right: 2rem;
}
.select-arrow {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

/* ── Drop zone ── */
.drop-zone {
  border: 2px dashed #D1D5DB;
  border-radius: 12px;
  padding: 2.5rem 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
  background: #FAFAFA;
  position: relative;
  overflow: hidden;
}
.drop-zone:hover,
.drop-zone--active { border-color: #10B981; background: #F0FDF4; }
.drop-icon-box {
  width: 58px;
  height: 58px;
  border-radius: 14px;
  background: #E6F7F2;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 0.85rem;
}
.drop-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.3rem;
}
.drop-sub {
  font-size: 0.78rem;
  color: #9CA3AF;
  margin: 0;
}
.cover-preview {
  width: 100%;
  max-height: 220px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
}

/* ── Content list ── */
.content-list { display: flex; flex-direction: column; gap: 0.6rem; margin-bottom: 0.85rem; }
.content-row {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.65rem 0.8rem;
  border-radius: 10px;
  border: 1.5px solid #F3F4F6;
  background: #FAFAFA;
  transition: box-shadow 0.15s;
  cursor: grab;
}
.content-row--dragging { opacity: 0.5; }
.content-row:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.07); }
.drag-handle {
  cursor: grab;
  display: flex;
  align-items: center;
  padding: 0 2px;
  flex-shrink: 0;
}
.content-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.content-icon--video { background: #EFF6FF; color: #3B82F6; }
.content-icon--doc   { background: #EFF6FF; color: #3B82F6; }
.content-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
}
.content-index { font-size: 0.78rem; color: #9CA3AF; flex-shrink: 0; }
.content-name {
  font-size: 0.85rem;
  font-weight: 500;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.content-type-badge {
  font-size: 0.7rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 20px;
  flex-shrink: 0;
}
.badge-video { background: #DBEAFE; color: #2563EB; }
.badge-doc   { background: #EDE9FE; color: #7C3AED; }
.content-size { font-size: 0.75rem; color: #9CA3AF; flex-shrink: 0; }
.remove-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #9CA3AF;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  flex-shrink: 0;
}
.remove-btn:hover { background: #FEF2F2; color: #EF4444; }

/* ── Add buttons ── */
.add-buttons {
  display: flex;
  gap: 0.75rem;
}
.add-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.6rem 1rem;
  border: 1.5px dashed #D1D5DB;
  border-radius: 8px;
  background: #fff;
  color: #6B7280;
  font-size: 0.83rem;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s, background 0.15s;
  font-family: inherit;
}
.add-btn:hover { border-color: #10B981; color: #10B981; background: #F0FDF4; }

/* ── Hidden input ── */
.hidden-input { display: none; }

/* ── Action buttons ── */
.actions {
  display: flex;
  gap: 0.85rem;
  padding-top: 0.25rem;
}
.btn-create {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.85rem 1.5rem;
  background: #10B981;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s, box-shadow 0.15s;
  font-family: inherit;
}
.btn-create:hover { background: #059669; box-shadow: 0 4px 14px rgba(16,185,129,0.35); }
.btn-cancel {
  padding: 0.85rem 1.75rem;
  background: #fff;
  color: #374151;
  border: 1.5px solid #E5E7EB;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  font-family: inherit;
}
.btn-cancel:hover { background: #F9FAFB; border-color: #D1D5DB; }

/* ── Responsive ── */
@media (max-width: 600px) {
  .form-row { flex-direction: column; }
  .topbar { padding: 0.85rem 1rem; }
  .card-section { padding: 1.25rem; }
  .add-buttons { flex-direction: column; }
}
</style>
