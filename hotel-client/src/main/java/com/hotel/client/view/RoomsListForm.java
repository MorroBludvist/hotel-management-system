package com.hotel.client.view;

import com.hotel.client.service.ApiService;
import com.hotel.client.model.Room;
import com.hotel.client.model.Client;
import com.hotel.client.service.RoomService;
import com.hotel.client.service.ClientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Форма для просмотра списка номеров с информацией о текущих клиентах
 */
public class RoomsListForm extends BaseTableForm {
    private static final Logger logger = LogManager.getLogger(RoomsListForm.class);

    private ApiService apiService;
    private RoomService roomService;
    private ClientService clientService;
    private JComboBox<String> filterComboBox;
    private JLabel statsLabel;

    // Карта для быстрого доступа к клиентам по номеру комнаты
    private Map<Integer, Client> roomToClientMap = new HashMap<>();

    public RoomsListForm(JFrame parent) {
        super(parent, "Список номеров отеля", 1000, 650);
        this.apiService = ApiService.getInstance();
        this.roomService = new RoomService(apiService);
        this.clientService = new ClientService(apiService);

        initializeComponents();
        setupRoomsLayout();
        setupBaseListeners();
        setupRoomsListeners();
        loadData();
    }

    @Override
    protected void initializeTable() {
        // 6 колонок: Номер, Тип, Статус, Клиент, Дата заезда, Дата выезда
        String[] columns = {"Номер", "Тип", "Статус", "Клиент", "Дата заезда", "Дата выезда"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        applyTableStyles();

        // Настройка ширины колонок для 6 колонок
        int[] columnWidths = {80, 120, 100, 150, 120, 120};
        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        // Специальный рендерер для номеров
        table.setDefaultRenderer(Object.class, new RoomCellRenderer());
    }

    @Override
    protected void loadData() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshButton.setEnabled(false);

            // Загружаем и комнаты, и клиентов
            List<Room> allRooms = roomService.getAllRooms();
            List<Client> allClients = clientService.getAllClients();

            // Создаем карту для быстрого поиска клиента по номеру комнаты
            updateRoomToClientMap(allClients);

            updateRoomsTable(allRooms);

            if (allRooms.isEmpty()) {
                showEmptyDataMessage();
            }

        } catch (Exception e) {
            logger.error("Ошибка загрузки данных номеров: {}", e.getMessage());
            showLoadingError("Ошибка загрузки данных: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshButton.setEnabled(true);
        }
    }

    /**
     * Обновляет карту соответствия комнат клиентам
     */
    private void updateRoomToClientMap(List<Client> clients) {
        roomToClientMap.clear();
        for (Client client : clients) {
            if (client.getRoomNumber() != null && "active".equals(client.getStatus())) {
                roomToClientMap.put(client.getRoomNumber(), client);
            }
        }
        logger.debug("Обновлена карта клиентов: {} активных клиентов", roomToClientMap.size());
    }

    @Override
    protected void setupAdditionalComponents() {
        // Комбобокс для фильтрации
        String[] filters = {"Все номера", "Свободные", "Занятые", "Эконом", "Стандарт", "Бизнес", "Люкс"};
        filterComboBox = new JComboBox<>(filters);
        styleComboBox(filterComboBox);

        // Статистика
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statsLabel.setForeground(new Color(100, 100, 100));
    }

