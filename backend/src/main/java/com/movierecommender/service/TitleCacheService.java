package com.movierecommender.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.movierecommender.client.OmdbClient;
import com.movierecommender.client.TmdbClient;
import com.movierecommender.entity.Title;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.entity.enums.SeriesStatus;
import com.movierecommender.entity.model.CastMember;
import com.movierecommender.exception.ApiException;
import com.movierecommender.repository.GenreRepository;
import com.movierecommender.repository.KeywordRepository;
import com.movierecommender.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Caches TMDB titles in MySQL. Search results are upserted with minimal fields immediately
 * (Forms Spec caching note); full detail (cast, genres, keywords, certification, runtime) is
 * fetched lazily the first time a title is opened or becomes a match candidate.
 */
@Service
@RequiredArgsConstructor
public class TitleCacheService {

    private final TmdbClient tmdbClient;
    private final TitleRepository titleRepository;
    private final GenreRepository genreRepository;
    private final KeywordRepository keywordRepository;
    private final OmdbClient omdbClient;

    @Transactional
    public List<Title> searchAndCache(String query, MediaType mediaType) {
        JsonNode response = tmdbClient.search(mediaType, query);
        List<Title> results = new ArrayList<>();
        for (JsonNode result : response.path("results")) {
            results.add(upsertBasic(result, mediaType));
        }
        return results;
    }

    /** Replenishes the local cache from TMDB Discover with the given filter params, upserting full details so the results are immediately usable as match candidates. */
    @Transactional
    public List<Title> discoverAndCache(MediaType mediaType, Map<String, String> params) {
        JsonNode response = mediaType == MediaType.MOVIE ? tmdbClient.discoverMovies(params) : tmdbClient.discoverTv(params);
        List<Title> results = new ArrayList<>();
        for (JsonNode r : response.path("results")) {
            results.add(upsertBasic(r, mediaType));
        }
        // Skip titles already hydrated and fetch the rest concurrently: sequential calls (~1s each) caused request timeouts.
        List<Title> missing = results.stream().filter(t -> t.getOverview() == null).toList();
        Map<Long, JsonNode> details = missing.parallelStream().collect(java.util.stream.Collectors.toMap(
                Title::getId, t -> fetchDetailJson(t)));
        missing.forEach(t -> applyFullDetails(t, details.get(t.getId())));
        return results;
    }

    @Transactional
    public Title getDetail(Long id) {
        Title title = titleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TITLE_NOT_FOUND", "Title not found."));
        if (title.getOverview() == null || title.getImdbId() == null) {
            fetchFullDetails(title);
        }
        if (title.getImdbId() != null && title.getImdbRating() == null) {
            omdbClient.fetch(title.getImdbId()).ifPresent(r -> {
                title.setImdbRating(r.imdb());
                title.setRottenTomatoesRating(r.rottenTomatoes());
                title.setMetacriticRating(r.metacritic());
                titleRepository.save(title);
            });
        }
        return title;
    }

