package com.hotel.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения - серверной части системы управления отелем.
 */
@SpringBootApplication
public class HotelServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelServerApplication.class, args);

        System.out.println("✅ Сервер отеля запущен! Доступен по: http://localhost:8080");
        System.out.println("🏠 Главная страница: http://localhost:8080");
        System.out.println("📚 Документация API: http://localhost:8080/api-info");
        System.out.println("🔐 Логин: admin, Пароль: hotel123");
        System.out.println("📊 API Endpoints:");
        System.out.println("   GET  http://localhost:8080/api/clients - список клиентов");
        System.out.println("   POST http://localhost:8080/api/clients - добавить клиента");
        System.out.println("   GET  http://localhost:8080/api/staff - список сотрудников");
        System.out.println("   POST http://localhost:8080/api/staff - добавить сотрудника");
        System.out.println("   GET  http://localhost:8080/api/rooms - список номеров");
        System.out.println("   GET  http://localhost:8080/api/rooms/free - свободные номера");
    }
}