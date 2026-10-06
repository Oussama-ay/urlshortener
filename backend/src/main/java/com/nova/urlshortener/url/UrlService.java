package com.nova.urlshortener.url;

import com.nova.urlshortener.url.dto.CachedUrl;
import com.nova.urlshortener.url.dto.CreateUrlRequest;
import com.nova.urlshortener.url.dto.UrlDetailsResponse;
import com.nova.urlshortener.url.dto.UrlResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.Optional;
import com.nova.urlshortener.user.User;
import com.nova.urlshortener.user.UserRepository;

import org.springframework.beans.factory.annotation.Value;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final String baseUrl;
    private final UrlCache urlCache;
    private final UserRepository userRepository;
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofHours(24);

    public UrlService(
            UrlRepository urlRepository,
            ShortCodeGenerator shortCodeGenerator,
            @Value("${app.base-url}") String baseUrl,
            UrlCache urlCache,
            UserRepository userRepository
    ) {
        this.urlRepository = urlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.baseUrl = baseUrl;
        this.urlCache = urlCache;
        this.userRepository = userRepository;
    }

    @Transactional
    public UrlResponse createUrl(CreateUrlRequest request, String authenticatedEmail) {
        validateUrl(request.url());
        if (request.expiresAt() != null && !request.expiresAt().isAfter(OffsetDateTime.now())) {
            throw new InvalidExpirationException();
        }

        User user = userRepository.findByEmail(authenticatedEmail)
            .orElseThrow(() ->
                    new IllegalStateException("Authenticated user not found"));

        String shortCode = generateAvailableShortCode();

        Url url = new Url(request.url(), shortCode, request.expiresAt(), user);
        urlRepository.save(url);

        return new UrlResponse(
                shortCode,
                baseUrl + "/" + shortCode
        );
    }

    private void validateUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUrlException();
        }

        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidUrlException();
        }

        String scheme = uri.getScheme();
        if ((!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || uri.getHost() == null) {
            throw new InvalidUrlException();
        }
    }

    private String generateAvailableShortCode() {
        String shortCode;

        do {
            shortCode = shortCodeGenerator.generate();
        } while (urlRepository.existsByShortCode(shortCode));

        return shortCode;
    }

    public String getOriginalUrl(String shortCode) {
        Optional<CachedUrl> cachedUrl = urlCache.get(shortCode);

        if (cachedUrl.isPresent()) {
            CachedUrl value = cachedUrl.get();

            if (value.expiresAt() != null &&
                    !value.expiresAt().isAfter(OffsetDateTime.now())) {
                urlCache.evict(shortCode);
                throw new UrlExpiredException(shortCode);
            }

            urlRepository.recordClick(shortCode);
            return value.originalUrl();
        }

        Url url = findValidUrl(shortCode);

        CachedUrl value = new CachedUrl(
                url.getOriginalUrl(),
                url.getExpiresAt()
        );

        Duration ttl = calculateCacheTtl(url);

        urlCache.put(shortCode, value, ttl);

        urlRepository.recordClick(shortCode);
        return url.getOriginalUrl();
    }

    private Url findValidUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (url.getExpiresAt() != null &&
                !url.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new UrlExpiredException(shortCode);
        }

        return url;
    }

    @Transactional(readOnly = true)
    public UrlDetailsResponse getUrlDetails(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        return new UrlDetailsResponse(
                url.getShortCode(),
                baseUrl + "/" + url.getShortCode(),
                url.getOriginalUrl(),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getLastAccessedAt(),
                url.getExpiresAt()
        );
    }

    private Duration calculateCacheTtl(Url url) {
        if (url.getExpiresAt() == null) {
            return DEFAULT_CACHE_TTL;
        }

        Duration remaining = Duration.between(
                OffsetDateTime.now(),
                url.getExpiresAt()
        );

        if (remaining.isNegative() || remaining.isZero()) {
            throw new UrlExpiredException(url.getShortCode());
        }

        return remaining;
    }
}
