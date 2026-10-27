package kz.amixady.urlshortener.link.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kz.amixady.urlshortener.link.enums.LinkStatus;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinkStatsResponse(
        String code,
        long clicks,
        LinkStatus status,
        Instant updatedAt
) {
}