package com.vipul.urlshortener.exception;

public class LinkExpiredException extends RuntimeException {
    private String shortCode;
    private String expiresAt;

    public LinkExpiredException(String message) {
        super(message);
    }

    public LinkExpiredException(String message, String shortCode, String expiresAt) {
        super(message);
        this.shortCode = shortCode;
        this.expiresAt = expiresAt;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
