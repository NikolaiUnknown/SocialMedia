package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.MessageRequestDTO;
import com.media.socialmedia.DTO.MessageResponseDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.Message;
import com.media.socialmedia.Repository.MessageRepository;
import com.media.socialmedia.util.MessageNotFoundException;
import com.media.socialmedia.util.MessageType;
import com.media.socialmedia.util.PairOfMessages;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.processing.Generated;
import java.util.*;

@Slf4j
@Service
public class ChatService {

    private final ModelMapper mapper;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    public ChatService(ModelMapper mapper, MessageRepository messageRepository, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.mapper = mapper;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    public PairOfMessages sendMessage(Long userId,MessageRequestDTO request){
        final Message message = Message.builder()
                .text(request.getText())
                .recipientId(request.getRecipientId())
                .senderId(userId)
                .dateOfSend(new Date())
                .isRead(false)
                .build();
        messageRepository.save(message);
        PairOfMessages pair = new PairOfMessages();
        MessageResponseDTO responseToSender = mapper.map(message, MessageResponseDTO.class);
        responseToSender.setType(MessageType.SEND);
        pair.setToSender(responseToSender);
        MessageResponseDTO responseToRecipient = mapper.map(message, MessageResponseDTO.class);
        responseToRecipient.setType(MessageType.RECEIVE);
        pair.setToRecipient(responseToRecipient);
        return pair;
    }

    public Set<MessageResponseDTO> getUsersMessages(Long userId, Long id, int page) {
        LinkedHashSet<MessageResponseDTO> messages= new LinkedHashSet<>(
                messageRepository.findAllMessagesByUsers(userId,id, PageRequest.of(page,10))
                        .stream()
                        .map((Message m) -> {
                            MessageResponseDTO dto = mapper.map(m, MessageResponseDTO.class);
                            if (m.getSenderId().equals(userId)) dto.setType(MessageType.SEND);
                            else dto.setType(MessageType.RECEIVE);
                            return dto;
                        }).toList().reversed()
        );
        return messages;
    }

    public Set<UserDTO> getUserChats(Long userId) {
        return new LinkedHashSet<>(
                messageRepository.findUsersLastMessages(userId)
                        .stream()
                        .map(userService::loadUserDtoById)
                        .toList()
        );
    }

    public void deleteMessage(Long userId, Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException("Message not found!"));
        if (!message.getSenderId().equals(userId) && !message.getRecipientId().equals(userId)){
            throw new MessageNotFoundException("Message not found!");
        }
        messageRepository.delete(message);
        messagingTemplate.convertAndSend(
                "/topic/delete/%d_%d".formatted(userId,message.getRecipientId()),
                message.getId()
        );
        messagingTemplate.convertAndSend(
                "/topic/delete/%d_%d".formatted(message.getRecipientId(),userId),
                message.getId()
        );
    }

    public void readMessages(Long userId, List<Long> messages) {
        Long senderId = null;
        for (Long messageId: messages){
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new MessageNotFoundException("Message mot found!"));
            senderId = message.getSenderId();
            if (!message.getRecipientId().equals(userId))
                throw new MessageNotFoundException("Message not found!");
            if (message.isRead()) throw new MessageNotFoundException("Message is already read");
            message.setRead(true);
            log.info("Was read message: %d by user %d".formatted(messageId,userId));
            messageRepository.save(message);
        }
        log.info("Read messaging to %s".formatted("/topic/read/%d_%d".formatted(senderId,userId)));
        messagingTemplate.convertAndSend(
                "/topic/read/%d_%d".formatted(senderId,userId),
                messages
        );
    }

}