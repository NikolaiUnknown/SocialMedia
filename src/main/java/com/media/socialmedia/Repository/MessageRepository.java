package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@Transactional
public interface MessageRepository extends JpaRepository<Message,Long> {
    Page<Message> findMessagesByChatIdOrderByDateOfSendDesc(UUID chatId, Pageable pageable);
    void deleteMessagesByChatId(UUID chat_id);
}
