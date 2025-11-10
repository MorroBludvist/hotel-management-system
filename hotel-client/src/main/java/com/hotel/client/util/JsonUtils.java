package com.hotel.client.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Утилитный класс для работы с Jackson JSON парсингом
 */
public class JsonUtils {
    private static final Logger logger = LogManager.getLogger(JsonUtils.class);

    private static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Настройки для красивого форматирования JSON (опционально)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Игнорировать неизвестные свойства (чтобы не ломаться при изменении API)
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Сериализовать объект в JSON строку
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Ошибка сериализации в JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Десериализовать JSON строку в объект
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Ошибка десериализации из JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Десериализовать JSON строку в список объектов
     */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            logger.error("Ошибка десериализации списка из JSON: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Десериализовать JSON строку в список Map (для сложных типов)
     */
    public static List<Map<String, Object>> fromJsonListToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Ошибка десериализации списка Map из JSON: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Десериализовать JSON строку в Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Ошибка десериализации Map из JSON: {}", e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}