package kz.amixady.urlshortener.link.outbox;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    /**
     * Сообщения, которые пора отправить.
     */
    default List<OutboxMessage> findReadyToPublish(Instant now, Limit limit) {
        return findByStatusReadyAt(OutboxStatus.PENDING, now, limit);
    }

    /**
     * Удалить отправленные.
     */
    default int deleteSent() {
        return deleteByStatus(OutboxStatus.PUBLISHED);
    }


    @Query("""  
            SELECT m FROM OutboxMessage m
            WHERE m.status = :status
              AND m.nextAttemptAt <= :now
            ORDER BY m.createdAt
            """)
    List<OutboxMessage> findByStatusReadyAt(@Param("status") OutboxStatus status,
                                            @Param("now") Instant now,
                                            Limit limit);

    @Transactional
    @Modifying
    @Query("DELETE FROM OutboxMessage m WHERE m.status = :status")
    int deleteByStatus(@Param("status") OutboxStatus status);
}
