package com.media.socialmedia.Controllers;

import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.AdminService;
import com.media.socialmedia.util.PostNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/check")
    public Boolean accessGranted() {
        return true;
    }

    @PatchMapping("/assign/{id}")
    public ResponseEntity<?> assign(@AuthenticationPrincipal JwtUserDetails userDetails
                                  , @PathVariable("id") Long id){
        if (userDetails.getUserId().equals(id)){
            return new ResponseEntity<>("This is you!",HttpStatus.BAD_REQUEST);
        }
        try {
            adminService.assign(id);
        } catch (UsernameIsUsedException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(String.format("User %d added to admins",id));
    }


    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id){
        try {
            adminService.deletePost(id);
        }
        catch (PostNotFoundException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(String.format("Post %d is deleted",id));
    }

    @PatchMapping("/users/ban/{id}")
    public ResponseEntity<?> ban(@PathVariable("id") Long id){
        try {
            adminService.ban(id);
        }catch (UsernameIsUsedException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }catch (UsernameNotFoundException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(String.format("User %d is banned",id));
    }


    @PatchMapping("/users/unban/{id}")
    public ResponseEntity<?> unban(@PathVariable("id") Long id){
        try {
            adminService.unban(id);
        } catch (UsernameIsUsedException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }catch (UsernameNotFoundException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(String.format("User %d is unbanned",id));
    }

}

