package com.hotel.server.controller;

import com.hotel.server.model.Client;
import com.hotel.server.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Заселение клиента
     */
    @PostMapping("/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody Client client) {
        try {
            System.out.println("🏨 Запрос на заселение: " + client.getFirstName() + " " +
                    client.getLastName() + " в номер " + client.getRoomNumber());

            boolean success = bookingService.checkInClient(client);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "Клиент успешно заселен" :
                    "Не удалось заселить клиента. Проверьте доступность номера.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Ошибка заселения: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Выселение клиента
     */
    @PostMapping("/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(@RequestBody Map<String, String> request) {
        try {
            String passportNumber = request.get("passportNumber");
            System.out.println("🚪 Запрос на выселение клиента с паспортом: " + passportNumber);

            boolean success = bookingService.checkOutClient(passportNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "Клиент успешно выселен" :
                    "Не удалось найти клиента для выселения");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Ошибка выселения: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Проверка возможности заселения
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateBooking(@RequestBody Client client) {
        try {
            Map<String, Object> validationResult = bookingService.validateBooking(client);
            return ResponseEntity.ok(validationResult);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("valid", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Получить всю историю бронирований
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getAllBookingHistory() {
        try {
            List<Map<String, Object>> history = bookingService.getAllBookingHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения всей истории бронирований: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Получить историю бронирований для конкретного номера
     */
    @GetMapping("/history/room/{roomNumber}")
    public ResponseEntity<List<Map<String, Object>>> getBookingHistoryByRoom(@PathVariable Integer roomNumber) {
        try {
            List<Map<String, Object>> history = bookingService.getBookingHistoryByRoom(roomNumber);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения истории для номера " + roomNumber + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Добавить запись в историю бронирований
     */
    @PostMapping("/history")
    public ResponseEntity<Map<String, Object>> addBookingHistory(@RequestBody Map<String, Object> request) {
        try {
            Integer roomNumber = (Integer) request.get("roomNumber");
            String clientPassport = (String) request.get("clientPassport");
            String checkInDate = (String) request.get("checkInDate");
            String checkOutDate = (String) request.get("checkOutDate");

            boolean success = bookingService.addToBookingHistory(roomNumber, clientPassport, checkInDate, checkOutDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "Запись добавлена в историю бронирований" :
                    "Ошибка добавления в историю бронирований");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Ошибка добавления в историю бронирований: " + e.getMessage());
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Очистка истории бронирований по паспорту клиента
     */
    @DeleteMapping("/history/passport/{passport}")
    public ResponseEntity<Map<String, Object>> clearBookingHistoryByPassport(@PathVariable String passport) {
        try {
            boolean success = bookingService.deleteBookingHistoryByPassport(passport);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "История бронирований для клиента очищена" :
                    "Ошибка очистки истории бронирований");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Ошибка очистки истории для паспорта " + passport + ": " + e.getMessage());
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Очистка всей истории бронирований
     */
    @DeleteMapping("/history/clear")
    public ResponseEntity<Map<String, Object>> clearAllBookingHistory() {
        try {
            boolean success = bookingService.clearAllBookingHistory();

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "Вся история бронирований очищена" :
                    "Ошибка очистки истории бронирований");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Ошибка очистки всей истории бронирований: " + e.getMessage());
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Получить статистику бронирований
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        try {
            Map<String, Object> statistics = bookingService.getBookingStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения статистики бронирований: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Не удалось получить статистику")
            );
        }
    }
}