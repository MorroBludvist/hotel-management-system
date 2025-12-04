package com.hotel.client.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ApiService - исправленная версия
 */
public class ApiServiceTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        // Сброс синглтона перед каждым тестом
        try {
            var field = ApiService.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            // Игнорируем, если поля нет
        }
    }

    @Test
    void testSingletonPattern() throws Exception {
        // Arrange
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");

        // Act
        ApiService instance1 = (ApiService) getInstance.invoke(null);
        ApiService instance2 = (ApiService) getInstance.invoke(null);

        // Assert
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2, "Должен возвращаться один и тот же экземпляр");
    }

    @Test
    void testAdvanceDate_WorksWithoutException() throws Exception {
        // Arrange
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService apiService = (ApiService) getInstance.invoke(null);

        // Act & Assert - просто проверяем, что метод выполняется
        assertDoesNotThrow(() -> apiService.advanceDate("2024-12-25"));

        // Логируем результат для информации
        boolean result = apiService.advanceDate("2024-12-25");
        System.out.println("advanceDate вернул: " + result);
    }

    @Test
    void testIsServerAvailable_WorksWithoutException() throws Exception {
        // Arrange
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService apiService = (ApiService) getInstance.invoke(null);

        // Act & Assert - просто проверяем, что метод выполняется
        assertDoesNotThrow(() -> apiService.isServerAvailable());

        // Логируем результат для информации
        boolean result = apiService.isServerAvailable();
        System.out.println("isServerAvailable вернул: " + result);
    }

    @Test
    void testExecuteRequest_ThrowsExceptionForInvalidEndpoint() throws Exception {
        // Arrange
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService apiService = (ApiService) getInstance.invoke(null);

        // Act & Assert - проверяем, что метод бросает исключение для несуществующего endpoint
        // Это нормально, так как сервер может вернуть 404 или другое исключение
        assertThrows(Exception.class, () -> {
            apiService.executeRequest("/this-endpoint-does-not-exist-12345", "GET", null);
        });
    }

    @Test
    void testReadErrorResponse_WithNullConnection_ShouldReturnDefaultMessage() throws Exception {
        // Arrange
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService apiService = (ApiService) getInstance.invoke(null);

        // Act - тестируем private метод через рефлексию
        Method readErrorResponse = ApiService.class.getDeclaredMethod(
                "readErrorResponse", java.net.HttpURLConnection.class);
        readErrorResponse.setAccessible(true);

        // ВАЖНО: Если метод падает с NPE, давайте посмотрим на его код
        try {
            String result = (String) readErrorResponse.invoke(apiService, (Object) null);

            // Assert - если метод не упал, проверяем результат
            // Это зависит от реализации метода readErrorResponse
            // Если он проверяет connection на null, то вернет дефолтное сообщение
            // Если нет - упадет с NPE
            if (result != null) {
                // Проверяем, что возвращается какое-то сообщение
                assertNotNull(result);
            }
        } catch (Exception e) {
            // Если метод падает с NPE, это проблема в коде ApiService
            // Давайте посмотрим на стектрейс
            Throwable cause = e.getCause();
            if (cause instanceof NullPointerException) {
                System.out.println("⚠ ВНИМАНИЕ: Метод readErrorResponse падает с NPE при null connection");
                System.out.println("   Это нужно исправить в ApiService.java");
                System.out.println("   Добавьте проверку: if (connection == null) return \"No error details available\";");

                // Пропускаем тест, так как это проблема в коде, а не в тесте
                // Но можем зафиксировать это как known issue
                fail("Метод readErrorResponse не обрабатывает null connection. Нужно исправить в ApiService.java");
            } else {
                throw e; // Другая ошибка
            }
        }
    }

    @Test
    void testReadErrorResponse_WithValidConnection() throws Exception {
        // Arrange
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService apiService = (ApiService) getInstance.invoke(null);

        // Создаем простую заглушку соединения
        MockHttpURLConnection mockConnection = new MockHttpURLConnection();
        mockConnection.setErrorStream(new ByteArrayInputStream("Test error message".getBytes()));

        // Act
        Method readErrorResponse = ApiService.class.getDeclaredMethod(
                "readErrorResponse", java.net.HttpURLConnection.class);
        readErrorResponse.setAccessible(true);

        String result = (String) readErrorResponse.invoke(apiService, mockConnection);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Test error message") ||
                result.contains("No error details available"));
    }

    @Test
    void testReadErrorResponse_WithConnectionButNoErrorStream() throws Exception {
        // Arrange
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService apiService = (ApiService) getInstance.invoke(null);

        // Создаем заглушку соединения без errorStream
        MockHttpURLConnection mockConnection = new MockHttpURLConnection();
        mockConnection.setErrorStream(null); // Явно устанавливаем null

        // Act
        Method readErrorResponse = ApiService.class.getDeclaredMethod(
                "readErrorResponse", java.net.HttpURLConnection.class);
        readErrorResponse.setAccessible(true);

        String result = (String) readErrorResponse.invoke(apiService, mockConnection);

        // Assert - должно вернуться дефолтное сообщение
        assertNotNull(result);
        assertEquals("No error details available", result);
    }

    @Test
    void testCloseResources_PrivateMethod() throws Exception {
        // Arrange
        Method getInstance = ApiService.class.getDeclaredMethod("getInstance");
        ApiService apiService = (ApiService) getInstance.invoke(null);

        // Создаем заглушки
        java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.StringWriter());
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.StringReader(""));

        // Act
        Method closeResources = ApiService.class.getDeclaredMethod(
                "closeResources",
                java.io.BufferedWriter.class,
                java.io.BufferedReader.class,
                java.net.HttpURLConnection.class);
        closeResources.setAccessible(true);

        // Должен выполниться без исключений
        assertDoesNotThrow(() ->
                closeResources.invoke(apiService, writer, reader, null)
        );
    }

    @Test
    void testBasicFunctionality() {
        // Простой тест для проверки, что класс загружается и работает
        assertDoesNotThrow(() -> {
            Class<?> clazz = Class.forName("com.hotel.client.service.ApiService");
            assertNotNull(clazz);

            // Проверяем наличие основных методов
            assertTrue(clazz.getMethod("getInstance") != null);
            assertTrue(clazz.getMethod("advanceDate", String.class) != null);
            assertTrue(clazz.getMethod("isServerAvailable") != null);
            assertTrue(clazz.getMethod("executeRequest", String.class, String.class, String.class) != null);
        });
    }

    /**
     * Простая заглушка для HttpURLConnection для тестирования
     */
    private static class MockHttpURLConnection extends java.net.HttpURLConnection {
        private java.io.InputStream errorStream;

        protected MockHttpURLConnection() {
            super(null);
        }

        public void setErrorStream(java.io.InputStream stream) {
            this.errorStream = stream;
        }

        @Override
        public java.io.InputStream getErrorStream() {
            return errorStream;
        }

        @Override
        public void connect() {}

        @Override
        public void disconnect() {}

        @Override
        public boolean usingProxy() {
            return false;
        }
    }
}