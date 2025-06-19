package com.media.socialmedia.Configs;

import com.media.socialmedia.Security.JwtUserDetails;
import com.media.socialmedia.Security.TokenFilter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenFilter tokenFilter;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic/");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> headers = accessor.getNativeHeader("Authorization");
                    if (headers != null && !headers.isEmpty() && !headers.getFirst().isEmpty()){
                        String jwt = headers.getFirst().substring(7);
                        if (tokenFilter.isBlocked(jwt)) return message;
                        tokenFilter.authJWT(jwt);
                        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext()
                                .getAuthentication().getPrincipal();
                        Principal principal = new Principal() {
                            @Override
                            public String getName() {
                                return String.valueOf(userDetails.getUserId());
                            }
                        };
                        accessor.setUser(principal);
                    }
                }
                return message;
            }
        });
    }
    @Bean(name = "bucketWebSocket")
    public Bucket bucket(){
        Bandwidth limit = Bandwidth.builder()
                .capacity(1)
                .refillGreedy(1, Duration.ofSeconds(2))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}