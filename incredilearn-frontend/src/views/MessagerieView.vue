<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  messageService,
  type MessageDTO,
  type ConversationDTO,
  type UserSearchResult,
} from '@/services/messageService'
import type { Client } from '@stomp/stompjs'

const authStore = useAuthStore()
const userId = computed(() => authStore.user?.id ?? 0)

// État
const conversations = ref<ConversationDTO[]>([])
const selectedConversation = ref<ConversationDTO | null>(null)
const messages = ref<MessageDTO[]>([])
const newMessage = ref('')
const searchQuery = ref('')
const searchResults = ref<UserSearchResult[]>([])
const showSearch = ref(false)
const loading = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)
const showDeleteModal = ref(false)
const messageToDelete = ref<MessageDTO | null>(null)
let stompClient: Client | null = null

// Charger les conversations
async function loadConversations() {
  if (!userId.value) return
  conversations.value = await messageService.getConversations(userId.value)
}

// Sélectionner une conversation
async function selectConversation(conv: ConversationDTO) {
  selectedConversation.value = conv
  loading.value = true
  messages.value = await messageService.getMessages(conv.id)
  loading.value = false

  // Marquer comme lu
  if (conv.unreadCount > 0) {
    await messageService.markAsRead(conv.id, userId.value)
    conv.unreadCount = 0
  }

  await nextTick()
  scrollToBottom()
}

// Envoyer un message
async function sendMessage() {
  if (!newMessage.value.trim() || !selectedConversation.value) return

  const request = {
    senderId: userId.value,
    receiverId: selectedConversation.value.otherUserId,
    content: newMessage.value.trim(),
  }

  newMessage.value = ''

  // Essayer d'envoyer via WebSocket, sinon REST
  const sentViaWs = messageService.sendViaWebSocket(request)
  if (!sentViaWs) {
    await messageService.sendMessage(request)
  }
}

// Recevoir un message en temps réel
function onMessageReceived(message: MessageDTO) {
  // Si c'est dans la conversation sélectionnée
  if (
    selectedConversation.value &&
    message.conversationId === selectedConversation.value.id
  ) {
    // Vérifier si c'est une mise à jour (suppression)
    const existingIndex = messages.value.findIndex((m) => m.id === message.id)
    if (existingIndex !== -1) {
      messages.value[existingIndex] = message
    } else {
      messages.value.push(message)
      nextTick(() => scrollToBottom())
    }

    // Marquer comme lu si on est le destinataire
    if (message.receiverId === userId.value) {
      messageService.markAsRead(message.conversationId, userId.value)
    }
  }

  // Mettre à jour la liste des conversations
  updateConversationPreview(message)
}

// Mettre à jour l'aperçu d'une conversation
function updateConversationPreview(message: MessageDTO) {
  const existingConv = conversations.value.find(
    (c) => c.id === message.conversationId,
  )
  if (existingConv) {
    existingConv.lastMessage =
      message.content.length > 50
        ? message.content.substring(0, 50) + '...'
        : message.content
    existingConv.lastMessageAt = message.sentAt

    // Incrémenter les non-lus si ce n'est pas la conversation active
    if (
      message.receiverId === userId.value &&
      (!selectedConversation.value ||
        selectedConversation.value.id !== message.conversationId)
    ) {
      existingConv.unreadCount++
    }

    // Remonter la conversation en haut de la liste
    conversations.value.sort(
      (a, b) =>
        new Date(b.lastMessageAt).getTime() -
        new Date(a.lastMessageAt).getTime(),
    )
  } else {
    // Nouvelle conversation, recharger la liste
    loadConversations()
  }
}

// Rechercher des utilisateurs
let searchTimeout: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimeout) clearTimeout(searchTimeout)
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  searchTimeout = setTimeout(async () => {
    searchResults.value = await messageService.searchUsers(
      searchQuery.value,
      userId.value,
    )
  }, 300)
}

