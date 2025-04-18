package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.UserDataResponse;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Security.UserDetailsImpl;
import com.media.socialmedia.Services.FriendService;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UserNotCreatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/friend")
public class FriendController {
    private final FriendService friendService;
    @Autowired
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("/all")
    public Set<UserDataResponse> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails){
        User user = userDetails.getUser();
        return friendService.getAllFriends(user.getId());
    }
    @GetMapping("/invites")
    public Set<UserDataResponse> getInvites(@AuthenticationPrincipal UserDetailsImpl userDetails){
        User user = userDetails.getUser();
        return friendService.getInvites(user.getId());
    }
    @GetMapping("/invited")
    public Set<UserDataResponse> getInvited(@AuthenticationPrincipal UserDetailsImpl userDetails){
        User user = userDetails.getUser();
        return friendService.getInvited(user.getId());
    }

    @PostMapping("/invite")
    public ResponseEntity<?> invite(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @RequestParam long friendId){
        User user = userDetails.getUser();
        try {
            friendService.inviteToFriend(user.getId(),friendId);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("invited!");
    }

    @PostMapping("/accept")
    public ResponseEntity<?> accept(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @RequestParam long friendId){
        User user = userDetails.getUser();
        try {
            friendService.acceptToFriend(user.getId(),friendId);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("accepted!");
    }
    @PostMapping("/deny")
    public ResponseEntity<?> deny(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @RequestParam long friendId){
        User user = userDetails.getUser();
        try {
            friendService.deny(user.getId(),friendId);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("deny!");
    }
    @PostMapping("/remove")
    public ResponseEntity<?> remove(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam long friendId){
        User user = userDetails.getUser();
        try {
            friendService.remove(user.getId(),friendId);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("removed!");
    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(InviteNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler
    public ResponseEntity<String> handleException(UserNotCreatedException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
