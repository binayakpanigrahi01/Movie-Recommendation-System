package com.movierecommender.entity;

import com.movierecommender.entity.converter.LongListConverter;
import com.movierecommender.entity.converter.StringListConverter;
import com.movierecommender.entity.enums.AgeBand;
import com.movierecommender.entity.enums.Intensity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "session_person_answers",
    uniqueConstraints = @UniqueConstraint(name = "uk_session_person", columnNames = {"session_id", "person_index"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionPersonAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "person_index", nullable = false)
    private Integer personIndex;

    @Column(length = 100)
    private String name;

    @Column(name = "age_band")
    private AgeBand ageBand;

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(name = "moods", nullable = false, columnDefinition = "JSON")
    private List<String> moods = new ArrayList<>();

    @Builder.Default
    @Convert(converter = LongListConverter.class)
    @Column(name = "genres_up_for", nullable = false, columnDefinition = "JSON")
    private List<Long> genresUpFor = new ArrayList<>();

    @Builder.Default
    @Convert(converter = LongListConverter.class)
    @Column(name = "hard_no_genres", nullable = false, columnDefinition = "JSON")
    private List<Long> hardNoGenres = new ArrayList<>();

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(name = "languages_ok", nullable = false, columnDefinition = "JSON")
    private List<String> languagesOk = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Intensity intensity = Intensity.BALANCED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "something_like_title_id")
    private Title somethingLikeTitle;
}
