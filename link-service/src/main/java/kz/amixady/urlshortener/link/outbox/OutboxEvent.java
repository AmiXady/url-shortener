package kz.amixady.urlshortener.link.outbox;

/** Событие, которое можно отправить через outbox. */
public interface OutboxEvent {

    String aggregateType();   // "link"
    String aggregateId();     // id link
    String eventType();       // "LinkDeleted"
}