package com.hotel.server.controller;

import com.hotel.server.model.Client;
import com.hotel.server.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addClient(@RequestBody Client client) {
        boolean success = clientService.addClient(client);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @DeleteMapping("/{passportNumber}")
    public ResponseEntity<Map<String, Object>> deleteClient(@PathVariable String passportNumber) {
        boolean success = clientService.deleteClient(passportNumber);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearAllClients() {
        boolean success = clientService.clearAll();
        return ResponseEntity.ok(Map.of("success", success));
    }


}