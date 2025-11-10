package com.hotel.server.controller;

import com.hotel.server.model.Room;
import com.hotel.server.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

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
            response.put("message", "Дата обновлена, проверена занятость номеров");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}