package kz.amixady.urlshortener.redirect.model;

import java.time.Instant;
import java.util.Objects;

public record Link(
        Long id,
        String originalUrl,
        Instant expiresAt,
        Instant deletedAt
) {

    public Link {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(originalUrl, "originalUrl");
    }

    public boolean isAvailable(Instant now) {
        return deletedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }
}
