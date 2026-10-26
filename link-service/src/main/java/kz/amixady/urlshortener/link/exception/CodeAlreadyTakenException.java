package kz.amixady.urlshortener.link.exception;

import lombok.Getter;

@Getter
public class CodeAlreadyTakenException extends RuntimeException {

    private final String code;

    public CodeAlreadyTakenException(String code) {
        super("Код '" + code + "' уже занят");
        this.code = code;
    }
}