package com.hotel.client.config;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Управление состоянием приложения (сохранение/загрузка даты)
 */
public class AppStateManager {
    private static final Logger logger = LogManager.getLogger(AppStateManager.class);
    private static final String STATE_FILE = "hotel_app_state.properties";
    private static final String DATE_KEY = "current_date";

    private static AppStateManager instance;
    private Properties properties;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static AppStateManager getInstance() {
        if (instance == null) {
            instance = new AppStateManager();
        }
        return instance;
    }

    private AppStateManager() {
        properties = new Properties();
        loadState();
    }

    /**
     * Загружает состояние из файла
     */
    private void loadState() {
        try (FileInputStream fis = new FileInputStream(STATE_FILE)) {
            properties.load(fis);
            logger.info("✅ Состояние приложения загружено из файла");
        } catch (FileNotFoundException e) {
            logger.info("Файл состояния не найден, используется текущая дата");
            // Если файла нет, используем текущую дату
            properties.setProperty(DATE_KEY, dateFormat.format(new Date()));
            saveState();
        } catch (IOException e) {
            logger.error("❌ Ошибка загрузки состояния: {}", e.getMessage());
            properties.setProperty(DATE_KEY, dateFormat.format(new Date()));
        }
    }

    /**
     * Сохраняет состояние в файл
     */
    private void saveState() {
        try (FileOutputStream fos = new FileOutputStream(STATE_FILE)) {
            properties.store(fos, "Hotel Management System State");
            logger.info("✅ Состояние приложения сохранено");
        } catch (IOException e) {
            logger.error("❌ Ошибка сохранения состояния: {}", e.getMessage());
        }
    }

    /**
     * Получает текущую дату из состояния
     */
    public String getCurrentDate() {
        return properties.getProperty(DATE_KEY, dateFormat.format(new Date()));
    }

    /**
     * Устанавливает новую дату и сохраняет состояние
     */
    public void setCurrentDate(String date) {
        properties.setProperty(DATE_KEY, date);
        saveState();
        logger.info("📅 Дата установлена: {}", date);
    }

    /**
     * Сбрасывает состояние к текущей дате
     */
    public void resetToToday() {
        String today = dateFormat.format(new Date());
        properties.setProperty(DATE_KEY, today);
        saveState();
        logger.info("🔄 Дата сброшена на сегодня: {}", today);
    }
}