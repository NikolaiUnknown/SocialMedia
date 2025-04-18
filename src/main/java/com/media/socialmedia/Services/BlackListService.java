package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDataResponse;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UserNotCreatedException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlackListService {

    private final UserRepository repository;
    private final ProfileService profileService;
    @Autowired
    public BlackListService(UserRepository repository, FriendService friendService, ProfileService profileService) {
        this.repository = repository;
        this.profileService = profileService;
    }
    public boolean isInBlackList(long userId, long id){
        if (userId == id) return false;
        User user1 = repository.findById(userId).get();
        User user2 = repository.findById(id).get();
        return user1.getBlacklist().contains(user2);
    }

    public void addToBlackList(long userId, long id) {
        if (userId == id) throw new UsernameIsUsedException("This is you!");
        User user1 = repository.findById(userId).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        User user2 = repository.findById(id).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        if (isInBlackList(userId,id)) throw new UsernameIsUsedException("User is Already in blacklist");
        String status = profileService.getStatus(userId,id);
        switch (status){
            case "friends": throw new InviteNotFoundException("You are friends now!");
            case "invite": {
                user1.getInvites().remove(user2);
            }
            case "invited": {
                user1.getInvited().remove(user2);
            }
        }
        user1.getBlacklist().add(user2);
        repository.save(user1);
    }

    public void removeFromBlackList(long userId, long id) {
        if (userId == id) throw new UsernameIsUsedException("This is you!");
        User user1 = repository.findById(userId).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        User user2 = repository.findById(id).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        if (!isInBlackList(userId,id)) throw new UsernameIsUsedException("User is not in blacklist");
        user1.getBlacklist().remove(user2);
        repository.save(user1);
    }

    public Set<UserDataResponse> getBlacklist(long userId) {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotCreatedException("User not found!"));
        Set<User> blacklist = user.getBlacklist();
        return blacklist.stream().map(this::userToUserDataResponse).collect(Collectors.toSet());
    }
    private UserDataResponse userToUserDataResponse(User user){
        return new UserDataResponse(user);
    }
}
