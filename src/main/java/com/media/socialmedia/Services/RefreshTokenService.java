package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.RefreshToken;
import com.media.socialmedia.Repository.RefreshTokenRepository;
import com.media.socialmedia.Repository.UserRepository;
import com.media.socialmedia.util.RefreshTokenExpireException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${socialmedia.security.refreshLifetime}")
    private int lifetime;

    private final RefreshTokenRepository tokenRepository;
    @Autowired
    public RefreshTokenService(RefreshTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public RefreshToken createRefreshToken(Long id){
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(id)
                .token(UUID.randomUUID().toString())
                .expiryDate(new Date((new Date()).getTime() + 600000)) // set expiry of refresh token to 10 minutes - you can configure it application.properties file
                .build();
        return tokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String jwt){
        return tokenRepository.findByToken(jwt);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(new Date())<0){
            tokenRepository.delete(token);
            throw new RefreshTokenExpireException(String.format("%s Refresh token is expired. Please make a new login..!",token));
        }
        return token;
    }

}
