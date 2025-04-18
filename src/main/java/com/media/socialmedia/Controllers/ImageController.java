package com.media.socialmedia.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/image")
public class ImageController {
    @Value("${socialmedia.pictures.dir}")
    private String pictureDirectory;

    @GetMapping("/{filename}")
    public ResponseEntity<?> getProfilPicutre(@PathVariable("filename") String filename){
        File resource = new File(pictureDirectory + filename);
        byte[] imageData = null;
        try {
            imageData = Files.readAllBytes(resource.toPath());
        } catch (IOException e) {
            return new ResponseEntity<>("Image not found!",HttpStatus.BAD_REQUEST);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

}
