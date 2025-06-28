package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.BlackListService;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/blacklist")
public class BlackListController {
    private final BlackListService blackListService;
    @Autowired
    public BlackListController(BlackListService blackListService) {
        this.blackListService = blackListService;
    }

    @GetMapping
    public ResponseEntity<Set<UserDTO>> allBlacklistedUsers(@AuthenticationPrincipal JwtUserDetails userDetails){
        return ResponseEntity.ok(blackListService.getBlacklist(userDetails.getUserId()));
    }


    @PostMapping()
    public ResponseEntity<?> addToBlackList(@AuthenticationPrincipal JwtUserDetails userDetails,
                                            @RequestParam long id){
        try {
            blackListService.addToBlackList(userDetails.getUserId(),id);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (UsernameIsUsedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("added!");
    }


    @DeleteMapping()
    public ResponseEntity<?> removeFromBlackList(@AuthenticationPrincipal JwtUserDetails userDetails,
                                                 @RequestParam long id){
        try {
            blackListService.removeFromBlackList(userDetails.getUserId(),id);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (UsernameIsUsedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("removed!");
    }
}
