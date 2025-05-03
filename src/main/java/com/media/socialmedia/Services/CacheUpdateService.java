package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CacheUpdateService {
    private final CacheService cacheService;
    @Autowired
    public CacheUpdateService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void addToFriends(Long userId, Set<User> friends, Long friendId){
        try {
            Set<Long> set = friends.stream().map(User::getId).collect(Collectors.toSet());
            set.add(friendId);
            cacheService.updateFriends(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void addToFriendsOf(Long userId,Set<User> friendsOf, Long friendId){
        try {
            Set<Long> set = friendsOf.stream().map(User::getId).collect(Collectors.toSet());
            set.add(friendId);
            cacheService.updateFriendsOf(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void addToBlacklist(Long userId,Set<User> blacklist, Long friendId){
        try {
            Set<Long> set = blacklist.stream().map(User::getId).collect(Collectors.toSet());
            set.add(friendId);
            cacheService.updateBlacklist(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void addToInvites(Long userId,Set<User> invites, Long friendId){
        try {
            Set<Long> set = invites.stream().map(User::getId).collect(Collectors.toSet());
            set.add(friendId);
            cacheService.updateInvites(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void addToInvitesOf(Long userId,Set<User> invitesOf, Long friendId){
        try {
            Set<Long> set = invitesOf.stream().map(User::getId).collect(Collectors.toSet());
            set.add(friendId);
            cacheService.updateInvitesOf(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void deleteFromFriends(Long userId, Set<User> friends, Long friendId){
        try {
            Set<Long> set = friends.stream().map(User::getId).collect(Collectors.toSet());
            set.remove(friendId);
            cacheService.updateFriends(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void deleteFromFriendsOf(Long userId,Set<User> friendsOf, Long friendId){
        try {
            Set<Long> set = friendsOf.stream().map(User::getId).collect(Collectors.toSet());
            set.remove(friendId);
            cacheService.updateFriendsOf(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void deleteFromBlacklist(Long userId,Set<User> blacklist, Long friendId){
        try {
            Set<Long> set = blacklist.stream().map(User::getId).collect(Collectors.toSet());
            set.remove(friendId);
            cacheService.updateBlacklist(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void deleteFromInvites(Long userId,Set<User> invites, Long friendId){
        try {
            Set<Long> set = invites.stream().map(User::getId).collect(Collectors.toSet());
            set.remove(friendId);
            cacheService.updateInvites(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void deleteFromInvitesOf(Long userId,Set<User> invitesOf, Long friendId){
        try {
            Set<Long> set = invitesOf.stream().map(User::getId).collect(Collectors.toSet());
            set.remove(friendId);
            cacheService.updateInvitesOf(userId,set);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }

    public void deletePostFromCache(Long id){
        try {
            cacheService.deletePost(id);
        } catch (RedisConnectionFailureException e) {
            System.out.println("Cannot connect to Redis");
        }
    }
}
