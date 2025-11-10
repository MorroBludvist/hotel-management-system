package com.hotel.client.view;

import com.hotel.client.service.ApiService;
import com.hotel.client.service.BookingService;
import com.hotel.client.service.RoomService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * Форма для просмотра истории бронирований
 */
public class BookingHistoryForm extends BaseTableForm {
    private static final Logger logger = LogManager.getLogger(BookingHistoryForm.class);

    private ApiService apiService;
    private RoomService roomService;
    private BookingService bookingService;
    private JComboBox<String> filterComboBox;

    public BookingHistoryForm(JFrame parent) {
        super(parent, "История бронирований", 1000, 600);
        this.apiService = ApiService.getInstance();
        this.roomService = new RoomService(apiService);
        this.bookingService = new BookingService(apiService);

        initializeComponents();
        setupBookingHistoryLayout();
        setupBaseListeners();
        setupBookingHistoryListeners();
        loadData();
    }

    @Override
    protected void initializeTable() {
        String[] columns = {"Номер", "Паспорт клиента", "Дата заезда", "Дата выезда", "Дата бронирования"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        applyTableStyles();

        // Настройка ширины колонок
        int[] columnWidths = {80, 150, 120, 120, 180};
        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
    }

    @Override
    protected void loadData() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshButton.setEnabled(false);

            List<Map<String, Object>> history = bookingService.getAllBookingHistory();
            updateBookingHistoryTable(history);

            if (history.isEmpty()) {
                showEmptyDataMessage();
            }

        } catch (Exception e) {
            logger.error("Ошибка загрузки истории бронирований: {}", e.getMessage());
            showLoadingError("Ошибка загрузки данных: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshButton.setEnabled(true);
        }
    }

    @Override
    protected void setupAdditionalComponents() {
        // Комбобокс для фильтрации
        String[] filters = {"Все записи", "За последнюю неделю", "За последний месяц", "За последний год"};
        filterComboBox = new JComboBox<>(filters);
        styleComboBox(filterComboBox);
    }

    private void setupBookingHistoryLayout() {
        // Убираем стандартную компоновку и создаем кастомную
        getContentPane().removeAll();

        // Градиентный заголовок с фильтром
        add(createBookingHistoryHeaderPanel(), BorderLayout.NORTH);

        // Таблица
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Панель с информацией и кнопками
        add(createBookingHistoryBottomPanel(), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel createBookingHistoryHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(155, 89, 182),
                        getWidth(), getHeight(), new Color(142, 68, 173));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("История бронирований отеля");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("Период:");
        filterLabel.setForeground(Color.WHITE);
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createBookingHistoryBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 249, 250));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        // Информация
        JLabel infoLabel = new JLabel("История всех бронирований номеров отеля");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        bottomPanel.add(infoLabel, BorderLayout.WEST);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.add(exportButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        return bottomPanel;
    }

    private void setupBookingHistoryListeners() {
        filterComboBox.addActionListener(e -> applyFilter());
    }

    private void updateBookingHistoryTable(List<Map<String, Object>> history) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        for (Map<String, Object> record : history) {
            String bookedAt = formatTimestamp(record.get("bookedAt"), dateFormat);

            model.addRow(new Object[]{
                    record.get("roomNumber"),
                    record.get("clientPassport"),
                    record.get("checkInDate"),
                    record.get("checkOutDate"),
                    bookedAt
            });
        }

        updateCountLabel(history.size());
    }

    private String formatTimestamp(Object timestamp, SimpleDateFormat dateFormat) {
        if (timestamp == null) return "-";
        try {
            if (timestamp instanceof java.sql.Timestamp) {
                return dateFormat.format((java.sql.Timestamp) timestamp);
            } else if (timestamp instanceof String) {
                return (String) timestamp;
            }
        } catch (Exception e) {
            logger.warn("Ошибка форматирования даты: {}", e.getMessage());
        }
        return String.valueOf(timestamp);
    }

    private void applyFilter() {
        // TODO: Реализовать фильтрацию по периодам
        // Пока просто перезагружаем все данные
        loadData();
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
}