package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.PostCreateRequestDTO;
import com.media.socialmedia.DTO.PostResponseDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.LikeService;
import com.media.socialmedia.Services.PostService;
import com.media.socialmedia.Services.UserService;
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
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private final UserService userService;
    @Autowired
    public PostController(PostService postService, LikeService likeService, UserService userService) {
        this.postService = postService;
        this.likeService = likeService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public PostResponseDTO post(@PathVariable("id") Long id){
        return postService.getPost(id);
    }
    @GetMapping("/all/{id}")
    public PostResponseDTO[] getAllPostsByUser(@PathVariable("id") long userId){
        return postService.loadPostsByUserId(userId);
    }
    @PostMapping("/like/{id}")
    public ResponseEntity<?> like(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal JwtUserDetails userDetails){
        try {
            postService.loadPostById(id);
        } catch (PostNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(likeService.like(userDetails.getUserId(), id));
    }
    @PostMapping(value="/new", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> create(@RequestPart("text") @Valid PostCreateRequestDTO request,
                                   @RequestPart("image")MultipartFile file,
                                    @AuthenticationPrincipal JwtUserDetails userDetails){
        if (file.isEmpty()) {
            postService.create(userDetails.getUserId(),request);
        }
        try {
            postService.create(userDetails.getUserId(),request,file);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error saving file",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("success");
    }
}
