package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum SessionMediaType {
    MOVIE("movie"),
    SERIES("series"),
    EITHER("either");

    private final String value;

    SessionMediaType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SessionMediaType fromValue(String value) {
        if (value == null) return null;
        for (SessionMediaType smt : values()) {
            if (smt.value.equalsIgnoreCase(value) || smt.name().equalsIgnoreCase(value)) {
                return smt;
            }
        }
        throw new IllegalArgumentException("Unknown SessionMediaType: " + value);
    }

    @Converter(autoApply = true)
    public static class SessionMediaTypeConverter implements AttributeConverter<SessionMediaType, String> {
        @Override
        public String convertToDatabaseColumn(SessionMediaType attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public SessionMediaType convertToEntityAttribute(String dbData) {
            return dbData != null ? SessionMediaType.fromValue(dbData) : null;
        }
    }
}
