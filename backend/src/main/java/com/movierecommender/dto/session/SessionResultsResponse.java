package com.movierecommender.dto.session;

import java.util.List;

public record SessionResultsResponse(Long sessionId, List<PersonSummaryDto> people, List<ResultDto> results) {}
