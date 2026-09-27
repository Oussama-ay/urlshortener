package com.nova.urlshortener.url;

import com.nova.urlshortener.url.dto.CreateUrlRequest;
import com.nova.urlshortener.url.dto.UrlResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final String baseUrl;

    public UrlService(
            UrlRepository urlRepository,
            ShortCodeGenerator shortCodeGenerator,
            @Value("${app.base-url}") String baseUrl
    ) {
        this.urlRepository = urlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.baseUrl = baseUrl;
    }

    public UrlResponse createUrl(CreateUrlRequest request) {
        String shortCode = generateAvailableShortCode();

        Url url = new Url(request.url(), shortCode);
        urlRepository.save(url);

        return new UrlResponse(
                shortCode,
                baseUrl + "/" + shortCode
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
