package kz.amixady.urlshortener.redirect.repository;

import kz.amixady.urlshortener.redirect.model.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LinkRepository {
    private static final String FIND_BY_CODE = """
            SELECT id, original_url, expires_at, deleted_at 
            FROM links 
            WHERE code = :code
            """;

    private final JdbcClient client;

    public Optional<Link> findByCode(String code) {
        return client.sql(FIND_BY_CODE)
                .param("code", code)
                .query((rs, rowNum) -> toLink(rs))
                .optional();
    }

    private static Link toLink(ResultSet rs) throws SQLException {
        return new Link(
                rs.getLong("id"),
                rs.getString("original_url"),
                toInstant(rs.getObject("expires_at", OffsetDateTime.class)),
                toInstant(rs.getObject("deleted_at", OffsetDateTime.class)));
    }

    private static Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

}