// Démarrer une conversation avec un utilisateur trouvé
async function startConversation(user: UserSearchResult) {
  showSearch.value = false
  searchQuery.value = ''
  searchResults.value = []

  // Vérifier si une conversation existe déjà
  const existing = conversations.value.find(
    (c) => c.otherUserId === user.id,
  )
  if (existing) {
    await selectConversation(existing)
    return
  }

  // Envoyer un premier message vide pour créer la conversation ou simplement sélectionner
  // On va créer une conversation "virtuelle" et laisser l'utilisateur envoyer le premier message
  selectedConversation.value = {
    id: 0, // temporaire
    otherUserId: user.id,
    otherUserNom: user.nom,
    otherUserRole: user.role,
    lastMessage: '',
    lastMessageAt: new Date().toISOString(),
    unreadCount: 0,
  }
  messages.value = []
}

// Envoyer un message vers un nouveau contact (pas encore de conversation)
async function sendFirstMessage() {
  if (!newMessage.value.trim() || !selectedConversation.value) return

  const request = {
    senderId: userId.value,
    receiverId: selectedConversation.value.otherUserId,
    content: newMessage.value.trim(),
  }

  newMessage.value = ''
  const message = await messageService.sendMessage(request)

  // Recharger les conversations pour obtenir le bon ID
  await loadConversations()

  // Trouver et sélectionner la nouvelle conversation
  const newConv = conversations.value.find(
    (c) => c.otherUserId === selectedConversation.value!.otherUserId,
  )
  if (newConv) {
    await selectConversation(newConv)
  }
}

// Gérer l'envoi (nouveau contact ou conversation existante)
async function handleSend() {
  if (selectedConversation.value && selectedConversation.value.id === 0) {
    await sendFirstMessage()
  } else {
    await sendMessage()
  }
}

// Scroller vers le bas
function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Formater la date
function formatTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const dayMs = 86400000

  if (diff < dayMs) {
    return date.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit',
    })
  } else if (diff < 7 * dayMs) {
    return date.toLocaleDateString('fr-FR', { weekday: 'short' })
  } else {
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
    })
  }
}

function getRoleBadge(role: string): string {
  switch (role) {
    case 'enseignant':
      return 'Enseignant'
    case 'etudiant':
      return 'Étudiant'
    case 'admin':
      return 'Admin'
    default:
      return role
  }
}

// Gestion touche Entrée
function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

// Supprimer un message
function handleDeleteMessage(msg: MessageDTO) {
  messageToDelete.value = msg
  showDeleteModal.value = true
}

function cancelDelete() {
  showDeleteModal.value = false
  messageToDelete.value = null
}

async function confirmDelete() {
  if (!messageToDelete.value) return

  const updated = await messageService.deleteMessage(messageToDelete.value.id, userId.value)

  // Mettre à jour localement
  const index = messages.value.findIndex((m) => m.id === messageToDelete.value!.id)
  if (index !== -1) {
    messages.value[index] = updated
  }

  // Mettre à jour l'aperçu si c'est le dernier message
  updateConversationPreview(updated)

  showDeleteModal.value = false
  messageToDelete.value = null
}

onMounted(async () => {
  await loadConversations()

  // Connexion WebSocket
  if (userId.value) {
    stompClient = messageService.connect(userId.value, onMessageReceived)
  }
})

onUnmounted(() => {
  messageService.disconnect()
})
</script>

