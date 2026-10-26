package kz.amixady.urlshortener.link.controller;

import jakarta.validation.Valid;
import kz.amixady.urlshortener.link.dto.request.BatchCreateLinkRequest;
import kz.amixady.urlshortener.link.dto.request.CreateLinkRequest;
import kz.amixady.urlshortener.link.dto.response.BatchCreateLinkResponse;
import kz.amixady.urlshortener.link.dto.response.LinkResponse;
import kz.amixady.urlshortener.link.dto.response.LinkStatsResponse;
import kz.amixady.urlshortener.link.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class LinkController {
    private static final String TOKEN_HEADER = "X-Management-Token";

    private final LinkService service;

    @PostMapping
    public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchCreateLinkResponse> createBatch(@Valid @RequestBody BatchCreateLinkRequest request) {
        return ResponseEntity.ok(service.createBatch(request));
    }

    @GetMapping("/{code}/stats")
    public ResponseEntity<LinkStatsResponse> getStats(
            @PathVariable String code,
            @RequestHeader(name = TOKEN_HEADER, required = false) String token) {
        return ResponseEntity.ok(service.getStats(code, token));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(
            @PathVariable String code,
            @RequestHeader(name = TOKEN_HEADER, required = false) String token) {
        service.delete(code, token);
        return ResponseEntity.noContent().build();
    }
}