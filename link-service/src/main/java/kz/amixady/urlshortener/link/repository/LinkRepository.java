package kz.amixady.urlshortener.link.repository;

import kz.amixady.urlshortener.link.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;


@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByCode(String code);


    @Transactional
    @Modifying
    @Query(value = """
        DELETE FROM links
        WHERE id IN (
            SELECT id FROM links
            WHERE deleted_at < :threshold
               OR expires_at < :threshold
            LIMIT :batchSize
        )
        """, nativeQuery = true)
    int deleteBatchExpiredOrDeletedBefore(@Param("threshold") Instant threshold, @Param("batchSize") int batchSize);
}
