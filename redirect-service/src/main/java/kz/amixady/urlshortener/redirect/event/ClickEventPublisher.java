package kz.amixady.urlshortener.redirect.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClickEventPublisher {

    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;
    private final String topic;

    public ClickEventPublisher(KafkaTemplate<String, ClickEvent> kafkaTemplate,
                               @Value("${app.kafka.topics.clicks}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(ClickEvent event) {
        try {
            kafkaTemplate.send(topic, String.valueOf(event.linkId()), event)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.warn("Click event not delivered for code '{}': {}", event.code(), error.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.warn("Click event dropped for code '{}': {}", event.code(), e.getMessage());
        }
    }
}
