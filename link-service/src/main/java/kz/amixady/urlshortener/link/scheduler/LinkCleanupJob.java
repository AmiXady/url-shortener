package kz.amixady.urlshortener.link.scheduler;

import kz.amixady.urlshortener.link.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;


@Slf4j
@Component
@RequiredArgsConstructor
public class LinkCleanupJob {
    private static final Duration RETENTION = Duration.ofDays(365);
    private static final int BATCH_SIZE = 1_000;

    private final LinkRepository repository;
    private final Clock clock;

    @Scheduled(cron = "${app.cleanup.cron}")
    public void cleanUp() {
        Instant threshold = clock.instant().minus(RETENTION);
        long total = 0;

        int deleted;
        do {
            deleted = repository.deleteBatchExpiredOrDeletedBefore(threshold, BATCH_SIZE);
            total += deleted;
        } while (deleted == BATCH_SIZE);

        log.info("Purged {} obsolete links older than {}", total, threshold);
    }
}
