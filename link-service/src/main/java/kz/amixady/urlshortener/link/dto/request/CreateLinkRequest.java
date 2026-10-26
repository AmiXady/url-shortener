package kz.amixady.urlshortener.link.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public record CreateLinkRequest(

        // обязательно
        @NotBlank(message = "URL обязателен")
        @Size(max = 2048, message = "URL не длиннее 2048 символов")
        @Pattern(regexp = "^https?://.+", message = "URL должен начинаться с http:// или https://")
        @URL(message = "Некорректный URL")
        String originalUrl,

        // необязательно: если null — код сгенерирует сервер
        @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$",
                message = "Код: 3–30 символов, латиница, цифры, '-' и '_'")
        String customCode,

        // необязательно: если null — ссылка бессрочная
        @Future(message = "Срок жизни должен быть в будущем")
        Instant expiresAt
) {

    public boolean hasCustomCode() {
        return customCode != null && !customCode.isBlank();
    }

    public boolean hasExpiration() {
        return expiresAt != null;
    }
}