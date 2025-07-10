package com.media.socialmedia.Controllers;

import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.FriendService;
import com.media.socialmedia.util.InviteNotFoundException;
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

import java.util.Set;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendController.class)
@Import({SecurityConfig.class, JwtCore.class})
class FriendControllerTest {


    @MockitoBean
    private FriendService friendService;

    @Autowired
    private MockMvc mockMvc;

    private void setPrincipal(long id){
        JwtUserDetails userDetails = new JwtUserDetails(id,false,"");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getAllFriendsWithCorrectData() throws Exception {
        setPrincipal(0L);
        UserDTO friend = new UserDTO();
        friend.setId(1L);
        when(friendService.getAllFriends(0L))
                .thenReturn(Set.of(friend));
        mockMvc.perform(get("/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(friend.getId()));
    }
    
    @Test
    void getAllFriendsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/friends"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getInvitesWithCorrectData() throws Exception {
        setPrincipal(0L);
        UserDTO invited = new UserDTO();
        invited.setId(1L);
        when(friendService.getUsersInvitedByMe(0L))
                .thenReturn(Set.of(invited));
        mockMvc.perform(get("/friends/invites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(invited.getId()));
    }

    @Test
    void getInvitedWithCorrectData() throws Exception {
        setPrincipal(0L);
        UserDTO inviter = new UserDTO();
        inviter.setId(1L);
        when(friendService.getUsersInvitingMe(0L))
                .thenReturn(Set.of(inviter));
        mockMvc.perform(get("/friends/invited"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(inviter.getId()));
    }

    @Test
    void inviteWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(post("/friends/invite")
                        .param("id","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("invited!"));
    }

    @Test
    void inviteBlackListedUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameIsUsedException("User is in blacklist now!"))
                .when(friendService).inviteToFriend(0L,1L);
        mockMvc.perform(post("/friends/invite")
                        .param("id","1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User is in blacklist now!"));
    }

    @Test
    void inviteFriend() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameIsUsedException("You are friends now!"))
                .when(friendService).inviteToFriend(0L,1L);
        mockMvc.perform(post("/friends/invite")
                        .param("id","1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("You are friends now!"));
    }

    @Test
    void inviteYourself() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameIsUsedException("This is you!"))
                .when(friendService).inviteToFriend(0L,0L);
        mockMvc.perform(post("/friends/invite")
                        .param("id","0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("This is you!"));
    }

    @Test
    void inviteNonExistUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameNotFoundException("User not found!"))
                .when(friendService).inviteToFriend(0L,1L);
        mockMvc.perform(post("/friends/invite")
                .param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }

    @Test
    void acceptWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(patch("/friends/accept")
                        .param("id","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("accepted!"));
    }


    @Test
    void acceptNonExistInvite() throws Exception {
        setPrincipal(0L);
        doThrow(new InviteNotFoundException("Invite not found!"))
                .when(friendService).acceptToFriend(0L,1L);
        mockMvc.perform(patch("/friends/accept")
                        .param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Invite not found!"));
    }


    @Test
    void acceptNonExistUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameNotFoundException("User not found!"))
                .when(friendService).acceptToFriend(0L,1L);
        mockMvc.perform(patch("/friends/accept")
                        .param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }

    @Test
    void denyWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(patch("/friends/deny")
                        .param("id","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("denied!"));
    }

    @Test
    void denyNonExistInvite() throws Exception {
        setPrincipal(0L);
        doThrow(new InviteNotFoundException("Invite not found!"))
                .when(friendService).deny(0L,1L);
        mockMvc.perform(patch("/friends/deny")
                        .param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Invite not found!"));
    }

    @Test
    void denyNonExistUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameNotFoundException("User not found!"))
                .when(friendService).deny(0L,1L);
        mockMvc.perform(patch("/friends/deny")
                        .param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }

    @Test
    void removeWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(delete("/friends")
                        .param("id","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("removed!"));
    }

    @Test
    void removeSMBWhoIsNotYourFriend() throws Exception {
        setPrincipal(0L);
        doThrow(new InviteNotFoundException("User1 is not your friend!"))
                .when(friendService).remove(0L,1L);
        mockMvc.perform(delete("/friends")
                        .param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User1 is not your friend!"));
    }

    @Test
    void removeNonExistUser() throws Exception {
        setPrincipal(0L);
        doThrow(new UsernameNotFoundException("User not found!"))
                .when(friendService).remove(0L,1L);
        mockMvc.perform(delete("/friends")
                        .param("id","1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }
}