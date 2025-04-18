package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.PostCreateRequest;
import com.media.socialmedia.DTO.PostResponse;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Security.UserDetailsImpl;
import com.media.socialmedia.Services.LikeService;
import com.media.socialmedia.Services.PostService;
import com.media.socialmedia.util.PostNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    @Autowired
    public PostController(PostService postService, LikeService likeService) {
        this.postService = postService;
        this.likeService = likeService;
    }

    @GetMapping("/{id}")
    public PostResponse post(@PathVariable("id") Long id){
        return postService.getPost(id);
    }
    @GetMapping("/all/{id}")
    public PostResponse[] getAllPostsByUser(@PathVariable("id") long userId){

        return postService.loadPostsByUserId(userId);
    }
    @PostMapping("/like/{id}")
    public ResponseEntity<?> like(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails){
        User user = userDetails.getUser();
        try {
            postService.loadPostById(id);
        } catch (PostNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(likeService.like(user.getId(), id));
    }
    @PostMapping(value="/new", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> create(@RequestPart("text") @Valid PostCreateRequest request,
                                   @RequestPart("image")MultipartFile file,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails){
        User user = userDetails.getUser();
        if (file.isEmpty()) {
            postService.create(user,request);
        }
        try {
            postService.create(user,request,file);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Ошибка при сохранении файла",HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("success");
    }
}
