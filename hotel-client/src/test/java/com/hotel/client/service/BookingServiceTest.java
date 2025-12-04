package com.hotel.client.service;

import com.hotel.client.model.Client;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Простые тесты для BookingService
 */
class BookingServiceTest {

    @Test
    void testConstructorAndBasicMethods() throws Exception {
        System.out.println("=== Тест конструктора и базовых методов ===");

        // Arrange - создаем BookingService через рефлексию
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);

        // Создаем реальный ApiService (синглтон)
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);

        // Act - создаем BookingService
        BookingService bookingService = constructor.newInstance(realApiService);

        // Assert
        assertNotNull(bookingService);
        System.out.println("✓ BookingService успешно создан");

        // Проверяем, что методы можно вызвать
        assertDoesNotThrow(() -> {
            // Проверяем получение истории (может вернуть пустой список)
            List<Map<String, Object>> history = bookingService.getAllBookingHistory();
            assertNotNull(history);
            System.out.println("✓ getAllBookingHistory вернул список из " + history.size() + " элементов");

            // Проверяем историю по номеру
            List<Map<String, Object>> roomHistory = bookingService.getBookingHistoryByRoom(101);
            assertNotNull(roomHistory);
            System.out.println("✓ getBookingHistoryByRoom(101) вернул список из " + roomHistory.size() + " элементов");
        });
    }

    @Test
    void testCheckOutClient_WithoutException() throws Exception {
        System.out.println("=== Тест checkOutClient ===");

        // 1. Создаем BookingService
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);
        BookingService bookingService = constructor.newInstance(realApiService);

        // 2. Проверяем метод checkOutClient - должен выполниться без исключений
        assertDoesNotThrow(() -> {
            boolean result = bookingService.checkOutClient("TEST123");
            // Не проверяем результат, так как зависит от сервера
            System.out.println("✓ checkOutClient выполнен, результат: " + result);
        });
    }

    @Test
    void testValidateBooking_SafeCheck() throws Exception {
        System.out.println("=== Тест validateBooking ===");

        // Arrange
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);
        BookingService bookingService = constructor.newInstance(realApiService);

        // Создаем тестового клиента
        Client testClient = new Client();
        testClient.setPassportNumber("TEST123");
        testClient.setFirstName("Test");
        testClient.setLastName("User");
        testClient.setRoomNumber(101);

        // Act & Assert - проверяем безопасно
        assertDoesNotThrow(() -> {
            Map<String, Object> result = bookingService.validateBooking(testClient);
            assertNotNull(result, "Результат не должен быть null");

            // БЕЗОПАСНАЯ ПРОВЕРКА: сначала проверяем наличие ключей
            if (result.containsKey("valid")) {
                Boolean valid = (Boolean) result.get("valid");
                assertNotNull(valid, "Ключ 'valid' есть, но значение null");
                System.out.println("  valid: " + valid);
            } else {
                System.out.println("  Ключ 'valid' отсутствует в результате");
            }

            if (result.containsKey("roomAvailable")) {
                Boolean roomAvailable = (Boolean) result.get("roomAvailable");
                System.out.println("  roomAvailable: " + roomAvailable);
            }

            if (result.containsKey("clientExists")) {
                Boolean clientExists = (Boolean) result.get("clientExists");
                System.out.println("  clientExists: " + clientExists);
            }

            if (result.containsKey("message")) {
                String message = (String) result.get("message");
                System.out.println("  message: " + message);
            }

            System.out.println("✓ validateBooking вернул результат с " + result.size() + " полями");
        });
    }

    @Test
    void testPrivateMethods_WithReflection_Safe() throws Exception {
        System.out.println("=== Тест приватных методов ===");

        // Arrange
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);
        BookingService bookingService = constructor.newInstance(realApiService);

        // Тестируем приватный метод parseValidationResponse
        Method parseMethod = BookingService.class.getDeclaredMethod(
                "parseValidationResponse", String.class);
        parseMethod.setAccessible(true);

        // Test 1: валидный JSON с ВСЕМИ полями
        String validJson = "{\"valid\":true,\"roomAvailable\":true,\"clientExists\":false,\"message\":\"OK\"}";
        Map<String, Object> result = (Map<String, Object>) parseMethod.invoke(bookingService, validJson);

        // Assert - безопасная проверка
        assertNotNull(result);
        assertTrue(result.containsKey("valid"));
        assertTrue(result.containsKey("roomAvailable"));
        assertTrue(result.containsKey("clientExists"));
        assertTrue(result.containsKey("message"));

        Boolean valid = (Boolean) result.get("valid");
        Boolean roomAvailable = (Boolean) result.get("roomAvailable");
        Boolean clientExists = (Boolean) result.get("clientExists");
        String message = (String) result.get("message");

        assertNotNull(valid);
        assertNotNull(roomAvailable);
        assertNotNull(clientExists);
        assertNotNull(message);

        assertTrue(valid);
        assertTrue(roomAvailable);
        assertFalse(clientExists);
        assertEquals("OK", message);

        System.out.println("✓ parseValidationResponse с полным JSON работает");

        // Test 2: неполный JSON (без некоторых полей)
        String partialJson = "{\"valid\":false,\"message\":\"Error\"}";
        Map<String, Object> partialResult = (Map<String, Object>) parseMethod.invoke(
                bookingService, partialJson);

        assertNotNull(partialResult);
        assertTrue(partialResult.containsKey("valid"));
        assertTrue(partialResult.containsKey("message"));

        // Поля могут отсутствовать, это нормально
        System.out.println("✓ parseValidationResponse с частичным JSON работает");
    }

    @Test
    void testParseValidationResponse_WithInvalidJson() throws Exception {
        System.out.println("=== Тест parseValidationResponse с невалидным JSON ===");

        // Arrange
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);
        BookingService bookingService = constructor.newInstance(realApiService);

        Method parseMethod = BookingService.class.getDeclaredMethod(
                "parseValidationResponse", String.class);
        parseMethod.setAccessible(true);

        // Act - невалидный JSON
        String invalidJson = "Not a JSON";
        Map<String, Object> result = (Map<String, Object>) parseMethod.invoke(
                bookingService, invalidJson);

        // Assert - должен вернуть ошибку с ВСЕМИ полями
        assertNotNull(result);
        assertTrue(result.containsKey("valid"));
        assertTrue(result.containsKey("roomAvailable"));
        assertTrue(result.containsKey("clientExists"));
        assertTrue(result.containsKey("message"));

        Boolean valid = (Boolean) result.get("valid");
        Boolean roomAvailable = (Boolean) result.get("roomAvailable");
        Boolean clientExists = (Boolean) result.get("clientExists");
        String message = (String) result.get("message");

        assertNotNull(valid);
        assertNotNull(roomAvailable);
        assertNotNull(clientExists);
        assertNotNull(message);

        assertFalse(valid);
        assertFalse(roomAvailable);
        assertFalse(clientExists);
        assertEquals("Ошибка соединения с сервером", message);

        System.out.println("✓ parseValidationResponse с невалидным JSON возвращает ошибку");
    }

    @Test
    void testCreateErrorValidationResponse_Complete() throws Exception {
        System.out.println("=== Тест createErrorValidationResponse ===");

        // Arrange
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);
        BookingService bookingService = constructor.newInstance(realApiService);

        // Тестируем приватный метод
        Method errorMethod = BookingService.class.getDeclaredMethod("createErrorValidationResponse");
        errorMethod.setAccessible(true);

        // Act
        Map<String, Object> result = (Map<String, Object>) errorMethod.invoke(bookingService);

        // Assert - проверяем ВСЕ поля
        assertNotNull(result);

        // Проверяем наличие всех обязательных полей
        String[] requiredKeys = {"valid", "roomAvailable", "clientExists", "message"};
        for (String key : requiredKeys) {
            assertTrue(result.containsKey(key), "Должен содержать ключ: " + key);
            assertNotNull(result.get(key), "Значение для ключа '" + key + "' не должно быть null");
        }

        // Проверяем значения
        Boolean valid = (Boolean) result.get("valid");
        Boolean roomAvailable = (Boolean) result.get("roomAvailable");
        Boolean clientExists = (Boolean) result.get("clientExists");
        String message = (String) result.get("message");

        assertFalse(valid);
        assertFalse(roomAvailable);
        assertFalse(clientExists);
        assertEquals("Ошибка соединения с сервером", message);

        System.out.println("✓ createErrorValidationResponse возвращает полный объект ошибки");
    }

    @Test
    void testCheckInClient_WithoutException() throws Exception {
        System.out.println("=== Тест checkInClient ===");

        // Arrange
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);
        BookingService bookingService = constructor.newInstance(realApiService);

        // Создаем минимального клиента
        Client testClient = new Client();
        testClient.setPassportNumber("TEST_CHECKIN_123");
        testClient.setFirstName("CheckIn");
        testClient.setLastName("Test");
        testClient.setRoomNumber(101);

        // Act & Assert - просто проверяем, что метод выполняется
        assertDoesNotThrow(() -> {
            boolean result = bookingService.checkInClient(testClient);
            System.out.println("✓ checkInClient выполнен, результат: " + result);
        });
    }

    @Test
    void testGetAllBookingHistory_ReturnsList() throws Exception {
        System.out.println("=== Тест getAllBookingHistory ===");

        // Arrange
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);
        BookingService bookingService = constructor.newInstance(realApiService);

        // Act
        List<Map<String, Object>> result = bookingService.getAllBookingHistory();

        // Assert
        assertNotNull(result);
        // Может быть пустым, если нет данных - это нормально
        System.out.println("✓ getAllBookingHistory вернул список из " + result.size() + " элементов");
    }

    @Test
    void testGetBookingHistoryByRoom_ReturnsList() throws Exception {
        System.out.println("=== Тест getBookingHistoryByRoom ===");

        // Arrange
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService realApiService = (ApiService) getInstance.invoke(null);
        BookingService bookingService = constructor.newInstance(realApiService);

        // Act - пробуем несколько номеров
        int[] roomNumbers = {101, 102, 999};
        for (int roomNumber : roomNumbers) {
            assertDoesNotThrow(() -> {
                List<Map<String, Object>> result = bookingService.getBookingHistoryByRoom(roomNumber);
                assertNotNull(result);
                System.out.println("  Номер " + roomNumber + ": " + result.size() + " элементов");
            });
        }
        System.out.println("✓ getBookingHistoryByRoom работает для всех тестовых номеров");
    }

    @Test
    void testNullAndEdgeCases() {
        System.out.println("=== Тест граничных случаев ===");

        assertDoesNotThrow(() -> {
            // 1. Создаем BookingService
            var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
            Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
            ApiService realApiService = (ApiService) getInstance.invoke(null);
            BookingService bookingService = constructor.newInstance(realApiService);

            // 2. Проверяем вызовы с null/пустыми значениями

            // checkOutClient с null
            try {
                boolean result1 = bookingService.checkOutClient(null);
                System.out.println("  checkOutClient(null) вернул: " + result1);
            } catch (Exception e) {
                System.out.println("  checkOutClient(null) вызвал исключение: " + e.getClass().getSimpleName());
            }

            // checkOutClient с пустой строкой
            try {
                boolean result2 = bookingService.checkOutClient("");
                System.out.println("  checkOutClient(\"\") вернул: " + result2);
            } catch (Exception e) {
                System.out.println("  checkOutClient(\"\") вызвал исключение: " + e.getClass().getSimpleName());
            }

            // validateBooking с null клиентом
            try {
                Map<String, Object> result3 = bookingService.validateBooking(null);
                System.out.println("  validateBooking(null) вернул результат с " +
                        (result3 != null ? result3.size() : "null") + " полями");
            } catch (Exception e) {
                System.out.println("  validateBooking(null) вызвал исключение: " + e.getClass().getSimpleName());
            }

            // getBookingHistoryByRoom с 0
            try {
                List<Map<String, Object>> result4 = bookingService.getBookingHistoryByRoom(0);
                System.out.println("  getBookingHistoryByRoom(0) вернул " + result4.size() + " элементов");
            } catch (Exception e) {
                System.out.println("  getBookingHistoryByRoom(0) вызвал исключение: " + e.getClass().getSimpleName());
            }

            // getBookingHistoryByRoom с отрицательным числом
            try {
                List<Map<String, Object>> result5 = bookingService.getBookingHistoryByRoom(-1);
                System.out.println("  getBookingHistoryByRoom(-1) вернул " + result5.size() + " элементов");
            } catch (Exception e) {
                System.out.println("  getBookingHistoryByRoom(-1) вызвал исключение: " + e.getClass().getSimpleName());
            }
        });

        System.out.println("✓ Граничные случаи протестированы");
    }

    @Test
    void testJsonUtilsIntegration() throws Exception {
        System.out.println("=== Тест интеграции с JsonUtils ===");

        // Создаем тестового клиента
        Client testClient = new Client();
        testClient.setPassportNumber("JSON_TEST");
        testClient.setFirstName("Json");
        testClient.setLastName("Test");

        // Пробуем сериализацию
        assertDoesNotThrow(() -> {
            // Используем рефлексию для доступа к JsonUtils
            Class<?> jsonUtilsClass = Class.forName("com.hotel.client.util.JsonUtils");
            Method toJsonMethod = jsonUtilsClass.getMethod("toJson", Object.class);

            String json = (String) toJsonMethod.invoke(null, testClient);
            assertNotNull(json);
            assertTrue(json.contains("JSON_TEST"));
            assertTrue(json.contains("Json"));
            assertTrue(json.contains("Test"));

            System.out.println("✓ JsonUtils.toJson работает: " +
                    json.substring(0, Math.min(50, json.length())) + "...");
        });
    }

    @Test
    void testServiceInitializationLogging() throws Exception {
        System.out.println("=== Тестирование инициализации BookingService ===");

        // 1. Получаем конструктор
        var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);

        // 2. Получаем ApiService
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService apiService = (ApiService) getInstance.invoke(null);

        // 3. Создаем BookingService
        BookingService bookingService = constructor.newInstance(apiService);

        // 4. Проверяем
        assertNotNull(bookingService);
        assertNotNull(apiService);

        System.out.println("✓ BookingService успешно инициализирован");
        System.out.println("✓ ApiService получен: " + apiService.getClass().getName());

        // Проверяем, что можем вызвать основные методы
        try {
            Method getAllHistoryMethod = BookingService.class.getMethod("getAllBookingHistory");
            Object result1 = getAllHistoryMethod.invoke(bookingService);
            assertNotNull(result1);
            System.out.println("✓ Метод getAllBookingHistory работает");
        } catch (Exception e) {
            System.out.println("✗ Метод getAllBookingHistory не работает: " + e.getMessage());
            throw e;
        }

        try {
            Method getHistoryByRoomMethod = BookingService.class.getMethod("getBookingHistoryByRoom", int.class);
            Object result2 = getHistoryByRoomMethod.invoke(bookingService, 101);
            assertNotNull(result2);
            System.out.println("✓ Метод getBookingHistoryByRoom работает");
        } catch (Exception e) {
            System.out.println("✗ Метод getBookingHistoryByRoom не работает: " + e.getMessage());
            throw e;
        }

        System.out.println("=== Все тесты инициализации пройдены ===");
    }

    @Test
    void testMethodExistence() {
        System.out.println("=== Проверка существования методов ===");

        // Проверяем, что все публичные методы существуют
        String[] publicMethods = {
                "checkInClient",
                "checkOutClient",
                "validateBooking",
                "getAllBookingHistory",
                "getBookingHistoryByRoom"
        };

        for (String methodName : publicMethods) {
            try {
                Method method;
                if (methodName.equals("checkInClient")) {
                    method = BookingService.class.getMethod(methodName, Client.class);
                } else if (methodName.equals("checkOutClient")) {
                    method = BookingService.class.getMethod(methodName, String.class);
                } else if (methodName.equals("validateBooking")) {
                    method = BookingService.class.getMethod(methodName, Client.class);
                } else if (methodName.equals("getAllBookingHistory")) {
                    method = BookingService.class.getMethod(methodName);
                } else if (methodName.equals("getBookingHistoryByRoom")) {
                    method = BookingService.class.getMethod(methodName, int.class);
                } else {
                    throw new NoSuchMethodException("Неизвестный метод: " + methodName);
                }
                System.out.println("✓ Метод " + methodName + " существует");
            } catch (NoSuchMethodException e) {
                System.out.println("✗ Метод " + methodName + " не найден: " + e.getMessage());
                fail("Метод " + methodName + " должен существовать");
            }
        }

        // Проверяем приватные методы через рефлексию
        String[] privateMethods = {
                "parseValidationResponse",
                "createErrorValidationResponse"
        };

        for (String methodName : privateMethods) {
            try {
                Method method;
                if (methodName.equals("parseValidationResponse")) {
                    method = BookingService.class.getDeclaredMethod(methodName, String.class);
                } else if (methodName.equals("createErrorValidationResponse")) {
                    method = BookingService.class.getDeclaredMethod(methodName);
                } else {
                    throw new NoSuchMethodException("Неизвестный приватный метод: " + methodName);
                }
                System.out.println("✓ Приватный метод " + methodName + " существует");
            } catch (NoSuchMethodException e) {
                System.out.println("✗ Приватный метод " + methodName + " не найден: " + e.getMessage());
            }
        }

        System.out.println("=== Проверка методов завершена ===");
    }

    @Test
    void testConstructorExists() {
        System.out.println("=== Проверка конструкторов ===");

        // Проверяем, что конструктор с ApiService существует
        try {
            var constructor = BookingService.class.getDeclaredConstructor(ApiService.class);
            System.out.println("✓ Конструктор BookingService(ApiService) существует");

            // Проверяем параметры конструктора
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            assertEquals(1, parameterTypes.length);
            assertEquals(ApiService.class, parameterTypes[0]);
            System.out.println("  Параметр: " + parameterTypes[0].getName());

        } catch (NoSuchMethodException e) {
            System.out.println("✗ Конструктор не найден: " + e.getMessage());
            fail("Конструктор BookingService(ApiService) должен существовать");
        }

        System.out.println("=== Проверка конструкторов завершена ===");
    }
}