package com.hotel.client;

import com.hotel.client.view.HotelAdminDashboard;

import javax.swing.*;

/**
 * Главный класс клиентского приложения
 */
public class HotelClientApplication {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //TODO: заменить логированием с TRACE
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            HotelAdminDashboard dashboard = new HotelAdminDashboard();
            dashboard.setVisible(true);
        });
    }
}