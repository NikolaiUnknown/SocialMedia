package com.media.socialmedia.Controllers;

import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.AdminService;
import com.media.socialmedia.util.PostNotFoundException;
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

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class,JwtCore.class})
class AdminControllerTest {

    @MockitoBean
    private AdminService adminService;

    @Autowired
    private MockMvc mockMvc;

    private void setPrincipal(long id, boolean isAdmin){
        JwtUserDetails userDetails = new JwtUserDetails(id,isAdmin,"");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void accessGrantedWithoutAdminRole() throws Exception {
        setPrincipal(0L,false);
        mockMvc.perform(get("/admin/check"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessGrantedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/admin/check"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void accessGrantedWithAdminRole() throws Exception {
        setPrincipal(0L,true);
        mockMvc.perform(get("/admin/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void assignWithCorrectData() throws Exception {
        setPrincipal(0L,true);
        mockMvc.perform(patch("/admin/assign/{id}",1L))
                .andExpect(status().isOk());
    }

    @Test
    void assignYourself() throws Exception {
        setPrincipal(0L,true);
        mockMvc.perform(patch("/admin/assign/{id}",0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("This is you!"));
    }

    @Test
    void assignAlreadyAdminUser() throws Exception {
        setPrincipal(0L,true);
        doThrow(new UsernameIsUsedException("This user is already admin")).when(adminService).assign(1L);
        mockMvc.perform(patch("/admin/assign/{id}",1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("This user is already admin"));
    }

    @Test
    void assignNonExistUser() throws Exception {
        setPrincipal(0L,true);
        doThrow(new UsernameNotFoundException("User not found!")).when(adminService).assign(1L);
        mockMvc.perform(patch("/admin/assign/{id}",1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }

    @Test
    void deletePostWithCorrectData() throws Exception {
        setPrincipal(0L,true);
        mockMvc.perform(delete("/admin/posts/{id}",0L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Post 0 is deleted"));
    }

    @Test
    void deleteNonExistPost() throws Exception {
        setPrincipal(0L,true);
        doThrow(new PostNotFoundException("Post not found!")).when(adminService).deletePost(0L);
        mockMvc.perform(delete("/admin/posts/{id}",0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Post not found!"));
    }

    @Test
    void banWithCorrectData() throws Exception {
        setPrincipal(0L,true);
        mockMvc.perform(patch("/admin/users/ban/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("User 1 is banned"));
    }

    @Test
    void banAlreadyBannedUser() throws Exception {
        setPrincipal(0L,true);
        doThrow(new UsernameIsUsedException("This user is already banned")).when(adminService).ban(1L);
        mockMvc.perform(patch("/admin/users/ban/{id}",1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("This user is already banned"));
    }

    @Test
    void banNonExistUser() throws Exception {
        setPrincipal(0L,true);
        doThrow(new UsernameNotFoundException("User not found!")).when(adminService).ban(1L);
        mockMvc.perform(patch("/admin/users/ban/{id}",1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }

    @Test
    void unbanWithCorrectData() throws Exception {
        setPrincipal(0L,true);
        mockMvc.perform(patch("/admin/users/unban/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("User 1 is unbanned"));
    }

    @Test
    void unbanNonBannedUser() throws Exception {
        setPrincipal(0L,true);
        doThrow(new UsernameIsUsedException("This user is not banned")).when(adminService).unban(1L);
        mockMvc.perform(patch("/admin/users/unban/{id}",1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("This user is not banned"));
    }

    @Test
    void unbanNonExistUser() throws Exception {
        setPrincipal(0L,true);
        doThrow(new UsernameNotFoundException("User not found!")).when(adminService).unban(1L);
        mockMvc.perform(patch("/admin/users/unban/{id}",1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }
}