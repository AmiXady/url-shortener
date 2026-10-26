package kz.amixady.urlshortener.redirect.service;

public sealed interface RedirectResult {

    /**
     * Ссылка действует → 302 на originalUrl
     */
    record Found(String originalUrl) implements RedirectResult {
    }

    /**
     * Ссылки с таким кодом нет → 404
     */
    record NotFound() implements RedirectResult {
    }

    /**
     * Ссылка удалена или истекла → 410
     */
    record Gone() implements RedirectResult {
    }
}