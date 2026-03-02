// @ts-expect-error - SockJS n'a pas de typages pour le chemin dist
import SockJS from 'sockjs-client/dist/sockjs'
import { Client } from '@stomp/stompjs'

const API_URL = 'http://localhost:8080/api/messages'
const WS_URL = 'http://localhost:8080/ws'

export interface MessageDTO {
  id: number
  conversationId: number
  senderId: number
  senderNom: string
  receiverId: number
  receiverNom: string
  content: string
  sentAt: string
  read: boolean
  deleted: boolean
}

export interface ConversationDTO {
  id: number
  otherUserId: number
  otherUserNom: string
  otherUserRole: string
  lastMessage: string
  lastMessageAt: string
  unreadCount: number
}

export interface UserSearchResult {
  id: number
  nom: string
  role: string
}

export interface SendMessageRequest {
  senderId: number
  receiverId: number
  content: string
}

let stompClient: Client | null = null

export const messageService = {
  /**
   * Se connecter au WebSocket STOMP.
   */
  connect(userId: number, onMessageReceived: (message: MessageDTO) => void): Client {
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        // S'abonner aux messages destinés à cet utilisateur
        client.subscribe(`/topic/messages/${userId}`, (frame) => {
          const message: MessageDTO = JSON.parse(frame.body)
          onMessageReceived(message)
        })
      },
      onStompError: (frame) => {
        console.error('Erreur STOMP:', frame.headers['message'])
      },
    })

    client.activate()
    stompClient = client
    return client
  },

  /**
   * Se déconnecter du WebSocket.
   */
  disconnect() {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
  },

  /**
   * Envoyer un message via WebSocket.
   */
  sendViaWebSocket(request: SendMessageRequest) {
    if (stompClient && stompClient.connected) {
      stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(request),
      })
      return true
    }
    return false
  },

  /**
   * Envoyer un message via REST (fallback).
   */
  async sendMessage(request: SendMessageRequest): Promise<MessageDTO> {
    const response = await fetch(`${API_URL}/send`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
    return response.json()
  },

  /**
   * Récupérer les conversations d'un utilisateur.
   */
  async getConversations(userId: number): Promise<ConversationDTO[]> {
    const response = await fetch(`${API_URL}/conversations/${userId}`)
    return response.json()
  },

  /**
   * Récupérer les messages d'une conversation.
   */
  async getMessages(conversationId: number): Promise<MessageDTO[]> {
    const response = await fetch(`${API_URL}/conversation/${conversationId}`)
    return response.json()
  },

  /**
   * Marquer les messages comme lus.
   */
  async markAsRead(conversationId: number, userId: number): Promise<void> {
    await fetch(`${API_URL}/read/${conversationId}/${userId}`, { method: 'POST' })
  },

  /**
   * Compter les messages non lus.
   */
  async getUnreadCount(userId: number): Promise<number> {
    const response = await fetch(`${API_URL}/unread/${userId}`)
    return response.json()
  },

  /**
   * Supprimer un message.
   */
  async deleteMessage(messageId: number, userId: number): Promise<MessageDTO> {
    const response = await fetch(`${API_URL}/delete/${messageId}/${userId}`, {
      method: 'DELETE',
    })
    return response.json()
  },

  /**
   * Rechercher des utilisateurs.
   */
  async searchUsers(query: string, currentUserId: number): Promise<UserSearchResult[]> {
    const response = await fetch(
      `${API_URL}/users/search?query=${encodeURIComponent(query)}&currentUserId=${currentUserId}`,
    )
    return response.json()
  },
}
