package com.hotel.server.service;

import com.hotel.server.model.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class BookingService {
    private final JdbcTemplate jdbcTemplate;
    private final RoomService roomService;
    private final ClientService clientService;
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    public BookingService(JdbcTemplate jdbcTemplate, RoomService roomService, ClientService clientService) {
        this.jdbcTemplate = jdbcTemplate;
        this.roomService = roomService;
        this.clientService = clientService;
    }

    /**
     * Заселение клиента
     */
    @Transactional
    public Map<String, Object> checkInClient(Client client) {
        Map<String, Object> result = new HashMap<>();
        try {

            //Проверяем доступность номера
            logger.trace("Попытка заселить клиента: {}", client);
            System.out.println(client.getPassportNumber());
            System.out.println(client.getStatus());
            boolean roomAvailable = roomService.isRoomAvailable(
                    client.getRoomNumber(), client.getCheckInDate(), client.getCheckOutDate());

            if (!roomAvailable) {
                result.put("success", false);
                result.put("error", "Номер недоступен в указанные даты");
                return result;
            }

            //Проверяем, не заселен ли уже клиент
            if (clientService.isNotClientAvailableForCheckIn(client.getPassportNumber())) {
                result.put("success", false);
                result.put("error", "Клиент с таким паспортом уже заселен");
                return result;
            }
            //Добавляем клиента
            System.out.println("ПОПЫТКА ЗАСЕЛИТЬ КЛИЕНАТ ФШАГЕЩГИАНВФЩГФФЩФЕАПЩФАФНАПГШФАФРАЩФАРФАФ");
            boolean clientAdded = clientService.reseatClient(client);


            if (clientAdded) {
                //Добавляем в историю бронирований
                addToBookingHistory(client.getRoomNumber(), client.getPassportNumber(),
                        client.getCheckInDate(), client.getCheckOutDate(), "active");

                //Обновляем статус комнаты, если сегодня дата заезда
                String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if (today.equals(client.getCheckInDate())) {
                    roomService.updateRoomStatus(client.getRoomNumber(), "occupied");
                }

                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("error", "Ошибка при добавлении клиента");
            }

            return result;
        } catch (Exception e) {
            logger.error("Ошибка при заселении клиента: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Выселение клиента
     */
    @Transactional
    public Map<String, Object> checkOutClient(String passportNumber) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Находим клиента и его номер
            Client client = clientService.getClientByPassport(passportNumber);

            if (client == null) {
                result.put("success", false);
                result.put("error", "Клиент не найден");
                return result;
            }

            // Обновляем историю бронирований
            updateBookingHistoryStatus(client.getRoomNumber(), passportNumber, "completed");

            // Освобождаем комнату
            roomService.updateRoomStatus(client.getRoomNumber(), "free");

            // Удаляем клиента
            boolean deleted = clientService.deleteClient(passportNumber);

            result.put("success", deleted);
            return result;
        } catch (Exception e) {
            logger.error("Ошибка при выселении клиента: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Получает всю историю бронирований
     */
    public List<Map<String, Object>> getAllBookingHistory() {
        String sql = "SELECT * FROM bookings ORDER BY check_in_date DESC";
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
        String sql = "SELECT * FROM bookings WHERE room_number = ? ORDER BY check_in_date DESC";
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

    private void addToBookingHistory(Integer roomNumber, String clientPassport, String checkInDate, String checkOutDate, String status) {
        String sql = "INSERT INTO bookings (room_number, client_passport, check_in_date, check_out_date, total_price, status) VALUES (?, ?, ?, ?, 0, ?)";
        jdbcTemplate.update(sql, roomNumber, clientPassport, checkInDate, checkOutDate, status);
    }

    private void updateBookingHistoryStatus(Integer roomNumber, String clientPassport, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE room_number = ? AND client_passport = ? AND status = 'active'";
        jdbcTemplate.update(sql, status, roomNumber, clientPassport);
    }
}