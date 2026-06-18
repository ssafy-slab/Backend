package com.ssafy.ssafy_slap.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetEmailRequest(
        @Email @NotBlank String email
) {
}
