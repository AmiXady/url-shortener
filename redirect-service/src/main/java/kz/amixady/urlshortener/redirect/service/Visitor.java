package kz.amixady.urlshortener.redirect.service;

/** Данные о том, кто перешёл по ссылке. По ним stats-service отличает людей от роботов. */
public record Visitor(
        String userAgent,   // заголовок User-Agent; null — заголовка не было
        String method,      // GET — переход, HEAD — проверка ссылки
        boolean prefetch    // браузер загрузил ссылку заранее, человек ещё не нажал
) {
}