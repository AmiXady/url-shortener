package kz.amixady.urlshortener.stats.event;

import kz.amixady.urlshortener.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickEventConsumer {
    private final StatsService service;

    @KafkaListener(topics = "${app.kafka.topics.clicks}")
    public void consume(List<ClickEvent> batch) {
        List<ClickEvent> events = batch.stream()
                .filter(Objects::nonNull)
                .toList();

        int skipped = batch.size() - events.size();
        if (skipped > 0) {
            log.warn("Skipped {} unreadable click events", skipped);
        }

        service.recordClicks(events);
    }

}
