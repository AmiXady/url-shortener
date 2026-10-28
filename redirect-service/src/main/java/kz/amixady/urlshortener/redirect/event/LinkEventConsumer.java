package kz.amixady.urlshortener.redirect.event;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.amixady.urlshortener.redirect.cache.LinkCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkEventConsumer {
    private static final String LINK_DELETED = "LinkDeleted";
    private static final String EVENT_TYPE_HEADER = "eventType";

    private final LinkCache linkCache;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "${app.kafka.topics.link-events}")
    public void consume(ConsumerRecord<String, String> record) {
        if (!LINK_DELETED.equals(header(record, EVENT_TYPE_HEADER))) {
            return;
        }

        LinkDeletedEvent event;

        try {
            event = objectMapper.readValue(record.value(), LinkDeletedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Битое событие, пропускаем: offset={}, value={}", record.offset(), record.value(), e);
            return;
        }

        linkCache.evict(event.code());
        log.debug("Ссылка {} убрана из кэша", event.code());
    }

    private static String header(ConsumerRecord<?, ?> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
