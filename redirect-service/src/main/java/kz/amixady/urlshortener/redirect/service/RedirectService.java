package kz.amixady.urlshortener.redirect.service;

import kz.amixady.urlshortener.redirect.cache.LinkCache;
import kz.amixady.urlshortener.redirect.event.ClickEvent;
import kz.amixady.urlshortener.redirect.event.ClickEventPublisher;
import kz.amixady.urlshortener.redirect.model.Link;
import kz.amixady.urlshortener.redirect.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedirectService {

    private final LinkRepository repository;
    private final LinkCache cache;
    private final Clock clock;
    private final ClickEventPublisher publisher;

    public RedirectResult resolve(String code, Visitor visitor) {
        String normalizeCode = normalize(code);
        Instant now = clock.instant();

        Optional<Link> found = findLink(normalizeCode);
        if (found.isEmpty()) {
            return new RedirectResult.NotFound();
        }

        Link link = found.get();
        if (!link.isAvailable(now)) {
            return new RedirectResult.Gone();
        }

        publisher.publish(new ClickEvent(
                link.id(),
                normalizeCode,
                now,
                visitor.userAgent(),
                visitor.method(),
                visitor.prefetch())
        );


        return new RedirectResult.Found(link.originalUrl());
    }


    private Optional<Link> findLink(String code) {
        Optional<Link> cached = cache.get(code);

        if (cached.isPresent()) {
            return cached;
        }

        Optional<Link> fromDb = repository.findByCode(code);
        fromDb.ifPresent(link -> cache.put(code, link));

        return fromDb;
    }

    private static String normalize(String code) {
        return code.toLowerCase(Locale.ROOT);
    }
}