package com.vipul.urlshortener.dto;

public class UrlResponse {

    private String shortUrl;
    private String shortCode;
    private String expiresAt;
    private String expiryIn;

    public UrlResponse(String shortUrl, String shortCode) {
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
    }

    public UrlResponse(String shortUrl, String shortCode, String expiresAt, String expiryIn) {
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
        this.expiresAt = expiresAt;
        this.expiryIn = expiryIn;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getExpiryIn() {
        return expiryIn;
    }

    public void setExpiryIn(String expiryIn) {
        this.expiryIn = expiryIn;
    }
}