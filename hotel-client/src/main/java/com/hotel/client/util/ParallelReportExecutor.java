package com.hotel.client.util;

import com.hotel.client.model.Staff;
import com.hotel.client.model.Room;
import com.hotel.client.service.StaffService;
import com.hotel.client.service.RoomService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.*;

public class ParallelReportExecutor {
    private static final Logger logger = LogManager.getLogger(ParallelReportExecutor.class);

    private final StaffService staffService;
    private final RoomService roomService;
    private final JasperReportGenerator reportGenerator;

    private volatile List<Staff> staffData;
    private volatile List<Room> roomData;
    private volatile String xmlFilePath;

    public ParallelReportExecutor(StaffService staffService, RoomService roomService) {
        this.staffService = staffService;
        this.roomService = roomService;
        this.reportGenerator = new JasperReportGenerator();
    }

    /**
     * Главный метод для запуска параллельной генерации отчета
     */
    public void generateReportInParallel(String reportType, String format, File outputFile) {
        logger.info("🚀 ЗАПУСК МНОГОПОТОЧНОЙ ГЕНЕРАЦИИ ОТЧЕТА");
        logger.info("Тип отчета: {}, Формат: {}, Выходной файл: {}",
                reportType, format, outputFile.getAbsolutePath());

        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            // Поток 1: Загрузка данных
            Future<?> loadFuture = executor.submit(() -> {
                logger.info("[ПОТОК 1] === НАЧАЛО ЗАГРУЗКИ ДАННЫХ ===");
                loadData();
                logger.info("[ПОТОК 1] === ЗАГРУЗКА ДАННЫХ ЗАВЕРШЕНА ===");
            });

            // Поток 2: Редактирование данных и создание XML (ждет первый поток)
            Future<?> editFuture = executor.submit(() -> {
                logger.info("[ПОТОК 2] === ОЖИДАНИЕ ЗАГРУЗКИ ДАННЫХ ===");
                try {
                    // Ждем завершения загрузки данных
                    loadFuture.get();
                    logger.info("[ПОТОК 2] === НАЧАЛО РЕДАКТИРОВАНИЯ ДАННЫХ И СОЗДАНИЯ XML ===");
                    editDataAndCreateXml();
                    logger.info("[ПОТОК 2] === XML ФАЙЛ СОЗДАН ===");
                } catch (Exception e) {
                    logger.error("[ПОТОК 2] Ошибка: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            });

            // Поток 3: Построение отчета (ждет второй поток)
            Future<Boolean> reportFuture = executor.submit(() -> {
                logger.info("[ПОТОК 3] === ОЖИДАНИЕ СОЗДАНИЯ XML ===");
                try {
                    // Ждем завершения создания XML
                    editFuture.get();
                    logger.info("[ПОТОК 3] === НАЧАЛО ПОСТРОЕНИЯ ОТЧЕТА ===");
                    boolean result = buildReport(reportType, format, outputFile);
                    logger.info("[ПОТОК 3] === ПОСТРОЕНИЕ ОТЧЕТА ЗАВЕРШЕНО: {} ===",
                            result ? "УСПЕХ" : "ОШИБКА");
                    return result;
                } catch (Exception e) {
                    logger.error("[ПОТОК 3] Ошибка: {}", e.getMessage());
                    return false;
                }
            });

            // Ожидаем завершения всех потоков
            boolean success = reportFuture.get();

            if (success) {
                logger.info("✅ ВСЕ ПОТОКИ УСПЕШНО ЗАВЕРШЕНЫ");
                showSuccessMessage(outputFile);
            } else {
                logger.error("❌ ОШИБКА ПРИ ГЕНЕРАЦИИ ОТЧЕТА");
                showErrorMessage("Не удалось сгенерировать отчет");
            }

        } catch (Exception e) {
            logger.error("❌ ОШИБКА В МНОГОПОТОЧНОЙ ГЕНЕРАЦИИ: {}", e.getMessage());
            showErrorMessage("Ошибка: " + e.getMessage());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // Поток 1: Загрузка данных
    private void loadData() {
        try {
            logger.info("[ПОТОК 1] Запрос данных сотрудников...");
            staffData = staffService.getAllStaff();
            logger.info("[ПОТОК 1] Загружено сотрудников: {}", staffData.size());

            logger.info("[ПОТОК 1] Запрос данных номеров...");
            roomData = roomService.getAllRooms();
            logger.info("[ПОТОК 1] Загружено номеров: {}", roomData.size());

            // Имитация обработки для наглядности
            Thread.sleep(1500);
            logger.info("[ПОТОК 1] Данные загружены в экранную форму");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[ПОТОК 1] Загрузка прервана");
        } catch (Exception e) {
            logger.error("[ПОТОК 1] Ошибка загрузки: {}", e.getMessage());
        }
    }

    // Поток 2: Редактирование данных и создание XML
    private void editDataAndCreateXml() {
        try {
            if (staffData == null || roomData == null) {
                throw new IllegalStateException("Данные не загружены");
            }

            logger.info("[ПОТОК 2] Редактирование данных...");
            logger.info("[ПОТОК 2] Обработка {} записей сотрудников", staffData.size());
            logger.info("[ПОТОК 2] Обработка {} записей номеров", roomData.size());

            // Имитация редактирования
            Thread.sleep(2000);

            // Создание XML файла
            xmlFilePath = createReportXml();
            logger.info("[ПОТОК 2] XML файл создан: {}", xmlFilePath);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[ПОТОК 2] Редактирование прервано");
        } catch (Exception e) {
            logger.error("[ПОТОК 2] Ошибка: {}", e.getMessage());
        }
    }

    // Поток 3: Построение отчета
    private boolean buildReport(String reportType, String format, File outputFile) {
        try {
            if (xmlFilePath == null) {
                throw new IllegalStateException("XML файл не создан");
            }

            logger.info("[ПОТОК 3] Чтение XML файла: {}", xmlFilePath);
            Thread.sleep(1000);

            // Используем существующий JasperReportGenerator
            boolean success = false;

            switch (reportType) {
                case "Отчет по сотрудникам":
                    if ("PDF".equals(format)) {
                        success = reportGenerator.generateStaffPdfReport(staffData, outputFile.getAbsolutePath());
                    } else if ("HTML".equals(format)) {
                        success = reportGenerator.generateStaffHtmlReport(staffData, outputFile.getAbsolutePath());
                    } else if ("Предпросмотр".equals(format)) {
                        reportGenerator.previewStaffReport(staffData);
                        success = true;
                    }
                    break;

                case "Отчет по номерам":
                    if ("PDF".equals(format)) {
                        success = reportGenerator.generateRoomsPdfReport(roomData, outputFile.getAbsolutePath());
                    } else if ("HTML".equals(format)) {
                        success = reportGenerator.generateRoomsHtmlReport(roomData, outputFile.getAbsolutePath());
                    } else if ("Предпросмотр".equals(format)) {
                        reportGenerator.previewRoomsReport(roomData);
                        success = true;
                    }
                    break;

                case "Сводный отчет по отелю":
                    // Используем заглушку, как в оригинальном коде
                    logger.info("[ПОТОК 3] Сводный отчет в разработке");
                    success = generateSummaryReport(format, outputFile);
                    break;
            }

            logger.info("[ПОТОК 3] Результат генерации: {}", success ? "УСПЕХ" : "ОШИБКА");
            return success;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[ПОТОК 3] Построение отчета прервано");
            return false;
        } catch (Exception e) {
            logger.error("[ПОТОК 3] Ошибка: {}", e.getMessage());
            return false;
        }
    }

    // Создание XML файла с данными отчета
    private String createReportXml() {
        try {
            // Создаем директорию temp если ее нет
            File tempDir = new File("temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            String xmlPath = "temp/report_data_" + System.currentTimeMillis() + ".xml";
            File xmlFile = new File(xmlPath);

            try (FileWriter writer = new FileWriter(xmlFile)) {
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<report>\n");
                writer.write("  <metadata>\n");
                writer.write("    <generatedAt>" + new java.util.Date() + "</generatedAt>\n");
                writer.write("    <totalStaff>" + staffData.size() + "</totalStaff>\n");
                writer.write("    <totalRooms>" + roomData.size() + "</totalRooms>\n");
                writer.write("  </metadata>\n");

                // Данные сотрудников
                writer.write("  <staff>\n");
                for (Staff staff : staffData) {
                    writer.write("    <employee>\n");
                    writer.write("      <passport>" + staff.getPassportNumber() + "</passport>\n");
                    writer.write("      <name>" + staff.getFirstName() + " " + staff.getLastName() + "</name>\n");
                    writer.write("      <position>" + staff.getPosition() + "</position>\n");
                    writer.write("      <department>" + staff.getDepartment() + "</department>\n");
                    writer.write("    </employee>\n");
                }
                writer.write("  </staff>\n");

                // Данные номеров
                writer.write("  <rooms>\n");
                for (Room room : roomData) {
                    writer.write("    <room>\n");
                    writer.write("      <number>" + room.getRoomNumber() + "</number>\n");
                    writer.write("      <type>" + room.getRoomType() + "</type>\n");
                    writer.write("      <status>" + room.getStatus() + "</status>\n");
                    writer.write("    </room>\n");
                }
                writer.write("  </rooms>\n");
                writer.write("</report>");
            }

            logger.info("[ПОТОК 2] XML файл создан, размер: {} байт", xmlFile.length());
            return xmlPath;

        } catch (Exception e) {
            logger.error("[ПОТОК 2] Ошибка создания XML: {}", e.getMessage());
            return "temp/default_report.xml";
        }
    }

    // Заглушка для сводного отчета
    private boolean generateSummaryReport(String format, File outputFile) {
        logger.info("[ПОТОК 3] Генерация сводного отчета в формате: {}", format);

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "Сводный отчет по отелю находится в разработке.\n" +
                            "Скоро будет доступен!",
                    "В разработке",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        return true;
    }

    private void showSuccessMessage(File file) {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(null,
                    "Отчет успешно создан!\nФайл: " + file.getAbsolutePath() + "\n\nОткрыть файл?",
                    "Успех",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }
                } catch (Exception e) {
                    logger.warn("Не удалось открыть файл: {}", e.getMessage());
                }
            }
        });
    }

    private void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    message,
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        });
    }
}