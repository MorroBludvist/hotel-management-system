package com.hotel.server.service;

import com.hotel.server.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class RoomService {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LogManager.getLogger(RoomService.class);

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
        // 1. Освобождаем комнаты, у которых истек срок бронирования
        String freeRoomsSql = "UPDATE rooms SET status = 'free' WHERE room_number IN (" +
                "SELECT room_number FROM bookings WHERE status = 'active' AND check_out_date <= ?)";
        int freedRooms = jdbcTemplate.update(freeRoomsSql, currentDate);

        // 2. Обновляем статус завершенных бронирований
        String completeBookingsSql = "UPDATE bookings SET status = 'completed' " +
                "WHERE status = 'active' AND check_out_date <= ?";
        int completedBookings = jdbcTemplate.update(completeBookingsSql, currentDate);

        // 3. Занимаем комнаты, у которых начался срок бронирования
        String occupyRoomsSql = "UPDATE rooms SET status = 'occupied' WHERE room_number IN (" +
                "SELECT room_number FROM bookings WHERE status = 'active' AND check_in_date = ?)";
        int occupiedRooms = jdbcTemplate.update(occupyRoomsSql, currentDate);

        logger.info("Обновление занятости: освобождено {} комнат, завершено {} бронирований, занято {} комнат",
                freedRooms, completedBookings, occupiedRooms);
    }

    /**
     * Проверка доступности номера для бронирования
     */
    public boolean isRoomAvailable(Integer roomNumber, String checkInDate, String checkOutDate) {
        try {
            String sql = "SELECT COUNT(*) FROM bookings WHERE room_number = ? AND status = 'active' " +
                    "AND ((check_in_date <= ? AND check_out_date > ?) OR (check_in_date < ? AND check_out_date >= ?))";

            int overlappingBookings = jdbcTemplate.queryForObject(sql, Integer.class,
                    roomNumber, checkOutDate, checkInDate, checkOutDate, checkInDate);

            return overlappingBookings == 0;
        } catch (Exception e) {
            logger.error("Ошибка проверки доступности номера {}: {}", roomNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Обновление статуса комнаты
     */
    public boolean updateRoomStatus(int roomNumber, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE room_number = ?";
        int result = jdbcTemplate.update(sql, status, roomNumber);
        return result > 0;
    }
}