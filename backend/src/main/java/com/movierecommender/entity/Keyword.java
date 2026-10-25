package com.movierecommender.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keywords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_keyword_id", nullable = false, unique = true)
    private Integer tmdbKeywordId;

    @Column(nullable = false, length = 50)
    private String name;

    @Builder.Default
    @Column(name = "is_intensity_marker", nullable = false)
    private boolean isIntensityMarker = false;
}
