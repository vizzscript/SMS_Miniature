package com.pinnacle.backend.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PrivilegesConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> privileges) {
        if (privileges == null) {
            return "[]";
        }
        try {
            return new ObjectMapper().writeValueAsString(privileges);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting privileges list to JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return new ArrayList<>();
        }
        try {
            return new ObjectMapper().readValue(dbData, List.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON to privileges list", e);
        }
    }
}