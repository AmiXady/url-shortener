package kz.amixady.urlshortener.stats.service;


import kz.amixady.urlshortener.stats.event.ClickEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;


@Component
public class HumanClickDetector {

    private static final Set<String> BOT_MARKERS = Set.of(
            "bot", "crawler", "spider", "preview",
            "facebookexternalhit", "whatsapp", "skypeuripreview", "vkshare",
            "curl", "wget", "python-requests", "httpclient", "okhttp", "go-http-client", "java/",
            "headless"
    );

    public boolean isHuman(ClickEvent event) {
        return isGet(event.method())
                && !event.prefetch()
                && event.hasUserAgent()
                && !looksLikeBot(event.userAgent());
    }

    private static boolean isGet(String method) {
        return "GET".equalsIgnoreCase(method);
    }

    private static boolean looksLikeBot(String userAgent) {
        String normalized = userAgent.toLowerCase(Locale.ROOT);
        return BOT_MARKERS.stream().anyMatch(normalized::contains);
    }
}
