package com.hotel.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GradientDialog extends JDialog {
    private int result = JOptionPane.CLOSED_OPTION;
    private Color startColor = new Color(74, 144, 226);
    private Color endColor = new Color(142, 45, 226);

    // Конструктор
    public GradientDialog(Window owner, String title, String message, int optionType) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        initializeDialog(message, optionType);
    }

    // Статические методы для удобства
    public static int showConfirmDialog(Component parent, String title, String message) {
        return showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
    }

    public static int showConfirmDialog(Component parent, String title, String message, int optionType) {
        GradientDialog dialog = new GradientDialog(getWindow(parent), message, title, optionType);
        return dialog.showDialog();
    }

    public static void showMessageDialog(Component parent, String title, String message) {
        GradientDialog dialog = new GradientDialog(getWindow(parent), title, message, JOptionPane.DEFAULT_OPTION);
        dialog.showDialog();
    }

    private static Window getWindow(Component parent) {
        if (parent == null) return null;
        if (parent instanceof Window) return (Window) parent;
        return SwingUtilities.getWindowAncestor(parent);
    }

    // Основная реализация
    private void initializeDialog(String message, int optionType) {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Создание основного панели с градиентом
        JPanel gradientPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, startColor,
                        getWidth(), getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setBorder(new EmptyBorder(25, 30, 20, 30));
        gradientPanel.setPreferredSize(new Dimension(400, 180));

        // Создание сообщения
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Создание кнопок
        JPanel buttonPanel = createButtonPanel(optionType);

        // Компоновка
        gradientPanel.add(messageLabel, BorderLayout.CENTER);
        gradientPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(gradientPanel);

        setupListeners();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel createButtonPanel(int optionType) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        switch (optionType) {
            case JOptionPane.YES_NO_OPTION:
                addButton(buttonPanel, "Да", JOptionPane.YES_OPTION, new Color(46, 204, 113));
                addButton(buttonPanel, "Нет", JOptionPane.NO_OPTION, new Color(231, 76, 60));
                break;
            case JOptionPane.OK_CANCEL_OPTION:
                addButton(buttonPanel, "OK", JOptionPane.OK_OPTION, new Color(52, 152, 219));
                addButton(buttonPanel, "Отмена", JOptionPane.CANCEL_OPTION, new Color(149, 165, 166));
                break;
            case JOptionPane.DEFAULT_OPTION:
                addButton(buttonPanel, "OK", JOptionPane.OK_OPTION, new Color(52, 152, 219));
                break;
            case JOptionPane.YES_NO_CANCEL_OPTION:
                addButton(buttonPanel, "Да", JOptionPane.YES_OPTION, new Color(46, 204, 113));
                addButton(buttonPanel, "Нет", JOptionPane.NO_OPTION, new Color(231, 76, 60));
                addButton(buttonPanel, "Отмена", JOptionPane.CANCEL_OPTION, new Color(149, 165, 166));
                break;
        }

        return buttonPanel;
    }

    private void addButton(JPanel panel, String text, int returnValue, Color baseColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color topColor, bottomColor;
                if (this.isHovered) {
                    // Более яркие цвета при наведении
                    topColor = baseColor.brighter().brighter();
                    bottomColor = baseColor.brighter();
                } else {
                    topColor = baseColor.brighter();
                    bottomColor = baseColor.darker();
                }

                GradientPaint btnGradient = new GradientPaint(
                        0, 0, topColor,
                        0, getHeight(), bottomColor
                );
                g2.setPaint(btnGradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Добавляем тонкую обводку при наведении
                if (this.isHovered) {
                    g2.setColor(baseColor.brighter().brighter());
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                }

                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(80, 30);
            }
        };

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Обработчики наведения мыши
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                JButton btn = (JButton) e.getSource();
                // Используем рефлексию для доступа к полю isHovered
                try {
                    java.lang.reflect.Field field = btn.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(btn, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                btn.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                JButton btn = (JButton) e.getSource();
                try {
                    java.lang.reflect.Field field = btn.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(btn, false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                btn.repaint();
            }
        });

        button.addActionListener(e -> {
            result = returnValue;
            dispose();
        });

        panel.add(button);
    }

    private void setupListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                result = JOptionPane.CLOSED_OPTION;
                dispose();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "closeDialog");
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = JOptionPane.CLOSED_OPTION;
                dispose();
            }
        });
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }

    // Сеттеры для кастомизации
    public void setGradientColors(Color start, Color end) {
        this.startColor = start;
        this.endColor = end;
        repaint();
    }
}