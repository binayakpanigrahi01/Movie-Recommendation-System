package com.movierecommender.dto.profile;

import com.movierecommender.entity.enums.EraPreference;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProfileRequest(
        @NotNull(message = "is required") Integer birthYear,
        @NotNull(message = "is required") Boolean subtitlesOk,
        @NotEmpty(message = "must include at least one language") List<String> languages,
        @NotNull(message = "is required") @Size(max = 5, message = "cannot exceed 5 genres") List<Long> favoriteGenres,
        @NotNull(message = "is required") List<Long> neverWantGenres,
        @Size(max = 5, message = "cannot exceed 5 titles") List<Long> lovedTitleIds,
        @NotNull(message = "is required") EraPreference eraPreference
) {}
