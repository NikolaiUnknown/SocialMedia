package com.media.socialmedia.Security;

import com.media.socialmedia.Entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
@Getter
public class AuthDetailsImpl implements UserDetails {
    private final User user;
    private final boolean blocked;

    public AuthDetailsImpl(User user) {
        this.user = user;
        this.blocked = user.isBlocked();
    }

    public Long getUserId(){
        return user.getId();
    }
    public Boolean isBlocked(){return blocked;}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER");
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

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