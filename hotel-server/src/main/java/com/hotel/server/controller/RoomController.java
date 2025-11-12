package com.hotel.server.controller;

import com.hotel.server.model.Room;
import com.hotel.server.service.ClientService;
import com.hotel.server.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;
    private static final Logger logger = LogManager.getLogger(RoomController.class);

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/free")
    public List<Room> getFreeRooms() {
        return roomService.getFreeRooms();
    }

    @GetMapping("/occupied")
    public List<Room> getOccupiedRooms() {
        return roomService.getOccupiedRooms();
    }

    @PutMapping("/{roomNumber}/status")
    public ResponseEntity<Map<String, Object>> updateRoomStatus(@PathVariable Integer roomNumber, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        boolean success = roomService.updateRoomStatus(roomNumber, status);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @DeleteMapping("/{roomNumber}")
    public ResponseEntity<Map<String, Object>> clearRoom(@PathVariable Integer roomNumber) {
        boolean success = roomService.clearRoom(roomNumber);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearAllRooms() {
        boolean success = roomService.clearAll();
        return ResponseEntity.ok(Map.of("success", success));
    }

    @PostMapping("/advance-date")
    public ResponseEntity<Map<String, Object>> advanceDate(@RequestBody Map<String, String> request) {
        try {
            String currentDate = request.get("currentDate");
            roomService.checkRoomOccupancy(currentDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Дата успешно обновлена, статусы комнат и бронирований проверены");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении даты: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    @PostMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(@RequestBody Map<String, Object> request) {
        try {
            Integer roomNumber = (Integer) request.get("roomNumber");
            String checkInDate = (String) request.get("checkInDate");
            String checkOutDate = (String) request.get("checkOutDate");

            boolean isAvailable = roomService.isRoomAvailable(roomNumber, checkInDate, checkOutDate);

            Map<String, Object> response = new HashMap<>();
            response.put("available", isAvailable);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка проверки доступности номера: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}