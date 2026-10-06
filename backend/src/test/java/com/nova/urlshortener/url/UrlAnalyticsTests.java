package com.nova.urlshortener.url;

import com.nova.urlshortener.url.dto.CachedUrl;
import com.nova.urlshortener.url.dto.CreateUrlRequest;
import com.nova.urlshortener.user.User;
import com.nova.urlshortener.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UrlAnalyticsTests {
    @Autowired private UrlService service;
    @Autowired private UrlCache cache;
    @Autowired private UrlRepository repository;
    @Autowired private UserRepository userRepository;
    @Autowired private ShortCodeGenerator generator;

    @BeforeEach
    void ensureTestUser() {
        userRepository.findByEmail("analytics@example.com")
                .orElseGet(() -> userRepository.save(
                        new User("analytics@example.com", "test-password-hash")));
    }

    @Test
    void countsColdAndCachedRedirectsButNotMetadataReads() {
        String destination = "https://example.com/analytics";
        String code = service.createUrl(
            new CreateUrlRequest(destination, null),
            "analytics@example.com").shortCode();
        assertTrue(cache.get(code).isEmpty());
        assertEquals(0L, service.getUrlDetails(code, "analytics@example.com").clickCount());

        assertEquals(destination, service.getOriginalUrl(code));
        assertTrue(cache.get(code).isPresent());
        var first = service.getUrlDetails(code, "analytics@example.com");
        assertEquals(1L, first.clickCount());
        assertNotNull(first.lastAccessedAt());

        assertEquals(destination, service.getOriginalUrl(code));
        var second = service.getUrlDetails(code, "analytics@example.com");
        assertEquals(2L, second.clickCount());
        assertFalse(second.lastAccessedAt().isBefore(first.lastAccessedAt()));
        assertEquals(2L, service.getUrlDetails(code, "analytics@example.com").clickCount());
    }

    @Test
    void concurrentRedirectsDoNotLoseClicks() throws Exception {
        String code = service.createUrl(
            new CreateUrlRequest("https://example.com/concurrent", null),
            "analytics@example.com").shortCode();
        service.getOriginalUrl(code); // Warm the cache before simultaneous redirects.
        var executor = Executors.newFixedThreadPool(8);
        try {
            var requests = new ArrayList<Callable<String>>();
            for (int i = 0; i < 40; i++) {
                requests.add(() -> service.getOriginalUrl(code));
            }
            for (var result : executor.invokeAll(requests, 30, TimeUnit.SECONDS)) {
                assertEquals("https://example.com/concurrent", result.get());
            }
        } finally {
            executor.shutdownNow();
        }
        assertEquals(41L, service.getUrlDetails(code, "analytics@example.com").clickCount());
    }

    @Test
    void expiredRedirectsDoNotCountWithOrWithoutCachedValue() {
        var expiry = OffsetDateTime.now().minusMinutes(1);
        String destination = "https://example.com/expired";
        // Seed an already-expired mapping directly; creation now rejects past expiration.
        String code = generator.generate();
        repository.save(new Url(destination, code, expiry,
                userRepository.findByEmail("analytics@example.com").orElseThrow()));
        assertThrows(UrlExpiredException.class, () -> service.getOriginalUrl(code));

        cache.put(code, new CachedUrl(destination, expiry), Duration.ofMinutes(1));
        assertThrows(UrlExpiredException.class, () -> service.getOriginalUrl(code));
        assertTrue(cache.get(code).isEmpty());
        var details = service.getUrlDetails(code, "analytics@example.com");
        assertEquals(0L, details.clickCount());
        assertNull(details.lastAccessedAt());
    }

    @Test
    void missingRedirectStillReturnsNotFound() {
        assertThrows(UrlNotFoundException.class,
                () -> service.getOriginalUrl(UUID.randomUUID().toString()));
    }
}
