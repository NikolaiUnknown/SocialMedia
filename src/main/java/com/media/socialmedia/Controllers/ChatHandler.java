package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.MessageRequestDTO;
import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Security.TokenFilter;
import com.media.socialmedia.Services.ChatService;
import com.media.socialmedia.util.PairOfMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ChatHandler {

    private final SimpMessagingTemplate template;
    private final ChatService chatService;
    @Autowired
    public ChatHandler(SimpMessagingTemplate template, ChatService chatService) {
        this.template = template;
        this.chatService = chatService;
    }

    @MessageMapping("/chat")
    public void message(Principal principal,
                        @Payload MessageRequestDTO request){
        System.out.println(principal.getName());
        Long userId = Long.valueOf(principal.getName());
        if (userId.equals(request.getRecipientId())) return;
        PairOfMessages messages = chatService.sendMessage(userId,request);
        template.convertAndSend(
                "/chat/%d_%d".formatted(userId,request.getRecipientId()),
                messages.getToSender()
                );
        template.convertAndSend(
                "/chat/%d_%d".formatted(request.getRecipientId(),userId),
                messages.getToRecipient()
        );
    }
}
