package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.ProfileService;
import com.media.socialmedia.Services.UserService;
import com.media.socialmedia.util.ProfileStatus;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/p")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final ModelMapper mapper;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService, ModelMapper mapper) {
        this.profileService = profileService;
        this.userService = userService;
        this.mapper = mapper;
    }
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

    @PutMapping(value = "/setting")
    public ResponseEntity<?> setting(
            @RequestBody @Valid SettingRequestDTO settingRequest,
            @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        profileService.setting(userDetails.getUserId(), settingRequest);
        return ResponseEntity.ok("success");
    }

}
