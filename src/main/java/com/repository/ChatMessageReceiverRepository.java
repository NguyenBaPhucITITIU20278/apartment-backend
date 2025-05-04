package com.repository;

import com.model.ChatMessage;
import com.model.ChatMessageReceiver;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageReceiverRepository extends JpaRepository<ChatMessageReceiver, Long> {
    @Query("SELECT r FROM ChatMessageReceiver r JOIN FETCH r.message WHERE r.receiverId = :receiverId AND r.isRead = false")
    List<ChatMessageReceiver> findByReceiverIdAndIsReadFalse(@Param("receiverId") String receiverId);
    
    List<ChatMessageReceiver> findByReceiverId(String receiverId);
    
    @Query("SELECT r FROM ChatMessageReceiver r JOIN FETCH r.message WHERE r.message.id = :messageId AND r.receiverId = :receiverId")
    Optional<ChatMessageReceiver> findByMessage_IdAndReceiverId(@Param("messageId") Long messageId, @Param("receiverId") String receiverId);

    @Modifying
    @Query("DELETE FROM ChatMessageReceiver r WHERE r.message = :message")
    void deleteByMessage(@Param("message") ChatMessage message);

} 