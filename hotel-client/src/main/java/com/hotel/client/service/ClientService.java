package com.hotel.client.service;

import com.hotel.client.model.Client;
import com.hotel.client.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ClientService {
    private static final Logger logger = LogManager.getLogger(ClientService.class);

    private final ApiService apiService;

    public ClientService(ApiService apiService) {
        this.apiService = apiService;
        logger.debug("ClientService инициализирован с Jackson");
    }

    public List<Client> getAllClients() {
        logger.info("🔄 Получаем список всех клиентов");
        try {
            String response = apiService.executeRequest("/clients", "GET", null);

            if (response != null && response.startsWith("[")) {
                List<Client> clients = JsonUtils.fromJsonList(response, Client.class);
                logger.info("✅ Успешно загружено {} клиентов", clients.size());
                return clients;
            } else {
                logger.error("❌ Сервер вернул некорректный ответ: {}", response);
                return List.of();
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка получения клиентов: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public boolean addClient(Client client) {
        logger.info("👤 Добавляем клиента: {} {} (паспорт: {})",
                client.getFirstName(), client.getLastName(), client.getPassportNumber());

        try {
            String jsonBody = JsonUtils.toJson(client);
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

    /**
     * Удаление клиента по паспорту
     */
    public boolean deleteClient(String passportNumber) {
        logger.info("🗑️ Удаление клиента с паспортом: {}", passportNumber);
        try {
            String response = apiService.executeRequest("/clients/" + passportNumber, "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Клиент с паспортом {} удален", passportNumber);
            } else {
                logger.warn("⚠️ Не удалось удалить клиента. Ответ: {}", response);
            }
            return success;

        } catch (Exception e) {
            logger.error("❌ Ошибка удаления клиента: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Очистка всех клиентских данных
     */
    public boolean clearClientData() {
        logger.info("🗑️ Очистка всех данных клиентов");
        try {
            String response = apiService.executeRequest("/clients", "DELETE", null);
            boolean success = response != null && response.contains("\"success\":true");

            if (success) {
                logger.info("✅ Данные клиентов успешно очищены");
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
     * Выселение клиента
     */
    public boolean checkOutClient(String passportNumber) {
        logger.info("🚪 Выселение клиента с паспортом: {}", passportNumber);

        try {
            String jsonBody = String.format("{\"passportNumber\":\"%s\"}", passportNumber);
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

    /**
     * Получить клиента по паспорту
     */
    public Client getClientByPassport(String passportNumber) {
        logger.info("🔍 Поиск клиента по паспорту: {}", passportNumber);
        try {
            String response = apiService.executeRequest("/clients/" + passportNumber, "GET", null);

            if (response != null && response.startsWith("{")) {
                Client client = JsonUtils.fromJson(response, Client.class);
                if (client != null) {
                    logger.info("✅ Найден клиент: {} {}", client.getFirstName(), client.getLastName());
                }
                return client;
            } else {
                logger.warn("⚠️ Клиент с паспортом {} не найден", passportNumber);
                return null;
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка поиска клиента: {}", e.getMessage(), e);
            return null;
        }
    }

}