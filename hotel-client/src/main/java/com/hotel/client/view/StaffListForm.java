package com.hotel.client.view;

import com.hotel.client.service.ApiService;
import com.hotel.client.model.Staff;
import com.hotel.client.service.StaffService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Форма для просмотра списка сотрудников с градиентным дизайном
 */
public class StaffListForm extends BaseTableForm {
    private static final Logger logger = LogManager.getLogger(StaffListForm.class);

    private ApiService apiService;
    private StaffService staffService;

    public StaffListForm(JFrame parent) {
        super(parent, "Список сотрудников", 1300, 700);
        this.apiService = ApiService.getInstance();
        this.staffService = new StaffService(apiService);

        initializeComponents();
        setupBaseLayout("Список сотрудников отеля",
                new Color(230, 126, 34),
                new Color(211, 84, 0));
        setupBaseListeners();
        loadData();
    }

    @Override
    protected void initializeTable() {
        String[] columns = {"Паспорт", "Имя", "Фамилия", "Должность", "Телефон", "Email",
                "Дата найма", "Зарплата", "Отдел", "Статус"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        applyTableStyles();

        // Настройка ширины колонок
        int[] columnWidths = {120, 90, 110, 130, 120, 180, 100, 110, 100, 80};
        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        // Специальный рендерер для сотрудников
        table.setDefaultRenderer(Object.class, new StaffCellRenderer());
    }

    @Override
    protected void loadData() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshButton.setEnabled(false);

            List<Staff> staffList = staffService.getAllStaff();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            for (Staff staff : staffList) {
                model.addRow(new Object[]{
                        staff.getPassportNumber(),
                        staff.getFirstName(),
                        staff.getLastName(),
                        staff.getPosition(),
                        formatPhoneNumber(staff.getPhoneNumber()),
                        staff.getEmail(),
                        staff.getHireDate(),
                        String.format("%,d руб.", (int) staff.getSalary()),
                        staff.getDepartment(),
                        "Активен"
                });
            }

            updateCountLabel(staffList.size());

            if (staffList.isEmpty()) {
                showEmptyDataMessage();
            }

        } catch (Exception e) {
            logger.error("Ошибка загрузки данных сотрудников: {}", e.getMessage());
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

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return "Не указан";
        return phone.replaceFirst("(\\d{1})(\\d{3})(\\d{3})(\\d{2})(\\d{2})", "+$1 ($2) $3-$4-$5");
    }

    // Специальный рендерер для ячеек сотрудников
    private static class StaffCellRenderer extends GradientCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Особые стили для статусов
            if (column == 9 && value != null) {
                String status = value.toString();
                if (!isSelected) {
                    if (status.equals("Активен")) {
                        setBackground(new Color(230, 255, 230));
                        setForeground(new Color(39, 174, 96));
                    } else {
                        setBackground(new Color(255, 230, 230));
                        setForeground(new Color(231, 76, 60));
                    }
                }
            }

            // Выравнивание для зарплаты
            if (column == 7) {
                setHorizontalAlignment(JLabel.RIGHT);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
            }

            return this;
        }
    }
}