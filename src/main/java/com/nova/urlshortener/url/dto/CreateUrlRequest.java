package com.nova.urlshortener.url.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record CreateUrlRequest(
    @NotBlank
    String url,
    
    OffsetDateTime expiresAt
) {
}