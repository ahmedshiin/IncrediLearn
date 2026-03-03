<script setup lang="ts">
import { computed } from 'vue'

export interface ConfirmModalProps {
  visible: boolean
  title?: string
  message: string
  type?: 'confirm' | 'alert'
  confirmText?: string
  cancelText?: string
  variant?: 'danger' | 'warning' | 'info'
}

const props = withDefaults(defineProps<ConfirmModalProps>(), {
  title: '',
  type: 'confirm',
  confirmText: 'Confirmer',
  cancelText: 'Annuler',
  variant: 'info',
})

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const iconPath = computed(() => {
  if (props.variant === 'danger')
    return 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 5h2v6h-2V7zm0 8h2v2h-2v-2z'
  if (props.variant === 'warning')
    return 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z'
  return 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z'
})

function onConfirm() {
  emit('confirm')
}
function onCancel() {
  emit('cancel')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="modal-overlay" @click.self="type === 'confirm' ? onCancel() : onConfirm()">
        <div class="modal-box" :class="`modal-box--${variant}`">

          <div class="modal-icon">
            <svg viewBox="0 0 24 24" width="38" height="38">
              <path :d="iconPath" :fill="variant === 'danger' ? '#EF4444' : variant === 'warning' ? '#F59E0B' : '#6366F1'" />
            </svg>
          </div>

          <h3 v-if="title" class="modal-title">{{ title }}</h3>

          <p class="modal-message">{{ message }}</p>

          <div class="modal-actions">
            <button v-if="type === 'confirm'" class="modal-btn modal-btn--cancel" @click="onCancel">{{ cancelText }}</button>
            <button class="modal-btn modal-btn--confirm" :class="`modal-btn--${variant}`" @click="onConfirm">{{ confirmText }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  backdrop-filter: blur(3px);
}

.modal-box {
  background: #fff;
  border-radius: 16px;
  padding: 32px 28px 24px;
  max-width: 400px;
  width: 90%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, .25);
  animation: modal-pop .25s ease;
}

@keyframes modal-pop {
  0% { transform: scale(.85); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

.modal-icon { margin-bottom: 12px; }

.modal-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 8px;
}

.modal-message {
  font-size: .95rem;
  color: #475569;
  margin: 0 0 24px;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.modal-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: .9rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all .2s;
}

.modal-btn--cancel {
  background: #F1F5F9;
  color: #475569;
}
.modal-btn--cancel:hover {
  background: #E2E8F0;
}

.modal-btn--confirm {
  color: #fff;
}
.modal-btn--info { background: #6366F1; }
.modal-btn--info:hover { background: #4F46E5; }

.modal-btn--danger { background: #EF4444; }
.modal-btn--danger:hover { background: #DC2626; }

.modal-btn--warning { background: #F59E0B; }
.modal-btn--warning:hover { background: #D97706; }

/* Transitions */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity .2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
