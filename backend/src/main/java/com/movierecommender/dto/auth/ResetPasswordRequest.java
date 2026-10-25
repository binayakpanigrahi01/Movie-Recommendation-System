package com.movierecommender.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "is required") String token,
        @NotBlank(message = "is required") @Size(min = 8, message = "must be at least 8 characters") String newPassword
) {}
