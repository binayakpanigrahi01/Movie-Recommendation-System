package com.movierecommender.dto.profile;

import com.movierecommender.entity.enums.EraPreference;

import java.util.List;

public record ProfileResponse(
        String name,
        String email,
        Integer birthYear,
        boolean subtitlesOk,
        List<String> languages,
        List<Long> favoriteGenres,
        List<Long> neverWantGenres,
        List<Long> lovedTitleIds,
        EraPreference eraPreference
) {}
