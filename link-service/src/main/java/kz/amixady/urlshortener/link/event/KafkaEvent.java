package kz.amixady.urlshortener.link.event;

public record KafkaEvent(
        String topic,
        String key,
        String eventType,
        String eventId,
        String payload
) {
}