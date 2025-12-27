package com.hotel.client.service;

import com.hotel.client.model.Staff;
import com.hotel.client.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class StaffService {
    private static final Logger logger = LogManager.getLogger(StaffService.class);

    private final ApiService apiService;

    public StaffService(ApiService apiService) {
        this.apiService = apiService;
        logger.debug("StaffService инициализирован с Jackson");
    }

    public List<Staff> getAllStaff() {
        logger.info("🔄 Получаем список всех сотрудников");
        try {
            String response = apiService.executeRequest("/staff", "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Staff> staffList = JsonUtils.fromJsonList(response, Staff.class);
                logger.info("✅ Успешно загружено {} сотрудников", staffList.size());
                return staffList;
            } else {
                logger.error("❌ Сервер вернул некорректный ответ: {}", response);
                return List.of();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения сотрудников: {}", e.getMessage(), e);
            return List.of();
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

            // Автоматическая сериализация в JSON
            String jsonBody = JsonUtils.toJson(staff);
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

    //TODO: доделать
    public boolean dismissStaff() {
        logger.info("🗑️ Очистка всех данных сотрудника");
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

    /**
     * Удалить сотрудника по паспорту
     */
    public boolean deleteStaff(String passportNumber) {
        logger.info("🗑️ Удаление сотрудника с паспортом: {}", passportNumber);
        try {
            String response = apiService.executeRequest("/staff/" + passportNumber, "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Сотрудник с паспортом {} успешно удален", passportNumber);
            } else {
                logger.warn("⚠️ Не удалось удалить сотрудника. Ответ сервера: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка удаления сотрудника {}: {}", passportNumber, e.getMessage(), e);
            return false;
        }
    }
}