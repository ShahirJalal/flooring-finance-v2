package com.flooring.finance.security;

import com.flooring.finance.entity.User;
import com.flooring.finance.repository.UserRepository;
import com.flooring.finance.security.dto.AuthDtos.ChangePasswordRequest;
import com.flooring.finance.security.dto.AuthDtos.CurrentUserResponse;
import com.flooring.finance.security.dto.AuthDtos.LoginRequest;
import com.flooring.finance.security.dto.AuthDtos.UpdateProfileRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login/logout/current-user for the single business-owner account. The JWT
 * is issued as an httpOnly cookie - it never appears in the JSON body, so
 * frontend JS can't read or leak it.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.cookie-secure}")
    private boolean cookieSecure;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<CurrentUserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        String token = jwtService.generateToken(authentication.getName());
        response.addHeader("Set-Cookie", buildCookie(token, jwtService.getExpirationSeconds()).toString());

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie("", 0).toString());
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me() {
        return ResponseEntity.ok(toResponse(currentUser()));
    }

    @PutMapping("/profile")
    public ResponseEntity<CurrentUserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = currentUser();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private ResponseCookie buildCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(JwtAuthFilter.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private CurrentUserResponse toResponse(User user) {
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFullName());
    }
}
