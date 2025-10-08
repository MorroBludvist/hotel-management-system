package com.hotel.client.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;
import com.hotel.client.service.DatabaseManager;
import com.hotel.client.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Форма для просмотра списка номеров
 */
public class RoomsListForm extends JDialog {
    private JTable roomsTable;
    private JButton refreshButton;
    private JButton closeButton;
    private JComboBox<String> filterComboBox;
    private DatabaseManager dbManager;

    public RoomsListForm(JFrame parent) {
        super(parent, "Список номеров отеля", true);
        this.dbManager = DatabaseManager.getInstance();
        initializeComponents();
        setupLayout();
        setupListeners();
        loadRoomsData();
        pack();
        setLocationRelativeTo(parent);
        setSize(900, 600);
    }

    private void initializeComponents() {
        // Создаем модель таблицы
        String[] columns = {"Номер", "Тип", "Статус", "Клиент", "Заезд", "Выезд"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование
            }
        };

        roomsTable = new JTable(model);
        roomsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomsTable.getTableHeader().setReorderingAllowed(false);

        // Настраиваем отображение таблицы
        roomsTable.setRowHeight(25);
        roomsTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // Номер
        roomsTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // Тип
        roomsTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // Статус
        roomsTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Клиент
        roomsTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Заезд
        roomsTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Выезд

        // Комбобокс для фильтрации
        String[] filters = {"Все номера", "Свободные", "Занятые", "Эконом", "Стандарт", "Бизнес", "Люкс"};
        filterComboBox = new JComboBox<>(filters);

        refreshButton = new JButton("Обновить");
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);

        closeButton = new JButton("Закрыть");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Заголовок и фильтры
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Список номеров отеля");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(70, 130, 180));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("Фильтр:"));
        filterPanel.add(filterComboBox);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Таблица с прокруткой
        JScrollPane scrollPane = new JScrollPane(roomsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок и статистики
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Статистика
        JLabel statsLabel = new JLabel("Всего номеров: 0 | Свободно: 0 | Занято: 0");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        bottomPanel.add(statsLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadRoomsData();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });
    }

    private void loadRoomsData() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshButton.setEnabled(false);

            // Получаем данные с сервера
            List<Room> allRooms = dbManager.getAllRooms();

            // Обновляем таблицу
            updateRoomsTable(allRooms);

            if (allRooms.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Нет данных о номерах.\n\n" +
                                "Возможные причины:\n" +
                                "• Сервер недоступен\n" +
                                "• Ошибка соединения",
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ошибка загрузки данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshButton.setEnabled(true);
        }
    }

    private void updateRoomsTable(List<Room> rooms) {
        DefaultTableModel model = (DefaultTableModel) roomsTable.getModel();
        model.setRowCount(0);

        for (Room room : rooms) {
            String status = "free".equals(room.getStatus()) ? "Свободен" : "Занят";
            String clientInfo = room.getClientPassport() != null ? room.getClientPassport() : "-";
            String checkIn = room.getCheckInDate() != null ? room.getCheckInDate() : "-";
            String checkOut = room.getCheckOutDate() != null ? room.getCheckOutDate() : "-";

            model.addRow(new Object[]{
                    room.getRoomNumber(),
                    room.getRoomType(),
                    status,
                    clientInfo,
                    checkIn,
                    checkOut
            });
        }

        // Обновляем статистику
        updateStatistics(rooms);
    }

    private void updateStatistics(List<Room> rooms) {
        long totalRooms = rooms.size();
        long freeRooms = rooms.stream().filter(r -> "free".equals(r.getStatus())).count();
        long occupiedRooms = rooms.stream().filter(r -> "occupied".equals(r.getStatus())).count();

        // Находим и обновляем label статистики
        Component[] components = ((JPanel)getContentPane().getComponent(2)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && comp != ((JPanel)getContentPane().getComponent(2)).getComponent(1)) {
                ((JLabel)comp).setText(
                        "Всего номеров: " + totalRooms + " | " +
                                "Свободно: " + freeRooms + " | " +
                                "Занято: " + occupiedRooms
                );
                break;
            }
        }
    }

    private void applyFilter() {
        try {
            String filter = (String) filterComboBox.getSelectedItem();
            List<Room> allRooms = dbManager.getAllRooms();
            List<Room> filteredRooms;

            switch (filter) {
                case "Свободные":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "free".equals(room.getStatus()))
                            .collect(Collectors.toList());
                    break;
                case "Занятые":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "occupied".equals(room.getStatus()))
                            .collect(Collectors.toList());
                    break;
                case "Эконом":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "Эконом".equals(room.getRoomType()))
                            .collect(Collectors.toList());
                    break;
                case "Стандарт":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "Стандарт".equals(room.getRoomType()))
                            .collect(Collectors.toList());
                    break;
                case "Бизнес":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "Бизнес".equals(room.getRoomType()))
                            .collect(Collectors.toList());
                    break;
                case "Люкс":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "Люкс".equals(room.getRoomType()))
                            .collect(Collectors.toList());
                    break;
                default:
                    filteredRooms = allRooms;
            }

            updateRoomsTable(filteredRooms);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ошибка применения фильтра: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}