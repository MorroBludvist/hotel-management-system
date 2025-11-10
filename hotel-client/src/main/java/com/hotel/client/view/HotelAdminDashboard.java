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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.hotel.client.config.AppStateManager;
import com.hotel.client.service.*;
import com.hotel.client.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главное окно панели администратора отеля
 */
public class HotelAdminDashboard extends JFrame {
    private JLabel currentDateLabel;
    private Date currentDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private ApiService apiService;
    private AppStateManager appStateManager;
    private ClientService clientService;
    private RoomService roomService;
    private StaffService staffService;

    private JPanel mainPanel;
    private JPanel roomTypesWidget;
    private JPanel calendarWidget;
    private JPanel quickActionsWidget;
    private JPanel todayEventsWidget;

    private static final Logger logger = LogManager.getLogger(HotelAdminDashboard.class);

    public HotelAdminDashboard() {
        apiService = ApiService.getInstance();
        appStateManager = AppStateManager.getInstance();
        this.clientService = new ClientService(apiService);
        this.roomService = new RoomService(apiService);
        this.staffService = new StaffService(apiService);

        // Загружаем дату из состояния приложения
        loadCurrentDateFromState();
        checkServerConnection();

        setTitle("Панель администратора отеля");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        createHeader();
        createNavigation();
        createMainContent();

        setVisible(true);
    }

    /**
     * Загружает текущую дату из состояния приложения
     */
    private void loadCurrentDateFromState() {
        try {
            String savedDate = AppStateManager.getInstance().getCurrentDate();
            currentDate = dateFormat.parse(savedDate);
            logger.info("📅 Загружена дата из состояния: {}", savedDate);
        } catch (Exception e) {
            logger.error("❌ Ошибка загрузки даты из состояния: {}", e.getMessage());
            currentDate = new Date(); // Используем текущую дату как запасной вариант
        }
    }

