package com.hotel.server.controller;

import com.hotel.server.model.Client;
import com.hotel.server.service.ClientService;
import com.hotel.server.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * REST контроллер для управления клиентами отеля.
 */
@RestController //Чтобы все возвращали json вместо html
@RequestMapping("/api/clients") //Адрес страницы
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Получение клиентов
     */
    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        try {
            List<Client> clients = clientService.getAllClients();
            System.out.println("Отправлено клиентов: " + clients.size());
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            System.err.println("Ошибка получения клиентов: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Добавление клиента
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addClient(@RequestBody Client client) {
        try {
            System.out.println("Получен клиент: " + client.getFirstName() + " " + client.getLastName());

            boolean success = clientService.addClient(client);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Клиент успешно добавлен" : "Ошибка добавления клиента");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Ошибка добавления клиента: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}