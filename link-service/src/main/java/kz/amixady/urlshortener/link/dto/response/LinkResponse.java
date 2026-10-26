package kz.amixady.urlshortener.link.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LinkResponse(
        String code,
        String shortUrl,
        String originalUrl,
        String token,
        Instant createdAt,
        Instant expiresAt
) {
}