package com.movierecommender.service;

import com.movierecommender.entity.enums.AgeBand;
import com.movierecommender.entity.enums.CertificationBand;

import java.time.Year;

/** Maps a birth year to an age band, and an age band to its certificate ceiling, per the Forms Spec age table. */
public final class AgeBandMapper {

    private AgeBandMapper() {}

    public static AgeBand fromBirthYear(Integer birthYear) {
        if (birthYear == null) return null;
        int age = Year.now().getValue() - birthYear;
        if (age < 7) return AgeBand.UNDER_7;
        if (age <= 12) return AgeBand.BETWEEN_7_12;
        if (age <= 15) return AgeBand.BETWEEN_13_15;
        if (age <= 17) return AgeBand.BETWEEN_16_17;
        return AgeBand.EIGHTEEN_PLUS;
    }

    public static CertificationBand certificationCeiling(AgeBand ageBand) {
        if (ageBand == null) return null;
        return switch (ageBand) {
            case UNDER_7 -> CertificationBand.U;
            case BETWEEN_7_12 -> CertificationBand.UA7;
            case BETWEEN_13_15 -> CertificationBand.UA13;
            case BETWEEN_16_17 -> CertificationBand.UA16;
            case EIGHTEEN_PLUS -> CertificationBand.A;
        };
    }
}
