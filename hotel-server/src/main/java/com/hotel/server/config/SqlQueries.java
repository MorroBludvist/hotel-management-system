package com.hotel.server.config;

import org.springframework.stereotype.Component;

/**
 * Класс для хранения всех SQL запросов приложения.
 */
@Component
public class SqlQueries {

    //ROOM QUERIES
    public static final String ROOM_SELECT_ALL = "SELECT * FROM rooms ORDER BY room_number";

    public static final String ROOM_SELECT_FREE = "SELECT * FROM rooms WHERE status = 'free' ORDER BY room_number";

    public static final String ROOM_SELECT_OCCUPIED = """
        SELECT room_number, room_type, status, client_passport, 
               check_in_date, check_out_date 
        FROM rooms 
        WHERE status = 'occupied' 
        ORDER BY room_number
    """;

    public static final String ROOM_CHECK_AVAILABILITY = "SELECT COUNT(*)" +
            " FROM bookings WHERE room_number = ? AND status = 'active' " +
            "AND ((check_in_date <= ? AND check_out_date > ?)" +
            " OR (check_in_date < ? AND check_out_date >= ?))";

    public static final String ROOM_OCCUPY = """
        UPDATE rooms 
        SET status = 'occupied', client_passport = ?, 
            check_in_date = ?, check_out_date = ?
        WHERE room_number = ?
    """;

    public static final String ROOM_FREE = "UPDATE rooms SET status = 'free' WHERE room_number = ?";

    public static final String ROOM_AUTO_FREE = """
        UPDATE rooms SET status = 'free', client_passport = NULL,
        check_in_date = NULL, check_out_date = NULL
        WHERE status = 'occupied' AND check_out_date <= ?
    """;

    public static final String ROOM_AUTO_OCCUPY = """
        UPDATE rooms SET status = 'occupied'
        WHERE status = 'free' AND check_in_date <= ? AND check_out_date > ?
    """;

    public static final String ROOM_FIND_BY_NUMBER = """
        SELECT room_number, room_type, status, client_passport, 
               check_in_date, check_out_date 
        FROM rooms 
        WHERE room_number = ?
    """;

