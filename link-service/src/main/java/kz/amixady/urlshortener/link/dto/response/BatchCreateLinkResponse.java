package kz.amixady.urlshortener.link.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record BatchCreateLinkResponse(
        List<ItemResult> results
) {

    public enum Status {
        CREATED,
        FAILED
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ItemResult(
            int index,
            Status status,
            LinkResponse link,   // заполнено, если CREATED
            String error         // заполнено, если FAILED
    ) {
        public static ItemResult created(int index, LinkResponse link) {
            return new ItemResult(index, Status.CREATED, link, null);
        }

        public static ItemResult failed(int index, String error) {
            return new ItemResult(index, Status.FAILED, null, error);
        }
    }
}