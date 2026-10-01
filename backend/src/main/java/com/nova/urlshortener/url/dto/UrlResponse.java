package com.nova.urlshortener.url.dto;

public record UrlResponse(
    String shortCode,
    String shortUrl
) {
}