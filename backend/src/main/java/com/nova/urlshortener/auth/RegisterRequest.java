package com.nova.urlshortener.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {
}
