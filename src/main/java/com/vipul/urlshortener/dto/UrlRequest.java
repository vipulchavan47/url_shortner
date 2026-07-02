package com.vipul.urlshortener.dto;

public class UrlRequest {

    private String longUrl;
    private String customCode;
    private Integer expiryTimeMinutes;
    private Integer expiryTimeHours;
    private Integer expiryTimeDays;
    private String expiresAtTimestamp;

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

    public Integer getExpiryTimeMinutes() {
        return expiryTimeMinutes;
    }

    public void setExpiryTimeMinutes(Integer expiryTimeMinutes) {
        this.expiryTimeMinutes = expiryTimeMinutes;
    }

    public Integer getExpiryTimeHours() {
        return expiryTimeHours;
    }

    public void setExpiryTimeHours(Integer expiryTimeHours) {
        this.expiryTimeHours = expiryTimeHours;
    }

    public Integer getExpiryTimeDays() {
        return expiryTimeDays;
    }

    public void setExpiryTimeDays(Integer expiryTimeDays) {
        this.expiryTimeDays = expiryTimeDays;
    }

    public String getExpiresAtTimestamp() {
        return expiresAtTimestamp;
    }

    public void setExpiresAtTimestamp(String expiresAtTimestamp) {
        this.expiresAtTimestamp = expiresAtTimestamp;
    }
}
