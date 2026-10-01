package com.nova.urlshortener.url.dto;

import java.time.OffsetDateTime;

public record UrlDetailsResponse(
        String shortCode,
        String shortUrl,
        String originalUrl,
        Long clickCount,
        OffsetDateTime createdAt,
        OffsetDateTime lastAccessedAt,
        OffsetDateTime expiresAt
) {
}
