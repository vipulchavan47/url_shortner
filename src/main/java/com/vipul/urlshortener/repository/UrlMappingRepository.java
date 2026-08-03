package com.vipul.urlshortener.repository;

import com.vipul.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    Optional<UrlMapping> findByShortCodeAndDeletedFalse(String shortCode);

    Optional<UrlMapping> findByLongUrl(String longUrl);

    Optional<UrlMapping> findByLongUrlAndDeletedFalse(String longUrl);

    @Query("SELECT u FROM UrlMapping u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < CURRENT_TIMESTAMP AND (u.deleted = false OR u.deleted IS NULL)")
    List<UrlMapping> findAllExpiredLinks();
}