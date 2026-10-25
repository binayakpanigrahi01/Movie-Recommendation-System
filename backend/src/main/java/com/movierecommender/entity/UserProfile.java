package com.movierecommender.entity;

import com.movierecommender.entity.converter.LongListConverter;
import com.movierecommender.entity.converter.StringListConverter;
import com.movierecommender.entity.enums.EraPreference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Builder.Default
    @Column(name = "subtitles_ok", nullable = false)
    private boolean subtitlesOk = false;

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(name = "languages", nullable = false, columnDefinition = "JSON")
    private List<String> languages = new ArrayList<>();

    @Builder.Default
    @Convert(converter = LongListConverter.class)
    @Column(name = "favorite_genres", nullable = false, columnDefinition = "JSON")
    private List<Long> favoriteGenres = new ArrayList<>();

    @Builder.Default
    @Convert(converter = LongListConverter.class)
    @Column(name = "never_want_genres", nullable = false, columnDefinition = "JSON")
    private List<Long> neverWantGenres = new ArrayList<>();

    @Builder.Default
    @Convert(converter = LongListConverter.class)
    @Column(name = "loved_title_ids", columnDefinition = "JSON")
    private List<Long> lovedTitleIds = new ArrayList<>();

    @Builder.Default
    @Column(name = "era_preference", nullable = false)
    private EraPreference eraPreference = EraPreference.ANY_ERA;
}
