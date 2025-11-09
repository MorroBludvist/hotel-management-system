package com.hotel.client.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Форма для отображения, сохранения и загрузки отчетов
 */
public class ReportForm extends JDialog {
    private JTextArea reportTextArea;
    private JButton saveButton;
    private JButton loadButton;
    private JButton closeButton;
    private JLabel fileInfoLabel;

    // Путь к папке с отчетами
    private final Path reportsDir = Paths.get("docs");

    public ReportForm(JFrame parent, String reportContent, String title) {
        super(parent, title, true);
        setSize(800, 700); // Увеличил размер для лучшего отображения
        setLocationRelativeTo(parent);
        setResizable(true);
        setLayout(new BorderLayout());

        // Создаем папку для отчетов если её нет
        createReportsDirectory();

        initializeComponents(reportContent);
        setupLayout();
        setupListeners();
    }

    private void createReportsDirectory() {
        try {
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
                logger.info("Создана папка для отчетов: " + reportsDir.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Ошибка создания папки для отчетов: {}", e.getMessage());
        }
    }

    private void initializeComponents(String reportContent) {
        reportTextArea = new JTextArea(reportContent);
        reportTextArea.setEditable(true);
        reportTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        reportTextArea.setBackground(new Color(248, 249, 250));
        reportTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        reportTextArea.setLineWrap(true);
        reportTextArea.setWrapStyleWord(true);

        saveButton = createGradientButton("Сохранить отчет", new Color(46, 204, 113), new Color(39, 174, 96));
        loadButton = createGradientButton("Загрузить отчет", new Color(52, 152, 219), new Color(41, 128, 185));
        closeButton = createGradientButton("Закрыть", new Color(231, 76, 60), new Color(192, 57, 43));

        fileInfoLabel = new JLabel(" ");
        fileInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fileInfoLabel.setForeground(new Color(100, 100, 100));
    }

    private void setupLayout() {
        // Заголовок
        JPanel headerPanel = createGradientHeader("Отчет по отелю",
                new Color(155, 89, 182), new Color(142, 68, 173));
        add(headerPanel, BorderLayout.NORTH);

        // Основное содержимое
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Содержимое отчета",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(52, 73, 94)
        ));

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель информации о файле
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        infoPanel.add(fileInfoLabel, BorderLayout.WEST);
        contentPanel.add(infoPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        saveButton.addActionListener(e -> saveReportToFile());
        loadButton.addActionListener(e -> loadReportFromFile());
        closeButton.addActionListener(e -> dispose());
    }

    private JPanel createGradientHeader(String title, Color startColor, Color endColor) {
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

        JLabel dateLabel = new JLabel(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateLabel.setForeground(new Color(236, 240, 241));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JButton createGradientButton(String text, Color startColor, Color endColor) {
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
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(140, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void saveReportToFile() {
        // Сохраняем сразу в папку docs с автоматическим именем
        String fileName = "отчет_отеля_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date()) + ".txt";
        Path filePath = reportsDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(reportTextArea.getText());

            // Обновляем информацию о файле
            updateFileInfo(filePath.toFile(), "Сохранен: ");

            JOptionPane.showMessageDialog(this,
                    "Отчет успешно сохранен в:\n" + filePath.toAbsolutePath(),
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка сохранения отчета: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReportFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите отчет для загрузки");
        fileChooser.setCurrentDirectory(reportsDir.toFile());
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Текстовые файлы (*.txt)", "txt"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadReportFile(selectedFile);
        }
    }

    private void loadReportFile(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
            reportTextArea.setText(content);

            // Обновляем информацию о файле
            updateFileInfo(file, "Загружен: ");

            // Обновляем заголовок окна
            setTitle("Отчет по отелю - " + file.getName());

            JOptionPane.showMessageDialog(this,
                    "Отчет успешно загружен!",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки отчета: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFileInfo(File file, String prefix) {
        try {
            String fileName = file.getName();
            long fileSize = file.length();
            String lastModified = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(
                    new Date(file.lastModified()));

            String info = String.format("%s%s | Размер: %,d байт | Изменен: %s",
                    prefix, fileName, fileSize, lastModified);
            fileInfoLabel.setText(info);

        } catch (Exception e) {
            fileInfoLabel.setText(prefix + file.getName());
        }
    }

    // Добавляем логгер (TODO: изменить формат добавления)
    private static final org.apache.logging.log4j.Logger logger =
            org.apache.logging.log4j.LogManager.getLogger(ReportForm.class);
}