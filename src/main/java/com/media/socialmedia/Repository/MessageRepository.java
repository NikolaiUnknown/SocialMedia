package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface MessageRepository extends JpaRepository<Message,Long> {
    @Query(value = """
                SELECT m from Message m
                WHERE (m.senderId=:first_id AND m.recipientId=:second_id)
                OR (m.recipientId=:first_id AND m.senderId=:second_id)
                ORDER BY m.dateOfSend
                """)
    Set<Message> findAllMessagesByUsers(Long first_id, Long second_id);

    @Query(value = """
                Select case
                when m.senderId=:id then m.recipientId
                when m.recipientId=:id then m.senderId
                end as user_id from Message m order by m.dateOfSend desc
                """)
    Set<Long> findUsersLastMessages(Long id);
    @Query("SELECT m FROM Message m WHERE m.recipientId= :recipientId")
    Set<Message> findDistinctSenderIdByRecipientId(Long recipientId);

    @Query("SELECT m FROM Message m WHERE m.senderId= :senderId")
    Set<Message> findDistinctRecipientIdBySenderId(Long senderId);
}
