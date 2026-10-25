package com.movierecommender.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movierecommender.entity.model.CastMember;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Converter
public class CastListConverter implements AttributeConverter<List<CastMember>, String> {

    private static final Logger log = LoggerFactory.getLogger(CastListConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<CastMember>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<CastMember> attribute) {
        if (attribute == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert List<CastMember> to JSON string", e);
            return "[]";
        }
    }

    @Override
    public List<CastMember> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, TYPE_REF);
        } catch (Exception e) {
            log.error("Failed to parse JSON string to List<CastMember>: {}", dbData, e);
            return new ArrayList<>();
        }
    }
}
