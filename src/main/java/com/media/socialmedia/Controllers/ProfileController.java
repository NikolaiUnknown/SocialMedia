package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Services.ProfileService;
import com.media.socialmedia.Services.SettingService;
import com.media.socialmedia.Services.UserService;
import com.media.socialmedia.util.ProfileStatus;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper mapper;
    @Autowired
    public ProfileController(SettingService settingService, ProfileService profileService, UserService userService, ModelMapper mapper)
    {   this.profileService = profileService;
        this.settingService = settingService;
        this.userService = userService;
        this.mapper = mapper;
    }
    @Value("${socialmedia.pictures.dir}")
    private  String pictureDirectory;
    @GetMapping("/users/{id}")
    public UserDTO getUser(@PathVariable("id") Long id,
                           @AuthenticationPrincipal JwtUserDetails userDetails){
        UserDTO user = userService.loadUserDtoById(id);
        if (user.isPrivate()){
            if (userDetails == null){
                return profileService.changeCredentials(mapper.map(user, UserDTO.class));
            }
            if (userDetails.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){
                return user;
            }
            Long authId = userDetails.getUserId();
            if (authId.equals(id)){
                return user;
            }
            if (profileService.getStatus(authId, user.getId()).equals(ProfileStatus.FRIENDS)){
                return user;
            }
            return profileService.changeCredentials(user);
        }
        else return user;

    }
    @GetMapping("/status")
    public String getStatus(@AuthenticationPrincipal JwtUserDetails userDetails,
                            @RequestParam long id){
        return profileService.getStatus(userDetails.getUserId(),id).name();
    }
    @GetMapping("/m")
    public long m(@AuthenticationPrincipal JwtUserDetails userDetails){
        return userDetails.getUserId();
    }
    @PostMapping(value = "/setting", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> setting(
            @RequestPart("settings") @Valid SettingRequestDTO settingRequest,
            @RequestPart("profilePicture") MultipartFile profilePicture,
            @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        if (!profilePicture.isEmpty()) {
            String extension = profilePicture.getOriginalFilename().substring(profilePicture.getOriginalFilename()
                                                                   .lastIndexOf('.'));
            String filename = UUID.randomUUID() + extension;
            try {
                profilePicture.transferTo(new File(pictureDirectory + filename));
                settingRequest.setProfilePicture(filename);
            } catch (IOException e) {
                return new ResponseEntity<>("Error saving file",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        settingService.setting(userDetails.getUserId(), settingRequest);
        return ResponseEntity.ok("success");
    }

}
