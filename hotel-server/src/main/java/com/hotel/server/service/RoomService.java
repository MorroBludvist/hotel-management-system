package com.hotel.server.service;

import com.hotel.server.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    private final JdbcTemplate jdbcTemplate;

    public RoomService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Room> getAllRooms() {
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Room room = new Room();
            room.setRoomNumber(rs.getInt("room_number"));
            room.setRoomType(rs.getString("room_type"));
            room.setStatus(rs.getString("status"));
            return room;
        });
    }

    public List<Room> getFreeRooms() {
        String sql = "SELECT * FROM rooms WHERE status = 'free' ORDER BY room_number";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Room room = new Room();
            room.setRoomNumber(rs.getInt("room_number"));
            room.setRoomType(rs.getString("room_type"));
            room.setStatus(rs.getString("status"));
            return room;
        });
    }

    public List<Room> getOccupiedRooms() {
        String sql = "SELECT * FROM rooms WHERE status = 'occupied' ORDER BY room_number";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Room room = new Room();
            room.setRoomNumber(rs.getInt("room_number"));
            room.setRoomType(rs.getString("room_type"));
            room.setStatus(rs.getString("status"));
            return room;
        });
    }

    public boolean updateRoomStatus(int roomNumber, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE room_number = ?";
        int result = jdbcTemplate.update(sql, status, roomNumber);
        return result > 0;
    }

    public boolean clearRoom(int roomNumber) {
        // Удаляем клиентов этой комнаты (каскадно удалится история)
        String deleteClientsSql = "DELETE FROM clients WHERE room_number = ?";
        jdbcTemplate.update(deleteClientsSql, roomNumber);

        // Освобождаем комнату
        String updateRoomSql = "UPDATE rooms SET status = 'free' WHERE room_number = ?";
        int result = jdbcTemplate.update(updateRoomSql, roomNumber);
        return result > 0;
    }

    public boolean clearAll() {
        // Удаляем всех клиентов (каскадно удалится история)
        jdbcTemplate.update("DELETE FROM clients");
        // Освобождаем все комнаты
        jdbcTemplate.update("UPDATE rooms SET status = 'free'");
        return true;
    }

    public void checkRoomOccupancy(String currentDate) {
        // Освобождаем номера, у которых дата выезда наступила
        String freeRoomsSql = """
            UPDATE rooms SET status = 'free', client_passport = NULL,
            check_in_date = NULL, check_out_date = NULL
            WHERE status = 'occupied' AND check_out_date <= ?
        """;
        jdbcTemplate.update(freeRoomsSql, currentDate);

        // Занимаем номера, у которых дата заезда наступила
        String occupyRoomsSql = """
            UPDATE rooms SET status = 'occupied'
            WHERE status = 'free' AND check_in_date <= ? AND check_out_date > ?
        """;
        jdbcTemplate.update(occupyRoomsSql, currentDate, currentDate);
    }
}