package com.movierecommender.entity;

import com.movierecommender.entity.converter.CastListConverter;
import com.movierecommender.entity.converter.LongListConverter;
import com.movierecommender.entity.enums.CertificationBand;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.entity.enums.SeriesStatus;
import com.movierecommender.entity.model.CastMember;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "titles",
    indexes = {
        @Index(name = "idx_titles_certification_band", columnList = "certification_band"),
        @Index(name = "idx_titles_original_language", columnList = "original_language"),
        @Index(name = "idx_titles_runtime_minutes", columnList = "runtime_minutes"),
        @Index(name = "idx_titles_series_status", columnList = "series_status"),
        @Index(name = "idx_titles_vote_average", columnList = "vote_average"),
        @Index(name = "idx_titles_vote_count", columnList = "vote_count"),
        @Index(name = "idx_titles_media_type", columnList = "media_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Title {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_id", nullable = false, unique = true)
    private Long tmdbId;

    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path", length = 255)
    private String posterPath;

    @Column(name = "backdrop_path", length = 255)
    private String backdropPath;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    @Column(name = "series_status")
    private SeriesStatus seriesStatus;

    @Column(name = "original_language", length = 10)
    private String originalLanguage;

    @Column(name = "vote_average", precision = 3, scale = 1)
    private BigDecimal voteAverage;

    @Builder.Default
    @Column(name = "vote_count")
    private Integer voteCount = 0;

    @Column(name = "certification_raw", length = 20)
    private String certificationRaw;

    @Column(name = "certification_band")
    private CertificationBand certificationBand;

    @Builder.Default
    @Convert(converter = LongListConverter.class)
    @Column(name = "genre_ids", columnDefinition = "JSON")
    private List<Long> genreIds = new ArrayList<>();

    @Builder.Default
    @Convert(converter = LongListConverter.class)
    @Column(name = "keyword_ids", columnDefinition = "JSON")
    private List<Long> keywordIds = new ArrayList<>();

    @Builder.Default
    @Convert(converter = CastListConverter.class)
    @Column(name = "cast", columnDefinition = "JSON")
    private List<CastMember> cast = new ArrayList<>();

    @Column(name = "imdb_id", length = 20)
    private String imdbId;

    @Column(name = "imdb_rating", precision = 3, scale = 1)
    private BigDecimal imdbRating;

    @Column(name = "rotten_tomatoes_rating")
    private Integer rottenTomatoesRating;

    @Column(name = "metacritic_rating")
    private Integer metacriticRating;

    @Column(name = "cached_at", nullable = false, updatable = false)
    private LocalDateTime cachedAt;

    @Column(name = "refreshed_at", nullable = false)
    private LocalDateTime refreshedAt;

    @PrePersist
    protected void onCreate() {
        if (cachedAt == null) {
            cachedAt = LocalDateTime.now();
        }
        if (refreshedAt == null) {
            refreshedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        refreshedAt = LocalDateTime.now();
    }
}
