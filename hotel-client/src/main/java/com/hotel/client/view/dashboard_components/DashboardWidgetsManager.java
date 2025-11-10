package com.hotel.client.view.dashboard_components;

import com.hotel.client.service.ClientService;
import com.hotel.client.service.RoomService;
import com.hotel.client.service.StaffService;
import com.hotel.client.view.HotelAdminDashboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер виджетов dashboard
 * Управляет созданием, обновлением и размещением виджетов
 */
public class DashboardWidgetsManager {
    private static final Logger logger = LogManager.getLogger(DashboardWidgetsManager.class);

    private final HotelAdminDashboard dashboard;
    private final ClientService clientService;
    private final RoomService roomService;
    private final StaffService staffService;

    private final Map<String, BaseWidget> widgets = new HashMap<>();

    public DashboardWidgetsManager(HotelAdminDashboard dashboard,
                                   ClientService clientService,
                                   RoomService roomService,
                                   StaffService staffService) {
        this.dashboard = dashboard;
        this.clientService = clientService;
        this.roomService = roomService;
        this.staffService = staffService;
        initializeWidgets();
    }

    /**
     * Инициализирует все виджеты
     */
    private void initializeWidgets() {
        try {
            widgets.put("roomTypes", new RoomTypesWidget(dashboard, roomService));
            widgets.put("calendar", new CalendarWidget(dashboard, clientService, roomService));
            widgets.put("quickActions", new QuickActionsWidget(dashboard));
            widgets.put("todayEvents", new TodayEventsWidget(dashboard, clientService, roomService));
            logger.info("Все виджеты инициализированы");
        } catch (Exception e) {
            logger.error("Ошибка инициализации виджетов: {}", e.getMessage());
        }
    }

    /**
     * Возвращает панель со всеми виджетами
     */
    public JPanel getWidgetsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 242, 245));

        // Безопасное добавление виджетов
        for (BaseWidget widget : widgets.values()) {
            if (widget != null) {
                panel.add(widget);
            }
        }
        return panel;
    }

    /**
     * Обновляет все виджеты
     */
    public void refreshAllWidgets() {
        logger.info("🔄 Начало обновления всех виджетов...");
        for (Map.Entry<String, BaseWidget> entry : widgets.entrySet()) {
            try {
                if (entry.getValue() != null) {
                    entry.getValue().refreshData();
                    logger.debug("Виджет {} обновлен", entry.getKey());
                }
            } catch (Exception e) {
                logger.error("Ошибка обновления виджета {}: {}", entry.getKey(), e.getMessage());
            }
        }
        logger.info("✅ Все виджеты обновлены");
    }

    /**
     * Обновляет конкретный виджет
     */
    public void refreshWidget(String widgetName) {
        BaseWidget widget = widgets.get(widgetName);
        if (widget != null) {
            try {
                widget.refreshData();
                logger.debug("Виджет {} обновлен", widgetName);
            } catch (Exception e) {
                logger.error("Ошибка обновления виджета {}: {}", widgetName, e.getMessage());
            }
        } else {
            logger.warn("Виджет {} не найден", widgetName);
        }
    }
}