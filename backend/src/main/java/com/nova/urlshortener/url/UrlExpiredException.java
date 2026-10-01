package com.nova.urlshortener.url;

public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String shortCode) {
        super("URL expired for short code: " + shortCode);
    }
}
