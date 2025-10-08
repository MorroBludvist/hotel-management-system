package com.hotel.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Простой контроллер для главной страницы
 */
@Controller
public class HomeController {

    /**
     * Главная страница - перенаправляет на static/index.html
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Страница информации о API
     */
    @GetMapping("/api-info")
    public String apiInfo() {
        return "index";
    }
}