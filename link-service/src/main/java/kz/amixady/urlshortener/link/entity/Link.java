package kz.amixady.urlshortener.link.entity;

import jakarta.persistence.*;
import kz.amixady.urlshortener.link.enums.LinkStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "links",
        uniqueConstraints = @UniqueConstraint(name = "uk_links_code", columnNames = "code")
)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "links_seq")
    @SequenceGenerator(name = "links_seq", sequenceName = "links_id_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(length = 64, updatable = false)
    private String tokenHash;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Дата истечения. null — ссылка бессрочная.
     */
    private Instant expiresAt;

    /**
     * Дата мягкого удаления. null — ссылка не удалена.
     */
    private Instant deletedAt;

    private Link(String code, String originalUrl, Instant expiresAt, String tokenHash) {
        this.code = normalizeCode(code);
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
        this.tokenHash = tokenHash;
    }

    public static Link createPermanent(String code, String originalUrl, String tokenHash) {
        return new Link(code, originalUrl, null, tokenHash);
    }

    public static Link createExpiring(String code, String originalUrl, Instant expiresAt, String tokenHash) {
        Objects.requireNonNull(expiresAt, "expiresAt обязателен для ссылки со сроком жизни");
        return new Link(code, originalUrl, expiresAt, tokenHash);
    }

    public static String normalizeCode(String code) {
        return code.toLowerCase(Locale.ROOT);
    }

    public void markDeleted(Instant now) {
        if (deletedAt == null) {
            deletedAt = now;
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public boolean isActive(Instant now) {
        return !isDeleted() && !isExpired(now);
    }

    public LinkStatus status(Instant now) {
        if (isDeleted()) {
            return LinkStatus.DELETED;
        }
        return isExpired(now) ? LinkStatus.EXPIRED : LinkStatus.ACTIVE;
    }
}