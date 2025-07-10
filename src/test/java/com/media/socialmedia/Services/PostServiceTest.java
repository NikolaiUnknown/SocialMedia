package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.PostResponseDTO;
import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.util.PostNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.RedisConnectionFailureException;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeService likeService;

    @Mock
    private ModelMapper mapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PostService postService;

    private PostResponseDTO mockForGet(){
        Mockito.when(cacheService.getCacheFrom(Mockito.any(),Mockito.any(),Mockito.any())).thenThrow(RedisConnectionFailureException.class);
        PostResponseDTO response = new PostResponseDTO();
        Mockito.when(mapper.map(Mockito.any(Post.class),Mockito.eq(PostResponseDTO.class))).thenReturn(response);
        return response;
    }

    @Test
    void loadPostsByUserIdWithCorrectData() {
        Mockito.when(postRepository.findPostsIdByUserId(0L)).thenReturn(Set.of(0L));
        PostResponseDTO response = mockForGet();
        Mockito.when(postRepository.findById(0L)).thenReturn(Optional.of(new Post()));
        Assertions.assertEquals(Set.of(response), postService.loadPostsByUserId(0L));
    }

    @Test
    void createPostWithCorrectData() {
        mockForGet();
        Assertions.assertDoesNotThrow(()-> postService.create(0L,""));
        Mockito.verify(postRepository).save(Mockito.any(Post.class));
    }
    @Test
    void getPostWithCorrectData() {
        PostResponseDTO response = mockForGet();
        Mockito.when(postRepository.findById(0L)).thenReturn(Optional.of(new Post()));
        Assertions.assertEquals(response,postService.getPost(0L));
    }

    @Test
    void getNonExistPost() {
        Mockito.when(cacheService.getCacheFrom(Mockito.any(),Mockito.any(),Mockito.any())).thenThrow(RedisConnectionFailureException.class);
        Assertions.assertThrows(PostNotFoundException.class,()-> postService.getPost(0L));
    }
}