    private Title upsertBasic(JsonNode r, MediaType mediaType) {
        long tmdbId = r.path("id").asLong();
        Title title = titleRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> Title.builder().tmdbId(tmdbId).mediaType(mediaType).build());
        title.setTitle(mediaType == MediaType.MOVIE ? textOrNull(r, "title") : textOrNull(r, "name"));
        title.setPosterPath(textOrNull(r, "poster_path"));
        title.setOriginalLanguage(textOrNull(r, "original_language"));
        title.setReleaseDate(parseDate(textOrNull(r, mediaType == MediaType.MOVIE ? "release_date" : "first_air_date")));
        title.setVoteAverage(bigDecimalOrNull(r.path("vote_average")));
        title.setVoteCount(r.path("vote_count").asInt(0));
        return titleRepository.save(title);
    }

    private void fetchFullDetails(Title title) {
        applyFullDetails(title, fetchDetailJson(title));
    }

    private JsonNode fetchDetailJson(Title title) {
        return title.getMediaType() == MediaType.MOVIE ? tmdbClient.movieDetails(title.getTmdbId()) : tmdbClient.tvDetails(title.getTmdbId());
    }

    private void applyFullDetails(Title title, JsonNode detail) {
        boolean isMovie = title.getMediaType() == MediaType.MOVIE;

        title.setTitle(textOrNull(detail, isMovie ? "title" : "name"));
        title.setOverview(detail.path("overview").asText(""));
        title.setPosterPath(textOrNull(detail, "poster_path"));
        title.setBackdropPath(textOrNull(detail, "backdrop_path"));
        title.setOriginalLanguage(textOrNull(detail, "original_language"));
        title.setVoteAverage(bigDecimalOrNull(detail.path("vote_average")));
        title.setVoteCount(detail.path("vote_count").asInt(0));
        title.setGenreIds(mapGenreIds(detail.path("genres"), title.getMediaType()));
        title.setKeywordIds(mapKeywordIds(detail.path("keywords"), title.getMediaType()));
        title.setCast(mapCast(detail.path("credits").path("cast")));

        if (isMovie) {
            title.setReleaseDate(parseDate(textOrNull(detail, "release_date")));
            title.setImdbId(textOrNull(detail, "imdb_id"));
            JsonNode runtime = detail.path("runtime");
            title.setRuntimeMinutes(runtime.isInt() ? runtime.asInt() : null);
            CertificationMapper.Result cert = CertificationMapper.resolveMovie(detail.path("release_dates"));
            title.setCertificationRaw(cert.raw());
            title.setCertificationBand(cert.band());
        } else {
            title.setReleaseDate(parseDate(textOrNull(detail, "first_air_date")));
            title.setImdbId(textOrNull(detail.path("external_ids"), "imdb_id"));
            JsonNode episodeRuntimes = detail.path("episode_run_time");
            title.setRuntimeMinutes(episodeRuntimes.isArray() && !episodeRuntimes.isEmpty() ? episodeRuntimes.get(0).asInt() : null);
            String status = detail.path("status").asText("");
            title.setSeriesStatus(status.equalsIgnoreCase("Ended") || status.equalsIgnoreCase("Canceled")
                    ? SeriesStatus.ENDED : SeriesStatus.ONGOING);
            CertificationMapper.Result cert = CertificationMapper.resolveTv(detail.path("content_ratings"));
            title.setCertificationRaw(cert.raw());
            title.setCertificationBand(cert.band());
        }

        titleRepository.save(title);
    }

    private List<Long> mapGenreIds(JsonNode genresNode, MediaType mediaType) {
        List<Long> ids = new ArrayList<>();
        for (JsonNode g : genresNode) {
            genreRepository.findByTmdbGenreIdAndMediaType(g.path("id").asInt(), mediaType)
                    .ifPresent(genre -> ids.add(genre.getId()));
        }
        return ids;
    }

    private List<Long> mapKeywordIds(JsonNode keywordsNode, MediaType mediaType) {
        JsonNode list = mediaType == MediaType.MOVIE ? keywordsNode.path("keywords") : keywordsNode.path("results");
        List<Long> ids = new ArrayList<>();
        for (JsonNode k : list) {
            keywordRepository.findByTmdbKeywordId(k.path("id").asInt())
                    .ifPresent(keyword -> ids.add(keyword.getId()));
        }
        return ids;
    }

    private List<CastMember> mapCast(JsonNode castNode) {
        List<CastMember> cast = new ArrayList<>();
        int limit = Math.min(5, castNode.size());
        for (int i = 0; i < limit; i++) {
            JsonNode c = castNode.get(i);
            cast.add(CastMember.builder()
                    .name(textOrNull(c, "name"))
                    .character(textOrNull(c, "character"))
                    .photoUrl(textOrNull(c, "profile_path"))
                    .build());
        }
        return cast;
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }

    private static LocalDate parseDate(String s) {
        return (s == null || s.isBlank()) ? null : LocalDate.parse(s);
    }

    private static BigDecimal bigDecimalOrNull(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : BigDecimal.valueOf(node.asDouble());
    }
}
