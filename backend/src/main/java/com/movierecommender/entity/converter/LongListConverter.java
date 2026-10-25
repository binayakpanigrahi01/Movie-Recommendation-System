package com.movierecommender.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Converter
public class LongListConverter implements AttributeConverter<List<Long>, String> {

    private static final Logger log = LoggerFactory.getLogger(LongListConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<Long>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert List<Long> to JSON string", e);
            return "[]";
        }
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, TYPE_REF);
        } catch (Exception e) {
            log.error("Failed to parse JSON string to List<Long>: {}", dbData, e);
            return new ArrayList<>();
        }
    }
}
