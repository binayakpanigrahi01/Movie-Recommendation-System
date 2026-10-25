package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum MediaType {
    MOVIE("movie"),
    TV("tv");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MediaType fromValue(String value) {
        if (value == null) return null;
        for (MediaType mt : values()) {
            if (mt.value.equalsIgnoreCase(value) || mt.name().equalsIgnoreCase(value)) {
                return mt;
            }
        }
        throw new IllegalArgumentException("Unknown MediaType: " + value);
    }

    @Converter(autoApply = true)
    public static class MediaTypeConverter implements AttributeConverter<MediaType, String> {
        @Override
        public String convertToDatabaseColumn(MediaType attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public MediaType convertToEntityAttribute(String dbData) {
            return dbData != null ? MediaType.fromValue(dbData) : null;
        }
    }
}
