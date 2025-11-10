package com.hotel.client.service;

import com.hotel.client.model.Staff;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class StaffService {
    private static final Logger logger = LogManager.getLogger(StaffService.class);

    private final ApiService apiService;

    public StaffService(ApiService apiService) {
        this.apiService = apiService;
        logger.debug("StaffService инициализирован");
    }

    public List<Staff> getAllStaff() {
        logger.info("🔄 Получаем список всех сотрудников");
        try {
            String response = apiService.executeRequest("/staff", "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Staff> staffList = parseJsonToStaff(response);
                logger.info("✅ Успешно загружено {} сотрудников", staffList.size());
                return staffList;
            } else {
                logger.error("❌ Сервер вернул некорректный ответ: {}", response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения сотрудников: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public boolean addStaff(Staff staff) {
        logger.info("👤 Добавляем сотрудника: {} {} (паспорт: {})",
                staff.getFirstName(), staff.getLastName(), staff.getPassportNumber());

        try {
            // Валидация salary
            if (Double.isNaN(staff.getSalary()) || Double.isInfinite(staff.getSalary())) {
                logger.error("❌ Неверное значение salary: {}", staff.getSalary());
                return false;
            }

            String jsonBody = String.format(
                    "{\"passportNumber\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"position\":\"%s\"," +
                            "\"phoneNumber\":\"%s\",\"email\":\"%s\",\"hireDate\":\"%s\"," +
                            "\"salary\":%s,\"department\":\"%s\"}",
                    apiService.escapeJson(staff.getPassportNumber()),
                    apiService.escapeJson(staff.getFirstName()),
                    apiService.escapeJson(staff.getLastName()),
                    apiService.escapeJson(staff.getPosition()),
                    apiService.escapeJson(staff.getPhoneNumber()),
                    apiService.escapeJson(staff.getEmail()),
                    apiService.escapeJson(staff.getHireDate()),
                    staff.getSalary(),  // Без форматирования для избежания проблем с локалью
                    apiService.escapeJson(staff.getDepartment())
            );

            logger.debug("📨 JSON для отправки: {}", jsonBody);
            String response = apiService.executeRequest("/staff", "POST", jsonBody);

            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Сотрудник {} {} успешно добавлен",
                        staff.getFirstName(), staff.getLastName());
            } else {
                logger.warn("⚠️ Не удалось добавить сотрудника. Ответ сервера: {}", response);
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка добавления сотрудника: {}", e.getMessage(), e);
            return false;
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

            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                logger.info("📭 Нет данных о сотрудниках");
                return staffList;
            }

            String[] objects = cleanJson.split("\\},\\s*\\{");
            logger.debug("📋 Найдено объектов: {}", objects.length);

            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i].trim();

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

    private Staff parseStaffObject(String jsonObject) {
        try {
            String passportNumber = apiService.extractStringValue(jsonObject, "passportNumber");
            String firstName = apiService.extractStringValue(jsonObject, "firstName");
            String lastName = apiService.extractStringValue(jsonObject, "lastName");
            String position = apiService.extractStringValue(jsonObject, "position");
            String phoneNumber = apiService.extractStringValue(jsonObject, "phoneNumber");
            String email = apiService.extractStringValue(jsonObject, "email");
            String hireDate = apiService.extractStringValue(jsonObject, "hireDate");
            Double salary = apiService.extractDoubleValue(jsonObject, "salary");
            String department = apiService.extractStringValue(jsonObject, "department");

            logger.debug("📊 Распаршены поля: {} {}, паспорт: {}, зарплата: {}",
                    firstName, lastName, passportNumber, salary);

            if (firstName == null || lastName == null || passportNumber == null) {
                logger.warn("⚠️ Отсутствуют обязательные поля в объекте: {}", jsonObject);
                return null;
            }

            Staff staff = new Staff(
                    firstName, lastName, passportNumber, position, phoneNumber, email,
                    hireDate, salary != null ? salary : 0.0,
                    department != null ? department : "Не указан"
            );

            logger.debug("✅ Создан сотрудник: {} {}, паспорт: {}",
                    firstName, lastName, passportNumber);
            return staff;

        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга объекта сотрудника: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean clearStaffData() {
        logger.info("🗑️ Очистка всех данных сотрудников");
        try {
            String response = apiService.executeRequest("/staff/clear", "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Данные сотрудников успешно очищены");
            } else {
                logger.warn("⚠️ Не удалось очистить данные сотрудников. Ответ: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка очистки сотрудников: {}", e.getMessage(), e);
            return false;
        }
    }
}