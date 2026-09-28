package com.nova.urlshortener.url.dto;

import java.time.OffsetDateTime;

public record CachedUrl(
        String originalUrl,
        OffsetDateTime expiresAt
) {
}
