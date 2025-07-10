package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;
@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private FriendService friendService;

    @Test
    void getAllFriendsWithCorrectData() {
        UserDTO friend1 = new UserDTO();
        friend1.setId(1L);
        UserDTO friend2 = new UserDTO();
        friend2.setId(2L);
        Set<UserDTO> userFriends = new HashSet<>(Set.of(friend1));
        Set<UserDTO> userFriendsOf = new HashSet<>(Set.of(friend2));
        Mockito.when(userService.loadUserFriends(0L)).thenReturn(userFriends);
        Mockito.when(userService.loadUserFriendsOf(0L)).thenReturn(userFriendsOf);
        Assertions.assertEquals(Set.of(friend1,friend2),friendService.getAllFriends(0L));
    }

    @Test
    void getAllFriendsNonExistUser() {
        Mockito.when(userService.loadUserFriends(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,()->friendService.getAllFriends(0L));
    }

    @Test
    void getUsersInvitedByMeWithCorrectData() {
        UserDTO invited = new UserDTO();
        invited.setId(1L);
        Set<UserDTO> invites = new HashSet<>(Set.of(invited));
        Mockito.when(userService.loadInvites(0L)).thenReturn(invites);
        Assertions.assertEquals(invites,friendService.getUsersInvitedByMe(0L));
    }

    @Test
    void getUsersInvitedByNonExistUser() {
        Mockito.when(userService.loadInvites(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,()->friendService.getUsersInvitedByMe(0L));
    }

    @Test
    void getUsersInvitingMeWithCorrectData() {
        UserDTO invited = new UserDTO();
        invited.setId(1L);
        Set<UserDTO> inviting = new HashSet<>(Set.of(invited));
        Mockito.when(userService.loadInvitesForMe(0L)).thenReturn(inviting);
        Assertions.assertEquals(inviting,friendService.getUsersInvitingMe(0L));
    }

    @Test
    void getUsersInvitingMeNonExistUser() {
        Mockito.when(userService.loadInvitesForMe(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,()->friendService.getUsersInvitingMe(0L));
    }

    @Test
    void inviteToFriendWithCorrectData() {
        User user1 = new User();
        user1.setId(0L);
        User user2 = new User();
        user2.setId(1L);
        Mockito.when(userService.loadUserById(0L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user2);
        Assertions.assertDoesNotThrow(()->friendService.inviteToFriend(0L,1L));
        Mockito.verify(userRepository).save(user1);
    }

    @Test
    void inviteToFriendAlreadyFriend() {
        User user1 = new User();
        user1.setId(0L);
        User user2 = new User();
        user2.setId(1L);
        user1.setFriends(Set.of(user2));
        Mockito.when(userService.loadUserById(0L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user2);
        Assertions.assertThrows(UsernameIsUsedException.class,
                ()->friendService.inviteToFriend(0L,1L));
    }

    @Test
    void inviteToFriendBlacklistedUser() {
        User user1 = new User();
        user1.setId(0L);
        User user2 = new User();
        user2.setId(1L);
        user1.setBlacklist(Set.of(user2));
        Mockito.when(userService.loadUserById(0L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user2);
        Assertions.assertThrows(UsernameIsUsedException.class,
                ()->friendService.inviteToFriend(0L,1L));
    }

    @Test
    void inviteToFriendWithIdenticalUser() {
        Assertions.assertThrows(UsernameIsUsedException.class,
                ()->friendService.inviteToFriend(0L,0L));
    }

    @Test
    void inviteToFriendNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,
                ()->friendService.inviteToFriend(0L,1L));
    }

    @Test
    void acceptToFriendWithCorrectData() {
        User user1 = new User();
        User user2 = new User();
        user2.setUsersInvitedByMe(new HashSet<>(Set.of(user1)));
        Mockito.when(userService.loadUserById(0L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user2);
        Assertions.assertDoesNotThrow(()-> friendService.acceptToFriend(0L,1L));
        Mockito.verify(userRepository,Mockito.times(2))
                .save(Mockito.any(User.class));
    }

    @Test
    void acceptToFriendNonExistInvite() {
        Mockito.when(userService.loadUserById(0L)).thenReturn(new User());
        Mockito.when(userService.loadUserById(1L)).thenReturn(new User());
        Assertions.assertThrows(InviteNotFoundException.class,
                ()-> friendService.acceptToFriend(0L,1L));
    }

    @Test
    void acceptToFriendNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,
                ()-> friendService.acceptToFriend(0L,0L));
    }

    @Test
    void denyInvitedByMe() {
        User user1 = new User();
        User user2 = new User();
        user2.setUsersInvitedByMe(new HashSet<>(Set.of(user1)));
        Mockito.when(userService.loadUserById(0L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user2);
        Assertions.assertDoesNotThrow(()-> friendService.deny(0L,1L));
        Mockito.verify(userRepository).save(user2);
    }

    @Test
    void denyInvitingMe() {
        User user1 = new User();
        User user2 = new User();
        user1.setUsersInvitedByMe(new HashSet<>(Set.of(user2)));
        Mockito.when(userService.loadUserById(0L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user2);
        Assertions.assertDoesNotThrow(()-> friendService.deny(0L,1L));
        Mockito.verify(userRepository).save(user1);
    }

    @Test
    void denyNonExistInvite() {
        Mockito.when(userService.loadUserById(0L)).thenReturn(new User());
        Mockito.when(userService.loadUserById(1L)).thenReturn(new User());
        Assertions.assertThrows(InviteNotFoundException.class,
                ()-> friendService.deny(0L,1L));
    }

    @Test
    void denyNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,
                ()-> friendService.deny(0L,0L));
    }

    @Test
    void removeWithCorrectData() {
        User user1 = new User();
        User user2 = new User();
        user1.setFriends(new HashSet<>(Set.of(user2)));
        user2.setFriendsOf(user1.getFriends());
        Mockito.when(userService.loadUserById(0L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user2);
        Assertions.assertDoesNotThrow(()->friendService.remove(0L,1L));
        Mockito.verify(userRepository).save(user2);
    }

    @Test
    void removeNonFriend() {
        Mockito.when(userService.loadUserById(0L)).thenReturn(new User());
        Mockito.when(userService.loadUserById(1L)).thenReturn(new User());
        Assertions.assertThrows(InviteNotFoundException.class,
                ()-> friendService.remove(0L,1L));
    }

    @Test
    void removeNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,
                ()-> friendService.remove(0L,0L));
    }
}