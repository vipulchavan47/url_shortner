package com.vipul.urlshortener.dto;

public class UrlResponse {

    private String shortUrl;

    public UrlResponse(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }
}