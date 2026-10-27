package kz.amixady.urlshortener.link.outbox;

import kz.amixady.urlshortener.link.event.KafkaEvent;
import kz.amixady.urlshortener.link.event.KafkaEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;

@Slf4j
@Component
public class OutboxKafkaSender {

    private static final String TOPIC_SUFFIX = "-events";

    private final OutboxRepository repository;
    private final KafkaEventPublisher publisher;
    private final Clock clock;
    private final int batchSize;
    private final int maxAttempts;

    public OutboxKafkaSender(OutboxRepository repository,
                             KafkaEventPublisher publisher,
                             Clock clock,
                             @Value("${app.outbox.batch-size}") int batchSize,
                             @Value("${app.outbox.max-attempts}") int maxAttempts) {
        this.repository = repository;
        this.publisher = publisher;
        this.clock = clock;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    public void sendPending() {
        List<OutboxMessage> batch =
                repository.findReadyToPublish(clock.instant(), Limit.of(batchSize));
        for (OutboxMessage message : batch) {
            send(message);
        }
    }


    private void send(OutboxMessage message) {
        try {
            publisher.publish(toKafkaEvent(message));
            message.markPublished(clock.instant());
        } catch (RuntimeException e) {
            message.markFiledAttempt(e.getMessage(), clock.instant(), maxAttempts);
            log.warn("Outbox message {} ({}) not sent, attempt {}/{}: {}",
                    message.getId(), message.getEventType(), message.getAttempts(), maxAttempts, e.getMessage());
        }
        repository.save(message);
    }

    private static KafkaEvent toKafkaEvent(OutboxMessage message) {
        return new KafkaEvent(
                message.getAggregateType() + TOPIC_SUFFIX,
                message.getAggregateId(),
                message.getEventType(),
                message.getId().toString(),
                message.getPayload());
    }
}
