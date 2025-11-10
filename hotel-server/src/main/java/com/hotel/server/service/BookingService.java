package com.hotel.server.service;

import com.hotel.server.config.SqlQueries;
import com.hotel.server.model.Client;
import com.hotel.server.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class BookingService {
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    private final RoomService roomService;
    private final ClientService clientService;
    private final JdbcTemplate jdbcTemplate;

    public BookingService(RoomService roomService, ClientService clientService, JdbcTemplate jdbcTemplate) {
        this.roomService = roomService;
        this.clientService = clientService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== ОСНОВНЫЕ ОПЕРАЦИИ БРОНИРОВАНИЯ ====================

    /**
     * Заселение клиента с проверкой доступности и транзакцией
     */
    @Transactional
    public boolean checkInClient(Client client) {
        logger.info("🏨 Попытка заселения клиента {} {} в номер {}",
                client.getFirstName(), client.getLastName(), client.getRoomNumber());

        try {
            // 1. Проверяем доступность номера (с учетом истории)
            boolean isAvailable = roomService.isRoomAvailable(
                    client.getRoomNumber(),
                    client.getCheckInDate(),
                    client.getCheckOutDate()
            );

            if (!isAvailable) {
                logger.warn("❌ Номер {} недоступен для заселения на даты {} - {}",
                        client.getRoomNumber(), client.getCheckInDate(), client.getCheckOutDate());
                return false;
            }

            // 2. Проверяем, не заселен ли уже клиент с этим паспортом
            boolean clientExists = checkClientExists(client.getPassportNumber());
            if (clientExists) {
                logger.warn("❌ Клиент с паспортом {} уже заселен", client.getPassportNumber());
                return false;
            }

            // 3. Занимаем номер
            boolean roomOccupied = occupyRoomWithLock(
                    client.getRoomNumber(),
                    client.getPassportNumber(),
                    client.getCheckInDate(),
                    client.getCheckOutDate()
            );

            if (!roomOccupied) {
                logger.warn("❌ Не удалось занять номер {}", client.getRoomNumber());
                return false;
            }

            // 4. Добавляем клиента
            boolean clientAdded = clientService.addClient(client);
            if (!clientAdded) {
                logger.error("❌ Клиент не добавлен, откатываем занятие номера");
                throw new RuntimeException("Failed to add client");
            }

            // 5. Добавляем запись в историю бронирований
            boolean historyAdded = addToBookingHistory(
                    client.getRoomNumber(),
                    client.getPassportNumber(),
                    client.getCheckInDate(),
                    client.getCheckOutDate()
            );

            if (!historyAdded) {
                logger.warn("⚠️ Не удалось добавить запись в историю бронирований, но заселение прошло успешно");
            }

            logger.info("✅ Клиент {} {} успешно заселен в номер {}",
                    client.getFirstName(), client.getLastName(), client.getRoomNumber());
            return true;

        } catch (Exception e) {
            logger.error("❌ Ошибка при заселении: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Выселение клиента
     */
    @Transactional
    public boolean checkOutClient(String passportNumber) {
        logger.info("🚪 Выселение клиента с паспортом {}", passportNumber);

        try {
            // 1. Находим клиента и его номер
            Client client = findClientByPassport(passportNumber);
            if (client == null) {
                logger.warn("❌ Клиент с паспортом {} не найден", passportNumber);
                return false;
            }

            // 2. Освобождаем номер
            boolean roomFreed = roomService.freeRoom(client.getRoomNumber());
            if (!roomFreed) {
                logger.error("❌ Не удалось освободить номер {}", client.getRoomNumber());
                return false;
            }

            // 3. Удаляем клиента (soft delete)
            String sql = "UPDATE clients SET status = 'checked_out' WHERE passport_number = ?";
            int updated = jdbcTemplate.update(sql, passportNumber);

            boolean success = updated > 0;
            if (success) {
                logger.info("✅ Клиент {} {} выселен из номера {}",
                        client.getFirstName(), client.getLastName(), client.getRoomNumber());
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка при выселении: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Проверка возможности бронирования
     */
    public Map<String, Object> validateBooking(Client client) {
        logger.info("🔍 Проверка возможности бронирования для клиента {} в номер {}",
                client.getPassportNumber(), client.getRoomNumber());

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Проверяем доступность номера
            boolean isAvailable = roomService.isRoomAvailable(
                    client.getRoomNumber(),
                    client.getCheckInDate(),
                    client.getCheckOutDate()
            );

            // 2. Проверяем, не заселен ли уже клиент
            boolean clientExists = checkClientExists(client.getPassportNumber());

            result.put("valid", isAvailable && !clientExists);
            result.put("roomAvailable", isAvailable);
            result.put("clientExists", clientExists);
            result.put("message", isAvailable && !clientExists ?
                    "Заселение возможно" :
                    (!isAvailable ? "Номер недоступен" : "Клиент уже заселен"));

            logger.info("📊 Результат проверки: valid={}, roomAvailable={}, clientExists={}",
                    result.get("valid"), isAvailable, clientExists);

        } catch (Exception e) {
            logger.error("❌ Ошибка проверки бронирования: {}", e.getMessage());
            result.put("valid", false);
            result.put("roomAvailable", false);
            result.put("clientExists", false);
            result.put("message", "Ошибка проверки бронирования");
        }

        return result;
    }

    // ==================== ИСТОРИЯ БРОНИРОВАНИЙ ====================

    /**
     * Получает всю историю бронирований
     */
    public List<Map<String, Object>> getAllBookingHistory() {
        logger.info("🔄 Получение всей истории бронирований");
        try {
            return jdbcTemplate.query(SqlQueries.BOOKING_HISTORY_SELECT_ALL,
                    (rs, rowNum) -> {
                        Map<String, Object> history = new HashMap<>();
                        history.put("roomNumber", rs.getInt("room_number"));
                        history.put("clientPassport", rs.getString("client_passport"));
                        history.put("checkInDate", rs.getString("check_in_date"));
                        history.put("checkOutDate", rs.getString("check_out_date"));
                        history.put("bookedAt", rs.getTimestamp("booked_at"));
                        return history;
                    });
        } catch (Exception e) {
            logger.error("❌ Ошибка получения всей истории бронирований: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Получает историю бронирований для конкретного номера
     */
    public List<Map<String, Object>> getBookingHistoryByRoom(Integer roomNumber) {
        logger.info("🔄 Получение истории бронирований для номера {}", roomNumber);
        try {
            return jdbcTemplate.query(SqlQueries.BOOKING_HISTORY_SELECT_BY_ROOM,
                    (rs, rowNum) -> {
                        Map<String, Object> history = new HashMap<>();
                        history.put("roomNumber", rs.getInt("room_number"));
                        history.put("clientPassport", rs.getString("client_passport"));
                        history.put("checkInDate", rs.getString("check_in_date"));
                        history.put("checkOutDate", rs.getString("check_out_date"));
                        history.put("bookedAt", rs.getTimestamp("booked_at"));
                        return history;
                    }, roomNumber);
        } catch (Exception e) {
            logger.error("❌ Ошибка получения истории для номера {}: {}", roomNumber, e.getMessage(), e);
            return new ArrayList<>();
        }
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
     * Удаляет историю бронирований по паспорту клиента
     */
    public boolean deleteBookingHistoryByPassport(String passport) {
        logger.info("🗑️ Очистка истории бронирований для паспорта: {}", passport);
        try {
            int result = jdbcTemplate.update(SqlQueries.BOOKING_HISTORY_DELETE_BY_PASSPORT, passport);
            logger.info("✅ Удалено записей истории для паспорта {}: {}", passport, result);
            return result >= 0; // Может быть 0 если записей не было
        } catch (Exception e) {
            logger.error("❌ Ошибка удаления истории бронирований для паспорта {}: {}", passport, e.getMessage());
            return false;
        }
    }

    /**
     * Очищает всю историю бронирований
     */
    public boolean clearAllBookingHistory() {
        logger.info("🗑️ Очистка всей истории бронирований");
        try {
            int result = jdbcTemplate.update(SqlQueries.BOOKING_HISTORY_DELETE_ALL);
            logger.info("✅ Удалено всех записей истории: {}", result);
            return true;
        } catch (Exception e) {
            logger.error("❌ Ошибка очистки всей истории бронирований: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет историю бронирований на конфликты
     */
    public boolean checkHistoricalAvailability(Integer roomNumber, String checkInDate, String checkOutDate) {
        logger.debug("🔍 Проверка исторической доступности номера {}", roomNumber);
        try {
            String sql = SqlQueries.BOOKING_HISTORY_CHECK_CONFLICT;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                    roomNumber, checkInDate, checkOutDate);
            return count == 0;
        } catch (Exception e) {
            logger.error("❌ Ошибка проверки исторической доступности: {}", e.getMessage(), e);
            return false;
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Улучшенный метод занятия номера с блокировкой
     */
    private boolean occupyRoomWithLock(Integer roomNumber, String clientPassport,
                                       String checkInDate, String checkOutDate) {
        String sql = """
            UPDATE rooms 
            SET status = 'occupied', client_passport = ?, 
                check_in_date = ?, check_out_date = ?
            WHERE room_number = ? 
            AND (status = 'free' OR 
                (status = 'occupied' AND check_out_date <= ?))
        """;

        // Если комната свободна ИЛИ занята, но дата выезда уже прошла
        int updated = jdbcTemplate.update(sql,
                clientPassport, checkInDate, checkOutDate, roomNumber, checkInDate);

        return updated > 0;
    }

    /**
     * Проверка существования клиента
     */
    private boolean checkClientExists(String passportNumber) {
        String sql = "SELECT COUNT(*) FROM clients WHERE passport_number = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, passportNumber);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("❌ Ошибка проверки существования клиента: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Поиск клиента по паспорту
     */
    private Client findClientByPassport(String passportNumber) {
        String sql = "SELECT * FROM clients WHERE passport_number = ?";
        try {
            return jdbcTemplate.queryForObject(sql, clientService.clientRowMapper(), passportNumber);
        } catch (Exception e) {
            logger.warn("⚠️ Клиент с паспортом {} не найден", passportNumber);
            return null;
        }
    }

    /**
     * Получает активных клиентов (для проверок)
     */
    public List<Client> getActiveClients() {
        try {
            String sql = "SELECT * FROM clients WHERE status IS NULL OR status != 'checked_out'";
            return jdbcTemplate.query(sql, clientService.clientRowMapper());
        } catch (Exception e) {
            logger.error("❌ Ошибка получения активных клиентов: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Получает статистику по бронированиям
     */
    public Map<String, Object> getBookingStatistics() {
        logger.info("📊 Получение статистики бронирований");
        Map<String, Object> stats = new HashMap<>();

        try {
            // Количество бронирований за все время
            String totalBookingsSql = "SELECT COUNT(*) FROM booking_history";
            Integer totalBookings = jdbcTemplate.queryForObject(totalBookingsSql, Integer.class);
            stats.put("totalBookings", totalBookings != null ? totalBookings : 0);

            // Количество активных бронирований
            String activeBookingsSql = "SELECT COUNT(*) FROM clients WHERE status IS NULL OR status != 'checked_out'";
            Integer activeBookings = jdbcTemplate.queryForObject(activeBookingsSql, Integer.class);
            stats.put("activeBookings", activeBookings != null ? activeBookings : 0);

            // Самый популярный номер
            String popularRoomSql = """
                SELECT room_number, COUNT(*) as booking_count 
                FROM booking_history 
                GROUP BY room_number 
                ORDER BY booking_count DESC 
                LIMIT 1
            """;

            try {
                Map<String, Object> popularRoom = jdbcTemplate.queryForMap(popularRoomSql);
                stats.put("mostPopularRoom", popularRoom);
            } catch (Exception e) {
                stats.put("mostPopularRoom", "Нет данных");
            }

            logger.info("✅ Статистика собрана: {} бронирований, {} активных",
                    stats.get("totalBookings"), stats.get("activeBookings"));

        } catch (Exception e) {
            logger.error("❌ Ошибка получения статистики бронирований: {}", e.getMessage());
            stats.put("error", "Не удалось собрать статистику");
        }

        return stats;
    }
}