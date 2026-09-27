package com.nova.urlshortener.url;

import com.nova.urlshortener.url.dto.CreateUrlRequest;
import com.nova.urlshortener.url.dto.UrlResponse;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private static final String BASE_URL = "http://localhost:8080/";

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    public UrlService(
            UrlRepository urlRepository,
            ShortCodeGenerator shortCodeGenerator
    ) {
        this.urlRepository = urlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    public UrlResponse createUrl(CreateUrlRequest request) {
        String shortCode = generateAvailableShortCode();

        Url url = new Url(request.url(), shortCode);
        urlRepository.save(url);

        return new UrlResponse(
                shortCode,
                BASE_URL + shortCode
        );
    }

    private String generateAvailableShortCode() {
        String shortCode;

        do {
            shortCode = shortCodeGenerator.generate();
        } while (urlRepository.existsByShortCode(shortCode));

        return shortCode;
    }

    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        return url.getOriginalUrl();
    }
}
