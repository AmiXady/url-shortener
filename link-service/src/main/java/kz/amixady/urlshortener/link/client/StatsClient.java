package kz.amixady.urlshortener.link.client;

import kz.amixady.urlshortener.link.exception.StatsUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class StatsClient {

    private final RestClient statsRestClient;

    public ClickStats getStats(long linkId) {
        try {
            ClickStats stats = statsRestClient.get()
                    .uri("/internal/stats/{linkId}", linkId)
                    .retrieve()
                    .body(ClickStats.class);
            if (stats == null) {
                throw new StatsUnavailableException("Empty response from stats-service");
            }
            return stats;
        } catch (RestClientException e) {
            throw new StatsUnavailableException(e);
        }
    }
}