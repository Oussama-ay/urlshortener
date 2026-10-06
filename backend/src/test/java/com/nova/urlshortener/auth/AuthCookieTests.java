package com.nova.urlshortener.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthCookieTests {
    @Test
    void productionCookieIsSecureHttpOnlyAndClearsWithMatchingScope() {
        AuthCookie cookies = new AuthCookie(true, "None", 86400000);
        var login = cookies.create("signed-token");
        assertTrue(login.isHttpOnly());
        assertTrue(login.isSecure());
        assertEquals("None", login.getSameSite());
        assertEquals("/", login.getPath());
        assertEquals(86400, login.getMaxAge().getSeconds());
        var logout = cookies.clear();
        assertEquals(login.getName(), logout.getName());
        assertEquals(login.getPath(), logout.getPath());
        assertEquals(login.getSameSite(), logout.getSameSite());
        assertEquals(login.isSecure(), logout.isSecure());
        assertTrue(logout.getValue().isEmpty());
        assertEquals(0, logout.getMaxAge().getSeconds());
    }

    @Test
    void rejectsUnsafeSameSiteNoneAndInvalidLifetimes() {
        assertThrows(IllegalArgumentException.class, () -> new AuthCookie(false, "None", 86400000));
        assertThrows(IllegalArgumentException.class, () -> new AuthCookie(true, "Unknown", 86400000));
        assertThrows(IllegalArgumentException.class, () -> new AuthCookie(true, "None", 0));
    }
}
