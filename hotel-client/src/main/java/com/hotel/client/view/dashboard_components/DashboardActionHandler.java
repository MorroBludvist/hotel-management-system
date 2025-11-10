package com.hotel.client.view.dashboard_components;

import com.hotel.client.service.*;
import com.hotel.client.view.*;
import com.hotel.client.config.AppStateManager;
import com.hotel.client.model.Client;
import com.hotel.client.model.Room;
import com.hotel.client.model.Staff;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
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

    public DashboardActionHandler(HotelAdminDashboard dashboard,
                                  ClientService clientService,
                                  RoomService roomService,
                                  StaffService staffService) {
        this.dashboard = dashboard;
        this.clientService = clientService;
        this.roomService = roomService;
        this.staffService = staffService;
        this.appStateManager = AppStateManager.getInstance();
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
            // Здесь будет логика увольнения сотрудника
            JOptionPane.showMessageDialog(dashboard,
                    "Функция увольнения сотрудника по паспорту в разработке\nПаспорт: " + passport,
                    "Увольнение сотрудника", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Сгенерировать отчет
     */
    public void generateReport() {
        try {
            // Получаем данные для отчета
            List<Client> clients = clientService.getAllClients();
            List<Room> rooms = roomService.getAllRooms();
            List<Staff> staffList = staffService.getAllStaff();

            // Анализируем данные
            String report = buildReport(clients, rooms, staffList);

            // Создаем и показываем форму отчета
            ReportForm reportForm = new ReportForm(dashboard, report, "Отчет по отелю");
            reportForm.setVisible(true);

        } catch (Exception e) {
            logger.error("Ошибка генерации отчета: {}", e.getMessage());
            JOptionPane.showMessageDialog(dashboard,
                    "Ошибка генерации отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
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
     * Построение отчета
     */
    private String buildReport(List<Client> clients, List<Room> rooms, List<Staff> staffList) {
        StringBuilder report = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        // Заголовок отчета
        report.append("=").append("=".repeat(50)).append("\n");
        report.append("               ОТЧЕТ ПО ОТЕЛЮ\n");
        report.append("=").append("=".repeat(50)).append("\n");
        report.append("Дата формирования: ").append(dateFormat.format(new Date())).append("\n\n");

        // Статистика по клиентам
        report.append("СТАТИСТИКА КЛИЕНТОВ:\n");
        report.append("-".repeat(30)).append("\n");
        long activeClients = clients.stream()
                .filter(c -> c.getCheckInDate() != null && !c.getCheckInDate().isEmpty())
                .count();
        long checkedOutClients = clients.stream()
                .filter(c -> c.getCheckOutDate() != null && !c.getCheckOutDate().isEmpty())
                .count();

        report.append("Всего клиентов: ").append(clients.size()).append("\n");
        report.append("Активных клиентов: ").append(activeClients).append("\n");
        report.append("Выселенных клиентов: ").append(checkedOutClients).append("\n\n");

        // Статистика по номерам
        report.append("СТАТИСТИКА НОМЕРОВ:\n");
        report.append("-".repeat(30)).append("\n");
        long totalRooms = rooms.size();
        long freeRooms = rooms.stream().filter(r -> "free".equals(r.getStatus())).count();
        long occupiedRooms = rooms.stream().filter(r -> "occupied".equals(r.getStatus())).count();

        // Статистика по типам номеров
        Map<String, Long> roomsByType = rooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomType, Collectors.counting()));

        report.append("Всего номеров: ").append(totalRooms).append("\n");
        report.append("Свободных номеров: ").append(freeRooms).append("\n");
        report.append("Занятых номеров: ").append(occupiedRooms).append("\n");
        report.append("Загрузка отеля: ").append(String.format("%.1f%%",
                totalRooms > 0 ? (occupiedRooms * 100.0 / totalRooms) : 0)).append("\n\n");

        report.append("Распределение по типам:\n");
        roomsByType.forEach((type, count) -> {
            long occupiedByType = rooms.stream()
                    .filter(r -> type.equals(r.getRoomType()) && "occupied".equals(r.getStatus()))
                    .count();
            report.append("  ").append(type).append(": ").append(count)
                    .append(" (занято: ").append(occupiedByType).append(")\n");
        });
        report.append("\n");

        // Статистика по персоналу
        report.append("СТАТИСТИКА ПЕРСОНАЛА:\n");
        report.append("-".repeat(30)).append("\n");

        Map<String, Long> staffByDepartment = staffList.stream()
                .collect(Collectors.groupingBy(Staff::getDepartment, Collectors.counting()));

        double totalSalary = staffList.stream().mapToDouble(Staff::getSalary).sum();

        report.append("Всего сотрудников: ").append(staffList.size()).append("\n");
        report.append("Общий фонд зарплат: ").append(String.format("%,.0f руб.", totalSalary)).append("\n");
        report.append("Средняя зарплата: ").append(String.format("%,.0f руб.",
                staffList.isEmpty() ? 0 : totalSalary / staffList.size())).append("\n\n");

        report.append("Распределение по отделам:\n");
        staffByDepartment.forEach((dept, count) -> {
            double deptSalary = staffList.stream()
                    .filter(s -> dept.equals(s.getDepartment()))
                    .mapToDouble(Staff::getSalary)
                    .sum();
            report.append("  ").append(dept).append(": ").append(count)
                    .append(" чел., зарплата: ").append(String.format("%,.0f руб.", deptSalary)).append("\n");
        });
        report.append("\n");

        // Финансовая сводка (упрощенная)
        report.append("ФИНАНСОВАЯ СВОДКА:\n");
        report.append("-".repeat(30)).append("\n");

        // Расчеты доходов
        double estimatedDailyIncome = occupiedRooms * 2500; // пример: 2500 руб./номер/день
        double monthlyExpenses = totalSalary; // только зарплаты для примера
        double estimatedMonthlyIncome = estimatedDailyIncome * 30;

        report.append("Примерный дневной доход: ").append(String.format("%,.0f руб.", estimatedDailyIncome)).append("\n");
        report.append("Примерный месячный доход: ").append(String.format("%,.0f руб.", estimatedMonthlyIncome)).append("\n");
        report.append("Месячные расходы (зарплаты): ").append(String.format("%,.0f руб.", monthlyExpenses)).append("\n");
        report.append("Примерная прибыль: ").append(String.format("%,.0f руб.", estimatedMonthlyIncome - monthlyExpenses)).append("\n\n");

        report.append("=").append("=".repeat(50)).append("\n");
        report.append("               КОНЕЦ ОТЧЕТА\n");
        report.append("=").append("=".repeat(50)).append("\n");

        return report.toString();
    }
}