package com.incredilearn.incredilearn.dto;

import java.time.LocalDateTime;

public class ConversationDTO {
    private Long id;
    private Long otherUserId;
    private String otherUserNom;
    private String otherUserRole;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;

    // Constructeurs
    public ConversationDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherUserNom() { return otherUserNom; }
    public void setOtherUserNom(String otherUserNom) { this.otherUserNom = otherUserNom; }

    public String getOtherUserRole() { return otherUserRole; }
    public void setOtherUserRole(String otherUserRole) { this.otherUserRole = otherUserRole; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
}
