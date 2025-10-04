package com.hotel.server.service;

import com.hotel.server.config.SqlQueries;
import com.hotel.server.model.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {
    private final JdbcTemplate jdbcTemplate;

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

    private RowMapper<Client> clientRowMapper() {
        return (rs, rowNum) -> {
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
}
