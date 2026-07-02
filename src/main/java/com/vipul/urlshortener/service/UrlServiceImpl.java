package com.vipul.urlshortener.service;


import com.vipul.urlshortener.entity.UrlMapping;
import com.vipul.urlshortener.exception.InvalidExpiryException;
import com.vipul.urlshortener.exception.LinkExpiredException;
import com.vipul.urlshortener.repository.UrlMappingRepository;
import com.vipul.urlshortener.util.Base62Encoder;
import com.vipul.urlshortener.util.ExpiryUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlServiceImpl {

    private final UrlMappingRepository repository;

    public UrlServiceImpl(UrlMappingRepository repository) {
        this.repository = repository;
    }

    // Shorten URL with optional custom code and expiry
    public String shortenUrl(String longUrl, String customCode, Integer expiryTimeMinutes, Integer expiryTimeHours, Integer expiryTimeDays, String expiresAtTimestamp) {

        // Validate URL
        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        // Calculate expiry time if provided
        LocalDateTime expiresAt = null;
        try {
            expiresAt = ExpiryUtil.calculateExpiryTime(expiryTimeMinutes, expiryTimeHours, expiryTimeDays, expiresAtTimestamp);
        } catch (IllegalArgumentException e) {
            throw new InvalidExpiryException(e.getMessage());
        }

        // If custom code is provided, validate it
        if (customCode != null && !customCode.trim().isEmpty()) {
            customCode = customCode.trim().toLowerCase();
            
            // Check if custom code already exists
            if (repository.findByShortCode(customCode).isPresent()) {
                throw new IllegalArgumentException("Custom alias already exists");
            }
            
            // Validate custom code format (alphanumeric only)
            if (!customCode.matches("^[a-z0-9]+$")) {
                throw new IllegalArgumentException("Custom alias must contain only lowercase letters and numbers");
            }
            
            // Save with custom code (allow same URL with different aliases)
            UrlMapping mapping = new UrlMapping(longUrl);
            mapping.setShortCode(customCode);
            mapping.setExpiresAt(expiresAt);
            repository.save(mapping);
            return customCode;
        } else {
            // Auto-generate short code
            // Check if URL already exists (to avoid creating duplicates with auto-generated codes)
            Optional<UrlMapping> existing = repository.findByLongUrl(longUrl);
            if (existing.isPresent()) {
                return existing.get().getShortCode();
            }

            // Save to get ID
            UrlMapping mapping = new UrlMapping(longUrl);
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

    // Backward compatibility - old method without expiry
    public String shortenUrl(String longUrl, String customCode) {
        return shortenUrl(longUrl, customCode, null, null, null, null);
    }

    // Resolve short URL and increment click count
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
        
        // Increment click count
        Long currentCount = urlMapping.getClickCount() != null ? urlMapping.getClickCount() : 0L;
        urlMapping.setClickCount(currentCount + 1);
        repository.save(urlMapping);
        
        return urlMapping.getLongUrl();
    }

    // Get click count for a short code
    public Long getClickCount(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(UrlMapping::getClickCount)
                .orElse(0L);
    }

    // Get analytics for a short code
    public UrlMapping getAnalytics(String shortCode) {
        Optional<UrlMapping> mapping = repository.findByShortCode(shortCode);
        if (mapping.isEmpty()) {
            throw new RuntimeException("Short URL not found: " + shortCode);
        }
        return mapping.get();
    }
}