package com.hotel.server.controller;

import com.hotel.server.model.Room;
import com.hotel.server.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * REST контроллер для управления номерами отеля.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private static final Logger logger = LogManager.getLogger(RoomController.class);

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/free")
    public ResponseEntity<List<Room>> getFreeRooms() {
        try {
            List<Room> rooms = roomService.getFreeRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/occupied")
    public ResponseEntity<List<Room>> getOccupiedRooms() {
        try {
            List<Room> rooms = roomService.getOccupiedRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkRoomAvailability(
            @RequestBody Map<String, String> request) {
        System.out.println("Trying to check availability");
        try {
            Integer roomNumber = Integer.parseInt(request.get("roomNumber"));
            String checkInDate = request.get("checkInDate");
            String checkOutDate = request.get("checkOutDate");

            boolean available = roomService.isRoomAvailable(roomNumber, checkInDate, checkOutDate);

            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("roomNumber", roomNumber);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/advance-date")
    public ResponseEntity<Map<String, Object>> advanceDate(@RequestBody Map<String, String> request) {
        try {
            String currentDate = request.get("currentDate");
            roomService.checkRoomOccupancy(currentDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Дата обновлена, проверена занятость номеров");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Получить историю бронирований номера
     */
    @GetMapping("/{roomNumber}/history")
    public ResponseEntity<List<Map<String, Object>>> getRoomHistory(@PathVariable Integer roomNumber) {
        try {
            List<Map<String, Object>> history = roomService.getBookingHistory(roomNumber);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Проверить доступность номера с подробной информацией
     */
    @PostMapping("/check-availability-detailed")
    public ResponseEntity<Map<String, Object>> checkRoomAvailabilityDetailed(
            @RequestBody Map<String, String> request) {
        try {
            Integer roomNumber = Integer.parseInt(request.get("roomNumber"));
            String checkInDate = request.get("checkInDate");
            String checkOutDate = request.get("checkOutDate");

            boolean available = roomService.isRoomAvailable(roomNumber, checkInDate, checkOutDate);

            // Получаем историю для отладки
            List<Map<String, Object>> history = roomService.getBookingHistory(roomNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("roomNumber", roomNumber);
            response.put("checkInDate", checkInDate);
            response.put("checkOutDate", checkOutDate);
            response.put("bookingHistory", history);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Очистка всех номеров (выселение клиентов)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllRooms() {
        try {
            logger.info("🔄 Запрос на очистку всех номеров");
            boolean success = roomService.clearAll();

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "Все номера очищены и клиенты выселены" :
                    "Ошибка очистки номеров");

            logger.info("✅ Ответ очистки номеров: {}", success);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Ошибка в контроллере очистки номеров: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}