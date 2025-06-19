package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.MessageRequestDTO;
import com.media.socialmedia.Services.ChatService;
import com.media.socialmedia.util.MessageNotFoundException;
import com.media.socialmedia.util.PairOfMessages;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class ChatHandler {

    private final SimpMessagingTemplate template;
    private final ChatService chatService;
    private final Bucket bucket;
    @Autowired
    public ChatHandler(@Qualifier(value = "bucketWebSocket") Bucket bucket,SimpMessagingTemplate template, ChatService chatService) {
        this.template = template;
        this.chatService = chatService;
        this.bucket = bucket;
    }

    @MessageMapping("/chat/send")
    public void message(Principal principal,
                        @Valid @Payload MessageRequestDTO request){
        if (!bucket.tryConsume(1)){
            throw new RuntimeException("Too many messages");
        }
        Long userId = Long.valueOf(principal.getName());
        if (userId.equals(request.getRecipientId())) throw new RuntimeException("This is you!");
        PairOfMessages messages = chatService.sendMessage(userId,request);
        template.convertAndSend(
                "/topic/chat/%d_%d".formatted(userId,request.getRecipientId()),
                messages.getToSender()
                );
        template.convertAndSend(
                "/topic/chat/%d_%d".formatted(request.getRecipientId(),userId),
                messages.getToRecipient()
        );
    }


    @MessageExceptionHandler
    private void handler(RuntimeException e){
        template.convertAndSend("/topic/errors",e.getMessage());
    }
}

