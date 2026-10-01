package com.yelpclone.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 100) String displayName,
        @NotBlank @Size(min = 8, max = 100) String password) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record UserResponse(Long id, String email, String displayName) {
        static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getDisplayName());
        }
    }

    public record AuthResponse(String token, UserResponse user) {}
}
