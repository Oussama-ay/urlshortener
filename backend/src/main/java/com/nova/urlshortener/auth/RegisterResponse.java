package com.nova.urlshortener.auth;

import java.time.OffsetDateTime;

public record RegisterResponse(
        Long id,
        String email,
        OffsetDateTime createdAt
) {
}