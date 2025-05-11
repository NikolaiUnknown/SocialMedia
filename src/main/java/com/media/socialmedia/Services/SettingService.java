package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.Repository.search.UserSearchRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SettingService {
    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;
    private final ModelMapper mapper;
    @Autowired
    public SettingService(UserRepository userRepository, UserSearchRepository userSearchRepository, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
        this.mapper = mapper;
    }

    public void setting(Long userId, SettingRequestDTO request){
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setDateOfBirthday(request.getDateOfBirthday());
        user.setCountry(request.getCountry());
        user.setProfilePicture(request.getProfilePicture());
        user.setValid(true);
        userRepository.save(user);
        userSearchRepository.save(mapper.map(user, UserSearchDocument.class));
    }

}