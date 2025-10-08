package com.hotel.server.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DataInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        // Проверяем существование таблицы rooms
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='rooms'",
                Integer.class
        );

        if (tableExists == null || tableExists == 0) {
            System.out.println("🔄 Инициализация БД из schema.sql...");
            initializeDatabase();
        } else {
            System.out.println("✅ БД уже инициализирована");
        }
    }

    private void initializeDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            // Выполняем скрипт из schema.sql
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
            System.out.println("✅ База данных успешно инициализирована из schema.sql");
        } catch (Exception e) {
            System.err.println("❌ Ошибка инициализации БД: " + e.getMessage());
        }
    }
}