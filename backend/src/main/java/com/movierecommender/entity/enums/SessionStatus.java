package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum SessionStatus {
    PENDING("pending"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String value;

    SessionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SessionStatus fromValue(String value) {
        if (value == null) return null;
        for (SessionStatus ss : values()) {
            if (ss.value.equalsIgnoreCase(value) || ss.name().equalsIgnoreCase(value)) {
                return ss;
            }
        }
        throw new IllegalArgumentException("Unknown SessionStatus: " + value);
    }

    @Converter(autoApply = true)
    public static class SessionStatusConverter implements AttributeConverter<SessionStatus, String> {
        @Override
        public String convertToDatabaseColumn(SessionStatus attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public SessionStatus convertToEntityAttribute(String dbData) {
            return dbData != null ? SessionStatus.fromValue(dbData) : null;
        }
    }
}
