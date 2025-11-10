package com.hotel.client.view;

import com.hotel.client.service.ApiService;
import com.hotel.client.model.Client;
import com.hotel.client.model.Room;
import com.hotel.client.service.BookingService;
import com.hotel.client.service.ClientService;
import com.hotel.client.service.RoomService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Форма для заселения клиента с градиентным дизайном и скроллингом
 */
public class CheckInForm extends BaseAddForm {
    private static final Logger logger = LogManager.getLogger(CheckInForm.class);

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
    private JLabel availabilityLabel;

    private ApiService apiService;
    private ClientService clientService;
    private RoomService roomService;

    private String currentDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public CheckInForm(JFrame parent, String currentDate) {
        super(parent, "Заселение клиента", 500, 600);
        this.apiService = ApiService.getInstance();
        this.clientService = new ClientService(apiService);
        this.roomService = new RoomService(apiService);
        this.currentDate = currentDate;

        initializeComponents();
        setupBaseLayout("Заселение клиента",
                new Color(41, 128, 185),
                new Color(52, 152, 219));
        setupListeners();
        loadAvailableRooms();
    }

    @Override
    protected void initializeComponents() {
        initializeBaseComponents();

        passportField = createStyledTextField();
        firstNameField = createStyledTextField();
        lastNameField = createStyledTextField();
        phoneField = createStyledTextField();
        emailField = createStyledTextField();
        checkInDateField = createStyledTextField();
        checkOutDateField = createStyledTextField();

        // Устанавливаем текущую дату как дату заезда
        checkInDateField.setText(currentDate);

        roomTypeComboBox = createStyledComboBox(new String[]{"Эконом", "Стандарт", "Бизнес", "Люкс"});
        roomNumberComboBox = createStyledComboBox();

        checkAvailabilityButton = createGradientButton("Проверить доступность",
                new Color(52, 152, 219), new Color(41, 128, 185));

        availabilityLabel = new JLabel("Выберите номер и даты для проверки доступности");
        availabilityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        availabilityLabel.setForeground(new Color(100, 100, 100));

        // Устанавливаем подсказки
        passportField.setToolTipText("Серия и номер паспорта (10 цифр)");
        checkInDateField.setToolTipText("Дата заезда (не может быть раньше сегодняшней)");
        checkOutDateField.setToolTipText("Дата выезда (должна быть после даты заезда)");
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

        // Информация о клиенте
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel clientLabel = new JLabel("Информация о клиенте");
        clientLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clientLabel.setForeground(new Color(52, 73, 94));
        panel.add(clientLabel, gbc);

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

        // Даты проживания
        gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel datesLabel = new JLabel("Даты проживания");
        datesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        datesLabel.setForeground(new Color(52, 73, 94));
        panel.add(datesLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Дата заезда *"), gbc);
        gbc.gridx = 1;
        panel.add(checkInDateField, gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Дата выезда *"), gbc);
        gbc.gridx = 1;
        panel.add(checkOutDateField, gbc);

        // Разделитель
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(createSeparator(), gbc);

        // Выбор номера
        gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel roomLabel = new JLabel("Выбор номера");
        roomLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roomLabel.setForeground(new Color(52, 73, 94));
        panel.add(roomLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Тип номера *"), gbc);
        gbc.gridx = 1;
        panel.add(roomTypeComboBox, gbc);

        gbc.gridy = row++; gbc.gridx = 0;
        panel.add(createStyledLabel("Номер *"), gbc);
        gbc.gridx = 1;
        panel.add(roomNumberComboBox, gbc);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(checkAvailabilityButton, gbc);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(availabilityLabel, gbc);

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
        roomTypeComboBox.addActionListener(e -> loadAvailableRooms());

        checkAvailabilityButton.addActionListener(e -> checkRoomAvailability());
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
        requiredFields.add(checkInDateField);
        requiredFields.add(checkOutDateField);

        if (!validateRequiredFields(requiredFields.toArray(new JTextField[0]))) {
            return false;
        }

        if (roomNumberComboBox.getSelectedItem() == null) {
            showError("Пожалуйста, выберите номер");
            return false;
        }

        if (!validatePassport(passportField.getText().trim())) {
            return false;
        }

        String checkInDate = checkInDateField.getText().trim();
        String checkOutDate = checkOutDateField.getText().trim();
        return validateDates(checkInDate, checkOutDate);
    }

    @Override
    //TODO: вынести booking service в поля класса
    protected void saveData() {
        try {
            Client client = new Client(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    passportField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    checkInDateField.getText().trim(),
                    checkOutDateField.getText().trim(),
                    (Integer) roomNumberComboBox.getSelectedItem()
                    // Убрали roomType - он больше не нужен в клиенте
            );

            if (clientService.checkInClient(client)) {
                showSuccess("Клиент успешно заселен!\n\n" +
                        "Номер: " + client.getRoomNumber() + "\n" +
                        "С " + client.getCheckInDate() + " по " + client.getCheckOutDate());
                dispose();
            } else {
                showError("Ошибка при заселении клиента");
            }
        } catch (Exception e) {
            logger.error("Ошибка сохранения клиента: {}", e.getMessage());
            showError("Неожиданная ошибка: " + e.getMessage());
        }
    }

    private void loadAvailableRooms() {
        try {
            String roomType = (String) roomTypeComboBox.getSelectedItem();

            // Получаем все номера выбранного типа (только базовую информацию)
            List<Room> allRooms = roomService.getAllRooms();
            List<Room> filteredRooms = allRooms.stream()
                    .filter(room -> roomType.equals(room.getRoomType()))
                    .collect(Collectors.toList());

            roomNumberComboBox.removeAllItems();

            if (filteredRooms.isEmpty()) {
                availabilityLabel.setText("Нет номеров выбранного типа");
                availabilityLabel.setForeground(new Color(231, 76, 60));
                return;
            }

            // Добавляем все номера в комбобокс
            for (Room room : filteredRooms) {
                roomNumberComboBox.addItem(room.getRoomNumber());
            }

            // Показываем только общую информацию без проверки доступности
            availabilityLabel.setText("Доступно номеров: " + filteredRooms.size() + " - нажмите 'Проверить доступность'");
            availabilityLabel.setForeground(new Color(243, 156, 18));

        } catch (Exception e) {
            logger.error("Ошибка загрузки номеров: {}", e.getMessage());
            availabilityLabel.setText("Ошибка загрузки номеров");
            availabilityLabel.setForeground(new Color(231, 76, 60));
            roomNumberComboBox.removeAllItems();
        }
    }

    private void checkRoomAvailability() {
        try {
            if (roomNumberComboBox.getSelectedItem() == null) {
                showError("Сначала выберите номер");
                return;
            }

            Integer roomNumber = (Integer) roomNumberComboBox.getSelectedItem();
            String checkInDate = checkInDateField.getText().trim();
            String checkOutDate = checkOutDateField.getText().trim();

            if (!validateDates(checkInDate, checkOutDate)) {
                return;
            }

            // Показываем индикатор загрузки
            availabilityLabel.setText("Проверяем доступность...");
            availabilityLabel.setForeground(new Color(243, 156, 18));
            checkAvailabilityButton.setEnabled(false);

            // Запускаем проверку в отдельном потоке, чтобы не блокировать UI
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return roomService.isRoomAvailable(roomNumber, checkInDate, checkOutDate);
                }

                @Override
                protected void done() {
                    try {
                        boolean available = get();

                        if (available) {
                            availabilityLabel.setText("✓ Номер " + roomNumber + " доступен в указанные даты");
                            availabilityLabel.setForeground(new Color(39, 174, 96));
                            JOptionPane.showMessageDialog(CheckInForm.this,
                                    "Номер " + roomNumber + " доступен для заселения!\n" +
                                            "С " + checkInDate + " по " + checkOutDate,
                                    "Проверка доступности",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            availabilityLabel.setText("✗ Номер " + roomNumber + " занят в указанные даты");
                            availabilityLabel.setForeground(new Color(231, 76, 60));
                            JOptionPane.showMessageDialog(CheckInForm.this,
                                    "Номер " + roomNumber + " занят в выбранные даты.\n" +
                                            "Пожалуйста, выберите другие даты или другой номер.",
                                    "Номер занят",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        logger.error("Ошибка проверки доступности: {}", e.getMessage());
                        availabilityLabel.setText("Ошибка проверки доступности");
                        availabilityLabel.setForeground(new Color(231, 76, 60));
                        showError("Ошибка при проверке доступности: " + e.getMessage());
                    } finally {
                        checkAvailabilityButton.setEnabled(true);
                    }
                }
            }.execute();

        } catch (Exception e) {
            logger.error("Ошибка проверки доступности: {}", e.getMessage());
            availabilityLabel.setText("Ошибка проверки доступности");
            availabilityLabel.setForeground(new Color(231, 76, 60));
            showError("Ошибка при проверке доступности: " + e.getMessage());
            checkAvailabilityButton.setEnabled(true);
        }
    }

    private boolean validateDates(String checkInDate, String checkOutDate) {
        try {
            if (checkInDate.isEmpty() || checkOutDate.isEmpty()) {
                showError("Пожалуйста, заполните обе даты");
                return false;
            }

            Date checkIn = dateFormat.parse(checkInDate);
            Date checkOut = dateFormat.parse(checkOutDate);
            Date today = dateFormat.parse(currentDate);

            if (checkIn.before(today)) {
                showError("Дата заезда не может быть раньше сегодняшней (" + currentDate + ")");
                return false;
            }

            if (!checkOut.after(checkIn)) {
                showError("Дата выезда должна быть после даты заезда");
                return false;
            }

            return true;

        } catch (Exception e) {
            showError("Неверный формат даты. Используйте ГГГГ-ММ-ДД");
            return false;
        }
    }

    public boolean validatePassport(String passport) {
        if (passport == null || passport.trim().isEmpty()) {
            showError("Паспортные данные обязательны для заполнения");
            return false;
        }

        // Простая проверка - паспорт должен содержать только цифры и быть длиной 10 символов
        if (!passport.matches("\\d{10}")) {
            showError("Паспорт должен содержать 10 цифр");
            return false;
        }

        return true;
    }

    // Вспомогательные методы для создания UI компонентов
    public JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }

    public JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(200, 200, 200));
        separator.setBackground(new Color(200, 200, 200));
        return separator;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Успех", JOptionPane.INFORMATION_MESSAGE);
    }
}