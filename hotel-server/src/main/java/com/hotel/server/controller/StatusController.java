package com.hotel.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для проверки статуса системы
 */
@RestController
@RequestMapping("/api")
public class StatusController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Статус системы в формате JSON
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("status", "🟢 ONLINE");
        status.put("server", "Hotel Management System");
        status.put("version", "1.0.0");
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        status.put("endpoints", Map.of(
                "clients", "GET/POST /api/clients",
                "staff", "GET/POST /api/staff",
                "rooms", "GET /api/rooms",
                "freeRooms", "GET /api/rooms/free"
        ));
        status.put("authentication", "Basic Auth: admin/hotel123");

        try {
            // Проверяем подключение к БД
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            status.put("database", "🟢 CONNECTED");

            // Базовая статистика
            status.put("totalClients", getCount("SELECT COUNT(*) FROM clients WHERE status = 'active'"));
            status.put("totalStaff", getCount("SELECT COUNT(*) FROM staff WHERE status = 'active'"));
            status.put("totalRooms", getCount("SELECT COUNT(*) FROM rooms"));

        } catch (Exception e) {
            status.put("database", "🔴 ERROR: " + e.getMessage());
            status.put("totalClients", 0);
            status.put("totalStaff", 0);
            status.put("totalRooms", 0);
        }

        return status;
    }

    private int getCount(String sql) {
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}