package com.movierecommender.service;

import com.movierecommender.entity.Genre;
import com.movierecommender.entity.Keyword;
import com.movierecommender.entity.Session;
import com.movierecommender.entity.SessionPersonAnswer;
import com.movierecommender.entity.Title;
import com.movierecommender.entity.enums.CertificationBand;
import com.movierecommender.entity.enums.EraPreference;
import com.movierecommender.entity.enums.Intensity;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.entity.enums.SeriesStatus;
import com.movierecommender.entity.enums.SessionMediaType;
import com.movierecommender.entity.enums.TimeLimit;
import com.movierecommender.repository.GenreRepository;
import com.movierecommender.repository.KeywordRepository;
import com.movierecommender.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Average without misery" matching engine (Forms Spec: how answers combine for a group).
 * Hard filters run first to build a candidate pool, then a per-person 0-100 score is computed
 * from plain Java (not SQL) because the weights are expected to change after testing, then the
 * misery threshold loop (40 -> 30 -> 20) picks which survivors are shown.
 */
@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private static final int[] MISERY_THRESHOLDS = {40, 30, 20};
    private static final int MIN_SURVIVORS_TARGET = 5;

    private static final Map<String, List<String>> MOVIE_MOOD_GENRES = Map.of(
            "Laugh", List.of("Comedy", "Animation"),
            "Thrill", List.of("Action", "Thriller", "Crime"),
            "Feel something", List.of("Drama", "Romance"),
            "Think", List.of("Mystery", "Science Fiction", "Documentary"),
            "Get scared", List.of("Horror", "Thriller"),
            "Comfort", List.of("Comedy", "Family", "Animation"),
            "Adventure", List.of("Adventure", "Fantasy", "Science Fiction"),
            "Romance", List.of("Romance", "Comedy")
    );

    private static final Map<String, List<String>> TV_MOOD_GENRES = Map.of(
            "Laugh", List.of("Comedy", "Animation"),
            "Thrill", List.of("Action & Adventure", "Crime"),
            "Feel something", List.of("Drama"),
            "Think", List.of("Mystery", "Sci-Fi & Fantasy", "Documentary"),
            "Get scared", List.of("Mystery"),
            "Comfort", List.of("Comedy", "Family"),
            "Adventure", List.of("Action & Adventure", "Sci-Fi & Fantasy"),
            "Romance", List.of("Drama", "Comedy")
    );

    private static final Map<String, String> TV_MOOD_KEYWORDS = Map.of(
            "Get scared", "horror",
            "Romance", "romance"
    );

    private final TitleRepository titleRepository;
    private final GenreRepository genreRepository;
    private final KeywordRepository keywordRepository;
    private final TitleCacheService titleCacheService;

    public record MatchResult(Title title, BigDecimal groupScore, Map<Integer, BigDecimal> personScores, boolean compromise) {}

    private record ScoredTitle(Title title, Map<Integer, BigDecimal> personScores, BigDecimal minScore, BigDecimal groupScore) {}

    public List<MatchResult> match(Session session, List<SessionPersonAnswer> people, EraPreference hostEraPreference,
                                    List<Long> hostLovedTitleIds, Set<Long> excludeTitleIds) {
        Map<Long, String> genreNameById = genreRepository.findAll().stream()
                .collect(Collectors.toMap(Genre::getId, Genre::getName));
        Map<Long, String> keywordNameById = keywordRepository.findAll().stream()
                .collect(Collectors.toMap(Keyword::getId, Keyword::getName));

        CertificationBand ceiling = people.stream()
                .map(SessionPersonAnswer::getAgeBand)
                .filter(Objects::nonNull)
                .map(AgeBandMapper::certificationCeiling)
                .min(Comparator.comparingInt(Enum::ordinal))
                .orElse(CertificationBand.A);
        List<CertificationBand> allowedBands = Arrays.stream(CertificationBand.values())
                .filter(b -> b.ordinal() <= ceiling.ordinal())
                .toList();

        List<String> languages = people.stream()
                .map(SessionPersonAnswer::getLanguagesOk)
                .filter(l -> l != null && !l.isEmpty())
                .reduce((a, b) -> {
                    List<String> intersection = new ArrayList<>(a);
                    intersection.retainAll(b);
                    return intersection;
                })
                .orElse(List.of());

        Set<String> hardNoNames = new HashSet<>();
        for (SessionPersonAnswer p : people) {
            hardNoNames.addAll(resolveNames(p.getHardNoGenres(), genreNameById));
        }

        Set<String> wantedNames = new HashSet<>();
        for (SessionPersonAnswer p : people) {
            wantedNames.addAll(resolveNames(p.getGenresUpFor(), genreNameById));
        }
        wantedNames.removeAll(hardNoNames);

        boolean anyLight = people.stream().anyMatch(p -> p.getIntensity() == Intensity.LIGHT);
        boolean anyIntense = people.stream().anyMatch(p -> p.getIntensity() == Intensity.INTENSE);
        Set<Long> intensityMarkerIds = keywordRepository.findByIsIntensityMarkerTrue().stream()
                .map(Keyword::getId).collect(Collectors.toSet());

        if (languages.isEmpty()) {
            return List.of();
        }

        Set<Long> seenIds = new LinkedHashSet<>();
        List<Title> pool = new ArrayList<>();
        for (MediaType mediaType : resolveMediaTypes(session.getMediaType())) {
            List<Title> candidates = titleRepository.findCandidates(mediaType, allowedBands, languages);
            // Always replenish: the cache is shared across sessions, so "enough titles" doesn't mean enough of the *wanted genres*.
            // Cheap after the first run (already-hydrated titles are skipped).
            replenish(mediaType, session, languages, hardNoNames, wantedNames, anyLight, anyIntense);
            candidates = titleRepository.findCandidates(mediaType, allowedBands, languages);
            for (Title t : candidates) {
                if (seenIds.add(t.getId())) pool.add(t);
            }
        }

        List<Title> survivors = pool.stream()
                .filter(t -> !excludeTitleIds.contains(t.getId()))
                .filter(t -> passesGenreAndIntensity(t, hardNoNames, anyLight, anyIntense, intensityMarkerIds, genreNameById))
                .filter(t -> passesTimeLimit(t, session.getTimeLimit()))
                .toList();

        Set<String> hostLovedSignals = new HashSet<>();
        if (hostLovedTitleIds != null) {
            for (Long titleId : hostLovedTitleIds) {
                Title loved = titleCacheService.getDetail(titleId);
                hostLovedSignals.addAll(resolveNames(loved.getGenreIds(), genreNameById));
                hostLovedSignals.addAll(resolveNames(loved.getKeywordIds(), keywordNameById));
            }
        }

        List<ScoredTitle> scored = survivors.stream()
                .map(t -> scoreTitle(t, people, hostEraPreference, hostLovedSignals, genreNameById, keywordNameById))
                .toList();

        List<ScoredTitle> chosen = List.of();
        for (int threshold : MISERY_THRESHOLDS) {
            List<ScoredTitle> atThreshold = scored.stream()
                    .filter(s -> s.minScore().compareTo(BigDecimal.valueOf(threshold)) >= 0)
                    .toList();
            if (atThreshold.size() >= MIN_SURVIVORS_TARGET || threshold == MISERY_THRESHOLDS[MISERY_THRESHOLDS.length - 1]) {
                chosen = atThreshold;
                break;
            }
        }

        return chosen.stream()
                .sorted(Comparator.comparing(ScoredTitle::groupScore).reversed())
                .map(s -> new MatchResult(s.title(), s.groupScore().min(BigDecimal.valueOf(100)), s.personScores(),
                        s.minScore().compareTo(BigDecimal.valueOf(40)) < 0))
                .toList();
    }

    private List<MediaType> resolveMediaTypes(SessionMediaType sessionMediaType) {
        return switch (sessionMediaType) {
            case MOVIE -> List.of(MediaType.MOVIE);
            case SERIES -> List.of(MediaType.TV);
            case EITHER -> List.of(MediaType.MOVIE, MediaType.TV);
        };
    }

    private boolean passesGenreAndIntensity(Title t, Set<String> hardNoNames, boolean anyLight, boolean anyIntense,
                                             Set<Long> intensityMarkerIds, Map<Long, String> genreNameById) {
        Set<String> titleGenreNames = resolveNames(t.getGenreIds(), genreNameById);
        if (!Collections.disjoint(titleGenreNames, hardNoNames)) return false;

        boolean hasMarker = t.getKeywordIds() != null && t.getKeywordIds().stream().anyMatch(intensityMarkerIds::contains);
        if (anyLight && hasMarker) return false;
        return !anyIntense || hasMarker;
    }

    private boolean passesTimeLimit(Title t, TimeLimit timeLimit) {
        if (t.getMediaType() == MediaType.MOVIE) {
            Integer cap = switch (timeLimit) {
                case UNDER_90 -> 90;
                case BETWEEN_90_120 -> 120;
                case BETWEEN_2_AND_2_5 -> 150;
                default -> null;
            };
            return cap == null || t.getRuntimeMinutes() == null || t.getRuntimeMinutes() <= cap;
        }
        // ponytail: episode-length/finished-vs-ongoing cutoffs are a starting guess, tune after testing like the scoring weights
        return switch (timeLimit) {
            case EPISODE_OR_TWO -> t.getSeriesStatus() == SeriesStatus.ENDED
                    && (t.getRuntimeMinutes() == null || t.getRuntimeMinutes() <= 45);
            case BINGE -> t.getSeriesStatus() == SeriesStatus.ENDED;
            case NEW_SHOW -> t.getSeriesStatus() == SeriesStatus.ONGOING;
            default -> true;
        };
    }

    private ScoredTitle scoreTitle(Title title, List<SessionPersonAnswer> people, EraPreference hostEraPreference,
                                    Set<String> hostLovedSignals, Map<Long, String> genreNameById, Map<Long, String> keywordNameById) {
        Set<String> titleGenreNames = resolveNames(title.getGenreIds(), genreNameById);
        Set<String> titleKeywordNames = resolveNames(title.getKeywordIds(), keywordNameById);
        Set<String> titleSignals = new HashSet<>(titleGenreNames);
        titleSignals.addAll(titleKeywordNames);

        Map<Integer, BigDecimal> personScores = new HashMap<>();
        for (SessionPersonAnswer person : people) {
            BigDecimal genreScore = BigDecimal.valueOf(35)
                    .multiply(BigDecimal.valueOf(overlapRatio(resolveNames(person.getGenresUpFor(), genreNameById), titleGenreNames)));

            Set<String> moodSignals = new HashSet<>();
            MediaType titleMediaType = title.getMediaType();
            for (String mood : person.getMoods()) {
                moodSignals.addAll((titleMediaType == MediaType.MOVIE ? MOVIE_MOOD_GENRES : TV_MOOD_GENRES)
                        .getOrDefault(mood, List.of()));
                if (titleMediaType == MediaType.TV) {
                    String kw = TV_MOOD_KEYWORDS.get(mood);
                    if (kw != null) moodSignals.add(kw);
                }
            }
            BigDecimal moodScore = BigDecimal.valueOf(25).multiply(BigDecimal.valueOf(overlapRatio(moodSignals, titleSignals)));

            // Q7 is enforced as a hard filter above, so every survivor already satisfies it for every person.
            BigDecimal intensityScore = BigDecimal.valueOf(15);

            BigDecimal similarityScore = similarityScore(person, titleSignals, hostLovedSignals, genreNameById, keywordNameById);
            BigDecimal eraScore = person.getPersonIndex() == 0
                    ? eraScore(hostEraPreference, title.getReleaseDate())
                    : BigDecimal.valueOf(10);

            BigDecimal total = genreScore.add(moodScore).add(intensityScore).add(similarityScore).add(eraScore)
                    .setScale(2, RoundingMode.HALF_UP);
            personScores.put(person.getPersonIndex(), total);
        }

        BigDecimal minScore = personScores.values().stream().min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal average = personScores.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(personScores.size()), 4, RoundingMode.HALF_UP);
        BigDecimal ratingBonus = title.getVoteAverage() == null ? BigDecimal.ZERO : title.getVoteAverage().multiply(BigDecimal.valueOf(0.5));
        BigDecimal groupScore = average.add(ratingBonus).setScale(2, RoundingMode.HALF_UP);

        return new ScoredTitle(title, personScores, minScore, groupScore);
    }

    private BigDecimal similarityScore(SessionPersonAnswer person, Set<String> titleSignals, Set<String> hostLovedSignals,
                                        Map<Long, String> genreNameById, Map<Long, String> keywordNameById) {
        Title liked = person.getSomethingLikeTitle();
        if (liked != null) {
            Title hydrated = titleCacheService.getDetail(liked.getId());
            Set<String> likedSignals = new HashSet<>(resolveNames(hydrated.getGenreIds(), genreNameById));
            likedSignals.addAll(resolveNames(hydrated.getKeywordIds(), keywordNameById));
            return BigDecimal.valueOf(15).multiply(BigDecimal.valueOf(overlapRatio(likedSignals, titleSignals)));
        }
        // Q8 skipped: the host falls back to their profile's loved titles (Forms Spec's cold-start signal); guests have no profile, so stay neutral.
        if (person.getPersonIndex() == 0 && !hostLovedSignals.isEmpty()) {
            return BigDecimal.valueOf(15).multiply(BigDecimal.valueOf(overlapRatio(hostLovedSignals, titleSignals)));
        }
        return BigDecimal.valueOf(15);
    }

    // ponytail: newer/classic cutoffs are a starting guess, tune after testing like the scoring weights
    private BigDecimal eraScore(EraPreference preference, LocalDate releaseDate) {
        if (preference == null || preference == EraPreference.ANY_ERA || releaseDate == null) return BigDecimal.valueOf(10);
        int age = Year.now().getValue() - releaseDate.getYear();
        boolean matches = preference == EraPreference.MOSTLY_NEWER ? age <= 7 : age >= 20;
        return matches ? BigDecimal.valueOf(10) : BigDecimal.ZERO;
    }

    private double overlapRatio(Set<String> wanted, Set<String> titleSignals) {
        if (wanted.isEmpty()) return 1.0;
        long hits = wanted.stream().filter(titleSignals::contains).count();
        return Math.min(1.0, (double) hits / wanted.size());
    }

    private Set<String> resolveNames(List<Long> ids, Map<Long, String> nameById) {
        if (ids == null) return Set.of();
        return ids.stream().map(nameById::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private void replenish(MediaType mediaType, Session session, List<String> languages, Set<String> hardNoNames,
                            Set<String> wantedNames, boolean anyLight, boolean anyIntense) {
        Map<String, String> params = new HashMap<>();
        params.put("with_original_language", String.join("|", languages));
        params.put("vote_count.gte", "20"); // ponytail: quality floor guess, tune after testing

        List<Integer> withoutGenreTmdbIds = genreRepository.findByMediaType(mediaType).stream()
                .filter(g -> hardNoNames.contains(g.getName()))
                .map(Genre::getTmdbGenreId)
                .toList();
        if (!withoutGenreTmdbIds.isEmpty()) {
            params.put("without_genres", withoutGenreTmdbIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }

        // Genre names differ between movie and TV lists, so map by name to this media type's TMDB ids ("|" = any of).
        String withGenres = genreRepository.findByMediaType(mediaType).stream()
                .filter(g -> wantedNames.contains(g.getName()))
                .map(g -> String.valueOf(g.getTmdbGenreId()))
                .collect(Collectors.joining("|"));
        if (!withGenres.isBlank()) params.put("with_genres", withGenres);

        if (anyLight ^ anyIntense) {
            String markerTmdbIds = keywordRepository.findByIsIntensityMarkerTrue().stream()
                    .map(k -> String.valueOf(k.getTmdbKeywordId()))
                    .collect(Collectors.joining("|"));
            if (!markerTmdbIds.isBlank()) {
                params.put(anyLight ? "without_keywords" : "with_keywords", markerTmdbIds);
            }
        }

        if (mediaType == MediaType.MOVIE) {
            Integer cap = switch (session.getTimeLimit()) {
                case UNDER_90 -> 90;
                case BETWEEN_90_120 -> 120;
                case BETWEEN_2_AND_2_5 -> 150;
                default -> null;
            };
            if (cap != null) params.put("with_runtime.lte", String.valueOf(cap));
        }

        titleCacheService.discoverAndCache(mediaType, params);
    }
}
