package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.Security.AuthDetailsImpl;
import com.media.socialmedia.util.Caches;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
            return cacheService.getCacheFrom(Caches.USERS,id,() -> {
                User user = loadUserById(id);
                UserDTO dto = mapper.map(user,UserDTO.class);
                dto.setDateOfBirthday(new SimpleDateFormat("dd.MM.yyyy").format(user.getDateOfBirthday()));
                return dto;
            });
        }catch (RedisConnectionFailureException e){
            User user = loadUserById(id);
            UserDTO dto = mapper.map(user,UserDTO.class);
            dto.setDateOfBirthday(new SimpleDateFormat("dd.MM.yyyy").format(user.getDateOfBirthday()));
            return dto;
        }
    }

    public Set<UserDTO> loadUserFriends(long id){
        try {
            return cacheService.getCacheFrom(Caches.FRIENDS,id,() -> userRepository.findFriendsById(id))
                    .stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findFriendsById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

    public Set<UserDTO> loadUserFriendsOf(long id){
        try {
            return cacheService.getCacheFrom(Caches.FRIENDS_OF,id,() -> userRepository.findFriendsOfById(id))
                    .stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findFriendsOfById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

    public Set<UserDTO> loadUserBlacklist(long id){
        try {
            return cacheService.getCacheFrom(Caches.BLACKLIST,id,() -> userRepository.findBlacklistById(id))
                    .stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findBlacklistById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

    public Set<UserDTO> loadInvites(long id){
        try {
            return cacheService.getCacheFrom(Caches.INVITES,id,() -> userRepository.findInvitesById(id))
                    .stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            return userRepository.findInvitesById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }

    public Set<UserDTO> loadInvitesForMe(long id){
        try {
            return cacheService.getCacheFrom(Caches.INVITES_OF,id,() -> userRepository.findInvitesOfById(id))
                    .stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to redis");
            return userRepository.findInvitesOfById(id).stream()
                    .map(this::loadUserDtoById)
                    .collect(Collectors.toSet());
        }
    }
    public void addToCache(Caches cacheName, Long userId, Set<User> users, Long friendId){
        try {
            Set<Long> set = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            set.add(friendId);
            cacheService.updateInCache(cacheName,userId,set);
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to redis");
        }
    }
    public void removeFromCache(Caches cacheName, Long userId, Set<User> users, Long friendId){
        try {
            Set<Long> set = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
            set.remove(friendId);
            cacheService.updateInCache(cacheName,userId,set);
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to redis");
        }
    }
    public void evictFromCache(Caches cacheName, Long key){
        try {
            cacheService.evictFromCache(cacheName, key);
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to redis");
        }
    }
}