package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class FriendService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final CacheUpdateService cacheUpdateService;
    @Autowired
    public FriendService(UserRepository userRepository, UserService userService, CacheUpdateService cacheUpdateService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.cacheUpdateService = cacheUpdateService;
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
            if (!user.getFriends().contains(friend)
                    && !user.getFriendsOf().contains(friend)
                    && !user.getBlacklist().contains(friend)
                    && !friend.getBlacklist().contains(user)){
                user.getUsersInvitedByMe().add(friend);
                cacheUpdateService.addToInvites(userId,user.getUsersInvitedByMe(),friendId);
                cacheUpdateService.addToInvitesOf(friendId,friend.getUsersInvitingMe(),userId);
                userRepository.save(user);
            }
            else throw new InviteNotFoundException("You are friends now!");
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
                cacheUpdateService.addToFriends(userId,user.getFriends(),friendId);
                cacheUpdateService.addToFriendsOf(friendId,friend.getFriendsOf(),userId);
                cacheUpdateService.deleteFromInvites(friendId,friend.getUsersInvitedByMe(),userId);
                cacheUpdateService.deleteFromInvitesOf(userId,user.getUsersInvitingMe(),friendId);
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
                cacheUpdateService.deleteFromInvites(friendId,friend.getUsersInvitedByMe(),userId);
                cacheUpdateService.deleteFromInvitesOf(userId,user.getUsersInvitingMe(),friendId);
                userRepository.save(friend);
            } else if (user.getUsersInvitedByMe().contains(friend)) {
                user.getUsersInvitedByMe().remove(friend);
                cacheUpdateService.deleteFromInvites(userId,user.getUsersInvitedByMe(),friendId);
                cacheUpdateService.deleteFromInvitesOf(friendId,friend.getUsersInvitedByMe(),userId);
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
                cacheUpdateService.deleteFromFriends(userId,user.getFriends(),friendId);
                cacheUpdateService.deleteFromFriendsOf(friendId,friend.getFriendsOf(),userId);
                userRepository.save(friend);
            } else if (user.getFriendsOf().contains(friend)) {
                friend.getFriends().remove(user);
                cacheUpdateService.deleteFromFriends(friendId,friend.getFriends(),userId);
                cacheUpdateService.deleteFromFriendsOf(userId,user.getFriendsOf(),friendId);
                userRepository.save(friend);
            } else throw new InviteNotFoundException(friend.getFirstname()+ " is not your friend!");
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

}
