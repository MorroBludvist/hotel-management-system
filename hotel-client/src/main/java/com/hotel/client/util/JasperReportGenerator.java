package com.hotel.client.util;

import com.hotel.client.model.Staff;
import com.hotel.client.model.Room;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class JasperReportGenerator {
    private static final Logger logger = LogManager.getLogger(JasperReportGenerator.class);

    // Пути к шаблонам
    private static final String STAFF_TEMPLATE = "/reports/staff_report.jrxml";
    private static final String ROOMS_TEMPLATE = "/reports/room_report.jrxml";

    /**
     * Генерация PDF отчета по сотрудникам
     */
    public boolean generateStaffPdfReport(List<Staff> staffList, String outputPath) {
        try {
            logger.info("Генерация PDF отчета по сотрудникам...");

            InputStream templateStream = getClass().getResourceAsStream(STAFF_TEMPLATE);
            if (templateStream == null) {
                logger.error("Шаблон отчета не найден: {}", STAFF_TEMPLATE);
                showErrorMessage("Шаблон отчета не найден: staff_report.jrxml");
                return false;
            }

            JasperReport report = JasperCompileManager.compileReport(templateStream);
            Map<String, Object> parameters = prepareReportParameters("Отчет по сотрудникам");

            List<Map<String, Object>> reportData = convertStaffToReportData(staffList);
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

            // Простой экспорт в PDF
            JasperExportManager.exportReportToPdfFile(print, outputPath);

            logger.info("PDF отчет по сотрудникам успешно создан: {}", outputPath);
            return true;

        } catch (Exception e) {
            logger.error("Ошибка генерации PDF отчета по сотрудникам: {}", e.getMessage(), e);
            showErrorMessage("Ошибка создания PDF: " + e.getMessage());
            return false;
        }
    }

    /**
     * Генерация HTML отчета по сотрудникам
     */
    public boolean generateStaffHtmlReport(List<Staff> staffList, String outputPath) {
        try {
            logger.info("Генерация HTML отчета по сотрудникам...");

            InputStream templateStream = getClass().getResourceAsStream(STAFF_TEMPLATE);
            if (templateStream == null) {
                logger.error("Шаблон отчета не найден: {}", STAFF_TEMPLATE);
                showErrorMessage("Шаблон отчета не найден: staff_report.jrxml");
                return false;
            }

            JasperReport report = JasperCompileManager.compileReport(templateStream);
            Map<String, Object> parameters = prepareReportParameters("Отчет по сотрудникам");
            List<Map<String, Object>> reportData = convertStaffToReportData(staffList);
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

            // Экспорт в HTML
            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputPath));

            SimpleHtmlReportConfiguration configuration = new SimpleHtmlReportConfiguration();
            configuration.setWhitePageBackground(false);
            configuration.setRemoveEmptySpaceBetweenRows(true);
            exporter.setConfiguration(configuration);

            exporter.exportReport();

            logger.info("HTML отчет по сотрудникам успешно создан: {}", outputPath);
            return true;

        } catch (Exception e) {
            logger.error("Ошибка генерации HTML отчета по сотрудникам: {}", e.getMessage(), e);
            showErrorMessage("Ошибка создания HTML: " + e.getMessage());
            return false;
        }
    }

    /**
     * Генерация PDF отчета по номерам
     */
    public boolean generateRoomsPdfReport(List<Room> rooms, String outputPath) {
        try {
            logger.info("Генерация PDF отчета по номерам...");

            InputStream templateStream = getClass().getResourceAsStream(ROOMS_TEMPLATE);
            if (templateStream == null) {
                logger.error("Шаблон отчета не найден: {}", ROOMS_TEMPLATE);
                showErrorMessage("Шаблон отчета не найден: room_report.jrxml");
                return false;
            }

            JasperReport report = JasperCompileManager.compileReport(templateStream);
            Map<String, Object> parameters = prepareReportParameters("Отчет по номерам");
            List<Map<String, Object>> reportData = convertRoomsToReportData(rooms);
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

            JasperExportManager.exportReportToPdfFile(print, outputPath);

            logger.info("PDF отчет по номерам успешно создан: {}", outputPath);
            return true;

        } catch (Exception e) {
            logger.error("Ошибка генерации PDF отчета по номерам: {}", e.getMessage(), e);
            showErrorMessage("Ошибка создания PDF: " + e.getMessage());
            return false;
        }
    }

    /**
     * Генерация HTML отчета по номерам
     */
    public boolean generateRoomsHtmlReport(List<Room> rooms, String outputPath) {
        try {
            logger.info("Генерация HTML отчета по номерам...");

            InputStream templateStream = getClass().getResourceAsStream(ROOMS_TEMPLATE);
            if (templateStream == null) {
                logger.error("Шаблон отчета не найден: {}", ROOMS_TEMPLATE);
                showErrorMessage("Шаблон отчета не найден: room_report.jrxml");
                return false;
            }

            JasperReport report = JasperCompileManager.compileReport(templateStream);
            Map<String, Object> parameters = prepareReportParameters("Отчет по номерам");
            List<Map<String, Object>> reportData = convertRoomsToReportData(rooms);
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputPath));

            SimpleHtmlReportConfiguration configuration = new SimpleHtmlReportConfiguration();
            configuration.setWhitePageBackground(false);
            configuration.setRemoveEmptySpaceBetweenRows(true);
            exporter.setConfiguration(configuration);

            exporter.exportReport();

            logger.info("HTML отчет по номерам успешно создан: {}", outputPath);
            return true;

        } catch (Exception e) {
            logger.error("Ошибка генерации HTML отчета по номерам: {}", e.getMessage(), e);
            showErrorMessage("Ошибка создания HTML: " + e.getMessage());
            return false;
        }
    }

    /**
     * Предпросмотр отчета по сотрудникам
     */
    public void previewStaffReport(List<Staff> staffList) throws JRException {
        try {
            InputStream templateStream = getClass().getResourceAsStream(STAFF_TEMPLATE);
            if (templateStream == null) {
                throw new RuntimeException("Шаблон отчета не найден: " + STAFF_TEMPLATE);
            }

            JasperReport report = JasperCompileManager.compileReport(templateStream);
            Map<String, Object> parameters = prepareReportParameters("Отчет по сотрудникам - Предпросмотр");
            List<Map<String, Object>> reportData = convertStaffToReportData(staffList);
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Отчет по сотрудникам - Предпросмотр");
            viewer.setVisible(true);

        } catch (Exception e) {
            logger.error("Ошибка предпросмотра отчета по сотрудникам: {}", e.getMessage(), e);
            throw new JRException("Не удалось открыть предпросмотр: " + e.getMessage(), e);
        }
    }

    /**
     * Предпросмотр отчета по номерам
     */
    public void previewRoomsReport(List<Room> rooms) throws JRException {
        try {
            InputStream templateStream = getClass().getResourceAsStream(ROOMS_TEMPLATE);
            if (templateStream == null) {
                throw new RuntimeException("Шаблон отчета не найден: " + ROOMS_TEMPLATE);
            }

            JasperReport report = JasperCompileManager.compileReport(templateStream);
            Map<String, Object> parameters = prepareReportParameters("Отчет по номерам - Предпросмотр");
            List<Map<String, Object>> reportData = convertRoomsToReportData(rooms);
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Отчет по номерам - Предпросмотр");
            viewer.setVisible(true);

        } catch (Exception e) {
            logger.error("Ошибка предпросмотра отчета по номерам: {}", e.getMessage(), e);
            throw new JRException("Не удалось открыть предпросмотр: " + e.getMessage(), e);
        }
    }

    /**
     * Подготовка параметров для отчета
     */
    private Map<String, Object> prepareReportParameters(String title) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", title);
        parameters.put("COMPANY_NAME", "Отель 'Какой то там отель'");
        parameters.put("GENERATED_BY", System.getProperty("user.name"));
        parameters.put("GENERATION_DATE", new Date());
        return parameters;
    }

    /**
     * Преобразование списка сотрудников в данные для отчета
     */
    private List<Map<String, Object>> convertStaffToReportData(List<Staff> staffList) {
        List<Map<String, Object>> reportData = new ArrayList<>();

        for (Staff staff : staffList) {
            Map<String, Object> row = new HashMap<>();
            row.put("passport_number", staff.getPassportNumber() != null ? staff.getPassportNumber() : "Не указан");
            row.put("first_name", staff.getFirstName() != null ? staff.getFirstName() : "");
            row.put("last_name", staff.getLastName() != null ? staff.getLastName() : "");
            row.put("position", staff.getPosition() != null ? staff.getPosition() : "Не указана");
            row.put("phone_number", staff.getPhoneNumber() != null ? staff.getPhoneNumber() : "Не указан");
            row.put("email", staff.getEmail() != null ? staff.getEmail() : "Не указан");
            row.put("hire_date", staff.getHireDate() != null ? staff.getHireDate() : "Не указана");
            row.put("salary", staff.getSalary());
            row.put("department", staff.getDepartment() != null ? staff.getDepartment() : "Не указан");
            row.put("status", "active");
            reportData.add(row);
        }

        return reportData;
    }

    /**
     * Преобразование списка номеров в данные для отчета
     */
    private List<Map<String, Object>> convertRoomsToReportData(List<Room> rooms) {
        List<Map<String, Object>> reportData = new ArrayList<>();

        for (Room room : rooms) {
            Map<String, Object> row = new HashMap<>();
            row.put("room_number", room.getRoomNumber());
            row.put("room_type", formatRoomType(room.getRoomType()));
            row.put("status", room.getStatus() != null ? room.getStatus() : "unknown");
            reportData.add(row);
        }

        return reportData;
    }

    /**
     * Форматирование типа комнаты для отчета
     */
    private String formatRoomType(String roomType) {
        if (roomType == null) return "Стандарт";

        String typeUpper = roomType.toUpperCase();
        if (typeUpper.contains("LUX") || typeUpper.contains("ЛЮКС")) return "Люкс";
        if (typeUpper.contains("SUITE") || typeUpper.contains("СЬЮТ")) return "Сьют";
        if (typeUpper.contains("DELUXE")) return "Делюкс";
        if (typeUpper.contains("ECONOMY") || typeUpper.contains("ЭКОНОМ")) return "Эконом";
        if (typeUpper.contains("STANDARD") || typeUpper.contains("СТАНДАРТ")) return "Стандарт";

        return roomType;
    }

    /**
     * Показать сообщение об ошибке
     */
    private void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    message,
                    "Ошибка генерации отчета",
                    JOptionPane.ERROR_MESSAGE);
        });
    }
}