package com.movierecommender.repository;

import com.movierecommender.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByTmdbKeywordId(Integer tmdbKeywordId);
    Optional<Keyword> findByName(String name);
    List<Keyword> findByIsIntensityMarkerTrue();
    boolean existsByTmdbKeywordId(Integer tmdbKeywordId);
}
