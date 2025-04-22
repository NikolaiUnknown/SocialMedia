package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.RegisterRequestDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.UsernameIsUsedException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Date;

@Service
public class RegService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper mapper;
    @Autowired
    public RegService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }


    public void register(RegisterRequestDTO registerRequest){
        User user = convertToUser(registerRequest);
        if (userRepository.findUserByEmail(registerRequest.getEmail()) != null){
            throw new UsernameIsUsedException("This email address: " + registerRequest.getEmail() + " is already in use!");
        }
        user.setFirstname("user" + user.getId());
        user.setLastname("last" + user.getId());
        user.setDateOfBirthday(new Date());
        user.setValid(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    private User convertToUser(RegisterRequestDTO user) {
        return mapper.map(user,User.class);
    }


}