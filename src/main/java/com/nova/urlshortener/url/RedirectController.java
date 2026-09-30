package com.nova.urlshortener.url;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.net.URI;

@RestController
public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @Operation(
            summary = "Redirect short URL",
            description = "Redirects a short code to its original URL"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found"),
            @ApiResponse(responseCode = "410", description = "URL expired")
    })
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
