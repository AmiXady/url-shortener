package kz.amixady.urlshortener.link.mapper;

import kz.amixady.urlshortener.link.dto.response.LinkResponse;
import kz.amixady.urlshortener.link.entity.Link;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {
    private final String baseUrl;

    public LinkMapper(@Value("${app.short-link.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public LinkResponse toResponse(Link link, String token) {
        return new LinkResponse(
                link.getCode(),
                toShortUrl(link.getCode()),
                link.getOriginalUrl(),
                token,
                link.getCreatedAt(),
                link.getExpiresAt()
        );
    }

    private String toShortUrl(String code) {
        return baseUrl + "/" + code;
    }
}