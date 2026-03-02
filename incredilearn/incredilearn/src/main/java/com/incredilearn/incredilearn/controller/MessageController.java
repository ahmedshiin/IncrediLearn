package com.incredilearn.incredilearn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incredilearn.incredilearn.dto.ConversationDTO;
import com.incredilearn.incredilearn.dto.MessageDTO;
import com.incredilearn.incredilearn.dto.SendMessageRequest;
import com.incredilearn.incredilearn.dto.UserSearchDTO;
import com.incredilearn.incredilearn.service.MessageService;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Récupérer les conversations d'un utilisateur.
     */
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationDTO>> getConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getConversations(userId));
    }

    /**
     * Récupérer les messages d'une conversation.
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.getMessages(conversationId));
    }

    /**
     * Marquer les messages comme lus.
     */
    @PostMapping("/read/{conversationId}/{userId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long conversationId, @PathVariable Long userId) {
        messageService.markAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Compter les messages non lus.
     */
    @GetMapping("/unread/{userId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getUnreadCount(userId));
    }

    /**
     * Rechercher des utilisateurs pour démarrer une conversation.
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam Long currentUserId) {
        return ResponseEntity.ok(messageService.searchUsers(query, currentUserId));
    }

    /**
     * Envoyer un message via REST (fallback si WebSocket indisponible).
     */
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(@org.springframework.web.bind.annotation.RequestBody SendMessageRequest request) {
        MessageDTO message = messageService.sendMessage(
                request.getSenderId(), request.getReceiverId(), request.getContent());

        // Notifier le destinataire en temps réel via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getReceiverId(), message);
        // Notifier aussi l'expéditeur (pour synchroniser les onglets)
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getSenderId(), message);

        return ResponseEntity.ok(message);
    }

    /**
     * Supprimer un message.
     */
    @DeleteMapping("/delete/{messageId}/{userId}")
    public ResponseEntity<MessageDTO> deleteMessage(@PathVariable Long messageId, @PathVariable Long userId) {
        MessageDTO message = messageService.deleteMessage(messageId, userId);

        // Notifier les deux utilisateurs via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/messages/" + message.getSenderId(), message);
        messagingTemplate.convertAndSend(
                "/topic/messages/" + message.getReceiverId(), message);

        return ResponseEntity.ok(message);
    }

    /**
     * Recevoir un message via WebSocket (STOMP).
     */
    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(@Payload SendMessageRequest request) {
        MessageDTO message = messageService.sendMessage(
                request.getSenderId(), request.getReceiverId(), request.getContent());

        // Notifier le destinataire
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getReceiverId(), message);
        // Notifier l'expéditeur
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getSenderId(), message);
    }
}
