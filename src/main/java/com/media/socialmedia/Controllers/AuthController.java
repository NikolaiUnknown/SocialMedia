package com.media.socialmedia.Controllers;

import com.media.socialmedia.DTO.LoginRequestDTO;
import com.media.socialmedia.DTO.RegisterRequestDTO;
import com.media.socialmedia.Entity.RefreshToken;
import com.media.socialmedia.Entity.User;
import com.media.socialmedia.Security.AuthDetailsImpl;
import com.media.socialmedia.Services.RefreshTokenService;
import com.media.socialmedia.Services.RegService;
import com.media.socialmedia.Services.UserService;
import com.media.socialmedia.util.RefreshTokenExpireException;
import com.media.socialmedia.util.UserErrorResponse;
import com.media.socialmedia.Security.JwtCore;
import com.media.socialmedia.util.UserNotCreatedException;
import com.media.socialmedia.util.UsernameIsUsedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/a")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final RegService regService;
    private final RefreshTokenService tokenService;
    private final UserService userService;
    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          RegService regService,
                          RefreshTokenService tokenService,
                          JwtCore jwtCore,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.regService = regService;
        this.tokenService = tokenService;
        this.jwtCore = jwtCore;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AuthDetailsImpl userDetails = (AuthDetailsImpl) authentication.getPrincipal();
        String jwt = jwtCore.generateToken(userDetails);
        tokenService.removeAllForUser(userDetails.getUserId());
        String refreshToken = tokenService.createRefreshToken(userDetails.getUserId()).getToken();
        Cookie newRefreshTokenCookie = new Cookie("refreshToken", refreshToken);
        newRefreshTokenCookie.setHttpOnly(true);
//        newRefreshTokenCookie.setSecure(true); // Only for HTTPS
        newRefreshTokenCookie.setPath("/");
        newRefreshTokenCookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(newRefreshTokenCookie);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken") String token){
        return tokenService.findByToken(token)
                .map((refreshToken) -> {
                    try {
                        tokenService.verifyExpiration(refreshToken);
                    }catch (RefreshTokenExpireException e){
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
                    }
                    return refreshToken;
                })
                .map(RefreshToken::getUserId)
                .map((userId) -> {
                    User user = userService.loadUserById(userId);
                    String accessToken = jwtCore.generateToken(userService.loadUserByUsername(user.getEmail()));
                    return ResponseEntity.ok(accessToken);
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Request Token not found in db"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDTO registerRequest, BindingResult bindingResult, HttpServletResponse response) {
        if(bindingResult.hasErrors()){
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors){
                errorMsg.append(error.getField())
                        .append(" - ").append(error.getDefaultMessage())
                        .append(";");
            }
            throw new UserNotCreatedException(errorMsg.toString());
        }
        try {
            regService.register(registerRequest);
        } catch (UsernameIsUsedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
        LoginRequestDTO request = new LoginRequestDTO(registerRequest.getEmail(),registerRequest.getPassword());
        return ResponseEntity.ok(login(request,response).getBody());
    }
    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(ResponseStatusException e){
        UserErrorResponse response = new UserErrorResponse(e.getReason());
        return new ResponseEntity<>(response,e.getStatusCode());
    }

}