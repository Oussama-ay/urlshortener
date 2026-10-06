package com.nova.urlshortener.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.urlshortener.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/** CSRF defense: browser writes must come from an explicitly trusted origin. */
public class TrustedOriginFilter extends OncePerRequestFilter {
    private final Set<String> trustedOrigins;
    private final ObjectMapper mapper;

    public TrustedOriginFilter(String frontendUrl, String backendUrl, ObjectMapper mapper) {
        this.trustedOrigins = Set.copyOf(java.util.List.of(origin(frontendUrl), origin(backendUrl)));
        this.mapper = mapper;
    }

    private static String origin(String url) {
        URI uri = URI.create(url);
        if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Trusted origin must be an HTTP(S) URL");
        }
        return uri.getScheme() + "://" + uri.getRawAuthority();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String requestOrigin = request.getHeader("Origin");
        if (!Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod())
                && (requestOrigin == null || !trustedOrigins.contains(requestOrigin))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(response.getOutputStream(), new ApiError(
                    403, "Untrusted or missing request origin", OffsetDateTime.now()));
            return;
        }
        chain.doFilter(request, response);
    }
}
