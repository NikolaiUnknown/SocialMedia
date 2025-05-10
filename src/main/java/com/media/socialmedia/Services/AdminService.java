package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.PostNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    @Autowired
    public AdminService(UserRepository userRepository, PostRepository postRepository, UserService userService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.userService = userService;
    }

    public void assign(Long userId){
        try {
            User user = userService.loadUserById(userId);
            if (user.isAdmin()){
                throw new UsernameIsUsedException("This user is already admin");
            }
            user.setAdmin(true);
            userRepository.save(user);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("Post not found!"));
        for (User user : post.getLikes()) {
            user.getLikes().remove(post);
        }
        post.getLikes().clear();
        postRepository.save(post);
        postRepository.delete(post);
    }

    public void ban(Long userId) {
        try {
            User user = userService.loadUserById(userId);
            if (user.isBlocked()){
                throw new UsernameIsUsedException("This user is already banned");
            }
            user.setBlocked(true);
            userRepository.save(user);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public void unban(Long userId) {
        try {
            User user = userService.loadUserById(userId);
            if (!user.isBlocked()){
                throw new UsernameIsUsedException("This user is not banned");
            }
            user.setBlocked(false);
            userRepository.save(user);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
}
