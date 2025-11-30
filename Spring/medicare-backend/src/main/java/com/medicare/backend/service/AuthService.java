package com.medicare.backend.service;

import com.medicare.backend.config.JwtUtil;
import com.medicare.backend.dto.AuthResponse;
import com.medicare.backend.dto.LoginRequest;
import com.medicare.backend.dto.RegisterRequest;
import com.medicare.backend.dto.UserResponse;
import com.medicare.backend.entity.User;
import com.medicare.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return new AuthResponse(false, "Email is already taken!", null, null);
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return new AuthResponse(false, "Username is already taken!", null, null);
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        // Parse date of birth
        if (registerRequest.getDob() != null && !registerRequest.getDob().isEmpty()) {
            try {
                LocalDate dob = LocalDate.parse(registerRequest.getDob(), DateTimeFormatter.ISO_DATE);
                user.setDateOfBirth(dob);
            } catch (Exception e) {
                return new AuthResponse(false, "Invalid date format. Use YYYY-MM-DD", null, null);
            }
        }

        userRepository.save(user);

        // Generate token using email
        String token = jwtUtil.generateToken(user.getEmail());

        // Create user response
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null
        );

        return new AuthResponse(true, "Registration successful!", token, userResponse);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = customUserDetailsService.loadUserEntityByEmail(loginRequest.getEmail());
            
            // Generate token using email
            String token = jwtUtil.generateToken(user.getEmail());

            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null
            );

            return new AuthResponse(true, "Login successful!", token, userResponse);

        } catch (Exception e) {
            return new AuthResponse(false, "Invalid email or password", null, null);
        }
    }
}