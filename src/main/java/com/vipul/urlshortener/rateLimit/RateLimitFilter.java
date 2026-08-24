package com.vipul.urlshortener.rateLimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;

    public RateLimitFilter(RateLimitService rateLimitService, RateLimitProperties properties) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        RateLimitService.RateLimitResult result = rateLimitService.tryConsume(clientIp, request);

        if (!result.allowed()) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            response.setHeader("X-RateLimit-Policy", result.policyName());
            response.getWriter().write("{\"error\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests. Please retry later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (properties.isUseForwardedHeaders()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String firstForwardedIp = forwardedFor.split(",")[0].trim();
                if (!firstForwardedIp.isEmpty()) {
                    return firstForwardedIp;
                }
            }

            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}
