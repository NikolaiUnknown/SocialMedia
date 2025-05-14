package com.media.socialmedia.Services;

import com.media.socialmedia.DTO.SettingRequestDTO;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Entity.search.UserSearchDocument;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.Repository.search.UserSearchRepository;
import com.media.socialmedia.util.Caches;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SettingService {
    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;
    private final ModelMapper mapper;
    private final CacheService cacheService;
    @Autowired
    public SettingService(UserRepository userRepository, UserSearchRepository userSearchRepository, ModelMapper mapper, CacheService cacheService) {
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
        this.mapper = mapper;
        this.cacheService = cacheService;
    }

    public void setting(Long userId, SettingRequestDTO request){
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setDateOfBirthday(request.getDateOfBirthday());
        user.setCountry(request.getCountry());
        user.setProfilePicture(request.getProfilePicture());
        user.setPrivate(request.getAccessibility());
        log.warn(String.valueOf(request.getAccessibility()));
        user.setValid(true);
        UserSearchDocument document = mapper.map(user, UserSearchDocument.class);
        document.setFullName(String.format("%s %s",user.getFirstname(), user.getLastname()));
        cacheService.evictFromCache(Caches.USERS,userId);
        userRepository.save(user);
        userSearchRepository.save(document);
    }

}