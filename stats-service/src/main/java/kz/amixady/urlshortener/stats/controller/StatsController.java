package kz.amixady.urlshortener.stats.controller;


import kz.amixady.urlshortener.stats.response.ClickStatsResponse;
import kz.amixady.urlshortener.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/stats")
public class StatsController {

    private final StatsService service;

    @GetMapping("/{linkId}")
    public ResponseEntity<ClickStatsResponse> getStats(@PathVariable long linkId){
        return ResponseEntity.ok(service.getStats(linkId));
    }
}
