package com.hotel.server.service;

import com.hotel.server.config.SqlQueries;
import com.hotel.server.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return jdbcTemplate.query(SqlQueries.ROOM_SELECT_ALL, roomRowMapper());
    }

    public List<Room> getFreeRooms() {
        return jdbcTemplate.query(SqlQueries.ROOM_SELECT_FREE, roomRowMapper());
    }

    public List<Room> getOccupiedRooms() {
        return jdbcTemplate.query(SqlQueries.ROOM_SELECT_OCCUPIED, roomRowMapper());
    }

    /**
     * Проверяет доступность номера с учетом истории бронирований
     */
    public boolean isRoomAvailable(Integer roomNumber, String checkInDate, String checkOutDate) {
        logger.info("🔍 Проверка доступности номера {} с {} по {}",
                roomNumber, checkInDate, checkOutDate);

        try {
            // 1. Проверяем текущую занятость
            boolean currentlyAvailable = checkCurrentAvailability(roomNumber, checkInDate, checkOutDate);

            // 2. Проверяем историю бронирований на конфликты
            boolean historicallyAvailable = checkHistoricalAvailability(roomNumber, checkInDate, checkOutDate);

            boolean available = currentlyAvailable && historicallyAvailable;

            logger.info("📊 Номер {} доступен: {} (текущая: {}, историческая: {})",
                    roomNumber, available, currentlyAvailable, historicallyAvailable);

            return available;

        } catch (Exception e) {
            logger.error("❌ Ошибка проверки доступности номера: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Проверяет текущую занятость номера
     */
    private boolean checkCurrentAvailability(Integer roomNumber, String checkInDate, String checkOutDate) {
        String sql = """
            SELECT COUNT(*) FROM rooms 
            WHERE room_number = ? AND status = 'occupied' 
            AND NOT (check_out_date <= ? OR check_in_date >= ?)
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                roomNumber, checkInDate, checkOutDate);
        return count == 0;
    }

    /**
     * Проверяет историю бронирований на конфликты
     */
    private boolean checkHistoricalAvailability(Integer roomNumber, String checkInDate, String checkOutDate) {
        String sql = SqlQueries.BOOKING_HISTORY_CHECK_CONFLICT;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                roomNumber, checkInDate, checkOutDate);
        return count == 0;
    }

    /**
     * Добавляет запись в историю бронирований
     */
    public boolean addToBookingHistory(Integer roomNumber, String clientPassport,
                                       String checkInDate, String checkOutDate) {
        logger.info("📝 Добавление в историю бронирований: номер {}, клиент {}",
                roomNumber, clientPassport);

        try {
            int result = jdbcTemplate.update(SqlQueries.BOOKING_HISTORY_INSERT,
                    roomNumber, clientPassport, checkInDate, checkOutDate);

            boolean success = result > 0;
            if (success) {
                logger.info("✅ Запись добавлена в историю бронирований");
            } else {
                logger.warn("⚠️ Не удалось добавить запись в историю бронирований");
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка добавления в историю бронирований: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Получает историю бронирований для номера
     */
    public List<Map<String, Object>> getBookingHistory(Integer roomNumber) {
        try {
            return jdbcTemplate.query(SqlQueries.BOOKING_HISTORY_SELECT_BY_ROOM,
                    (rs, rowNum) -> {
                        Map<String, Object> history = new HashMap<>();
                        history.put("clientPassport", rs.getString("client_passport"));
                        history.put("checkInDate", rs.getString("check_in_date"));
                        history.put("checkOutDate", rs.getString("check_out_date"));
                        history.put("bookedAt", rs.getTimestamp("booked_at"));
                        return history;
                    }, roomNumber);
        } catch (Exception e) {
            logger.error("❌ Ошибка получения истории бронирований: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
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

    public boolean clearAll() {
        logger.info("🗑️ Очистка всех номеров с выселением клиентов");
        try {
            // Сначала выселяем всех клиентов (освобождаем номера)
            jdbcTemplate.update(SqlQueries.ROOM_FREE_ALL);
            logger.info("✅ Все клиенты выселены, номера освобождены");

            // Очищаем историю бронирований
            jdbcTemplate.update(SqlQueries.BOOKING_HISTORY_DELETE_ALL);
            logger.info("✅ История бронирований очищена");

            return true;
        } catch (Exception e) {
            logger.error("❌ Ошибка очистки номеров: {}", e.getMessage(), e);
            return false;
        }
    }
}
