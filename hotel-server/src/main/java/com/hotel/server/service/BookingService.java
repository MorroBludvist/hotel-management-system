package com.hotel.server.service;

import com.hotel.server.model.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {
    private final JdbcTemplate jdbcTemplate;
    private final RoomService roomService;
    private final ClientService clientService;

    public BookingService(JdbcTemplate jdbcTemplate, RoomService roomService, ClientService clientService) {
        this.jdbcTemplate = jdbcTemplate;
        this.roomService = roomService;
        this.clientService = clientService;
    }

    /**
     * Заселение клиента
     */
    @Transactional
    public boolean checkInClient(Client client) {
        try {
            // 1. Проверяем доступность номера
            if (!isRoomAvailable(client.getRoomNumber(), client.getCheckInDate(), client.getCheckOutDate())) {
                return false;
            }

            // 2. Проверяем, не заселен ли уже клиент
            if (clientExists(client.getPassportNumber())) {
                return false;
            }

            // 3. Занимаем номер
            roomService.updateRoomStatus(client.getRoomNumber(), "occupied");

            // 4. Добавляем клиента
            boolean clientAdded = clientService.addClient(client);

            // 5. Добавляем в историю
            if (clientAdded) {
                addToBookingHistory(client.getRoomNumber(), client.getPassportNumber(),
                        client.getCheckInDate(), client.getCheckOutDate());
            }

            return clientAdded;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Выселение клиента
     */
    @Transactional
    public boolean checkOutClient(String passportNumber) {
        try {
            // Находим клиента и его номер
            String findClientSql = "SELECT * FROM clients WHERE passport_number = ? AND status = 'active'";
            Client client = jdbcTemplate.queryForObject(findClientSql, (rs, rowNum) -> {
                Client c = new Client();
                c.setPassportNumber(rs.getString("passport_number"));
                c.setRoomNumber(rs.getInt("room_number"));
                c.setFirstName(rs.getString("first_name"));
                c.setLastName(rs.getString("last_name"));
                return c;
            }, passportNumber);

            if (client == null) return false;

            // Освобождаем комнату
            roomService.updateRoomStatus(client.getRoomNumber(), "free");

            // Удаляем клиента (каскадно удалится история бронирований)
            return clientService.deleteClient(passportNumber);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Проверка возможности бронирования
     */
    public Map<String, Object> validateBooking(Client client) {
        Map<String, Object> result = new HashMap<>();

        boolean roomAvailable = isRoomAvailable(client.getRoomNumber(), client.getCheckInDate(), client.getCheckOutDate());
        boolean clientExists = clientExists(client.getPassportNumber());

        result.put("valid", roomAvailable && !clientExists);
        result.put("roomAvailable", roomAvailable);
        result.put("clientExists", clientExists);
        result.put("message", roomAvailable && !clientExists ?
                "Заселение возможно" :
                (!roomAvailable ? "Номер недоступен" : "Клиент уже заселен"));

        return result;
    }

    /**
     * Получает всю историю бронирований
     */
    public List<Map<String, Object>> getAllBookingHistory() {
        String sql = "SELECT * FROM booking_history ORDER BY check_in_date DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> history = new HashMap<>();
            history.put("id", rs.getLong("id"));
            history.put("roomNumber", rs.getInt("room_number"));
            history.put("clientPassport", rs.getString("client_passport"));
            history.put("checkInDate", rs.getString("check_in_date"));
            history.put("checkOutDate", rs.getString("check_out_date"));
            history.put("totalPrice", rs.getDouble("total_price"));
            history.put("status", rs.getString("status"));
            return history;
        });
    }

    /**
     * Получает историю бронирований для конкретного номера
     */
    public List<Map<String, Object>> getBookingHistoryByRoom(Integer roomNumber) {
        String sql = "SELECT * FROM booking_history WHERE room_number = ? ORDER BY check_in_date DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> history = new HashMap<>();
            history.put("id", rs.getLong("id"));
            history.put("roomNumber", rs.getInt("room_number"));
            history.put("clientPassport", rs.getString("client_passport"));
            history.put("checkInDate", rs.getString("check_in_date"));
            history.put("checkOutDate", rs.getString("check_out_date"));
            history.put("totalPrice", rs.getDouble("total_price"));
            history.put("status", rs.getString("status"));
            return history;
        }, roomNumber);
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private boolean isRoomAvailable(Integer roomNumber, String checkInDate, String checkOutDate) {
        // Проверяем только историю бронирований, так как текущая занятость теперь через статус комнаты
        String sql = """
            SELECT COUNT(*) FROM booking_history 
            WHERE room_number = ? AND status = 'completed'
            AND NOT (check_out_date <= ? OR check_in_date >= ?)
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roomNumber, checkInDate, checkOutDate);
        return count == 0;
    }

    private boolean clientExists(String passportNumber) {
        String sql = "SELECT COUNT(*) FROM clients WHERE passport_number = ? AND status = 'active'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, passportNumber);
        return count != null && count > 0;
    }

    private void addToBookingHistory(Integer roomNumber, String clientPassport, String checkInDate, String checkOutDate) {
        String sql = "INSERT INTO booking_history (room_number, client_passport, check_in_date, check_out_date, total_price, status) VALUES (?, ?, ?, ?, 0, 'completed')";
        jdbcTemplate.update(sql, roomNumber, clientPassport, checkInDate, checkOutDate);
    }
}