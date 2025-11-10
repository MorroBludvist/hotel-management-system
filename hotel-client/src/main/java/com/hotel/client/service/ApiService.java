package com.hotel.client.service;

import com.hotel.client.config.AppConfig;
import com.hotel.client.exception.HotelException;
import com.hotel.client.exception.ServerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Сервис для выполнения HTTP запросов к API
 * После перехода на Jackson удалены ручные методы парсинга JSON
 */
public class ApiService {
    private static ApiService instance;
    private static final Logger logger = LogManager.getLogger(ApiService.class);

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    /**
     * Выполняет HTTP запрос к серверу
     *
     * @param endpoint endpoint API
     * @param method HTTP метод (GET, POST, PUT, DELETE)
     * @param jsonBody тело запроса в формате JSON (может быть null для GET/DELETE)
     * @return ответ сервера в виде строки
     * @throws ServerException при ошибках сети, таймаутах и HTTP ошибках
     * @throws HotelException при других ошибках приложения
     */
    public String executeRequest(String endpoint, String method, String jsonBody)
            throws ServerException, HotelException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            URL url = new URL(AppConfig.API_BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();

            // Настройка соединения
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            // Базовая аутентификация
            String auth = AppConfig.API_USERNAME + ":" + AppConfig.API_PASSWORD;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Таймауты
            connection.setConnectTimeout(AppConfig.API_TIMEOUT);
            connection.setReadTimeout(AppConfig.API_TIMEOUT);

            // Отправка тела запроса для POST/PUT
            if (jsonBody != null && (method.equals("POST") || method.equals("PUT"))) {
                connection.setDoOutput(true);
                writer = new BufferedWriter(new OutputStreamWriter(
                        connection.getOutputStream(), StandardCharsets.UTF_8));
                writer.write(jsonBody);
                writer.flush();
            }

            // Получение ответа
            int responseCode = connection.getResponseCode();
            logger.debug("HTTP {} {} -> {}", method, endpoint, responseCode);

            // Обработка HTTP ошибок
            if (responseCode >= 400) {
                String errorMessage = readErrorResponse(connection);
                logger.error("HTTP error {} for {} {}: {}", responseCode, method, endpoint, errorMessage);
                throw new ServerException(responseCode, "HTTP " + responseCode + ": " + errorMessage);
            }

            // Чтение успешного ответа
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String responseBody = response.toString();
                logger.debug("Response for {} {}: {}", method, endpoint, responseBody);
                return responseBody;

            } else {
                throw new HotelException("Unexpected response code: " + responseCode);
            }

        } catch (SocketTimeoutException e) {
            logger.error("Request timeout for {} {}: {}", method, endpoint, e.getMessage());
            throw new ServerException(408, "Request timeout - server not responding", e);
        } catch (UnknownHostException e) {
            logger.error("Server not found for {} {}: {}", method, endpoint, e.getMessage());
            throw new ServerException(503, "Server unavailable - unknown host", e);
        } catch (IOException e) {
            logger.error("Network error for {} {}: {}", method, endpoint, e.getMessage());
            throw new ServerException(500, "Network communication error", e);
        } catch (ServerException e) {
            // Пробрасываем ServerException без изменений
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error for {} {}: {}", method, endpoint, e.getMessage());
            throw new HotelException("Unexpected error during request execution", e);
        } finally {
            // Закрытие ресурсов
            closeResources(writer, reader, connection);
        }
    }

    /**
     * Обновляет дату на сервере и проверяет занятость номеров
     *
     * @param currentDate новая дата в формате yyyy-MM-dd
     * @return true если операция успешна, false в противном случае
     */
    public boolean advanceDate(String currentDate) {
        try {
            String jsonBody = String.format("{\"currentDate\":\"%s\"}", currentDate);
            String response = executeRequest("/rooms/advance-date", "POST", jsonBody);
            return response != null && response.contains("\"success\":true");
        } catch (Exception e) {
            logger.error("❌ Ошибка обновления даты: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет доступность сервера
     *
     * @return true если сервер доступен, false в противном случае
     */
    public boolean isServerAvailable() {
        try {
            // Пробуем получить список клиентов - быстрый endpoint для проверки
            String response = executeRequest("/clients", "GET", null);
            return response != null;

        } catch (ServerException e) {
            // Ожидаемые сетевые ошибки
            logger.warn("Server unavailable: {} (HTTP {})", e.getMessage(), e.getStatusCode());
            return false;

        } catch (HotelException e) {
            // Непредвиденные ошибки приложения
            logger.error("Application error while checking server: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Чтение тела ошибки из HTTP response
     */
    private String readErrorResponse(HttpURLConnection connection) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(errorStream, StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                return errorResponse.toString();
            }
        } catch (IOException e) {
            logger.warn("Could not read error response: {}", e.getMessage());
        }
        return "No error details available";
    }

    /**
     * Безопасное закрытие ресурсов
     */
    private void closeResources(BufferedWriter writer, BufferedReader reader, HttpURLConnection connection) {
        try {
            if (writer != null) writer.close();
        } catch (Exception e) {
            logger.warn("Error closing writer: {}", e.getMessage());
        }

        try {
            if (reader != null) reader.close();
        } catch (Exception e) {
            logger.warn("Error closing reader: {}", e.getMessage());
        }

        try {
            if (connection != null) connection.disconnect();
        } catch (Exception e) {
            logger.warn("Error disconnecting connection: {}", e.getMessage());
        }
    }
}