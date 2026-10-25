package com.movierecommender.service;

import com.movierecommender.client.GroqClient;
import com.movierecommender.entity.Genre;
import com.movierecommender.entity.Session;
import com.movierecommender.entity.SessionPersonAnswer;
import com.movierecommender.entity.Title;
import com.movierecommender.entity.enums.Occasion;
import com.movierecommender.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Phase 6: one Groq call per top-5 title, dispatched concurrently, each with its own timeout
 * and fallback so a slow/unreachable Groq degrades the explanation, not the whole request
 * (TRD's "GROQ_UNREACHABLE" behavior).
 */
@Service
@RequiredArgsConstructor
public class GroqExplanationService {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final GroqClient groqClient;
    private final GenreRepository genreRepository;

    public Map<Long, String> explain(List<MatchingEngineService.MatchResult> matches, Session session, List<SessionPersonAnswer> people) {
        if (matches.isEmpty()) return Map.of();

        Map<Long, String> genreNameById = genreRepository.findAll().stream()
                .collect(Collectors.toMap(Genre::getId, Genre::getName));

        List<Mono<Map.Entry<Long, String>>> calls = matches.stream()
                .map(match -> groqClient.explain(buildPrompt(match, session, people, genreNameById))
                        .timeout(TIMEOUT)
                        .onErrorReturn(fallback(match))
                        .map(text -> Map.entry(match.title().getId(), text)))
                .toList();

        return Flux.merge(calls)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .block();
    }

    private String buildPrompt(MatchingEngineService.MatchResult match, Session session,
                                List<SessionPersonAnswer> people, Map<Long, String> genreNameById) {
        Title title = match.title();
        String genres = title.getGenreIds().stream()
                .map(genreNameById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
        String occasion = formatOccasion(session.getOccasion());
        String matchedPeople = people.stream()
                .filter(p -> match.personScores().containsKey(p.getPersonIndex()))
                .sorted(Comparator.comparing((SessionPersonAnswer p) -> match.personScores().get(p.getPersonIndex())).reversed())
                .limit(2)
                .map(p -> displayName(p) + " (" + String.join("/", p.getMoods()) + ")")
                .collect(Collectors.joining(", "));

        return """
                You are recommending "%s" (%s) to a group watching for %s.
                It scored well for: %s.
                Write a friendly 2-line reason this fits the group.
                """.formatted(title.getTitle(), genres, occasion, matchedPeople);
    }

    private String displayName(SessionPersonAnswer p) {
        return p.getName() != null && !p.getName().isBlank() ? p.getName() : "Person " + (p.getPersonIndex() + 1);
    }

    private String formatOccasion(Occasion occasion) {
        if (occasion == null) return "movie night";
        return switch (occasion) {
            case FRIENDS_HANGOUT -> "a friends hangout";
            case DATE_NIGHT -> "date night";
            case FAMILY_TIME -> "family time";
            case SOLO_CHILL -> "solo chill time";
        };
    }

    private String fallback(MatchingEngineService.MatchResult match) {
        return "A solid pick for the whole group, scoring " + match.groupScore() + " out of 100.";
    }
}
