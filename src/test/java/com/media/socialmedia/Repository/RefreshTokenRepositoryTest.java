package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void findByJwt() {
        RefreshToken token = new RefreshToken();
        String jwt = UUID.randomUUID().toString();
        token.setToken(jwt);
        refreshTokenRepository.save(token);
        assertEquals(token,refreshTokenRepository.findByToken(jwt).orElseThrow());
    }

    @Test
    void findByNonExistJwt() {
        assertTrue(refreshTokenRepository.findByToken("").isEmpty());
    }

    @Test
    void deleteAllTokensByUserId() {
        RefreshToken firstToken = new RefreshToken();
        String firstJwt = UUID.randomUUID().toString();
        firstToken.setToken(firstJwt);
        firstToken.setUserId(0L);
        RefreshToken secondToken = new RefreshToken();
        String secondJwt = UUID.randomUUID().toString();
        secondToken.setToken(secondJwt);
        secondToken.setUserId(0L);
        refreshTokenRepository.save(firstToken);
        refreshTokenRepository.save(secondToken);
        assertTrue(refreshTokenRepository.findById(firstToken.getId()).isPresent());
        assertTrue(refreshTokenRepository.findById(secondToken.getId()).isPresent());
        refreshTokenRepository.deleteAllByUserId(0L);
        assertTrue(refreshTokenRepository.findById(firstToken.getId()).isEmpty());
        assertTrue(refreshTokenRepository.findById(secondToken.getId()).isEmpty());

    }
}