package com.hotel.client.exception;

/**
 * Базовое исключение для всех ошибок приложения отеля.
 * Используется для унификации обработки ошибок в клиент-серверном взаимодействии.
 *
 * @author Igor Sekirin
 * @version 1.0
 */
public class HotelException extends Exception {

    /**
     * Создает новое исключение отеля с указанным сообщением.
     *
     * @param message детальное сообщение об ошибке
     */
    public HotelException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение отеля с сообщением и причиной.
     *
     * @param message детальное сообщение об ошибке
     * @param cause исходное исключение
     */
    public HotelException(String message, Throwable cause) {
        super(message, cause);
    }
}