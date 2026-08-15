package com.flooring.finance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flooring.finance.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Without this, Spring Security's default for a stateless setup with no
 * entry point configured is Http403ForbiddenEntryPoint - every unauthenticated
 * request (no cookie, expired token, tampered token) comes back as 403
 * instead of 401. The frontend's error interceptor only treats 401 as "not
 * logged in, redirect quietly" and shows an error toast for anything else,
 * so every page load with no session (e.g. just opening the login page)
 * was surfacing a spurious red toast for what's actually normal, expected
 * behavior. Restores the standard "401 = not authenticated" meaning, with
 * the same ApiError JSON shape as every other error response.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError error = ApiError.of(401, "Unauthorized", "Authentication is required", request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), error);
    }
}
