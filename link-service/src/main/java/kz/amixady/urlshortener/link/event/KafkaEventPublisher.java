package kz.amixady.urlshortener.link.event;

import kz.amixady.urlshortener.link.exception.EventPublishException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Синхронная отправка события в Kafka.
 */
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private static final long SEND_TIMEOUT_MS = 5_000;
    private static final String EVENT_TYPE_HEADER = "eventType";
    private static final String EVENT_ID_HEADER = "eventId";

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * @throws EventPublishException если Kafka не подтвердила получение
     */
    public void publish(KafkaEvent event) {
        try {
            kafkaTemplate.send(toRecord(event)).get(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // восстановить флаг прерывания
            throw new EventPublishException("Interrupted while publishing " + event.eventId(), e);
        } catch (ExecutionException e) {
            throw new EventPublishException(e.getCause().getMessage(), e.getCause());
        } catch (TimeoutException e) {
            throw new EventPublishException("Kafka did not respond in " + SEND_TIMEOUT_MS + " ms", e);
        }
    }

    private static ProducerRecord<String, String> toRecord(KafkaEvent event) {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(event.topic(), event.key(), event.payload());
        record.headers()
                .add(EVENT_TYPE_HEADER, bytes(event.eventType()))
                .add(EVENT_ID_HEADER, bytes(event.eventId()));
        return record;
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}