package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Chat;
import com.media.socialmedia.Entity.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Repeat;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    private Chat chat;
    private Message message1;
    private Message message2;
    private Message message3;

    @BeforeEach
    void setUp() {
        chat = new Chat();
        chatRepository.save(chat);
        message1 = new Message();
        message1.setDateOfSend(new Date(new Date().getTime() -1));
        message1.setChat(chat);
        message2 = new Message();
        message2.setDateOfSend(new Date(new Date().getTime() -3));
        message2.setChat(chat);
        message3 = new Message();
        message3.setDateOfSend(new Date(new Date().getTime() -2));
        message3.setChat(chat);
        messageRepository.saveAll(List.of(message1,message2,message3));
    }

    @Test
    void findMessagesByChatIdOrderByDateOfSendDesc() {
        assertEquals(List.of(message1,message3,message2),
                messageRepository.findMessagesByChatIdOrderByDateOfSendDesc(chat.getId(), PageRequest.ofSize(5))
                        .toList());
    }

    @Test
    void deleteMessagesByChatId() {
        assertEquals(List.of(message1,message2,message3),
                messageRepository.findAllById(List.of(message1.getId(),message2.getId(),message3.getId())));
        messageRepository.deleteMessagesByChatId(chat.getId());
        assertEquals(Collections.emptyList(),
                messageRepository.findAllById(List.of(message1.getId(),message2.getId(),message3.getId())));
    }
}