<template>
  <div class="messagerie-container">
    <div class="messagerie-layout">
      <!-- Panneau latéral : liste des conversations -->
      <aside class="conversations-panel">
        <div class="panel-header">
          <h2>Messages</h2>
          <button class="btn-new-chat" @click="showSearch = !showSearch" title="Nouvelle conversation">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              <line x1="12" y1="8" x2="12" y2="14" />
              <line x1="9" y1="11" x2="15" y2="11" />
            </svg>
          </button>
        </div>

        <!-- Barre de recherche utilisateurs -->
        <div v-if="showSearch" class="search-section">
          <div class="search-input-wrapper">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="2">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input v-model="searchQuery" @input="onSearchInput" type="text"
              placeholder="Rechercher un utilisateur..." class="search-input" autofocus />
          </div>
          <div v-if="searchResults.length" class="search-results">
            <button v-for="user in searchResults" :key="user.id" @click="startConversation(user)"
              class="search-result-item">
              <div class="user-avatar-sm">{{ user.nom.charAt(0).toUpperCase() }}</div>
              <div class="search-result-info">
                <span class="search-result-name">{{ user.nom }}</span>
                <span class="search-result-role">{{ getRoleBadge(user.role) }}</span>
              </div>
            </button>
          </div>
          <div v-else-if="searchQuery && !searchResults.length" class="search-empty">
            Aucun utilisateur trouvé
          </div>
        </div>

        <!-- Liste des conversations -->
        <div class="conversations-list">
          <div v-if="!conversations.length" class="no-conversations">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            <p>Aucune conversation</p>
            <small>Cliquez sur + pour démarrer</small>
          </div>

          <button v-for="conv in conversations" :key="conv.id" @click="selectConversation(conv)" :class="[
            'conversation-item',
            { active: selectedConversation?.id === conv.id },
          ]">
            <div class="conv-avatar">
              {{ conv.otherUserNom.charAt(0).toUpperCase() }}
            </div>
            <div class="conv-info">
              <div class="conv-top-row">
                <span class="conv-name">{{ conv.otherUserNom }}</span>
                <span class="conv-time">{{ formatTime(conv.lastMessageAt) }}</span>
              </div>
              <div class="conv-bottom-row">
                <span class="conv-last-message">{{ conv.lastMessage || 'Pas encore de message' }}</span>
                <span v-if="conv.unreadCount > 0" class="conv-badge">{{ conv.unreadCount }}</span>
              </div>
            </div>
          </button>
        </div>
      </aside>

      <!-- Zone de chat -->
      <main class="chat-panel">
        <!-- Aucune conversation sélectionnée -->
        <div v-if="!selectedConversation" class="chat-empty">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          <h3>Bienvenue dans la messagerie</h3>
          <p>Sélectionnez une conversation ou créez-en une nouvelle</p>
        </div>

        <!-- Chat actif -->
        <template v-else>
          <!-- En-tête du chat -->
          <div class="chat-header">
            <button class="btn-back-mobile" @click="selectedConversation = null">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6" />
              </svg>
            </button>
            <div class="chat-header-avatar">
              {{ selectedConversation.otherUserNom.charAt(0).toUpperCase() }}
            </div>
            <div class="chat-header-info">
              <span class="chat-header-name">{{ selectedConversation.otherUserNom }}</span>
              <span class="chat-header-role">{{ getRoleBadge(selectedConversation.otherUserRole) }}</span>
            </div>
          </div>

          <!-- Messages -->
          <div ref="messagesContainer" class="messages-area">
            <div v-if="loading" class="messages-loading">Chargement des messages...</div>

            <div v-else-if="!messages.length" class="messages-empty">
              <p>Aucun message. Envoyez le premier !</p>
            </div>

            <template v-else>
              <div v-for="msg in messages" :key="msg.id" :class="[
                'message-wrapper',
                { 'message-sent': msg.senderId === userId },
                { 'message-received': msg.senderId !== userId },
              ]">
                <div :class="['message-bubble', { 'message-deleted': msg.deleted }]">
                  <p class="message-content">{{ msg.content }}</p>
                  <div class="message-meta">
                    <span class="message-time">{{
                      formatTime(msg.sentAt)
                    }}</span>
                    <svg v-if="msg.senderId === userId && msg.read && !msg.deleted" width="14" height="14" viewBox="0 0 24 24"
                      fill="none" stroke="#10B981" stroke-width="2.5">
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                    <button
                      v-if="msg.senderId === userId && !msg.deleted"
                      @click="handleDeleteMessage(msg)"
                      class="btn-delete-msg"
                      title="Supprimer le message"
                    >
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3 6 5 6 21 6" />
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- Zone de saisie -->
          <div class="chat-input-area">
            <textarea v-model="newMessage" @keydown="handleKeydown" placeholder="Écrivez votre message..."
              class="chat-input" rows="1"></textarea>
            <button @click="handleSend" :disabled="!newMessage.trim()" class="btn-send">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round">
                <line x1="22" y1="2" x2="11" y2="13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
            </button>
          </div>
        </template>
      </main>
    </div>

    <!-- Modal de confirmation de suppression -->
    <Teleport to="body">
      <div v-if="showDeleteModal" class="modal-overlay" @click.self="cancelDelete">
        <div class="modal-box">
          <div class="modal-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#DC2626" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
          </div>
          <h3 class="modal-title">Supprimer ce message ?</h3>
          <p class="modal-text">Les deux participants verront « Message supprimé » à la place du contenu.</p>
          <div class="modal-actions">
            <button class="modal-btn modal-btn-cancel" @click="cancelDelete">Annuler</button>
            <button class="modal-btn modal-btn-confirm" @click="confirmDelete">Supprimer</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.messagerie-container {
  height: calc(100vh - 64px);
  background: #F9FAFB;
}

