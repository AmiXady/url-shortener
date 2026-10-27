package kz.amixady.urlshortener.link.exception;

public class OutboxSendException extends RuntimeException {

    public OutboxSendException(String message, Throwable cause) {
        super(message, cause);
    }
}