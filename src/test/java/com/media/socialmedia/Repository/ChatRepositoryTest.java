package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.Chat;
import com.media.socialmedia.Entity.User;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findUserChatsOrderByDateOfLastSendDesc() {
        User user = new User();
        userRepository.save(user);
        Chat chat1 = new Chat();
        chat1.setMembers(Set.of(user));
        chat1.setDateOfLastSend(new Date(new Date().getTime() - 1));
        Chat chat2 = new Chat();
        chat2.setMembers(Set.of(user));
        chat2.setDateOfLastSend(new Date(new Date().getTime() - 3));
        Chat chat3 = new Chat();
        chat3.setMembers(Set.of(user));
        chat3.setDateOfLastSend(new Date(new Date().getTime() - 2));
        chatRepository.saveAll(List.of(chat1,chat2,chat3));
        assertEquals(Set.of(chat1.getId(),chat3.getId(),chat2.getId()),
                chatRepository.findUserChatsOrderByDateOfLastSendDesc(user.getId()));
    }

    @Test
    void isUserMember() {
        Chat chat = new Chat();
        User user = new User();
        chat.setMembers(Set.of(user));
        chatRepository.save(chat);
        userRepository.save(user);
        assertTrue(chatRepository.isUserMember(user.getId(),chat.getId()));
    }

    @Test
    void isUserMemberWrong() {
        Chat chat = new Chat();
        chatRepository.save(chat);
        User user = new User();
        userRepository.save(user);
        assertFalse(chatRepository.isUserMember(user.getId(),chat.getId()));
    }

    @Test
    void findOtherMember() {
        User user1 = new User();
        User user2 = new User();
        userRepository.saveAll(List.of(user1,user2));
        Chat chat = new Chat();
        chat.setMembers(Set.of(user1,user2));
        chatRepository.save(chat);
        assertEquals(user2.getId(),
                chatRepository.findOtherMember(user1.getId(),chat.getId()));
        assertEquals(user1.getId(),
                chatRepository.findOtherMember(user2.getId(),chat.getId()));
    }
}