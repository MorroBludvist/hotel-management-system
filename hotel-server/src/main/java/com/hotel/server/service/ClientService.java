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

import static com.hotel.server.config.SqlQueries.*;

@Service
public class ClientService {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LogManager.getLogger(ClientService.class);

    public ClientService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Client> getAllClients() {
        return jdbcTemplate.query(CLIENT_SELECT_ALL, (rs, rowNum) -> {
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

    //TODO: либо убрать после рефакторинга, либо перенести сюда логику reseatClient
    /**
     * Добавление клиента - обновление данных существующего клиента
     */
    public boolean addClient(Client client) {
        int result = jdbcTemplate.update(ADD_CLIENT,
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

    /**
     * Перезаселение клиента - обновление данных существующего клиента
     */
    public boolean reseatClient(Client client) {
        int result = jdbcTemplate.update(RESEAT_CLIENT,
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

    /**
     * Удаление клиента - обновление данных существующего клиента
     */
    public boolean deleteClient(String passportNumber) {
        int result = jdbcTemplate.update(DELETE_CLIENT, passportNumber);
        return result > 0;
    }

    /**
     * Очистка всей клиентской базы
     */
    public boolean clearAll() {
        jdbcTemplate.update(CLIENT_DELETE_ALL);
        jdbcTemplate.update(ROOMS_SET_FREE);
        return true;
    }

    /**
     * Проверка существования клиента
     */
    public boolean clientExists(String passportNumber) {
        Integer count = jdbcTemplate.queryForObject(IS_CLIENT_EXISTS, Integer.class, passportNumber);
        return count > 0;
    }

    /**
     * Проверка существования клиента по паспорту с учетом статусов active и pending
     * (клиенты, которые уже заселены или ожидают заселения)
     */
    public boolean isNotClientAvailableForCheckIn(String passportNumber) {
        String sql = CLIENT_IS_AVAILABLE_TO_CHECK_IN;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, passportNumber);
        return count > 0;
    }

    /**
     * Получение клиента по номеру паспорта
     */
    public Client getClientByPassport(String passportNumber) {
        try {
            return jdbcTemplate.queryForObject(SELECT_CLIENT_BY_PASSPORT, (rs, rowNum) -> {
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