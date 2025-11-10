package com.hotel.client.view.dashboard_components;

import com.hotel.client.model.Client;
import com.hotel.client.model.Room;
import com.hotel.client.service.ClientService;
import com.hotel.client.service.RoomService;
import com.hotel.client.view.HotelAdminDashboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Виджет календаря событий (правый верхний угол)
 * Содержит информацию о предстоящих заселениях/выселениях
 */
//TODO: на текущий момент не имеет рабочей реализации
public class CalendarWidget extends BaseWidget {
    private static final Logger logger = LogManager.getLogger(CalendarWidget.class);

    private final ClientService clientService;
    private final RoomService roomService;
    private JPanel calendarPanel;

    public CalendarWidget(HotelAdminDashboard dashboard, ClientService clientService, RoomService roomService) {
        super(dashboard, "Календарь событий");
        this.clientService = clientService;
        this.roomService = roomService;
        initializeWidget();
        refreshData();
    }

    private void initializeWidget() {
        setLayout(new BorderLayout());
        calendarPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(calendarPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        try {
            calendarPanel.removeAll();
            calendarPanel.setLayout(new GridLayout(0, 7, 2, 2));
            calendarPanel.setBackground(Color.WHITE);
            calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Заголовки дней недели
            String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            for (String day : days) {
                JLabel dayLabel = new JLabel(day, JLabel.CENTER);
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                dayLabel.setForeground(new Color(100, 100, 100));
                dayLabel.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
                calendarPanel.add(dayLabel);
            }

            // Заполняем календарь днями
            Calendar cal = Calendar.getInstance();
            cal.setTime(dashboard.getCurrentDate());
            cal.set(Calendar.DAY_OF_MONTH, 1);

            int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Пустые ячейки до первого дня месяца
            for (int i = 1; i < firstDayOfWeek; i++) {
                calendarPanel.add(new JLabel(""));
            }

            // Получаем события для подсветки
            List<Client> clients = clientService.getAllClients();
            List<Room> rooms = roomService.getAllRooms();

            // Дни месяца
            for (int day = 1; day <= daysInMonth; day++) {
                JLabel dayLabel = new JLabel(String.valueOf(day), JLabel.CENTER);
                dayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                dayLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
                dayLabel.setOpaque(true);

                // Проверяем события на этот день
                String dateStr = formatDateForDay(cal, day);
                if (hasCheckInEvents(clients, dateStr)) {
                    dayLabel.setBackground(new Color(230, 255, 230)); // Зеленый для заездов
                    dayLabel.setToolTipText("Заезды: " + countCheckIns(clients, dateStr));
                } else if (hasCheckOutEvents(clients, dateStr)) {
                    dayLabel.setBackground(new Color(255, 230, 230)); // Красный для выездов
                    dayLabel.setToolTipText("Выезды: " + countCheckOuts(clients, dateStr));
                } else {
                    dayLabel.setBackground(Color.WHITE);
                }

                // Подсвечиваем текущий день
                if (isToday(cal, day)) {
                    dayLabel.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
                    dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                }

                calendarPanel.add(dayLabel);
            }

            calendarPanel.revalidate();
            calendarPanel.repaint();

        } catch (Exception e) {
            logger.error("Ошибка обновления календаря: {}", e.getMessage());
        }
    }

    private String formatDateForDay(Calendar cal, int day) {
        Calendar dateCal = (Calendar) cal.clone();
        dateCal.set(Calendar.DAY_OF_MONTH, day);
        return new SimpleDateFormat("yyyy-MM-dd").format(dateCal.getTime());
    }

    private boolean isToday(Calendar monthCal, int day) {
        Calendar today = Calendar.getInstance();
        Calendar dateCal = (Calendar) monthCal.clone();
        dateCal.set(Calendar.DAY_OF_MONTH, day);

        return dateCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                dateCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                dateCal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    private boolean hasCheckInEvents(List<Client> clients, String date) {
        return clients.stream().anyMatch(client ->
                date.equals(client.getCheckInDate()));
    }

    private boolean hasCheckOutEvents(List<Client> clients, String date) {
        return clients.stream().anyMatch(client ->
                date.equals(client.getCheckOutDate()));
    }

    private int countCheckIns(List<Client> clients, String date) {
        return (int) clients.stream()
                .filter(client -> date.equals(client.getCheckInDate()))
                .count();
    }

    private int countCheckOuts(List<Client> clients, String date) {
        return (int) clients.stream()
                .filter(client -> date.equals(client.getCheckOutDate()))
                .count();
    }
}