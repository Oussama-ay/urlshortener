package com.nova.urlshortener.auth;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookie {
    public static final String NAME = "access_token";
    private final boolean secure;
    private final String sameSite;
    private final Duration lifetime;

    public AuthCookie(@Value("${app.auth-cookie.secure:true}") boolean secure,
                      @Value("${app.auth-cookie.same-site:None}") String sameSite,
                      @Value("${jwt.expiration}") long expiration) {
        if (!("None".equals(sameSite) || "Lax".equals(sameSite) || "Strict".equals(sameSite))
                || ("None".equals(sameSite) && !secure) || expiration < 1000) {
            throw new IllegalArgumentException("Invalid authentication cookie configuration");
        }
        this.secure = secure;
        this.sameSite = sameSite;
        this.lifetime = Duration.ofMillis(expiration);
    }

    public ResponseCookie create(String token) {
        return cookie(token, lifetime);
    }

    public ResponseCookie clear() {
        return cookie("", Duration.ZERO);
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value).httpOnly(true).secure(secure)
                .sameSite(sameSite).path("/").maxAge(maxAge).build();
    }
}
