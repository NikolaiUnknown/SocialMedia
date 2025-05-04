package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.Caches;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.ProfileStatus;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class BlackListService {

    private final UserRepository repository;
    private final ProfileService profileService;
    private final UserService userService;

    @Autowired
    public BlackListService(UserRepository repository, ProfileService profileService, UserService userService) {
        this.repository = repository;
        this.profileService = profileService;
        this.userService = userService;
    }
    public boolean isInBlackList(long userId, long id){
        if (userId == id) return false;
        try {
            UserDTO user = userService.loadUserDtoById(id);
            return userService.loadUserBlacklist(userId).contains(user);
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
                    user1.getUsersInvitedByMe().remove(user2);
                    userService.removeFromCache(Caches.INVITES,userId,user1.getUsersInvitedByMe(),id);
                    userService.removeFromCache(Caches.INVITES_OF,id,user2.getUsersInvitingMe(),userId);
                    repository.save(user1);
                }
                case ProfileStatus.INVITED: {
                    user2.getUsersInvitedByMe().remove(user1);
                    userService.removeFromCache(Caches.INVITES,id,user2.getUsersInvitedByMe(),userId);
                    userService.removeFromCache(Caches.INVITES_OF,userId,user1.getUsersInvitingMe(),id);
                    repository.save(user2);
                }
            }
            user1.getBlacklist().add(user2);
            userService.addToCache(Caches.BLACKLIST,userId,user1.getBlacklist(),id);
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
            userService.removeFromCache(Caches.BLACKLIST,userId,user1.getBlacklist(),id);
            repository.save(user1);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public Set<UserDTO> getBlacklist(long userId) {
        try {
            return userService.loadUserBlacklist(userId);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
}
