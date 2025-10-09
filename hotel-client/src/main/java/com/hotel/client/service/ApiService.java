package com.hotel.client.service;

import com.hotel.client.config.AppConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//TODO: залогировать
public class ApiService {
    private static ApiService instance;

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    /**
     * Отправление запроса на сервер
     */
    public String executeRequest(String endpoint, String method, String jsonBody) {
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
            System.out.println("📡 HTTP " + method + " " + endpoint + " -> " + responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    reader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
                }
            }

            if (reader != null) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }

        } catch (IOException e) {
            System.err.println("❌ Ошибка сети: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
        } finally {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                System.err.println("❌ Ошибка закрытия ресурсов: " + e.getMessage());
            }
        }
        return null;
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
     * Проверяет доступность сервера
     */
    public boolean isServerAvailable() {
        try {
            String response = executeRequest("/clients", "GET", null);
            return response != null;
        } catch (Exception e) {
            return false;
        }
    }
}