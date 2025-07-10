package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.ProfileStatus;
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
class BlackListServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private ProfileService profileService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlackListService blackListService;

    @Test
    void isInBlackListWithBlackListedUser() {
        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        Mockito.when(userService.loadUserBlacklist(1L)).thenReturn(Set.of(user2));
        Mockito.when(userService.loadUserDtoById(2L)).thenReturn(user2);
        Assertions.assertTrue(blackListService.isInBlackList(1L,2L));

    }

    @Test
    void isInBlackListWithNonBlackListedUser() {
        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        Mockito.when(userService.loadUserBlacklist(1L)).thenReturn(new HashSet<>());
        Mockito.when(userService.loadUserDtoById(2L)).thenReturn(user2);
        Assertions.assertFalse(blackListService.isInBlackList(1L,2L));

    }

    @Test
    void isInBlackListNonExistUser() {
        Mockito.when(userService.loadUserDtoById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,()->blackListService.isInBlackList(1L,0L));
    }

    @Test
    void isInBlackListWithIdenticalUsers() {
        Assertions.assertFalse(blackListService.isInBlackList(0L,0L));
    }

    @Test
    void addToBlackListWithCorrectData() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(2L)).thenReturn(user2);
        Mockito.when(profileService.getStatus(1L,2L)).thenReturn(ProfileStatus.UNKNOWN);
        Assertions.assertDoesNotThrow(() -> blackListService.addToBlackList(1L,2L));
        Mockito.verify(userRepository).save(user1);
    }

    @Test
    void addToBlackListAlreadyBlacklistedUsers() {
        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        Mockito.when(userService.loadUserBlacklist(1L)).thenReturn(Set.of(user2));
        Mockito.when(userService.loadUserDtoById(2L)).thenReturn(user2);
        Assertions.assertThrows(UsernameIsUsedException.class,()->blackListService.addToBlackList(1L,2L));
    }

    @Test
    void addToBlackListNonExistUser() {
        Mockito.when(userService.loadUserById(1L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,()->blackListService.addToBlackList(1L,2L));
    }

    @Test
    void addToBlackListFriends() {
        Mockito.when(profileService.getStatus(0L,1L)).thenReturn(ProfileStatus.FRIENDS);
        Assertions.assertThrows(UsernameIsUsedException.class,()->blackListService.addToBlackList(0L,1L));
    }

    @Test
    void addToBlackListWithIdenticalUser() {
        Assertions.assertThrows(UsernameIsUsedException.class,()->blackListService.addToBlackList(0L,0L));
    }

    @Test
    void removeFromBlackListWithCorrectData() {
        User user1 = new User();
        user1.setId(1L);
        UserDTO user2Dto = new UserDTO();
        user2Dto.setId(2L);
        Mockito.when(userService.loadUserBlacklist(1L)).thenReturn(Set.of(user2Dto));
        Mockito.when(userService.loadUserDtoById(2L)).thenReturn(user2Dto);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user1);
        Assertions.assertDoesNotThrow(()->blackListService.removeFromBlackList(1L,2L));
        Mockito.verify(userRepository).save(user1);
    }

    @Test
    void removeFromBlackListNotBlacklistedUser() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(2L)).thenReturn(user2);
        Assertions.assertThrows(UsernameIsUsedException.class,()->blackListService.removeFromBlackList(1L,2L));
    }

    @Test
    void removeFromBlackListNonExistUser() {
        Mockito.when(userService.loadUserById(1L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,()->blackListService.removeFromBlackList(1L,2L));
    }

    @Test
    void removeFromBlackListWithIdenticalUser() {
        Assertions.assertThrows(UsernameIsUsedException.class,()->blackListService.removeFromBlackList(0L,0L));
    }

    @Test
    void getBlacklist() {
        UserDTO userDTO = new UserDTO();
        Mockito.when(userService.loadUserBlacklist(0L)).thenReturn(Set.of(userDTO));
        Assertions.assertEquals(Set.of(userDTO), blackListService.getBlacklist(0L));
    }

    @Test
    void getBlacklistNonExistUser() {
        Mockito.when(userService.loadUserBlacklist(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,()-> blackListService.getBlacklist(0L));
    }
}