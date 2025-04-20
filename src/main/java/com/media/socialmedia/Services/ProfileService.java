package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDataResponse;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.ProfileStatus;
import com.media.socialmedia.util.UserNotCreatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    @Autowired
    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileStatus getStatus(long userId,long friendId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotCreatedException("User not found!"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotCreatedException("User not found!"));
        if (friend.getBlacklist().contains(user)){
            return ProfileStatus.BLACKLIST;
        }  else if (user.getBlacklist().contains(friend)){
            return ProfileStatus.BLACKLISTED;
        }else if (user.getFriends().contains(friend) || user.getFriendsOf().contains(friend)){
            return ProfileStatus.FRIENDS;
        } else if (user.getInvites().contains(friend)) {
            return ProfileStatus.INVITE;
        } else if (user.getInvited().contains(friend)) {
            return ProfileStatus.INVITED;
        }
        return ProfileStatus.UNKNOWN;
    }

    public UserDataResponse changeCredentials(UserDataResponse response){
        response.setEmail(null);
        response.setCountry(null);
        response.setDateOfBirthday(null);
        return response;
    }

}
