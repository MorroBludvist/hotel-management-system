package com.hotel.client.service;

import com.hotel.client.model.Staff;
import java.util.ArrayList;
import java.util.List;

public class StaffService {
    private final ApiService apiService;

    public StaffService(ApiService apiService) {
        this.apiService = apiService;
    }

    public List<Staff> getAllStaff() {
        try {
            String response = apiService.executeRequest("/staff", "GET", null);
            if (response != null && response.startsWith("[")) {
                return parseJsonToStaff(response);
            } else {
                System.out.println("❌ Сервер вернул некорректный ответ: " + response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения сотрудников: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean addStaff(Staff staff) {
        try {
            String jsonBody = String.format(
                    "{\"passportNumber\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"position\":\"%s\"," +
                            "\"phoneNumber\":\"%s\",\"email\":\"%s\",\"hireDate\":\"%s\"," +
                            "\"salary\":%.2f,\"department\":\"%s\"}",
                    apiService.escapeJson(staff.getPassportNumber()),
                    apiService.escapeJson(staff.getFirstName()),
                    apiService.escapeJson(staff.getLastName()),
                    apiService.escapeJson(staff.getPosition()),
                    apiService.escapeJson(staff.getPhoneNumber()),
                    apiService.escapeJson(staff.getEmail()),
                    apiService.escapeJson(staff.getHireDate()),
                    staff.getSalary(),
                    apiService.escapeJson(staff.getDepartment())
            );

            System.out.println("📨 Отправляем сотрудника: " + jsonBody);
            String response = apiService.executeRequest("/staff", "POST", jsonBody);
            boolean success = response != null && response.contains("\"success\":true");
            System.out.println("✅ Результат добавления сотрудника: " + success);
            return success;

        } catch (Exception e) {
            System.err.println("❌ Ошибка добавления сотрудника: " + e.getMessage());
            return false;
        }
    }

    private List<Staff> parseJsonToStaff(String json) {
        List<Staff> staffList = new ArrayList<>();
        // TODO: Перенести логику парсинга из DatabaseManager
        return staffList;
    }
}