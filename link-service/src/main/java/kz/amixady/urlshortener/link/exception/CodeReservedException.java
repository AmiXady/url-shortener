package kz.amixady.urlshortener.link.exception;

import lombok.Getter;

@Getter
public class CodeReservedException extends RuntimeException {

    private final String code;

    public CodeReservedException(String code) {
        super("Код '" + code + "' зарезервирован и не может быть использован");
        this.code = code;
    }
}