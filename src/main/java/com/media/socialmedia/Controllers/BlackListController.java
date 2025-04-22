package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.UserDataResponseDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.BlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Set<UserDataResponseDTO> allBlacklistedUsers(@AuthenticationPrincipal JwtUserDetails userDetails){

        return blackListService.getBlacklist(userDetails.getUserId());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToBlackList(@AuthenticationPrincipal JwtUserDetails userDetails,
                                            @RequestParam long id){
        try {
            blackListService.addToBlackList(userDetails.getUserId(),id);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("added!");
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromBlackList(@AuthenticationPrincipal JwtUserDetails userDetails,
                                                 @RequestParam long id){
        try {
            blackListService.removeFromBlackList(userDetails.getUserId(),id);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("removed!");
    }
}
