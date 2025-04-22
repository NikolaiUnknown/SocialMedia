package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDataResponseDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UserNotCreatedException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final UserRepository userRepository;
    private final ProfileService profileService;
    @Autowired
    public FriendService(UserRepository userRepository, ProfileService profileService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    public Set<UserDataResponseDTO> getAllFriends(long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        Set<User> friends = new HashSet<>();
        friends.addAll(user.getFriends());
        friends.addAll(user.getFriendsOf());
        return friends.stream()
                .map(this::userToUserDataResponse)
                .collect(Collectors.toSet());
    }

    public Set<UserDataResponseDTO> getInvites(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        Set<User> invites = user.getInvites();
        return invites.stream()
                .map(this::userToUserDataResponse)
                .map(userData -> {
                    if (userData.isPrivate()){
                        return profileService.changeCredentials(userData);
                    }
                    return userData;
                        }).collect(Collectors.toSet());

    }
    public Set<UserDataResponseDTO> getInvited(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        Set<User> invited = user.getInvited();
        return invited.stream()
                .map(this::userToUserDataResponse)
                .map(userData -> {
                    if (userData.isPrivate()){
                        return profileService.changeCredentials(userData);
                    }
                    return userData;
                }).collect(Collectors.toSet());
    }

    public void inviteToFriend(long userId,long friendId){
        if (userId == friendId) throw new UsernameIsUsedException("This is you!");
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        User friend = userRepository.findById(friendId).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        if (!user.getFriends().contains(friend)
                && !user.getFriendsOf().contains(friend)
                && !user.getBlacklist().contains(friend)
                && !friend.getBlacklist().contains(user)){
            user.getInvites().add(friend);
            userRepository.save(user);
        }
        else throw new InviteNotFoundException("You are friends now!");
    }

    public void acceptToFriend(long userId, long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotCreatedException("User not found!"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotCreatedException("User not found!"));
        if (friend.getInvites().contains(user)) {
            friend.getInvites().remove(user);
            user.getFriends().add(friend);
            friend.getFriendsOf().add(user);
            userRepository.save(user);
            userRepository.save(friend);
        }
        else throw new InviteNotFoundException("Invite not found!");
    }
    public void deny(long userId,long friendId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotCreatedException("User not found!"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotCreatedException("User not found!"));
        if (friend.getInvites().contains(user)){
            friend.getInvites().remove(user);
            userRepository.save(friend);
        } else if (user.getInvites().contains(friend)) {
            user.getInvites().remove(friend);
            userRepository.save(user);
        } else throw new InviteNotFoundException("Invite not found!");
    }
    public void remove(long userId,long friendId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotCreatedException("User not found!"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotCreatedException("User not found!"));

        if (user.getFriends().contains(friend)){
            user.getFriends().remove(friend);
            userRepository.save(friend);
        } else if (user.getFriendsOf().contains(friend)) {
            friend.getFriends().remove(user);
            userRepository.save(friend);
        } else throw new InviteNotFoundException(friend.getFirstname()+ " is not your friend!");
    }
    private UserDataResponseDTO userToUserDataResponse(User user){
        return new UserDataResponseDTO(user);
    }


}
