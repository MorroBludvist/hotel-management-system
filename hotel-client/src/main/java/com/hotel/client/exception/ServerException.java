package com.hotel.client.exception;

/**
 * Исключение, возникающее при ошибках взаимодействия с сервером.
 * Контролирует ситуации недоступности сервера, таймаутов и HTTP ошибок.
 *
 * @author Igor Sekirin  
 * @version 1.0
 */
public class ServerException extends HotelException {
    private final int statusCode;

    /**
     * Создает исключение сервера с HTTP статус кодом и сообщением.
     *
     * @param statusCode HTTP статус код ответа сервера
     * @param message детальное сообщение об ошибке
     */
    public ServerException(int statusCode, String message) {
        super("HTTP " + statusCode + ": " + message);
        this.statusCode = statusCode;
    }

    /**
     * Создает исключение сервера с HTTP статус кодом, сообщением и причиной.
     *
     * @param statusCode HTTP статус код ответа сервера
     * @param message детальное сообщение об ошибке
     * @param cause исходное исключение
     */
    public ServerException(int statusCode, String message, Throwable cause) {
        super("HTTP " + statusCode + ": " + message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Возвращает HTTP статус код ошибки.
     *
     * @return код статуса HTTP
     */
    public int getStatusCode() {
        return statusCode;
    }
}