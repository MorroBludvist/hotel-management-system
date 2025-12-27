package com.hotel.server.controller;

import com.hotel.server.model.Staff;
import com.hotel.server.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * REST контроллер для управления персоналом отеля.
 */
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;
    private static final Logger logger = LogManager.getLogger(StaffController.class);

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }
    /**
     * Получить всех сотрудников
     */
    @GetMapping
    public ResponseEntity<List<Staff>> getAllStaff() {
        try {
            List<Staff> staff = staffService.getAllStaff();
            System.out.println("Отправлено сотрудников: " + staff.size());
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            System.err.println("Ошибка получения сотрудников: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Добавить нового сотрудника
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addStaff(@RequestBody Staff staff) {
        System.out.println("Staff Controller: addStaff method call");
        try {
            System.out.println("Получен сотрудник: " + staff.getFirstName() + " " + staff.getLastName());

            boolean success = staffService.addStaff(staff);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Сотрудник успешно добавлен" : "Ошибка добавления сотрудника");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Ошибка добавления сотрудника: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Очистка всего персонала
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllStaff() {
        try {
            logger.info("🔄 Запрос на очистку всего персонала");
            boolean success = staffService.clearAll();

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "Весь персонал удален" :
                    "Ошибка очистки персонала");

            logger.info("✅ Ответ очистки персонала: {}", success);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Ошибка в контроллере очистки персонала: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    /**
     * Удалить сотрудника по паспорту
     */
    @DeleteMapping("/{passportNumber}")
    public ResponseEntity<Map<String, Object>> deleteStaff(@PathVariable String passportNumber) {
        try {
            logger.info("🗑️ Запрос на удаление сотрудника с паспортом: {}", passportNumber);

            // Ищем сотрудника
            Staff staff = staffService.getStaffByPassport(passportNumber);
            if (staff == null) {
                logger.warn("⚠️ Сотрудник с паспортом {} не найден", passportNumber);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("success", false, "message", "Сотрудник не найден")
                );
            }

            // Удаляем сотрудника
            boolean success = staffService.deleteStaff(passportNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "Сотрудник успешно удален" :
                    "Ошибка удаления сотрудника");

            logger.info("✅ Результат удаления сотрудника {}: {}", passportNumber, success);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Ошибка удаления сотрудника {}: {}", passportNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}