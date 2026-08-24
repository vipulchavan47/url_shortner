package com.vipul.urlshortener.rateLimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private int urlCreation = 10;
    private Duration urlCreationDuration = Duration.ofMinutes(1);

    private int qrGeneration = 20;
    private Duration qrGenerationDuration = Duration.ofMinutes(1);

    private int redirect = 60;
    private Duration redirectDuration = Duration.ofMinutes(1);

    private int defaultLimit = 60;
    private Duration defaultDuration = Duration.ofMinutes(1);

    private boolean useForwardedHeaders = false;

    public int getUrlCreation() {
        return urlCreation;
    }

    public void setUrlCreation(int urlCreation) {
        this.urlCreation = urlCreation;
    }

    public Duration getUrlCreationDuration() {
        return urlCreationDuration;
    }

    public void setUrlCreationDuration(Duration urlCreationDuration) {
        this.urlCreationDuration = urlCreationDuration;
    }

    public int getQrGeneration() {
        return qrGeneration;
    }

    public void setQrGeneration(int qrGeneration) {
        this.qrGeneration = qrGeneration;
    }

    public Duration getQrGenerationDuration() {
        return qrGenerationDuration;
    }

    public void setQrGenerationDuration(Duration qrGenerationDuration) {
        this.qrGenerationDuration = qrGenerationDuration;
    }

    public int getRedirect() {
        return redirect;
    }

    public void setRedirect(int redirect) {
        this.redirect = redirect;
    }

    public Duration getRedirectDuration() {
        return redirectDuration;
    }

    public void setRedirectDuration(Duration redirectDuration) {
        this.redirectDuration = redirectDuration;
    }

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public Duration getDefaultDuration() {
        return defaultDuration;
    }

    public void setDefaultDuration(Duration defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public boolean isUseForwardedHeaders() {
        return useForwardedHeaders;
    }

    public void setUseForwardedHeaders(boolean useForwardedHeaders) {
        this.useForwardedHeaders = useForwardedHeaders;
    }
}
