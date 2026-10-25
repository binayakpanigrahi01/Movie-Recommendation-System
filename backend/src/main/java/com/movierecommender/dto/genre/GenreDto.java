package com.movierecommender.dto.genre;

import com.movierecommender.entity.enums.MediaType;

public record GenreDto(Long id, String name, MediaType mediaType) {}
