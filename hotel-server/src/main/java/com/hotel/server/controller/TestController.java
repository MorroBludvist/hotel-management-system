//package com.hotel.server.controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class TestController {
//
//    @GetMapping("/test")
//    public String test() {
//        return "✅ Сервер работает! Test endpoint доступен";
//    }
//
//    @GetMapping("/")
//    public String home() {
//        return """
//            <!DOCTYPE html>
//            <html>
//            <head>
//                <title>Hotel System</title>
//                <style>
//                    body { font-family: Arial; padding: 40px; background: #f0f0f0; }
//                    .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; }
//                </style>
//            </head>
//            <body>
//                <div class="container">
//                    <h1>🏨 Hotel Management System</h1>
//                    <p>✅ Сервер запущен и работает!</p>
//                    <p><strong>Тестовые endpoints:</strong></p>
//                    <ul>
//                        <li><a href="/test">/test</a> - простой тест</li>
//                        <li><a href="/api/clients">/api/clients</a> - клиенты</li>
//                        <li><a href="/api/staff">/api/staff</a> - сотрудники</li>
//                    </ul>
//                </div>
//            </body>
//            </html>
//            """;
//    }
//}