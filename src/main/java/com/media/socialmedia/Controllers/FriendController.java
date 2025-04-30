package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Set<UserDTO> getAll(@AuthenticationPrincipal JwtUserDetails userDetails){
        return friendService.getAllFriends(userDetails.getUserId());
    }
    @GetMapping("/invites")
    public Set<UserDTO> getInvites(@AuthenticationPrincipal JwtUserDetails userDetails){
        return friendService.getInvites(userDetails.getUserId());
    }
    @GetMapping("/invited")
    public Set<UserDTO> getInvited(@AuthenticationPrincipal JwtUserDetails userDetails){
        return friendService.getInvited(userDetails.getUserId());
    }

    @PostMapping("/invite")
    public ResponseEntity<?> invite(@AuthenticationPrincipal JwtUserDetails userDetails,
                                    @RequestParam long id){
        try {
            friendService.inviteToFriend(userDetails.getUserId(),id);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("invited!");
    }

    @PostMapping("/accept")
    public ResponseEntity<?> accept(@AuthenticationPrincipal JwtUserDetails userDetails,
                                    @RequestParam long id){
        try {
            friendService.acceptToFriend(userDetails.getUserId(),id);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("accepted!");
    }
    @PostMapping("/deny")
    public ResponseEntity<?> deny(@AuthenticationPrincipal JwtUserDetails userDetails,
                                  @RequestParam long id){
        try {
            friendService.deny(userDetails.getUserId(),id);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("deny!");
    }
    @PostMapping("/remove")
    public ResponseEntity<?> remove(@AuthenticationPrincipal JwtUserDetails userDetails,
                                    @RequestParam long id){
        try {
            friendService.remove(userDetails.getUserId(),id);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("removed!");
    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(RuntimeException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
