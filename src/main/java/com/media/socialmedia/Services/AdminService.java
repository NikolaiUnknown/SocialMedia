package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDataResponse;
import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.PostNotFoundException;
import com.media.socialmedia.util.UserNotCreatedException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Autowired
    public AdminService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public void assign(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        if (user.isAdmin()){
            throw new UsernameIsUsedException("This user is already admin");
        }
        user.setAdmin(true);
        userRepository.save(user);
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
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        if (user.isBlocked()){
            throw new UsernameIsUsedException("This user is already banned");
        }
        user.setBlocked(true);
        userRepository.save(user);
    }

    public void unban(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        if (!user.isBlocked()){
            throw new UsernameIsUsedException("This user is not banned");
        }
        user.setBlocked(false);
        userRepository.save(user);
    }

    public Set<UserDataResponse> getUsersWhoLike(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("Post not found!"));
        return post.getLikes().stream().map(this::userToUserDataResponse).collect(Collectors.toSet());
    }
    private UserDataResponse userToUserDataResponse(User user){
        return new UserDataResponse(user);
    }
}
