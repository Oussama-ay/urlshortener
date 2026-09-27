package com.nova.urlshortener.url;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String shortCode) {
        super("URL expired for short code: " + shortCode);
    }
}
