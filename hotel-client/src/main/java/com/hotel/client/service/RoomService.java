package com.hotel.client.service;

import com.hotel.client.model.Room;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomService {
    private static final Logger logger = LogManager.getLogger(RoomService.class);

    private final ApiService apiService;

    public RoomService(ApiService apiService) {
        this.apiService = apiService;
        logger.debug("RoomService инициализирован");
    }

    public List<Room> getAllRooms() {
        logger.info("🔄 Получаем список всех номеров");
        try {
            String response = apiService.executeRequest("/rooms", "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Room> rooms = parseJsonToRooms(response);
                logger.info("✅ Успешно загружено {} номеров", rooms.size());
                return rooms;
            } else {
                logger.error("❌ Сервер вернул некорректный ответ для номеров: {}", response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения номеров: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Room> getFreeRooms() {
        logger.info("🔄 Получаем список свободных номеров");
        try {
            String response = apiService.executeRequest("/rooms/free", "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Room> rooms = parseJsonToRooms(response);
                logger.info("✅ Найдено {} свободных номеров", rooms.size());
                return rooms;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения свободных номеров: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public boolean isRoomAvailable(int roomNumber, String checkInDate, String checkOutDate) {
        logger.info("🔍 Проверяем доступность номера {} с {} по {}", roomNumber, checkInDate, checkOutDate);
        try {
            String jsonBody = String.format(
                    "{\"roomNumber\":%d,\"checkInDate\":\"%s\",\"checkOutDate\":\"%s\"}",
                    roomNumber, checkInDate, checkOutDate
            );

            String response = apiService.executeRequest("/rooms/check-availability", "POST", jsonBody);
            boolean available = response != null && response.contains("\"available\":true");

            logger.info("📊 Номер {} доступен: {}", roomNumber, available);
            return available;
        } catch (Exception e) {
            logger.error("❌ Ошибка проверки доступности номера: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean advanceDate(String currentDate) {
        logger.info("📅 Продвигаем дату на сервере: {}", currentDate);
        try {
            String jsonBody = String.format("{\"currentDate\":\"%s\"}", currentDate);
            String response = apiService.executeRequest("/rooms/advance-date", "POST", jsonBody);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Дата успешно обновлена на сервере");
            } else {
                logger.warn("⚠️ Не удалось обновить дату на сервере");
            }
            return success;
        } catch (Exception e) {
            logger.error("❌ Ошибка обновления даты: {}", e.getMessage(), e);
            return false;
        }
    }

    private List<Room> parseJsonToRooms(String json) {
        List<Room> rooms = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            logger.warn("❌ JSON пустой или null");
            return rooms;
        }

        try {
            logger.debug("🔧 Начинаем парсинг JSON номеров...");

            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                logger.info("📭 Нет данных о номерах");
                return rooms;
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

                Room room = parseRoomObject(obj);
                if (room != null) {
                    rooms.add(room);
                }
            }

            logger.info("🎯 Итого распаршено номеров: {}", rooms.size());
            return rooms;

        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга номеров: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private Room parseRoomObject(String jsonObject) {
        try {
            Integer roomNumber = apiService.extractIntegerValue(jsonObject, "roomNumber");
            String roomType = apiService.extractStringValue(jsonObject, "roomType");
            String status = apiService.extractStringValue(jsonObject, "status");
            String clientPassport = apiService.extractStringValue(jsonObject, "clientPassport");
            String checkInDate = apiService.extractStringValue(jsonObject, "checkInDate");
            String checkOutDate = apiService.extractStringValue(jsonObject, "checkOutDate");

            if (roomNumber != null && roomType != null) {
                Room room = new Room(roomNumber, roomType, status != null ? status : "free",
                        clientPassport, checkInDate, checkOutDate);

                //TODO: remove or add this logger
                //logger.debug("✅ Создан номер: {} ({})", roomNumber, roomType);
                return room;
            }

        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга объекта номера: {}", e.getMessage(), e);
        }
        return null;
    }

    public boolean clearRoomsData() {
        logger.info("🗑️ Очистка всех данных номеров");
        try {
            String response = apiService.executeRequest("/rooms/clear", "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Данные номеров успешно очищены, клиенты выселены");
            } else {
                logger.warn("⚠️ Не удалось очистить данные номеров. Ответ: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка очистки номеров: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Проверяет доступность с подробной информацией
     */
    public Map<String, Object> checkAvailabilityDetailed(int roomNumber, String checkInDate, String checkOutDate) {
        logger.info("🔍 Детальная проверка доступности номера {} с {} по {}",
                roomNumber, checkInDate, checkOutDate);

        try {
            String jsonBody = String.format(
                    "{\"roomNumber\":%d,\"checkInDate\":\"%s\",\"checkOutDate\":\"%s\"}",
                    roomNumber, checkInDate, checkOutDate
            );

            String response = apiService.executeRequest("/rooms/check-availability-detailed", "POST", jsonBody);

            if (response != null) {
                // Парсим JSON ответ
                boolean available = response.contains("\"available\":true");
                String historyInfo = extractHistoryInfo(response);

                Map<String, Object> result = new HashMap<>();
                result.put("available", available);
                result.put("historyInfo", historyInfo);
                result.put("rawResponse", response);

                return result;
            }

            return Map.of("available", false, "error", "No response from server");

        } catch (Exception e) {
            logger.error("❌ Ошибка детальной проверки доступности: {}", e.getMessage(), e);
            return Map.of("available", false, "error", e.getMessage());
        }
    }

    private String extractHistoryInfo(String json) {
        try {
            // Простая обработка JSON для извлечения информации об истории
            if (json.contains("bookingHistory")) {
                return "История бронирований доступна";
            }
            return "Нет истории бронирований";
        } catch (Exception e) {
            return "Ошибка при анализе истории";
        }
    }

    /**
     * Получает все номера определенного типа (включая занятые)
     */
    public List<Room> getRoomsByType(String roomType) {
        logger.info("🔄 Получаем номера типа: {}", roomType);
        try {
            List<Room> allRooms = getAllRooms();
            List<Room> filteredRooms = allRooms.stream()
                    .filter(room -> roomType.equals(room.getRoomType()))
                    .collect(Collectors.toList());

            logger.info("✅ Найдено {} номеров типа {}", filteredRooms.size(), roomType);
            return filteredRooms;

        } catch (Exception e) {
            logger.error("❌ Ошибка получения номеров по типу: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Получает доступные номера на указанные даты
     */
    public List<Room> getAvailableRooms(String roomType, String checkInDate, String checkOutDate) {
        logger.info("🔄 Получаем доступные номера типа {} с {} по {}",
                roomType, checkInDate, checkOutDate);
        try {
            List<Room> roomsByType = getRoomsByType(roomType);
            List<Room> availableRooms = new ArrayList<>();

            for (Room room : roomsByType) {
                if (isRoomAvailable(room.getRoomNumber(), checkInDate, checkOutDate)) {
                    availableRooms.add(room);
                }
            }

            logger.info("✅ Найдено {} доступных номеров", availableRooms.size());
            return availableRooms;

        } catch (Exception e) {
            logger.error("❌ Ошибка получения доступных номеров: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}