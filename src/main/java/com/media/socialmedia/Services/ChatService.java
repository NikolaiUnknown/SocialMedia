package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.MessageRequestDTO;
import com.media.socialmedia.DTO.MessageResponseDTO;
import com.media.socialmedia.DTO.UserDTO;
import com.media.socialmedia.Entity.Message;
import com.media.socialmedia.Repository.MessageRepository;
import com.media.socialmedia.util.MessageNotFoundException;
import com.media.socialmedia.util.MessageType;
import com.media.socialmedia.util.PairOfMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ModelMapper mapper;
    private final MessageRepository messageRepository;
    private final UserService userService;
    @Autowired
    public ChatService(ModelMapper mapper, MessageRepository messageRepository, UserService userService) {
        this.mapper = mapper;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    public PairOfMessages sendMessage(Long userId,MessageRequestDTO request){
        final Message message = Message.builder()
                .text(request.getText())
                .recipientId(request.getRecipientId())
                .senderId(userId)
                .dateOfSend(new Date())
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

    public Set<MessageResponseDTO> getUsersMessages(Long userId, Long id) {
        LinkedHashSet<MessageResponseDTO> messages= new LinkedHashSet<>(
                messageRepository.findAllMessagesByUsers(userId,id).stream()
                        .map((Message m) -> {
                            MessageResponseDTO dto = mapper.map(m, MessageResponseDTO.class);
                            if (m.getSenderId().equals(userId)) dto.setType(MessageType.SEND);
                            else dto.setType(MessageType.RECEIVE);
                            return dto;
                        }).toList()
        );
        return messages;
    }

    public Set<UserDTO> getUserChats(Long userId) {
        return new LinkedHashSet<>(
                messageRepository.findUsersLastMessages(userId).stream()
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
    }
}