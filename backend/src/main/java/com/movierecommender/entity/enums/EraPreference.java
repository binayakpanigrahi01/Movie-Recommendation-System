package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EraPreference {
    MOSTLY_NEWER("mostly_newer"),
    ANY_ERA("any_era"),
    LOVE_CLASSICS("love_classics");

    private final String value;

    EraPreference(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EraPreference fromValue(String value) {
        if (value == null) return null;
        for (EraPreference ep : values()) {
            if (ep.value.equalsIgnoreCase(value) || ep.name().equalsIgnoreCase(value)) {
                return ep;
            }
        }
        throw new IllegalArgumentException("Unknown EraPreference: " + value);
    }

    @Converter(autoApply = true)
    public static class EraPreferenceConverter implements AttributeConverter<EraPreference, String> {
        @Override
        public String convertToDatabaseColumn(EraPreference attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public EraPreference convertToEntityAttribute(String dbData) {
            return dbData != null ? EraPreference.fromValue(dbData) : null;
        }
    }
}
