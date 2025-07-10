package com.media.socialmedia.Controllers;

import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.DTO.MessageResponseDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.Chat;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.ChatService;
import com.media.socialmedia.util.ChatForbiddenException;
import com.media.socialmedia.util.ChatNotFoundException;
import com.media.socialmedia.util.MessageNotFoundException;
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

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({SecurityConfig.class, JwtCore.class})
class ChatControllerTest {

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private MockMvc mockMvc;

    private void setPrincipal(long id){
        JwtUserDetails userDetails = new JwtUserDetails(id,false,"");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getChatsWithCorrectData() throws Exception {
        setPrincipal(0L);
        when(chatService.getUserChats(0L)).thenReturn(Set.of(UUID.randomUUID()));
        mockMvc.perform(get("/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getChatsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/chats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createChatWithCorrectData() throws Exception {
        setPrincipal(0L);
        Chat chat = new Chat();
        when(chatService.createChat(0L,1L)).thenReturn(chat);
        mockMvc.perform(post("/chats/{id}",1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(chat));
    }

    @Test
    void createChatWithYourself() throws Exception {
        setPrincipal(0L);
        when(chatService.createChat(0L,0L))
                .thenThrow(new UsernameIsUsedException("This is you!"));
        mockMvc.perform(post("/chats/{id}",0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("This is you!"));
    }

    @Test
    void createChatWithNonExistUser() throws Exception {
        setPrincipal(0L);
        when(chatService.createChat(0L,1L))
                .thenThrow(new UsernameNotFoundException("User not found!"));
        mockMvc.perform(post("/chats/{id}",1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found!"));
    }

    @Test
    void deleteChatWithCorrectData() throws Exception {
        setPrincipal(0L);
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID());
        mockMvc.perform(delete("/chats/{id}",chat.getId()))
                .andExpect(status().isOk());
        verify(chatService).deleteChat(0L,chat.getId());
    }

    @Test
    void deleteForbiddenChat() throws Exception {
        setPrincipal(0L);
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID());
        doThrow(new ChatForbiddenException("Access denied!"))
                .when(chatService).deleteChat(0L,chat.getId());
        mockMvc.perform(delete("/chats/{id}",chat.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Access denied!"));
    }

    @Test
    void deleteNonExistChat() throws Exception {
        setPrincipal(0L);
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID());
        doThrow(new ChatNotFoundException("Chat not found!"))
                .when(chatService).deleteChat(0L,chat.getId());
        mockMvc.perform(delete("/chats/{id}",chat.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Chat not found!"));
    }

    @Test
    void getMessagesWithCorrectData() throws Exception {
        setPrincipal(0L);
        UUID chatId = UUID.randomUUID();
        MessageResponseDTO message = new MessageResponseDTO(
                0L,0L,new Date(),"",false
        );
        when(chatService.getChatHistory(0L,chatId, 0))
                .thenReturn(Set.of(message));
        mockMvc.perform(get("/chats/messages/{chatId}/0",chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(message.getId()));
    }

    @Test
    void getMessagesFromForbiddenChat() throws Exception {
        setPrincipal(0L);
        UUID chatId = UUID.randomUUID();
        when(chatService.getChatHistory(0L,chatId, 0))
                .thenThrow(new ChatForbiddenException("Access denied!"));
        mockMvc.perform(get("/chats/messages/{chatId}/0",chatId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Access denied!"));
    }

    @Test
    void deleteMessageWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(delete("/chats/messages/{id}",0L))
                .andExpect(status().isOk());
    }

    @Test
    void deleteNonNotYourMessage() throws Exception {
        setPrincipal(0L);
        doThrow(new MessageNotFoundException("This isn't your message"))
                .when(chatService).deleteMessage(0L, 0L);
        mockMvc.perform(delete("/chats/messages/{id}",0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("This isn't your message"));
    }

    @Test
    void deleteNonExistMessage() throws Exception {
        setPrincipal(0L);
        doThrow(new MessageNotFoundException("Message not found!"))
                .when(chatService).deleteMessage(0L, 0L);
        mockMvc.perform(delete("/chats/messages/{id}",0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Message not found!"));
    }

    @Test
    void readMessageWithCorrectData() throws Exception {
        setPrincipal(0L);
        mockMvc.perform(patch("/chats/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk());
    }

    @Test
    void readAlreadyReadMessage() throws Exception {
        setPrincipal(0L);
        doThrow(new MessageNotFoundException("Message is already read"))
                .when(chatService).readMessages(0L,Collections.emptyList());
        mockMvc.perform(patch("/chats/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Message is already read"));
    }

    @Test
    void readMessageFromForbiddenChat() throws Exception {
        setPrincipal(0L);
        doThrow(new ChatForbiddenException("Access denied!"))
                .when(chatService).readMessages(0L,Collections.emptyList());
        mockMvc.perform(patch("/chats/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Access denied!"));
    }

    @Test
    void readNonExistMessage() throws Exception {
        setPrincipal(0L);
        doThrow(new MessageNotFoundException("Message not found!"))
                .when(chatService).readMessages(0L,Collections.emptyList());
        mockMvc.perform(patch("/chats/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Message not found!"));
    }

    @Test
    void getUserByChatWithCorrectData() throws Exception {
        setPrincipal(0L);
        UserDTO user = new UserDTO();
        user.setId(1L);
        UUID chatId = UUID.randomUUID();
        when(chatService.getUserByChat(0L,chatId))
                .thenReturn(user);
        mockMvc.perform(get("/chats/user")
                        .param("id",chatId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()));
    }

    @Test
    void getUserByForbiddenChat() throws Exception {
        setPrincipal(0L);
        UUID chatId = UUID.randomUUID();
        when(chatService.getUserByChat(0L,chatId))
                .thenThrow(new ChatForbiddenException("Access denied!"));
        mockMvc.perform(get("/chats/user")
                        .param("id",chatId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Access denied!"));
    }

}