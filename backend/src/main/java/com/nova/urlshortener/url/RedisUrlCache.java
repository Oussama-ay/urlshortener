package com.nova.urlshortener.url;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.nova.urlshortener.url.dto.CachedUrl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Optional;
import java.time.Duration;

@Component
public class RedisUrlCache implements UrlCache {

    private static final String KEY_PREFIX = "url:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisUrlCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CachedUrl> get(String shortCode) {
        String value = redisTemplate.opsForValue()
                .get(KEY_PREFIX + shortCode);

        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, CachedUrl.class));
        } catch (JsonProcessingException exception) {
            evict(shortCode);
            return Optional.empty();
        }
    }

    @Override
    public void put(String shortCode, CachedUrl cachedUrl, Duration ttl) {
        try {
            String value = objectMapper.writeValueAsString(cachedUrl);

            redisTemplate.opsForValue()
                    .set(KEY_PREFIX + shortCode, value, ttl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize cached URL", exception);
        }
    }

    @Override
    public void evict(String shortCode) {
        redisTemplate.delete(KEY_PREFIX + shortCode);
    }
}
