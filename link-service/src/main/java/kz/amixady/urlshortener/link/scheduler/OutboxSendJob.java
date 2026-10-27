package kz.amixady.urlshortener.link.scheduler;

import kz.amixady.urlshortener.link.outbox.OutboxKafkaSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxSendJob {

    private final OutboxKafkaSender sender;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms}")
    public void sendPending() {
        sender.sendPending();
    }
}
