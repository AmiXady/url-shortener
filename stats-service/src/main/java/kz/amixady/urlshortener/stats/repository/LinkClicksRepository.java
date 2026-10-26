package kz.amixady.urlshortener.stats.repository;


import kz.amixady.urlshortener.stats.entity.LinkClicks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface LinkClicksRepository extends JpaRepository<LinkClicks, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO link_clicks (link_id, clicks, updated_at)
            VALUES (:linkId, :clicks, :updatedAt)
            ON CONFLICT (link_id) DO UPDATE
            SET clicks     = link_clicks.clicks + EXCLUDED.clicks,
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    void addClicks(@Param("linkId") long linkId,
                   @Param("clicks") long clicks,
                   @Param("updatedAt") Instant updatedAt);
}
