package com.hotel.server.service;

import com.hotel.server.config.SqlQueries;
import com.hotel.server.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class RoomService {

    private final JdbcTemplate jdbcTemplate;

    public RoomService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Room> getAllRooms() {
        return jdbcTemplate.query(SqlQueries.ROOM_SELECT_ALL, roomRowMapper());
    }

    public List<Room> getFreeRooms() {
        return jdbcTemplate.query(SqlQueries.ROOM_SELECT_FREE, roomRowMapper());
    }

    public List<Room> getOccupiedRooms() {
        return jdbcTemplate.query(SqlQueries.ROOM_SELECT_OCCUPIED, roomRowMapper());
    }

    public boolean isRoomAvailable(Integer roomNumber, String checkInDate, String checkOutDate) {
        String sql = """
            SELECT COUNT(*) FROM rooms 
            WHERE room_number = ? AND status = 'occupied' 
            AND NOT (check_out_date <= ? OR check_in_date >= ?)
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                roomNumber, checkInDate, checkOutDate);
        System.out.println(roomNumber);
        System.out.println(checkInDate);
        System.out.println(checkOutDate);
        return count == 0;
    }

    public boolean occupyRoom(Integer roomNumber, String clientPassport,
                              String checkInDate, String checkOutDate) {
        String sql = """
            UPDATE rooms 
            SET status = 'occupied', client_passport = ?, 
                check_in_date = ?, check_out_date = ?
            WHERE room_number = ?
        """;

        try {
            int result = jdbcTemplate.update(sql,
                    clientPassport, checkInDate, checkOutDate, roomNumber);
            return result > 0;
        } catch (Exception e) {
            System.err.println("❌ Ошибка занятия номера: " + e.getMessage());
            return false;
        }
    }

    public boolean freeRoom(Integer roomNumber) {
        String sql = """
            UPDATE rooms 
            SET status = 'free', client_passport = NULL, 
                check_in_date = NULL, check_out_date = NULL
            WHERE room_number = ?
        """;

        try {
            int result = jdbcTemplate.update(sql, roomNumber);
            return result > 0;
        } catch (Exception e) {
            System.err.println("❌ Ошибка освобождения номера: " + e.getMessage());
            return false;
        }
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

    private RowMapper<Room> roomRowMapper() {
        return (rs, rowNum) -> {
            Room room = new Room();
            room.setRoomNumber(rs.getInt("room_number"));
            room.setRoomType(rs.getString("room_type"));
            room.setStatus(rs.getString("status"));
            room.setClientPassport(rs.getString("client_passport"));
            room.setCheckInDate(rs.getString("check_in_date"));
            room.setCheckOutDate(rs.getString("check_out_date"));
            return room;
        };
    }
}
