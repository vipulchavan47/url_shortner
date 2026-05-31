package com.vipul.urlshortener.dto;

public class UrlRequest {

    private String longUrl;
    private String customCode;

    public UrlRequest() {
    }

    public UrlRequest(String longUrl) {
        this.longUrl = longUrl;
    }

    public UrlRequest(String longUrl, String customCode) {
        this.longUrl = longUrl;
        this.customCode = customCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getCustomCode() {
        return customCode;
    }

    public void setCustomCode(String customCode) {
        this.customCode = customCode;
    }
}
