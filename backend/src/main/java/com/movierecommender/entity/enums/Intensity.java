package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum Intensity {
    LIGHT("light"),
    BALANCED("balanced"),
    INTENSE("intense");

    private final String value;

    Intensity(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Intensity fromValue(String value) {
        if (value == null) return null;
        for (Intensity i : values()) {
            if (i.value.equalsIgnoreCase(value) || i.name().equalsIgnoreCase(value)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown Intensity: " + value);
    }

    @Converter(autoApply = true)
    public static class IntensityConverter implements AttributeConverter<Intensity, String> {
        @Override
        public String convertToDatabaseColumn(Intensity attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public Intensity convertToEntityAttribute(String dbData) {
            return dbData != null ? Intensity.fromValue(dbData) : null;
        }
    }
}
