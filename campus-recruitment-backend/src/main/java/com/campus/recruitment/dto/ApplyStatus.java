package com.campus.recruitment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public enum ApplyStatus {
    NOT_STARTED("not_started"),
    ONGOING("ongoing"),
    EXPIRED("expired");

    private final String value;

    ApplyStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Calculate apply status based on dates.
     * If expiredAt is null, use publishedAt + 90 days as default expiry.
     */
    public static String calculate(LocalDateTime publishedAt, LocalDateTime expiredAt) {
        if (publishedAt == null) return ONGOING.value;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(publishedAt)) return NOT_STARTED.value;
        if (expiredAt != null && now.isAfter(expiredAt)) return EXPIRED.value;
        return ONGOING.value;
    }
}
