package kz.amixady.urlshortener.link.exception;

public class CodeGenerationException extends RuntimeException {

    public CodeGenerationException(String originalUrl) {
        super("Не удалось сгенерировать уникальный код для URL: " + stripQuery(originalUrl));
    }

    private static String stripQuery(String url) {
        int queryStart = url.indexOf('?');
        return queryStart == -1 ? url : url.substring(0, queryStart) + "?...";
    }
}