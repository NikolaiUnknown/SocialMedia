package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.MessageResponseDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.Chat;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Services.ChatService;
import com.media.socialmedia.util.ChatForbiddenException;
import com.media.socialmedia.util.ChatNotFoundException;
import com.media.socialmedia.util.MessageNotFoundException;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;
    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> createChat(@AuthenticationPrincipal JwtUserDetails userDetails,
                                        @PathVariable("id") Long id){
        try {
            Chat chat = chatService.createChat(userDetails.getUserId(),id);
            return new ResponseEntity<>(chat, HttpStatus.CREATED);
        } catch (UsernameIsUsedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteChat(@AuthenticationPrincipal JwtUserDetails userDetails,
                           @PathVariable("id") UUID chatId){
        try {
            chatService.deleteChat(userDetails.getUserId(),chatId);
        } catch (ChatNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ChatForbiddenException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping("/messages/{id}/{page}")
    public ResponseEntity<Set<MessageResponseDTO>> getMessages(@AuthenticationPrincipal JwtUserDetails userDetails,
                                               @PathVariable("id") UUID chatId,
                                               @PathVariable int page
                                               ){
        try {
            return ResponseEntity.ok(chatService.getChatHistory(userDetails.getUserId(),chatId, page));
        } catch (ChatForbiddenException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
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

    @PatchMapping("/read")
    public void readMessage(@AuthenticationPrincipal JwtUserDetails userDetails,
                            @RequestBody List<Long> messages){
        try {
            chatService.readMessages(userDetails.getUserId(),messages);
        } catch (MessageNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        } catch (ChatForbiddenException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<UserDTO> getUserByChat(@RequestParam("id") UUID id, @AuthenticationPrincipal JwtUserDetails userDetails){
        try {
            return ResponseEntity.ok(chatService.getUserByChat(userDetails.getUserId(),id));
        } catch (ChatForbiddenException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,e.getMessage());
        }
    }

    @GetMapping // /api/chats
    public ResponseEntity<Set<UUID>> getChats(@AuthenticationPrincipal JwtUserDetails userDetails){
        return ResponseEntity.ok(chatService.getUserChats(userDetails.getUserId()));
    }

    @ExceptionHandler
    private ResponseEntity<String> handleException(ResponseStatusException e){
        return new ResponseEntity<>(e.getReason(), e.getStatusCode());
    }

}
