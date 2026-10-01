package com.nova.urlshortener.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRateLimiter implements RateLimiter {

	private static final String KEY_PREFIX = "rate:create-url:";
	private static final long LIMIT = 20;
	private static final Duration WINDOW = Duration.ofMinutes(1);

	private final StringRedisTemplate redisTemplate;

	public RedisRateLimiter(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public boolean allow(String clientId) {
		String key = KEY_PREFIX + clientId;

		Long count = redisTemplate.opsForValue().increment(key);

		if (count == null) {
			throw new IllegalStateException("Could not update rate limit counter");
		}

		if (count == 1) {
			redisTemplate.expire(key, WINDOW);
		}

		return count <= LIMIT;
	}
}
