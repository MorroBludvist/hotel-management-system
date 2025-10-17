package com.hotel.client.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Базовая форма для добавления записей с градиентным дизайном и скроллингом
 */
public abstract class BaseAddForm extends JDialog {
    protected JButton saveButton;
    protected JButton cancelButton;
    protected JScrollPane scrollPane;

    public BaseAddForm(JFrame parent, String title, int width, int height) {
        super(parent, title, true);
        setSize(width, height);
        setLocationRelativeTo(parent);
        setResizable(true); // Разрешаем изменение размера для скролла
        setLayout(new BorderLayout());
    }

    protected abstract void initializeComponents();
    protected abstract JPanel createFieldsPanel(); // Изменен возвращаемый тип
    protected abstract void setupListeners();
    protected abstract boolean validateForm();
    protected abstract void saveData();

    protected void initializeBaseComponents() {
        saveButton = createGradientButton("Сохранить", new Color(46, 204, 113), new Color(39, 174, 96));
        cancelButton = createGradientButton("Отмена", new Color(231, 76, 60), new Color(192, 57, 43));
    }

    protected void setupBaseLayout(String title, Color gradientStart, Color gradientEnd) {
        // Градиентный заголовок
        add(createGradientHeader(title, gradientStart, gradientEnd), BorderLayout.NORTH);

        // Создаем панель с полями
        JPanel fieldsPanel = createFieldsPanel();

        // Создаем скроллпанель
        scrollPane = new JScrollPane(fieldsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Настраиваем скроллбар
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setUI(new CustomScrollBarUI());

        // Основная панель контента
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                createShadowBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Панель кнопок
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // Кастомный скроллбар
    private static class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        private final Dimension THUMB_SIZE = new Dimension(8, 8);

        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(150, 150, 150);
            this.trackColor = new Color(240, 240, 240);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y, thumbBounds.width - 4, thumbBounds.height, 4, 4);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return THUMB_SIZE;
        }
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

        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    protected JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    protected Border createShadowBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        );
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                if (getModel().isRollover() && !getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
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
        button.setPreferredSize(new Dimension(120, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    protected JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        return field;
    }

    protected JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return comboBox;
    }

    protected JComboBox<Integer> createStyledComboBox() {
        JComboBox<Integer> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return comboBox;
    }

    protected JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }

    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    protected void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean validateRequiredFields(JTextField[] requiredFields) {
        for (JTextField field : requiredFields) {
            if (field.getText().trim().isEmpty()) {
                field.requestFocus();
                showError("Пожалуйста, заполните все обязательные поля");
                return false;
            }
        }
        return true;
    }

    protected boolean validatePassport(String passport) {
        if (!passport.matches("\\d{10}")) {
            showError("Паспорт должен содержать ровно 10 цифр");
            return false;
        }
        return true;
    }

    protected JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(220, 220, 220));
        return separator;
    }
}