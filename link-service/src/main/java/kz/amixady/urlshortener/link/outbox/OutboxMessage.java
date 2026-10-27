package kz.amixady.urlshortener.link.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage {
    private static final Duration BASE_BACKOFF = Duration.ofSeconds(1);
    private static final Duration MAX_BACKOFF = Duration.ofMinutes(1);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false, length = 50)
    private String aggregateType;

    @Column(nullable = false, updatable = false, length = 50)
    private String aggregateId;

    @Column(nullable = false, updatable = false, length = 100)
    private String eventType;

    @Column(nullable = false, updatable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant publishedAt;

    private OutboxMessage(String aggregateType, String aggregateId,
                          String eventType, String payload, Instant now) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = now;
        this.createdAt = now;
    }

    public static OutboxMessage create(String aggregateType,
                                       String aggregateId,
                                       String eventType,
                                       String payload,
                                       Instant now) {
        return new OutboxMessage(aggregateType, aggregateId, eventType, payload, now);
    }

    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.lastError = null;
    }

    public void markFiledAttempt(String error, Instant now, int maxAttempts) {
        this.attempts++;
        this.lastError = error;
        if (attempts >= maxAttempts) {
            this.status = OutboxStatus.FAILED;
        }
        this.nextAttemptAt = now.plus(backoff(attempts));

    }

    private static Duration backoff(int attempts) {
        long seconds = BASE_BACKOFF.toSeconds() << Math.min(attempts - 1, 6);
        return Duration.ofSeconds(Math.min(seconds, MAX_BACKOFF.toSeconds()));
    }
}