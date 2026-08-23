package com.vipul.urlshortener.service;


import com.vipul.urlshortener.dto.AnalyticsFullResponse;
import com.vipul.urlshortener.entity.AnalyticsEvent;
import com.vipul.urlshortener.entity.UrlMapping;
import com.vipul.urlshortener.exception.InvalidExpiryException;
import com.vipul.urlshortener.exception.LinkExpiredException;
import com.vipul.urlshortener.repository.AnalyticsEventRepository;
import com.vipul.urlshortener.repository.UrlMappingRepository;
import com.vipul.urlshortener.util.Base62Encoder;
import com.vipul.urlshortener.util.ExpiryUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class UrlServiceImpl {

    private final UrlMappingRepository repository;
    private final AnalyticsEventRepository analyticsRepository;

    public UrlServiceImpl(UrlMappingRepository repository, AnalyticsEventRepository analyticsRepository) {
        this.repository = repository;
        this.analyticsRepository = analyticsRepository;
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
            mapping.setAnalyticsToken(generateAnalyticsToken());
            repository.save(mapping);
            return customCode;
        } else {
            // Auto-generate short code
            // Check if URL already exists (to avoid creating duplicates with auto-generated codes)
            Optional<UrlMapping> existing = repository.findByLongUrlAndDeletedFalse(longUrl);
            if (existing.isPresent()) {
                UrlMapping ex = existing.get();
                if (ex.getAnalyticsToken() == null || ex.getAnalyticsToken().isEmpty()) {
                    ex.setAnalyticsToken(generateAnalyticsToken());
                    repository.save(ex);
                }
                return ex.getShortCode();
            }

            // Save to get ID
            UrlMapping mapping = new UrlMapping(longUrl);
            mapping.setExpiresAt(expiresAt);
            mapping = repository.save(mapping);

            // Generate short code from ID
            String shortCode = Base62Encoder.encode(mapping.getId());

            // Update entity with generated code and analytics token
            mapping.setShortCode(shortCode);
            mapping.setAnalyticsToken(generateAnalyticsToken());
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
        Optional<UrlMapping> mapping = repository.findByShortCodeAndDeletedFalse(shortCode);
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

    // Record analytics event from HttpServletRequest
    public void recordAnalyticsEvent(String shortCode, HttpServletRequest request) {
        Optional<UrlMapping> mappingOpt = repository.findByShortCodeAndDeletedFalse(shortCode);
        if (mappingOpt.isEmpty()) return;
        UrlMapping mapping = mappingOpt.get();

        try {
            AnalyticsEvent event = new AnalyticsEvent();
            event.setUrlMapping(mapping);

            String xf = request.getHeader("X-Forwarded-For");
            String ip = (xf != null && !xf.isEmpty()) ? xf.split(",")[0].trim() : request.getRemoteAddr();
            event.setIp(ip);

            String ua = request.getHeader("User-Agent");
            event.setUserAgent(ua);

            String ref = request.getHeader("Referer");
            event.setReferrer(ref);

            analyticsRepository.save(event);
        } catch (Exception e) {
            // swallow - analytics should not break redirects
            System.err.println("Failed to record analytics event: " + e.getMessage());
        }
    }

    // Get click count for a short code
    public Long getClickCount(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(UrlMapping::getClickCount)
                .orElse(0L);
    }

    // Get analytics for a short code (by shortCode)
    public UrlMapping getAnalytics(String shortCode) {
        Optional<UrlMapping> mapping = repository.findByShortCode(shortCode);
        if (mapping.isEmpty()) {
            throw new RuntimeException("Short URL not found: " + shortCode);
        }
        return mapping.get();
    }

    // Get analytics by analytics token
    public AnalyticsFullResponse getAnalyticsByToken(String analyticsToken) {
        System.out.println("getAnalyticsByToken called with: '" + analyticsToken + "'");
        Optional<UrlMapping> mappingOpt = repository.findByAnalyticsToken(analyticsToken);
        System.out.println("repository.findByAnalyticsToken returned present? " + mappingOpt.isPresent());
        if (mappingOpt.isEmpty()) throw new RuntimeException("Analytics token not found");
        UrlMapping mapping = mappingOpt.get();

        AnalyticsFullResponse resp = new AnalyticsFullResponse();
        resp.setShortCode(mapping.getShortCode());
        resp.setOriginalUrl(mapping.getLongUrl());
        resp.setCreatedAt(mapping.getCreatedAt());
        resp.setExpiresAt(mapping.getExpiresAt());
        resp.setStatus(mapping.isExpired() ? "EXPIRED" : "ACTIVE");

        // Summary
        resp.setTotalClicks(mapping.getClickCount() != null ? mapping.getClickCount() : 0L);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime start7 = now.minusDays(6).toLocalDate().atStartOfDay(); // last 7 days inclusive

        Long today = analyticsRepository.countByUrlMappingAndOccurredAtBetween(mapping, startOfToday, now);
        Long last7 = analyticsRepository.countByUrlMappingAndOccurredAtBetween(mapping, start7, now);
        resp.setTodayClicks(today != null ? today : 0L);
        resp.setLast7DaysClicks(last7 != null ? last7 : 0L);

        // Clicks over time (daily for last 7 days)
        List<Object[]> daily = analyticsRepository.countDaily(mapping.getId(), start7, now.plusDays(1));
        List<AnalyticsFullResponse.DailyCount> dailyCounts = new ArrayList<>();
        // Build map of date->count
        Map<LocalDate, Long> dateMap = new HashMap<>();
        for (Object[] row : daily) {
            Object dayObj = row[0];
            LocalDate d;
            if (dayObj instanceof java.sql.Timestamp) {
                d = ((java.sql.Timestamp) dayObj).toLocalDateTime().toLocalDate();
            } else if (dayObj instanceof java.time.LocalDateTime) {
                d = ((java.time.LocalDateTime) dayObj).toLocalDate();
            } else if (dayObj instanceof java.sql.Date) {
                d = ((java.sql.Date) dayObj).toLocalDate();
            } else if (dayObj instanceof java.time.OffsetDateTime) {
                d = ((java.time.OffsetDateTime) dayObj).toLocalDate();
            } else {
                // fallback - try parsing ISO-like date
                String s = dayObj != null ? dayObj.toString() : "";
                d = s.length() >= 10 ? LocalDate.parse(s.substring(0, 10)) : start7.toLocalDate();
            }
            Long cnt = ((Number) row[1]).longValue();
            dateMap.put(d, cnt);
        }
        LocalDate cursor = start7.toLocalDate();
        LocalDate endDate = now.toLocalDate();
        while (!cursor.isAfter(endDate)) {
            dailyCounts.add(new AnalyticsFullResponse.DailyCount(cursor.toString(), dateMap.getOrDefault(cursor, 0L)));
            cursor = cursor.plusDays(1);
        }
        resp.setClicksOverTime(dailyCounts);

        // Device aggregation (simple UA heuristics)
        List<AnalyticsEvent> recentAll = analyticsRepository.findTop10ByUrlMappingOrderByOccurredAtDesc(mapping);
        long mobile = 0, desktop = 0, tablet = 0, unknown = 0;
        List<AnalyticsFullResponse.RecentClick> recent = new ArrayList<>();
        for (AnalyticsEvent e : recentAll) {
            String ua = e.getUserAgent() != null ? e.getUserAgent().toLowerCase() : "";
            String device = "Unknown";
            if (ua.contains("mobi") || ua.contains("android") || ua.contains("iphone")) {
                device = "Mobile"; mobile++;
            } else if (ua.contains("tablet") || ua.contains("ipad")) {
                device = "Tablet"; tablet++;
            } else if (!ua.isEmpty()) {
                device = "Desktop"; desktop++;
            } else {
                unknown++; 
            }
            String ref = e.getReferrer();
            String refLabel = classifyReferrer(ref);
            recent.add(new AnalyticsFullResponse.RecentClick(e.getOccurredAt(), refLabel, device));
        }
        resp.setDevices(Map.of("desktop", desktop, "mobile", mobile, "tablet", tablet, "unknown", unknown));

        // Referrer aggregation (simple over recent events)
        long direct = 0, google = 0, linkedin = 0, other = 0;
        List<AnalyticsEvent> allRecent = analyticsRepository.findTop10ByUrlMappingOrderByOccurredAtDesc(mapping);
        for (AnalyticsEvent e : allRecent) {
            String r = e.getReferrer();
            String label = classifyReferrer(r);
            switch (label) {
                case "Direct": direct++; break;
                case "Google": google++; break;
                case "LinkedIn": linkedin++; break;
                default: other++; break;
            }
        }
        resp.setReferrers(Map.of("direct", direct, "google", google, "linkedin", linkedin, "other", other));

        // Recent clicks
        resp.setRecentClicks(recent);

        return resp;
    }

    private String classifyReferrer(String ref) {
        if (ref == null || ref.trim().isEmpty()) return "Direct";
        String r = ref.toLowerCase();
        if (r.contains("google.")) return "Google";
        if (r.contains("linkedin.")) return "LinkedIn";
        return "Other";
    }

    private String generateAnalyticsToken() {
        // generate 24 random bytes -> base64url ~32 chars
        byte[] bytes = new byte[24];
        new java.security.SecureRandom().nextBytes(bytes);
        String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // ensure uniqueness (very unlikely collision)
        Optional<UrlMapping> exists = repository.findByAnalyticsToken(token);
        if (exists.isPresent()) return generateAnalyticsToken();
        return token;
    }

    @Transactional
    public int softDeleteExpiredLinks() {
        List<UrlMapping> expired = repository.findAllExpiredLinks();
        if (expired == null || expired.isEmpty()) return 0;
        for (UrlMapping u : expired) {
            u.setDeleted(true);
            u.setDeletedAt(LocalDateTime.now());
        }
        repository.saveAll(expired);
        return expired.size();
    }
}