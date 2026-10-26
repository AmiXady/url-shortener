package kz.amixady.urlshortener.link.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Недействительный токен управления");
    }
}