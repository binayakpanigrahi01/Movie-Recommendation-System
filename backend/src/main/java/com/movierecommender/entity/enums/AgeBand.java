package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum AgeBand {
    UNDER_7("under_7"),
    BETWEEN_7_12("7_12"),
    BETWEEN_13_15("13_15"),
    BETWEEN_16_17("16_17"),
    EIGHTEEN_PLUS("18_plus");

    private final String value;

    AgeBand(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AgeBand fromValue(String value) {
        if (value == null) return null;
        for (AgeBand ab : values()) {
            if (ab.value.equalsIgnoreCase(value) || ab.name().equalsIgnoreCase(value)) {
                return ab;
            }
        }
        throw new IllegalArgumentException("Unknown AgeBand: " + value);
    }

    @Converter(autoApply = true)
    public static class AgeBandConverter implements AttributeConverter<AgeBand, String> {
        @Override
        public String convertToDatabaseColumn(AgeBand attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public AgeBand convertToEntityAttribute(String dbData) {
            return dbData != null ? AgeBand.fromValue(dbData) : null;
        }
    }
}
