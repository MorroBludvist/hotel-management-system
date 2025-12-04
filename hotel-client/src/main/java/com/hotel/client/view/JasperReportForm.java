package com.hotel.client.view;

import javax.swing.*;
import java.awt.*;

/**
 * Форма для работы с JasperReports
 */
public class JasperReportForm extends JFrame {
    private JButton compileTemplateButton;
    private JButton generatePdfButton;
    private JButton generateHtmlButton;
    private JButton previewButton;
    private JComboBox<String> templateComboBox;
    private JTextArea logArea;

    public JasperReportForm() {
        super("JasperReports - Генерация отчетов");
        initializeComponents();
        setupLayout();
        setupListeners();
        loadTemplates();
    }

    private void initializeComponents() {
        compileTemplateButton = createStyledButton("Компилировать шаблон", new Color(155, 89, 182));
        generatePdfButton = createStyledButton("PDF отчет", new Color(41, 128, 185));
        generateHtmlButton = createStyledButton("HTML отчет", new Color(39, 174, 96));
        previewButton = createStyledButton("Предпросмотр", new Color(230, 126, 34));

        templateComboBox = new JComboBox<>();
        templateComboBox.setPreferredSize(new Dimension(200, 35));

        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(800, 500));

        // Заголовок
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("JasperReports - Генерация отчетов");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(titleLabel);

        // Панель управления
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        controlPanel.setBackground(Color.WHITE);

        // Выбор шаблона
        JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        templatePanel.setBackground(Color.WHITE);
        templatePanel.add(new JLabel("Шаблон отчета:"));
        templatePanel.add(templateComboBox);
        templatePanel.add(compileTemplateButton);

        // Кнопки генерации
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(generatePdfButton);
        buttonPanel.add(generateHtmlButton);
        buttonPanel.add(previewButton);

        controlPanel.add(templatePanel, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);

        // Лог
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Лог выполнения"));

        add(headerPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void setupListeners() {
        compileTemplateButton.addActionListener(e -> compileTemplate());
        generatePdfButton.addActionListener(e -> generatePdfReport());
        generateHtmlButton.addActionListener(e -> generateHtmlReport());
        previewButton.addActionListener(e -> previewReport());
    }

    private void loadTemplates() {
        // Загрузка доступных шаблонов
        templateComboBox.addItem("staff_report.jrxml");
        templateComboBox.addItem("client_report.jrxml");
        templateComboBox.addItem("room_report.jrxml");
    }

    private void compileTemplate() {
        String template = (String) templateComboBox.getSelectedItem();
        logArea.append("🔨 Компиляция шаблона: " + template + "\n");
        // Здесь будет вызов JasperReports для компиляции
    }

    private void generatePdfReport() {
        String template = (String) templateComboBox.getSelectedItem();
        logArea.append("📄 Генерация PDF отчета из: " + template + "\n");
        // Здесь будет вызов JasperReports для генерации PDF
    }

    private void generateHtmlReport() {
        String template = (String) templateComboBox.getSelectedItem();
        logArea.append("🌐 Генерация HTML отчета из: " + template + "\n");
        // Здесь будет вызов JasperReports для генерации HTML
    }

    private void previewReport() {
        String template = (String) templateComboBox.getSelectedItem();
        logArea.append("👁️ Предпросмотр отчета: " + template + "\n");
        // Здесь будет вызов JasperReports для предпросмотра
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return button;
    }

    public static void showJasperReportForm() {
        SwingUtilities.invokeLater(() -> {
            JasperReportForm form = new JasperReportForm();
            form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            form.setVisible(true);
        });
    }
}