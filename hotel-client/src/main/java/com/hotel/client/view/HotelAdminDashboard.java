package com.hotel.client.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import com.hotel.client.service.*;
import com.hotel.client.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главное окно панели администратора отеля с управлением датой
 */
public class HotelAdminDashboard extends JFrame {
    private JLabel currentDateLabel;
    private Date currentDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private ApiService apiService;
    private ClientService clientService;
    private RoomService roomService;
    private StaffService staffService;
    //private UIThemeManager uiThemeManager;

    private static final Logger logger = LogManager.getLogger(HotelAdminDashboard.class);

    public HotelAdminDashboard() {
        //Инициализация сервисов для отправки и обработки запросов
        apiService = ApiService.getInstance();
        this.clientService = new ClientService(apiService);
        this.roomService = new RoomService(apiService);
        this.staffService = new StaffService(apiService);

        // Устанавливаем текущую дату
        currentDate = new Date();

        // Проверяем доступность сервера
        checkServerConnection();

        // Основные настройки окна
        //uiThemeManager = new UIThemeManager();
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
        headerPanel.setBackground(new Color(44, 62, 80)); // Темный фон
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Левая часть - название и дата
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftHeader.setOpaque(false);

        JLabel appTitle = new JLabel("🏨 Панель администратора отеля");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(Color.WHITE);

        currentDateLabel = new JLabel("📅 Сегодня: " + dateFormat.format(currentDate));
        currentDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        currentDateLabel.setForeground(new Color(152, 251, 152)); // Светло-зеленый

        leftHeader.add(appTitle);
        leftHeader.add(Box.createHorizontalStrut(20));
        leftHeader.add(currentDateLabel);

        // Правая часть - информация пользователя и кнопки
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setOpaque(false);

        JLabel userLabel = new JLabel("👤 Администратор: Игорь Секирин");
        userLabel.setForeground(Color.WHITE);

        JButton advanceDateButton = createHeaderButton("⏭ Следующий день", new Color(46, 204, 113));
        JButton logoutButton = createHeaderButton("🚪 Выход", new Color(231, 76, 60));

        advanceDateButton.addActionListener(e -> advanceDate());
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

    private JButton createHeaderButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setBorderPainted(false);
        button.setOpaque(true);

        // Эффекты при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
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
            boolean success = apiService.advanceDate(newDate);

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
        navPanel.setBackground(new Color(52, 73, 94)); // Темный фон для контраста

        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        navPanel.setPreferredSize(new Dimension(220, 0));

        // Создаем кнопки с применением стилей
        JButton viewClientsButton = createStyledButton("👥 Список клиентов", "nav-button");
        JButton viewStaffButton = createStyledButton("👨‍💼 Список сотрудников", "nav-button");
        JButton viewRoomsButton = createStyledButton("🏨 Список номеров", "nav-button");

        JButton generateReportButton = createStyledButton("📊 Сгенерировать отчет", "nav-button");
        JButton clearAllDataButton = createStyledButton("🗑 Очистить всю БД", "danger-button");
        JButton clearClientsButton = createStyledButton("Очистить клиентов", "danger-button");
        JButton clearStaffButton = createStyledButton("Очистить сотрудников", "danger-button");
        JButton clearRoomsButton = createStyledButton("Очистить номера", "danger-button");

        // Обработчики событий...
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
        clearAllDataButton.addActionListener(e -> clearAllData());
        clearClientsButton.addActionListener(e -> clearClientsData());
        clearStaffButton.addActionListener(e -> clearStaffData());
        clearRoomsButton.addActionListener(e -> clearRoomsData());

        // Добавляем компоненты
        navPanel.add(createSectionLabel("Управление данными"));
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(viewClientsButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(viewStaffButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(viewRoomsButton);

        navPanel.add(Box.createVerticalStrut(15));
        navPanel.add(createSectionLabel("Отчеты"));
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(generateReportButton);

        navPanel.add(Box.createVerticalStrut(15));
        navPanel.add(createSectionLabel("Очистка данных"));
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(clearClientsButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(clearStaffButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(clearRoomsButton);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(clearAllDataButton);

        navPanel.add(Box.createVerticalGlue());

        add(navPanel, BorderLayout.WEST);
    }

    private JButton createStyledButton(String text, String styleClass) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Рисуем скругленный фон
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setMaximumSize(new Dimension(200, 40));
        button.setPreferredSize(new Dimension(200, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        return button;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(52, 73, 94));
        label.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
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
        List<Room> allRooms = roomService.getAllRooms();
        List<Room> freeRooms = roomService.getFreeRooms();
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
        List<Room> allRooms = roomService.getAllRooms();

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

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(44, 62, 80)
        );
        panel.setBorder(border);
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

    //TODO: функция для генерации XML отчета (заглушка)
    private void generateReport() {
        try {
            // Генерация отчета
            String report = "Отчет по отелю\n" +
                    "Дата: " + new Date() + "\n" +
                    "Клиентов: " + clientService.getAllClients().size() + "\n" +
                    "Сотрудников: " + staffService.getAllStaff().size() + "\n" +
                    "Номеров: " + roomService.getAllRooms().size();

            JOptionPane.showMessageDialog(this, report, "Отчет", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка генерации отчета: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAllData() {
        int result = JOptionPane.showConfirmDialog(this,
                "Вы уверены что хотите очистить ВСЕ данные?\nЭто действие нельзя отменить!",
                "Подтверждение очистки", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean success1 = roomService.clearRoomsData();
            boolean success2 = staffService.clearStaffData();
            boolean success3 = clientService.clearClientData();

            if (success1 && success2 && success3) {
                JOptionPane.showMessageDialog(this, "Все данные успешно удалены!");
                return;
            }
            JOptionPane.showMessageDialog(this, "Ошибка полной очистки данных!");
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
                return;
            }
            JOptionPane.showMessageDialog(this, "Ошибка очистки данных клиентов!");
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
                return;
            }
            JOptionPane.showMessageDialog(this, "Ошибка очистки данных сотрудников!");
        }
    }

    private void clearRoomsData() {
        if (false) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Очистить все номера?",
                    "Подтверждение", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                boolean success = roomService.clearRoomsData();
                if (success) {
                    JOptionPane.showMessageDialog(this, "Данные о клиентах удалены!");
                    return;
                }
                JOptionPane.showMessageDialog(this, "Ошибка очистки данных!");

            }
        }
        showStyledDialog();
    }

    private void showStyledDialog() {
        JDialog dialog = new JDialog((Frame) null, "Красивый диалог", true);
        dialog.setLayout(new BorderLayout());

        // Панель с градиентом
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(74, 144, 226),
                        getWidth(), getHeight(), new Color(142, 45, 226)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setLayout(new BorderLayout(10, 10));
        gradientPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Стилизованные компоненты
        JLabel label = new JLabel("Очистить все номера?", JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);

        // Стилизованные кнопки
        JButton yesButton = createGradientButton("Да", new Color(46, 204, 113));
        JButton noButton = createGradientButton("Нет", new Color(231, 76, 60));

        yesButton.addActionListener(e -> {
            dialog.dispose();
            // Действие при подтверждении
        });

        noButton.addActionListener(e -> dialog.dispose());

        // Компоновка
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        gradientPanel.add(label, BorderLayout.CENTER);
        gradientPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(gradientPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private JButton createGradientButton(String text, Color color) {
        return new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, color.brighter(),
                        0, getHeight(), color.darker()
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(80, 35);
            }
        };
    }
}