package com.media.socialmedia.Repository;

import com.media.socialmedia.Entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
@Transactional
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUserId(Long userId);
}
