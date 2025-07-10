package com.media.socialmedia.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.media.socialmedia.Configs.SecurityConfig;
import com.media.socialmedia.DTO.LoginRequestDTO;
import com.media.socialmedia.DTO.RegisterRequestDTO;
import com.media.socialmedia.Entity.RefreshToken;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Security.AuthDetailsImpl;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.Services.RefreshTokenService;
import com.media.socialmedia.Services.RegService;
import com.media.socialmedia.Services.UserService;
import com.media.socialmedia.util.RefreshTokenExpireException;
import com.media.socialmedia.util.UsernameIsUsedException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private RefreshTokenService tokenService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtCore jwtCore;
    @MockitoBean
    private RegService regService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginWithCorrectData() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO("email@test.com","password");
        User authPrincipal = new User();
        authPrincipal.setId(0L);
        var userDetails = new AuthDetailsImpl(authPrincipal);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(jwtCore.generateToken(userDetails)).thenReturn("BearerToken");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(new Date());
        refreshToken.setToken("RefreshToken");
        when(tokenService.createRefreshToken(userDetails.getUserId())).thenReturn(refreshToken);
        mockMvc.perform(post("/a/login").
                contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("refreshToken","RefreshToken"))
                .andExpect(cookie().httpOnly("refreshToken",true))
                .andExpect(jsonPath("$").value("BearerToken"));
    }

    @Test
    void loginWithBadCredentials() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO("email@test.com","password");
        when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenThrow(BadCredentialsException.class);
        mockMvc.perform(post("/a/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"));
    }

    @Test
    void refreshTokenWithCorrectData() throws Exception {
        RefreshToken token = new RefreshToken();
        token.setId(0L);
        token.setToken("refreshToken");
        token.setUserId(0L);
        User authPrincipal = new User();
        authPrincipal.setId(0L);
        authPrincipal.setEmail("email");
        var userDetails = new AuthDetailsImpl(authPrincipal);
        when(userService.loadUserById(0L)).thenReturn(authPrincipal);
        when(userService.loadUserByUsername("email")).thenReturn(userDetails);
        when(jwtCore.generateToken(userDetails)).thenReturn("RefreshedBearerToken");
        when(tokenService.findByToken("refreshToken")).thenReturn(Optional.of(token));
        mockMvc.perform(post("/a/refresh").cookie(new Cookie("refreshToken","refreshToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("RefreshedBearerToken"));
    }

    @Test
    void refreshWithoutToken() throws Exception {
        mockMvc.perform(post("/a/refresh").cookie(new Cookie("refreshToken","")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Cannot refresh token"));
    }

    @Test
    void refreshWithExpireToken() throws Exception {
        RefreshToken token = new RefreshToken();
        token.setToken("expiredToken");
        doThrow(new RefreshTokenExpireException("Refresh token is expired. Please make a new login..!"))
                .when(tokenService).verifyExpiration(token);
        when(tokenService.findByToken("expiredToken")).thenReturn(Optional.of(token));
        mockMvc.perform(post("/a/refresh").cookie(new Cookie("refreshToken","expiredToken")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token is expired. Please make a new login..!"));
    }

    @Test
    void refreshWithoutTokenInDB() throws Exception {
        mockMvc.perform(post("/a/refresh").cookie(new Cookie("refreshToken","token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh Token not found in db"));
    }

    @Test
    void registerWithCorrectData() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("test@test.com", "12345678");
        User authPrincipal = new User();
        authPrincipal.setId(0L);
        var userDetails = new AuthDetailsImpl(authPrincipal);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,null);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(jwtCore.generateToken(userDetails)).thenReturn("BearerToken");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(new Date());
        refreshToken.setToken("RefreshToken");
        when(tokenService.createRefreshToken(userDetails.getUserId())).thenReturn(refreshToken);
        mockMvc.perform(post("/a/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("refreshToken","RefreshToken"))
                .andExpect(cookie().httpOnly("refreshToken",true))
                .andExpect(jsonPath("$").value("BearerToken"));
        verify(regService).register(registerRequestDTO);
    }

    @Test
    void registerWithAlreadyUsedEmail() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("test@test.com", "12345678");
        doThrow(new UsernameIsUsedException("This email address: test@test.com is already in use!"))
                .when(regService).register(registerRequestDTO);
        mockMvc.perform(post("/a/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This email address: test@test.com is already in use!"));
    }

    @Test
    void registerWithNonValidRequest() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("", "");
        mockMvc.perform(post("/a/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password - password must be greater than 6!;"));
    }
}