package com.movierecommender.dto.session;

import java.math.BigDecimal;

public record PersonScoreDto(Integer personIndex, BigDecimal score) {}