.messagerie-layout {
  display: flex;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Panneau conversations */
.conversations-panel {
  width: 360px;
  min-width: 360px;
  border-right: 1px solid #E5E7EB;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1rem;
  border-bottom: 1px solid #E5E7EB;
}

.panel-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.btn-new-chat {
  width: 36px;
  height: 36px;
  border: none;
  background: #F0FDF4;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10B981;
  transition: background 0.15s;
}

.btn-new-chat:hover {
  background: #D1FAE5;
}

/* Recherche */
.search-section {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #E5E7EB;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #F3F4F6;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
}

.search-input {
  flex: 1;
  border: none;
  background: none;
  font-size: 0.9rem;
  color: #111827;
  outline: none;
}

.search-input::placeholder {
  color: #9CA3AF;
}

.search-results {
  margin-top: 0.5rem;
  max-height: 200px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.6rem 0.5rem;
  border: none;
  background: none;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
}

.search-result-item:hover {
  background: #F3F4F6;
}

.user-avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #10B981;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.8rem;
  flex-shrink: 0;
}

.search-result-info {
  display: flex;
  flex-direction: column;
}

.search-result-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #111827;
}

.search-result-role {
  font-size: 0.75rem;
  color: #6B7280;
}

.search-empty {
  text-align: center;
  padding: 1rem;
  color: #9CA3AF;
  font-size: 0.85rem;
}

/* Liste conversations */
.conversations-list {
  flex: 1;
  overflow-y: auto;
}

.no-conversations {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 0.5rem;
  color: #9CA3AF;
}

.no-conversations p {
  font-size: 0.95rem;
  font-weight: 500;
  margin: 0;
}

.no-conversations small {
  font-size: 0.8rem;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.85rem 1rem;
  border: none;
  background: none;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
  border-bottom: 1px solid #F3F4F6;
}

.conversation-item:hover {
  background: #F9FAFB;
}

.conversation-item.active {
  background: #F0FDF4;
  border-left: 3px solid #10B981;
}

.conv-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #10B981;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1rem;
  flex-shrink: 0;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conv-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #111827;
}

.conv-time {
  font-size: 0.75rem;
  color: #9CA3AF;
  flex-shrink: 0;
}

.conv-bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.2rem;
}

.conv-last-message {
  font-size: 0.8rem;
  color: #6B7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conv-badge {
  background: #10B981;
  color: white;
  font-size: 0.7rem;
  font-weight: 600;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  flex-shrink: 0;
  margin-left: 0.5rem;
}

/* Zone de chat */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  color: #9CA3AF;
}

.chat-empty h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #6B7280;
  margin: 0;
}

.chat-empty p {
  font-size: 0.9rem;
  margin: 0;
}

/* En-tête chat */
.chat-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.85rem 1.25rem;
  border-bottom: 1px solid #E5E7EB;
  background: #ffffff;
}

.btn-back-mobile {
  display: none;
  border: none;
  background: none;
  cursor: pointer;
  color: #374151;
  padding: 0.25rem;
}

