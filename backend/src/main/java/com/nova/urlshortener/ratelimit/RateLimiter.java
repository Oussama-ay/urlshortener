package com.nova.urlshortener.ratelimit;

public interface RateLimiter {

    boolean allow(String clientId);
}
