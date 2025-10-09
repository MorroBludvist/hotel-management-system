package com.hotel.client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.hotel.client.service.ApiService;
import com.hotel.client.model.Staff;

import com.hotel.client.service.StaffService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Форма для добавления сотрудника с паспортом как первичным ключом
 */
public class AddStaffForm extends JDialog {
    private JTextField passportField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField positionField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField hireDateField;
    private JTextField salaryField;
    private JComboBox<String> departmentComboBox;
    private JButton saveButton;
    private JButton cancelButton;

    private ApiService apiService;
    private StaffService staffService;

    public AddStaffForm(JFrame parent) {
        super(parent, "Добавление сотрудника", true);
        this.apiService = ApiService.getInstance();
        this.staffService = new StaffService(apiService);
        initializeComponents();
        setupLayout();
        setupListeners();
        pack();
        setLocationRelativeTo(parent);
        setSize(500, 500);  //Увеличиваем высоту для нового поля
    }

    private void initializeComponents() {
        passportField = new JTextField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        positionField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        hireDateField = new JTextField(20);
        salaryField = new JTextField(20);

        String[] departments = {"Администрация", "Обслуживание", "Кухня", "Уборка", "Безопасность", "IT"};
        departmentComboBox = new JComboBox<>(departments);

        saveButton = new JButton("Добавить сотрудника");
        saveButton.setBackground(new Color(210, 105, 30));
        saveButton.setForeground(Color.WHITE);

        cancelButton = new JButton("Отмена");

        // Устанавливаем подсказки
        passportField.setToolTipText("Серия и номер паспорта (10 цифр)");
        hireDateField.setToolTipText("Формат: ГГГГ-ММ-ДД (например: 2024-01-20)");
        salaryField.setToolTipText("Например: 50000.00");
        positionField.setToolTipText("Например: Администратор, Горничная, Повар");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Основная панель с полями
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Заголовок
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Информация о сотруднике");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(210, 105, 30));
        fieldsPanel.add(titleLabel, gbc);

        // Пустая строка для отступа
        gbc.gridy = 1; gbc.gridwidth = 2;
        fieldsPanel.add(Box.createVerticalStrut(10), gbc);

        // Поля формы - ДОБАВЛЯЕМ поле паспорта
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        fieldsPanel.add(new JLabel("Паспорт*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(passportField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        fieldsPanel.add(new JLabel("Имя*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(firstNameField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; fieldsPanel.add(new JLabel("Фамилия*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(lastNameField, gbc);

        gbc.gridy = 5; gbc.gridx = 0; fieldsPanel.add(new JLabel("Должность*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(positionField, gbc);

        gbc.gridy = 6; gbc.gridx = 0; fieldsPanel.add(new JLabel("Телефон:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(phoneField, gbc);

        gbc.gridy = 7; gbc.gridx = 0; fieldsPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(emailField, gbc);

        gbc.gridy = 8; gbc.gridx = 0; fieldsPanel.add(new JLabel("Дата найма*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(hireDateField, gbc);

        gbc.gridy = 9; gbc.gridx = 0; fieldsPanel.add(new JLabel("Зарплата*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(salaryField, gbc);

        gbc.gridy = 10; gbc.gridx = 0; fieldsPanel.add(new JLabel("Отдел*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(departmentComboBox, gbc);

        // Подпись обязательных полей
        gbc.gridy = 11; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel requiredLabel = new JLabel("* - обязательные поля");
        requiredLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        requiredLabel.setForeground(Color.GRAY);
        fieldsPanel.add(requiredLabel, gbc);

        add(fieldsPanel, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        saveButton.addActionListener(e -> saveStaff());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveStaff() {
        // Валидация обязательных полей - ДОБАВЛЯЕМ паспорт
        if (passportField.getText().trim().isEmpty() ||
                firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                positionField.getText().trim().isEmpty() ||
                hireDateField.getText().trim().isEmpty() ||
                salaryField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Пожалуйста, заполните все обязательные поля (отмечены *)",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Валидация паспорта (должен быть 10 цифр)
        String passport = passportField.getText().trim();
        if (!passport.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this,
                    "❌ Паспорт должен содержать ровно 10 цифр",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Создаем объект сотрудника С ПАСПОРТОМ
            Staff staff = new Staff(
                    passport,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    positionField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    hireDateField.getText().trim(),
                    Double.parseDouble(salaryField.getText().trim()),
                    departmentComboBox.getSelectedItem().toString()
            );

            // Пытаемся отправить на сервер
            if (staffService.addStaff(staff)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Сотрудник успешно добавлен!\n\n" +
                                "Паспорт: " + passport + "\n" +
                                "Данные отправлены на сервер.",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Ошибка при добавлении сотрудника\n\n" +
                                "Возможные причины:\n" +
                                "• Сотрудник с таким паспортом уже существует\n" +
                                "• Сервер недоступен\n" +
                                "• Проверьте корректность данных",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Зарплата должна быть числом (например: 50000.00)",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Неожиданная ошибка: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}