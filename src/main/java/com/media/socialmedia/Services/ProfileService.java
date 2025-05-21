package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.Repository.search.UserSearchRepository;
import com.media.socialmedia.util.Caches;
import com.media.socialmedia.util.FileException;
import com.media.socialmedia.util.ProfileStatus;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfileService {
    @Value("${socialmedia.pictures.dir}")
    private String pictureDirectory;

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;
    private final ModelMapper mapper;

    @Autowired
    public ProfileService(UserService userService, UserRepository userRepository, UserSearchRepository userSearchRepository, ModelMapper mapper) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
        this.mapper = mapper;
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

    public void setProfilePicture(Long userId, MultipartFile profilePicture) {
        String extension = profilePicture.getOriginalFilename().substring(profilePicture.getOriginalFilename()
                .lastIndexOf('.'));
        String filename = UUID.randomUUID() + extension;
        try {
            profilePicture.transferTo(new File(pictureDirectory + filename));
        } catch (IOException e) {
            throw new FileException("Error saving file");
        }
        User user = userService.loadUserById(userId);
        if (!user.getProfilePicture().equals("default-avatar.png")){
            File file = new File(pictureDirectory + user.getProfilePicture());
            file.delete();
        }
        user.setProfilePicture(filename);
        UserSearchDocument document = userSearchRepository.searchById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User document not found!"));
        document.setProfilePicture(filename);
        userService.evictFromCache(Caches.USERS, userId);
        userRepository.save(user);
        userSearchRepository.save(document);
    }

    public void setting(Long userId, SettingRequestDTO request){
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setDateOfBirthday(request.getDateOfBirthday());
        user.setCountry(request.getCountry());
        if (user.getProfilePicture() == null){
            user.setProfilePicture("default-avatar.png");
        }
        user.setPrivate(request.getIsPrivate());
        user.setValid(true);
        UserSearchDocument document = mapper.map(user, UserSearchDocument.class);
        document.setFullName(String.format("%s %s",user.getFirstname(), user.getLastname()));
        userService.evictFromCache(Caches.USERS,userId);
        userRepository.save(user);
        userSearchRepository.save(document);
    }
}
