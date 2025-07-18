package com.media.socialmedia.Controllers;

import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.ProfileService;
import com.media.socialmedia.util.FileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Value("${socialmedia.pictures.dir}")
    private String pictureDirectory;
    private final ProfileService profileService;
    @Autowired
    public ImageController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<?> getImage(@PathVariable("filename") String filename){
        File resource = new File(pictureDirectory + filename);
        byte[] imageData = null;
        try {
            imageData = Files.readAllBytes(resource.toPath());
        } catch (IOException e) {
            return new ResponseEntity<>("Image not found",HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @PatchMapping(value="/profile", consumes= {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> setProfilePicture(@AuthenticationPrincipal JwtUserDetails userDetails,
                                               @RequestParam MultipartFile profilePicture){
        try {
            if (profilePicture != null && !profilePicture.isEmpty()) {
                profileService.setProfilePicture(userDetails.getUserId(),profilePicture);
            }
            else return new ResponseEntity<>("Image not found", HttpStatus.NOT_FOUND);
        } catch (FileException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("success", HttpStatus.OK);
    }
}
