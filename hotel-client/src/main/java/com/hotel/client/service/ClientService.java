package com.hotel.client.service;

import com.hotel.client.model.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ClientService {
    private static final Logger logger = LogManager.getLogger(ClientService.class);

    private final ApiService apiService;

    public ClientService(ApiService apiService) {
        this.apiService = apiService;
        logger.debug("ClientService инициализирован");
    }

    public List<Client> getAllClients() {
        logger.info("🔄 Получаем список всех клиентов");
        try {
            String response = apiService.executeRequest("/clients", "GET", null);
            if (response != null && response.startsWith("[")) {
                List<Client> clients = parseJsonToClients(response);
                logger.info("✅ Успешно загружено {} клиентов", clients.size());
                return clients;
            } else {
                logger.error("❌ Сервер вернул некорректный ответ: {}", response);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения клиентов: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public boolean addClient(Client client) {
        logger.info("👤 Добавляем клиента: {} {} (паспорт: {})",
                client.getFirstName(), client.getLastName(), client.getPassportNumber());

        try {
            String jsonBody = String.format(
                    "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"passportNumber\":\"%s\"," +
                            "\"phoneNumber\":\"%s\",\"email\":\"%s\",\"checkInDate\":\"%s\"," +
                            "\"checkOutDate\":\"%s\",\"roomNumber\":%d,\"roomType\":\"%s\"}",
                    apiService.escapeJson(client.getFirstName()),
                    apiService.escapeJson(client.getLastName()),
                    apiService.escapeJson(client.getPassportNumber()),
                    apiService.escapeJson(client.getPhoneNumber()),
                    apiService.escapeJson(client.getEmail()),
                    apiService.escapeJson(client.getCheckInDate()),
                    apiService.escapeJson(client.getCheckOutDate()),
                    client.getRoomNumber(),
                    apiService.escapeJson(client.getRoomType())
            );

            logger.debug("📨 JSON для отправки: {}", jsonBody);
            String response = apiService.executeRequest("/clients", "POST", jsonBody);

            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Клиент {} {} успешно добавлен",
                        client.getFirstName(), client.getLastName());
            } else {
                logger.warn("⚠️ Не удалось добавить клиента. Ответ сервера: {}", response);
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка добавления клиента: {}", e.getMessage(), e);
            return false;
        }
    }

    private List<Client> parseJsonToClients(String json) {
        List<Client> clients = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            logger.warn("❌ JSON пустой или null");
            return clients;
        }

        try {
            logger.debug("🔧 Начинаем парсинг JSON клиентов...");

            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1).trim();
            }

            if (cleanJson.isEmpty()) {
                logger.info("📭 Нет данных о клиентах");
                return clients;
            }

            String[] objects = cleanJson.split("\\},\\s*\\{");
            logger.debug("📋 Найдено объектов: {}", objects.length);

            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i].trim();

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

            logger.info("🎯 Итого распаршено клиентов: {}", clients.size());
            return clients;

        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга клиентов: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private Client parseClientObject(String jsonObject) {
        try {
            String firstName = apiService.extractStringValue(jsonObject, "firstName");
            String lastName = apiService.extractStringValue(jsonObject, "lastName");
            String passportNumber = apiService.extractStringValue(jsonObject, "passportNumber");
            String phoneNumber = apiService.extractStringValue(jsonObject, "phoneNumber");
            String email = apiService.extractStringValue(jsonObject, "email");
            String checkInDate = apiService.extractStringValue(jsonObject, "checkInDate");
            String checkOutDate = apiService.extractStringValue(jsonObject, "checkOutDate");
            Integer roomNumber = apiService.extractIntegerValue(jsonObject, "roomNumber");
            String roomType = apiService.extractStringValue(jsonObject, "roomType");

            logger.debug("📊 Распаршены поля: {} {}, паспорт: {}, номер: {}",
                    firstName, lastName, passportNumber, roomNumber);

            if (firstName == null || lastName == null || passportNumber == null) {
                logger.warn("⚠️ Отсутствуют обязательные поля у клиента");
                return null;
            }

            Client client = new Client(
                    firstName, lastName, passportNumber, phoneNumber, email,
                    checkInDate, checkOutDate,
                    roomNumber != null ? roomNumber : 0,
                    roomType != null ? roomType : "Не указан"
            );

            logger.debug("✅ Создан клиент: {} {}", firstName, lastName);
            return client;

        } catch (Exception e) {
            logger.error("❌ Ошибка парсинга объекта клиента: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Очистка всех клиентских данных
     */
    public boolean clearClientData() {
        logger.info("🗑️ Очистка всех данных клиентов");
        try {
            String response = apiService.executeRequest("/clients/clear", "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Данные клиентов успешно очищены, номера освобождены");
            } else {
                logger.warn("⚠️ Не удалось очистить данные клиентов. Ответ: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка очистки клиентов: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Заселение клиента с полной валидацией
     */
    public boolean checkInClient(Client client) {
        logger.info("👤 Заселение клиента: {} {} (паспорт: {}) в номер {}",
                client.getFirstName(), client.getLastName(),
                client.getPassportNumber(), client.getRoomNumber());

        try {
            String jsonBody = String.format(
                    "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"passportNumber\":\"%s\"," +
                            "\"phoneNumber\":\"%s\",\"email\":\"%s\",\"checkInDate\":\"%s\"," +
                            "\"checkOutDate\":\"%s\",\"roomNumber\":%d,\"roomType\":\"%s\"}",
                    apiService.escapeJson(client.getFirstName()),
                    apiService.escapeJson(client.getLastName()),
                    apiService.escapeJson(client.getPassportNumber()),
                    apiService.escapeJson(client.getPhoneNumber()),
                    apiService.escapeJson(client.getEmail()),
                    apiService.escapeJson(client.getCheckInDate()),
                    apiService.escapeJson(client.getCheckOutDate()),
                    client.getRoomNumber(),
                    apiService.escapeJson(client.getRoomType())
            );

            logger.debug("📨 Отправка запроса на заселение: {}", jsonBody);
            String response = apiService.executeRequest("/bookings/check-in", "POST", jsonBody);

            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Клиент {} {} успешно заселен в номер {}",
                        client.getFirstName(), client.getLastName(), client.getRoomNumber());
            } else {
                logger.warn("⚠️ Не удалось заселить клиента. Ответ сервера: {}", response);
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка заселения клиента: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Выселение клиента
     */
    public boolean checkOutClient(String passportNumber) {
        logger.info("🚪 Выселение клиента с паспортом: {}", passportNumber);

        try {
            String jsonBody = String.format("{\"passportNumber\":\"%s\"}",
                    apiService.escapeJson(passportNumber));

            String response = apiService.executeRequest("/bookings/check-out", "POST", jsonBody);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Клиент с паспортом {} успешно выселен", passportNumber);
            } else {
                logger.warn("⚠️ Не удалось выселить клиента. Ответ: {}", response);
            }

            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка выселения клиента: {}", e.getMessage(), e);
            return false;
        }
    }
}