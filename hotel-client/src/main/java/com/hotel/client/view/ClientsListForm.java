package com.hotel.client.view;

import com.hotel.client.service.ApiService;
import com.hotel.client.model.Client;
import com.hotel.client.service.ClientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Форма для просмотра списка клиентов с градиентным дизайном
 */
public class ClientsListForm extends BaseTableForm {
    private static final Logger logger = LogManager.getLogger(ClientsListForm.class);

    private ApiService apiService;
    private ClientService clientService;

    public ClientsListForm(JFrame parent) {
        super(parent, "Список клиентов", 1200, 700);
        this.apiService = ApiService.getInstance();
        this.clientService = new ClientService(apiService);

        initializeComponents();
        setupBaseLayout("Список клиентов отеля",
                new Color(41, 128, 185),
                new Color(52, 152, 219));
        setupBaseListeners();
        loadData();
    }

    @Override
    protected void initializeTable() {
        // Убрали "Тип номера" из колонок
        String[] columns = {"Паспорт", "Имя", "Фамилия", "Телефон", "Email",
                "Дата заезда", "Дата выезда", "Номер", "Статус"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        applyTableStyles();

        // Настройка ширины колонок (убрали одну колонку)
        int[] columnWidths = {120, 100, 120, 130, 180, 110, 110, 80, 90};
        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        table.setDefaultRenderer(Object.class, new ClientCellRenderer());
    }

    @Override
    protected void loadData() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshButton.setEnabled(false);

            List<Client> clients = clientService.getAllClients();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            for (Client client : clients) {
                String status = getClientStatus(client);
                model.addRow(new Object[]{
                        client.getPassportNumber(),
                        client.getFirstName(),
                        client.getLastName(),
                        formatPhoneNumber(client.getPhoneNumber()),
                        client.getEmail(),
                        client.getCheckInDate(),
                        client.getCheckOutDate(),
                        client.getRoomNumber(),
                        // Убрали client.getRoomType() - его больше нет
                        status
                });
            }

            updateCountLabel(clients.size());

            if (clients.isEmpty()) {
                showEmptyDataMessage();
            }

        } catch (Exception e) {
            logger.error("Ошибка загрузки данных клиентов: {}", e.getMessage());
            showLoadingError("Ошибка загрузки данных: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshButton.setEnabled(true);
        }
    }

    @Override
    protected void setupAdditionalComponents() {
        // Дополнительные компоненты не требуются
    }

    private String getClientStatus(Client client) {
        if (client.getCheckInDate() == null || client.getCheckInDate().isEmpty()) {
            return "Не заселен";
        } else if (client.getCheckOutDate() == null || client.getCheckOutDate().isEmpty()) {
            return "Проживает";
        } else {
            return "Выселен";
        }
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return "Не указан";
        return phone.replaceFirst("(\\d{1})(\\d{3})(\\d{3})(\\d{2})(\\d{2})", "+$1 ($2) $3-$4-$5");
    }

    // Специальный рендерер для ячеек клиентов
    private static class ClientCellRenderer extends GradientCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Особые стили для статусов
            if (column == 9 && value != null) {
                String status = value.toString();
                if (!isSelected) {
                    if (status.equals("Проживает")) {
                        setBackground(new Color(230, 255, 230));
                        setForeground(new Color(39, 174, 96));
                    } else if (status.equals("Не заселен")) {
                        setBackground(new Color(255, 243, 205));
                        setForeground(new Color(255, 193, 7));
                    } else if (status.equals("Выселен")) {
                        setBackground(new Color(255, 230, 230));
                        setForeground(new Color(231, 76, 60));
                    }
                }
            }

            return this;
        }
    }
}