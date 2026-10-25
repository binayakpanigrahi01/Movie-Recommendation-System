package com.movierecommender.dto.session;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SessionSubmitRequest(
        @NotEmpty(message = "must include at least one person") @Valid List<PersonAnswerRequest> people
) {}
