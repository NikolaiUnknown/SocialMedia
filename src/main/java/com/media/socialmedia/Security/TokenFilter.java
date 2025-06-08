package com.media.socialmedia.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {
    private final JwtCore jwtCore;
    @Autowired
    public TokenFilter(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = null;
        try {
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                jwt = headerAuth.substring(7);
            }
            if (isBlocked(jwt)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("User is blocked!");
                return;
            }
            authJWT(jwt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        filterChain.doFilter(request, response);
    }

    public void authJWT(String jwt){
        Long userId = null;
        boolean admin = false;
        String country = null;
        Claims claims;
        UserDetails userDetails;
        UsernamePasswordAuthenticationToken auth;
        if (jwt != null && !jwt.isEmpty()) {
            try {
                claims = jwtCore.claims(jwt);
                userId = Long.valueOf(claims.getSubject());
                admin = claims.get("admin", Boolean.class);
                country = claims.get("country",String.class);
            } catch (SignatureException | ExpiredJwtException | MalformedJwtException _) {
            }
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                userDetails = new JwtUserDetails(userId,admin,country);
                auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
    }

    public boolean isBlocked(String jwt){
        boolean blocked = false;
        if (jwt != null && !jwt.isEmpty()) {
            try {
                blocked = jwtCore.claims(jwt).get("blocked", Boolean.class);
            } catch (SignatureException | ExpiredJwtException | MalformedJwtException _) {

            }
        }
        return blocked;
    }
}