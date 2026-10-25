package com.movierecommender.dto.title;

import com.movierecommender.entity.enums.MediaType;

import java.time.LocalDate;

public record TitleSummaryDto(
        Long id,
        Long tmdbId,
        MediaType mediaType,
        String title,
        String posterPath,
        LocalDate releaseDate
) {}
