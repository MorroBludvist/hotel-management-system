// com/hotel/client/view/BaseTableForm.java
package com.hotel.client.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Базовый класс для форм с таблицами с градиентным дизайном
 */
public abstract class BaseTableForm extends JDialog {
    protected JTable table;
    protected JButton refreshButton;
    protected JButton closeButton;
    protected JButton exportButton;
    protected JLabel countLabel;

    public BaseTableForm(JFrame parent, String title, int width, int height) {
        super(parent, title, true);
        setSize(width, height);
        setLocationRelativeTo(parent);
        setResizable(true);
        setLayout(new BorderLayout());
    }

    protected abstract void initializeTable();
    protected abstract void loadData();
    protected abstract void setupAdditionalComponents();

    protected void initializeComponents() {
        initializeTable();
        createButtons();
        setupAdditionalComponents();
    }

    protected void createButtons() {
        refreshButton = createGradientButton("Обновить", new Color(52, 152, 219), new Color(41, 128, 185));
        exportButton = createGradientButton("Экспорт", new Color(46, 204, 113), new Color(39, 174, 96));
        closeButton = createGradientButton("Закрыть", new Color(231, 76, 60), new Color(192, 57, 43));
    }

    protected void setupBaseLayout(String title, Color gradientStart, Color gradientEnd) {
        // Градиентный заголовок
        add(createGradientHeader(title, gradientStart, gradientEnd), BorderLayout.NORTH);

        // Панель таблицы с тенью
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Панель кнопок
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    protected JPanel createGradientHeader(String title, Color startColor, Color endColor) {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        countLabel = new JLabel("Всего записей: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        countLabel.setForeground(new Color(236, 240, 241));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(countLabel, BorderLayout.EAST);

        return headerPanel;
    }

    protected JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                createShadowBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        tablePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(248, 249, 250));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Стилизация скроллбара
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    protected JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        //TODO: реализовать экспорт данных, если появится потребность
        //buttonPanel.add(exportButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    protected Border createShadowBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
    }

    protected void setupBaseListeners() {
        refreshButton.addActionListener(e -> loadData());
        closeButton.addActionListener(e -> dispose());
        exportButton.addActionListener(e -> showExportMessage());
    }

    protected void showExportMessage() {
        JOptionPane.showMessageDialog(this,
                "Функция экспорта данных в разработке",
                "Экспорт данных",
                JOptionPane.INFORMATION_MESSAGE);
    }

    protected JButton createGradientButton(String text, Color startColor, Color endColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, startColor.darker(), 0, getHeight(), endColor.darker());
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, startColor.brighter(), 0, getHeight(), endColor.brighter());
                } else {
                    gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                }

                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Тень при наведении
                if (getModel().isRollover() && !getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 25, 25);
                }

                g2.dispose();

                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Убираем стандартную границу
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    protected void applyTableStyles() {
        table.setRowHeight(42);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new GradientHeaderRenderer());
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        table.setDefaultRenderer(Object.class, new GradientCellRenderer());
        table.setSelectionBackground(new Color(52, 152, 219, 80));
        table.setSelectionForeground(new Color(44, 62, 80));
    }

    // Рендерер для заголовка таблицы с градиентом
    protected static class GradientHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setOpaque(true);
            setBackground(new Color(52, 73, 94));
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(41, 128, 185)),
                    BorderFactory.createEmptyBorder(12, 10, 12, 10)
            ));
            setHorizontalAlignment(JLabel.CENTER);

            return this;
        }
    }

    // Рендерер для ячеек таблицы
    protected static class GradientCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(240, 240, 240)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            setHorizontalAlignment(JLabel.CENTER);

            // Зебровый эффект с мягкими цветами
            if (row % 2 == 0) {
                setBackground(new Color(250, 251, 252));
            } else {
                setBackground(Color.WHITE);
            }

            // Выделение строки
            if (isSelected) {
                setBackground(new Color(52, 152, 219, 60));
                setForeground(new Color(44, 62, 80));
            } else {
                setForeground(new Color(52, 73, 94));
            }

            return this;
        }
    }

    protected void updateCountLabel(int count) {
        if (countLabel != null) {
            countLabel.setText("Всего записей: " + count);
        }
    }

    protected void showLoadingError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка загрузки", JOptionPane.ERROR_MESSAGE);
    }

    protected void showEmptyDataMessage() {
        JOptionPane.showMessageDialog(this,
                "Нет данных для отображения.\n\n" +
                        "Возможные причины:\n" +
                        "• Сервер недоступен\n" +
                        "• Нет данных в базе\n" +
                        "• Ошибка соединения",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
}