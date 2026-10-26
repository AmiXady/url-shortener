package kz.amixady.urlshortener.stats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConsumerConfig {

    /**
     * Если recordClicks упал (например, база недоступна), пачка повторяется бесконечно:
     * через 1 с, 2 с, 4 с ... но не реже раза в минуту.
     * Пропускать пачку нельзя — это была бы потеря кликов (NFR-6.2).
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxInterval(60_000L);
        return new DefaultErrorHandler(backOff);
    }
}