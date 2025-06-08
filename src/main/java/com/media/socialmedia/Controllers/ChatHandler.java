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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import java.util.Optional;

@Controller
public class ChatHandler {

    private final SimpMessagingTemplate template;
    private final ChatService chatService;
    private final TokenFilter tokenFilter;
    @Autowired
    public ChatHandler(SimpMessagingTemplate template, ChatService chatService, TokenFilter tokenFilter) {
        this.template = template;
        this.chatService = chatService;
        this.tokenFilter = tokenFilter;
    }

    @MessageMapping("/chat")
    public void message(@Payload MessageRequestDTO request){
        tokenFilter.authJWT(request.getToken());
        Optional<Authentication> auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        if (auth.isEmpty()) return;
        JwtUserDetails userDetails = (JwtUserDetails) auth
                .get()
                .getPrincipal();
        if (userDetails.getUserId().equals(request.getRecipientId())) return;
        PairOfMessages messages = chatService.sendMessage(userDetails.getUserId(),request);
        template.convertAndSend(
                "/chat/%d_%d".formatted(userDetails.getUserId(),request.getRecipientId()),
                messages.getToSender()
                );
        template.convertAndSend(
                "/chat/%d_%d".formatted(request.getRecipientId(),userDetails.getUserId()),
                messages.getToRecipient()
        );
    }
}
