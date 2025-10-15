package com.hotel.client.service;

import com.hotel.client.model.Staff;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StaffService {
    private final ApiService apiService;
    private static final Logger logger = LogManager.getLogger(StaffService.class);

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
            // ВАЛИДАЦИЯ salary
            if (Double.isNaN(staff.getSalary()) || Double.isInfinite(staff.getSalary())) {
                System.err.println("❌ Неверное значение salary: " + staff.getSalary());
                return false;
            }

            String jsonBody = String.format(
                    "{\"passportNumber\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"position\":\"%s\"," +
                            "\"phoneNumber\":\"%s\",\"email\":\"%s\",\"hireDate\":\"%s\"," +
                            "\"salary\":%s,\"department\":\"%s\"}",  // изменено %.2f на %s
                    apiService.escapeJson(staff.getPassportNumber()),
                    apiService.escapeJson(staff.getFirstName()),
                    apiService.escapeJson(staff.getLastName()),
                    apiService.escapeJson(staff.getPosition()),
                    apiService.escapeJson(staff.getPhoneNumber()),
                    apiService.escapeJson(staff.getEmail()),
                    apiService.escapeJson(staff.getHireDate()),
                    staff.getSalary(),  // ← Теперь без форматирования
                    apiService.escapeJson(staff.getDepartment())
            );

            System.out.println("🔍 ОТПРАВЛЯЕМЫЙ JSON: " + jsonBody); //убрать

            String response = apiService.executeRequest("/staff", "POST", jsonBody);
            System.out.println("📥 ОТВЕТ СЕРВЕРА: " + response);

            return response != null && response.contains("\"success\":true");

        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Парсим один объект сотрудника
     */
    private Staff parseStaffObject(String jsonObject) {
        try {
            //logger.trace("🔍 Парсим объект: {}", jsonObject);

            String passportNumber = apiService.extractStringValue(jsonObject, "passportNumber");
            String firstName = apiService.extractStringValue(jsonObject, "firstName");
            String lastName = apiService.extractStringValue(jsonObject, "lastName");
            String position = apiService.extractStringValue(jsonObject, "position");
            String phoneNumber = apiService.extractStringValue(jsonObject, "phoneNumber");
            String email = apiService.extractStringValue(jsonObject, "email");
            String hireDate = apiService.extractStringValue(jsonObject, "hireDate");
            Double salary = apiService.extractDoubleValue(jsonObject, "salary");
            String department = apiService.extractStringValue(jsonObject, "department");

            //logger.debug("📊 Распаршены поля: {} {}, паспорт: {}, зарплата: {}",
                    //firstName, lastName, passportNumber, salary);

            // Проверяем обязательные поля
            if (firstName == null || lastName == null || passportNumber == null) {
                //logger.warn("⚠️ Отсутствуют обязательные поля в объекте: {}", jsonObject);
                return null;
            }

            Staff staff = new Staff(
                    firstName, lastName, passportNumber, position, phoneNumber, email,
                    hireDate, salary != null ? salary : 0.0,
                    department != null ? department : "Не указан"
            );

            //logger.debug("✅ Создан сотрудник: {} {}, паспорт: {}",
                    //firstName, lastName, passportNumber);
            return staff;

        } catch (Exception e) {
            //logger.error("❌ Ошибка парсинга объекта сотрудника: {}", e.getMessage(), e);
            return null;
        }
    }

    private List<Staff> parseJsonToStaff(String json) {
        List<Staff> staffList = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            logger.warn("❌ JSON пустой или null");
            return staffList;
        }

        try {
            logger.debug("🔧 Начинаем парсинг JSON сотрудников...");
            logger.trace("📄 Исходный JSON: {}", json);

            // Убираем внешние скобки
            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                logger.info("📭 Нет данных о сотрудниках");
                return staffList;
            }

            // Разделяем на объекты
            String[] objects = cleanJson.split("\\},\\s*\\{");
            logger.debug("📋 Найдено объектов: {}", objects.length);

            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i].trim();

                // Восстанавливаем фигурные скобки
                if (i == 0 && !obj.startsWith("{")) obj = "{" + obj;
                if (i == objects.length - 1 && !obj.endsWith("}")) obj = obj + "}";
                if (i > 0 && i < objects.length - 1) {
                    if (!obj.startsWith("{")) obj = "{" + obj;
                    if (!obj.endsWith("}")) obj = obj + "}";
                }

                Staff staff = parseStaffObject(obj);
                if (staff != null) {
                    staffList.add(staff);
                }
            }

            logger.info("🎯 Итого распаршено сотрудников: {}", staffList.size());
            return staffList;

        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга сотрудников: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}