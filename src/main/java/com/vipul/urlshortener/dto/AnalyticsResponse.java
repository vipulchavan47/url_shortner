package com.vipul.urlshortener.dto;

public class AnalyticsResponse {
    private String shortCode;
    private Long clickCount;
    private String expiresAt;
    private Boolean isExpired;
    private String status;
    private String createdAt;

    public AnalyticsResponse(String shortCode, Long clickCount) {
        this.shortCode = shortCode;
        this.clickCount = clickCount;
    }

    public AnalyticsResponse(String shortCode, Long clickCount, String expiresAt, Boolean isExpired, String status, String createdAt) {
        this.shortCode = shortCode;
        this.clickCount = clickCount;
        this.expiresAt = expiresAt;
        this.isExpired = isExpired;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
