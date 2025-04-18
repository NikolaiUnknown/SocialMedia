package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.UserNotCreatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    @Autowired
    public LikeService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public long like(long userId, long postId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new UserNotCreatedException("Post not found!"));
        if (user.getLikes().contains(post)){
            user.getLikes().remove(post);
        }
        else {
            user.getLikes().add(post);
        }
        userRepository.save(user);
        return getCountOfLike(postId);
    }
    public long getCountOfLike(Long postId){
        return postRepository.getPostById(postId).getLikes().size();
    }
}