    private void setupRoomsLayout() {
        // Убираем стандартную компоновку и создаем кастомную
        getContentPane().removeAll();

        // Градиентный заголовок с фильтром
        add(createRoomsHeaderPanel(), BorderLayout.NORTH);

        // Таблица
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Панель с статистикой и кнопками
        add(createRoomsBottomPanel(), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel createRoomsHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(39, 174, 96),
                        getWidth(), getHeight(), new Color(46, 204, 113));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Список номеров отеля");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("Фильтр:");
        filterLabel.setForeground(Color.WHITE);
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createRoomsBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 249, 250));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        // Статистика
        bottomPanel.add(statsLabel, BorderLayout.WEST);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.add(exportButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        return bottomPanel;
    }

    private void setupRoomsListeners() {
        filterComboBox.addActionListener(e -> applyFilter());
    }

    private void updateRoomsTable(List<Room> rooms) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Room room : rooms) {
            String status = "free".equals(room.getStatus()) ? "Свободен" : "Занят";

            // Получаем информацию о клиенте из карты
            Client client = roomToClientMap.get(room.getRoomNumber());
            String clientInfo = "-";
            String checkIn = "-";
            String checkOut = "-";

            if (client != null && "occupied".equals(room.getStatus())) {
                // Форматируем информацию о клиенте
                clientInfo = String.format("%s %s (%s)",
                        client.getFirstName(),
                        client.getLastName(),
                        client.getPassportNumber());
                checkIn = client.getCheckInDate() != null ? client.getCheckInDate() : "-";
                checkOut = client.getCheckOutDate() != null ? client.getCheckOutDate() : "-";
            }

            model.addRow(new Object[]{
                    room.getRoomNumber(),
                    room.getRoomType(),
                    status,
                    clientInfo,
                    checkIn,
                    checkOut
            });
        }

        updateStatistics(rooms);
    }

    private void updateStatistics(List<Room> rooms) {
        long totalRooms = rooms.size();
        long freeRooms = rooms.stream().filter(r -> "free".equals(r.getStatus())).count();
        long occupiedRooms = rooms.stream().filter(r -> "occupied".equals(r.getStatus())).count();
        long clientsInRooms = roomToClientMap.size();

        statsLabel.setText(String.format(
                "Всего номеров: %d | Свободно: %d | Занято: %d | Клиентов: %d",
                totalRooms, freeRooms, occupiedRooms, clientsInRooms
        ));
        updateCountLabel(rooms.size());
    }

    private void applyFilter() {
        try {
            String filter = (String) filterComboBox.getSelectedItem();
            List<Room> allRooms = roomService.getAllRooms();
            List<Room> filteredRooms;

            switch (filter) {
                case "Свободные":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "free".equals(room.getStatus()))
                            .collect(Collectors.toList());
                    break;
                case "Занятые":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "occupied".equals(room.getStatus()))
                            .collect(Collectors.toList());
                    break;
                case "Эконом":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "Эконом".equals(room.getRoomType()))
                            .collect(Collectors.toList());
                    break;
                case "Стандарт":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "Стандарт".equals(room.getRoomType()))
                            .collect(Collectors.toList());
                    break;
                case "Бизнес":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "Бизнес".equals(room.getRoomType()))
                            .collect(Collectors.toList());
                    break;
                case "Люкс":
                    filteredRooms = allRooms.stream()
                            .filter(room -> "Люкс".equals(room.getRoomType()))
                            .collect(Collectors.toList());
                    break;
                default:
                    filteredRooms = allRooms;
            }

            updateRoomsTable(filteredRooms);

        } catch (Exception e) {
            logger.error("Ошибка применения фильтра: {}", e.getMessage());
            showLoadingError("Ошибка применения фильтра: " + e.getMessage());
        }
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    // Специальный рендерер для номеров с улучшенным отображением клиентов
    private static class RoomCellRenderer extends GradientCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Особые стили для статусов
            if (column == 2 && value != null) {
                String status = value.toString();
                if (!isSelected) {
                    if (status.equals("Свободен")) {
                        setBackground(new Color(230, 255, 230));
                        setForeground(new Color(39, 174, 96));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (status.equals("Занят")) {
                        setBackground(new Color(255, 230, 230));
                        setForeground(new Color(231, 76, 60));
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
            }

            // Стили для информации о клиенте (колонка 3)
            if (column == 3) {
                if (value != null && !value.equals("-") && !isSelected) {
                    setForeground(new Color(52, 73, 94));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (value != null && value.equals("-") && !isSelected) {
                    setForeground(new Color(150, 150, 150));
                    setFont(getFont().deriveFont(Font.ITALIC));
                }
            }

            // Стили для дат
            if ((column == 4 || column == 5) && value != null && !value.equals("-") && !isSelected) {
                setForeground(new Color(41, 128, 185));
                setFont(getFont().deriveFont(Font.PLAIN));
            } else if ((column == 4 || column == 5) && value != null && value.equals("-") && !isSelected) {
                setForeground(new Color(150, 150, 150));
                setFont(getFont().deriveFont(Font.ITALIC));
            }

            // Выравнивание для номера комнаты
            if (column == 0) {
                setHorizontalAlignment(JLabel.CENTER);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }

            return this;
        }
    }

    /**
     * Переопределяем метод обновления данных для перезагрузки и клиентов
     */
    protected void refreshData() {
        loadData();
    }
}