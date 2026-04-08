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

    // 1. Shorten URL
    public String shortenUrl(String longUrl) {

        // Check if already exists
        Optional<UrlMapping> existing = repository.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            return existing.get().getShortCode();
        }

        // Save to get ID
        UrlMapping mapping = new UrlMapping(longUrl);
        mapping = repository.save(mapping);

        // Generate short code
        String shortCode = Base62Encoder.encode(mapping.getId());

        // Update entity
        mapping.setShortCode(shortCode);
        repository.save(mapping);

        return shortCode;
    }

    // Resolve short URL
    public String getLongUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(UrlMapping::getLongUrl)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));
    }
}


/* shortinign flow :
1. Check duplicate
2. Save URL → get ID
3. Encode ID → shortCode
4. Update DB
5. Return shortCode
 */