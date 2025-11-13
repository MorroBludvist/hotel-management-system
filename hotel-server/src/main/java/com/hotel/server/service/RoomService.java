package com.hotel.server.service;

import com.hotel.server.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.hotel.server.config.SqlQueries.*;

@Service
public class RoomService {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LogManager.getLogger(RoomService.class);

    public RoomService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Room> getAllRooms() {
        return jdbcTemplate.query(ROOM_SELECT_ALL, (rs, rowNum) -> {
            Room room = new Room();
            room.setRoomNumber(rs.getInt("room_number"));
            room.setRoomType(rs.getString("room_type"));
            room.setStatus(rs.getString("status"));
            return room;
        });
    }

    public List<Room> getFreeRooms() {
        return jdbcTemplate.query(ROOM_SELECT_FREE, (rs, rowNum) -> {
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
        int result = jdbcTemplate.update(ROOM_FREE, roomNumber);
        return result > 0;
    }

    public boolean clearAll() {
        // Удаляем всех клиентов (каскадно удалится история)
        jdbcTemplate.update("DELETE FROM clients");
        // Освобождаем все комнаты`
        jdbcTemplate.update("UPDATE rooms SET status = 'free'");
        return true;
    }

    public void checkRoomOccupancy(String currentDate) {
        try {
            // 1. Заселяем клиентов (pending → active)
            String checkInClientsSql = "UPDATE clients SET status = 'active' " +
                    "WHERE status = 'pending' AND check_in_date <= ?";
            int checkedInClients = jdbcTemplate.update(checkInClientsSql, currentDate);

            // 2. Занимаем комнаты для новых заездов
            String occupyRoomsSql = "UPDATE rooms SET status = 'occupied' WHERE room_number IN (" +
                    "SELECT room_number FROM clients WHERE status = 'active' " +
                    "AND check_in_date <= ? AND check_out_date > ?)";
            int occupiedRooms = jdbcTemplate.update(occupyRoomsSql, currentDate, currentDate);

            // 3. Выселяем клиентов (active → checked_out)
            String checkOutClientsSql = "UPDATE clients SET status = 'checked_out' " +
                    "WHERE status = 'active' AND check_out_date < ?";
            int checkedOutClients = jdbcTemplate.update(checkOutClientsSql, currentDate);

            // 4. Освобождаем комнаты, где бронирование ЗАВЕРШИЛОСЬ
            String freeRoomsSql = "UPDATE rooms SET status = 'free' WHERE room_number IN (" +
                    "SELECT room_number FROM clients WHERE status = 'checked_out' " +
                    "AND check_out_date < ?)";
            int freedRooms = jdbcTemplate.update(freeRoomsSql, currentDate);

            // 5. Обновляем статус завершенных бронирований (если нужно)
            String completeBookingsSql = "UPDATE bookings SET status = 'completed' " +
                    "WHERE status = 'active' AND check_out_date < ?";
            int completedBookings = jdbcTemplate.update(completeBookingsSql, currentDate);

            logger.info("Обновление занятости: " +
                            "заселено {} клиентов, " +
                            "занято {} комнат, " +
                            "выселено {} клиентов, " +
                            "освобождено {} комнат, " +
                            "завершено {} бронирований",
                    checkedInClients, occupiedRooms, checkedOutClients, freedRooms, completedBookings);

        } catch (Exception e) {
            logger.error("Ошибка обновления занятости комнат: {}", e.getMessage());
            throw new RuntimeException("Ошибка обновления занятости", e);
        }
    }

    /**
     * Проверка доступности номера для бронирования
     */
    public boolean isRoomAvailable(Integer roomNumber, String checkInDate, String checkOutDate) {
        try {
            int overlappingBookings = jdbcTemplate.queryForObject(ROOM_CHECK_AVAILABILITY, Integer.class,
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
        int result = jdbcTemplate.update(ROOMS_UPDATE_STATUS, status, roomNumber);
        return result > 0;
    }
}