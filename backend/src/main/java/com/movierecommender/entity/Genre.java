package com.movierecommender.entity;

import com.movierecommender.entity.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "genres",
    uniqueConstraints = @UniqueConstraint(name = "uk_genres_tmdb_media", columnNames = {"tmdb_genre_id", "media_type"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_genre_id", nullable = false)
    private Integer tmdbGenreId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;
}
