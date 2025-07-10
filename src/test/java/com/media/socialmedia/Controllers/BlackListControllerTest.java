package com.media.socialmedia.Controllers;

import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.BlackListService;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlackListController.class)
@Import({SecurityConfig.class, JwtCore.class})
class BlackListControllerTest {

    @MockitoBean
    private BlackListService blackListService;

    @Autowired
    private MockMvc mockMvc;

    private void setPrincipal(long id){
        JwtUserDetails userDetails = new JwtUserDetails(id,false,"");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    @Test
    void allBlacklistedUsersWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(get("/blacklist"))
                .andExpect(status().isOk());
    }
    @Test
    void allBlacklistedUsersWithoutAuthentication() throws Exception {
           mockMvc.perform(get("/blacklist")).andExpect(status().isUnauthorized());
    }

    @Test
    void addToBlackListWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(post("/blacklist").param("id","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("added!"));
    }

    @Test
    void addToBlackListAlreadyBlacklistedUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameIsUsedException("User is Already in blacklist")).when(blackListService).addToBlackList(0L,1L);
        mockMvc.perform(post("/blacklist").param("id","1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User is Already in blacklist"));
    }

    @Test
    void addToBlackListFriend() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameIsUsedException("You are friends now!")).when(blackListService).addToBlackList(0L,1L);
        mockMvc.perform(post("/blacklist").param("id","1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("You are friends now!"));
    }

    @Test
    void addToBlackListYourSelf() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameIsUsedException("This is you!")).when(blackListService).addToBlackList(0L,0L);
        mockMvc.perform(post("/blacklist").param("id","0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("This is you!"));
    }

    @Test
    void addToBlackListNonExistUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameNotFoundException("User not found!")).when(blackListService).addToBlackList(0L,1L);
        mockMvc.perform(post("/blacklist").param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }

    @Test
    void removeFromBlackListWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(delete("/blacklist").param("id","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("removed!"));
    }

    @Test
    void removeFromBlackListYourself() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameIsUsedException("This is you!")).when(blackListService).removeFromBlackList(0L,0L);
        mockMvc.perform(delete("/blacklist").param("id","0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("This is you!"));
    }

    @Test
    void removeFromBlackListNonBlacklistedUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameIsUsedException("User is not in blacklist")).when(blackListService).removeFromBlackList(0L,1L);
        mockMvc.perform(delete("/blacklist").param("id","1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User is not in blacklist"));
    }

    @Test
    void removeFromBlackListNonExistUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameNotFoundException("User not found!")).when(blackListService).removeFromBlackList(0L,1L);
        mockMvc.perform(delete("/blacklist").param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }
}