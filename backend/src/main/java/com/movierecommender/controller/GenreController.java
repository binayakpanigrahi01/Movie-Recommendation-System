package com.movierecommender.controller;

import com.movierecommender.dto.genre.GenreDto;
import com.movierecommender.dto.genre.GenreListResponse;
import com.movierecommender.entity.Genre;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;

    @GetMapping
    public GenreListResponse list(@RequestParam(required = false) String mediaType) {
        List<Genre> genres = mediaType != null
                ? genreRepository.findByMediaType(MediaType.fromValue(mediaType))
                : genreRepository.findAll();
        List<GenreDto> dtos = genres.stream()
                .map(g -> new GenreDto(g.getId(), g.getName(), g.getMediaType()))
                .toList();
        return new GenreListResponse(dtos);
    }
}
