package kz.amixady.urlshortener.link.repository;

import kz.amixady.urlshortener.link.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByCode(String code);
}
