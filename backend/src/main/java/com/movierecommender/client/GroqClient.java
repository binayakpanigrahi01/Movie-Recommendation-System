package com.movierecommender.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class GroqClient {

    private final WebClient webClient;
    private final String model;

    public GroqClient(WebClient.Builder builder,
                       @Value("${app.groq.base-url}") String baseUrl,
                       @Value("${app.groq.api-key}") String apiKey,
                       @Value("${app.groq.model}") String model) {
        this.webClient = builder.baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        this.model = model;
    }

    public Mono<String> explain(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 200,
                "reasoning_effort", "low"
        );
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.path("choices").path(0).path("message").path("content").asText().trim());
    }
}
