package com.hotel.client.view;

import com.hotel.client.service.ApiService;
import com.hotel.client.model.Staff;
import com.hotel.client.service.StaffService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Форма для добавления сотрудника с градиентным дизайном и скроллингом
 */
public class AddStaffForm extends BaseAddForm {
    private static final Logger logger = LogManager.getLogger(AddStaffForm.class);

    private JTextField passportField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField positionField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField hireDateField;
    private JTextField salaryField;
    private JComboBox<String> departmentComboBox;

    private ApiService apiService;
    private StaffService staffService;

    public AddStaffForm(JFrame parent) {
        super(parent, "Добавление сотрудника", 500, 550); // Уменьшил высоту, т.к. теперь есть скролл
        this.apiService = ApiService.getInstance();
        this.staffService = new StaffService(apiService);

        initializeComponents();
        setupBaseLayout("Добавление сотрудника",
                new Color(230, 126, 34),
                new Color(211, 84, 0));
        setupListeners();
    }

    @Override
    protected void initializeComponents() {
        initializeBaseComponents();

        passportField = createStyledTextField();
        firstNameField = createStyledTextField();
        lastNameField = createStyledTextField();
        positionField = createStyledTextField();
        phoneField = createStyledTextField();
        emailField = createStyledTextField();
        hireDateField = createStyledTextField();
        salaryField = createStyledTextField();

        String[] departments = {"Администрация", "Обслуживание", "Кухня", "Уборка", "Безопасность", "IT"};
        departmentComboBox = createStyledComboBox(departments);

        // Устанавливаем подсказки
        passportField.setToolTipText("Серия и номер паспорта (10 цифр)");
        hireDateField.setToolTipText("Формат: ГГГГ-ММ-ДД (например: 2024-01-20)");
        salaryField.setToolTipText("Например: 50000.00");
        positionField.setToolTipText("Например: Администратор, Горничная, Повар");
    }

    @Override
    protected JPanel createFieldsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        int row = 0;

        // Заголовок раздела
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel sectionLabel = new JLabel("Основная информация");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sectionLabel.setForeground(new Color(52, 73, 94));
        panel.add(sectionLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Паспорт *"), gbc);
        gbc.gridx = 1;
        panel.add(passportField, gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Имя *"), gbc);
        gbc.gridx = 1;
        panel.add(firstNameField, gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Фамилия *"), gbc);
        gbc.gridx = 1;
        panel.add(lastNameField, gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Должность *"), gbc);
        gbc.gridx = 1;
        panel.add(positionField, gbc);

        // Разделитель
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(createSeparator(), gbc);

        // Контактная информация
        gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel contactLabel = new JLabel("Контактная информация");
        contactLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contactLabel.setForeground(new Color(52, 73, 94));
        panel.add(contactLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Телефон"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Email"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Разделитель
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(createSeparator(), gbc);

        // Рабочая информация
        gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel workLabel = new JLabel("Рабочая информация");
        workLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        workLabel.setForeground(new Color(52, 73, 94));
        panel.add(workLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Дата найма *"), gbc);
        gbc.gridx = 1;
        panel.add(hireDateField, gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Зарплата *"), gbc);
        gbc.gridx = 1;
        panel.add(salaryField, gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Отдел *"), gbc);
        gbc.gridx = 1;
        panel.add(departmentComboBox, gbc);

        // Подпись обязательных полей
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel requiredLabel = new JLabel("* - обязательные поля");
        requiredLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        requiredLabel.setForeground(new Color(150, 150, 150));
        panel.add(requiredLabel, gbc);

        // Пустое пространство внизу
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);

        return panel;
    }

    @Override
    protected void setupListeners() {
        saveButton.addActionListener(e -> {
            if (validateForm()) {
                saveData();
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }

    @Override
    protected boolean validateForm() {
        List<JTextField> requiredFields = new ArrayList<>();
        requiredFields.add(passportField);
        requiredFields.add(firstNameField);
        requiredFields.add(lastNameField);
        requiredFields.add(positionField);
        requiredFields.add(hireDateField);
        requiredFields.add(salaryField);

        if (!validateRequiredFields(requiredFields.toArray(new JTextField[0]))) {
            return false;
        }

        if (!validatePassport(passportField.getText().trim())) {
            return false;
        }

        try {
            Double.parseDouble(salaryField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Зарплата должна быть числом (например: 50000.00)");
            return false;
        }

        return true;
    }

    @Override
    protected void saveData() {
        try {
            Staff staff = new Staff(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    passportField.getText().trim(),
                    positionField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    hireDateField.getText().trim(),
                    Double.parseDouble(salaryField.getText().trim()),
                    departmentComboBox.getSelectedItem().toString()
            );

            if (staffService.addStaff(staff)) {
                showSuccess("Сотрудник успешно добавлен!\n\n" +
                        "Паспорт: " + staff.getPassportNumber() + "\n" +
                        "Должность: " + staff.getPosition());
                dispose();
            } else {
                showError("Ошибка при добавлении сотрудника\n\n" +
                        "Возможные причины:\n" +
                        "• Сотрудник с таким паспортом уже существует\n" +
                        "• Сервер недоступен");
            }
        } catch (Exception e) {
            logger.error("Ошибка сохранения сотрудника: {}", e.getMessage());
            showError("Неожиданная ошибка: " + e.getMessage());
        }
    }
}