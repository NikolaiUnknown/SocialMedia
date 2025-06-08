package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.MessageResponseDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.ChatService;
import com.media.socialmedia.util.MessageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages/{id}")
    public Set<MessageResponseDTO> getMessages(@AuthenticationPrincipal JwtUserDetails userDetails,
                                               @PathVariable Long id){
        if (userDetails.getUserId().equals(id)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This is you!");
        }
        return chatService.getUsersMessages(userDetails.getUserId(), id);
    }

    @DeleteMapping("/messages/{id}")
    public void deleteMessage(@AuthenticationPrincipal JwtUserDetails userDetails,
                              @PathVariable Long id){
        try {
            chatService.deleteMessage(userDetails.getUserId(), id);
        } catch (MessageNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }


    @GetMapping("/users")
    public Set<UserDTO> getUsers(@AuthenticationPrincipal JwtUserDetails userDetails){
        return chatService.getUserChats(userDetails.getUserId());
    }

    @ExceptionHandler
    private ResponseEntity<String> handleException(ResponseStatusException e){
        return new ResponseEntity<>(e.getReason(), e.getStatusCode());
    }

}
