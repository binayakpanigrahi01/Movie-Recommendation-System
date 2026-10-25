package com.movierecommender.dto.title;

import com.movierecommender.entity.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TitleSearchRequest(
        @NotBlank(message = "is required") String query,
        @NotNull(message = "is required") MediaType mediaType
) {}
