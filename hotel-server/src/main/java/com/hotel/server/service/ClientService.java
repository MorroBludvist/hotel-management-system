package com.hotel.server.service;

import com.hotel.server.config.SqlQueries;
import com.hotel.server.model.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        return jdbcTemplate.query(SqlQueries.CLIENT_SELECT_ALL, clientRowMapper());
    }

    public boolean addClient(Client client) {
        try {
            int result = jdbcTemplate.update(SqlQueries.CLIENT_INSERT,
                    client.getFirstName(),
                    client.getLastName(),
                    client.getPassportNumber(),
                    client.getPhoneNumber(),
                    client.getEmail(),
                    client.getCheckInDate(),
                    client.getCheckOutDate(),
                    client.getRoomNumber(),
                    client.getRoomType()
            );
            return result > 0;
        } catch (Exception e) {
            System.err.println("Ошибка добавления клиента: " + e.getMessage());
            return false;
        }
    }

    public RowMapper<Client> clientRowMapper() {
        return (rs, rowNum) -> {
            logger.trace("Обработка ResultSet для строки #{}, данные:", rowNum);
            logger.trace("first_name: {}", rs.getString("first_name"));
            logger.trace("last_name: {}", rs.getString("last_name"));
            logger.trace("passport_number: {}", rs.getString("passport_number"));
            logger.trace("phone_number: {}", rs.getString("phone_number"));
            logger.trace("email: {}", rs.getString("email"));
            logger.trace("check_in_date: {}", rs.getString("check_in_date"));
            logger.trace("check_out_date: {}", rs.getString("check_out_date"));
            logger.trace("room_number: {}", rs.getInt("room_number"));
            logger.trace("room_type: {}", rs.getString("room_type"));

            Client client = new Client();
            client.setFirstName(rs.getString("first_name"));
            client.setLastName(rs.getString("last_name"));
            client.setPassportNumber(rs.getString("passport_number"));
            client.setPhoneNumber(rs.getString("phone_number"));
            client.setEmail(rs.getString("email"));
            client.setCheckInDate(rs.getString("check_in_date"));
            client.setCheckOutDate(rs.getString("check_out_date"));
            client.setRoomNumber(rs.getInt("room_number"));
            client.setRoomType(rs.getString("room_type"));
            return client;
        };
    }

    public boolean clearAll() {
        logger.info("🗑️ Очистка всех клиентов и освобождение номеров");
        try {
            // Сначала освобождаем все номера
            jdbcTemplate.update(SqlQueries.ROOM_FREE_ALL);
            logger.info("✅ Все номера освобождены");

            // Затем удаляем всех клиентов
            int deletedClients = jdbcTemplate.update(SqlQueries.CLIENT_DELETE_ALL);
            logger.info("✅ Удалено клиентов: {}", deletedClients);

            // Очищаем историю бронирований
            jdbcTemplate.update(SqlQueries.BOOKING_HISTORY_DELETE_ALL);
            logger.info("✅ История бронирований очищена");

            return true;
        } catch (Exception e) {
            logger.error("❌ Ошибка очистки клиентов: {}", e.getMessage(), e);
            return false;
        }
    }
}
