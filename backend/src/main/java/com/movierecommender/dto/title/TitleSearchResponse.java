package com.movierecommender.dto.title;

import java.util.List;

public record TitleSearchResponse(List<TitleSummaryDto> results) {}
