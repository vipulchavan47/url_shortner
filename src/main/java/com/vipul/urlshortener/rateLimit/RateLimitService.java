package com.vipul.urlshortener.rateLimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public RateLimitResult tryConsume(String clientIp, HttpServletRequest request) {
        RateLimitPolicy policy = determinePolicy(request);
        Bucket bucket = getOrCreateBucket(clientIp, policy);
        var probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return new RateLimitResult(true, 0, policy.name());
        }

        long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
        return new RateLimitResult(false, retryAfterSeconds, policy.name());
    }

    public RateLimitPolicy determinePolicy(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method) && isUrlCreationEndpoint(requestUri)) {
            return RateLimitPolicy.URL_CREATION;
        }

        if (isQrGenerationEndpoint(requestUri)) {
            return RateLimitPolicy.QR_GENERATION;
        }

        if (isRedirectEndpoint(requestUri)) {
            return RateLimitPolicy.REDIRECT;
        }

        return RateLimitPolicy.DEFAULT;
    }

    private Bucket getOrCreateBucket(String clientIp, RateLimitPolicy policy) {
        String bucketKey = clientIp + ":" + policy.name();
        return buckets.computeIfAbsent(bucketKey, ignored -> createBucket(policy));
    }

    private Bucket createBucket(RateLimitPolicy policy) {
        Duration duration = switch (policy) {
            case URL_CREATION -> properties.getUrlCreationDuration();
            case QR_GENERATION -> properties.getQrGenerationDuration();
            case REDIRECT -> properties.getRedirectDuration();
            case DEFAULT -> properties.getDefaultDuration();
        };

        int capacity = switch (policy) {
            case URL_CREATION -> properties.getUrlCreation();
            case QR_GENERATION -> properties.getQrGeneration();
            case REDIRECT -> properties.getRedirect();
            case DEFAULT -> properties.getDefaultLimit();
        };

        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, duration));
        return Bucket.builder().addLimit(limit).build();
    }

    private boolean isUrlCreationEndpoint(String requestUri) {
        return requestUri != null && (requestUri.equals("/api/shorten") || requestUri.endsWith("/api/shorten"));
    }

    private boolean isQrGenerationEndpoint(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        String normalized = requestUri.toLowerCase();
        return normalized.contains("/qr") || normalized.contains("/qrcode") || normalized.contains("qr") || normalized.contains("qrcode");
    }

    private boolean isRedirectEndpoint(String requestUri) {
        if (requestUri == null || requestUri.startsWith("/api/") || requestUri.equals("/")) {
            return false;
        }

        String normalized = requestUri.trim();
        return !normalized.isEmpty() && !normalized.contains(".") && normalized.split("/").length <= 2;
    }

    public record RateLimitResult(boolean allowed, long retryAfterSeconds, String policyName) {
    }
}
