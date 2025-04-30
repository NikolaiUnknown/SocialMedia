package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.UserNotCreatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public LikeService(PostRepository postRepository, UserRepository userRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public long like(long userId, long postId){
        try {
            User user = userService.loadUserById(userId);
            Post post = postRepository.findById(postId).orElseThrow(() -> new UserNotCreatedException("Post not found!"));
            if (user.getLikes().contains(post)){
                user.getLikes().remove(post);
            }
            else {
                user.getLikes().add(post);
            }
            userRepository.save(user);
            return getCountOfLike(postId);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
    public long getCountOfLike(Long postId){
        return postRepository.getPostById(postId).getLikes().size();
    }
}
