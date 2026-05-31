package com.vipul.urlshortener.service;


import com.vipul.urlshortener.entity.UrlMapping;
import com.vipul.urlshortener.repository.UrlMappingRepository;
import com.vipul.urlshortener.util.Base62Encoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlServiceImpl {

    private final UrlMappingRepository repository;

    public UrlServiceImpl(UrlMappingRepository repository) {
        this.repository = repository;
    }

    // Shorten URL with optional custom code
    public String shortenUrl(String longUrl, String customCode) {

        // Validate URL
        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
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
            
            // Save with custom code
            UrlMapping mapping = new UrlMapping(longUrl);
            mapping.setShortCode(customCode);
            repository.save(mapping);
            return customCode;
        } else {
            // Auto-generate short code
            // Check if URL already exists
            Optional<UrlMapping> existing = repository.findByLongUrl(longUrl);
            if (existing.isPresent()) {
                return existing.get().getShortCode();
            }

            // Save to get ID
            UrlMapping mapping = new UrlMapping(longUrl);
            mapping = repository.save(mapping);

            // Generate short code from ID
            String shortCode = Base62Encoder.encode(mapping.getId());

            // Update entity with generated code
            mapping.setShortCode(shortCode);
            repository.save(mapping);

            return shortCode;
        }
    }

    // Resolve short URL
    public String getLongUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(UrlMapping::getLongUrl)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));
    }
}