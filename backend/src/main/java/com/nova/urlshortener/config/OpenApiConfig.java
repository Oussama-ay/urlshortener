package com.nova.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI urlShortenerOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("URL Shortener API")
						.description(
								"URL shortening service with PostgreSQL, Redis caching, expiration, analytics and rate limiting"
						)
						.version("1.0"));
	}
}
