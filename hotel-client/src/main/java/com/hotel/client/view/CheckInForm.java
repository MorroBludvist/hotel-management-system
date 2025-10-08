package com.hotel.client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.hotel.client.service.DatabaseManager;
import com.hotel.client.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Форма для заселения клиента с валидацией дат и номеров
 */
public class CheckInForm extends JDialog {
    private JTextField passportField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField checkInDateField;
    private JTextField checkOutDateField;
    private JComboBox<Integer> roomNumberComboBox;
    private JComboBox<String> roomTypeComboBox;
    private JButton checkAvailabilityButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel availabilityLabel;

    private DatabaseManager dbManager;
    private String currentDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public CheckInForm(JFrame parent, String currentDate) {
        super(parent, "Заселение клиента", true);
        this.dbManager = DatabaseManager.getInstance();
        this.currentDate = currentDate;
        initializeComponents();
        setupLayout();
        setupListeners();
        loadAvailableRooms();
        pack();
        setLocationRelativeTo(parent);
        setSize(500, 550);
    }

    private void initializeComponents() {
        passportField = new JTextField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);

        // Устанавливаем текущую дату как дату заезда
        checkInDateField = new JTextField(currentDate, 20);
        checkOutDateField = new JTextField(20);

        // Комбобоксы для номеров и типов
        roomTypeComboBox = new JComboBox<>(new String[]{"Эконом", "Стандарт", "Бизнес", "Люкс"});
        roomNumberComboBox = new JComboBox<>();

        checkAvailabilityButton = new JButton("Проверить доступность");
        checkAvailabilityButton.setBackground(new Color(255, 165, 0));
        checkAvailabilityButton.setForeground(Color.WHITE);

