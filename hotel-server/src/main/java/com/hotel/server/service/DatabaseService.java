//package com.hotel.server.service;
//
//import com.hotel.server.model.Client;
//import com.hotel.server.model.Room;
//import com.hotel.server.model.Staff;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class DatabaseService {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public DatabaseService(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    // Методы для номеров
//    public List<Room> getAllRooms() {
//        String sql = "SELECT * FROM rooms ORDER BY room_number";
//        return jdbcTemplate.query(sql, roomRowMapper());
//    }
//
//    public List<Room> getFreeRooms() {
//        String sql = "SELECT * FROM rooms WHERE status = 'free' ORDER BY room_number";
//        return jdbcTemplate.query(sql, roomRowMapper());
//    }
//
//    public List<Room> getOccupiedRooms() {
//        String sql = "SELECT * FROM rooms WHERE status = 'occupied' ORDER BY room_number";
//        return jdbcTemplate.query(sql, roomRowMapper());
//    }
//
//    public boolean isRoomAvailable(Integer roomNumber, String checkInDate, String checkOutDate) {
//        String sql = """
//            SELECT COUNT(*) FROM rooms
//            WHERE room_number = ? AND status = 'occupied'
//            AND NOT (check_out_date <= ? OR check_in_date >= ?)
//        """;
//
//        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
//                roomNumber, checkInDate, checkOutDate);
//        return count == 0;
//    }
//
//    public boolean occupyRoom(Integer roomNumber, String clientPassport,
//                              String checkInDate, String checkOutDate) {
//        String sql = """
//            UPDATE rooms
//            SET status = 'occupied', client_passport = ?,
//                check_in_date = ?, check_out_date = ?
//            WHERE room_number = ?
//        """;
//
//        try {
//            int result = jdbcTemplate.update(sql,
//                    clientPassport, checkInDate, checkOutDate, roomNumber);
//            return result > 0;
//        } catch (Exception e) {
//            System.err.println("❌ Ошибка занятия номера: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public boolean freeRoom(Integer roomNumber) {
//        String sql = """
//            UPDATE rooms
//            SET status = 'free', client_passport = NULL,
//                check_in_date = NULL, check_out_date = NULL
//            WHERE room_number = ?
//        """;
//
//        try {
//            int result = jdbcTemplate.update(sql, roomNumber);
//            return result > 0;
//        } catch (Exception e) {
//            System.err.println("❌ Ошибка освобождения номера: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public void checkRoomOccupancy(String currentDate) {
//        // Освобождаем номера, у которых дата выезда наступила
//        String freeRoomsSql = """
//            UPDATE rooms SET status = 'free', client_passport = NULL,
//            check_in_date = NULL, check_out_date = NULL
//            WHERE status = 'occupied' AND check_out_date <= ?
//        """;
//        jdbcTemplate.update(freeRoomsSql, currentDate);
//
//        // Занимаем номера, у которых дата заезда наступила
//        String occupyRoomsSql = """
//            UPDATE rooms SET status = 'occupied'
//            WHERE status = 'free' AND check_in_date <= ? AND check_out_date > ?
//        """;
//        jdbcTemplate.update(occupyRoomsSql, currentDate, currentDate);
//    }
//
//    private RowMapper<Room> roomRowMapper() {
//        return (rs, rowNum) -> {
//            Room room = new Room();
//            room.setRoomNumber(rs.getInt("room_number"));
//            room.setRoomType(rs.getString("room_type"));
//            room.setStatus(rs.getString("status"));
//            room.setClientPassport(rs.getString("client_passport"));
//            room.setCheckInDate(rs.getString("check_in_date"));
//            room.setCheckOutDate(rs.getString("check_out_date"));
//            return room;
//        };
//    }
//
//    public List<Client> getAllClients() {
//        String sql = "SELECT * FROM clients WHERE status = 'active'";
//        return jdbcTemplate.query(sql, clientRowMapper());
//    }
//
//    public boolean addClient(Client client) {
//        String sql = """
//            INSERT INTO clients (first_name, last_name, passport_number, phone_number,
//                                email, check_in_date, check_out_date, room_number, room_type)
//            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
//        """;
//
//        try {
//            int result = jdbcTemplate.update(sql,
//                    client.getFirstName(),
//                    client.getLastName(),
//                    client.getPassportNumber(),
//                    client.getPhoneNumber(),
//                    client.getEmail(),
//                    client.getCheckInDate(),
//                    client.getCheckOutDate(),
//                    client.getRoomNumber(),
//                    client.getRoomType()
//            );
//            return result > 0;
//        } catch (Exception e) {
//            System.err.println("Ошибка добавления клиента: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public List<Staff> getAllStaff() {
//        String sql = "SELECT * FROM staff WHERE status = 'active'";
//        return jdbcTemplate.query(sql, staffRowMapper());
//    }
//
//    public boolean addStaff(Staff staff) {
//        String sql = """
//            INSERT INTO staff (passport_number, first_name, last_name, position, phone_number,
//                              email, hire_date, salary, department)
//            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
//        """;
//
//        try {
//            int result = jdbcTemplate.update(sql,
//                    staff.getPassportNumber(),
//                    staff.getFirstName(),
//                    staff.getLastName(),
//                    staff.getPosition(),
//                    staff.getPhoneNumber(),
//                    staff.getEmail(),
//                    staff.getHireDate(),
//                    staff.getSalary(),
//                    staff.getDepartment()
//            );
//            return result > 0;
//        } catch (Exception e) {
//            System.err.println("Ошибка добавления сотрудника: " + e.getMessage());
//            return false;
//        }
//    }
//
//    private RowMapper<Client> clientRowMapper() {
//        return (rs, rowNum) -> {
//            Client client = new Client();
//            client.setFirstName(rs.getString("first_name"));
//            client.setLastName(rs.getString("last_name"));
//            client.setPassportNumber(rs.getString("passport_number"));
//            client.setPhoneNumber(rs.getString("phone_number"));
//            client.setEmail(rs.getString("email"));
//            client.setCheckInDate(rs.getString("check_in_date"));
//            client.setCheckOutDate(rs.getString("check_out_date"));
//            client.setRoomNumber(rs.getInt("room_number"));
//            client.setRoomType(rs.getString("room_type"));
//            return client;
//        };
//    }
//
//    private RowMapper<Staff> staffRowMapper() {
//        return (rs, rowNum) -> {
//            Staff staff = new Staff();
//            staff.setPassportNumber(rs.getString("passport_number"));
//            staff.setFirstName(rs.getString("first_name"));
//            staff.setLastName(rs.getString("last_name"));
//            staff.setPosition(rs.getString("position"));
//            staff.setPhoneNumber(rs.getString("phone_number"));
//            staff.setEmail(rs.getString("email"));
//            staff.setHireDate(rs.getString("hire_date"));
//            staff.setSalary(rs.getDouble("salary"));
//            staff.setDepartment(rs.getString("department"));
//            return staff;
//        };
//    }
//}