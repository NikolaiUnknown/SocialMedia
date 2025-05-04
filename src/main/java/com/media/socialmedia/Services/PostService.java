package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.PostCreateRequestDTO;
import com.media.socialmedia.DTO.PostResponseDTO;
import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.util.Caches;
import com.media.socialmedia.util.PostNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService {

    private final PostRepository repository;
    private final LikeService likeService;
    private final CacheService cacheService;
    private final ModelMapper mapper;
    @Value("${socialmedia.pictures.dir}")
    private  String pictureDirectory;
    @Autowired
    public PostService(PostRepository repository, LikeService likeService, CacheService cacheService, ModelMapper mapper) {
        this.repository = repository;
        this.likeService = likeService;
        this.cacheService = cacheService;
        this.mapper = mapper;
    }
    public Set<PostResponseDTO> loadPostsByUserId(long userId){
        try {
            return cacheService.getCacheFrom(Caches.POSTS,userId,() ->repository.findPostsIdByUserId(userId).stream()
                    .map(this::getPost).collect(Collectors.toSet()));
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to redis");
            return repository.findPostsIdByUserId(userId).stream()
                    .map(this::getPost).collect(Collectors.toSet());
        }
    }
    public void create(Long userId, PostCreateRequestDTO request){
        Post post = new Post();
        post.setText(request.getText());
        post.setPhotoUrl(null);
        post.setUserId(userId);
        addPostToCache(userId, post);
        repository.save(post);
    }
    public void create(Long userId, PostCreateRequestDTO request, MultipartFile file){
        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        String filename = UUID.randomUUID() + extension;
        try {
            file.transferTo(new File(pictureDirectory + filename));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла", e);

        }
        Post post = new Post();
        post.setText(request.getText());
        post.setPhotoUrl(filename);
        post.setUserId(userId);
        addPostToCache(userId, post);
        repository.save(post);
    }
    public PostResponseDTO getPost(Long id){
        try {
            PostResponseDTO response = cacheService.getCacheFrom(Caches.POST, id,() ->{
                Post post = repository.findById(id).orElseThrow(()-> new PostNotFoundException("Post not found!"));
                return mapper.map(post, PostResponseDTO.class);
            });
            response.setCountOfLike(likeService.getCountOfLike(id));
            return response;
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to redis");
            Post post = repository.findById(id).orElseThrow(()-> new PostNotFoundException("Post not found!"));
            PostResponseDTO response = mapper.map(post, PostResponseDTO.class);
            response.setCountOfLike(likeService.getCountOfLike(id));
            return response;
        }
    }

    public void addPostToCache(Long key, Post newPost){
        try {
            Set<PostResponseDTO> response = loadPostsByUserId(key);
            response.add(mapper.map(newPost,PostResponseDTO.class));
            cacheService.updateInCache(Caches.POSTS,key,response);
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to redis");
        }
    }

}
