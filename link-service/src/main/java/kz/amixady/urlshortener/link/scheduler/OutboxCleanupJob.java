package kz.amixady.urlshortener.link.scheduler;

import kz.amixady.urlshortener.link.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxCleanupJob {

    private final OutboxRepository repository;

    @Scheduled(cron = "${app.outbox.cleanup-cron}")
    public void cleanUp() {
        int deleted = repository.deleteSent();
        log.info("Purged {} sent outbox messages", deleted);
    }
}