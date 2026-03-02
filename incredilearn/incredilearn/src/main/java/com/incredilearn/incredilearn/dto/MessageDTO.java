package com.incredilearn.incredilearn.dto;

import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderNom;
    private Long receiverId;
    private String receiverNom;
    private String content;
    private LocalDateTime sentAt;
    private boolean read;

    // Constructeurs
    public MessageDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderNom() { return senderNom; }
    public void setSenderNom(String senderNom) { this.senderNom = senderNom; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getReceiverNom() { return receiverNom; }
    public void setReceiverNom(String receiverNom) { this.receiverNom = receiverNom; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
