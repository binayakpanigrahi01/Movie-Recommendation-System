package com.movierecommender.repository;

import com.movierecommender.entity.Genre;
import com.movierecommender.entity.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    List<Genre> findByMediaType(MediaType mediaType);
    Optional<Genre> findByTmdbGenreIdAndMediaType(Integer tmdbGenreId, MediaType mediaType);
    boolean existsByTmdbGenreIdAndMediaType(Integer tmdbGenreId, MediaType mediaType);
}
