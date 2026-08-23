package com.vipul.urlshortener.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnalyticsFullResponse {

    private String shortCode;
    private String originalUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String status;

    private Long totalClicks;
    private Long todayClicks;
    private Long last7DaysClicks;

    private List<DailyCount> clicksOverTime;
    private Map<String, Long> devices;
    private Map<String, Long> referrers;
    private List<RecentClick> recentClicks;

    public static class DailyCount {
        private String date;
        private Long clicks;

        public DailyCount() {}
        public DailyCount(String date, Long clicks) { this.date = date; this.clicks = clicks; }
        public String getDate() { return date; }
        public Long getClicks() { return clicks; }
        public void setDate(String date) { this.date = date; }
        public void setClicks(Long clicks) { this.clicks = clicks; }
    }

    public static class RecentClick {
        private LocalDateTime occurredAt;
        private String referrer;
        private String device;

        public RecentClick() {}
        public RecentClick(LocalDateTime occurredAt, String referrer, String device) { this.occurredAt = occurredAt; this.referrer = referrer; this.device = device; }
        public LocalDateTime getOccurredAt() { return occurredAt; }
        public String getReferrer() { return referrer; }
        public String getDevice() { return device; }
        public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
        public void setReferrer(String referrer) { this.referrer = referrer; }
        public void setDevice(String device) { this.device = device; }
    }

    // Getters and setters
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(Long totalClicks) { this.totalClicks = totalClicks; }
    public Long getTodayClicks() { return todayClicks; }
    public void setTodayClicks(Long todayClicks) { this.todayClicks = todayClicks; }
    public Long getLast7DaysClicks() { return last7DaysClicks; }
    public void setLast7DaysClicks(Long last7DaysClicks) { this.last7DaysClicks = last7DaysClicks; }

    public List<DailyCount> getClicksOverTime() { return clicksOverTime; }
    public void setClicksOverTime(List<DailyCount> clicksOverTime) { this.clicksOverTime = clicksOverTime; }

    public Map<String, Long> getDevices() { return devices; }
    public void setDevices(Map<String, Long> devices) { this.devices = devices; }

    public Map<String, Long> getReferrers() { return referrers; }
    public void setReferrers(Map<String, Long> referrers) { this.referrers = referrers; }

    public List<RecentClick> getRecentClicks() { return recentClicks; }
    public void setRecentClicks(List<RecentClick> recentClicks) { this.recentClicks = recentClicks; }
}