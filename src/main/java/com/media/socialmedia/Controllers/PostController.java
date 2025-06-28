package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.PostResponseDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.LikeService;
import com.media.socialmedia.Services.PostService;
import com.media.socialmedia.util.PostNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final LikeService likeService;

    @Autowired
    public PostController(PostService postService, LikeService likeService) {
        this.postService = postService;
        this.likeService = likeService;
    }

    @GetMapping("/users/{id}")
    public Set<PostResponseDTO> getAllPostsByUser(@PathVariable("id") long userId){
        return postService.loadPostsByUserId(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> post(@PathVariable("id") Long id){
        try {
            return ResponseEntity.ok(postService.getPost(id));
        } catch (PostNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping(value="/", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> create(@RequestPart("text") @Valid String text,
                                    @RequestPart("image")MultipartFile file,
                                    @AuthenticationPrincipal JwtUserDetails userDetails){
        if (file == null || file.isEmpty()) {
            postService.create(userDetails.getUserId(),text);
        }
        else {
            try {
                postService.create(userDetails.getUserId(),text,file);
            } catch (RuntimeException e) {
                return new ResponseEntity<>("Error saving file",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return ResponseEntity.ok("success");
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<?> like(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal JwtUserDetails userDetails){
        try {
            return ResponseEntity.ok(likeService.like(userDetails.getUserId(), id));
        } catch (PostNotFoundException | UsernameNotFoundException e) {
           return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
    }
}
