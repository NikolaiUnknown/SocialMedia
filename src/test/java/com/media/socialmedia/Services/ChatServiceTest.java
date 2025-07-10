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
import com.media.socialmedia.util.UsernameIsUsedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ModelMapper mapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatService chatService;

    @Test
    void createChatWithCorrectData() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Mockito.when(userService.loadUserById(1L)).thenReturn(user1);
        Mockito.when(userService.loadUserById(2L)).thenReturn(user2);
        Assertions.assertDoesNotThrow(()->chatService.createChat(1L,2L));
    }

    @Test
    void createChatWithIdenticalUser() {
        Assertions.assertThrows(UsernameIsUsedException.class,() -> chatService.createChat(0L,0L));
    }

    @Test
    void createChatNonExistUser() {
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,() -> chatService.createChat(0L,1L));
    }


    @Test
    void sendMessageWithCorrectData() {
        UUID chatId = UUID.randomUUID();
        Chat chat = new Chat();
        chat.setId(chatId);
        User user = new User();
        user.setId(0L);
        MessageRequestDTO requestDTO = new MessageRequestDTO(chatId,null);
        Mockito.when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        Mockito.when(userService.loadUserById(0L)).thenReturn(user);
        Mockito.when(chatRepository.isUserMember(0L,chatId)).thenReturn(true);
        MessageResponseDTO response = new MessageResponseDTO();
        Mockito.when(mapper.map(Mockito.any(Message.class), Mockito.eq(MessageResponseDTO.class))).thenReturn(response);
        Assertions.assertDoesNotThrow(() -> chatService.sendMessage(0L,requestDTO));
        Mockito.verify(messagingTemplate).convertAndSend(
                "/topic/chat/%s".formatted(chatId),
                response
        );
    }

    @Test
    void sendMessageNonExistChat() {
        UUID chatId = UUID.randomUUID();
        MessageRequestDTO requestDTO = new MessageRequestDTO(chatId,null);
        Assertions.assertThrows(ChatNotFoundException.class,
                () -> chatService.sendMessage(0L,requestDTO));
    }

    @Test
    void sendMessageNonExistUser() {
        UUID chatId = UUID.randomUUID();
        MessageRequestDTO requestDTO = new MessageRequestDTO(chatId,null);
        Mockito.when(chatRepository.findById(chatId)).thenReturn(Optional.of(new Chat()));
        Mockito.when(userService.loadUserById(0L)).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> chatService.sendMessage(0L,requestDTO));
    }

    @Test
    void sendMessageForbiddenChat() {
        UUID chatId = UUID.randomUUID();
        MessageRequestDTO requestDTO = new MessageRequestDTO(chatId,null);
        Mockito.when(chatRepository.findById(chatId)).thenReturn(Optional.of(new Chat()));
        Mockito.when(chatRepository.isUserMember(0L,chatId)).thenReturn(false);
        Assertions.assertThrows(ChatForbiddenException.class,() -> chatService.sendMessage(0L,requestDTO));
    }

    @Test
    void getChatHistoryWithCorrectData() {
        UUID chatId = UUID.randomUUID();
        Mockito.when(chatRepository.isUserMember(0L,chatId)).thenReturn(true);
        Mockito.when(messageRepository.findMessagesByChatIdOrderByDateOfSendDesc(
                chatId, PageRequest.of(0,30))
        ).thenReturn(Page.empty());
        Assertions.assertDoesNotThrow(() -> chatService.getChatHistory(0L,chatId,0));
    }

    @Test
    void getForbiddenChatHistory() {
        UUID chatId = UUID.randomUUID();
        Mockito.when(chatRepository.isUserMember(0L,chatId)).thenReturn(false);
        Assertions.assertThrows(ChatForbiddenException.class,() -> chatService.getChatHistory(0L,chatId,0));
    }

    @Test
    void getUserChatsWithData() {
        Set<UUID> chats = new HashSet<>();
        chats.add(UUID.randomUUID());
        chats.add(UUID.randomUUID());
        chats.add(UUID.randomUUID());
        Mockito.when(chatRepository.findUserChatsOrderByDateOfLastSendDesc(0L)).thenReturn(chats);
        Assertions.assertEquals(chats,chatService.getUserChats(0L));
    }

    @Test
    void deleteMessageWithCorrectData() {
        User sender = new User();
        sender.setId(0L);
        Message message = Message.builder()
                .sender(sender)
                .chat(new Chat())
                .build();
        Mockito.when(messageRepository.findById(0L)).thenReturn(Optional.of(message));
        Assertions.assertDoesNotThrow(()->chatService.deleteMessage(0L,0L));
        Mockito.verify(messagingTemplate).convertAndSend(
                "/topic/delete/%s".formatted(message.getChat().getId()),
                message.getId()
        );
    }

    @Test
    void deleteNonExistMessage() {
        Assertions.assertThrows(MessageNotFoundException.class,()->chatService.deleteMessage(0L,0L));
    }

    @Test
    void deleteForbiddenMessage() {
        User sender = new User();
        sender.setId(0L);
        Message message = Message.builder()
                .sender(sender)
                .build();
        Mockito.when(messageRepository.findById(0L)).thenReturn(Optional.of(message));
        Assertions.assertThrows(MessageNotFoundException.class,()->chatService.deleteMessage(1L,0L));
    }

    @Test
    void readMessagesWithCorrectData() {
        User sender = new User();
        sender.setId(0L);
        UUID chatId = UUID.randomUUID();
        Chat chat = new Chat();
        chat.setId(chatId);
        Message message = Message.builder()
                .id(0L)
                .chat(chat)
                .sender(sender)
                .isRead(false)
                .build();
        Mockito.when(messageRepository.findAllById(List.of(0L))).thenReturn(List.of(message));
        Mockito.when(chatRepository.isUserMember(1L,chatId)).thenReturn(true);
        Assertions.assertDoesNotThrow(()->chatService.readMessages(1L,List.of(0L)));
        Mockito.verify(messageRepository).saveAll(List.of(message));
        Mockito.verify(messagingTemplate).convertAndSend(
                "/topic/read/%s".formatted(chatId),
                List.of(message)
        );
    }

    @Test
    void readMyMessages() {
        User sender = new User();
        sender.setId(0L);
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID());
        Message message = Message.builder()
                .id(0L)
                .chat(chat)
                .sender(sender)
                .build();
        Mockito.when(messageRepository.findAllById(List.of(0L))).thenReturn(List.of(message));
        Assertions.assertThrows(MessageNotFoundException.class,
                ()-> chatService.readMessages(0L,List.of(0L)));
    }

    @Test
    void readForbiddenMessages() {
        User sender = new User();
        sender.setId(0L);
        UUID chatId = UUID.randomUUID();
        Chat chat = new Chat();
        chat.setId(chatId);
        Message message = Message.builder()
                .id(0L)
                .chat(chat)
                .sender(sender)
                .build();
        Mockito.when(messageRepository.findAllById(List.of(0L))).thenReturn(List.of(message));
        Mockito.when(chatRepository.isUserMember(1L,chatId)).thenReturn(false);
        Assertions.assertThrows(ChatForbiddenException.class,
                ()-> chatService.readMessages(1L,List.of(0L)));
    }

    @Test
    void readAlreadyReadMessages() {
        User sender = new User();
        sender.setId(0L);
        UUID chatId = UUID.randomUUID();
        Chat chat = new Chat();
        chat.setId(chatId);
        Message message = Message.builder()
                .id(0L)
                .chat(chat)
                .sender(sender)
                .isRead(true)
                .build();
        Mockito.when(messageRepository.findAllById(List.of(0L))).thenReturn(List.of(message));
        Mockito.when(chatRepository.isUserMember(1L,chatId)).thenReturn(true);
        Assertions.assertThrows(MessageNotFoundException.class,
                ()-> chatService.readMessages(1L,List.of(0L)));
    }

    @Test
    void getUserByChatWithCorrectData() {
        UUID chatId = UUID.randomUUID();
        UserDTO otherUser = new UserDTO();
        otherUser.setId(1L);
        Mockito.when(chatRepository.isUserMember(0L,chatId)).thenReturn(true);
        Mockito.when(chatRepository.findOtherMember(0L,chatId)).thenReturn(1L);
        Mockito.when(userService.loadUserDtoById(1L)).thenReturn(otherUser);
        Assertions.assertEquals(otherUser,chatService.getUserByChat(0L,chatId));
    }


    @Test
    void getUserByForbiddenChat() {
        UUID chatId = UUID.randomUUID();
        Mockito.when(chatRepository.isUserMember(0L,chatId)).thenReturn(false);
        Assertions.assertThrows(ChatForbiddenException.class, ()-> chatService.getUserByChat(0L,chatId));
    }

    @Test
    void deleteChatWithCorrectData() {
        UUID chatId = UUID.randomUUID();
        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setMembers(new HashSet<>());
        Mockito.when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        Mockito.when(chatRepository.isUserMember(0L,chatId)).thenReturn(true);
        Assertions.assertDoesNotThrow(()->chatService.deleteChat(0L,chatId));
        Mockito.verify(chatRepository).delete(chat);
    }

    @Test
    void deleteNonExistChat() {
        Assertions.assertThrows(ChatNotFoundException.class,
                ()->chatService.deleteChat(0L,UUID.randomUUID()));
    }

    @Test
    void deleteForbiddenChat() {
        UUID chatId = UUID.randomUUID();
        Chat chat = new Chat();
        chat.setId(chatId);
        Mockito.when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        Mockito.when(chatRepository.isUserMember(0L,chatId)).thenReturn(false);
        Assertions.assertThrows(ChatForbiddenException.class,
                ()->chatService.deleteChat(0L,chatId));
    }
}