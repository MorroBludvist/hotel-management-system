package com.hotel.client.view.dashboard_components;

import javax.swing.*;
import java.awt.*;
import com.hotel.client.view.HotelAdminDashboard;

/**
 * Панель навигации приложения
 * Содержит кнопки для доступа к различным функциям системы
 */
public class NavigationPanel extends JPanel {
    private final HotelAdminDashboard dashboard;
    private final DashboardActionHandler actionHandler;

    public NavigationPanel(HotelAdminDashboard dashboard, DashboardActionHandler actionHandler) {
        this.dashboard = dashboard;
        this.actionHandler = actionHandler;
        initializePanel();
        setupNavigation();
    }

    private void initializePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        setPreferredSize(new Dimension(250, 0));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(245, 248, 250),
                getWidth(), getHeight(), new Color(235, 242, 248)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void setupNavigation() {
        // Управление данными
        add(createSectionLabel("Управление данными"));
        add(Box.createVerticalStrut(8));
        add(createNavButton("Список клиентов", new Color(41, 128, 185),
                e -> actionHandler.showClientsList()));
        add(Box.createVerticalStrut(5));
        add(createNavButton("Список сотрудников", new Color(230, 126, 34),
                e -> actionHandler.showStaffList()));
        add(Box.createVerticalStrut(5));
        add(createNavButton("Список номеров", new Color(39, 174, 96),
                e -> actionHandler.showRoomsList()));

        // Операции
        add(Box.createVerticalStrut(20));
        add(createSectionLabel("Операции"));
        add(Box.createVerticalStrut(5));
        add(createNavButton("Сгенерировать отчет", new Color(155, 89, 182),
                e -> actionHandler.generateReport()));
        add(Box.createVerticalStrut(5));
        add(createNavButton("Загрузить отчет", new Color(125, 60, 152),
                e -> actionHandler.loadReport()));
        add(Box.createVerticalStrut(5));
        add(createNavButton("История бронирований", new Color(142, 68, 173),
                e -> actionHandler.showBookingHistory()));

        add(Box.createVerticalGlue());
    }

    private JButton createNavButton(String text, Color color, java.awt.event.ActionListener listener) {
        JButton button = ButtonFactory.createNavButton(text, color);
        button.addActionListener(listener);
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
}