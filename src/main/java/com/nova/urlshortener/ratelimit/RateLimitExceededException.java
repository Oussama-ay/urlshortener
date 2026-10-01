package com.nova.urlshortener.ratelimit;

public class RateLimitExceededException extends RuntimeException {

	public RateLimitExceededException() {
		super("Too many requests");
	}
}
