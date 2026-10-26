package kz.amixady.urlshortener.link.exception;

import lombok.Getter;

@Getter
public class LinkNotFoundException extends RuntimeException {

    private final String code;

    public LinkNotFoundException(String code) {
        super("Ссылка с кодом '" + code + "' не найдена");
        this.code = code;
    }
}