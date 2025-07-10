package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.PostNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void assignNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,() ->adminService.assign(0L));
    }

    @Test
    void assignExistUser() {
        User user = new User();
        Mockito.when(userService.loadUserById(0L)).thenReturn(user);
        Assertions.assertDoesNotThrow(() -> adminService.assign(0L));
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void assignAdmin() {
        User user = new User();
        user.setId(0L);
        user.setAdmin(true);
        Mockito.when(userService.loadUserById(0L)).thenReturn(user);
        Assertions.assertThrows(UsernameIsUsedException.class,()-> adminService.assign(0L));
    }

    @Test
    void banNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class, ()-> adminService.ban(0L));
    }

    @Test
    void banExistUser() {
        User user = new User();
        user.setId(0L);

        Mockito.when(userService.loadUserById(0L)).thenReturn(user);
        Assertions.assertDoesNotThrow(() -> adminService.ban(0L));
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void banAlreadyBlockedUser() {
        User user = new User();
        user.setId(0L);
        user.setBlocked(true);
        Mockito.when(userService.loadUserById(0L)).thenReturn(user);
        Assertions.assertThrows(UsernameIsUsedException.class,()-> adminService.ban(0L));
    }

    @Test
    void unbanNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class, ()-> adminService.unban(0L));
    }

    @Test
    void unbanNonBannedUser() {
        User user = new User();
        user.setId(0L);
        Mockito.when(userService.loadUserById(0L)).thenReturn(user);
        Assertions.assertThrows(UsernameIsUsedException.class,()-> adminService.unban(0L));
    }

    @Test
    void unbanExistUser() {
        User user = new User();
        user.setId(0L);
        user.setBlocked(true);
        Mockito.when(userService.loadUserById(0L)).thenReturn(user);
        Assertions.assertDoesNotThrow(() -> adminService.unban(0L));
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void deleteNotExistPost() {
        Post post = new Post();
        post.setId(0L);
        Mockito.when(postRepository.findById(0L)).thenThrow(PostNotFoundException.class);
        Assertions.assertThrows(PostNotFoundException.class,() -> adminService.deletePost(0L));
    }

    @Test
    void deleteExistPost() {
        Post post = new Post();
        post.setId(0L);
        Mockito.when(postRepository.findById(0L))
                .thenReturn(Optional.of(post));
        Assertions.assertDoesNotThrow(()-> adminService.deletePost(0L));
        Mockito.verify(postRepository).delete(post);
    }
}