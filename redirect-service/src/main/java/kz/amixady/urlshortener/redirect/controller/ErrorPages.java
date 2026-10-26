package kz.amixady.urlshortener.redirect.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ErrorPages {

    private final String notFound;
    private final String gone;

    public ErrorPages(@Value("classpath:pages/not-found.html") Resource notFound,
                      @Value("classpath:pages/gone.html") Resource gone) throws IOException {
        this.notFound = notFound.getContentAsString(StandardCharsets.UTF_8);
        this.gone = gone.getContentAsString(StandardCharsets.UTF_8);
    }

    public String notFound() {
        return notFound;
    }

    public String gone() {
        return gone;
    }
}