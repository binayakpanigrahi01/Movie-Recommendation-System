package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum Occasion {
    FRIENDS_HANGOUT("friends_hangout"),
    DATE_NIGHT("date_night"),
    FAMILY_TIME("family_time"),
    SOLO_CHILL("solo_chill");

    private final String value;

    Occasion(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Occasion fromValue(String value) {
        if (value == null) return null;
        for (Occasion o : values()) {
            if (o.value.equalsIgnoreCase(value) || o.name().equalsIgnoreCase(value)) {
                return o;
            }
        }
        throw new IllegalArgumentException("Unknown Occasion: " + value);
    }

    @Converter(autoApply = true)
    public static class OccasionConverter implements AttributeConverter<Occasion, String> {
        @Override
        public String convertToDatabaseColumn(Occasion attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public Occasion convertToEntityAttribute(String dbData) {
            return dbData != null ? Occasion.fromValue(dbData) : null;
        }
    }
}