    /**
     * Сохраняет текущую дату в состояние приложения
     */
    private void saveCurrentDateToState() {
        String dateStr = dateFormat.format(currentDate);
        AppStateManager.getInstance().setCurrentDate(dateStr);
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Светлый градиент для верхней панели
                GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 245, 249),
                        getWidth(), getHeight(), new Color(225, 235, 245));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Левая часть - название и дата
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftHeader.setOpaque(false);

        JButton resetDateButton = createHeaderButton("Сбросить дату", new Color(155, 89, 182));
        resetDateButton.addActionListener(e -> resetDateToToday());



        JLabel appTitle = new JLabel("Панель администратора отеля");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(new Color(52, 73, 94));

        currentDateLabel = new JLabel("Сегодня: " + dateFormat.format(currentDate));
        currentDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        currentDateLabel.setForeground(new Color(100, 100, 100));

        leftHeader.add(appTitle);
        leftHeader.add(Box.createHorizontalStrut(20));
        leftHeader.add(currentDateLabel);

        // Правая часть - информация пользователя и кнопки
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setOpaque(false);

        JLabel userLabel = new JLabel("Администратор: Игорь Секирин");
        userLabel.setForeground(new Color(100, 100, 100));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton advanceDateButton = createHeaderButton("Следующий день", new Color(46, 204, 113));
        JButton logoutButton = createHeaderButton("Выход", new Color(231, 76, 60));

        advanceDateButton.addActionListener(e -> advanceDate());
        logoutButton.addActionListener(e -> System.exit(0));

        rightHeader.add(userLabel);
        rightHeader.add(Box.createHorizontalStrut(10));
        rightHeader.add(advanceDateButton);
        rightHeader.add(Box.createHorizontalStrut(10));
        rightHeader.add(logoutButton);

        // Добавляем кнопку в rightHeader
        rightHeader.add(resetDateButton);
        rightHeader.add(Box.createHorizontalStrut(10));
        rightHeader.add(advanceDateButton);
        rightHeader.add(Box.createHorizontalStrut(10));
        rightHeader.add(logoutButton);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JButton createHeaderButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, color.darker(), 0, getHeight(), color.darker().darker());
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, color.brighter(), 0, getHeight(), color);
                } else {
                    gradient = new GradientPaint(0, 0, color, 0, getHeight(), color.darker());
                }

                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void createNavigation() {
        JPanel navPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Светлый фон для левой панели
                GradientPaint gradient = new GradientPaint(0, 0, new Color(245, 248, 250),
                        getWidth(), getHeight(), new Color(235, 242, 248));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        navPanel.setPreferredSize(new Dimension(250, 0));

        // Кнопки управления данными
        JButton viewClientsButton = createNavButton("Список клиентов", new Color(41, 128, 185));
        JButton viewStaffButton = createNavButton("Список сотрудников", new Color(230, 126, 34));
        JButton viewRoomsButton = createNavButton("Список номеров", new Color(39, 174, 96));

        // Функциональные кнопки
        JButton generateReportButton = createNavButton("Сгенерировать отчет", new Color(155, 89, 182));
        JButton loadReportButton = createNavButton("Загрузить отчет", new Color(125, 60, 152));
        JButton checkOutClientButton = createNavButton("Выселить клиента", new Color(231, 76, 60));
        JButton dismissStaffButton = createNavButton("Уволить сотрудника", new Color(192, 57, 43));

        // Очистка данных
        JButton clearAllDataButton = createNavButton("Очистить всю БД", new Color(149, 165, 166));
        JButton clearClientsButton = createNavButton("Очистить клиентов", new Color(149, 165, 166));
        JButton clearStaffButton = createNavButton("Очистить сотрудников", new Color(149, 165, 166));
        JButton clearRoomsButton = createNavButton("Очистить номера", new Color(149, 165, 166));

        // Обработчики
        viewClientsButton.addActionListener(e -> {
            ClientsListForm clientsListForm = new ClientsListForm(this);
            clientsListForm.setVisible(true);
        });

        viewStaffButton.addActionListener(e -> {
            StaffListForm staffListForm = new StaffListForm(this);
            staffListForm.setVisible(true);
        });

        viewRoomsButton.addActionListener(e -> {
            RoomsListForm roomsListForm = new RoomsListForm(this);
            roomsListForm.setVisible(true);
        });

        generateReportButton.addActionListener(e -> generateReport());
        loadReportButton.addActionListener(e -> loadReport());
        checkOutClientButton.addActionListener(e -> checkOutClient());
        dismissStaffButton.addActionListener(e -> dismissStaff());

        clearAllDataButton.addActionListener(e -> clearAllData());
        clearClientsButton.addActionListener(e -> clearClientsData());
        clearStaffButton.addActionListener(e -> clearStaffData());
        clearRoomsButton.addActionListener(e -> clearRoomsData());

        // Добавление компонентов
        navPanel.add(createSectionLabel("Управление данными"));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(viewClientsButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(viewStaffButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(viewRoomsButton);

        navPanel.add(Box.createVerticalStrut(20));
        navPanel.add(createSectionLabel("Операции"));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(checkOutClientButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(dismissStaffButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(generateReportButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(loadReportButton); // Добавляем новую кнопку

        navPanel.add(Box.createVerticalStrut(20));
        navPanel.add(createSectionLabel("Очистка данных"));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(clearClientsButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(clearStaffButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(clearRoomsButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(clearAllDataButton);

        navPanel.add(Box.createVerticalGlue());

        add(navPanel, BorderLayout.WEST);

        JButton viewBookingHistoryButton = createNavButton("История бронирований", new Color(142, 68, 173));

        // Добавить обработчик:
        viewBookingHistoryButton.addActionListener(e -> {
            BookingHistoryForm bookingHistoryForm = new BookingHistoryForm(this);
            bookingHistoryForm.setVisible(true);
        });

        // В код добавления кнопок в navPanel (в раздел "Операции"):
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(viewBookingHistoryButton);
    }

    private JButton createNavButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, baseColor.darker(), 0, getHeight(), baseColor.darker().darker());
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, baseColor.brighter(), 0, getHeight(), baseColor);
                } else {
                    gradient = new GradientPaint(0, 0, baseColor, 0, getHeight(), baseColor.darker());
                }

                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setMaximumSize(new Dimension(220, 40));
        button.setPreferredSize(new Dimension(220, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(100, 100, 100));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void createMainContent() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 242, 245));

        JPanel widgetsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        widgetsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        widgetsPanel.setBackground(new Color(240, 242, 245));
        widgetsPanel.setOpaque(true);

        // Сохраняем ссылки на виджеты для последующего обновления
        roomTypesWidget = createRoomTypesWidget();
        calendarWidget = createCalendarWidget();
        quickActionsWidget = createQuickActionsWidget();
        todayEventsWidget = createTodayEventsWidget();

        widgetsPanel.add(roomTypesWidget);
        widgetsPanel.add(calendarWidget);
        widgetsPanel.add(quickActionsWidget);
        widgetsPanel.add(todayEventsWidget);

        mainPanel.add(widgetsPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }



    /**
     * Виджет типов номеров (левый верхний)
     */
    private JPanel createRoomTypesWidget() {
        JPanel panel = createWidgetPanel("Типы номеров");
        panel.setLayout(new BorderLayout());

        // Получаем актуальные данные о номерах
        List<Room> allRooms = roomService.getAllRooms();
        List<Room> occupiedRooms = allRooms.stream()
                .filter(room -> "occupied".equals(room.getStatus()))
                .collect(Collectors.toList());

        // Статистика по типам номеров
        Map<String, RoomStats> roomStats = calculateRoomStats(allRooms, occupiedRooms);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setOpaque(false);

        // Эконом
        statsPanel.add(createRoomTypePanel("Эконом",
                roomStats.getOrDefault("Эконом", new RoomStats(0, 0)),
                new Color(100, 149, 237)));

        // Стандарт
        statsPanel.add(createRoomTypePanel("Стандарт",
                roomStats.getOrDefault("Стандарт", new RoomStats(0, 0)),
                new Color(60, 179, 113)));

        // Бизнес
        statsPanel.add(createRoomTypePanel("Бизнес",
                roomStats.getOrDefault("Бизнес", new RoomStats(0, 0)),
                new Color(255, 165, 0)));

        // Люкс
        statsPanel.add(createRoomTypePanel("Люкс",
                roomStats.getOrDefault("Люкс", new RoomStats(0, 0)),
                new Color(186, 85, 211)));

        panel.add(statsPanel, BorderLayout.CENTER);

        // Общая статистика
        JPanel totalPanel = new JPanel(new FlowLayout());
        totalPanel.setOpaque(false);
        JLabel totalLabel = new JLabel("Всего номеров: " + allRooms.size() +
                " | Занято: " + occupiedRooms.size());
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        totalLabel.setForeground(new Color(100, 100, 100));
        totalPanel.add(totalLabel);

        panel.add(totalPanel, BorderLayout.SOUTH);

        return panel;
    }

    private Map<String, RoomStats> calculateRoomStats(List<Room> allRooms, List<Room> occupiedRooms) {
        Map<String, RoomStats> stats = new HashMap<>();

        for (Room room : allRooms) {
            String type = room.getRoomType();
            RoomStats current = stats.getOrDefault(type, new RoomStats(0, 0));
            current.total++;
            stats.put(type, current);
        }

        for (Room room : occupiedRooms) {
            String type = room.getRoomType();
            RoomStats current = stats.getOrDefault(type, new RoomStats(0, 0));
            current.occupied++;
            stats.put(type, current);
        }

        return stats;
    }

    private JPanel createRoomTypePanel(String type, RoomStats stats, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(type, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(color.darker());

        JLabel statsLabel = new JLabel(stats.occupied + "/" + stats.total + " занято", JLabel.CENTER);
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(color.darker());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(statsLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Виджет календаря (правый верхний)
     */
    private JPanel createCalendarWidget() {
        JPanel panel = createWidgetPanel("Календарь событий");
        panel.setLayout(new BorderLayout());

        // Создаем календарь на текущий месяц
        JPanel calendarPanel = createCalendarPanel();

        JScrollPane scrollPane = new JScrollPane(calendarPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 7, 2, 2));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовки дней недели
        String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, JLabel.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            dayLabel.setForeground(new Color(100, 100, 100));
            dayLabel.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
            panel.add(dayLabel);
        }

        // Заполняем календарь днями
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Пустые ячейки до первого дня месяца
        for (int i = 1; i < firstDayOfWeek; i++) {
            panel.add(new JLabel(""));
        }

        // Дни месяца
        for (int day = 1; day <= daysInMonth; day++) {
            JLabel dayLabel = new JLabel(String.valueOf(day), JLabel.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            dayLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            dayLabel.setOpaque(true);

            // Проверяем события на этот день
            if (hasCheckInEvents(day)) {
                dayLabel.setBackground(new Color(230, 255, 230)); // Зеленый для заездов
            } else if (hasCheckOutEvents(day)) {
                dayLabel.setBackground(new Color(255, 230, 230)); // Красный для выездов
            } else {
                dayLabel.setBackground(Color.WHITE);
            }

            panel.add(dayLabel);
        }

        return panel;
    }

    private boolean hasCheckInEvents(int day) {
        // Заглушка - здесь будет логика проверки заездов
        // Например: день % 3 == 0 для демонстрации
        return day % 3 == 0;
    }

    private boolean hasCheckOutEvents(int day) {
        // Заглушка - здесь будет логика проверки выездов
        // Например: день % 4 == 0 для демонстрации
        return day % 4 == 0;
    }

    /**
     * Виджет быстрых действий (левый нижний)
     */
    private JPanel createQuickActionsWidget() {
        JPanel panel = createWidgetPanel("Быстрые действия");
        panel.setLayout(new GridLayout(4, 1, 10, 10));

        JButton checkinButton = createActionButton("Заселить клиента", new Color(41, 128, 185));
        JButton addStaffButton = createActionButton("Добавить сотрудника", new Color(230, 126, 34));
        JButton checkOutButton = createActionButton("Выселить клиента", new Color(231, 76, 60));
        JButton dismissStaffButton = createActionButton("Уволить сотрудника", new Color(192, 57, 43));

        checkinButton.addActionListener(e -> {
            CheckInForm checkInForm = new CheckInForm(this, dateFormat.format(currentDate));
            checkInForm.setVisible(true);
        });

        addStaffButton.addActionListener(e -> {
            AddStaffForm addStaffForm = new AddStaffForm(this);
            addStaffForm.setVisible(true);
        });

        checkOutButton.addActionListener(e -> checkOutClient());
        dismissStaffButton.addActionListener(e -> dismissStaff());

        panel.add(checkinButton);
        panel.add(addStaffButton);
        panel.add(checkOutButton);
        panel.add(dismissStaffButton);

        return panel;
    }

    /**
     * Виджет событий на сегодня (правый нижний)
     */
    private JPanel createTodayEventsWidget() {
        JPanel panel = createWidgetPanel("События на сегодня");
        panel.setLayout(new BorderLayout());

        String today = dateFormat.format(currentDate);

        // Получаем события на сегодня
        List<String> todayEvents = getTodayEvents();

        JTextArea eventsArea = new JTextArea();
        eventsArea.setEditable(false);
        eventsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventsArea.setBackground(Color.WHITE);

        StringBuilder eventsText = new StringBuilder();
        eventsText.append("Дата: ").append(today).append("\n\n");

        if (todayEvents.isEmpty()) {
            eventsText.append("Событий на сегодня нет\n\n");
        } else {
            for (String event : todayEvents) {
                eventsText.append("• ").append(event).append("\n");
            }
        }

        eventsText.append("\nОбновите дату для проверки новых событий");

        eventsArea.setText(eventsText.toString());

        JScrollPane scrollPane = new JScrollPane(eventsArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Получает актуальные события на текущую дату
     */
    private List<String> getTodayEvents() {
        List<String> events = new ArrayList<>();
        String today = dateFormat.format(currentDate);

        try {
            List<Client> clients = clientService.getAllClients();
            List<Room> rooms = roomService.getAllRooms();

            // События заезда
            for (Client client : clients) {
                if (today.equals(client.getCheckInDate())) {
                    events.add("🏨 Заезд: " + client.getFirstName() + " " + client.getLastName() +
                            " (номер " + client.getRoomNumber() + ")");
                }
            }

            // События выезда
            for (Client client : clients) {
                if (today.equals(client.getCheckOutDate())) {
                    events.add("🚪 Выезд: " + client.getFirstName() + " " + client.getLastName() +
                            " (номер " + client.getRoomNumber() + ")");
                }
            }

            // Автоматические выселения
            for (Room room : rooms) {
                if ("occupied".equals(room.getStatus()) && today.equals(room.getCheckOutDate())) {
                    events.add("🔄 Автовыезд: номер " + room.getRoomNumber() +
                            " (клиент: " + room.getClientPassport() + ")");
                }
            }

            if (events.isEmpty()) {
                events.add("📭 Событий на сегодня нет");
            }

        } catch (Exception e) {
            events.add("❌ Ошибка загрузки событий: " + e.getMessage());
        }

        return events;
    }

    // Вспомогательные методы
    private JPanel createWidgetPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(52, 73, 94)
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, color.darker(), 0, getHeight(), color.darker().darker());
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, color.brighter(), 0, getHeight(), color);
                } else {
                    gradient = new GradientPaint(0, 0, color, 0, getHeight(), color.darker());
                }

                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        return button;
    }

    private void advanceDate() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            currentDate = calendar.getTime();

            String newDate = dateFormat.format(currentDate);
            boolean success = apiService.advanceDate(newDate);

            if (success) {
                // Сохраняем новую дату в состоянии
                saveCurrentDateToState();

                currentDateLabel.setText("Сегодня: " + newDate);
                JOptionPane.showMessageDialog(this,
                        "Дата обновлена: " + newDate + "\n" +
                                "Проверена занятость номеров.",
                        "Дата обновлена", JOptionPane.INFORMATION_MESSAGE);

                // Полностью обновляем все виджеты
                refreshAllWidgets();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка обновления даты",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Полностью обновляет все виджеты на панели
     */
    public void refreshAllWidgets() {
        logger.info("🔄 Обновление всех виджетов...");

        // Удаляем текущую основную панель
        if (mainPanel != null) {
            remove(mainPanel);
        }

        // Создаем новую основную панель с обновленными данными
        createMainContent();

        // Перерисовываем интерфейс
        revalidate();
        repaint();

        logger.info("✅ Все виджеты обновлены");
    }

    /**
     * Обновляет только виджет событий (более легковесный метод)
     */
    public void refreshTodayEventsWidget() {
        if (todayEventsWidget != null) {
            // Находим родительскую панель и обновляем только этот виджет
            Container parent = todayEventsWidget.getParent();
            if (parent != null) {
                parent.remove(todayEventsWidget);
                JPanel newEventsWidget = createTodayEventsWidget();
                parent.add(newEventsWidget);
                parent.revalidate();
                parent.repaint();
            }
        }
    }



    private void updateWidgets() {
        // Обновляем все виджеты при смене даты
        revalidate();
        repaint();
    }

    private void checkServerConnection() {
        boolean serverAvailable = apiService.isServerAvailable();
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

    // Новые методы для операций
    private void checkOutClient() {
        String passport = JOptionPane.showInputDialog(this,
                "Введите паспорт клиента для выселения:", "Выселение клиента", JOptionPane.QUESTION_MESSAGE);

        if (passport != null && !passport.trim().isEmpty()) {
            // Здесь будет логика выселения клиента
            JOptionPane.showMessageDialog(this,
                    "Функция выселения клиента по паспорту в разработке\nПаспорт: " + passport,
                    "Выселение клиента", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void dismissStaff() {
        String passport = JOptionPane.showInputDialog(this,
                "Введите паспорт сотрудника для увольнения:", "Увольнение сотрудника", JOptionPane.QUESTION_MESSAGE);

        if (passport != null && !passport.trim().isEmpty()) {
            // Здесь будет логика увольнения сотрудника
            JOptionPane.showMessageDialog(this,
                    "Функция увольнения сотрудника по паспорту в разработке\nПаспорт: " + passport,
                    "Увольнение сотрудника", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearStaffData() {
        int result = JOptionPane.showConfirmDialog(this,
                "Очистить всех сотрудников?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = staffService.clearStaffData();
            if (success) {
                JOptionPane.showMessageDialog(this, "Данные о сотрудниках удалены!");
                refreshAllWidgets(); // ОБНОВЛЯЕМ ВИДЖЕТЫ
                return;
            }
            JOptionPane.showMessageDialog(this, "Ошибка очистки данных сотрудников!");
        }
    }

    private void clearClientsData() {
        int result = JOptionPane.showConfirmDialog(this,
                "Очистить всех клиентов?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = clientService.clearClientData();
            if (success) {
                JOptionPane.showMessageDialog(this, "Данные о клиентах удалены!");
                refreshAllWidgets(); // ОБНОВЛЯЕМ ВИДЖЕТЫ
                return;
            }
            JOptionPane.showMessageDialog(this, "Ошибка очистки данных клиентов!");
        }
    }

    private void clearAllData() {
        int result = JOptionPane.showConfirmDialog(this,
                "Вы уверены что хотите очистить ВСЕ данные?\nЭто действие нельзя отменить!",
                "Подтверждение очистки",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean success1 = roomService.clearRoomsData();
            boolean success2 = staffService.clearStaffData();
            boolean success3 = clientService.clearClientData();

            if (success1 && success2 && success3) {
                JOptionPane.showMessageDialog(this, "Все данные успешно удалены!");
                refreshAllWidgets(); // ОБНОВЛЯЕМ ВИДЖЕТЫ
                return;
            }
            JOptionPane.showMessageDialog(this, "Ошибка полной очистки данных!");
        }
    }

    private void clearRoomsData() {
        int result = JOptionPane.showConfirmDialog(this,
                "Очистить все номера?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = roomService.clearRoomsData();
            if (success) {
                JOptionPane.showMessageDialog(this, "Данные о номерах удалены!");
                refreshAllWidgets(); // ОБНОВЛЯЕМ ВИДЖЕТЫ
                return;
            }
            JOptionPane.showMessageDialog(this, "Ошибка очистки данных!");
        }
    }

    private void resetDateToToday() {
        int result = JOptionPane.showConfirmDialog(this,
                "Сбросить дату на сегодняшнюю?\nЭто обновит все данные.",
                "Сброс даты", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            AppStateManager.getInstance().resetToToday();
            loadCurrentDateFromState();
            currentDateLabel.setText("Сегодня: " + dateFormat.format(currentDate));
            refreshAllWidgets();
            JOptionPane.showMessageDialog(this,
                    "Дата сброшена на сегодня: " + dateFormat.format(currentDate),
                    "Дата сброшена", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Вспомогательный класс для статистики номеров
    private static class RoomStats {
        int total;
        int occupied;

        RoomStats(int total, int occupied) {
            this.total = total;
            this.occupied = occupied;
        }
    }

    private void generateReport() {
        try {
            // Получаем данные для отчета
            List<Client> clients = clientService.getAllClients();
            List<Room> rooms = roomService.getAllRooms();
            List<Staff> staffList = staffService.getAllStaff();

            // Анализируем данные
            String report = buildReport(clients, rooms, staffList);

            // Создаем и показываем форму отчета
            ReportForm reportForm = new ReportForm(this, report, "Отчет по отелю");
            reportForm.setVisible(true);

        } catch (Exception e) {
            logger.error("Ошибка генерации отчета: {}", e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Ошибка генерации отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

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
                .collect(java.util.stream.Collectors.groupingBy(Room::getRoomType,
                        java.util.stream.Collectors.counting()));

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
                .collect(java.util.stream.Collectors.groupingBy(Staff::getDepartment,
                        java.util.stream.Collectors.counting()));

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

        //Расчеты доходов (не реализовано)
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

    private void loadReport() {
        try {
            // Создаем пустую форму отчета и сразу вызываем загрузку файла
            ReportForm reportForm = new ReportForm(this,
                    "Выберите файл отчета для загрузки...\n\n" +
                            "Для загрузки отчета нажмите кнопку 'Загрузить отчет'",
                    "Загрузка отчета");
            reportForm.setVisible(true);

        } catch (Exception e) {
            logger.error("Ошибка загрузки отчета: {}", e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки отчета: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}