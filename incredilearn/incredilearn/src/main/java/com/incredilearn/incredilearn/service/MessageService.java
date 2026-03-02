package com.incredilearn.incredilearn.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incredilearn.incredilearn.dto.ConversationDTO;
import com.incredilearn.incredilearn.dto.MessageDTO;
import com.incredilearn.incredilearn.dto.UserSearchDTO;
import com.incredilearn.incredilearn.model.Conversation;
import com.incredilearn.incredilearn.model.Message;
import com.incredilearn.incredilearn.model.User;
import com.incredilearn.incredilearn.repository.ConversationRepository;
import com.incredilearn.incredilearn.repository.MessageRepository;
import com.incredilearn.incredilearn.repository.UserRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
                          ConversationRepository conversationRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Envoyer un message. Crée la conversation si elle n'existe pas encore.
     */
    @Transactional
    public MessageDTO sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Expéditeur introuvable"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Destinataire introuvable"));

        // Trouver ou créer la conversation
        Conversation conversation = conversationRepository.findByUsers(sender, receiver)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation(sender, receiver);
                    return conversationRepository.save(newConv);
                });

        // Mettre à jour le timestamp de la conversation
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Créer le message
        Message message = new Message(conversation, sender, receiver, content);
        message = messageRepository.save(message);

        return toMessageDTO(message);
    }

    /**
     * Récupérer les conversations d'un utilisateur.
     */
    public List<ConversationDTO> getConversations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Conversation> conversations = conversationRepository.findByUser(user);

        return conversations.stream().map(conv -> {
            ConversationDTO dto = new ConversationDTO();
            dto.setId(conv.getId());

            // Déterminer l'autre utilisateur
            User otherUser = conv.getUser1().getId().equals(userId) ? conv.getUser2() : conv.getUser1();
            dto.setOtherUserId(otherUser.getId());
            dto.setOtherUserNom(otherUser.getNom());
            dto.setOtherUserRole(otherUser.getRole().name().toLowerCase());

            // Dernier message
            List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conv);
            if (!messages.isEmpty()) {
                Message lastMsg = messages.get(messages.size() - 1);
                String preview = lastMsg.getContent();
                if (preview.length() > 50) {
                    preview = preview.substring(0, 50) + "...";
                }
                dto.setLastMessage(preview);
            }

            dto.setLastMessageAt(conv.getLastMessageAt());
            dto.setUnreadCount(messageRepository.countUnreadByConversationAndUser(conv, user));

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Récupérer les messages d'une conversation.
     */
    public List<MessageDTO> getMessages(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));

        return messageRepository.findByConversationOrderBySentAtAsc(conversation)
                .stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marquer les messages d'une conversation comme lus pour un utilisateur.
     */
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        messageRepository.markAsReadByConversationAndUser(conversation, user);
    }

    /**
     * Compter les messages non lus pour un utilisateur.
     */
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return messageRepository.countUnreadByUser(user);
    }

    /**
     * Rechercher des utilisateurs par nom (pour démarrer une conversation).
     */
    public List<UserSearchDTO> searchUsers(String query, Long currentUserId) {
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> !u.isBlocked())
                .filter(u -> u.getNom().toLowerCase().contains(query.toLowerCase()))
                .map(u -> new UserSearchDTO(u.getId(), u.getNom(), u.getRole().name().toLowerCase()))
                .collect(Collectors.toList());
    }

    private MessageDTO toMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderNom(message.getSender().getNom());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverNom(message.getReceiver().getNom());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setRead(message.isRead());
        return dto;
    }
}
