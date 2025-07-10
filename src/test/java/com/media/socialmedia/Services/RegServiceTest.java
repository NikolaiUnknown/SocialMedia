package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.RegisterRequestDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private RegService regService;
    @Test
    void registerWithWrongData() {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(
                "test@test.com",
                "test"
        );
        User expected = new User();
        expected.setEmail(registerRequestDTO.getEmail());
        Mockito.when(userRepository.findUserByEmail("test@test.com")).thenReturn(expected);
        Assertions.assertThrows(UsernameIsUsedException.class, () -> regService.register(registerRequestDTO));
    }

    @Test
    void registerWithCorrectData() {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(
                "test@test.com",
                "test"
        );
        User actual = new User();
        actual.setEmail(registerRequestDTO.getEmail());
        Mockito.when(mapper.map(registerRequestDTO,User.class)).thenReturn(actual);
        Assertions.assertDoesNotThrow(() -> regService.register(registerRequestDTO));
        Mockito.verify(userRepository).save(actual);
    }

}