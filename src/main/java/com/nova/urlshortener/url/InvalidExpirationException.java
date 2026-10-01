package com.nova.urlshortener.url;

public class InvalidExpirationException extends RuntimeException {
    public InvalidExpirationException() {
        super("Expiration must be in the future");
    }
}
