package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Set;

public interface MessageRepository extends JpaRepository<Message,Long> {
    @Query(value = """
                SELECT m from Message m
                WHERE (m.senderId=:first_id AND m.recipientId=:second_id)
                OR (m.recipientId=:first_id AND m.senderId=:second_id)
                ORDER BY m.dateOfSend DESC
                """)
    Page<Message> findAllMessagesByUsers(Long first_id, Long second_id, Pageable pageable);

    @Query(value = """
                SELECT CASE
                WHEN m.senderId=:id THEN m.recipientId
                WHEN m.recipientId=:id THEN m.senderId
                END AS user_id FROM Message m ORDER BY m.dateOfSend DESC
                """)
    Set<Long> findUsersLastMessages(Long id);
}
