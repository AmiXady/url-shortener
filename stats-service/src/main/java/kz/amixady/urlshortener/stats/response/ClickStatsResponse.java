package kz.amixady.urlshortener.stats.response;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClickStatsResponse(
        long linkId,
        long clicks,
        Instant updatedAt) {

    public static ClickStatsResponse empty(long linkId) {
        return new ClickStatsResponse(linkId, 0, null);
    }
}