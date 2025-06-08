package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface MessageRepository extends JpaRepository<Message,Long> {
    Set<Message> findMessagesBySenderIdAndRecipientId(Long senderId, Long recipientId);

    @Modifying
    @Query("SELECT m FROM Message m WHERE m.recipientId= :recipientId")
    Set<Message> findDistinctSenderIdByRecipientId(Long recipientId);

    @Modifying
    @Query("SELECT m FROM Message m WHERE m.senderId= :senderId")
    Set<Message> findDistinctRecipientIdBySenderId(Long senderId);
}
