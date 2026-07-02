package com.vipul.urlshortener.controller;

import com.vipul.urlshortener.dto.AnalyticsResponse;
import com.vipul.urlshortener.dto.ErrorResponse;
import com.vipul.urlshortener.dto.UrlRequest;
import com.vipul.urlshortener.dto.UrlResponse;
import com.vipul.urlshortener.entity.UrlMapping;
import com.vipul.urlshortener.exception.InvalidExpiryException;
import com.vipul.urlshortener.exception.LinkExpiredException;
import com.vipul.urlshortener.service.UrlServiceImpl;
import com.vipul.urlshortener.util.ExpiryUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlServiceImpl service;

    public UrlController(UrlServiceImpl service) {
        this.service = service;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody UrlRequest request) {
        try {

            if (request.getLongUrl() == null || request.getLongUrl().trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse(
                                "VALIDATION_ERROR",
                                "URL cannot be empty"
                        ));
            }

            String shortCode = service.shortenUrl(
                    request.getLongUrl(),
                    request.getCustomCode(),
                    request.getExpiryTimeMinutes(),
                    request.getExpiryTimeHours(),
                    request.getExpiryTimeDays(),
                    request.getExpiresAtTimestamp()
            );

            String shortUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/")
                    .path(shortCode)
                    .toUriString();

            // Get created mapping to fetch expiry info
            UrlMapping mapping = service.getAnalytics(shortCode);
            String expiresAt = ExpiryUtil.formatDateTime(mapping.getExpiresAt());
            String expiryIn = mapping.getExpiresAt() != null ? ExpiryUtil.getTimeUntilExpiry(mapping.getExpiresAt()) : null;

            return ResponseEntity.ok(
                    new UrlResponse(shortUrl, shortCode, expiresAt, expiryIn)
            );

        } catch (InvalidExpiryException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(
                            "INVALID_EXPIRY",
                            e.getMessage()
                    ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(
                            "VALIDATION_ERROR",
                            e.getMessage()
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "SERVER_ERROR",
                            "An unexpected error occurred"
                    ));
        }
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {

        try {

            String longUrl = service.getLongUrl(shortCode);

            return ResponseEntity
                    .status(302)
                    .location(URI.create(longUrl))
                    .build();

        } catch (LinkExpiredException ex) {
            return ResponseEntity.notFound().build();

        } catch (RuntimeException ex) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {

        try {

            UrlMapping mapping = service.getAnalytics(shortCode);
            
            String expiresAt = ExpiryUtil.formatDateTime(mapping.getExpiresAt());
            Boolean isExpired = mapping.isExpired();
            String status = isExpired ? "expired" : "active";
            String createdAt = ExpiryUtil.formatDateTime(mapping.getCreatedAt());

            return ResponseEntity.ok(
                    new AnalyticsResponse(
                            shortCode,
                            mapping.getClickCount(),
                            expiresAt,
                            isExpired,
                            status,
                            createdAt
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.notFound().build();
        }
    }
}