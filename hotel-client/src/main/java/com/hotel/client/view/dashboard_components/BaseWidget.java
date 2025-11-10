package com.hotel.client.view.dashboard_components;

import com.hotel.client.view.HotelAdminDashboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Абстрактный базовый класс для всех виджетов dashboard
 */
public abstract class BaseWidget extends JPanel {
    protected static final Logger logger = LogManager.getLogger(BaseWidget.class);

    protected final HotelAdminDashboard dashboard;

    public BaseWidget(HotelAdminDashboard dashboard, String title) {
        this.dashboard = dashboard;
        setBorder(createWidgetBorder(title));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
    }

    /**
     * Создает стандартную границу для виджета
     */
    protected TitledBorder createWidgetBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(52, 73, 94)
        );
    }

    /**
     * Обновляет данные виджета
     */
    public abstract void refreshData();
}