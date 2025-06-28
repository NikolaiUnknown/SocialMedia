package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.FriendService;
import com.media.socialmedia.util.InviteNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;
    @Autowired
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping()
    public ResponseEntity<Set<UserDTO>> getAll(@AuthenticationPrincipal JwtUserDetails userDetails){
        return ResponseEntity.ok(friendService.getAllFriends(userDetails.getUserId()));
    }


    @GetMapping("/invites")
    public ResponseEntity<Set<UserDTO>> getInvites(@AuthenticationPrincipal JwtUserDetails userDetails){
        return ResponseEntity.ok(friendService.getUsersInvitedByMe(userDetails.getUserId()));
    }


    @GetMapping("/invited")
    public ResponseEntity<Set<UserDTO>> getInvited(@AuthenticationPrincipal JwtUserDetails userDetails){
        return ResponseEntity.ok(friendService.getUsersInvitingMe(userDetails.getUserId()));
    }

    @PostMapping("/invite")
    public ResponseEntity<?> invite(@AuthenticationPrincipal JwtUserDetails userDetails,
                                    @RequestParam long id){
        try {
            friendService.inviteToFriend(userDetails.getUserId(),id);
        } catch (UsernameNotFoundException | InviteNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        } catch (UsernameIsUsedException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("invited!");
    }

    @PatchMapping("/accept")
    public ResponseEntity<?> accept(@AuthenticationPrincipal JwtUserDetails userDetails,
                                    @RequestParam long id){
        try {
            friendService.acceptToFriend(userDetails.getUserId(),id);
        }catch (InviteNotFoundException|UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("accepted!");
    }

    @PatchMapping("/deny")
    public ResponseEntity<?> deny(@AuthenticationPrincipal JwtUserDetails userDetails,
                                  @RequestParam long id){
        try {
            friendService.deny(userDetails.getUserId(),id);
        }catch (InviteNotFoundException|UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("denied!");
    }


    @DeleteMapping()
    public ResponseEntity<?> remove(@AuthenticationPrincipal JwtUserDetails userDetails,
                                    @RequestParam long id){
        try {
            friendService.remove(userDetails.getUserId(),id);
        }catch (InviteNotFoundException|UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("removed!");
    }
}
