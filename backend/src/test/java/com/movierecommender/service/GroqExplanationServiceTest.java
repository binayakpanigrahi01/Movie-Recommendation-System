package com.movierecommender.service;

import com.movierecommender.client.GroqClient;
import com.movierecommender.entity.Genre;
import com.movierecommender.entity.Session;
import com.movierecommender.entity.SessionPersonAnswer;
import com.movierecommender.entity.Title;
import com.movierecommender.entity.enums.AgeBand;
import com.movierecommender.entity.enums.Intensity;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.entity.enums.SessionMediaType;
import com.movierecommender.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the two behaviors Phase 6 promises: a successful Groq call's text is returned
 * keyed by title id, and an erroring one degrades to the fallback instead of failing
 * the whole batch (the "GROQ_UNREACHABLE" per-title degrade from the TRD).
 */
class GroqExplanationServiceTest {

    @Test
    void mergesSuccessAndFallbackPerTitle() {
        Genre comedy = Genre.builder().id(1L).tmdbGenreId(35).name("Comedy").mediaType(MediaType.MOVIE).build();

        Title okTitle = Title.builder().id(10L).tmdbId(100L).mediaType(MediaType.MOVIE)
                .title("Good Movie").genreIds(List.of(1L)).build();
        Title failTitle = Title.builder().id(11L).tmdbId(101L).mediaType(MediaType.MOVIE)
                .title("Bad Groq Movie").genreIds(List.of(1L)).build();

        GenreRepository genreRepository = mock(GenreRepository.class);
        when(genreRepository.findAll()).thenReturn(List.of(comedy));

        GroqClient groqClient = mock(GroqClient.class);
        when(groqClient.explain(any())).thenReturn(Mono.just("Great pick!"), Mono.error(new RuntimeException("groq down")));

        GroqExplanationService service = new GroqExplanationService(groqClient, genreRepository);

        SessionPersonAnswer host = SessionPersonAnswer.builder()
                .personIndex(0).ageBand(AgeBand.EIGHTEEN_PLUS).name("Alice")
                .moods(List.of("Laugh")).intensity(Intensity.BALANCED).build();
        Session session = Session.builder().mediaType(SessionMediaType.MOVIE).groupSize(1).build();

        MatchingEngineService.MatchResult okMatch = new MatchingEngineService.MatchResult(
                okTitle, BigDecimal.valueOf(90), Map.of(0, BigDecimal.valueOf(90)), false);
        MatchingEngineService.MatchResult failMatch = new MatchingEngineService.MatchResult(
                failTitle, BigDecimal.valueOf(80), Map.of(0, BigDecimal.valueOf(80)), false);

        Map<Long, String> explanations = service.explain(List.of(okMatch, failMatch), session, List.of(host));

        assertThat(explanations.get(10L)).isEqualTo("Great pick!");
        assertThat(explanations.get(11L)).isEqualTo("A solid pick for the whole group, scoring 80 out of 100.");
    }
}
