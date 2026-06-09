package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final CustomUserDetailsService uds;

    public AuthController(UserRepository userRepository, PasswordEncoder encoder, JwtService jwtService,
            AuthenticationManager authManager, CustomUserDetailsService uds) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.uds = uds;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByLogin(req.login())) {
            throw new ConflictException("Login already exists: " + req.login());
        }
        
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already exists: " + req.email());
        }

        AppUser user = new AppUser(
                req.login(),
                encoder.encode(req.password()),
                Role.USER 
        );
        user.setEmail(req.email());
        
        userRepository.save(user);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.login(), req.password())
        );

        UserDetails user = uds.loadUserByUsername(req.login());
        String accessToken = jwtService.generateAccessToken(user);

        return new AuthResponse(accessToken);
    }
}