package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum ResultBadge {
    NONE("none"),
    COMPROMISE("compromise"),
    NOT_RATED("not_rated");

    private final String value;

    ResultBadge(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ResultBadge fromValue(String value) {
        if (value == null) return null;
        for (ResultBadge rb : values()) {
            if (rb.value.equalsIgnoreCase(value) || rb.name().equalsIgnoreCase(value)) {
                return rb;
            }
        }
        throw new IllegalArgumentException("Unknown ResultBadge: " + value);
    }

    @Converter(autoApply = true)
    public static class ResultBadgeConverter implements AttributeConverter<ResultBadge, String> {
        @Override
        public String convertToDatabaseColumn(ResultBadge attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public ResultBadge convertToEntityAttribute(String dbData) {
            return dbData != null ? ResultBadge.fromValue(dbData) : null;
        }
    }
}
