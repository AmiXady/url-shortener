package kz.amixady.urlshortener.stats.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "link_clicks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkClicks {
    @Id
    private Long linkId;

    @Column(nullable = false)
    private long clicks;

    @Column(nullable = false)
    private Instant updatedAt;
}
