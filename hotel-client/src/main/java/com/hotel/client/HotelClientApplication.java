package com.hotel.client;

import com.hotel.client.view.HotelAdminDashboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import javax.swing.*;

/**
 * Главный класс клиентского приложения
 */
public class HotelClientApplication {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //TODO: заменить логированием с TRACE
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            HotelAdminDashboard dashboard = new HotelAdminDashboard();
            dashboard.setVisible(true);
        });

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.reconfigure(); // Перезагружает конфигурацию

        // 2. Получите логгер ПОСЛЕ сброса кэша
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(HotelClientApplication.class);

        // 3. Тестовые сообщения
        logger.trace("TRACE сообщение - должно отображаться только в DEBUG");
        logger.debug("DEBUG сообщение - должно отображаться в DEV/DEBUG");
        logger.info("INFO сообщение - должно отображаться в DEV/INFO");
        logger.warn("WARN сообщение - должно отображаться всегда");
        logger.error("ERROR сообщение - должно отображаться всегда");

        // 4. Покажите текущую конфигурацию
        System.out.println("\n=== Log4j2 Configuration ===");
        System.out.println("Config file property: " + System.getProperty("log4j.configurationFile"));
        System.out.println("Logger level: " + logger.getLevel());
        System.out.println("Logger name: " + logger.getName());
    }
}