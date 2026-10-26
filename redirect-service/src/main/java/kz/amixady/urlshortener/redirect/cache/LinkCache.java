package kz.amixady.urlshortener.redirect.cache;


import com.fasterxml.jackson.databind.ObjectMapper;
import kz.amixady.urlshortener.redirect.model.Link;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkCache {
    private static final String KEY_PREFIX = "link:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.link-ttl}")
    private final Duration ttl;


    public Optional<Link> get(String code) {
        try {
            String json = redis.opsForValue().get(key(code));
            return json == null
                    ? Optional.empty()
                    : Optional.of(objectMapper.readValue(json, Link.class));
        } catch (Exception e) {
            log.warn("Redis read failed for code '{}': {}", code, e.getMessage());
            return Optional.empty();
        }
    }


    public void put(String code, Link link) {
        try {
            String json = objectMapper.writeValueAsString(link);
            redis.opsForValue().set(key(code), json, ttl);
        } catch (Exception e) {
            log.warn("Redis write failed for code '{}': {}", code, e.getMessage());

        }
    }

    private static String key(String code) {
        return KEY_PREFIX + code;
    }

}
