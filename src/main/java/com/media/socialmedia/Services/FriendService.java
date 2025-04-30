package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final UserService userService;
    private final ModelMapper mapper;
    @Autowired
    public FriendService(UserRepository userRepository, ProfileService profileService, UserService userService, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.userService = userService;
        this.mapper = mapper;
    }

    public Set<UserDTO> getAllFriends(long userId){
        try {
            User user = userService.loadUserById(userId);
            Set<User> friends = new HashSet<>();
            friends.addAll(user.getFriends());
            friends.addAll(user.getFriendsOf());
            return friends.stream()
                    .map(this::userToUserDataResponse)
                    .collect(Collectors.toSet());
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public Set<UserDTO> getInvites(long id) {
        try {
            User user = userService.loadUserById(id);
            Set<User> invites = user.getInvites();
            return invites.stream()
                    .map(this::userToUserDataResponse)
                    .map(userData -> {
                        if (userData.isPrivate()){
                            return profileService.changeCredentials(userData);
                        }
                        return userData;
                    }).collect(Collectors.toSet());
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
    public Set<UserDTO> getInvited(long id) {
        try {
            User user = userService.loadUserById(id);
            Set<User> invited = user.getInvited();
            return invited.stream()
                    .map(this::userToUserDataResponse)
                    .map(userData -> {
                        if (userData.isPrivate()){
                            return profileService.changeCredentials(userData);
                        }
                        return userData;
                    }).collect(Collectors.toSet());
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
                user.getInvites().add(friend);
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
            if (friend.getInvites().contains(user)) {
                friend.getInvites().remove(user);
                user.getFriends().add(friend);
                friend.getFriendsOf().add(user);
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
            if (friend.getInvites().contains(user)){
                friend.getInvites().remove(user);
                userRepository.save(friend);
            } else if (user.getInvites().contains(friend)) {
                user.getInvites().remove(friend);
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
                userRepository.save(friend);
            } else if (user.getFriendsOf().contains(friend)) {
                friend.getFriends().remove(user);
                userRepository.save(friend);
            } else throw new InviteNotFoundException(friend.getFirstname()+ " is not your friend!");
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
    private UserDTO userToUserDataResponse(User user){
        return mapper.map(user, UserDTO.class);
    }


}