    public static final String ROOM_INSERT = """
        INSERT INTO rooms (room_number, room_type, status, client_passport, 
                          check_in_date, check_out_date)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    public static final String ROOM_DELETE = "DELETE FROM rooms WHERE room_number = ?";

    //CLIENT QUERIES

    public static final String CLIENT_SELECT_ALL = """
        SELECT *
        FROM clients
        ORDER BY last_name, first_name
    """;

    public static final String CLIENT_FIND_BY_PASSPORT = """
        SELECT first_name, last_name, passport_number, phone_number, 
               email, check_in_date, check_out_date, room_number, room_type
        FROM clients 
        WHERE passport_number = ? AND status = 'active'
    """;

    public static final String CLIENT_INSERT = """
        INSERT INTO clients (first_name, last_name, passport_number, phone_number, 
                            email, check_in_date, check_out_date, room_number, room_type, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active')
    """;

    public static final String CLIENT_UPDATE = """
        UPDATE clients 
        SET first_name = ?, last_name = ?, phone_number = ?, email = ?,
            check_in_date = ?, check_out_date = ?, room_number = ?, room_type = ?
        WHERE passport_number = ? AND status = 'active'
    """;

    public static final String CLIENT_SOFT_DELETE = """
        UPDATE clients SET status = 'inactive' WHERE passport_number = ?
    """;

    public static final String CLIENT_CHECK_EXISTS = """
        SELECT COUNT(*) FROM clients 
        WHERE passport_number = ? AND status = 'active'
    """;

    public static final String CLIENT_FIND_BY_ROOM = """
        SELECT first_name, last_name, passport_number, phone_number, 
               email, check_in_date, check_out_date, room_number, room_type
        FROM clients 
        WHERE room_number = ? AND status = 'active'
    """;

    //STAFF QUERIES

    public static final String STAFF_SELECT_ALL = """
        SELECT passport_number, first_name, last_name, position, phone_number, 
               email, hire_date, salary, department
        FROM staff 
        WHERE status = 'active'
        ORDER BY last_name, first_name
    """;

    public static final String STAFF_INSERT = """
        INSERT INTO staff (passport_number, first_name, last_name, position, 
                          phone_number, email, hire_date, salary, department, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active')
    """;

    //REPORTING QUERIES

    public static final String REPORT_OCCUPANCY_STATS = """
        SELECT 
            COUNT(*) as total_rooms,
            SUM(CASE WHEN status = 'occupied' THEN 1 ELSE 0 END) as occupied_rooms,
            SUM(CASE WHEN status = 'free' THEN 1 ELSE 0 END) as free_rooms,
            ROUND((SUM(CASE WHEN status = 'occupied' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 2) as occupancy_rate
        FROM rooms
    """;

    public static final String REPORT_REVENUE_BY_ROOM_TYPE = """
        SELECT
            room_type,
            COUNT(*) as room_count,
            SUM(CASE WHEN status = 'occupied' THEN 1 ELSE 0 END) as occupied_count,
            AVG(price) as avg_price
        FROM rooms
        GROUP BY room_type
        ORDER BY room_type
    """;

    public static final String REPORT_CLIENT_STATS = """
        SELECT
            COUNT(*) as total_clients,
            COUNT(DISTINCT room_number) as occupied_rooms_count,
            AVG(DATEDIFF(check_out_date, check_in_date)) as avg_stay_days
        FROM clients
        WHERE status = 'active'
    """;

    //VALIDATION QUERIES

    public static final String VALIDATE_ROOM_EXISTS = """
        SELECT COUNT(*) FROM rooms WHERE room_number = ?
    """;

    public static final String VALIDATE_CLIENT_EXISTS = """
        SELECT COUNT(*) FROM clients WHERE passport_number = ? AND status = 'active'
    """;

    public static final String VALIDATE_STAFF_EXISTS = """
        SELECT COUNT(*) FROM staff WHERE passport_number = ? AND status = 'active'
    """;

    //NEW QUERIES

    public static final String BOOKING_CHECK_AVAILABILITY = """
    SELECT COUNT(*) FROM rooms 
    WHERE room_number = ? AND status = 'occupied' 
    AND NOT (check_out_date <= ? OR check_in_date >= ?)
    """;

    public static final String BOOKING_OCCUPY_WITH_LOCK = """
    UPDATE rooms 
    SET status = 'occupied', client_passport = ?, 
        check_in_date = ?, check_out_date = ?
    WHERE room_number = ? AND status = 'free'
    AND NOT EXISTS (
        SELECT 1 FROM rooms r2 
        WHERE r2.room_number = ? 
        AND r2.status = 'occupied' 
        AND NOT (r2.check_out_date <= ? OR r2.check_in_date >= ?)
    )
    """;

//    public static final String BOOKING_HISTORY_CHECK_CONFLICT = """
//    SELECT COUNT(*) FROM booking_history
//    WHERE room_number = ?
//    AND status = 'completed'
//    AND NOT (check_out_date <= ? OR check_in_date >= ?)
//    """;

    public static final String CLIENT_DELETE_ALL = "DELETE FROM clients";
    public static final String STAFF_DELETE_ALL = "DELETE FROM staff";
    public static final String ROOM_DELETE_ALL = "DELETE FROM rooms";
    public static final String ROOM_FREE_ALL = "UPDATE rooms SET status = 'free', client_passport = NULL, check_in_date = NULL, check_out_date = NULL";
    public static final String BOOKING_HISTORY_DELETE_ALL = "DELETE FROM booking_history";

    public static final String BOOKING_HISTORY_SELECT_ALL =
            "SELECT * FROM booking_history ORDER BY booked_at DESC";

    public static final String BOOKING_HISTORY_SELECT_BY_ROOM =
            "SELECT * FROM booking_history WHERE room_number = ? ORDER BY booked_at DESC";

    public static final String BOOKING_HISTORY_INSERT =
            "INSERT INTO booking_history (room_number, client_passport, check_in_date, check_out_date) VALUES (?, ?, ?, ?)";

    public static final String BOOKING_HISTORY_DELETE_BY_PASSPORT =
            "DELETE FROM booking_history WHERE client_passport = ?";

    public static final String BOOKING_HISTORY_CHECK_CONFLICT =
            "SELECT COUNT(*) FROM booking_history WHERE room_number = ? AND NOT (check_out_date <= ? OR check_in_date >= ?)";

    public static final String CLIENT_IS_AVAILABLE_TO_CHECK_IN =
            "SELECT COUNT(*) FROM clients WHERE passport_number = ? AND status IN ('active', 'pending')";

    public static final String IS_CLIENT_EXISTS = "SELECT COUNT(*) FROM clients WHERE passport_number = ?";
    public static final String ADD_CLIENT = "INSERT INTO clients (passport_number, first_name, last_name, phone_number, email, check_in_date, check_out_date, room_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending')";
    public static final String RESEAT_CLIENT = "INSERT OR REPLACE INTO clients (passport_number, first_name, last_name, phone_number, email, check_in_date, check_out_date, room_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending')";

    public static final String DELETE_CLIENT = "DELETE FROM clients WHERE passport_number = ?";

    public static final String ROOMS_SET_FREE = "UPDATE rooms SET status = 'free'";
    public static final String ROOMS_UPDATE_STATUS = "UPDATE rooms SET status = ? WHERE room_number = ?";
    public static final String SELECT_CLIENT_BY_PASSPORT = "SELECT * FROM clients WHERE passport_number = ?";

}