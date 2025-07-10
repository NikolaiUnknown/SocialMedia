package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findUserByEmail() {
        User user = new User();
        user.setEmail("email");
        userRepository.save(user);
        assertEquals(user, userRepository.findUserByEmail("email"));
    }

    @Test
    void findNonExistUserByEmail() {
        assertNull(userRepository.findUserByEmail("nonExist"));
    }

    @Test
    void findUserByGeneratedId() {
        User user = new User();
        userRepository.save(user);
        assertEquals(user,userRepository.findUserById(user.getId()).orElseThrow());
    }

    @Test
    void findNonExistUserById() {
        assertTrue(userRepository.findUserById(0L).isEmpty());
    }

    @Test
    void findFriendsById() {
        User user1 = new User();
        User user2 = new User();
        user1.setFriends(Set.of(user2));
        userRepository.save(user1);
        userRepository.save(user2);
        assertEquals(Set.of(user2.getId()),userRepository.findFriendsById(user1.getId()));
    }

    @Test
    void findFriendsOfById() {
        User user1 = new User();
        User user2 = new User();
        user1.setFriends(Set.of(user2));
        userRepository.save(user1);
        userRepository.save(user2);
        assertEquals(Set.of(user1.getId()),userRepository.findFriendsOfById(user2.getId()));
    }

    @Test
    void findBlacklistById() {
        User user1 = new User();
        User user2 = new User();
        user1.setBlacklist(Set.of(user2));
        userRepository.save(user1);
        userRepository.save(user2);
        assertEquals(Set.of(user2.getId()),userRepository.findBlacklistById(user1.getId()));
    }

    @Test
    void findInvitesById() {
        User user1 = new User();
        User user2 = new User();
        user1.setUsersInvitedByMe(Set.of(user2));
        userRepository.save(user1);
        userRepository.save(user2);
        assertEquals(Set.of(user2.getId()),userRepository.findInvitesById(user1.getId()));

    }

    @Test
    void findInvitesOfById() {
        User user1 = new User();
        User user2 = new User();
        user1.setUsersInvitedByMe(Set.of(user2));
        userRepository.save(user1);
        userRepository.save(user2);
        assertEquals(Set.of(user1.getId()),userRepository.findInvitesOfById(user2.getId()));
    }
}