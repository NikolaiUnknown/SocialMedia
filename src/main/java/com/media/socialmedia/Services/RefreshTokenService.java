package com.media.socialmedia.Services;

import com.media.socialmedia.Entity.RefreshToken;
import com.media.socialmedia.Repository.RefreshTokenRepository;
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
                .expiryDate(new Date((new Date()).getTime() + lifetime))
                .build();
        return tokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token){
        return tokenRepository.findByToken(token);
    }
    public void removeAllForUser(Long userId){
        tokenRepository.deleteAllByUserId(userId);
    }
    public void verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(new Date())<0){
            tokenRepository.delete(token);
            throw new RefreshTokenExpireException("Refresh token is expired. Please make a new login..!");
        }
    }

}
