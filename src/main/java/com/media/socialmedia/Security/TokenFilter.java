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
        Long userId = null;
        boolean admin = false;
        boolean blocked = false;
        Claims claims;
        UserDetails userDetails;
        UsernamePasswordAuthenticationToken auth;

        try {
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                jwt = headerAuth.substring(7);
            }
            if (jwt != null && !jwt.isEmpty()) {
                try {
                    claims = jwtCore.claims(jwt);
                    userId = Long.valueOf(claims.getSubject());
                    admin = claims.get("admin", Boolean.class);
                    blocked = claims.get("blocked", Boolean.class);

                } catch (SignatureException | ExpiredJwtException | MalformedJwtException _) {

                }
                if (blocked) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("text/plain");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("User is blocked!");
                    return;
                }

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userDetails = new JwtUserDetails(userId,admin);
                    auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        filterChain.doFilter(request, response);
    }

}