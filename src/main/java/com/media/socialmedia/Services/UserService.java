package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.Security.AuthDetailsImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
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
    private final ModelMapper mapper;

    @Autowired
    public UserService(UserRepository userRepository, CacheService cacheService, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
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

    public UserDTO loadUserDtoById(long id){
        try {
            return cacheService.loadCachedUserDtoById(id);
        }catch (RedisConnectionFailureException e){
            return mapper.map(loadUserById(id),UserDTO.class);
        }
    }

    public Set<UserDTO> loadUserFriends(long id){
        try {
            return cacheService.loadCachedUserFriends(id).stream()
                    .map(cacheService::loadCachedUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findFriendsById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

    public Set<UserDTO> loadUserFriendsOf(long id){
        try {
            return cacheService.loadCachedUserFriendsOf(id).stream()
                    .map(cacheService::loadCachedUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findFriendsOfById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

    public Set<UserDTO> loadUserBlacklist(long id){
        try {
            return cacheService.loadCachedUserBlacklist(id).stream()
                    .map(cacheService::loadCachedUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findBlacklistById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

    public Set<UserDTO> loadInvites(long id){
        try {
            return cacheService.loadCachedInvites(id).stream()
                    .map(cacheService::loadCachedUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findInvitesById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

    public Set<UserDTO> loadInvitesForMe(long id){
        try {
            return cacheService.loadCachedInvitesForMe(id).stream()
                    .map(cacheService::loadCachedUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findInvitesOfById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

}