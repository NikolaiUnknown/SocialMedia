package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.PostNotFoundException;
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
class LikeServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private LikeService likeService;

    @Test
    void likeWithCorrectData() {
        Mockito.when(userService.loadUserById(0L)).thenReturn(new User());
        Mockito.when(postRepository.countPostLikesById(0L)).thenReturn(4L);
        Mockito.when(postRepository.findById(0L)).thenReturn(Optional.of(new Post()));
        Assertions.assertDoesNotThrow(()->likeService.like(0L,0L));
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void likeByNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class, ()->likeService.like(0L,0L));
    }
    @Test
    void likeByNonExistPost() {
        Mockito.when(userService.loadUserById(0L)).thenReturn(new User());
        Assertions.assertThrows(PostNotFoundException.class, ()->likeService.like(0L,0L));
    }

}