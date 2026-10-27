package kz.amixady.urlshortener.link.outbox;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;


    public void write(OutboxEvent event){
        repository.save(OutboxMessage.create(
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                toPayload(event),
                clock.instant()
        ));
    }

    private String toPayload(OutboxEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event " + event.eventType(), e);
        }
    }

}
