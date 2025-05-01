package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.Security.AuthDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CacheService cacheService;
    @Autowired
    public UserService(UserRepository userRepository, CacheService cacheService) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
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

    public UserDTO loadUserDtoById(long id){
        return cacheService.loadCachedUserDtoById(id);
    }

    public Set<UserDTO> loadUserFriends(long id){
        return cacheService.loadCachedUserFriends(id).stream()
                .map(cacheService::loadCachedUserDtoById)
                .collect(Collectors.toSet());
    }

    public Set<UserDTO> loadUserFriendsOf(long id){
        return cacheService.loadCachedUserFriendsOf(id).stream()
                .map(cacheService::loadCachedUserDtoById)
                .collect(Collectors.toSet());
    }

    public Set<UserDTO> loadUserBlacklist(long id){
        return cacheService.loadCachedUserBlacklist(id).stream()
                .map(cacheService::loadCachedUserDtoById)
                .collect(Collectors.toSet());
    }

    public Set<UserDTO> loadInvites(long id){
        return cacheService.loadCachedInvites(id).stream()
                .map(cacheService::loadCachedUserDtoById)
                .collect(Collectors.toSet());
    }

    public Set<UserDTO> loadInvitesForMe(long id){
        return cacheService.loadCachedInvitesForMe(id).stream()
                .map(cacheService::loadCachedUserDtoById)
                .collect(Collectors.toSet());
    }


}