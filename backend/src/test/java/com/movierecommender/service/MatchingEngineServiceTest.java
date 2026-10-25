package com.movierecommender.service;

import com.movierecommender.entity.Genre;
import com.movierecommender.entity.Session;
import com.movierecommender.entity.SessionPersonAnswer;
import com.movierecommender.entity.Title;
import com.movierecommender.entity.enums.AgeBand;
import com.movierecommender.entity.enums.CertificationBand;
import com.movierecommender.entity.enums.EraPreference;
import com.movierecommender.entity.enums.Intensity;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.entity.enums.SessionMediaType;
import com.movierecommender.entity.enums.TimeLimit;
import com.movierecommender.repository.GenreRepository;
import com.movierecommender.repository.KeywordRepository;
import com.movierecommender.repository.TitleRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Smallest possible check that the hard-filter + scoring wiring hasn't regressed:
 * a hard-no genre must remove a title outright, and a surviving title's score must
 * reflect its genre/mood overlap under the documented weights (35/25/15/15/10).
 */
class MatchingEngineServiceTest {

    @Test
    void excludesHardNoGenreAndScoresSurvivorByOverlap() {
        Genre comedy = Genre.builder().id(1L).tmdbGenreId(35).name("Comedy").mediaType(MediaType.MOVIE).build();
        Genre horror = Genre.builder().id(2L).tmdbGenreId(27).name("Horror").mediaType(MediaType.MOVIE).build();

        Title comedyMovie = Title.builder().id(10L).tmdbId(100L).mediaType(MediaType.MOVIE).title("Comedy Movie")
                .genreIds(List.of(1L)).keywordIds(List.of()).certificationBand(CertificationBand.U)
                .originalLanguage("en").voteAverage(BigDecimal.valueOf(7.0)).runtimeMinutes(100).build();
        Title horrorMovie = Title.builder().id(11L).tmdbId(101L).mediaType(MediaType.MOVIE).title("Horror Movie")
                .genreIds(List.of(2L)).keywordIds(List.of()).certificationBand(CertificationBand.U)
                .originalLanguage("en").voteAverage(BigDecimal.valueOf(8.0)).runtimeMinutes(100).build();

        TitleRepository titleRepository = mock(TitleRepository.class);
        GenreRepository genreRepository = mock(GenreRepository.class);
        KeywordRepository keywordRepository = mock(KeywordRepository.class);
        TitleCacheService titleCacheService = mock(TitleCacheService.class);

        when(titleRepository.findCandidates(any(), any(), any())).thenReturn(List.of(comedyMovie, horrorMovie));
        when(genreRepository.findAll()).thenReturn(List.of(comedy, horror));
        when(genreRepository.findByMediaType(MediaType.MOVIE)).thenReturn(List.of(comedy, horror));
        when(keywordRepository.findAll()).thenReturn(List.of());
        when(keywordRepository.findByIsIntensityMarkerTrue()).thenReturn(List.of());

        MatchingEngineService engine = new MatchingEngineService(titleRepository, genreRepository, keywordRepository, titleCacheService);

        SessionPersonAnswer host = SessionPersonAnswer.builder()
                .personIndex(0).ageBand(AgeBand.EIGHTEEN_PLUS).moods(List.of("Laugh"))
                .genresUpFor(List.of(1L)).hardNoGenres(List.of(2L)).languagesOk(List.of("en"))
                .intensity(Intensity.BALANCED).build();
        SessionPersonAnswer guest = SessionPersonAnswer.builder()
                .personIndex(1).ageBand(AgeBand.EIGHTEEN_PLUS).moods(List.of("Laugh"))
                .genresUpFor(List.of(1L)).hardNoGenres(List.of()).languagesOk(List.of("en"))
                .intensity(Intensity.BALANCED).build();
        Session session = Session.builder().mediaType(SessionMediaType.MOVIE).timeLimit(TimeLimit.NO_LIMIT).groupSize(2).build();

        List<MatchingEngineService.MatchResult> results = engine.match(session, List.of(host, guest), EraPreference.ANY_ERA, List.of(), Set.of());

        assertThat(results).hasSize(1);
        MatchingEngineService.MatchResult survivor = results.get(0);
        assertThat(survivor.title().getId()).isEqualTo(10L);
        assertThat(survivor.compromise()).isFalse();
        // genre 35*1.0 + mood 25*0.5 ("Comedy" hits, "Animation" doesn't) + intensity 15 + similarity 15 (no Q8) + era 10
        assertThat(survivor.personScores().get(0)).isEqualByComparingTo(BigDecimal.valueOf(87.5));
        assertThat(survivor.groupScore()).isEqualByComparingTo(BigDecimal.valueOf(91.0)); // avg 87.5 + voteAverage 7.0*0.5
    }
}
