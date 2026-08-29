package com.vipul.urlshortener.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExpiryUtilTest {

    @Test
    void calculateExpiryTimeReturnsNullWhenNoExpiryProvided() {
        assertNull(ExpiryUtil.calculateExpiryTime(null, null, null, null));
        assertNull(ExpiryUtil.calculateExpiryTime(0, 0, 0, null));
    }

    @Test
    void calculateExpiryTimeAddsMinutes() {
        LocalDateTime result = ExpiryUtil.calculateExpiryTime(60, null, null, null);
        assertNotNull(result);
        assertTrue(result.isAfter(LocalDateTime.now()));
    }

    @Test
    void calculateExpiryTimeParsesFutureTimestamp() {
        LocalDateTime expected = LocalDateTime.parse("2030-01-01T10:30:00");
        assertEquals(expected, ExpiryUtil.calculateExpiryTime(null, null, null, "2030-01-01T10:30:00"));
    }

    @Test
    void calculateExpiryTimeRejectsPastTimestamp() {
        String past = LocalDateTime.now().minusMinutes(5).toString();
        assertThrows(IllegalArgumentException.class,
                () -> ExpiryUtil.calculateExpiryTime(null, null, null, past));
    }

    @Test
    void calculateExpiryTimeRejectsInvalidTimestampFormat() {
        assertThrows(IllegalArgumentException.class,
                () -> ExpiryUtil.calculateExpiryTime(null, null, null, "not-a-timestamp"));
    }

    @Test
    void isLinkExpiredHandlesNullAndDateTime() {
        assertFalse(ExpiryUtil.isLinkExpired(null));
        assertTrue(ExpiryUtil.isLinkExpired(LocalDateTime.now().minusSeconds(1)));
        assertFalse(ExpiryUtil.isLinkExpired(LocalDateTime.now().plusSeconds(1)));
    }
}