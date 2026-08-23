package com.vipul.urlshortener.config;

import com.vipul.urlshortener.entity.UrlMapping;
import com.vipul.urlshortener.repository.UrlMappingRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Component
public class DataInitializer {

    private final UrlMappingRepository repository;

    public DataInitializer(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        List<UrlMapping> all = repository.findAll();
        SecureRandom rnd = new SecureRandom();
        for (UrlMapping u : all) {
            if (u.getAnalyticsToken() == null || u.getAnalyticsToken().isEmpty()) {
                // generate token
                String token;
                do {
                    byte[] bytes = new byte[24];
                    rnd.nextBytes(bytes);
                    token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                } while (repository.findByAnalyticsToken(token).isPresent());
                u.setAnalyticsToken(token);
            }
        }
        repository.saveAll(all);
    }
}