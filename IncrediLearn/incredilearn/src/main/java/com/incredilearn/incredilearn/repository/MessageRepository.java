package com.incredilearn.incredilearn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.incredilearn.incredilearn.model.Conversation;
import com.incredilearn.incredilearn.model.Message;
import com.incredilearn.incredilearn.model.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :user AND m.isRead = false")
    long countUnreadByUser(@Param("user") User user);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.receiver = :user AND m.isRead = false")
    long countUnreadByConversationAndUser(@Param("conversation") Conversation conversation, @Param("user") User user);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation = :conversation AND m.receiver = :user AND m.isRead = false")
    void markAsReadByConversationAndUser(@Param("conversation") Conversation conversation, @Param("user") User user);
}
