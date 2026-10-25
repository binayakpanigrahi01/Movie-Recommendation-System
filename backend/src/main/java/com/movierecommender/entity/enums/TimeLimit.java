package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum TimeLimit {
    UNDER_90("under_90"),
    BETWEEN_90_120("90_120"),
    BETWEEN_2_AND_2_5("2_2.5"),
    NO_LIMIT("no_limit"),
    EPISODE_OR_TWO("episode_or_two"),
    BINGE("binge"),
    NEW_SHOW("new_show");

    private final String value;

    TimeLimit(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TimeLimit fromValue(String value) {
        if (value == null) return null;
        for (TimeLimit tl : values()) {
            if (tl.value.equalsIgnoreCase(value) || tl.name().equalsIgnoreCase(value)) {
                return tl;
            }
        }
        throw new IllegalArgumentException("Unknown TimeLimit: " + value);
    }

    @Converter(autoApply = true)
    public static class TimeLimitConverter implements AttributeConverter<TimeLimit, String> {
        @Override
        public String convertToDatabaseColumn(TimeLimit attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public TimeLimit convertToEntityAttribute(String dbData) {
            return dbData != null ? TimeLimit.fromValue(dbData) : null;
        }
    }
}
