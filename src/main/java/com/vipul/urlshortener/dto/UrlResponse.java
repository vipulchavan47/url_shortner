package com.vipul.urlshortener.dto;

public class UrlResponse {

    private String shortUrl;
    private String shortCode;

    public UrlResponse(String shortUrl, String shortCode) {
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getShortCode() {
        return shortCode;
    }
}