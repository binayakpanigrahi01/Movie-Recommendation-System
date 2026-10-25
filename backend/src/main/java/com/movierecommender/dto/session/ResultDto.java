package com.movierecommender.dto.session;

import com.movierecommender.entity.enums.CertificationBand;
import com.movierecommender.entity.enums.MediaType;
import com.movierecommender.entity.enums.ResultBadge;

import java.math.BigDecimal;
import java.util.List;

public record ResultDto(
        Integer rank,
        Long titleId,
        String title,
        String posterPath,
        MediaType mediaType,
        Integer year,
        Integer runtimeMinutes,
        String certificationRaw,
        CertificationBand certificationBand,
        BigDecimal groupScore,
        ResultBadge badge,
        String aiExplanation,
        List<PersonScoreDto> personScores
) {}
