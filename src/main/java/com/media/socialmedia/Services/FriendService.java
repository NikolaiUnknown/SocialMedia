package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class FriendService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final RedisTemplate<String, String> template;

    @Autowired
    public FriendService(UserRepository userRepository, UserService userService, RedisTemplate<String, String> template) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.template = template;
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
                template.delete(Arrays.asList("invites::%d".formatted(userId)
                                            , "invitesOf::%d".formatted(friendId)));
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
                template.delete(Arrays.asList("friends::%d".formatted(userId)
                                            , "friendsOf::%d".formatted(userId)
                                            , "friends::%d".formatted(friendId)
                                            , "friendsOf::%d".formatted(friendId)
                                            , "invites::%d".formatted(friendId)
                                            , "invitesOf::%d".formatted(userId)));
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
                template.delete("invites::%d".formatted(friendId));
                userRepository.save(friend);
            } else if (user.getUsersInvitedByMe().contains(friend)) {
                user.getUsersInvitedByMe().remove(friend);
                template.delete("invites::%d".formatted(userId));
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
                template.delete("friends::%d".formatted(userId));
                template.delete("friendsOf::%d".formatted(friendId));
                userRepository.save(friend);
            } else if (user.getFriendsOf().contains(friend)) {
                friend.getFriends().remove(user);
                template.delete("friends::%d".formatted(friendId));
                template.delete("friendsOf::%d".formatted(userId));
                userRepository.save(friend);
            } else throw new InviteNotFoundException(friend.getFirstname()+ " is not your friend!");
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

}
