package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingService {
    private final UserRepository userRepository;
    @Autowired
    public SettingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setting(Long userId, SettingRequestDTO request){
        userRepository.setUserInfoById(request.getFirstname(),request.getLastname(),request.getDateOfBirthday(),request.getCountry(),request.getProfilePicture(),userId);
    }

}