package com.movierecommender.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

/** Optional enrichment: with no OMDB_API_KEY (or on any failure) ratings just stay empty. */
@Component
public class OmdbClient {

    public record Ratings(BigDecimal imdb, Integer rottenTomatoes, Integer metacritic) {}

    private final WebClient webClient;
    private final String apiKey;

    public OmdbClient(WebClient.Builder builder,
                      @Value("${app.omdb.base-url}") String baseUrl,
                      @Value("${app.omdb.api-key}") String apiKey) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public Optional<Ratings> fetch(String imdbId) {
        if (apiKey == null || apiKey.isBlank()) return Optional.empty();
        try {
            JsonNode r = webClient.get()
                    .uri(b -> b.queryParam("i", imdbId).queryParam("apikey", apiKey).build())
                    .retrieve().bodyToMono(JsonNode.class).block(Duration.ofSeconds(8));
            if (r == null || !"True".equals(r.path("Response").asText())) return Optional.empty();
            BigDecimal imdb = parseDecimal(r.path("imdbRating").asText());
            Integer rt = null;
            for (JsonNode x : r.path("Ratings")) {
                if ("Rotten Tomatoes".equals(x.path("Source").asText())) {
                    rt = parseInt(x.path("Value").asText().replace("%", ""));
                }
            }
            return Optional.of(new Ratings(imdb, rt, parseInt(r.path("Metascore").asText())));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static BigDecimal parseDecimal(String s) {
        try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
    }

    private static Integer parseInt(String s) {
        try { return Integer.valueOf(s); } catch (NumberFormatException e) { return null; }
    }
}
