package com.hotel.client.service;

import com.hotel.client.model.Room;
import com.hotel.client.util.JsonUtils;
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
        logger.debug("RoomService инициализирован с Jackson");
    }

    public List<Room> getAllRooms() {
        logger.info("🔄 Получаем список всех номеров");
        try {
            String response = apiService.executeRequest("/rooms", "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Room> rooms = JsonUtils.fromJsonList(response, Room.class);
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
                List<Room> rooms = JsonUtils.fromJsonList(response, Room.class);
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

    public List<Room> getOccupiedRooms() {
        logger.info("🔄 Получаем список занятых номеров");
        try {
            String response = apiService.executeRequest("/rooms/occupied", "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Room> rooms = JsonUtils.fromJsonList(response, Room.class);
                logger.info("✅ Найдено {} занятых номеров", rooms.size());
                return rooms;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения занятых номеров: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public boolean isRoomAvailable(int roomNumber, String checkInDate, String checkOutDate) {
        logger.info("🔍 Проверяем доступность номера {} с {} по {}", roomNumber, checkInDate, checkOutDate);
        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("roomNumber", roomNumber);
            requestData.put("checkInDate", checkInDate);
            requestData.put("checkOutDate", checkOutDate);

            String jsonBody = JsonUtils.toJson(requestData);
            String response = apiService.executeRequest("/rooms/check-availability", "POST", jsonBody);

            boolean available = response != null && response.contains("\"available\":true");
            logger.info("📊 Номер {} доступен: {}", roomNumber, available);
            return available;

        } catch (Exception e) {
            logger.error("❌ Ошибка проверки доступности номера: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Обновление статуса номера
     */
    public boolean updateRoomStatus(int roomNumber, String status) {
        logger.info("🔄 Обновление статуса номера {} на '{}'", roomNumber, status);
        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("status", status);

            String jsonBody = JsonUtils.toJson(requestData);
            String response = apiService.executeRequest("/rooms/" + roomNumber + "/status", "PUT", jsonBody);

            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Статус номера {} обновлен на '{}'", roomNumber, status);
            } else {
                logger.warn("⚠️ Не удалось обновить статус номера. Ответ: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка обновления статуса номера: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Очистка конкретного номера
     */
    public boolean clearRoom(int roomNumber) {
        logger.info("🗑️ Очистка номера {}", roomNumber);
        try {
            String response = apiService.executeRequest("/rooms/" + roomNumber, "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Номер {} очищен", roomNumber);
            } else {
                logger.warn("⚠️ Не удалось очистить номер. Ответ: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка очистки номера: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Очистка всех номеров
     */
    public boolean clearRoomsData() {
        logger.info("🗑️ Очистка всех данных номеров");
        try {
            String response = apiService.executeRequest("/rooms", "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Данные номеров успешно очищены");
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
     * Получает все номера определенного типа
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