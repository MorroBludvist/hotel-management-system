package com.hotel.client.view.dashboard_components;

import com.hotel.client.model.Room;
import com.hotel.client.service.RoomService;
import com.hotel.client.view.HotelAdminDashboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Виджет типов номеров (левый верхний угол)
 * Содержит информацию об общем количестве комнат каждого типа
 * И об их занятости
 */
//TODO: дописать документацию
public class RoomTypesWidget extends BaseWidget {
    private static final Logger logger = LogManager.getLogger(RoomTypesWidget.class);

    private final RoomService roomService;
    private JPanel statsPanel;

    public RoomTypesWidget(HotelAdminDashboard dashboard, RoomService roomService) {
        super(dashboard, "Типы номеров");
        this.roomService = roomService;
        initializeWidget();
        refreshData();
    }

    private void initializeWidget() {
        setLayout(new BorderLayout());

        statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setOpaque(false);

        add(statsPanel, BorderLayout.CENTER);

        // Общая статистика
        JPanel totalPanel = new JPanel(new FlowLayout());
        totalPanel.setOpaque(false);
        add(totalPanel, BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        try {
            List<Room> allRooms = roomService.getAllRooms();
            List<Room> occupiedRooms = allRooms.stream()
                    .filter(room -> "occupied".equals(room.getStatus()))
                    .collect(Collectors.toList());

            Map<String, RoomStats> roomStats = calculateRoomStats(allRooms, occupiedRooms);

            statsPanel.removeAll();

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

            // Обновляем общую статистику
            JPanel totalPanel = (JPanel) getComponent(1);
            totalPanel.removeAll();
            JLabel totalLabel = new JLabel("Всего номеров: " + allRooms.size() +
                    " | Занято: " + occupiedRooms.size());
            totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            totalLabel.setForeground(new Color(100, 100, 100));
            totalPanel.add(totalLabel);

            revalidate();
            repaint();

        } catch (Exception e) {
            logger.error("Ошибка обновления виджета типов номеров: {}", e.getMessage());
        }
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


    private static class RoomStats {
        int total;
        int occupied;

        RoomStats(int total, int occupied) {
            this.total = total;
            this.occupied = occupied;
        }
    }
}