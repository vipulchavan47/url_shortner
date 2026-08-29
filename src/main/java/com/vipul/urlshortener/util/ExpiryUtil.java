package com.vipul.urlshortener.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ExpiryUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static LocalDateTime calculateExpiryTime(Integer minutes, Integer hours, Integer days, String timestamp) {
        if (minutes != null && minutes > 0) {
            return LocalDateTime.now().plusMinutes(minutes);
        }
        if (hours != null && hours > 0) {
            return LocalDateTime.now().plusHours(hours);
        }
        if (days != null && days > 0) {
            return LocalDateTime.now().plusDays(days);
        }
        if (timestamp != null && !timestamp.trim().isEmpty()) {
            LocalDateTime parsed;
            try {
                parsed = LocalDateTime.parse(timestamp.trim(), ISO_FORMATTER);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid timestamp format. Expected ISO 8601: yyyy-MM-ddTHH:mm:ss");
            }
            if (parsed.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Expiration date/time must be in the future");
            }
            return parsed;
        }
        return null;
    }

    public static boolean isLinkExpired(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public static String getTimeUntilExpiry(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return "never";
        }

        if (isLinkExpired(expiresAt)) {
            return "expired";
        }

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(now, expiresAt);
        long hours = ChronoUnit.HOURS.between(now, expiresAt);
        long minutes = ChronoUnit.MINUTES.between(now, expiresAt);

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return "less than a minute";
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }
}