        saveButton = new JButton("Заселить");
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.WHITE);

        cancelButton = new JButton("Отмена");

        availabilityLabel = new JLabel("Выберите номер и даты для проверки");
        availabilityLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        // Устанавливаем подсказки
        checkInDateField.setToolTipText("Дата заезда (не может быть раньше сегодняшней)");
        checkOutDateField.setToolTipText("Дата выезда (должна быть после даты заезда)");
        passportField.setToolTipText("Серия и номер паспорта (10 цифр)");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Заголовок
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Информация о клиенте");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(70, 130, 180));
        fieldsPanel.add(titleLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 2;
        fieldsPanel.add(Box.createVerticalStrut(10), gbc);

        // Поля формы
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        fieldsPanel.add(new JLabel("Паспорт*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(passportField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        fieldsPanel.add(new JLabel("Имя*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(firstNameField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; fieldsPanel.add(new JLabel("Фамилия*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(lastNameField, gbc);

        gbc.gridy = 5; gbc.gridx = 0; fieldsPanel.add(new JLabel("Телефон:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(phoneField, gbc);

        gbc.gridy = 6; gbc.gridx = 0; fieldsPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(emailField, gbc);

        gbc.gridy = 7; gbc.gridx = 0; fieldsPanel.add(new JLabel("Дата заезда*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(checkInDateField, gbc);

        gbc.gridy = 8; gbc.gridx = 0; fieldsPanel.add(new JLabel("Дата выезда*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(checkOutDateField, gbc);

        gbc.gridy = 9; gbc.gridx = 0; fieldsPanel.add(new JLabel("Тип номера*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(roomTypeComboBox, gbc);

        gbc.gridy = 10; gbc.gridx = 0; fieldsPanel.add(new JLabel("Номер*:"), gbc);
        gbc.gridx = 1; fieldsPanel.add(roomNumberComboBox, gbc);

        gbc.gridy = 11; gbc.gridx = 0; gbc.gridwidth = 2;
        fieldsPanel.add(checkAvailabilityButton, gbc);

        gbc.gridy = 12; gbc.gridx = 0; gbc.gridwidth = 2;
        fieldsPanel.add(availabilityLabel, gbc);

        // Подпись обязательных полей
        gbc.gridy = 13; gbc.gridx = 0; gbc.gridwidth = 2;
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
        roomTypeComboBox.addActionListener(e -> loadAvailableRooms());
        checkAvailabilityButton.addActionListener(e -> checkRoomAvailability());
        saveButton.addActionListener(e -> saveClient());
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadAvailableRooms() {
        try {
            String roomType = (String) roomTypeComboBox.getSelectedItem();
            List<Room> freeRooms = dbManager.getFreeRooms();

            // Фильтруем по типу
            List<Room> filteredRooms = freeRooms.stream()
                    .filter(room -> roomType.equals(room.getRoomType()))
                    .collect(Collectors.toList());

            roomNumberComboBox.removeAllItems();
            for (Room room : filteredRooms) {
                roomNumberComboBox.addItem(room.getRoomNumber());
            }

            if (filteredRooms.isEmpty()) {
                availabilityLabel.setText("❌ Нет свободных номеров выбранного типа");
                availabilityLabel.setForeground(Color.RED);
            } else {
                availabilityLabel.setText("✅ Доступно номеров: " + filteredRooms.size());
                availabilityLabel.setForeground(new Color(0, 100, 0));
            }

        } catch (Exception e) {
            availabilityLabel.setText("❌ Ошибка загрузки номеров");
            availabilityLabel.setForeground(Color.RED);
        }
    }

    private void checkRoomAvailability() {
        try {
            if (roomNumberComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "❌ Сначала выберите номер",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Integer roomNumber = (Integer) roomNumberComboBox.getSelectedItem();
            String checkInDate = checkInDateField.getText().trim();
            String checkOutDate = checkOutDateField.getText().trim();

            // Валидация дат
            if (!validateDates(checkInDate, checkOutDate)) {
                return;
            }

            boolean available = dbManager.isRoomAvailable(roomNumber, checkInDate, checkOutDate);

            if (available) {
                availabilityLabel.setText("✅ Номер доступен в указанные даты");
                availabilityLabel.setForeground(new Color(0, 100, 0));
            } else {
                availabilityLabel.setText("❌ Номер занят в указанные даты");
                availabilityLabel.setForeground(Color.RED);
            }

        } catch (Exception e) {
            availabilityLabel.setText("❌ Ошибка проверки доступности");
            availabilityLabel.setForeground(Color.RED);
        }
    }

    private boolean validateDates(String checkInDate, String checkOutDate) {
        try {
            // Проверяем формат дат
            Date checkIn = dateFormat.parse(checkInDate);
            Date checkOut = dateFormat.parse(checkOutDate);
            Date today = dateFormat.parse(currentDate);

            // Дата заезда не может быть раньше сегодняшней
            if (checkIn.before(today)) {
                JOptionPane.showMessageDialog(this,
                        "❌ Дата заезда не может быть раньше сегодняшней (" + currentDate + ")",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Дата выезда должна быть после даты заезда
            if (!checkOut.after(checkIn)) {
                JOptionPane.showMessageDialog(this,
                        "❌ Дата выезда должна быть после даты заезда",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Неверный формат даты. Используйте ГГГГ-ММ-ДД",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void saveClient() {
        // Валидация обязательных полей
        if (passportField.getText().trim().isEmpty() ||
                firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                checkInDateField.getText().trim().isEmpty() ||
                checkOutDateField.getText().trim().isEmpty() ||
                roomNumberComboBox.getSelectedItem() == null) {

            JOptionPane.showMessageDialog(this,
                    "Пожалуйста, заполните все обязательные поля (отмечены *)",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Валидация паспорта
        String passport = passportField.getText().trim();
        if (!passport.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this,
                    "❌ Паспорт должен содержать ровно 10 цифр",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Валидация дат
        String checkInDate = checkInDateField.getText().trim();
        String checkOutDate = checkOutDateField.getText().trim();
        if (!validateDates(checkInDate, checkOutDate)) {
            return;
        }

        try {
            // Создаем объект клиента
            Client client = new Client(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    passport,
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    checkInDate,
                    checkOutDate,
                    (Integer) roomNumberComboBox.getSelectedItem(),
                    (String) roomTypeComboBox.getSelectedItem()
            );

            // Пытаемся отправить на сервер
            if (dbManager.addClient(client)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Клиент успешно заселен!\n\n" +
                                "Номер: " + client.getRoomNumber() + "\n" +
                                "С " + checkInDate + " по " + checkOutDate,
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Ошибка при заселении клиента\n\n" +
                                "Возможные причины:\n" +
                                "• Клиент с таким паспортом уже существует\n" +
                                "• Номер стал занят в указанные даты\n" +
                                "• Сервер недоступен",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Неожиданная ошибка: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}