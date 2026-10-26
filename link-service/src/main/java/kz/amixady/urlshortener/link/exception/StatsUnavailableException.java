package kz.amixady.urlshortener.link.exception;

/** stats-service недоступен или ответил ошибкой. → 503 */
public class StatsUnavailableException extends RuntimeException {

    public StatsUnavailableException(String message) {
        super(message);
    }

    public StatsUnavailableException(Throwable cause) {
        super("Stats service is unavailable", cause);
    }
}