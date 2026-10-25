package com.movierecommender.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum CertificationBand {
    U("U"),
    UA7("UA7"),
    UA13("UA13"),
    UA16("UA16"),
    A("A");

    private final String value;

    CertificationBand(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CertificationBand fromValue(String value) {
        if (value == null) return null;
        for (CertificationBand cb : values()) {
            if (cb.value.equalsIgnoreCase(value) || cb.name().equalsIgnoreCase(value)) {
                return cb;
            }
        }
        throw new IllegalArgumentException("Unknown CertificationBand: " + value);
    }

    @Converter(autoApply = true)
    public static class CertificationBandConverter implements AttributeConverter<CertificationBand, String> {
        @Override
        public String convertToDatabaseColumn(CertificationBand attribute) {
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public CertificationBand convertToEntityAttribute(String dbData) {
            return dbData != null ? CertificationBand.fromValue(dbData) : null;
        }
    }
}
