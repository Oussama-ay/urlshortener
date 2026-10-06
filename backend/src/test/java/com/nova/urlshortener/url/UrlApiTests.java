package com.nova.urlshortener.url;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.urlshortener.ratelimit.RateLimiter;
import com.nova.urlshortener.auth.JwtService;
import com.nova.urlshortener.user.User;
import com.nova.urlshortener.user.UserRepository;
import com.nova.urlshortener.url.dto.CreateUrlRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlApiTests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UrlRepository repository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private ShortCodeGenerator generator;
    @MockitoBean private RateLimiter rateLimiter;
    private String authorization;

    @BeforeEach
    void allowRequests() {
        when(rateLimiter.allow(anyString())).thenReturn(true);
        userRepository.findByEmail("test@example.com")
            .orElseGet(() -> userRepository.save(
                new User("test@example.com", "test-password-hash")));
        var user = org.springframework.security.core.userdetails.User
            .withUsername("test@example.com")
            .password("test-password-hash")
            .roles("USER")
            .build();
        authorization = "Bearer " + jwtService.generateToken(user);
    }

    @Test
    void protectedCreateRequiresAuthentication() throws Exception {
        mvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "hello", "example.com", "https://", "https:///path",
            "https://bad host/", "javascript:alert(1)", "ftp://example.com", "https://example.com/%zz"})
    void invalidDestinationsReturnConsistent400WithoutSaving(String destination) throws Exception {
        long before = repository.count();
        mvc.perform(post("/api/urls").header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CreateUrlRequest(destination, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
        assertEquals(before, repository.count());
    }

    @Test
    void pastExpirationReturns400WithoutSaving() throws Exception {
        long before = repository.count();
        mvc.perform(post("/api/urls").header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url":"https://example.com", "expiresAt":"2020-01-01T00:00:00Z"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Expiration must be in the future"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
        assertEquals(before, repository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "", "{}", "{\"url\":\"https://example.com\",\"expiresAt\":\"tomorrow\"}"})
    void malformedOrMissingFieldsUseApiError(String body) throws Exception {
        mvc.perform(post("/api/urls").header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://example.com/path?q=1#fragment", "HTTPS://example.com/"})
    @Transactional
    void validFutureExpirationSurvivesCacheRoundTrip(String destination) throws Exception {
        // Use ISO text like a real HTTP client; CachedUrl uses the configured Jackson mapper.
        String body = mapper.writeValueAsString(java.util.Map.of(
                "url", destination, "expiresAt", OffsetDateTime.now().plusHours(1).toString()));
        String response = mvc.perform(post("/api/urls").header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String code = mapper.readTree(response).get("shortCode").asText();
        assertEquals("test@example.com", repository.findByShortCode(code)
            .orElseThrow()
            .getUser()
            .getEmail());
        for (int i = 0; i < 2; i++) {
            mvc.perform(get("/" + code).header("Authorization", authorization))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", destination));
        }
        mvc.perform(get("/api/urls/" + code).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(2))
                .andExpect(jsonPath("$.lastAccessedAt").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void detailsRequireOwnershipWhileRedirectsRemainPublic() throws Exception {
        String destination = "https://example.com/owned";
        String response = mvc.perform(post("/api/urls")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CreateUrlRequest(destination, null))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String code = mapper.readTree(response).get("shortCode").asText();
        String otherEmail = "other@example.com";
        userRepository.findByEmail(otherEmail).orElseGet(() -> userRepository.save(
                new User(otherEmail, "test-password-hash")));
        var otherUser = org.springframework.security.core.userdetails.User
                .withUsername(otherEmail).password("test-password-hash").roles("USER").build();
        String otherAuthorization = "Bearer " + jwtService.generateToken(otherUser);

        mvc.perform(get("/api/urls/" + code).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(destination));
        mvc.perform(get("/api/urls/" + code).header("Authorization", otherAuthorization))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("URL not found for short code: " + code));
        mvc.perform(get("/api/urls/" + code)).andExpect(status().isUnauthorized());
        // Verify both cold and cached public redirects.
        for (int i = 0; i < 2; i++) {
            mvc.perform(get("/" + code)).andExpect(status().isFound())
                    .andExpect(header().string("Location", destination));
        }
        mvc.perform(get("/" + code).header("Authorization", otherAuthorization))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", destination));
        // Warming the redirect cache must not grant access to details.
        mvc.perform(get("/api/urls/" + code).header("Authorization", otherAuthorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void listUrlsFiltersOwnersAndSupportsPaginationAndSorting() throws Exception {
        User ownerA = userRepository.save(new User(UUID.randomUUID() + "@example.com", "test-password-hash"));
        User ownerB = userRepository.save(new User(UUID.randomUUID() + "@example.com", "test-password-hash"));
        for (int i = 0; i < 3; i++) {
            repository.save(new Url("https://example.com/a/" + i, generator.generate(), null, ownerA));
        }
        for (int i = 0; i < 2; i++) {
            repository.save(new Url("https://example.com/b/" + i, generator.generate(), null, ownerB));
        }
        String tokenA = authorizationFor(ownerA.getEmail());
        String tokenB = authorizationFor(ownerB.getEmail());

        mvc.perform(get("/api/urls").header("Authorization", tokenA)
                        .param("size", "10").param("sort", "originalUrl,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].originalUrl").value("https://example.com/a/0"))
                .andExpect(jsonPath("$.content[1].originalUrl").value("https://example.com/a/1"))
                .andExpect(jsonPath("$.content[2].originalUrl").value("https://example.com/a/2"))
                .andExpect(jsonPath("$.content[0].shortUrl").isNotEmpty())
                .andExpect(jsonPath("$.content[0].clickCount").value(0))
                .andExpect(jsonPath("$.content[0].createdAt").isNotEmpty());
        mvc.perform(get("/api/urls").header("Authorization", tokenB)
                        .param("sort", "originalUrl,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].originalUrl").value("https://example.com/b/0"))
                .andExpect(jsonPath("$.content[1].originalUrl").value("https://example.com/b/1"));
        mvc.perform(get("/api/urls").header("Authorization", tokenA)
                        .param("page", "0").param("size", "2").param("sort", "originalUrl,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].originalUrl").value("https://example.com/a/2"))
                .andExpect(jsonPath("$.content[1].originalUrl").value("https://example.com/a/1"));
        mvc.perform(get("/api/urls").header("Authorization", tokenA)
                        .param("page", "1").param("size", "2").param("sort", "originalUrl,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].originalUrl").value("https://example.com/a/0"));
        mvc.perform(get("/api/urls").header("Authorization", tokenA)
                        .param("page", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void listUrlsRequiresAuthenticationAndReturnsEmptyPageForNewUser() throws Exception {
        mvc.perform(get("/api/urls")).andExpect(status().isUnauthorized());
        User owner = userRepository.save(new User(UUID.randomUUID() + "@example.com", "test-password-hash"));
        mvc.perform(get("/api/urls").header("Authorization", authorizationFor(owner.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    private String authorizationFor(String email) {
        var user = org.springframework.security.core.userdetails.User
                .withUsername(email).password("test-password-hash").roles("USER").build();
        return "Bearer " + jwtService.generateToken(user);
    }

    @Test
    void missingMappingUses404ApiError() throws Exception {
        String code = UUID.randomUUID().toString();
        for (String path : new String[]{"/" + code, "/api/urls/" + code}) {
            mvc.perform(get(path).header("Authorization", authorization))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("URL not found for short code: " + code))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }
    }

    @Test
    void expiredMappingUses410ApiError() throws Exception {
        String code = generator.generate();
        repository.save(new Url("https://example.com", code, OffsetDateTime.now().minusMinutes(1)));
        mvc.perform(get("/" + code).header("Authorization", authorization))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.status").value(410))
                .andExpect(jsonPath("$.message").value("URL expired for short code: " + code))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void rateLimitedRequestUses429ApiErrorWithoutSaving() throws Exception {
        when(rateLimiter.allow(anyString())).thenReturn(false);
        long before = repository.count();
        mvc.perform(post("/api/urls").header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Too many requests"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
        assertEquals(before, repository.count());
    }
}
