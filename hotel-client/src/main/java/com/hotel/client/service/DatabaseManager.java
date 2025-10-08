package com.hotel.client.service;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import com.hotel.client.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseManager {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "hotel123";
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);

    private static DatabaseManager instance;

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Выполняет HTTP запрос к серверу
     */
    private String executeRequest(String endpoint, String method, String jsonBody) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();

            // Настройки соединения
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            // Basic аутентификация
            String auth = USERNAME + ":" + PASSWORD;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            // Для POST запросов
            if (jsonBody != null && (method.equals("POST") || method.equals("PUT"))) {
                connection.setDoOutput(true);
                writer = new BufferedWriter(new OutputStreamWriter(
                        connection.getOutputStream(), StandardCharsets.UTF_8));
                writer.write(jsonBody);
                writer.flush();
            }

            // Получаем ответ
            int responseCode = connection.getResponseCode();
            System.out.println("📡 HTTP " + method + " " + endpoint + " -> " + responseCode);

            // Читаем ответ
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    reader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
                }
            }

            // Собираем ответ
            if (reader != null) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }

        } catch (IOException e) {
            System.err.println("❌ Ошибка сети: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
        } finally {
            // Закрываем ресурсы
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                System.err.println("❌ Ошибка закрытия ресурсов: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * РАБОЧИЙ парсер JSON для клиентов
     */
    private List<Client> parseJsonToClients(String json) {
        List<Client> clients = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            System.out.println("❌ JSON пустой или null");
            return clients;
        }

        try {
            System.out.println("🔧 Начинаем парсинг JSON клиентов...");

            // Убираем внешние скобки
            System.out.println(json);
            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                System.out.println("📭 Нет данных о клиентах");
                return clients;
            }

            // Разделяем на объекты
            String[] objects = cleanJson.split("\\},\\s*\\{");
            System.out.println("📋 Найдено объектов: " + objects.length);

            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i].trim();

                // Восстанавливаем фигурные скобки
                if (i == 0 && !obj.startsWith("{")) obj = "{" + obj;
                if (i == objects.length - 1 && !obj.endsWith("}")) obj = obj + "}";
                if (i > 0 && i < objects.length - 1) {
                    if (!obj.startsWith("{")) obj = "{" + obj;
                    if (!obj.endsWith("}")) obj = obj + "}";
                }

                Client client = parseClientObject(obj);
                if (client != null) {
                    clients.add(client);
                }
            }

            System.out.println("🎯 Итого распаршено клиентов: " + clients.size());

        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга клиентов: " + e.getMessage());
        }
        return clients;
    }

    /**
     * Парсим один объект клиента
     */
    private Client parseClientObject(String jsonObject) {
        try {
            // Извлекаем все поля
            String firstName = extractStringValue(jsonObject, "firstName");
            String lastName = extractStringValue(jsonObject, "lastName");
            String passportNumber = extractStringValue(jsonObject, "passportNumber");
            String phoneNumber = extractStringValue(jsonObject, "phoneNumber");
            String email = extractStringValue(jsonObject, "email");
            String checkInDate = extractStringValue(jsonObject, "checkInDate");
            String checkOutDate = extractStringValue(jsonObject, "checkOutDate");
            Integer roomNumber = extractIntegerValue(jsonObject, "roomNumber");
            String roomType = extractStringValue(jsonObject, "roomType");

            // Проверяем обязательные поля
            if (firstName == null || lastName == null || passportNumber == null) {
                logger.debug("Отсутствуют обязательные поля");
                //System.out.println("⚠️ Отсутствуют обязательные поля");
                return null;
            }

            // Создаем клиента
            Client client = new Client(
                    firstName, lastName, passportNumber, phoneNumber, email,
                    checkInDate, checkOutDate,
                    roomNumber != null ? roomNumber : 0,
                    roomType != null ? roomType : "Не указан"
            );

            System.out.println("✅ Создан клиент: " + firstName + " " + lastName);
            return client;

        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга объекта клиента: " + e.getMessage());
            return null;
        }
    }

    /**
     * РАБОЧИЙ парсер JSON для сотрудников
     */
    private List<Staff> parseJsonToStaff(String json) {
        List<Staff> staffList = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            System.out.println("❌ JSON пустой или null");
            return staffList;
        }

        try {
            System.out.println("🔧 Начинаем парсинг JSON сотрудников...");

            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                System.out.println("📭 Нет данных о сотрудниках");
                return staffList;
            }

            String[] objects = cleanJson.split("\\},\\s*\\{");
            System.out.println("📋 Найдено объектов: " + objects.length);

            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i].trim();

                if (i == 0 && !obj.startsWith("{")) obj = "{" + obj;
                if (i == objects.length - 1 && !obj.endsWith("}")) obj = obj + "}";
                if (i > 0 && i < objects.length - 1) {
                    if (!obj.startsWith("{")) obj = "{" + obj;
                    if (!obj.endsWith("}")) obj = obj + "}";
                }

                Staff staff = parseStaffObject(obj);
                if (staff != null) {
                    staffList.add(staff);
                }
            }

            System.out.println("🎯 Итого распаршено сотрудников: " + staffList.size());

        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга сотрудников: " + e.getMessage());
        }
        return staffList;
    }

    /**
     * Парсим один объект сотрудника
     */
    private Staff parseStaffObject(String jsonObject) {
        try {
            String passportNumber = extractStringValue(jsonObject, "passportNumber");
            String firstName = extractStringValue(jsonObject, "firstName");
            String lastName = extractStringValue(jsonObject, "lastName");
            String position = extractStringValue(jsonObject, "position");
            String phoneNumber = extractStringValue(jsonObject, "phoneNumber");
            String email = extractStringValue(jsonObject, "email");
            String hireDate = extractStringValue(jsonObject, "hireDate");
            Double salary = extractDoubleValue(jsonObject, "salary");
            String department = extractStringValue(jsonObject, "department");

            if (firstName == null || lastName == null || passportNumber == null) {
                System.out.println("⚠️ Отсутствуют обязательные поля");
                return null;
            }

            Staff staff = new Staff(
                    firstName, lastName, passportNumber,  position, phoneNumber, email,
                    hireDate, salary != null ? salary : 0.0,
                    department != null ? department : "Не указан"
            );

            System.out.println("✅ Создан сотрудник: " + firstName + " " + lastName + ", паспорт: " + passportNumber);
            return staff;

        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга объекта сотрудника: " + e.getMessage());
            return null;
        }
    }

    /**
     * Извлекает строковое значение из JSON
     */
    private String extractStringValue(String json, String key) {
        try {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start == -1) return null;

            start += search.length();
            int end = json.indexOf("\"", start);
            if (end == -1) return null;

            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Извлекает целочисленное значение
     */
    private Integer extractIntegerValue(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start == -1) return null;

            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) return null;

            String value = json.substring(start, end).trim();
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Извлекает дробное значение
     */
    private Double extractDoubleValue(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start == -1) return null;

            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) return null;

            String value = json.substring(start, end).trim();
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Получает клиентов с сервера
     */
    public List<Client> getAllClients() {
        try {
            String response = executeRequest("/clients", "GET", null);
            if (response != null && response.startsWith("[")) {
                System.out.println("✅ Получен JSON клиентов");
                return parseJsonToClients(response);
            } else {
                System.out.println("❌ Сервер вернул некорректный ответ: " + response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения клиентов: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Получает сотрудников с сервера
     */
    public List<Staff> getAllStaff() {
        try {
            String response = executeRequest("/staff", "GET", null);
            if (response != null && response.startsWith("[")) {
                System.out.println("✅ Получен JSON сотрудников");
                return parseJsonToStaff(response);
            } else {
                System.out.println("❌ Сервер вернул некорректный ответ: " + response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения сотрудников: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Добавляет клиента
     */
    public boolean addClient(Client client) {
        try {
            String jsonBody = String.format(
                    "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"passportNumber\":\"%s\"," +
                            "\"phoneNumber\":\"%s\",\"email\":\"%s\",\"checkInDate\":\"%s\"," +
                            "\"checkOutDate\":\"%s\",\"roomNumber\":%d,\"roomType\":\"%s\"}",
                    escapeJson(client.getFirstName()),
                    escapeJson(client.getLastName()),
                    escapeJson(client.getPassportNumber()),
                    escapeJson(client.getPhoneNumber()),
                    escapeJson(client.getEmail()),
                    escapeJson(client.getCheckInDate()),
                    escapeJson(client.getCheckOutDate()),
                    client.getRoomNumber(),
                    escapeJson(client.getRoomType())
            );

            String response = executeRequest("/clients", "POST", jsonBody);
            boolean success = response != null && response.contains("\"success\":true");
            System.out.println("✅ Результат добавления клиента: " + success);
            return success;

        } catch (Exception e) {
            System.err.println("❌ Ошибка добавления клиента: " + e.getMessage());
            return false;
        }
    }

    /**
     * Добавляет сотрудника с паспортом как первичным ключом
     */
    public boolean addStaff(Staff staff) {
        try {
            String jsonBody = String.format(
                    "{\"passportNumber\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"position\":\"%s\"," +
                            "\"phoneNumber\":\"%s\",\"email\":\"%s\",\"hireDate\":\"%s\"," +
                            "\"salary\":%.2f,\"department\":\"%s\"}",
                    escapeJson(staff.getPassportNumber()),  // ← паспорт как первичный ключ
                    escapeJson(staff.getFirstName()),
                    escapeJson(staff.getLastName()),
                    escapeJson(staff.getPosition()),
                    escapeJson(staff.getPhoneNumber()),
                    escapeJson(staff.getEmail()),
                    escapeJson(staff.getHireDate()),
                    staff.getSalary(),
                    escapeJson(staff.getDepartment())
            );

            logger.debug("Отправляем сотрудника BRUH: {}", jsonBody);
            System.out.println("📨 Отправляем сотрудника: " + jsonBody);
            String response = executeRequest("/staff", "POST", jsonBody);
            boolean success = response != null && response.contains("\"success\":true");
            System.out.println("✅ Результат добавления сотрудника: " + success);
            return success;

        } catch (Exception e) {
            System.err.println("❌ Ошибка добавления сотрудника: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет доступность сервера
     */
    public boolean isServerAvailable() {
        try {
            String response = executeRequest("/clients", "GET", null);
            return response != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Экранирует JSON
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public List<Room> getAllRooms() {
        try {
            String response = executeRequest("/rooms", "GET", null);
            if (response != null && response.startsWith("[")) {
                return parseJsonToRooms(response);
            } else {
                System.out.println("❌ Сервер вернул некорректный ответ для номеров");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения номеров: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Получает свободные номера
     */
    public List<Room> getFreeRooms() {
        try {
            String response = executeRequest("/rooms/free", "GET", null);
            if (response != null && response.startsWith("[")) {
                return parseJsonToRooms(response);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения свободных номеров: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Проверяет доступность номера
     */
    public boolean isRoomAvailable(int roomNumber, String checkInDate, String checkOutDate) {
        try {
            String jsonBody = String.format(
                    "{\"roomNumber\":%d,\"checkInDate\":\"%s\",\"checkOutDate\":\"%s\"}",
                    roomNumber, checkInDate, checkOutDate
            );

            String response = executeRequest("/rooms/check-availability", "POST", jsonBody);
            return response != null && response.contains("\"available\":true");
        } catch (Exception e) {
            System.err.println("❌ Ошибка проверки доступности номера: " + e.getMessage());
            return false;
        }
    }

    /**
     * Обновляет дату и проверяет занятость
     */
    public boolean advanceDate(String currentDate) {
        try {
            String jsonBody = String.format("{\"currentDate\":\"%s\"}", currentDate);
            String response = executeRequest("/rooms/advance-date", "POST", jsonBody);
            return response != null && response.contains("\"success\":true");
        } catch (Exception e) {
            System.err.println("❌ Ошибка обновления даты: " + e.getMessage());
            return false;
        }
    }

    /**
     * Парсер JSON для номеров
     */
    private List<Room> parseJsonToRooms(String json) {
        List<Room> rooms = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            return rooms;
        }

        try {
            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                return rooms;
            }

            String[] objects = cleanJson.split("\\},\\s*\\{");

            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i].trim();

                if (i == 0 && !obj.startsWith("{")) obj = "{" + obj;
                if (i == objects.length - 1 && !obj.endsWith("}")) obj = obj + "}";
                if (i > 0 && i < objects.length - 1) {
                    if (!obj.startsWith("{")) obj = "{" + obj;
                    if (!obj.endsWith("}")) obj = obj + "}";
                }

                Room room = parseRoomObject(obj);
                if (room != null) {
                    rooms.add(room);
                }
            }

            System.out.println("🎯 Распаршено номеров: " + rooms.size());

        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга номеров: " + e.getMessage());
        }
        return rooms;
    }

    private Room parseRoomObject(String jsonObject) {
        try {
            Integer roomNumber = extractIntegerValue(jsonObject, "roomNumber");
            String roomType = extractStringValue(jsonObject, "roomType");
            String status = extractStringValue(jsonObject, "status");
            String clientPassport = extractStringValue(jsonObject, "clientPassport");
            String checkInDate = extractStringValue(jsonObject, "checkInDate");
            String checkOutDate = extractStringValue(jsonObject, "checkOutDate");

            //TODO: вернуть старый конструктор
            if (roomNumber != null && roomType != null) {
                Room room = new Room(roomNumber, roomType, status != null ? status : "free",
                        clientPassport, checkInDate, checkOutDate);
                // Дополнительные поля если нужны
                return room;
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка парсинга объекта номера: " + e.getMessage());
        }
        return null;
    }
}