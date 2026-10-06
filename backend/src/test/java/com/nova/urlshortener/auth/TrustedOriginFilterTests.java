package com.nova.urlshortener.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class TrustedOriginFilterTests {
    private final TrustedOriginFilter filter = new TrustedOriginFilter(
            "https://frontend.example", "https://api.example", new ObjectMapper().findAndRegisterModules());

    @Test
    void rejectsMissingNullAndLookalikeOriginsOnAllWrites() throws Exception {
        for (String method : new String[]{"POST", "PUT", "PATCH", "DELETE"}) {
            for (String origin : new String[]{null, "null", "https://evil.example", "https://frontend.example.evil.example"}) {
                var request = new MockHttpServletRequest(method, "/api/auth/login");
                if (origin != null) request.addHeader("Origin", origin);
                var response = new MockHttpServletResponse();
                var called = new AtomicBoolean();
                filter.doFilter(request, response, (req, res) -> called.set(true));
                assertEquals(403, response.getStatus());
                assertFalse(called.get());
                assertTrue(response.getContentAsString().contains("Untrusted or missing request origin"));
            }
        }
    }

    @Test
    void allowsTrustedWritesAndAnonymousReadsAndPreflights() throws Exception {
        for (String origin : new String[]{"https://frontend.example", "https://api.example"}) {
            var request = new MockHttpServletRequest("POST", "/api/auth/logout");
            request.addHeader("Origin", origin);
            var called = new AtomicBoolean();
            filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> called.set(true));
            assertTrue(called.get());
        }
        for (String method : new String[]{"GET", "HEAD", "OPTIONS"}) {
            var called = new AtomicBoolean();
            filter.doFilter(new MockHttpServletRequest(method, "/abc123"),
                    new MockHttpServletResponse(), (req, res) -> called.set(true));
            assertTrue(called.get());
        }
    }
}
