package com.vipul.urlshortener.dto;

public class AnalyticsResponse {
    private String shortCode;
    private Long clickCount;

    public AnalyticsResponse(String shortCode, Long clickCount) {
        this.shortCode = shortCode;
        this.clickCount = clickCount;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }
}
