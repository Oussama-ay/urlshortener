package com.nova.urlshortener.url;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nova.urlshortener.url.dto.CreateUrlRequest;
import com.nova.urlshortener.url.dto.UrlResponse;
import com.nova.urlshortener.url.dto.UrlDetailsResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping ("/api/urls")
public class UrlController {
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public UrlResponse createUrl(@RequestBody CreateUrlRequest request) {
        // Call the service to create a new URL
        return urlService.createUrl(request);
    }

    @GetMapping("/{shortCode}")
    public UrlDetailsResponse getUrlDetails(@PathVariable String shortCode) {
        return urlService.getUrlDetails(shortCode);
    }
}
