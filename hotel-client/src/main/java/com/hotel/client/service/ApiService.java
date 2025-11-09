package com.hotel.client.service;

import com.hotel.client.config.AppConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.hotel.client.exception.HotelException;
import com.hotel.client.exception.ServerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

//TODO: залогировать
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
     * Отправление запроса на сервер
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

            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            String auth = AppConfig.API_USERNAME + ":" + AppConfig.API_PASSWORD;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            connection.setConnectTimeout(AppConfig.API_TIMEOUT);
            connection.setReadTimeout(AppConfig.API_TIMEOUT);

            if (jsonBody != null && (method.equals("POST") || method.equals("PUT"))) {
                connection.setDoOutput(true);
                writer = new BufferedWriter(new OutputStreamWriter(
                        connection.getOutputStream(), StandardCharsets.UTF_8));
                writer.write(jsonBody);
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            logger.info("HTTP {} {} -> {}", method, endpoint, responseCode);

            if (responseCode >= 400) {
                String errorMessage = readErrorResponse(connection);
                logger.error("HTTP error {}: {}", responseCode, errorMessage);
                throw new ServerException(responseCode, "HTTP " + responseCode + ": " + errorMessage);
            }

            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } else {
                throw new HotelException("Unexpected response code: " + responseCode);
            }

        } catch (SocketTimeoutException e) {
            logger.error("Request timeout: {}", e.getMessage());
            throw new ServerException(408, "Request timeout - server not responding", e);
        } catch (UnknownHostException e) {
            logger.error("Server not found: {}", e.getMessage());
            throw new ServerException(503, "Server unavailable - unknown host", e);
        } catch (IOException e) {
            logger.error("Network error: {}", e.getMessage());
            throw new ServerException(500, "Network communication error", e);
        } catch (ServerException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            throw new HotelException("Unexpected error during request execution", e);
        } finally {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                logger.warn("Error closing resources: {}", e.getMessage());
            }
        }
    }

    /**
     * Чтение ошибки из HTTP response
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
     * Обработка строки для устранения лишних символов
     */
    public String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Извлечение целочисленное значение
     */
    public String extractStringValue(String json, String key) {
        try {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start == -1) return null;

            start += search.length();
            int end = json.indexOf("\"", start);
            if (end == -1) return null;

            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Извлечение целочисленное значение
     */
    public Integer extractIntegerValue(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start == -1) return null;

            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) return null;

            String value = json.substring(start, end).trim();
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Извлечение дробное значение
     */
    public Double extractDoubleValue(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start == -1) return null;

            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) return null;

            String value = json.substring(start, end).trim();
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Обновляет дату и проверяет занятость
     */
    public boolean advanceDate(String currentDate) {
        try {
            String jsonBody = String.format("{\"currentDate\":\"%s\"}", currentDate);
            String response = executeRequest("/rooms/advance-date", "POST", jsonBody);
            return response != null && response.contains("\"success\":true");
        } catch (Exception e) {
            System.err.println("❌ Ошибка обновления даты: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет доступность сервера с обработкой исключений
     * @return true если сервер доступен, false в противном случае
     */
    public boolean isServerAvailable() {
        try {
            String response = executeRequest("/clients", "GET", null);
            return response != null;

        } catch (ServerException e) {
            // Обрабатываем ServerException - это ожидаемые сетевые ошибки
            logger.warn("Server unavailable: {} (HTTP {})", e.getMessage(), e.getStatusCode());
            return false;

        } catch (HotelException e) {
            // Обрабатываем HotelException - это непредвиденные ошибки приложения
            logger.error("Application error while checking server: {}", e.getMessage());
            return false;
        }
    }
}