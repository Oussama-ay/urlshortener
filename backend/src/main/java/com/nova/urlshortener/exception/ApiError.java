package com.nova.urlshortener.exception;

import java.time.OffsetDateTime;

public record ApiError(int status, String message, OffsetDateTime timestamp) {
}
