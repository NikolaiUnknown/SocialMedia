package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.SettingRequest;
import com.media.socialmedia.DTO.UserDataResponse;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Services.ProfileService;
import com.media.socialmedia.Services.SettingService;
import com.media.socialmedia.Services.UserService;
import com.media.socialmedia.util.ProfileStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/p")
public class ProfileController {

    private final SettingService settingService;
    private final ProfileService profileService;
    private final UserService userService;
    @Autowired
    public ProfileController(SettingService settingService, ProfileService profileService, UserService userService)
    {   this.profileService = profileService;
        this.settingService = settingService;
        this.userService = userService;
    }
    @Value("${socialmedia.pictures.dir}")
    private  String pictureDirectory;
    @GetMapping("/user/{id}")
    public UserDataResponse getUser(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal JwtUserDetails userDetails){
        User user = userService.loadUserById(id);
        if (user.isPrivate()){
            if (userDetails == null){
                return profileService.changeCredentials(new UserDataResponse(user));
            }
            if (userDetails.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
                return new UserDataResponse(user);
            }
            Long authId = userDetails.getUserId();
            if (authId.equals(id)){
                return new UserDataResponse(user);
            }
            if (profileService.getStatus(authId, user.getId()).equals(ProfileStatus.FRIENDS)){
                return new UserDataResponse(user);
            }
            return profileService.changeCredentials(new UserDataResponse(user));
        }
        else return new UserDataResponse(user);

    }
    @GetMapping("/status")
    public String getStatus(@AuthenticationPrincipal JwtUserDetails userDetails,
                            @RequestParam long friendId){
        return profileService.getStatus(userDetails.getUserId(),friendId).name();
    }
    @GetMapping("/m")
    public long m(@AuthenticationPrincipal JwtUserDetails userDetails){
        return userDetails.getUserId();
    }
    @PostMapping(value = "/setting", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> setting(
            @RequestPart("settings") @Valid SettingRequest settingRequest,
            @RequestPart("profilePicture") MultipartFile profilePicture,
            @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        User user = userService.loadUserById(userDetails.getUserId());
        if (!profilePicture.isEmpty()) {
            String extension = profilePicture.getOriginalFilename().substring(profilePicture.getOriginalFilename()
                                                                   .lastIndexOf('.'));
            String filename = UUID.randomUUID() + extension;
            try {
                profilePicture.transferTo(new File(pictureDirectory + filename));
                settingRequest.setProfilePicture(filename);
            } catch (IOException e) {
                return new ResponseEntity<>("Ошибка при сохранении файла",HttpStatus.BAD_REQUEST);
            }
        }

        settingService.setting(user, settingRequest);
        return ResponseEntity.ok("success");
    }
}