.chat-header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #10B981;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1rem;
  flex-shrink: 0;
}

.chat-header-info {
  display: flex;
  flex-direction: column;
}

.chat-header-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #111827;
}

.chat-header-role {
  font-size: 0.75rem;
  color: #6B7280;
}

/* Zone des messages */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background: #F9FAFB;
}

.messages-loading,
.messages-empty {
  text-align: center;
  color: #9CA3AF;
  padding: 2rem;
  font-size: 0.9rem;
}

.message-wrapper {
  display: flex;
  max-width: 70%;
}

.message-sent {
  align-self: flex-end;
}

.message-received {
  align-self: flex-start;
}

.message-bubble {
  padding: 0.65rem 1rem;
  border-radius: 16px;
  max-width: 100%;
  word-break: break-word;
}

.message-sent .message-bubble {
  background: #10B981;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-received .message-bubble {
  background: #ffffff;
  color: #111827;
  border: 1px solid #E5E7EB;
  border-bottom-left-radius: 4px;
}

.message-content {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.4;
  white-space: pre-wrap;
}

.message-meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.25rem;
  margin-top: 0.3rem;
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
}

/* Bouton supprimer message */
.btn-delete-msg {
  border: none;
  background: none;
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.6);
  opacity: 0;
  transition: opacity 0.15s, color 0.15s;
}

.message-bubble:hover .btn-delete-msg {
  opacity: 1;
}

.btn-delete-msg:hover {
  color: #FCA5A5;
}

/* Message supprimé */
.message-deleted {
  opacity: 0.7;
}

.message-sent .message-deleted {
  background: #6B7280 !important;
}

.message-received .message-deleted {
  background: #F3F4F6 !important;
  border-color: #D1D5DB !important;
}

.message-deleted .message-content {
  font-style: italic;
  font-size: 0.85rem;
}

/* Zone de saisie */
.chat-input-area {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  padding: 0.85rem 1.25rem;
  border-top: 1px solid #E5E7EB;
  background: #ffffff;
}

.chat-input {
  flex: 1;
  border: 1.5px solid #E5E7EB;
  border-radius: 12px;
  padding: 0.65rem 1rem;
  font-size: 0.9rem;
  font-family: inherit;
  resize: none;
  outline: none;
  max-height: 120px;
  transition: border-color 0.15s;
}

.chat-input:focus {
  border-color: #10B981;
}

.btn-send {
  width: 40px;
  height: 40px;
  border: none;
  background: #10B981;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: background 0.15s;
  flex-shrink: 0;
}

.btn-send:hover:not(:disabled) {
  background: #059669;
}

.btn-send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .conversations-panel {
    width: 100%;
    min-width: unset;
  }

  .chat-panel {
    display: none;
  }

  .messagerie-layout:has(.chat-panel > .chat-header) .conversations-panel {
    display: none;
  }

  .messagerie-layout:has(.chat-panel > .chat-header) .chat-panel {
    display: flex;
  }

  .btn-back-mobile {
    display: block;
  }
}

/* Modal suppression */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box {
  background: #ffffff;
  border-radius: 14px;
  padding: 2rem;
  width: 380px;
  max-width: 90vw;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  animation: modalIn 0.2s ease;
}

@keyframes modalIn {
  from { opacity: 0; transform: scale(0.92); }
  to   { opacity: 1; transform: scale(1); }
}

.modal-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #FEE2E2;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}

.modal-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 0.5rem;
}

.modal-text {
  font-size: 0.88rem;
  color: #6B7280;
  margin: 0 0 1.5rem;
  line-height: 1.45;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}

.modal-btn {
  padding: 0.55rem 1.4rem;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: background 0.15s;
}

.modal-btn-cancel {
  background: #F3F4F6;
  color: #374151;
}

.modal-btn-cancel:hover {
  background: #E5E7EB;
}

.modal-btn-confirm {
  background: #DC2626;
  color: #ffffff;
}

.modal-btn-confirm:hover {
  background: #B91C1C;
}
</style>
