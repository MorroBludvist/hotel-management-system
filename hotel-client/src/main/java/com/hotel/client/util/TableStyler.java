package com.hotel.client.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class TableStyler {

    /**
     * Применяет красивый стиль к таблице
     */
    public static void styleTable(JTable table) {
        // Основные настройки таблицы
        table.setRowHeight(35);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        // Настройка шрифта
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Стилизация заголовка
        styleTableHeader(table);

        // Установка кастомного рендерера
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        // Отключаем перетаскивание колонок
        table.getTableHeader().setReorderingAllowed(false);

        // Настройка выделения
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Стилизация заголовка таблицы
     */
    private static void styleTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(52, 73, 94)); // Темно-синий
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Кастомный рендерер для заголовка
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setBackground(new Color(52, 73, 94));
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(41, 128, 185)),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                setHorizontalAlignment(JLabel.CENTER);

                return this;
            }
        });
    }

    /**
     * Настраивает ширину колонок для конкретного типа таблицы
     */
    public static void setupColumnWidths(JTable table, String tableType) {
        switch (tableType) {
            case "clients":
                setupClientColumns(table);
                break;
            case "staff":
                setupStaffColumns(table);
                break;
            case "rooms":
                setupRoomColumns(table);
                break;
        }
    }

    private static void setupClientColumns(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Паспорт
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Имя
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Фамилия
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Телефон
        table.getColumnModel().getColumn(4).setPreferredWidth(150); // Email
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Заезд
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Выезд
        table.getColumnModel().getColumn(7).setPreferredWidth(60);  // Номер
        table.getColumnModel().getColumn(8).setPreferredWidth(80);  // Тип номера
    }

    private static void setupStaffColumns(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Паспорт
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Имя
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Фамилия
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Должность
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Телефон
        table.getColumnModel().getColumn(5).setPreferredWidth(150); // Email
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Дата найма
        table.getColumnModel().getColumn(7).setPreferredWidth(90);  // Зарплата
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Отдел
    }

    private static void setupRoomColumns(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(70);  // Номер
        table.getColumnModel().getColumn(1).setPreferredWidth(90);  // Тип
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Статус
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Клиент
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Заезд
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Выезд
    }
}

/**
 * Кастомный рендерер для ячеек таблицы
 */
class CustomTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Чередование цветов строк
        if (row % 2 == 0) {
            setBackground(new Color(248, 249, 249)); // Светло-серый
        } else {
            setBackground(Color.WHITE);
        }

        // Выделенная строка
        if (isSelected) {
            setBackground(new Color(173, 216, 230)); // Светло-голубой
            setForeground(Color.BLACK);
        } else {
            setForeground(Color.BLACK);
        }

        // Стиль границ
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Выравнивание для числовых колонок
        if (value instanceof Number) {
            setHorizontalAlignment(JLabel.RIGHT);
        } else {
            setHorizontalAlignment(JLabel.LEFT);
        }

        // Особые стили для статусов
        if (column == 2 && value != null) { // Колонка статуса
            String status = value.toString();
            if (status.equals("Занят") || status.equals("occupied")) {
                setBackground(new Color(255, 230, 230)); // Светло-красный
                if (!isSelected) setForeground(new Color(192, 57, 43));
            } else if (status.equals("Свободен") || status.equals("free")) {
                setBackground(new Color(230, 255, 230)); // Светло-зеленый
                if (!isSelected) setForeground(new Color(39, 174, 96));
            }
        }

        return this;
    }
}