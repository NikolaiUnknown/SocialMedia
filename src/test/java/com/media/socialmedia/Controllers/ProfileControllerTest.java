package com.media.socialmedia.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.ProfileService;
import com.media.socialmedia.util.ProfileStatus;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, JwtCore.class})
class ProfileControllerTest {

    @MockitoBean
    private ProfileService profileService;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void setPrincipal(long id){
        JwtUserDetails userDetails = new JwtUserDetails(id,false,"");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getMeWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(get("/p/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0L));
    }

    @Test
    void getMeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/p/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserWithCorrectData() throws Exception {
        UserDTO user = new UserDTO();
        when(profileService.getUser(null,1L)).thenReturn(user);
        mockMvc.perform(get("/p/users/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    void getNonExistUser() throws Exception {
        when(profileService.getUser(null,1L)).thenThrow(new UsernameNotFoundException("User not found!"));
        mockMvc.perform(get("/p/users/{id}",1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStatusWithCorrectData() throws Exception {
        setPrincipal(0L);
        when(profileService.getStatus(0L,1L))
                .thenReturn(ProfileStatus.UNKNOWN);
        mockMvc.perform(get("/p/status")
                        .param("id","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("UNKNOWN"));
    }

    @Test
    void getStatusWithYourself() throws Exception {
        setPrincipal(0L);
        when(profileService.getStatus(0L,0L))
                .thenThrow(new UsernameIsUsedException("This is you!"));
        mockMvc.perform(get("/p/status")
                        .param("id","0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This is you!"));
    }

    @Test
    void getStatusWithNonExistUser() throws Exception {
        setPrincipal(0L);
        when(profileService.getStatus(0L,1L))
                .thenThrow(new UsernameNotFoundException("User not found!"));
        mockMvc.perform(get("/p/status")
                        .param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found!"));
    }



    @Test
    void settingWithCorrectData() throws Exception{
        setPrincipal(0L);
        SettingRequestDTO request = new SettingRequestDTO(
                "firstname","lastname","",new Date(),false
        );
        mockMvc.perform(put("/p/setting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("success"));
    }

    @Test
    void settingWithWrongData() throws Exception {
        setPrincipal(0L);
        SettingRequestDTO request = new SettingRequestDTO(
                "firstname","lastname","",null,false
        );
        mockMvc.perform(put("/p/setting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dateOfBirth - date is null!;"));
    }
    @Test
    void settingWithEmptyData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(put("/p/setting"))
                .andExpect(status().isBadRequest());
    }
}