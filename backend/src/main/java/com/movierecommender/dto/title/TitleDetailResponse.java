package com.movierecommender.dto.title;

import com.movierecommender.entity.enums.CertificationBand;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.entity.enums.SeriesStatus;
import com.movierecommender.entity.model.CastMember;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TitleDetailResponse(
        Long id,
        Long tmdbId,
        MediaType mediaType,
        String title,
        String overview,
        String posterPath,
        String backdropPath,
        LocalDate releaseDate,
        Integer runtimeMinutes,
        SeriesStatus seriesStatus,
        List<String> genreNames,
        List<CastMember> cast,
        String certificationRaw,
        CertificationBand certificationBand,
        BigDecimal imdbRating,
        Integer rottenTomatoesRating,
        Integer metacriticRating,
        String imdbId,
        BigDecimal tmdbRating
) {}
