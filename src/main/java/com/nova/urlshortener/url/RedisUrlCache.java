package com.nova.urlshortener.url;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RedisUrlCache implements UrlCache {

    private static final String KEY_PREFIX = "url:";

    private final StringRedisTemplate redisTemplate;

    public RedisUrlCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<String> getOriginalUrl(String shortCode) {
        String value = redisTemplate.opsForValue()
                .get(KEY_PREFIX + shortCode);

        return Optional.ofNullable(value);
    }

    @Override
    public void putOriginalUrl(String shortCode, String originalUrl) {
        redisTemplate.opsForValue()
                .set(KEY_PREFIX + shortCode, originalUrl);
    }
}
