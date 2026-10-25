package com.movierecommender.dto.session;

import com.movierecommender.entity.enums.AgeBand;
import com.movierecommender.entity.enums.Intensity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PersonAnswerRequest(
        @NotNull(message = "is required") Integer personIndex,
        String name,
        AgeBand ageBand,
        @NotEmpty(message = "must include at least one mood") @Size(max = 2, message = "cannot exceed 2 moods") List<String> moods,
        @NotNull(message = "is required") @Size(max = 3, message = "cannot exceed 3 genres") List<Long> genresUpFor,
        @NotNull(message = "is required") List<Long> hardNoGenres,
        @NotEmpty(message = "must include at least one language") List<String> languagesOk,
        @NotNull(message = "is required") Intensity intensity,
        Long somethingLikeTitleId
) {}
