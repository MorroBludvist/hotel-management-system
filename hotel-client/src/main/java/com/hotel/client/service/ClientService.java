package com.hotel.client.service;

import com.hotel.client.model.Client;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientService {
    private final ApiService apiService;
    private static final Logger logger = LogManager.getLogger(ClientService.class);

    public ClientService(ApiService apiService) {
        this.apiService = apiService;
    }

    public List<Client> getAllClients() {
        try {
            String response = apiService.executeRequest("/clients", "GET", null);
            if (response != null && response.startsWith("[")) {
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

    public boolean addClient(Client client) {
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

            String response = apiService.executeRequest("/clients", "POST", jsonBody);
            return response != null && response.contains("\"success\":true");

        } catch (Exception e) {
            System.err.println("❌ Ошибка добавления клиента: " + e.getMessage());
            return false;
        }
    }

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

    private Client parseClientObject(String jsonObject) {
        try {
            // Извлекаем все поля
            String firstName = apiService.extractStringValue(jsonObject, "firstName");
            String lastName = apiService.extractStringValue(jsonObject, "lastName");
            String passportNumber = apiService.extractStringValue(jsonObject, "passportNumber");
            String phoneNumber = apiService.extractStringValue(jsonObject, "phoneNumber");
            String email = apiService.extractStringValue(jsonObject, "email");
            String checkInDate = apiService.extractStringValue(jsonObject, "checkInDate");
            String checkOutDate = apiService.extractStringValue(jsonObject, "checkOutDate");
            Integer roomNumber = apiService.extractIntegerValue(jsonObject, "roomNumber");
            String roomType = apiService.extractStringValue(jsonObject, "roomType");

            // Проверяем обязательные поля
            if (firstName == null || lastName == null || passportNumber == null) {
                //logger.debug("Отсутствуют обязательные поля");
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
}