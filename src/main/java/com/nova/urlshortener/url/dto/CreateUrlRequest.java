package com.nova.urlshortener.url.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUrlRequest(
    @NotBlank
    String url
) {
}