package kz.amixady.urlshortener.link.client;

import java.time.Instant;

public record ClickStats(
        long linkId,
        long clicks,
        Instant updatedAt
) {
}