package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.ProfileStatus;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlackListService {

    private final UserRepository repository;
    private final ProfileService profileService;
    private final UserService userService;
    private final ModelMapper mapper;

    @Autowired
    public BlackListService(UserRepository repository, FriendService friendService, ProfileService profileService, UserService userService, ModelMapper mapper) {
        this.repository = repository;
        this.profileService = profileService;
        this.userService = userService;
        this.mapper = mapper;
    }
    public boolean isInBlackList(long userId, long id){
        if (userId == id) return false;
        try {
            User user1 = userService.loadUserById(userId);
            User user2 = userService.loadUserById(id);
            return user1.getBlacklist().contains(user2);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public void addToBlackList(long userId, long id) {
        if (userId == id) throw new UsernameIsUsedException("This is you!");
        try {
            User user1 = userService.loadUserById(userId);
            User user2 = userService.loadUserById(id);
            if (isInBlackList(userId,id)) throw new UsernameIsUsedException("User is Already in blacklist");
            ProfileStatus status = profileService.getStatus(userId,id);
            switch (status){
                case ProfileStatus.FRIENDS: throw new InviteNotFoundException("You are friends now!");
                case ProfileStatus.INVITE: {
                    user1.getFriendsInvitedByMe().remove(user2);
                }
                case ProfileStatus.INVITED: {
                    user1.getFriendsInvitingMe().remove(user2);
                }
            }
            user1.getBlacklist().add(user2);
            repository.save(user1);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public void removeFromBlackList(long userId, long id) {
        if (userId == id) throw new UsernameIsUsedException("This is you!");
        try {
            User user1 = userService.loadUserById(userId);
            User user2 = userService.loadUserById(id);
            if (!isInBlackList(userId,id)) throw new UsernameIsUsedException("User is not in blacklist");
            user1.getBlacklist().remove(user2);
            repository.save(user1);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public Set<UserDTO> getBlacklist(long userId) {
        try {
            User user = userService.loadUserById(userId);
            Set<User> blacklist = user.getBlacklist();
            return blacklist.stream().map(this::userToUserDataResponse).collect(Collectors.toSet());
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
    private UserDTO userToUserDataResponse(User user){
        return mapper.map(user, UserDTO.class);
    }
}
