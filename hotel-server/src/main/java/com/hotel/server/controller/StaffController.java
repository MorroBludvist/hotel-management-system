package com.hotel.server.controller;

import com.hotel.server.model.Staff;
import com.hotel.server.service.StaffService;
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
}