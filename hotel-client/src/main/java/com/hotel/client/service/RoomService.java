package com.hotel.client.service;

import com.hotel.client.model.Room;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoomService {
    private final ApiService apiService;
    private static final Logger logger = LogManager.getLogger(RoomService.class);

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

        if (json == null || json.trim().isEmpty()) {
            return rooms;
        }

        try {
            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                return rooms;
            }

            String[] objects = cleanJson.split("\\},\\s*\\{");

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

            System.out.println("🎯 Распаршено номеров: " + rooms.size());

        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга номеров: " + e.getMessage());
        }
        return rooms;
    }

    private Room parseRoomObject(String jsonObject) {
        try {
            Integer roomNumber = apiService.extractIntegerValue(jsonObject, "roomNumber");
            String roomType = apiService.extractStringValue(jsonObject, "roomType");
            String status = apiService.extractStringValue(jsonObject, "status");
            String clientPassport = apiService.extractStringValue(jsonObject, "clientPassport");
            String checkInDate = apiService.extractStringValue(jsonObject, "checkInDate");
            String checkOutDate = apiService.extractStringValue(jsonObject, "checkOutDate");

            //TODO: вернуть редуцированный конструктор???
            if (roomNumber != null && roomType != null) {
                Room room = new Room(roomNumber, roomType, status != null ? status : "free",
                        clientPassport, checkInDate, checkOutDate);
                // Дополнительные поля если нужны
                return room;
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга объекта номера: " + e.getMessage());
        }
        return null;
    }
}