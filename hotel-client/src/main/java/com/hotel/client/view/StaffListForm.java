package com.hotel.client.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import com.hotel.client.service.DatabaseManager;
import com.hotel.client.model.Staff;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Форма для просмотра списка сотрудников.
 */
public class StaffListForm extends JDialog {
    private JTable staffTable;
    private JButton refreshButton;
    private JButton closeButton;
    private DatabaseManager dbManager;

    public StaffListForm(JFrame parent) {
        super(parent, "Список сотрудников", true);
        this.dbManager = DatabaseManager.getInstance();
        initializeComponents();
        setupLayout();
        setupListeners();
        loadStaffData();
        pack();
        setLocationRelativeTo(parent);
        setSize(900, 500);
    }

    private void initializeComponents() {
        // Создаем модель таблицы
        String[] columns = {"Паспорт", "Имя", "Фамилия", "Должность", "Телефон", "Email",
                "Дата найма", "Зарплата", "Отдел"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        staffTable = new JTable(model);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        staffTable.getTableHeader().setReorderingAllowed(false);

        // Настраиваем отображение таблицы
        staffTable.setRowHeight(25);
        staffTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        staffTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // Имя
        staffTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Фамилия
        staffTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Должность
        staffTable.getColumnModel().getColumn(7).setPreferredWidth(80);   // Зарплата

        refreshButton = new JButton("Обновить");
        refreshButton.setBackground(new Color(210, 105, 30));
        refreshButton.setForeground(Color.WHITE);

        closeButton = new JButton("Закрыть");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Заголовок
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Список сотрудников отеля");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(210, 105, 30));

        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel countLabel = new JLabel("Всего сотрудников: 0");
        countLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(countLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Таблица с прокруткой
        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadStaffData();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadStaffData() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshButton.setEnabled(false);

            List<Staff> staffList = dbManager.getAllStaff();

            DefaultTableModel model = (DefaultTableModel) staffTable.getModel();
            model.setRowCount(0);

            for (Staff staff : staffList) {
                model.addRow(new Object[]{
                        staff.getPassportNumber(),  // ← показываем паспорт вместо ID
                        staff.getFirstName(),
                        staff.getLastName(),
                        staff.getPosition(),
                        staff.getPhoneNumber(),
                        staff.getEmail(),
                        staff.getHireDate(),
                        String.format("%.2f руб.", staff.getSalary()),
                        staff.getDepartment()
                });
            }

            updateStaffCount(staffList.size());

            if (staffList.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Нет данных о сотрудниках.\n\n" +
                                "Возможные причины:\n" +
                                "• Сервер недоступен\n" +
                                "• Нет сотрудников в базе данных\n" +
                                "• Ошибка соединения",
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ошибка загрузки данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshButton.setEnabled(true);
        }
    }

    private void updateStaffCount(int count) {
        Component[] components = ((JPanel)getContentPane().getComponent(0)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] headerComps = ((JPanel)comp).getComponents();
                for (Component headerComp : headerComps) {
                    if (headerComp instanceof JLabel && headerComp != ((JPanel)comp).getComponent(0)) {
                        ((JLabel)headerComp).setText("Всего сотрудников: " + count);
                        return;
                    }
                }
            }
        }
    }
}