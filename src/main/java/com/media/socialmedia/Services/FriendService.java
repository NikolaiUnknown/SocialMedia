package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.Caches;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class FriendService {
    private final UserRepository userRepository;
    private final UserService userService;
    @Autowired
    public FriendService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public Set<UserDTO> getAllFriends(long userId){
        try {
            Set<UserDTO> friends = new HashSet<>();
            friends.addAll(userService.loadUserFriends(userId));
            friends.addAll(userService.loadUserFriendsOf(userId));
            return friends;
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public Set<UserDTO> getUsersInvitedByMe(long id) {
        try {
            return userService.loadInvites(id);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public Set<UserDTO> getUsersInvitingMe(long id) {
        try {
            return userService.loadInvitesForMe(id);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public void inviteToFriend(long userId,long friendId){
        if (userId == friendId) throw new UsernameIsUsedException("This is you!");
        try {
            User user = userService.loadUserById(userId);
            User friend = userService.loadUserById(friendId);
            if (user.getFriends().contains(friend)
                    || user.getFriendsOf().contains(friend)){
                throw new UsernameIsUsedException("You are friends now!");
            } else if (user.getBlacklist().contains(friend) ||
                    friend.getBlacklist().contains(user)
            ) {
                throw new UsernameIsUsedException("User is in blacklist now!");
            } else {
                user.getUsersInvitedByMe().add(friend);
                userService.addToCache(Caches.INVITES,userId,user.getUsersInvitedByMe(),friendId);
                userService.addToCache(Caches.INVITES_OF,friendId,friend.getUsersInvitingMe(),userId);
                userRepository.save(user);
            }
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public void acceptToFriend(long userId, long friendId) {
        try {
            User user = userService.loadUserById(userId);
            User friend = userService.loadUserById(friendId);
            if (friend.getUsersInvitedByMe().contains(user)) {
                friend.getUsersInvitedByMe().remove(user);
                user.getFriends().add(friend);
                friend.getFriendsOf().add(user);
                userService.addToCache(Caches.FRIENDS,userId,user.getFriends(),friendId);
                userService.addToCache(Caches.FRIENDS_OF,friendId,friend.getFriendsOf(),userId);
                userService.removeFromCache(Caches.INVITES,friendId,friend.getUsersInvitedByMe(),userId);
                userService.removeFromCache(Caches.INVITES_OF,userId,user.getUsersInvitingMe(),friendId);
                userRepository.save(user);
                userRepository.save(friend);
            }
            else throw new InviteNotFoundException("Invite not found!");
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
    public void deny(long userId,long friendId){
        try {
            User user = userService.loadUserById(userId);
            User friend = userService.loadUserById(friendId);
            if (friend.getUsersInvitedByMe().contains(user)){
                friend.getUsersInvitedByMe().remove(user);
                userService.removeFromCache(Caches.INVITES,friendId,friend.getUsersInvitedByMe(),userId);
                userService.removeFromCache(Caches.INVITES_OF,userId,user.getUsersInvitingMe(),friendId);
                userRepository.save(friend);
            } else if (user.getUsersInvitedByMe().contains(friend)) {
                user.getUsersInvitedByMe().remove(friend);
                userService.removeFromCache(Caches.INVITES,userId,user.getUsersInvitedByMe(),friendId);
                userService.removeFromCache(Caches.INVITES_OF,friendId,friend.getUsersInvitedByMe(),userId);
                userRepository.save(user);
            } else throw new InviteNotFoundException("Invite not found!");
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
    public void remove(long userId,long friendId){
        try {
            User user = userService.loadUserById(userId);
            User friend = userService.loadUserById(friendId);
            if (user.getFriends().contains(friend)){
                user.getFriends().remove(friend);
                userService.removeFromCache(Caches.FRIENDS,userId,user.getFriends(),friendId);
                userService.removeFromCache(Caches.FRIENDS_OF,friendId,friend.getFriendsOf(),userId);
                userRepository.save(friend);
            } else if (user.getFriendsOf().contains(friend)) {
                friend.getFriends().remove(user);
                userService.removeFromCache(Caches.FRIENDS,friendId,friend.getFriends(),userId);
                userService.removeFromCache(Caches.FRIENDS_OF,userId,user.getFriendsOf(),friendId);
                userRepository.save(friend);
            } else throw new InviteNotFoundException(friend.getFirstname()+ " is not your friend!");
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

}
