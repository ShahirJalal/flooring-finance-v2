package com.flooring.finance.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    /**
     * The JWT itself is never in this body - it's set as an httpOnly cookie
     * by AuthController so frontend JS never touches it.
     */
    public record CurrentUserResponse(
            Long id,
            String username,
            String email,
            String fullName
    ) {
    }

    public record UpdateProfileRequest(
            @NotBlank @Email String email,
            String fullName
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, message = "must be at least 8 characters") String newPassword
    ) {
    }
}
