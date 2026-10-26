package kz.amixady.urlshortener.link.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchCreateLinkRequest(

        @NotEmpty(message = "Пачка не может быть пустой")
        @Size(max = 1000, message = "Не больше 1000 ссылок в пачке")
        List<CreateLinkRequest> items
) {
}