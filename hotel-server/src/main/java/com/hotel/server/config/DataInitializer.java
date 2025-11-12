package com.hotel.server.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
@Component
public class DataInitializer {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LogManager.getLogger(DataInitializer.class);

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct //вызов метода после инициализации подключения
    public void initialize() {
        try {
            // Проверяем, есть ли уже комнаты
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rooms", Integer.class);
            if (count != null && count == 0) {
                logger.info("Инициализация комнат в базе данных...");

                String sql = """
                    INSERT INTO rooms (room_number, room_type) VALUES
                    (101, 'Эконом'), (102, 'Эконом'), (103, 'Эконом'), (104, 'Эконом'), (105, 'Эконом'),
                    (106, 'Эконом'), (107, 'Эконом'), (108, 'Эконом'), (109, 'Эконом'), (110, 'Эконом'),
                    (201, 'Стандарт'), (202, 'Стандарт'), (203, 'Стандарт'), (204, 'Стандарт'), (205, 'Стандарт'),
                    (206, 'Стандарт'), (207, 'Стандарт'), (208, 'Стандарт'), (209, 'Стандарт'), (210, 'Стандарт'),
                    (211, 'Стандарт'), (212, 'Стандарт'), (213, 'Стандарт'), (214, 'Стандарт'), (215, 'Стандарт'),
                    (216, 'Стандарт'), (217, 'Стандарт'), (218, 'Стандарт'), (219, 'Стандарт'), (220, 'Стандарт'),
                    (301, 'Бизнес'), (302, 'Бизнес'), (303, 'Бизнес'), (304, 'Бизнес'),
                    (305, 'Бизнес'), (306, 'Бизнес'), (307, 'Бизнес'),
                    (401, 'Люкс'), (402, 'Люкс'), (403, 'Люкс')
                    """;

                jdbcTemplate.update(sql);
                logger.info("Комнаты успешно инициализированы");
            } else {
                logger.info("Комнаты уже существуют в базе данных ({} записей)", count);
            }
        } catch (Exception e) {
            logger.error("Ошибка при инициализации базы данных: {}", e.getMessage());
        }
    }

    //TODO: убрать инициализацию в schema.sql в будущем
    private void createBookingHistoryTable() {
        try {
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS booking_history (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    room_number INT NOT NULL,
                    client_passport VARCHAR(50) NOT NULL,
                    check_in_date VARCHAR(10) NOT NULL,
                    check_out_date VARCHAR(10) NOT NULL,
                    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) DEFAULT 'completed'
                )
            """;

            jdbcTemplate.execute(createTableSQL);
            logger.info("✅ Таблица booking_history создана или уже существует");

        } catch (Exception e) {
            logger.error("❌ Ошибка создания таблицы booking_history: {}", e.getMessage());
        }
    }
}