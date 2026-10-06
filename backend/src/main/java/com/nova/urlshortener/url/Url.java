package com.nova.urlshortener.url;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import com.nova.urlshortener.user.User;

@Entity
@Table(name = "urls")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 16)
    private String shortCode;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = true)
    private OffsetDateTime expiresAt;

    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

    @Column(name = "last_accessed_at")
    private OffsetDateTime lastAccessedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    public User getUser() {
        return user;
    }

    protected Url() {
    }

    public Url(String originalUrl, String shortCode, OffsetDateTime expiresAt, User user) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    public Url(String originalUrl, String shortCode, OffsetDateTime expiresAt) {
        this(originalUrl, shortCode, expiresAt, null);
    }

    // getters
    public Long getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public OffsetDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

}
