package com.hotel.client.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class JasperReportService {
    private static final Logger logger = LogManager.getLogger(JasperReportService.class);

    // Пути к шаблонам в resources
    private static final String STAFF_PDF_TEMPLATE = "/reports/staff_report_pdf.jrxml";
    private static final String ROOMS_HTML_TEMPLATE = "/reports/room_report.jrxml";

    // Параметры подключения к SQLite
    private static final String DB_URL = "jdbc:sqlite:hotel-server/hotel.db";

    /**
     * Генерация PDF отчета по сотрудникам
     */
    public void generateStaffPdfReport() {
        Connection connection = null;
        try {
            // 1. Получаем соединение с БД
            connection = DriverManager.getConnection(DB_URL);

            // 2. Загружаем шаблон из resources
            InputStream templateStream = getClass().getResourceAsStream(STAFF_PDF_TEMPLATE);
            if (templateStream == null) {
                throw new RuntimeException("Шаблон отчета не найден: " + STAFF_PDF_TEMPLATE);
            }

            // 3. Компилируем шаблон
            JasperReport report = JasperCompileManager.compileReport(templateStream);

            // 4. Параметры отчета
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Отчет по сотрудникам");
            parameters.put("COMPANY_NAME", "Отель 'Люкс'");
            parameters.put("GENERATED_BY", System.getProperty("user.name"));
            parameters.put("GENERATION_DATE", new java.util.Date());

            // 5. Заполняем отчет данными из БД
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);

            // 6. Выбор места сохранения файла
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить PDF отчет");
            fileChooser.setSelectedFile(new java.io.File("staff_report_" + System.currentTimeMillis() + ".pdf"));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                // Добавляем расширение .pdf если нужно
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }

                // 7. Экспортируем в PDF
                JasperExportManager.exportReportToPdfFile(print, filePath);

                logger.info("PDF отчет создан: {}", filePath);

                // 8. Показать сообщение об успехе
                int option = JOptionPane.showConfirmDialog(null,
                        "PDF отчет успешно создан!\nОткрыть файл?",
                        "Успех",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    openFile(filePath);
                }
            }

        } catch (Exception e) {
            logger.error("Ошибка при генерации PDF отчета: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                    "Ошибка при генерации PDF отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Закрываем соединение
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    logger.error("Ошибка при закрытии соединения: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Генерация HTML отчета по номерам
     */
    public void generateRoomsHtmlReport() {
        Connection connection = null;
        try {
            // 1. Получаем соединение с БД
            connection = DriverManager.getConnection(DB_URL);

            // 2. Загружаем шаблон из resources
            InputStream templateStream = getClass().getResourceAsStream(ROOMS_HTML_TEMPLATE);
            if (templateStream == null) {
                throw new RuntimeException("Шаблон отчета не найден: " + ROOMS_HTML_TEMPLATE);
            }

            // 3. Компилируем шаблон
            JasperReport report = JasperCompileManager.compileReport(templateStream);

            // 4. Параметры отчета
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Справочник номеров");
            parameters.put("COMPANY_NAME", "Отель 'Люкс'");
            parameters.put("GENERATED_BY", System.getProperty("user.name"));
            parameters.put("GENERATION_DATE", new java.util.Date());

            // 5. Заполняем отчет данными из БД
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);

            // 6. Выбор места сохранения файла
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить HTML отчет");
            fileChooser.setSelectedFile(new java.io.File("rooms_report_" + System.currentTimeMillis() + ".html"));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML Files", "html"));

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                // Добавляем расширение .html если нужно
                if (!filePath.toLowerCase().endsWith(".html")) {
                    filePath += ".html";
                }

                // 7. Экспортируем в HTML
                HtmlExporter exporter = new HtmlExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleHtmlExporterOutput(filePath));

                // Настройки для HTML
                SimpleHtmlReportConfiguration configuration = new SimpleHtmlReportConfiguration();
                configuration.setWhitePageBackground(false);
                configuration.setRemoveEmptySpaceBetweenRows(true);
                exporter.setConfiguration(configuration);

                exporter.exportReport();

                logger.info("HTML отчет создан: {}", filePath);

                // 8. Показать сообщение об успехе
                int option = JOptionPane.showConfirmDialog(null,
                        "HTML отчет успешно создан!\nОткрыть файл в браузере?",
                        "Успех",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    openFile(filePath);
                }
            }

        } catch (Exception e) {
            logger.error("Ошибка при генерации HTML отчета: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                    "Ошибка при генерации HTML отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Закрываем соединение
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    logger.error("Ошибка при закрытии соединения: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Генерация отчета из XML файла (для лабораторной работы)
     */
    public void generateReportFromXml(String xmlFilePath, String jrxmlTemplate, String outputFormat) {
        try {
            // 1. Загружаем шаблон
            InputStream templateStream = getClass().getResourceAsStream(jrxmlTemplate);
            if (templateStream == null) {
                throw new RuntimeException("Шаблон отчета не найден: " + jrxmlTemplate);
            }

            // 2. Компилируем шаблон
            JasperReport report = JasperCompileManager.compileReport(templateStream);

            // 3. Параметры отчета
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Отчет из XML");
            parameters.put("COMPANY_NAME", "Отель 'Люкс'");
            parameters.put("GENERATED_BY", System.getProperty("user.name"));
            parameters.put("GENERATION_DATE", new java.util.Date());

            // 4. Используем XML как источник данных
            // В реальном проекте нужно преобразовать XML в JRDataSource

            // 5. Заполняем отчет
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

            // 6. Экспортируем в нужный формат
            switch (outputFormat.toLowerCase()) {
                case "pdf":
                    JasperExportManager.exportReportToPdfFile(print, "output_report.pdf");
                    break;
                case "html":
                    HtmlExporter exporter = new HtmlExporter();
                    exporter.setExporterInput(new SimpleExporterInput(print));
                    exporter.setExporterOutput(new SimpleHtmlExporterOutput("output_report.html"));
                    exporter.exportReport();
                    break;
                default:
                    throw new IllegalArgumentException("Неподдерживаемый формат: " + outputFormat);
            }

            logger.info("Отчет из XML создан в формате {}", outputFormat);

        } catch (Exception e) {
            logger.error("Ошибка при генерации отчета из XML: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                    "Ошибка при генерации отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Открытие файла в соответствующем приложении
     */
    private void openFile(String filePath) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
            }
        } catch (Exception e) {
            logger.error("Не удалось открыть файл: {}", e.getMessage());
        }
    }
}