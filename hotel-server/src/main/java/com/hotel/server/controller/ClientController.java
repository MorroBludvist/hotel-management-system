package com.hotel.server.controller;

import com.hotel.server.model.Client;
import com.hotel.server.service.ClientService;
import com.hotel.server.service.RoomService;
import com.hotel.server.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * REST контроллер для управления клиентами отеля.
 */
@RestController //Чтобы все возвращали json вместо html
@RequestMapping("/api/clients") //Адрес страницы
public class ClientController {
    private final ClientService clientService;
    private static final Logger logger = LogManager.getLogger(ClientController.class);

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

    /**
     * Очистка всех клиентов и освобождение номеров
     */
    //TODO: убрать /clear и сделать соответствие остальным методам
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllClients() {
        try {
            logger.info("🔄 Запрос на очистку всех клиентов");
            boolean success = clientService.clearAll();

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ?
                    "Все клиенты удалены и номера освобождены" :
                    "Ошибка очистки клиентов");

            logger.info("✅ Ответ очистки клиентов: {}", success);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Ошибка в контроллере очистки клиентов: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}