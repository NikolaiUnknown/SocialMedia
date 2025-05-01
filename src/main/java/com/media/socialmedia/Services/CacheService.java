package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.PostResponseDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.Post;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.PostRepository;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.PostNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CacheService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    @Autowired
    public CacheService(PostRepository postRepository, UserRepository userRepository, ModelMapper mapper) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Cacheable(value = "users", key = "#id")
    public UserDTO loadCachedUserDtoById(long id){
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));
        return mapper.map(user, UserDTO.class);
    }

    @Cacheable(value = "friends", key = "#id")
    public Set<Long> loadCachedUserFriends(long id) {
        return userRepository.findFriendsById(id);
    }
    @Cacheable(value = "friendsOf", key = "#id")
    public Set<Long> loadCachedUserFriendsOf(long id) {
        return userRepository.findFriendsOfById(id);
    }
    @Cacheable(value = "blacklist", key = "#id")
    public Set<Long> loadCachedUserBlacklist(long id) {
        return userRepository.findBlacklistById(id);
    }
    @Cacheable(value = "invites", key = "#id")
    public Set<Long> loadCachedInvites(long id) {
        return userRepository.findInvitesById(id);
    }
    @Cacheable(value = "invitesOf", key = "#id")
    public Set<Long> loadCachedInvitesForMe(long id) {
        return userRepository.findInvitesOfById(id);
    }

    @Cacheable(value = "post",key = "#id")
    public PostResponseDTO getCachedPost(Long id){
        Post post = postRepository.findById(id).orElseThrow(()-> new PostNotFoundException("Post not found!"));
        return mapper.map(post, PostResponseDTO.class);
    }

    @Cacheable(value = "posts",key = "#userId")
    public Set<Long> loadCachedPostsByUserId(long userId){
        return postRepository.findPostsIdByUserId(userId);
    }
}
