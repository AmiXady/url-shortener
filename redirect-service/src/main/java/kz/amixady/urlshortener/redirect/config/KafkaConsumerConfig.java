package kz.amixady.urlshortener.redirect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import java.time.Duration;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        ExponentialBackOff backOff = new ExponentialBackOff(1_000, 2);
        backOff.setMaxInterval(30_000);
        backOff.setMaxElapsedTime(Duration.ofMinutes(5).toMillis());
        return new DefaultErrorHandler(backOff);
    }
}