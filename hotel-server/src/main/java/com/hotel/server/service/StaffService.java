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
        return jdbcTemplate.query(SqlQueries.STAFF_SELECT_ALL, staffRowMapper());
    }

    public boolean addStaff(Staff staff) {
        try {
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
            return result > 0;
        } catch (Exception e) {
            System.err.println("Ошибка добавления сотрудника: " + e.getMessage());
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

    public boolean clearAll() {
        logger.info("🗑️ Очистка всего персонала");
        try {
            int deletedStaff = jdbcTemplate.update(SqlQueries.STAFF_DELETE_ALL);
            logger.info("✅ Удалено сотрудников: {}", deletedStaff);
            return true;
        } catch (Exception e) {
            logger.error("❌ Ошибка очистки персонала: {}", e.getMessage(), e);
            return false;
        }
    }
}
