package com.hotel.server.controller;

import com.hotel.server.model.Client;
import com.hotel.server.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody Client client) {
        Map<String, Object> result = bookingService.checkInClient(client);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(@RequestBody Map<String, String> request) {
        String passportNumber = request.get("passportNumber");
        Map<String, Object> result = bookingService.checkOutClient(passportNumber);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getAllBookingHistory() {
        return bookingService.getAllBookingHistory();
    }

    @GetMapping("/history/room/{roomNumber}")
    public List<Map<String, Object>> getBookingHistoryByRoom(@PathVariable Integer roomNumber) {
        return bookingService.getBookingHistoryByRoom(roomNumber);
    }
}