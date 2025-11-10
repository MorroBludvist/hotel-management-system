package com.hotel.client.view.dashboard_components;

import com.hotel.client.view.HotelAdminDashboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Виджет быстрых действий (левый нижний угол)
 * Содержит кнопки заселения/выселения клиента
 * и найма/увольнения сотрудника
 */
public class QuickActionsWidget extends BaseWidget {
    private static final Logger logger = LogManager.getLogger(QuickActionsWidget.class);

    /**
     * Конструктор QuickActionsWidget
     */
    public QuickActionsWidget(HotelAdminDashboard dashboard) {
        super(dashboard, "Быстрые действия");
        initializeWidget();
    }

    /**
     * Инициализация компонентов виджета
     */
    private void initializeWidget() {
        setLayout(new GridLayout(4, 1, 10, 10));

        JButton checkinButton = ButtonFactory.createActionButton("Заселить клиента", new Color(41, 128, 185));
        JButton addStaffButton = ButtonFactory.createActionButton("Добавить сотрудника", new Color(230, 126, 34));
        JButton checkOutButton = ButtonFactory.createActionButton("Выселить клиента", new Color(231, 76, 60));
        JButton dismissStaffButton = ButtonFactory.createActionButton("Уволить сотрудника", new Color(192, 57, 43));

        // Назначаем обработчики через action handler
        checkinButton.addActionListener(e ->
                dashboard.getActionHandler().showCheckInForm());
        addStaffButton.addActionListener(e ->
                dashboard.getActionHandler().showAddStaffForm());
        checkOutButton.addActionListener(e ->
                dashboard.getActionHandler().checkOutClient());
        dismissStaffButton.addActionListener(e ->
                dashboard.getActionHandler().dismissStaff());

        add(checkinButton);
        add(addStaffButton);
        add(checkOutButton);
        add(dismissStaffButton);
    }

    /**
     * Обновление данных (не используется)
     */
    @Override
    public void refreshData() {}
}