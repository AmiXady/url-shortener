package kz.amixady.urlshortener.redirect.event;

import java.time.Instant;

public record ClickEvent(
        Long linkId,
        String code,
        Instant occurredAt,
        String userAgent,
        String method,
        boolean prefetch
) {
}