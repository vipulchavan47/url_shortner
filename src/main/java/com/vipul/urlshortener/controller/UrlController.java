package com.vipul.urlshortener.controller;

import com.vipul.urlshortener.dto.UrlRequest;
import com.vipul.urlshortener.dto.UrlResponse;
import com.vipul.urlshortener.service.UrlServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/")
public class UrlController {

    private final UrlServiceImpl service;

    public UrlController(UrlServiceImpl service) {
        this.service = service;
    }

    // 1. Shorten URL (POST)
    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@RequestBody UrlRequest request) {

        String shortCode = service.shortenUrl(request.getLongUrl());

        String shortUrl = "http://localhost:8080/" + shortCode;

        return ResponseEntity.ok(new UrlResponse(shortUrl));
    }

    // 2. Redirect (GET)
    @GetMapping("/{shortCode}")
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