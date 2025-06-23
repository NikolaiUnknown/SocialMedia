package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Chat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;
@Repository
@Transactional
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query(value = """
                SELECT c.id
                FROM Chat c
                JOIN c.members m
                WHERE m.id=:id
                ORDER BY c.dateOfLastSend DESC
                """)
    Set<UUID> findUserChatsOrderByDateOfLastSendDesc(Long id);

    @Query(value = """
            SELECT EXISTS(
                SELECT *
                FROM users_chats
                WHERE user_id=:userId
                AND chat_id=:chatId
            )
            """,nativeQuery = true)
    boolean isUserMember(Long userId, UUID chatId);
    @Query(value = """
            SELECT user_id
            FROM users_chats
            WHERE chat_id=:chatId
            AND user_id <>:userId
            """ ,nativeQuery = true)
    Long findOtherMember(Long userId, UUID chatId);
}
