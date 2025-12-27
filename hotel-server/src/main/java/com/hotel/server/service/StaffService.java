package com.hotel.server.service;

import com.hotel.server.config.SqlQueries;
import com.hotel.server.model.Staff;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class StaffService {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LogManager.getLogger(StaffService.class);

    public StaffService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Staff> getAllStaff() {
        logger.info("📋 Получение списка всех сотрудников");
        try {
            List<Staff> staffList = jdbcTemplate.query(SqlQueries.STAFF_SELECT_ALL, staffRowMapper());
            logger.info("✅ Получено {} сотрудников", staffList.size());
            return staffList;
        } catch (Exception e) {
            logger.error("❌ Ошибка получения сотрудников: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения сотрудников", e);
        }
    }

    public Staff getStaffByPassport(String passportNumber) {
        logger.info("🔍 Поиск сотрудника по паспорту: {}", passportNumber);
        try {
            List<Staff> staffList = jdbcTemplate.query(
                    SqlQueries.STAFF_SELECT_BY_PASSPORT,
                    staffRowMapper(),
                    passportNumber
            );

            if (!staffList.isEmpty()) {
                Staff staff = staffList.get(0);
                logger.info("✅ Найден сотрудник: {} {}", staff.getFirstName(), staff.getLastName());
                return staff;
            } else {
                logger.warn("⚠️ Сотрудник с паспортом {} не найден", passportNumber);
                return null;
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка поиска сотрудника: {}", e.getMessage());
            return null;
        }
    }

    public boolean addStaff(Staff staff) {
        logger.info("👤 Добавление сотрудника: {} {} (паспорт: {})",
                staff.getFirstName(), staff.getLastName(), staff.getPassportNumber());
        try {
            // Проверяем, существует ли уже сотрудник с таким паспортом
            Staff existingStaff = getStaffByPassport(staff.getPassportNumber());
            if (existingStaff != null) {
                logger.warn("⚠️ Сотрудник с паспортом {} уже существует", staff.getPassportNumber());
                return false;
            }

            int result = jdbcTemplate.update(SqlQueries.STAFF_INSERT,
                    staff.getPassportNumber(),
                    staff.getFirstName(),
                    staff.getLastName(),
                    staff.getPosition(),
                    staff.getPhoneNumber(),
                    staff.getEmail(),
                    staff.getHireDate(),
                    staff.getSalary(),
                    staff.getDepartment()
            );

            boolean success = result > 0;
            if (success) {
                logger.info("✅ Сотрудник успешно добавлен в БД");
            } else {
                logger.warn("⚠️ Не удалось добавить сотрудника в БД");
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка добавления сотрудника: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteStaff(String passportNumber) {
        logger.info("🗑️ Удаление сотрудника по паспорту: {}", passportNumber);
        try {
            // Сначала проверяем существует ли сотрудник
            Staff staff = getStaffByPassport(passportNumber);
            if (staff == null) {
                logger.warn("⚠️ Не удалось удалить: сотрудник не найден");
                return false;
            }

            // Удаляем сотрудника
            int result = jdbcTemplate.update(SqlQueries.STAFF_DELETE_BY_PASSPORT, passportNumber);
            boolean success = result > 0;

            if (success) {
                logger.info("✅ Сотрудник удален: {} {}", staff.getFirstName(), staff.getLastName());
            } else {
                logger.warn("⚠️ Не удалось удалить сотрудника из БД");
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка удаления сотрудника: {}", e.getMessage());
            return false;
        }
    }

    public boolean clearAll() {
        logger.info("🔄 Очистка всего персонала");
        try {
            int deletedStaff = jdbcTemplate.update(SqlQueries.STAFF_DELETE_ALL);
            logger.info("✅ Удалено сотрудников: {}", deletedStaff);
            return true;
        } catch (Exception e) {
            logger.error("❌ Ошибка очистки персонала: {}", e.getMessage(), e);
            return false;
        }
    }

    private RowMapper<Staff> staffRowMapper() {
        return (rs, rowNum) -> {
            Staff staff = new Staff();
            staff.setPassportNumber(rs.getString("passport_number"));
            staff.setFirstName(rs.getString("first_name"));
            staff.setLastName(rs.getString("last_name"));
            staff.setPosition(rs.getString("position"));
            staff.setPhoneNumber(rs.getString("phone_number"));
            staff.setEmail(rs.getString("email"));
            staff.setHireDate(rs.getString("hire_date"));
            staff.setSalary(rs.getDouble("salary"));
            staff.setDepartment(rs.getString("department"));
            return staff;
        };
    }
}