package com.hotel.client.view.dashboard_components;

import com.hotel.client.service.*;
import com.hotel.client.util.JasperReportGenerator;
import com.hotel.client.view.*;
import com.hotel.client.config.AppStateManager;
import com.hotel.client.model.Client;
import com.hotel.client.model.Room;
import com.hotel.client.model.Staff;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Обработчик действий для панели администратора
 * Инкапсулирует бизнес-логику взаимодействий
 */
public class DashboardActionHandler {
    private static final Logger logger = LogManager.getLogger(DashboardActionHandler.class);

    private final HotelAdminDashboard dashboard;
    private final ClientService clientService;
    private final RoomService roomService;
    private final StaffService staffService;
    private final AppStateManager appStateManager;
    private final JasperReportGenerator reportGenerator;

    public DashboardActionHandler(HotelAdminDashboard dashboard,
                                  ClientService clientService,
                                  RoomService roomService,
                                  StaffService staffService) {
        this.dashboard = dashboard;
        this.clientService = clientService;
        this.roomService = roomService;
        this.staffService = staffService;
        this.appStateManager = AppStateManager.getInstance();
        this.reportGenerator = new JasperReportGenerator();
    }

    /**
     * Показать форму списка клиентов
     */
    public void showClientsList() {
        SwingUtilities.invokeLater(() -> {
            ClientsListForm clientsListForm = new ClientsListForm(dashboard);
            clientsListForm.setVisible(true);
        });
    }

    /**
     * Показать форму списка сотрудников
     */
    public void showStaffList() {
        SwingUtilities.invokeLater(() -> {
            StaffListForm staffListForm = new StaffListForm(dashboard);
            staffListForm.setVisible(true);
        });
    }

    /**
     * Показать форму списка номеров
     */
    public void showRoomsList() {
        SwingUtilities.invokeLater(() -> {
            RoomsListForm roomsListForm = new RoomsListForm(dashboard);
            roomsListForm.setVisible(true);
        });
    }

    /**
     * Показать историю бронирований
     */
    public void showBookingHistory() {
        SwingUtilities.invokeLater(() -> {
            BookingHistoryForm bookingHistoryForm = new BookingHistoryForm(dashboard);
            bookingHistoryForm.setVisible(true);
        });
    }

    /**
     * Показать форму заселения клиента
     */
    public void showCheckInForm() {
        SwingUtilities.invokeLater(() -> {
            String currentDate = dashboard.getDateFormat().format(dashboard.getCurrentDate());
            CheckInForm checkInForm = new CheckInForm(dashboard, currentDate);
            checkInForm.setVisible(true);
        });
    }

    /**
     * Показать форму добавления сотрудника
     */
    public void showAddStaffForm() {
        SwingUtilities.invokeLater(() -> {
            AddStaffForm addStaffForm = new AddStaffForm(dashboard);
            addStaffForm.setVisible(true);
        });
    }

