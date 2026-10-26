package kz.amixady.urlshortener.stats.event;

import java.time.Instant;
import java.util.Objects;

public record ClickEvent(
        Long linkId,
        String code,
        Instant occurredAt,
        String userAgent,
        String method,
        boolean prefetch
) {

    public ClickEvent {
        Objects.requireNonNull(linkId, "linkId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(method, "method");
    }

    public boolean hasUserAgent(){
        return userAgent != null && !userAgent.isBlank();
    }
}