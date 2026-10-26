package kz.amixady.urlshortener.stats.service;


import kz.amixady.urlshortener.stats.entity.LinkClicks;
import kz.amixady.urlshortener.stats.event.ClickEvent;
import kz.amixady.urlshortener.stats.repository.LinkClicksRepository;
import kz.amixady.urlshortener.stats.response.ClickStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.*;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final LinkClicksRepository repository;
    private final HumanClickDetector detector;
    private final Clock clock;

    @Transactional
    public void recordClicks(List<ClickEvent> events) {
        countHumanClicksByLink(events)
                .forEach((linkId, clicks) ->
                        repository.addClicks(linkId, clicks, clock.instant()));
    }


    @Transactional(readOnly = true)
    public ClickStatsResponse getStats(long linkId) {
        Optional<LinkClicks> found = repository.findById(linkId);
        if (found.isEmpty()) {
            return ClickStatsResponse.empty(linkId);
        }

        LinkClicks counter = found.get();
        return new ClickStatsResponse(counter.getLinkId(), counter.getClicks(), counter.getUpdatedAt());
    }


    //тримап сортировка решает проблему с дедлок
    private Map<Long, Long> countHumanClicksByLink(List<ClickEvent> events) {
        return events.stream()
                .filter(detector::isHuman)
                .collect(groupingBy(ClickEvent::linkId, TreeMap::new, counting()));
    }
}