    /**
     * Выселить клиента
     */
    public void checkOutClient() {
        String passport = JOptionPane.showInputDialog(dashboard,
                "Введите паспорт клиента для выселения:", "Выселение клиента", JOptionPane.QUESTION_MESSAGE);

        if (passport != null && !passport.trim().isEmpty()) {
            // Здесь будет логика выселения клиента
            JOptionPane.showMessageDialog(dashboard,
                    "Функция выселения клиента по паспорту в разработке\nПаспорт: " + passport,
                    "Выселение клиента", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Уволить сотрудника
     */
    public void dismissStaff() {
        String passport = JOptionPane.showInputDialog(dashboard,
                "Введите паспорт сотрудника для увольнения:", "Увольнение сотрудника", JOptionPane.QUESTION_MESSAGE);

        if (passport != null && !passport.trim().isEmpty()) {
            staffService.dismissStaff();
            JOptionPane.showMessageDialog(dashboard,
                    "Функция увольнения сотрудника по паспорту в разработке\nПаспорт: " + passport,
                    "Увольнение сотрудника", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Загрузить отчет
     */
    public void loadReport() {
        try {
            // Создаем пустую форму отчета и сразу вызываем загрузку файла
            ReportForm reportForm = new ReportForm(dashboard,
                    "Выберите файл отчета для загрузки...\n\n" +
                            "Для загрузки отчета нажмите кнопку 'Загрузить отчет'",
                    "Загрузка отчета");
            reportForm.setVisible(true);

        } catch (Exception e) {
            logger.error("Ошибка загрузки отчета: {}", e.getMessage());
            JOptionPane.showMessageDialog(dashboard,
                    "Ошибка загрузки отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Переход на следующий день
     */
    public void advanceDate() {
        try {
            Date currentDate = dashboard.getCurrentDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date newDate = calendar.getTime();

            String newDateStr = dashboard.getDateFormat().format(newDate);
            boolean success = ApiService.getInstance().advanceDate(newDateStr);

            if (success) {
                dashboard.setCurrentDate(newDate);
                dashboard.saveCurrentDateToState();
                dashboard.refreshAllWidgets();

                JOptionPane.showMessageDialog(dashboard,
                        "Дата обновлена: " + newDateStr + "\n" +
                                "Проверена занятость номеров.",
                        "Дата обновлена", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dashboard,
                        "Ошибка обновления даты",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(dashboard,
                    "Ошибка: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Сбросить дату на сегодня
     */
    public void resetDateToToday() {
        int result = JOptionPane.showConfirmDialog(dashboard,
                "Сбросить дату на сегодняшнюю?\nЭто обновит все данные.",
                "Сброс даты", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            appStateManager.resetToToday();
            dashboard.loadCurrentDateFromState();
            dashboard.refreshAllWidgets();
            JOptionPane.showMessageDialog(dashboard,
                    "Дата сброшена на сегодня: " +
                            dashboard.getDateFormat().format(dashboard.getCurrentDate()),
                    "Дата сброшена", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Очистка данных клиентов
     */
    public void clearClientsData() {
        int result = JOptionPane.showConfirmDialog(dashboard,
                "Очистить всех клиентов?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = clientService.clearClientData();
            if (success) {
                JOptionPane.showMessageDialog(dashboard, "Данные о клиентах удалены!");
                dashboard.refreshAllWidgets();
            } else {
                JOptionPane.showMessageDialog(dashboard, "Ошибка очистки данных клиентов!");
            }
        }
    }

    /**
     * Очистка данных сотрудников
     */
    public void clearStaffData() {
        int result = JOptionPane.showConfirmDialog(dashboard,
                "Очистить всех сотрудников?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = staffService.clearStaffData();
            if (success) {
                JOptionPane.showMessageDialog(dashboard, "Данные о сотрудниках удалены!");
                dashboard.refreshAllWidgets();
            } else {
                JOptionPane.showMessageDialog(dashboard, "Ошибка очистки данных сотрудников!");
            }
        }
    }

    /**
     * Очистка данных номеров
     */
    public void clearRoomsData() {
        int result = JOptionPane.showConfirmDialog(dashboard,
                "Очистить все номера?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = roomService.clearRoomsData();
            if (success) {
                JOptionPane.showMessageDialog(dashboard, "Данные о номерах удалены!");
                dashboard.refreshAllWidgets();
            } else {
                JOptionPane.showMessageDialog(dashboard, "Ошибка очистки данных!");
            }
        }
    }

    /**
     * Очистка всех данных
     */
    public void clearAllData() {
        int result = JOptionPane.showConfirmDialog(dashboard,
                "Вы уверены что хотите очистить ВСЕ данные?\nЭто действие нельзя отменить!",
                "Подтверждение очистки",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean success1 = roomService.clearRoomsData();
            boolean success2 = staffService.clearStaffData();
            boolean success3 = clientService.clearClientData();

            if (success1 && success2 && success3) {
                JOptionPane.showMessageDialog(dashboard, "Все данные успешно удалены!");
                dashboard.refreshAllWidgets();
            } else {
                JOptionPane.showMessageDialog(dashboard, "Ошибка полной очистки данных!");
            }
        }
    }

    /**
     * Выход из приложения
     */
    public void logout() {
        int result = JOptionPane.showConfirmDialog(dashboard,
                "Вы уверены, что хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * Генерация отчета с выбором типа и формата
     */
    public void generateReport() {
        try {
            logger.info("🔄 Начало генерации отчета...");

            // Создаем диалоговое окно для выбора параметров отчета
            ReportGenerationDialog dialog = new ReportGenerationDialog(dashboard, this);
            dialog.setVisible(true);

        } catch (Exception e) {
            logger.error("❌ Ошибка генерации отчета: {}", e.getMessage());
            JOptionPane.showMessageDialog(dashboard,
                    "Ошибка генерации отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Внутренний метод для генерации отчета (вызывается из диалога)
     */
    public void executeReportGeneration(String reportType, String format, File outputFile) {
        try {
            switch (reportType) {
                case "Отчет по сотрудникам" -> generateStaffReport(format, outputFile);
                case "Отчет по номерам" -> generateRoomsReport(format, outputFile);
                case "Сводный отчет по отелю" -> generateSummaryReport(format, outputFile);
                default -> throw new IllegalArgumentException("Неизвестный тип отчета: " + reportType);
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка генерации отчета: {}", e.getMessage());
            JOptionPane.showMessageDialog(dashboard,
                    "Ошибка создания отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Генерация отчета по сотрудникам
     */
    private void generateStaffReport(String format, File outputFile) {
        try {
            List<Staff> staffList = staffService.getAllStaff();

            if (staffList.isEmpty()) {
                JOptionPane.showMessageDialog(dashboard,
                        "Нет данных о сотрудниках для генерации отчета",
                        "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean success = false;

            switch (format) {
                case "PDF" -> {
                    success = reportGenerator.generateStaffPdfReport(staffList, outputFile.getAbsolutePath());
                    if (success) {
                        showSuccessMessage("PDF отчет по сотрудникам успешно создан!", outputFile);
                    }
                }
                case "HTML" -> {
                    success = reportGenerator.generateStaffHtmlReport(staffList, outputFile.getAbsolutePath());
                    if (success) {
                        showSuccessMessage("HTML отчет по сотрудникам успешно создан!", outputFile);
                    }
                }
                case "Предпросмотр" -> {
                    reportGenerator.previewStaffReport(staffList);
                    logger.info("✅ Предпросмотр отчета по сотрудникам открыт");
                    return;
                }
                default -> {
                    JOptionPane.showMessageDialog(dashboard,
                            "Неизвестный формат: " + format,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (!success) {
                throw new Exception("Не удалось создать отчет в формате " + format);
            }

        } catch (Exception e) {
            logger.error("❌ Ошибка генерации отчета по сотрудникам: {}", e.getMessage());
            JOptionPane.showMessageDialog(dashboard,
                    "Ошибка создания отчета по сотрудникам: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Генерация отчета по номерам
     */
    private void generateRoomsReport(String format, File outputFile) {
        try {
            List<Room> rooms = roomService.getAllRooms();

            if (rooms.isEmpty()) {
                JOptionPane.showMessageDialog(dashboard,
                        "Нет данных о номерах для генерации отчета",
                        "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean success = false;

            switch (format) {
                case "PDF" -> {
                    success = reportGenerator.generateRoomsPdfReport(rooms, outputFile.getAbsolutePath());
                    if (success) {
                        showSuccessMessage("PDF отчет по номерам успешно создан!", outputFile);
                    }
                }
                case "HTML" -> {
                    success = reportGenerator.generateRoomsHtmlReport(rooms, outputFile.getAbsolutePath());
                    if (success) {
                        showSuccessMessage("HTML отчет по номерам успешно создан!", outputFile);
                    }
                }
                case "Предпросмотр" -> {
                    reportGenerator.previewRoomsReport(rooms);
                    logger.info("✅ Предпросмотр отчета по номерам открыт");
                    return;
                }
                default -> {
                    JOptionPane.showMessageDialog(dashboard,
                            "Неизвестный формат: " + format,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (!success) {
                throw new Exception("Не удалось создать отчет в формате " + format);
            }

        } catch (Exception e) {
            logger.error("❌ Ошибка генерации отчета по номерам: {}", e.getMessage());
            JOptionPane.showMessageDialog(dashboard,
                    "Ошибка создания отчета по номерам: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Генерация сводного отчета (в разработке)
     */
    private void generateSummaryReport(String format, File outputFile) {
        JOptionPane.showMessageDialog(dashboard,
                "Сводный отчет по отелю находится в разработке.\n" +
                        "Скоро будет доступен!",
                "В разработке",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Генерация имени файла на основе типа отчета
     */
    public String generateDefaultFileName(String reportType, String format) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String extension = format.equals("HTML") ? ".html" : ".pdf";

        return switch (reportType) {
            case "Отчет по сотрудникам" -> "staff_report_" + timestamp + extension;
            case "Отчет по номерам" -> "rooms_report_" + timestamp + extension;
            case "Сводный отчет по отелю" -> "hotel_summary_" + timestamp + extension;
            default -> "report_" + timestamp + extension;
        };
    }

    /**
     * Показывает сообщение об успешной генерации и предлагает открыть файл
     */
    private void showSuccessMessage(String message, File file) {
        int result = JOptionPane.showConfirmDialog(dashboard,
                message + "\nФайл: " + file.getAbsolutePath() + "\n\nОткрыть файл?",
                "Успех",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            openFile(file);
        }
    }

    /**
     * Открытие файла в системном просмотрщике
     */
    private void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(dashboard,
                        "Не удалось открыть файл автоматически.\n" +
                                "Файл сохранен по пути: " + file.getAbsolutePath(),
                        "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            logger.warn("Не удалось открыть файл: {}", e.getMessage());
            JOptionPane.showMessageDialog(dashboard,
                    "Не удалось открыть файл: " + e.getMessage() + "\n" +
                            "Файл сохранен по пути: " + file.getAbsolutePath(),
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}

/**
 * Диалоговое окно для генерации отчетов
 */
class ReportGenerationDialog extends JDialog {
    private final DashboardActionHandler handler;
    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> formatCombo;
    private JTextField filePathField;
    private JButton browseButton;
    private JButton generateButton;
    private JButton cancelButton;

    public ReportGenerationDialog(Frame parent, DashboardActionHandler handler) {
        super(parent, "Генерация отчета", true);
        this.handler = handler;
        initializeComponents();
        setupLayout();
        setupListeners();
    }

    private void initializeComponents() {
        // Типы отчетов
        String[] reportTypes = {"Отчет по сотрудникам", "Отчет по номерам", "Сводный отчет по отелю"};
        reportTypeCombo = new JComboBox<>(reportTypes);

        // Форматы
        String[] formats = {"PDF", "HTML", "Предпросмотр"};
        formatCombo = new JComboBox<>(formats);

        // Поле для пути файла
        filePathField = new JTextField(30);
        filePathField.setEditable(false);

        // Кнопки
        browseButton = new JButton("Обзор...");
        generateButton = new JButton("Сгенерировать");
        generateButton.setBackground(new Color(46, 204, 113));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cancelButton = new JButton("Отмена");
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Обновляем путь файла при изменении выбора
        reportTypeCombo.addActionListener(e -> updateFilePath());
        formatCombo.addActionListener(e -> updateFilePath());
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 250);
        setLocationRelativeTo(getParent());

        // Панель содержимого
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Тип отчета
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Тип отчета:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        contentPanel.add(reportTypeCombo, gbc);

        // Формат отчета
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Формат:"), gbc);

        gbc.gridx = 1;
        contentPanel.add(formatCombo, gbc);

        // Сохранить в
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Сохранить в:"), gbc);

        gbc.gridx = 1;
        contentPanel.add(filePathField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(browseButton, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(cancelButton);
        buttonPanel.add(generateButton);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Инициализируем путь файла
        updateFilePath();
    }

    private void setupListeners() {
        browseButton.addActionListener(e -> browseForFile());
        generateButton.addActionListener(e -> generateReport());
        cancelButton.addActionListener(e -> dispose());

        // Закрытие по ESC
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void updateFilePath() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String format = (String) formatCombo.getSelectedItem();

        if (format.equals("Предпросмотр")) {
            filePathField.setText("(предпросмотр в окне программы)");
            filePathField.setEnabled(false);
            browseButton.setEnabled(false);
        } else {
            String fileName = handler.generateDefaultFileName(reportType, format);
            filePathField.setText("reports/" + fileName);
            filePathField.setEnabled(true);
            browseButton.setEnabled(true);
        }
    }

    private void browseForFile() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String format = (String) formatCombo.getSelectedItem();
        String extension = format.equals("HTML") ? ".html" : ".pdf";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранение отчета");
        fileChooser.setSelectedFile(new File(handler.generateDefaultFileName(reportType, format)));

        if (format.equals("HTML")) {
            fileChooser.setFileFilter(new FileNameExtensionFilter("HTML файлы (*.html)", "html"));
        } else {
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF файлы (*.pdf)", "pdf"));
        }

        // Создаем директорию reports если не существует
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        fileChooser.setCurrentDirectory(reportsDir);

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            // Добавляем расширение если его нет
            if (!filePath.toLowerCase().endsWith(extension.toLowerCase())) {
                selectedFile = new File(filePath + extension);
            }
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String format = (String) formatCombo.getSelectedItem();

        if (format.equals("Предпросмотр")) {
            handler.executeReportGeneration(reportType, format, null);
            dispose();
            return;
        }

        String filePath = filePathField.getText();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Пожалуйста, выберите место для сохранения файла",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        File outputFile = new File(filePath);

        // Проверяем директорию
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Подтверждение перезаписи
        if (outputFile.exists()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Файл уже существует. Перезаписать?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        handler.executeReportGeneration(reportType, format, outputFile);
        dispose();
    }
}