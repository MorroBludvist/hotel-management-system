package com.hotel.server.service;

import com.hotel.server.model.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {
    private final JdbcTemplate jdbcTemplate;

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
}