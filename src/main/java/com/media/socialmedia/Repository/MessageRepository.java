package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface MessageRepository extends JpaRepository<Message,Long> {
    @Modifying
    @Query(value = """
                SELECT * from messages
                WHERE (sender_id=:first_id AND recipient_id=:second_id)
                OR (recipient_id=:first_id AND sender_id=:second_id)
                ORDER BY date_of_send;
                """,nativeQuery = true)
    Set<Message> findAllMessagesByUsers(Long first_id, Long second_id);

    @Modifying
    @Query("SELECT m FROM Message m WHERE m.recipientId= :recipientId")
    Set<Message> findDistinctSenderIdByRecipientId(Long recipientId);

    @Modifying
    @Query("SELECT m FROM Message m WHERE m.senderId= :senderId")
    Set<Message> findDistinctRecipientIdBySenderId(Long senderId);
}
