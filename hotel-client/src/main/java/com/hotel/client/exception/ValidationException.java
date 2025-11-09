package com.hotel.client.exception;

/**
 * Исключение, возникающее при ошибках валидации данных.
 * Контролирует ситуации с некорректными входными данными от пользователя
 * или нарушениями бизнес-правил приложения.
 *
 * @author Igor Sekirin
 * @version 1.0
 */
public class ValidationException extends HotelException {

    /**
     * Создает исключение валидации с указанным сообщением.
     *
     * @param message детальное сообщение об ошибке валидации
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Создает исключение валидации с сообщением и причиной.
     *
     * @param message детальное сообщение об ошибке валидации
     * @param cause исходное исключение
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}