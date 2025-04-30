package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.util.ProfileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class ProfileService {
    private final UserService userService;
    @Autowired
    public ProfileService(UserService userService) {
        this.userService = userService;
    }

    public ProfileStatus getStatus(long userId,long friendId){
        try {
            UserDTO user = userService.loadUserDtoById(userId);
            UserDTO friend = userService.loadUserDtoById(friendId);
            Set<UserDTO> userFriends = userService.loadUserFriends(userId);
            Set<UserDTO> friendFriends = userService.loadUserFriends(friendId);
            if (friend.isBlocked()){
                return ProfileStatus.BLOCKED;
            }
            else if (userService.loadUserBlacklist(friendId).contains(user)){
                return ProfileStatus.BLACKLISTED;
            }  else if (userService.loadUserBlacklist(userId).contains(friend)){
                return ProfileStatus.BLACKLIST;
            }else if (userFriends.contains(friend) || friendFriends.contains(user)){
                return ProfileStatus.FRIENDS;
            } else if (userService.loadInvites(userId).contains(friend)) {
                return ProfileStatus.INVITE;
            } else if (userService.loadInvites(friendId).contains(user)) {
                return ProfileStatus.INVITED;
            }
            return ProfileStatus.UNKNOWN;
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    public UserDTO changeCredentials(UserDTO response){
        response.setEmail(null);
        response.setCountry(null);
        response.setDateOfBirthday(null);
        return response;
    }

}
