package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.Repository.search.UserSearchRepository;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.util.ProfileStatus;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper mapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSearchRepository userSearchRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getStatusFriends() {
        UserDTO user1 = new UserDTO();
        UserDTO user2 = new UserDTO();
        user1.setId(1L);
        user2.setId(2L);
        Mockito.when(userService.loadUserDtoById(1)).thenReturn(user1);
        Mockito.when(userService.loadUserDtoById(2)).thenReturn(user2);
        Mockito.when(userService.loadUserFriends(1)).thenReturn(Set.of(user2));
        Assertions.assertEquals(ProfileStatus.FRIENDS, profileService.getStatus(1,2));
    }

    @Test
    void getStatusInvite() {
        UserDTO user1 = new UserDTO();
        UserDTO user2 = new UserDTO();
        user1.setId(1L);
        user2.setId(2L);
        Mockito.when(userService.loadUserDtoById(1)).thenReturn(user1);
        Mockito.when(userService.loadUserDtoById(2)).thenReturn(user2);
        Mockito.when(userService.loadInvites(1)).thenReturn(Set.of(user2));
        Assertions.assertEquals(ProfileStatus.INVITE, profileService.getStatus(1,2));
    }

    @Test
    void getStatusInvited() {
        UserDTO user1 = new UserDTO();
        UserDTO user2 = new UserDTO();
        user1.setId(1L);
        user2.setId(2L);
        Mockito.when(userService.loadUserDtoById(1)).thenReturn(user1);
        Mockito.when(userService.loadUserDtoById(2)).thenReturn(user2);
        Mockito.when(userService.loadInvites(1)).thenReturn(Collections.emptySet());
        Mockito.when(userService.loadInvites(2)).thenReturn(Set.of(user1));
        Assertions.assertEquals(ProfileStatus.INVITED, profileService.getStatus(1,2));
    }

    @Test
    void getStatusWithIdenticalIds() {
        Assertions.assertThrows(UsernameIsUsedException.class, () -> profileService.getStatus(0,0));
    }

    @Test
    void settingWithCorrectData(){
        SettingRequestDTO requestDTO = new SettingRequestDTO("","","",new Date(),false);
        Mockito.when(userRepository.findUserById(0L)).thenReturn(Optional.of(new User()));
        Mockito.when(mapper.map(Mockito.any(User.class), Mockito.eq(UserSearchDocument.class))).thenReturn(new UserSearchDocument());
        Assertions.assertDoesNotThrow(()->profileService.setting(0L,requestDTO));
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verify(userSearchRepository).save(Mockito.any(UserSearchDocument.class));
    }

    @Test
    void settingNonExistUser(){
        Assertions.assertThrows(UsernameNotFoundException.class,()->profileService.setting(0L,new SettingRequestDTO()));
    }

    @Test
    void getPublicUser() {
        UserDTO startUser = new UserDTO();
        startUser.setId(0L);
        startUser.setEmail("test");
        startUser.setPrivate(false);
        UserDTO finalUser = new UserDTO();
        finalUser.setId(0L);
        finalUser.setEmail("test");
        finalUser.setPrivate(false);
        Mockito.when(userService.loadUserDtoById(0L)).thenReturn(startUser);
        Assertions.assertEquals(finalUser,profileService.getUser(null,0L));
    }

    @Test
    void getPrivateUserWithIdenticalIds() {
        UserDTO startUser = new UserDTO();
        startUser.setId(0L);
        startUser.setEmail("test");
        startUser.setPrivate(true);
        UserDTO finalUser = new UserDTO();
        finalUser.setId(0L);
        finalUser.setEmail("test");
        finalUser.setPrivate(true);
        Mockito.when(userService.loadUserDtoById(0L)).thenReturn(startUser);
        JwtUserDetails userDetails = new JwtUserDetails(0L,false,"");
        Assertions.assertEquals(finalUser,profileService.getUser(userDetails,0L));
    }

    @Test
    void getPrivateUserWithAdminPrincipal() {
        UserDTO startUser = new UserDTO();
        startUser.setId(0L);
        startUser.setEmail("test");
        startUser.setPrivate(true);
        UserDTO finalUser = new UserDTO();
        finalUser.setId(0L);
        finalUser.setEmail("test");
        finalUser.setPrivate(true);
        Mockito.when(userService.loadUserDtoById(0L)).thenReturn(startUser);
        JwtUserDetails userDetails = new JwtUserDetails(1L,true,"");
        Assertions.assertEquals(finalUser,profileService.getUser(userDetails,0L));
    }

    @Test
    void getPrivateFriendUser() {
        UserDTO startUser = new UserDTO();
        startUser.setId(0L);
        startUser.setEmail("test");
        startUser.setPrivate(true);
        UserDTO finalUser = new UserDTO();
        finalUser.setId(0L);
        finalUser.setEmail("test");
        finalUser.setPrivate(true);
        UserDTO principal = new UserDTO();
        principal.setId(1L);
        Mockito.when(userService.loadUserDtoById(1L)).thenReturn(principal);
        Mockito.when(userService.loadUserFriends(1L)).thenReturn(Set.of(startUser));
        Mockito.when(userService.loadUserDtoById(0L)).thenReturn(startUser);
        JwtUserDetails userDetails = new JwtUserDetails(1L,false,"");
        Assertions.assertEquals(finalUser,profileService.getUser(userDetails,0L));
    }

    @Test
    void getPrivateUser() {
        UserDTO startUser = new UserDTO();
        startUser.setId(0L);
        startUser.setEmail("test");
        startUser.setPrivate(true);
        UserDTO finalUser = new UserDTO();
        finalUser.setId(0L);
        finalUser.setEmail(null);
        finalUser.setPrivate(true);
        Mockito.when(userService.loadUserDtoById(0L)).thenReturn(startUser);
        Assertions.assertEquals(finalUser,profileService.getUser(null,0L));
    }

    @Test
    void getNonExistUser() {
        Mockito.when(userService.loadUserDtoById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,()->profileService.getUser(null,0L));
    }
}