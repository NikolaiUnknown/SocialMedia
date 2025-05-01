package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.PostCreateRequestDTO;
import com.media.socialmedia.DTO.PostResponseDTO;
import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.util.PostNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository repository;
    private final ModelMapper mapper;
    private final LikeService likeService;
    @Value("${socialmedia.pictures.dir}")
    private  String pictureDirectory;
    @Autowired
    public PostService(PostRepository repository, ModelMapper mapper, LikeService likeService) {
        this.repository = repository;
        this.mapper = mapper;
        this.likeService = likeService;
    }
    @Cacheable(value = "posts",key = "#userId")
    public Set<PostResponseDTO> loadPostsByUserId(long userId){
        return repository.getPostsByUserId(userId).stream()
                .map((Post p) -> {
                    PostResponseDTO response =mapper.map(p, PostResponseDTO.class);
                    response.setCountOfLike(likeService.getCountOfLike(p.getId()));
                    return response;
                })
                .collect(Collectors.toSet());
    }
    public void create(Long userId, PostCreateRequestDTO request){
        Post post = new Post();
        post.setText(request.getText());
        post.setPhotoUrl(null);
        post.setUserId(userId);
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
        repository.save(post);
    }
    public Post loadPostById(Long id){
        Optional<Post> post = Optional.ofNullable(repository.getPostById(id));
        if (post.isEmpty()){
            throw new PostNotFoundException("Post not found!");
        }
        return post.get();
    }
    @Cacheable(value = "post",key = "#id")
    public PostResponseDTO getPost(Long id){
        PostResponseDTO post = converToPostResponse(loadPostById(id));
        post.setCountOfLike(likeService.getCountOfLike(id));
        return post;
    }
    private PostResponseDTO converToPostResponse(Post post){return mapper.map(post, PostResponseDTO.class);}

}
