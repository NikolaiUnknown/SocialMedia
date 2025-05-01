package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.ProfileStatus;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlackListService {

    private final UserRepository repository;
    private final ProfileService profileService;
    private final UserService userService;
    private final RedisTemplate<String, String> template;

    @Autowired
    public BlackListService(UserRepository repository, ProfileService profileService, UserService userService, RedisTemplate<String, String> template) {
        this.repository = repository;
        this.profileService = profileService;
        this.userService = userService;
        this.template = template;
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
            System.out.println(status);
            switch (status){
                case ProfileStatus.FRIENDS: throw new InviteNotFoundException("You are friends now!");
                case ProfileStatus.INVITE: {
                    user1.getUsersInvitedByMe().remove(user2);
                    repository.save(user1);
                }
                case ProfileStatus.INVITED: {
                    user2.getUsersInvitedByMe().remove(user1);
                    repository.save(user2);
                }
            }
            user1.getBlacklist().add(user2);
            template.delete(new ArrayList<>(Arrays.asList(
                        "blacklist::%d".formatted(userId),
                        "invites::%d".formatted(userId),
                        "invitesOf::%d".formatted(userId),
                        "invites::%d".formatted(id),
                        "invitesOf::%d".formatted(id))));
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
            template.delete("blacklist::%d".formatted(userId));
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
