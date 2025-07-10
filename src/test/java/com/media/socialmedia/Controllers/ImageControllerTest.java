package com.media.socialmedia.Controllers;

import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.ProfileService;
import com.media.socialmedia.util.FileException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
@Import({SecurityConfig.class, JwtCore.class})
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProfileService profileService;

    private void setPrincipal(long id){
        JwtUserDetails userDetails = new JwtUserDetails(id,false,"");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getDefaultImage() throws Exception {
        mockMvc.perform(get("/images/{filename}","default-avatar.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void getNonExistImage() throws Exception {
        mockMvc.perform(get("/images/{filename}","non-exist-image.png"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Image not found"));
    }

    @Test
    void setProfilePictureWithCorrectData() throws Exception {
        setPrincipal(0L);
        MockMultipartFile file = new MockMultipartFile(
                "profilePicture","image.png",MediaType.IMAGE_PNG_VALUE,"Some picture data".getBytes()
        );
        mockMvc.perform(multipart(HttpMethod.PATCH,"/images/profile")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("success"));
    }

    @Test
    void setProfilePictureWithServerError() throws Exception {
        setPrincipal(0L);
        MockMultipartFile file = new MockMultipartFile(
                "profilePicture","image.png",MediaType.IMAGE_PNG_VALUE,"Some picture data".getBytes()
        );
        doThrow(FileException.class).when(profileService).setProfilePicture(0L,file);
        mockMvc.perform(multipart(HttpMethod.PATCH,"/images/profile")
                        .file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void setProfilePictureWithEmptyFile() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(multipart(HttpMethod.PATCH,"/images/profile")
                        .file(new MockMultipartFile("profilePicture","file.png","", new byte[0])))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Image not found"));
    }


    @Test
    void setProfilePictureWithoutAuthentication() throws Exception {
        mockMvc.perform(patch("/images/profile"))
                .andExpect(status().isUnauthorized());
    }
}