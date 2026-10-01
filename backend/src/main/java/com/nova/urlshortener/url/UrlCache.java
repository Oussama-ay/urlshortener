package com.nova.urlshortener.url;

import java.util.Optional;
import com.nova.urlshortener.url.dto.CachedUrl;
import java.time.Duration;

public interface UrlCache {

    Optional<CachedUrl> get(String shortCode);

    void put(String shortCode, CachedUrl cachedUrl, Duration ttl);

    void evict(String shortCode);
}
