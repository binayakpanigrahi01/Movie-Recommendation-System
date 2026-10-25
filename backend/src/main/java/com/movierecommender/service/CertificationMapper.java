package com.movierecommender.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.movierecommender.entity.enums.CertificationBand;

/** Maps TMDB release_dates (movies) / content_ratings (TV) into the app's Indian certificate bands, per the Forms Spec's IN-first-then-US fallback table. */
public final class CertificationMapper {

    private CertificationMapper() {}

    public record Result(String raw, CertificationBand band) {
        static final Result NONE = new Result(null, null);
    }

    public static Result resolveMovie(JsonNode releaseDates) {
        String in = findCertification(releaseDates, "IN");
        if (in != null) {
            CertificationBand band = mapIndian(in);
            if (band != null) return new Result(in, band);
        }
        String us = findCertification(releaseDates, "US");
        if (us != null) {
            CertificationBand band = mapUsMovie(us);
            if (band != null) return new Result(us, band);
        }
        return Result.NONE;
    }

    public static Result resolveTv(JsonNode contentRatings) {
        String in = findRating(contentRatings, "IN");
        if (in != null) {
            CertificationBand band = mapIndian(in);
            if (band != null) return new Result(in, band);
        }
        String us = findRating(contentRatings, "US");
        if (us != null) {
            CertificationBand band = mapUsTv(us);
            if (band != null) return new Result(us, band);
        }
        return Result.NONE;
    }

    private static String findCertification(JsonNode releaseDates, String country) {
        for (JsonNode entry : releaseDates.path("results")) {
            if (country.equalsIgnoreCase(entry.path("iso_3166_1").asText(""))) {
                for (JsonNode rd : entry.path("release_dates")) {
                    String cert = rd.path("certification").asText("");
                    if (!cert.isBlank()) return cert;
                }
            }
        }
        return null;
    }

    private static String findRating(JsonNode contentRatings, String country) {
        for (JsonNode entry : contentRatings.path("results")) {
            if (country.equalsIgnoreCase(entry.path("iso_3166_1").asText(""))) {
                String rating = entry.path("rating").asText("");
                if (!rating.isBlank()) return rating;
            }
        }
        return null;
    }

    private static CertificationBand mapIndian(String raw) {
        return switch (raw.trim().toUpperCase().replaceAll("\\s+", " ")) {
            case "U" -> CertificationBand.U;
            case "UA 7+" -> CertificationBand.UA7;
            case "UA", "UA 13+" -> CertificationBand.UA13;
            case "UA 16+" -> CertificationBand.UA16;
            case "A" -> CertificationBand.A;
            default -> null;
        };
    }

    private static CertificationBand mapUsMovie(String raw) {
        return switch (raw.trim().toUpperCase()) {
            case "G" -> CertificationBand.U;
            case "PG" -> CertificationBand.UA7;
            case "PG-13" -> CertificationBand.UA13;
            case "R", "NC-17" -> CertificationBand.A;
            default -> null;
        };
    }

    private static CertificationBand mapUsTv(String raw) {
        return switch (raw.trim().toUpperCase()) {
            case "TV-Y", "TV-G" -> CertificationBand.U;
            case "TV-Y7", "TV-PG" -> CertificationBand.UA7;
            case "TV-14" -> CertificationBand.UA16;
            case "TV-MA" -> CertificationBand.A;
            default -> null;
        };
    }
}
