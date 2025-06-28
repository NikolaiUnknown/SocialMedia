package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.MessageRequestDTO;
import com.media.socialmedia.DTO.MessageResponseDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.Chat;
import com.media.socialmedia.Entity.Message;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.ChatRepository;
import com.media.socialmedia.Repository.MessageRepository;
import com.media.socialmedia.util.ChatForbiddenException;
import com.media.socialmedia.util.ChatNotFoundException;
import com.media.socialmedia.util.MessageNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ChatService {

    private final ModelMapper mapper;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    public ChatService(ModelMapper mapper, MessageRepository messageRepository, ChatRepository chatRepository, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.mapper = mapper;
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    public Chat createChat(Long user1Id, Long user2Id){
        try {
            User user1 = userService.loadUserById(user1Id);
            User user2 = userService.loadUserById(user2Id);
            Chat chat = new Chat();
            chat.setMembers(Set.of(user1,user2));
            chat.setDateOfLastSend(new Date());
            return chatRepository.save(chat);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
    @Transactional
    public void sendMessage(Long userId,MessageRequestDTO request){
        Date date = new Date();
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new ChatNotFoundException("Chat not found!"));
        User sender = userService.loadUserById(userId);
        if (!isUserMember(userId,request.getChatId())){
            throw new ChatForbiddenException("Access denied!");
        }
        final Message message = Message.builder()
                .text(request.getText())
                .sender(sender)
                .chat(chat)
                .dateOfSend(date)
                .isRead(false)
                .build();
        messageRepository.save(message);
        chat.setDateOfLastSend(date);
        chatRepository.save(chat);
        MessageResponseDTO response = mapper.map(message, MessageResponseDTO.class);
        response.setSenderId(message.getSender().getId());
        messagingTemplate.convertAndSend(
                "/topic/chat/%s".formatted(chat.getId()),
                response
        );
    }

    public Set<MessageResponseDTO> getChatHistory(Long userId,UUID chatId, int page) {
        if (!isUserMember(userId,chatId)){
            throw new ChatForbiddenException("Access denied!");
        }
        LinkedHashSet<MessageResponseDTO> messages= new LinkedHashSet<>(
                messageRepository.findMessagesByChatIdOrderByDateOfSendDesc(chatId, PageRequest.of(page,30))
                        .stream()
                        .map((msg)-> mapper.map(msg, MessageResponseDTO.class))
                        .toList().reversed()
        );
        return messages;
    }
    public Set<UUID> getUserChats(Long userId) {
        return new LinkedHashSet<>(
                chatRepository.findUserChatsOrderByDateOfLastSendDesc(userId)
        );
    }

    public void deleteMessage(Long userId, Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException("Message not found!"));
        if (!message.getSender().getId().equals(userId)){
            throw new MessageNotFoundException("This isn't your message");
        }
        messageRepository.delete(message);
        messagingTemplate.convertAndSend(
                "/topic/delete/%s".formatted(message.getChat().getId()),
                message.getId()
        );
    }
    @Transactional
    public void readMessages(Long userId,List<Long> messagesIds) {
        if (messagesIds.isEmpty()){
            return;
        }
        List<Message> messages = messageRepository.findAllById(messagesIds);
        UUID chatId = messages.getFirst().getChat().getId();
        for (Message message: messages){
            if (message.getSender().getId().equals(userId)) {
                throw new MessageNotFoundException("Message not found!");
            }
            if (!isUserMember(userId,chatId)){
                throw new ChatForbiddenException("Access denied!");
            }
            if (message.isRead()) throw new MessageNotFoundException("Message is already read");
            message.setRead(true);
        }
        messageRepository.saveAll(messages);
        messagingTemplate.convertAndSend(
                "/topic/read/%s".formatted(chatId),
                messages
        );
    }

    public UserDTO getUserByChat(Long userId,UUID id) {
        if (!isUserMember(userId,id)){
            throw new ChatForbiddenException("Access denied!");
        }
        return userService.loadUserDtoById(chatRepository.findOtherMember(userId,id));
    }
    @Transactional
    public void deleteChat(Long userId, UUID chatId) {
        if (!isUserMember(userId,chatId)){
            throw new ChatForbiddenException("Access denied!");
        }
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found!"));
        chat.getMembers().clear();
        chatRepository.save(chat);
        messageRepository.deleteMessagesByChatId(chatId);
        chatRepository.delete(chat);
    }

    private boolean isUserMember(Long userId, UUID chatId){
        return chatRepository.isUserMember(userId,chatId);
    }
}