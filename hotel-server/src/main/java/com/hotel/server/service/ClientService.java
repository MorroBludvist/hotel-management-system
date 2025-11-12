package com.hotel.server.service;

import com.hotel.server.model.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class ClientService {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LogManager.getLogger(ClientService.class);

    public ClientService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Client> getAllClients() {
        String sql = "SELECT * FROM clients WHERE status = 'active'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Client client = new Client();
            client.setPassportNumber(rs.getString("passport_number"));
            client.setFirstName(rs.getString("first_name"));
            client.setLastName(rs.getString("last_name"));
            client.setPhoneNumber(rs.getString("phone_number"));
            client.setEmail(rs.getString("email"));
            client.setCheckInDate(rs.getString("check_in_date"));
            client.setCheckOutDate(rs.getString("check_out_date"));
            client.setRoomNumber(rs.getInt("room_number"));
            client.setStatus(rs.getString("status"));
            return client;
        });
    }

    public boolean addClient(Client client) {
        String sql = "INSERT INTO clients (passport_number, first_name, last_name, phone_number, email, check_in_date, check_out_date, room_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'active')";
        int result = jdbcTemplate.update(sql,
                client.getPassportNumber(),
                client.getFirstName(),
                client.getLastName(),
                client.getPhoneNumber(),
                client.getEmail(),
                client.getCheckInDate(),
                client.getCheckOutDate(),
                client.getRoomNumber());
        return result > 0;
    }

    public boolean deleteClient(String passportNumber) {
        String sql = "DELETE FROM clients WHERE passport_number = ?";
        int result = jdbcTemplate.update(sql, passportNumber);
        return result > 0;
    }

    public boolean clearAll() {
        // Удаляем всех клиентов (каскадно удалится история бронирований)
        jdbcTemplate.update("DELETE FROM clients");
        // Освобождаем все комнаты
        jdbcTemplate.update("UPDATE rooms SET status = 'free'");
        return true;
    }

    public Map<String, Object> checkInClient(Client client) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Проверяем доступность номера
            String checkAvailabilitySql = "SELECT COUNT(*) FROM booking_history WHERE room_number = ? AND status = 'active' " +
                    "AND ((check_in_date <= ? AND check_out_date > ?) OR (check_in_date < ? AND check_out_date >= ?))";

            int overlappingBookings = jdbcTemplate.queryForObject(checkAvailabilitySql, Integer.class,
                    client.getRoomNumber(), client.getCheckOutDate(), client.getCheckInDate(),
                    client.getCheckOutDate(), client.getCheckInDate());

            if (overlappingBookings > 0) {
                response.put("success", false);
                response.put("error", "Номер недоступен в указанные даты");
                return response;
            }

            // 1. Добавляем клиента в таблицу clients
            String insertClientSql = "INSERT OR REPLACE INTO clients (passport_number, first_name, last_name, phone_number, email, check_in_date, check_out_date, room_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'active')";
            jdbcTemplate.update(insertClientSql,
                    client.getPassportNumber(),
                    client.getFirstName(),
                    client.getLastName(),
                    client.getPhoneNumber(),
                    client.getEmail(),
                    client.getCheckInDate(),
                    client.getCheckOutDate(),
                    client.getRoomNumber());

            // 2. Добавляем запись в историю бронирований
            String insertBookingSql = "INSERT INTO booking_history (room_number, client_passport, check_in_date, check_out_date, status) VALUES (?, ?, ?, ?, 'active')";
            jdbcTemplate.update(insertBookingSql,
                    client.getRoomNumber(),
                    client.getPassportNumber(),
                    client.getCheckInDate(),
                    client.getCheckOutDate());

            // 3. Обновляем статус комнаты на 'occupied' (если сегодня дата заезда)
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            if (today.equals(client.getCheckInDate())) {
                String updateRoomSql = "UPDATE rooms SET status = 'occupied' WHERE room_number = ?";
                jdbcTemplate.update(updateRoomSql, client.getRoomNumber());
            }

            response.put("success", true);
            return response;
        } catch (Exception e) {
            logger.error("Ошибка при заселении клиента: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * Проверка существования клиента по паспорту
     */
    public boolean clientExists(String passportNumber) {
        String sql = "SELECT COUNT(*) FROM clients WHERE passport_number = ? AND status = 'active'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, passportNumber);
        return count != null && count > 0;
    }

    /**
     * Получение клиента по номеру паспорта
     */
    public Client getClientByPassport(String passportNumber) {
        try {
            String sql = "SELECT * FROM clients WHERE passport_number = ? AND status = 'active'";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Client client = new Client();
                client.setPassportNumber(rs.getString("passport_number"));
                client.setFirstName(rs.getString("first_name"));
                client.setLastName(rs.getString("last_name"));
                client.setPhoneNumber(rs.getString("phone_number"));
                client.setEmail(rs.getString("email"));
                client.setCheckInDate(rs.getString("check_in_date"));
                client.setCheckOutDate(rs.getString("check_out_date"));
                client.setRoomNumber(rs.getInt("room_number"));
                client.setStatus(rs.getString("status"));
                return client;
            }, passportNumber);
        } catch (Exception e) {
            return null;
        }
    }
}