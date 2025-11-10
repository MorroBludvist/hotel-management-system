package com.hotel.client.service;

import com.hotel.client.model.Client;
import com.hotel.client.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с бронированиями и историей с Jackson
 */
public class BookingService {
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    private final ApiService apiService;

    public BookingService(ApiService apiService) {
        this.apiService = apiService;
        logger.debug("BookingService инициализирован с Jackson");
    }

    // === МЕТОДЫ ДЛЯ ОПЕРАЦИЙ БРОНИРОВАНИЯ ===

    /**
     * Заселение клиента
     */
    public boolean checkInClient(Client client) {
        logger.info("🏨 Заселение клиента: {}", client.getPassportNumber());
        try {
            String jsonBody = JsonUtils.toJson(client);
            String response = apiService.executeRequest("/bookings/check-in", "POST", jsonBody);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Клиент успешно заселен");
            } else {
                logger.warn("⚠️ Не удалось заселить клиента. Ответ: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка заселения клиента: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Выселение клиента
     */
    public boolean checkOutClient(String passportNumber) {
        logger.info("🚪 Выселение клиента с паспортом: {}", passportNumber);
        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("passportNumber", passportNumber);

            String jsonBody = JsonUtils.toJson(requestData);
            String response = apiService.executeRequest("/bookings/check-out", "POST", jsonBody);

            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Клиент успешно выселен");
            } else {
                logger.warn("⚠️ Не удалось выселить клиента. Ответ: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка выселения клиента: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Проверка возможности бронирования
     */
    public Map<String, Object> validateBooking(Client client) {
        logger.info("🔍 Проверка возможности бронирования");
        try {
            String jsonBody = JsonUtils.toJson(client);
            String response = apiService.executeRequest("/bookings/validate", "POST", jsonBody);

            if (response != null) {
                return parseValidationResponse(response);
            } else {
                logger.warn("⚠️ Не удалось проверить бронирование. Ответ: {}", response);
                return createErrorValidationResponse();
            }

        } catch (Exception e) {
            logger.error("❌ Ошибка проверки бронирования: {}", e.getMessage(), e);
            return createErrorValidationResponse();
        }
    }

    // === МЕТОДЫ ДЛЯ ИСТОРИИ БРОНИРОВАНИЙ ===

    /**
     * Получает всю историю бронирований
     */
    public List<Map<String, Object>> getAllBookingHistory() {
        logger.info("🔄 Получение всей истории бронирований");
        try {
            String response = apiService.executeRequest("/bookings/history", "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Map<String, Object>> history = JsonUtils.fromJsonListToMap(response);
                logger.info("✅ Успешно загружено {} записей истории", history.size());
                return history;
            } else {
                logger.error("❌ Сервер вернул некорректный ответ для истории бронирований: {}", response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения истории бронирований: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Получает историю бронирований для конкретного номера
     */
    public List<Map<String, Object>> getBookingHistoryByRoom(int roomNumber) {
        logger.info("🔄 Получение истории бронирований для номера {}", roomNumber);
        try {
            String response = apiService.executeRequest("/bookings/history/room/" + roomNumber, "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Map<String, Object>> history = JsonUtils.fromJsonListToMap(response);
                logger.info("✅ Успешно загружено {} записей для номера {}", history.size(), roomNumber);
                return history;
            } else {
                logger.error("❌ Сервер вернул некорректный ответ для истории номера {}: {}", roomNumber, response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения истории для номера {}: {}", roomNumber, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private Map<String, Object> parseValidationResponse(String response) {
        try {
            return JsonUtils.fromJsonToMap(response);
        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга ответа валидации: {}", e.getMessage());
            return createErrorValidationResponse();
        }
    }

    private Map<String, Object> createErrorValidationResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", false);
        result.put("roomAvailable", false);
        result.put("clientExists", false);
        result.put("message", "Ошибка соединения с сервером");
        return result;
    }
}