package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum SeriesStatus {
    ONGOING("ongoing"),
    ENDED("ended");

    private final String value;

    SeriesStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SeriesStatus fromValue(String value) {
        if (value == null) return null;
        for (SeriesStatus ss : values()) {
            if (ss.value.equalsIgnoreCase(value) || ss.name().equalsIgnoreCase(value)) {
                return ss;
            }
        }
        throw new IllegalArgumentException("Unknown SeriesStatus: " + value);
    }

    @Converter(autoApply = true)
    public static class SeriesStatusConverter implements AttributeConverter<SeriesStatus, String> {
        @Override
        public String convertToDatabaseColumn(SeriesStatus attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public SeriesStatus convertToEntityAttribute(String dbData) {
            return dbData != null ? SeriesStatus.fromValue(dbData) : null;
        }
    }
}
