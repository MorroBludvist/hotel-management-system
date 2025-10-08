package com.hotel.client.service;

import com.hotel.client.model.Room;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoomService {
    private final ApiService apiService;

    public RoomService(ApiService apiService) {
        this.apiService = apiService;
    }

    public List<Room> getAllRooms() {
        try {
            String response = apiService.executeRequest("/rooms", "GET", null);
            if (response != null && response.startsWith("[")) {
                return parseJsonToRooms(response);
            } else {
                System.out.println("❌ Сервер вернул некорректный ответ для номеров");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения номеров: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Room> getFreeRooms() {
        try {
            String response = apiService.executeRequest("/rooms/free", "GET", null);
            if (response != null && response.startsWith("[")) {
                return parseJsonToRooms(response);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения свободных номеров: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean isRoomAvailable(int roomNumber, String checkInDate, String checkOutDate) {
        try {
            String jsonBody = String.format(
                    "{\"roomNumber\":%d,\"checkInDate\":\"%s\",\"checkOutDate\":\"%s\"}",
                    roomNumber, checkInDate, checkOutDate
            );

            String response = apiService.executeRequest("/rooms/check-availability", "POST", jsonBody);
            return response != null && response.contains("\"available\":true");
        } catch (Exception e) {
            System.err.println("❌ Ошибка проверки доступности номера: " + e.getMessage());
            return false;
        }
    }

    public boolean advanceDate(String currentDate) {
        try {
            String jsonBody = String.format("{\"currentDate\":\"%s\"}", currentDate);
            String response = apiService.executeRequest("/rooms/advance-date", "POST", jsonBody);
            return response != null && response.contains("\"success\":true");
        } catch (Exception e) {
            System.err.println("❌ Ошибка обновления даты: " + e.getMessage());
            return false;
        }
    }

    private List<Room> parseJsonToRooms(String json) {
        List<Room> rooms = new ArrayList<>();
        // TODO: Перенести логику парсинга из DatabaseManager
        return rooms;
    }

    public List<Room> getAllRooms() {
        try {
            String response = executeRequest("/rooms", "GET", null);
            if (response != null && response.startsWith("[")) {
                return parseJsonToRooms(response);
            } else {
                System.out.println("❌ Сервер вернул некорректный ответ для номеров");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения номеров: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Получает свободные номера
     */
    public List<Room> getFreeRooms() {
        try {
            String response = executeRequest("/rooms/free", "GET", null);
            if (response != null && response.startsWith("[")) {
                return parseJsonToRooms(response);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения свободных номеров: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}