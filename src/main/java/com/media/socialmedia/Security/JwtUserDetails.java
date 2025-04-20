package com.media.socialmedia.Security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class JwtUserDetails implements UserDetails {
    private final Long userId;
    private final boolean admin;
    public JwtUserDetails(Long userId, boolean admin) {
        this.userId = userId;
        this.admin = admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> admin ? "ROLE_ADMIN" : "ROLE_USER");
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {return null;}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
