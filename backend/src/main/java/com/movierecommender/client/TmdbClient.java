package com.movierecommender.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriBuilder;

import java.util.Map;
import java.util.function.UnaryOperator;

@Component
public class TmdbClient {

    private final WebClient webClient;
    private final String apiKey;

    public TmdbClient(WebClient.Builder builder,
                       @Value("${app.tmdb.base-url}") String baseUrl,
                       @Value("${app.tmdb.api-key}") String apiKey) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public JsonNode search(MediaType mediaType, String query) {
        String path = mediaType == MediaType.MOVIE ? "/search/movie" : "/search/tv";
        return get(path, b -> b.queryParam("query", query));
    }

    public JsonNode movieDetails(long tmdbId) {
        return get("/movie/" + tmdbId, b -> b.queryParam("append_to_response", "release_dates,credits,keywords"));
    }

    public JsonNode tvDetails(long tmdbId) {
        return get("/tv/" + tmdbId, b -> b.queryParam("append_to_response", "content_ratings,credits,keywords,external_ids"));
    }

    public JsonNode discoverMovies(Map<String, String> params) {
        return get("/discover/movie", b -> applyParams(b, params));
    }

    public JsonNode discoverTv(Map<String, String> params) {
        return get("/discover/tv", b -> applyParams(b, params));
    }

    private static UriBuilder applyParams(UriBuilder builder, Map<String, String> params) {
        params.forEach(builder::queryParam);
        return builder;
    }

    private JsonNode get(String path, UnaryOperator<UriBuilder> customizer) {
        try {
            return webClient.get()
                    .uri(b -> customizer.apply(b.path(path)).queryParam("api_key", apiKey).build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(java.time.Duration.ofSeconds(15));
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "TMDB_UNREACHABLE", "Couldn't reach the movie database");
        }
    }
}
