package kz.amixady.urlshortener.redirect.event;

import java.time.Instant;

public record LinkDeletedEvent(
        long linkId,
        String code,
        Instant deletedAt
) {

}