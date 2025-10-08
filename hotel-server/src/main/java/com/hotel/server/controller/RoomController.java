package com.hotel.server.controller;

import com.hotel.server.model.Room;
import com.hotel.server.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
/**
 * REST контроллер для управления номерами отеля.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

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
}