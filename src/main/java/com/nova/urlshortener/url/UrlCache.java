package com.nova.urlshortener.url;

import java.util.Optional;

public interface UrlCache {

    Optional<String> getOriginalUrl(String shortCode);

    void putOriginalUrl(String shortCode, String originalUrl);
}
