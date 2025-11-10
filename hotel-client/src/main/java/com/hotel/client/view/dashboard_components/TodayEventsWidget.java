package com.hotel.client.view.dashboard_components;

import com.hotel.client.model.Client;
import com.hotel.client.model.Room;
import com.hotel.client.service.ClientService;
import com.hotel.client.service.RoomService;
import com.hotel.client.view.HotelAdminDashboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Виджет для отображения событий на сегодня (заезды и выезды)
 */
public class TodayEventsWidget extends BaseWidget {
    private static final Logger logger = LogManager.getLogger(TodayEventsWidget.class);

    private JLabel checkInLabel;
    private JLabel checkOutLabel;
    private JPanel checkInPanel;
    private JPanel checkOutPanel;

    private ClientService clientService;
    private RoomService roomService;

    // Карта для быстрого доступа к информации о комнатах
    private Map<Integer, Room> roomMap = new HashMap<>();

    public TodayEventsWidget(HotelAdminDashboard dashboard, ClientService clientService, RoomService roomService) {
        super(dashboard, "События на сегодня");
        this.clientService = clientService;
        this.roomService = roomService;

        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        setBackground(Color.WHITE);

        // Заголовки
        checkInLabel = new JLabel("Заезды (0)");
        checkInLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkInLabel.setForeground(new Color(39, 174, 96));

        checkOutLabel = new JLabel("Выезды (0)");
        checkOutLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkOutLabel.setForeground(new Color(231, 76, 60));

        // Панели для событий
        checkInPanel = new JPanel();
        checkInPanel.setLayout(new BoxLayout(checkInPanel, BoxLayout.Y_AXIS));
        checkInPanel.setBackground(Color.WHITE);

        checkOutPanel = new JPanel();
        checkOutPanel.setLayout(new BoxLayout(checkOutPanel, BoxLayout.Y_AXIS));
        checkOutPanel.setBackground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Заголовок виджета
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("События на сегодня");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        add(titleLabel, gbc);

        // Разделитель
        gbc.gridy = 1; gbc.gridwidth = 2;
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(200, 200, 200));
        add(separator, gbc);

        // Заезды
        gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        add(createEventsSection(checkInLabel, checkInPanel, new Color(39, 174, 96)), gbc);

        // Выезды
        gbc.gridx = 1;
        add(createEventsSection(checkOutLabel, checkOutPanel, new Color(231, 76, 60)), gbc);
    }

    private JPanel createEventsSection(JLabel title, JPanel eventsPanel, Color color) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок с иконкой
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);

        // Простая иконка (круг)
        JLabel icon = new JLabel("●");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        icon.setForeground(color);
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        headerPanel.add(icon);
        headerPanel.add(title);

        section.add(headerPanel, BorderLayout.NORTH);

        // Прокручиваемая панель событий
        JScrollPane scrollPane = new JScrollPane(eventsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Устанавливаем фиксированную высоту для прокрутки
        scrollPane.setPreferredSize(new Dimension(200, 150));

        section.add(scrollPane, BorderLayout.CENTER);

        return section;
    }

    /**
     * Загружает события для текущей даты
     */
    private void loadEvents() {
        try {
            // Получаем текущую дату из dashboard
            String currentDate = dashboard.getDateFormat().format(dashboard.getCurrentDate());

            // Загружаем комнаты для информации о типах
            List<Room> rooms = roomService.getAllRooms();
            roomMap.clear();
            for (Room room : rooms) {
                roomMap.put(room.getRoomNumber(), room);
            }

            // Загружаем клиентов
            List<Client> clients = clientService.getAllClients();

            // Очищаем панели
            checkInPanel.removeAll();
            checkOutPanel.removeAll();

            int checkInCount = 0;
            int checkOutCount = 0;

            // Фильтруем клиентов по датам
            for (Client client : clients) {
                // Проверяем заезды на сегодня
                if (currentDate.equals(client.getCheckInDate())) {
                    addCheckInEvent(client);
                    checkInCount++;
                }

                // Проверяем выезды на сегодня
                if (currentDate.equals(client.getCheckOutDate())) {
                    addCheckOutEvent(client);
                    checkOutCount++;
                }
            }

            // Обновляем счетчики
            checkInLabel.setText("Заезды (" + checkInCount + ")");
            checkOutLabel.setText("Выезды (" + checkOutCount + ")");

            // Если событий нет, показываем сообщение
            if (checkInCount == 0) {
                checkInPanel.add(createEmptyEventLabel("Нет запланированных заездов"));
            }
            if (checkOutCount == 0) {
                checkOutPanel.add(createEmptyEventLabel("Нет запланированных выездов"));
            }

            revalidate();
            repaint();

        } catch (Exception e) {
            logger.error("Ошибка загрузки событий: {}", e.getMessage());
            showErrorInPanel("Ошибка загрузки событий");
        }
    }

    private void addCheckInEvent(Client client) {
        JPanel eventPanel = createEventPanel(
                client.getFirstName() + " " + client.getLastName(),
                getRoomInfo(client.getRoomNumber()),
                "Заезд в " + client.getCheckInDate(),
                new Color(39, 174, 96)
        );
        checkInPanel.add(eventPanel);
    }

    private void addCheckOutEvent(Client client) {
        JPanel eventPanel = createEventPanel(
                client.getFirstName() + " " + client.getLastName(),
                getRoomInfo(client.getRoomNumber()),
                "Выезд в " + client.getCheckOutDate(),
                new Color(231, 76, 60)
        );
        checkOutPanel.add(eventPanel);
    }

    private String getRoomInfo(Integer roomNumber) {
        Room room = roomMap.get(roomNumber);
        if (room != null) {
            return "Номер " + roomNumber + " (" + room.getRoomType() + ")";
        }
        return "Номер " + roomNumber;
    }

    private JPanel createEventPanel(String clientName, String roomInfo, String timeInfo, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Имя клиента
        JLabel nameLabel = new JLabel(clientName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(new Color(52, 73, 94));

        // Информация о номере
        JLabel roomLabel = new JLabel(roomInfo);
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roomLabel.setForeground(new Color(100, 100, 100));

        // Время
        JLabel timeLabel = new JLabel(timeInfo);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(color);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(new Color(250, 250, 250));
        infoPanel.add(nameLabel);
        infoPanel.add(roomLabel);

        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(timeLabel, BorderLayout.EAST);

        return panel;
    }

    private JLabel createEmptyEventLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(new Color(150, 150, 150));
        label.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        return label;
    }

    private void showErrorInPanel(String message) {
        checkInPanel.removeAll();
        checkOutPanel.removeAll();

        JLabel errorLabel = new JLabel(message, JLabel.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(231, 76, 60));

        checkInPanel.add(errorLabel);
        checkOutPanel.add(errorLabel);

        revalidate();
        repaint();
    }

    /**
     * Реализация метода refreshData из BaseWidget
     */
    @Override
    public void refreshData() {
        loadEvents();
        logger.debug("Виджет событий обновлен");
    }
}