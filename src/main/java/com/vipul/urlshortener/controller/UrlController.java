package com.vipul.urlshortener.controller;

import com.vipul.urlshortener.dto.ErrorResponse;
import com.vipul.urlshortener.dto.UrlRequest;
import com.vipul.urlshortener.dto.UrlResponse;
import com.vipul.urlshortener.service.UrlServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlServiceImpl service;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    public UrlController(UrlServiceImpl service) {
        this.service = service;
    }

    // API endpoint for shortening URL
    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody UrlRequest request) {
        try {
            // Validate request
            if (request.getLongUrl() == null || request.getLongUrl().trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("VALIDATION_ERROR", "URL cannot be empty"));
            }

            // Shorten URL with optional custom code
            String shortCode = service.shortenUrl(
                    request.getLongUrl(),
                    request.getCustomCode()
            );

            // Build full short URL
            String shortUrl = baseUrl + "/" + shortCode;

            return ResponseEntity.ok(new UrlResponse(shortUrl, shortCode));

        } catch (IllegalArgumentException e) {
            // Handle validation errors (duplicate alias, invalid format, etc.)
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));

        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("SERVER_ERROR", "An unexpected error occurred"));
        }
    }

    // Redirect endpoint for short URLs - only match alphanumeric codes
    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        try {
            String longUrl = service.getLongUrl(shortCode);

            return ResponseEntity
                    .status(302)
                    .location(URI.create(longUrl))
                    .build();

        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}