package com.movierecommender.dto.session;

import com.movierecommender.entity.enums.Occasion;
import com.movierecommender.entity.enums.SessionMediaType;
import com.movierecommender.entity.enums.TimeLimit;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SessionStartRequest(
        @NotNull(message = "is required") SessionMediaType mediaType,
        @NotNull(message = "is required") @Min(value = 1, message = "must be at least 1") @Max(value = 8, message = "cannot exceed 8") Integer groupSize,
        @NotNull(message = "is required") TimeLimit timeLimit,
        Occasion occasion
) {}
