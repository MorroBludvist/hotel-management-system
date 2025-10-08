package com.hotel.client.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import com.hotel.client.model.Client;
import com.hotel.client.service.ClientService;
import com.hotel.client.service.DatabaseManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Форма для просмотра списка клиентов.
 */
public class ClientsListForm extends JDialog {
    private JTable clientsTable;
    private JButton refreshButton;
    private JButton closeButton;
    private DatabaseManager dbManager;
    private ClientService clientService;

    public ClientsListForm(JFrame parent) {
        super(parent, "Список клиентов", true);
        this.dbManager = DatabaseManager.getInstance();
        this.clientService = ClientService.getInstanse();
        initializeComponents();
        setupLayout();
        setupListeners();
        loadClientsData();
        pack();
        setLocationRelativeTo(parent);
        setSize(800, 500);
    }

    private void initializeComponents() {
        // Создаем модель таблицы
        String[] columns = {"Паспорт", "Имя", "Фамилия", "Телефон", "Email",
                "Заезд", "Выезд", "Номер", "Тип номера"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование
            }
        };

        clientsTable = new JTable(model);
        clientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientsTable.getTableHeader().setReorderingAllowed(false);

        // Настраиваем отображение таблицы
        clientsTable.setRowHeight(25);
        clientsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        clientsTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Имя
        clientsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Фамилия
        clientsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Паспорт
        clientsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Телефон

        refreshButton = new JButton("Обновить");
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);

        closeButton = new JButton("Закрыть");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Заголовок
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Список клиентов отеля");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(70, 130, 180));

        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel countLabel = new JLabel("Всего клиентов: 0");
        countLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(countLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Таблица с прокруткой
        JScrollPane scrollPane = new JScrollPane(clientsTable);
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
        refreshButton.addActionListener(e -> loadClientsData());

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadClientsData() {
        try {
            // Показываем индикатор загрузки
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshButton.setEnabled(false);

            // Получаем данные с сервера
            List<Client> clients = dbManager.getAllClients();

            // Очищаем таблицу
            DefaultTableModel model = (DefaultTableModel) clientsTable.getModel();
            model.setRowCount(0);

            // Заполняем таблицу данными
            for (Client client : clients) {
                model.addRow(new Object[]{
                        client.getPassportNumber(),
                        client.getFirstName(),
                        client.getLastName(),
                        client.getPhoneNumber(),
                        client.getEmail(),
                        client.getCheckInDate(),
                        client.getCheckOutDate(),
                        client.getRoomNumber(),
                        client.getRoomType()
                });
            }

            // Обновляем счетчик
            updateClientCount(clients.size());

            if (clients.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Нет данных о клиентах.\n\n" +
                                "Возможные причины:\n" +
                                "• Сервер недоступен\n" +
                                "• Нет клиентов в базе данных\n" +
                                "• Ошибка соединения",
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ошибка загрузки данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Восстанавливаем курсор
            setCursor(Cursor.getDefaultCursor());
            refreshButton.setEnabled(true);
        }
    }

    private void updateClientCount(int count) {
        // Находим и обновляем label с количеством
        Component[] components = ((JPanel)getContentPane().getComponent(0)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] headerComps = ((JPanel)comp).getComponents();
                for (Component headerComp : headerComps) {
                    if (headerComp instanceof JLabel && headerComp != ((JPanel)comp).getComponent(0)) {
                        ((JLabel)headerComp).setText("Всего клиентов: " + count);
                        return;
                    }
                }
            }
        }
    }
}