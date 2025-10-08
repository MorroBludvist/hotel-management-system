package com.hotel.client.service;

import com.hotel.client.model.Client;
import java.util.ArrayList;
import java.util.List;

public class ClientService {
    private final ApiService apiService;

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
        // TODO: Перенести логику парсинга из DatabaseManager
        return clients;
    }
}