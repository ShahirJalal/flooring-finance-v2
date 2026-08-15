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
import org.springframework.security.core.AuthenticationException;
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
    private final LoginRateLimiter loginRateLimiter;

    @Value("${app.security.cookie-secure}")
    private boolean cookieSecure;

    public AuthController(
            AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository,
            PasswordEncoder passwordEncoder, LoginRateLimiter loginRateLimiter
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<CurrentUserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        loginRateLimiter.checkAllowed(request.username());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException ex) {
            loginRateLimiter.recordFailure(request.username());
            throw ex;
        }
        loginRateLimiter.recordSuccess(request.username());

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        String token = jwtService.generateToken(authentication.getName(), user.getTokenVersion());
        response.addHeader("Set-Cookie", buildCookie(token, jwtService.getExpirationSeconds()).toString());

        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Belt-and-suspenders alongside clearing the cookie: a JWT that was
        // captured before logout (proxy log, compromised machine, ...) stops
        // being accepted by JwtAuthFilter from this moment on, rather than
        // silently remaining valid for the rest of its 7-day lifetime. Bumping
        // an integer version (rather than comparing timestamps) means there's
        // no clock-precision race window at any timing.
        User user = currentUser();
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

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
        // Any session started under the old password stops working immediately.
        user.setTokenVersion(user.getTokenVersion() + 1);
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
