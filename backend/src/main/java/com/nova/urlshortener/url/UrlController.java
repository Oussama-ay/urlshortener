package com.nova.urlshortener.url;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nova.urlshortener.ratelimit.RateLimitExceededException;
import com.nova.urlshortener.url.dto.CreateUrlRequest;
import com.nova.urlshortener.url.dto.UrlResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

	@Operation(
			summary = "Create a short URL",
			description = "Creates a short code for an original URL"
	)
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Short URL created"),
			@ApiResponse(responseCode = "400", description = "Invalid request"),
			@ApiResponse(responseCode = "429", description = "Rate limit exceeded")
	})
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

	@Operation(
			summary = "Get URL details",
			description = "Returns URL metadata and click analytics"
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "URL found"),
			@ApiResponse(responseCode = "404", description = "Short code not found"),
			@ApiResponse(responseCode = "410", description = "URL expired")
	})
	@GetMapping("/{shortCode}")
	public UrlDetailsResponse getUrlDetails(@PathVariable String shortCode) {
		return urlService.getUrlDetails(shortCode);
	}
}
