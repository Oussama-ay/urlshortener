package com.nova.urlshortener.url;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nova.urlshortener.ratelimit.RateLimitExceededException;
import com.nova.urlshortener.url.dto.CreateUrlRequest;
import com.nova.urlshortener.url.dto.UrlResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.nova.urlshortener.url.dto.UrlDetailsResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.nova.urlshortener.ratelimit.RateLimiter;

@RestController
@RequestMapping ("/api/urls")
public class UrlController {
	private final UrlService urlService;
	private final RateLimiter rateLimiter;

	public UrlController(
			UrlService urlService,
			RateLimiter rateLimiter) {
		this.urlService = urlService;
		this.rateLimiter = rateLimiter;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UrlResponse createUrl(
			@Valid @RequestBody CreateUrlRequest request,
			HttpServletRequest httpRequest) {

		String clientIp = httpRequest.getRemoteAddr();

		if (!rateLimiter.allow(clientIp)) {
			throw new RateLimitExceededException();
		}

		return urlService.createUrl(request);
	}

	@GetMapping("/{shortCode}")
	public UrlDetailsResponse getUrlDetails(@PathVariable String shortCode) {
		return urlService.getUrlDetails(shortCode);
	}
}
