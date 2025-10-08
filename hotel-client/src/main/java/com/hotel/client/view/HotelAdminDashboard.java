package com.hotel.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.hotel.client.service.DatabaseManager;
import com.hotel.client.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главное окно панели администратора отеля с управлением датой
 */
public class HotelAdminDashboard extends JFrame {
    private DatabaseManager dbManager;
    private JLabel currentDateLabel;
    private Date currentDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public HotelAdminDashboard() {
        // Инициализируем менеджер базы данных
        dbManager = DatabaseManager.getInstance();

        // Устанавливаем текущую дату
        currentDate = new Date();

        // Проверяем доступность сервера
        checkServerConnection();

        // Основные настройки окна
        setTitle("Панель администратора отеля - Управление номерами");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        createHeader();
        createNavigation();
        createMainContent();

        setVisible(true);
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Левая часть - название и дата
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftHeader.setOpaque(false);

        JLabel appTitle = new JLabel("Панель администратора отеля");
        appTitle.setFont(new Font("Arial", Font.BOLD, 18));

        // Метка с текущей датой
        currentDateLabel = new JLabel("Сегодня: " + dateFormat.format(currentDate));
        currentDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentDateLabel.setForeground(new Color(0, 100, 0));

        leftHeader.add(appTitle);
        leftHeader.add(Box.createHorizontalStrut(20));
        leftHeader.add(currentDateLabel);

        // Правая часть - информация пользователя и кнопки
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setOpaque(false);

        JLabel userLabel = new JLabel("Администратор: Игорь Секирин");

        // Кнопка продления даты
        JButton advanceDateButton = new JButton("Следующий день");
        advanceDateButton.setBackground(new Color(70, 130, 180));
        advanceDateButton.setForeground(Color.WHITE);
        advanceDateButton.addActionListener(e -> advanceDate());

        JButton logoutButton = new JButton("Выход");
        logoutButton.addActionListener(e -> System.exit(0));

        rightHeader.add(userLabel);
        rightHeader.add(Box.createHorizontalStrut(10));
        rightHeader.add(advanceDateButton);
        rightHeader.add(Box.createHorizontalStrut(10));
        rightHeader.add(logoutButton);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Продвигает дату на один день вперед
     */
    private void advanceDate() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            currentDate = calendar.getTime();

            String newDate = dateFormat.format(currentDate);

            // Обновляем дату на сервере и проверяем занятость номеров
            boolean success = dbManager.advanceDate(newDate);

            if (success) {
                currentDateLabel.setText("Сегодня: " + newDate);
                JOptionPane.showMessageDialog(this,
                        "✅ Дата обновлена: " + newDate + "\n" +
                                "Проверена занятость номеров.",
                        "Дата обновлена", JOptionPane.INFORMATION_MESSAGE);

                // Обновляем виджеты с актуальными данными
                updateRoomWidgets();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Ошибка обновления даты",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ошибка: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createNavigation() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(new Color(245, 245, 245));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        navPanel.setPreferredSize(new Dimension(200, 0));

        Font navFont = new Font("Arial", Font.PLAIN, 14);
        Dimension navButtonSize = new Dimension(180, 40);

        JButton homeButton = createNavButton("Главная", navFont, navButtonSize);
        JButton clientsButton = createNavButton("Клиенты", navFont, navButtonSize);
        JButton viewClientsButton = createNavButton("Список клиентов", navFont, navButtonSize);
        JButton staffButton = createNavButton("Персонал", navFont, navButtonSize);
        JButton viewStaffButton = createNavButton("Список сотрудников", navFont, navButtonSize);
        JButton roomsButton = createNavButton("Номера", navFont, navButtonSize);
        JButton viewRoomsButton = createNavButton("Список номеров", navFont, navButtonSize);
        JButton reportsButton = createNavButton("Отчеты", navFont, navButtonSize);

        // Добавляем обработчики для новых кнопок
        viewRoomsButton.addActionListener(e -> {
            RoomsListForm roomsListForm = new RoomsListForm(this);
            roomsListForm.setVisible(true);
        });

        viewClientsButton.addActionListener(e -> {
            ClientsListForm clientsListForm = new ClientsListForm(this);
            clientsListForm.setVisible(true);
        });

        viewStaffButton.addActionListener(e -> {
            StaffListForm staffListForm = new StaffListForm(this);
            staffListForm.setVisible(true);
        });

        navPanel.add(homeButton);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(clientsButton);
        navPanel.add(viewClientsButton);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(staffButton);
        navPanel.add(viewStaffButton);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(roomsButton);
        navPanel.add(viewRoomsButton);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(reportsButton);
        navPanel.add(Box.createVerticalGlue());

        add(navPanel, BorderLayout.WEST);
    }

    private void createMainContent() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createRoomStatusWidget());
        mainPanel.add(createRoomTypesWidget());
        mainPanel.add(createQuickActionsWidget());
        mainPanel.add(createTodayEventsWidget());

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Виджет статуса номеров
     */
    private JPanel createRoomStatusWidget() {
        JPanel panel = createWidgetPanel("Статус номеров на сегодня");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Получаем актуальные данные о номерах
        List<Room> allRooms = dbManager.getAllRooms();
        List<Room> freeRooms = dbManager.getFreeRooms();
        List<Room> occupiedRooms = allRooms.stream()
                .filter(room -> "occupied".equals(room.getStatus()))
                .collect(Collectors.toList());

        JPanel chartPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartPanel.setOpaque(false);

        JPanel occupiedPanel = createStatusPanel("Занято",
                occupiedRooms.size() + " номеров", Color.RED);
        JPanel freePanel = createStatusPanel("Свободно",
                freeRooms.size() + " номеров", Color.GREEN);

        chartPanel.add(occupiedPanel);
        chartPanel.add(freePanel);

        JPanel detailsPanel = new JPanel(new FlowLayout());
        detailsPanel.setOpaque(false);
        detailsPanel.add(new JLabel("Всего номеров: " + allRooms.size()));

        JButton detailsButton = new JButton("Подробнее");
        detailsButton.addActionListener(e -> {
            RoomsListForm roomsListForm = new RoomsListForm(this);
            roomsListForm.setVisible(true);
        });

        panel.add(chartPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(detailsPanel);
        panel.add(detailsButton);

        return panel;
    }

    /**
     * Виджет типов номеров
     */
    private JPanel createRoomTypesWidget() {
        JPanel panel = createWidgetPanel("Типы номеров");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Статистика по типам номеров
        List<Room> allRooms = dbManager.getAllRooms();

        long economyRooms = allRooms.stream().filter(r -> "Эконом".equals(r.getRoomType())).count();
        long standardRooms = allRooms.stream().filter(r -> "Стандарт".equals(r.getRoomType())).count();
        long businessRooms = allRooms.stream().filter(r -> "Бизнес".equals(r.getRoomType())).count();
        long luxuryRooms = allRooms.stream().filter(r -> "Люкс".equals(r.getRoomType())).count();

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setOpaque(false);

        statsPanel.add(createStatsPanel("Эконом", economyRooms + " номеров", new Color(100, 149, 237)));
        statsPanel.add(createStatsPanel("Стандарт", standardRooms + " номеров", new Color(60, 179, 113)));
        statsPanel.add(createStatsPanel("Бизнес", businessRooms + " номеров", new Color(255, 165, 0)));
        statsPanel.add(createStatsPanel("Люкс", luxuryRooms + " номеров", new Color(186, 85, 211)));

        panel.add(statsPanel);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    /**
     * Виджет событий на сегодня
     */
    private JPanel createTodayEventsWidget() {
        JPanel panel = createWidgetPanel("События на сегодня");
        panel.setLayout(new BorderLayout());

        String today = dateFormat.format(currentDate);

        // Здесь можно добавить логику для получения событий на сегодня
        JTextArea eventsArea = new JTextArea();
        eventsArea.setEditable(false);
        eventsArea.setFont(new Font("Arial", Font.PLAIN, 12));
        eventsArea.setText("Дата: " + today + "\n\n" +
                "Заезды: проверяется автоматически\n" +
                "Выезды: проверяется автоматически\n\n" +
                "Нажмите 'Следующий день' для обновления");

        JScrollPane scrollPane = new JScrollPane(eventsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Обновляет виджеты с информацией о номерах
     */
    private void updateRoomWidgets() {
        // Можно добавить логику для обновления виджетов
        // при изменении даты
        System.out.println("🔄 Обновление данных после смены даты");
    }

    // Вспомогательные методы остаются без изменений
    private JButton createNavButton(String text, Font font, Dimension size) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setMaximumSize(size);
        button.setPreferredSize(size);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    private JPanel createWidgetPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12)
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JPanel createStatusPanel(String title, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        panel.setBorder(BorderFactory.createLineBorder(color, 2));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));

        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private JPanel createStatsPanel(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        return button;
    }

    private void checkServerConnection() {
        boolean serverAvailable = dbManager.isServerAvailable();
        if (!serverAvailable) {
            JOptionPane.showMessageDialog(this,
                    "Сервер недоступен!\n\nУбедитесь что:\n" +
                            "1. Сервер запущен на localhost:8080\n" +
                            "2. Приложение имеет доступ к сети\n\n" +
                            "Приложение будет работать в ограниченном режиме.",
                    "Внимание",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // Quick Actions Widget (обновленный)
    private JPanel createQuickActionsWidget() {
        JPanel panel = createWidgetPanel("Быстрые действия");
        panel.setLayout(new GridLayout(4, 1, 10, 10));

        JButton checkinButton = createActionButton("Заселить клиента", new Color(70, 130, 180));
        JButton addStaffButton = createActionButton("Добавить сотрудника", new Color(210, 105, 30));
        JButton manageRoomsButton = createActionButton("Управление номерами", new Color(60, 179, 113));
        JButton advanceDateButton = createActionButton("Следующий день", new Color(147, 112, 219));

        checkinButton.addActionListener(e -> {
            CheckInForm checkInForm = new CheckInForm(this, dateFormat.format(currentDate));
            checkInForm.setVisible(true);
        });

        addStaffButton.addActionListener(e -> {
            AddStaffForm addStaffForm = new AddStaffForm(this);
            addStaffForm.setVisible(true);
        });

        manageRoomsButton.addActionListener(e -> {
            RoomsListForm roomsListForm = new RoomsListForm(this);
            roomsListForm.setVisible(true);
        });

        advanceDateButton.addActionListener(e -> advanceDate());

        panel.add(checkinButton);
        panel.add(addStaffButton);
        panel.add(manageRoomsButton);
        panel.add(advanceDateButton);

        return panel;
    }
}