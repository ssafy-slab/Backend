package com.ssafy.ssafy_slap.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthTicketRequest(
        @NotBlank String ticket
) {
}
