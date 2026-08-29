package com.vipul.urlshortener.service;

import com.vipul.urlshortener.entity.UrlMapping;
import com.vipul.urlshortener.exception.InvalidExpiryException;
import com.vipul.urlshortener.exception.LinkExpiredException;
import com.vipul.urlshortener.repository.UrlMappingRepository;
import com.vipul.urlshortener.util.Base62Encoder;
import com.vipul.urlshortener.util.ExpiryUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UrlServiceImpl {

    private final UrlMappingRepository repository;

    public UrlServiceImpl(UrlMappingRepository repository) {
        this.repository = repository;
    }

    // Shorten URL with optional custom code and expiry
    @Transactional
    public String shortenUrl(String longUrl, String customCode, Integer expiryTimeMinutes, Integer expiryTimeHours, Integer expiryTimeDays, String expiresAtTimestamp) {

        // Validate URL
        validateLongUrl(longUrl);

        // Calculate expiry time if provided
        LocalDateTime expiresAt;
        try {
            expiresAt = ExpiryUtil.calculateExpiryTime(expiryTimeMinutes, expiryTimeHours, expiryTimeDays, expiresAtTimestamp);
        } catch (IllegalArgumentException e) {
            throw new InvalidExpiryException(e.getMessage());
        }

        String trimmedUrl = longUrl.trim();

        // If custom code is provided, validate it
        if (customCode != null && !customCode.trim().isEmpty()) {
            customCode = customCode.trim().toLowerCase();

            // Validate custom code format (alphanumeric only)
            if (!customCode.matches("^[a-z0-9]+$")) {
                throw new IllegalArgumentException("Custom alias must contain only lowercase letters and numbers");
            }

            // Check if custom code already exists
            if (repository.findByShortCode(customCode).isPresent()) {
                throw new IllegalArgumentException("Custom alias already exists");
            }

            // Save with custom code (allow same URL with different aliases)
            UrlMapping mapping = new UrlMapping(trimmedUrl);
            mapping.setShortCode(customCode);
            mapping.setExpiresAt(expiresAt);
            repository.save(mapping);
            return customCode;
        } else {
            // Auto-generate short code
            // Reuse an existing non-expired mapping for the same URL (avoid duplicates with auto-generated codes)
            Optional<UrlMapping> existing = repository.findAllByLongUrl(trimmedUrl).stream()
                    .filter(mapping -> !mapping.isExpired())
                    .findFirst();
            if (existing.isPresent()) {
                return existing.get().getShortCode();
            }

            // Save to get ID
            UrlMapping mapping = new UrlMapping(trimmedUrl);
            mapping.setExpiresAt(expiresAt);
            mapping = repository.save(mapping);

            // Generate short code from ID
            String shortCode = Base62Encoder.encode(mapping.getId());

            // Update entity with generated code
            mapping.setShortCode(shortCode);
            repository.save(mapping);

            return shortCode;
        }
    }

    private void validateLongUrl(String longUrl) {
        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        String url = longUrl.trim();

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Only HTTP/HTTPS URLs are supported");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Invalid URL format");
        }
    }


    // Resolve short URL to its original URL
    public String getLongUrl(String shortCode) {
        Optional<UrlMapping> mapping = repository.findByShortCode(shortCode);
        if (mapping.isEmpty()) {
            throw new RuntimeException("Short URL not found: " + shortCode);
        }

        UrlMapping urlMapping = mapping.get();

        // Check if link has expired
        if (urlMapping.isExpired()) {
            throw new LinkExpiredException("This link has expired", shortCode, ExpiryUtil.formatDateTime(urlMapping.getExpiresAt()));
        }

        return urlMapping.getLongUrl();
    }

    public Optional<UrlMapping> findByShortCode(String shortCode) {
        return repository.findByShortCode(shortCode);
    }

    // Permanently delete links whose expiration time has passed
    @Transactional
    public int deleteExpiredLinks() {
        return repository.deleteExpiredLinks(LocalDateTime.now());
    }
}