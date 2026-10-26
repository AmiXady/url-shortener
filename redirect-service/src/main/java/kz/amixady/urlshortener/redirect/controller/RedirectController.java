package kz.amixady.urlshortener.redirect.controller;

import kz.amixady.urlshortener.redirect.service.RedirectResult;
import kz.amixady.urlshortener.redirect.service.RedirectService;
import kz.amixady.urlshortener.redirect.service.Visitor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private static final MediaType HTML_UTF8 = new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);
    private static final String SEC_PURPOSE_HEADER = "Sec-Purpose";
    private static final String PURPOSE_HEADER = "Purpose";

    private final RedirectService service;
    private final ErrorPages pages;


    @GetMapping("/{code:[a-zA-Z0-9_-]+}")
    public ResponseEntity<String> redirect(
            @PathVariable String code,
            HttpMethod method,
            @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent,
            @RequestHeader(name = SEC_PURPOSE_HEADER, required = false) String secPurpose,
            @RequestHeader(name = PURPOSE_HEADER, required = false) String purpose) {

        Visitor visitor = new Visitor(userAgent, method.name(), isPrefetch(secPurpose, purpose));

        return switch (service.resolve(code, visitor)) {
            case RedirectResult.Found found -> redirectTo(found.originalUrl());
            case RedirectResult.NotFound notFound -> page(HttpStatus.NOT_FOUND, pages.notFound());
            case RedirectResult.Gone gone -> page(HttpStatus.GONE, pages.gone());
        };
    }

    private static ResponseEntity<String> redirectTo(String url) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }

    private static ResponseEntity<String> page(HttpStatus status, String html) {
        return ResponseEntity.status(status)
                .contentType(HTML_UTF8)
                .body(html);
    }

    /**
     * Браузер загрузил ссылку заранее, а не по нажатию человека.
     */
    private static boolean isPrefetch(String... headers) {
        for (String header : headers) {
            if (header != null && header.toLowerCase(Locale.ROOT).contains("prefetch")) {
                return true;
            }
        }
        return false;
    }
}