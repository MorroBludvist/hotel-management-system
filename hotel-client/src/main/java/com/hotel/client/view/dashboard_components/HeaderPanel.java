package com.hotel.client.view.dashboard_components;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

import com.hotel.client.view.HotelAdminDashboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Панель заголовка приложения
 * Отображает информацию о дате, пользователе и основные кнопки управления
 */
public class HeaderPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(HeaderPanel.class);

    private final HotelAdminDashboard dashboard;
    private final DashboardActionHandler actionHandler;

    private JLabel currentDateLabel;
    private JLabel appTitleLabel;
    private JLabel userLabel;

    public HeaderPanel(HotelAdminDashboard dashboard, DashboardActionHandler actionHandler) {
        this.dashboard = dashboard;
        this.actionHandler = actionHandler;
        initializePanel();
        setupComponents();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(dashboard.getWidth(), 80));
        setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        setOpaque(false); // Для кастомной отрисовки
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Градиентный фон
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(240, 245, 249),
                getWidth(), getHeight(), new Color(225, 235, 245)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void setupComponents() {
        // Левая часть - название и дата
        JPanel leftPanel = createLeftPanel();

        // Правая часть - пользователь и кнопки
        JPanel rightPanel = createRightPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        appTitleLabel = new JLabel("Панель администратора отеля");
        appTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitleLabel.setForeground(new Color(52, 73, 94));

        // Безопасное получение даты
        String dateString = "Дата не установлена";
        try {
            Date currentDate = dashboard.getCurrentDate();
            if (currentDate != null) {
                dateString = dashboard.getDateFormat().format(currentDate);
            }
        } catch (Exception e) {
            logger.error("Ошибка форматирования даты: {}", e.getMessage());
            dateString = "Ошибка даты";
        }

        currentDateLabel = new JLabel("Сегодня: " + dateString);
        currentDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        currentDateLabel.setForeground(new Color(100, 100, 100));

        panel.add(appTitleLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(currentDateLabel);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        userLabel = new JLabel("Администратор: Игорь Секирин");
        userLabel.setForeground(new Color(100, 100, 100));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Кнопки управления
        JButton resetDateButton = ButtonFactory.createHeaderButton("Сбросить дату", new Color(155, 89, 182));
        JButton advanceDateButton = ButtonFactory.createHeaderButton("Следующий день", new Color(46, 204, 113));
        JButton logoutButton = ButtonFactory.createHeaderButton("Выход", new Color(231, 76, 60));

        // Назначаем обработчики
        resetDateButton.addActionListener(e -> actionHandler.resetDateToToday());
        advanceDateButton.addActionListener(e -> actionHandler.advanceDate());
        logoutButton.addActionListener(e -> actionHandler.logout());

        panel.add(userLabel);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(resetDateButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(advanceDateButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(logoutButton);

        return panel;
    }

    /**
     * Обновляет отображение даты в заголовке
     */
    public void refreshDateDisplay() {
        String dateString = "Дата не установлена";
        try {
            Date currentDate = dashboard.getCurrentDate();
            if (currentDate != null) {
                dateString = dashboard.getDateFormat().format(currentDate);
            }
        } catch (Exception e) {
            logger.error("Ошибка обновления отображения даты: {}", e.getMessage());
            dateString = "Ошибка даты";
        }

        currentDateLabel.setText("Сегодня: " + dateString);
        logger.debug("Обновлено отображение даты: {}", dateString);
    }

    /**
     * Обновляет информацию о пользователе
     * @param userInfo информация о пользователе
     */
    public void updateUserInfo(String userInfo) {
        userLabel.setText(userInfo);
    }
}