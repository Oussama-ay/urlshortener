package com.nova.urlshortener.url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    
    Optional<Url> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Url u set u.clickCount = u.clickCount + 1, "
            + "u.lastAccessedAt = CURRENT_TIMESTAMP where u.shortCode = :shortCode")
    void recordClick(@Param("shortCode") String shortCode);
}
