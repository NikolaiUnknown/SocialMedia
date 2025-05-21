package com.media.socialmedia.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtCore {
    @Value("${socialmedia.security.secret}")
    private String secret;
    @Value("${socialmedia.security.accessLifetime}")
    private int lifetime;

    private SecretKey key;
    @PostConstruct
    void init(){
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(UserDetails user){
        AuthDetailsImpl userDetails = (AuthDetailsImpl)user;
        return Jwts.builder()
                .subject(String.valueOf(userDetails.getUserId()))
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + lifetime))
                .claim("country",userDetails.getUser().getCountry())
                .claim("admin", userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                .claim("blocked",userDetails.isBlocked())
                .signWith(key)
                .compact();
    }
    Claims claims(String jwt){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();
    }

}