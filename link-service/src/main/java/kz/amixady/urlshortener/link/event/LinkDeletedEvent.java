package kz.amixady.urlshortener.link.event;

import kz.amixady.urlshortener.link.outbox.OutboxEvent;

import java.time.Instant;

public record LinkDeletedEvent(
        long linkId,
        String code,
        Instant deletedAt
) implements OutboxEvent {

    @Override
    public String aggregateType() {
        return "link";
    }

    @Override
    public String aggregateId() {
        return String.valueOf(linkId);
    }

    @Override
    public String eventType() {
        return "LinkDeleted";
    }
}