package com.nova.urlshortener.url;

public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException() {
        super("URL must be an absolute HTTP or HTTPS URL with a valid host");
    }
}
