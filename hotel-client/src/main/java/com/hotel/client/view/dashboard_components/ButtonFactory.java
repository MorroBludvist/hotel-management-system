package com.hotel.client.view.dashboard_components;

import javax.swing.*;
import java.awt.*;

/**
 * Фабрика для создания стандартизированных кнопок интерфейса
 */
public class ButtonFactory {

    /**
     * Создание кнопки для header (наверху)
     */
    public static JButton createHeaderButton(String text, Color baseColor) {
        return createGradientButton(text, baseColor, new Dimension(120, 35), 20);
    }

    /**
     * Создание кнопки для навигационной панели (слева)
     */
    public static JButton createNavButton(String text, Color baseColor) {
        return createGradientButton(text, baseColor, new Dimension(220, 40), 15);
    }

    /**
     * Создание кнопки для виджета быстрых действий (левый нижний угол)
     */
    public static JButton createActionButton(String text, Color baseColor) {
        JButton button = createGradientButton(text, baseColor, null, 10);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return button;
    }

    /**
     * Создание кастомной кнопки с градиентом
     */
    private static JButton createGradientButton(String text, Color baseColor,
                                                Dimension preferredSize, int arc) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, baseColor.darker(), 0, getHeight(), baseColor.darker().darker());
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, baseColor.brighter(), 0, getHeight(), baseColor);
                } else {
                    gradient = new GradientPaint(0, 0, baseColor, 0, getHeight(), baseColor.darker());
                }

                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        if (preferredSize != null) {
            button.setPreferredSize(preferredSize);
        }
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}