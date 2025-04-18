package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.UserDataResponse;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Security.UserDetailsImpl;
import com.media.socialmedia.Services.BlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/get")
    public Set<UserDataResponse> allBlacklistedUsers(@AuthenticationPrincipal UserDetailsImpl userDetails){

        User user = userDetails.getUser();
        return blackListService.getBlacklist(user.getId());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToBlackList(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestParam long id){
        User user = userDetails.getUser();
        try {
            blackListService.addToBlackList(user.getId(),id);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("added!");
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromBlackList(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @RequestParam long id){
        User user = userDetails.getUser();
        try {
            blackListService.removeFromBlackList(user.getId(),id);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("removed!");
    }
}
