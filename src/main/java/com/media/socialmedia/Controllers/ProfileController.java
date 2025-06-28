package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.ProfileService;
import com.media.socialmedia.Services.UserService;
import com.media.socialmedia.util.ProfileStatus;
import com.media.socialmedia.util.UserErrorResponse;
import com.media.socialmedia.util.UsernameIsUsedException;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
        try {
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
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }

    }
    @GetMapping("/status")
    public String getStatus(@AuthenticationPrincipal JwtUserDetails userDetails,
                            @RequestParam long id){
        try {
            return profileService.getStatus(userDetails.getUserId(),id).name();
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        } catch (UsernameIsUsedException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    @GetMapping("/me")
    public long me(@AuthenticationPrincipal JwtUserDetails userDetails){
        return userDetails.getUserId();
    }


    @PutMapping(value = "/setting")
    public ResponseEntity<?> setting(
            @RequestBody @Valid SettingRequestDTO settingRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        if(bindingResult.hasErrors()){
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors){
                errorMsg.append(error.getField())
                        .append(" - ").append(error.getDefaultMessage())
                        .append(";");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,errorMsg.toString());
        }
        profileService.setting(userDetails.getUserId(), settingRequest);
        return ResponseEntity.ok("success");
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(ResponseStatusException e){
        UserErrorResponse response = new UserErrorResponse(e.getReason());
        return new ResponseEntity<>(response,e.getStatusCode());
    }

}
