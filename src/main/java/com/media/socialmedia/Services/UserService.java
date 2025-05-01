package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.Security.AuthDetailsImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service

public class UserService implements UserDetailsService{
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    @Autowired
    public UserService(UserRepository userRepository, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = Optional.ofNullable(userRepository.findUserByEmail(username));
        if (user.isEmpty())
            throw new UsernameNotFoundException("User not found!");
        return new AuthDetailsImpl(user.get());
    }
    public User loadUserById(long id){
        return userRepository.findUserById(id).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }
    @Cacheable(value = "users", key = "#id")
    public UserDTO loadUserDtoById(long id){
        User user = loadUserById(id);
        return mapper.map(user, UserDTO.class);
    }



    @Cacheable(value = "friends", key = "#id")
    public Set<UserDTO> loadUserFriends(long id) {
        Set<User> userSet = userRepository.findFriendsById(id);
        return userSet.stream()
                .map((User u) ->mapper.map(u,UserDTO.class))
                .collect(Collectors.toSet());
    }
    @Cacheable(value = "friendsOf", key = "#id")
    public Set<UserDTO> loadUserFriendsOf(long id) {
        Set<User> userSet = userRepository.findFriendsOfById(id);
        return userSet.stream()
                .map((User u) ->mapper.map(u,UserDTO.class))
                .collect(Collectors.toSet());
    }
    @Cacheable(value = "blacklist", key = "#id")
    public Set<UserDTO> loadUserBlacklist(long id) {
        Set<User> userSet = userRepository.findBlacklistById(id);
        return userSet.stream()
                .map((User u) ->mapper.map(u,UserDTO.class))
                .collect(Collectors.toSet());
    }
    @Cacheable(value = "invites", key = "#id")
    public Set<UserDTO> loadInvites(long id) {
        Set<User> userSet = userRepository.findInvitesById(id);
        return userSet.stream()
                .map((User u) ->mapper.map(u,UserDTO.class))
                .collect(Collectors.toSet());
    }
    @Cacheable(value = "invitesOf", key = "#id")
    public Set<UserDTO> loadInvitesForMe(long id) {
        Set<User> userSet = userRepository.findInvitesOfById(id);
        return userSet.stream()
                .map((User u) ->mapper.map(u,UserDTO.class))
                .collect(Collectors.toSet());
    }
}