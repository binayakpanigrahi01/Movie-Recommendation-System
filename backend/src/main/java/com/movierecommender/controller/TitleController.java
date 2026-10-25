package com.movierecommender.controller;

import com.movierecommender.dto.title.TitleDetailResponse;
import com.movierecommender.dto.title.TitleSearchRequest;
import com.movierecommender.dto.title.TitleSearchResponse;
import com.movierecommender.dto.title.TitleSummaryDto;
import com.movierecommender.entity.Genre;
import com.movierecommender.entity.Title;
import com.movierecommender.repository.GenreRepository;
import com.movierecommender.service.TitleCacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/titles")
@RequiredArgsConstructor
public class TitleController {

    private final TitleCacheService titleCacheService;
    private final GenreRepository genreRepository;

    @PostMapping("/search")
    public TitleSearchResponse search(@Valid @RequestBody TitleSearchRequest request) {
        List<TitleSummaryDto> results = titleCacheService.searchAndCache(request.query(), request.mediaType())
                .stream()
                .map(this::toSummary)
                .toList();
        return new TitleSearchResponse(results);
    }

    @GetMapping("/{id}")
    public TitleDetailResponse getDetail(@PathVariable Long id) {
        return toDetail(titleCacheService.getDetail(id));
    }

    private TitleSummaryDto toSummary(Title t) {
        return new TitleSummaryDto(t.getId(), t.getTmdbId(), t.getMediaType(), t.getTitle(), t.getPosterPath(), t.getReleaseDate());
    }

    private TitleDetailResponse toDetail(Title t) {
        List<String> genreNames = genreRepository.findAllById(t.getGenreIds()).stream().map(Genre::getName).toList();
        return new TitleDetailResponse(
                t.getId(), t.getTmdbId(), t.getMediaType(), t.getTitle(), t.getOverview(),
                t.getPosterPath(), t.getBackdropPath(), t.getReleaseDate(), t.getRuntimeMinutes(), t.getSeriesStatus(),
                genreNames, t.getCast(), t.getCertificationRaw(), t.getCertificationBand(),
                t.getImdbRating(), t.getRottenTomatoesRating(), t.getMetacriticRating(),
                t.getImdbId(), t.getVoteAverage());
    }
}
