package com.hotel.client.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с бронированиями и историей
 */
public class BookingService {
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    private final ApiService apiService;

    public BookingService(ApiService apiService) {
        this.apiService = apiService;
        logger.debug("BookingService инициализирован");
    }

    // === МЕТОДЫ ДЛЯ ОПЕРАЦИЙ БРОНИРОВАНИЯ ===

    /**
     * Заселение клиента
     */
    public boolean checkInClient(Map<String, Object> bookingData) {
        logger.info("🏨 Заселение клиента: {}", bookingData.get("passportNumber"));
        try {
            String jsonBody = createBookingJson(bookingData);
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
            String jsonBody = String.format("{\"passportNumber\":\"%s\"}",
                    apiService.escapeJson(passportNumber));

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
    public Map<String, Object> validateBooking(Map<String, Object> bookingData) {
        logger.info("🔍 Проверка возможности бронирования");
        try {
            String jsonBody = createBookingJson(bookingData);
            String response = apiService.executeRequest("/bookings/validate", "POST", jsonBody);

            if (response != null && response.contains("\"valid\"")) {
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
                List<Map<String, Object>> history = parseJsonToBookingHistory(response);
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
                List<Map<String, Object>> history = parseJsonToBookingHistory(response);
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

    /**
     * Добавляет запись в историю бронирований
     */
    public boolean addBookingHistory(int roomNumber, String clientPassport, String checkInDate, String checkOutDate) {
        logger.info("📝 Добавление в историю бронирований: номер {}, клиент {}", roomNumber, clientPassport);
        try {
            String jsonBody = String.format(
                    "{\"roomNumber\":%d,\"clientPassport\":\"%s\",\"checkInDate\":\"%s\",\"checkOutDate\":\"%s\"}",
                    roomNumber,
                    apiService.escapeJson(clientPassport),
                    apiService.escapeJson(checkInDate),
                    apiService.escapeJson(checkOutDate)
            );

            String response = apiService.executeRequest("/bookings/history", "POST", jsonBody);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Запись добавлена в историю бронирований");
            } else {
                logger.warn("⚠️ Не удалось добавить запись в историю бронирований. Ответ: {}", response);
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка добавления в историю бронирований: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Очищает историю бронирований для конкретного клиента
     */
    public boolean clearBookingHistoryByPassport(String passport) {
        logger.info("🗑️ Очистка истории бронирований для паспорта: {}", passport);
        try {
            String response = apiService.executeRequest("/bookings/history/passport/" + passport, "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ История бронирований для паспорта {} очищена", passport);
            } else {
                logger.warn("⚠️ Не удалось очистить историю бронирований для паспорта {}. Ответ: {}", passport, response);
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка очистки истории бронирований для паспорта {}: {}", passport, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Очищает всю историю бронирований
     */
    public boolean clearAllBookingHistory() {
        logger.info("🗑️ Очистка всей истории бронирований");
        try {
            String response = apiService.executeRequest("/bookings/history/clear", "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Вся история бронирований очищена");
            } else {
                logger.warn("⚠️ Не удалось очистить историю бронирований. Ответ: {}", response);
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка очистки всей истории бронирований: {}", e.getMessage(), e);
            return false;
        }
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private String createBookingJson(Map<String, Object> bookingData) {
        // Создает JSON для операций бронирования
        return String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"passportNumber\":\"%s\"," +
                        "\"phoneNumber\":\"%s\",\"email\":\"%s\",\"checkInDate\":\"%s\"," +
                        "\"checkOutDate\":\"%s\",\"roomNumber\":%d,\"roomType\":\"%s\"}",
                apiService.escapeJson((String) bookingData.get("firstName")),
                apiService.escapeJson((String) bookingData.get("lastName")),
                apiService.escapeJson((String) bookingData.get("passportNumber")),
                apiService.escapeJson((String) bookingData.get("phoneNumber")),
                apiService.escapeJson((String) bookingData.get("email")),
                apiService.escapeJson((String) bookingData.get("checkInDate")),
                apiService.escapeJson((String) bookingData.get("checkOutDate")),
                bookingData.get("roomNumber"),
                apiService.escapeJson((String) bookingData.get("roomType"))
        );
    }

    private Map<String, Object> parseValidationResponse(String response) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("valid", response.contains("\"valid\":true"));
            result.put("roomAvailable", response.contains("\"roomAvailable\":true"));
            result.put("clientExists", response.contains("\"clientExists\":true"));
            result.put("message", extractMessage(response));
        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга ответа валидации: {}", e.getMessage());
            result.put("valid", false);
            result.put("message", "Ошибка проверки бронирования");
        }
        return result;
    }

    private Map<String, Object> createErrorValidationResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", false);
        result.put("roomAvailable", false);
        result.put("clientExists", false);
        result.put("message", "Ошибка соединения с сервером");
        return result;
    }

    private String extractMessage(String json) {
        try {
            String search = "\"message\":\"";
            int start = json.indexOf(search);
            if (start == -1) return "Неизвестная ошибка";

            start += search.length();
            int end = json.indexOf("\"", start);
            if (end == -1) return "Неизвестная ошибка";

            return json.substring(start, end);
        } catch (Exception e) {
            return "Неизвестная ошибка";
        }
    }

    /**
     * Парсит JSON в список записей истории бронирований
     */
    private List<Map<String, Object>> parseJsonToBookingHistory(String json) {
        List<Map<String, Object>> history = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            logger.warn("❌ JSON пустой или null");
            return history;
        }

        try {
            logger.debug("🔧 Начинаем парсинг JSON истории бронирований...");

            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                logger.info("📭 Нет данных истории бронирований");
                return history;
            }

            String[] objects = cleanJson.split("\\},\\s*\\{");
            logger.debug("📋 Найдено объектов: {}", objects.length);

            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i].trim();

                if (i == 0 && !obj.startsWith("{")) obj = "{" + obj;
                if (i == objects.length - 1 && !obj.endsWith("}")) obj = obj + "}";
                if (i > 0 && i < objects.length - 1) {
                    if (!obj.startsWith("{")) obj = "{" + obj;
                    if (!obj.endsWith("}")) obj = obj + "}";
                }

                Map<String, Object> record = parseBookingHistoryObject(obj);
                if (record != null) {
                    history.add(record);
                }
            }

            logger.info("🎯 Итого распаршено записей истории: {}", history.size());
            return history;

        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга истории бронирований: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private Map<String, Object> parseBookingHistoryObject(String jsonObject) {
        try {
            Integer roomNumber = apiService.extractIntegerValue(jsonObject, "roomNumber");
            String clientPassport = apiService.extractStringValue(jsonObject, "clientPassport");
            String checkInDate = apiService.extractStringValue(jsonObject, "checkInDate");
            String checkOutDate = apiService.extractStringValue(jsonObject, "checkOutDate");
            String bookedAt = apiService.extractStringValue(jsonObject, "bookedAt");

            if (roomNumber != null) {
                Map<String, Object> record = new HashMap<>();
                record.put("roomNumber", roomNumber);
                record.put("clientPassport", clientPassport != null ? clientPassport : "-");
                record.put("checkInDate", checkInDate != null ? checkInDate : "-");
                record.put("checkOutDate", checkOutDate != null ? checkOutDate : "-");
                record.put("bookedAt", bookedAt != null ? bookedAt : "-");

                logger.debug("✅ Создана запись истории: номер {}, клиент {}", roomNumber, clientPassport);
                return record;
            }

        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга объекта истории бронирования: {}", e.getMessage(), e);
        }
        return null;
    }
}