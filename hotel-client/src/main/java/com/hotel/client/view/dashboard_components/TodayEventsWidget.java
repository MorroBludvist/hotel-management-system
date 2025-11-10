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
import java.util.ArrayList;
import java.util.List;

/**
 * Виджет событий на сегодня
 */
public class TodayEventsWidget extends BaseWidget {
    private static final Logger logger = LogManager.getLogger(TodayEventsWidget.class);

    private final ClientService clientService;
    private final RoomService roomService;
    private JTextArea eventsArea;

    public TodayEventsWidget(HotelAdminDashboard dashboard, ClientService clientService, RoomService roomService) {
        super(dashboard, "События на сегодня");
        this.clientService = clientService;
        this.roomService = roomService;
        initializeWidget();
        refreshData();
    }

    private void initializeWidget() {
        setLayout(new BorderLayout());

        eventsArea = new JTextArea();
        eventsArea.setEditable(false);
        eventsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventsArea.setBackground(Color.WHITE);
        eventsArea.setLineWrap(true);
        eventsArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(eventsArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        try {
            List<String> todayEvents = getTodayEvents();
            StringBuilder eventsText = new StringBuilder();

            String today = dashboard.getDateFormat().format(dashboard.getCurrentDate());
            eventsText.append("Дата: ").append(today).append("\n\n");

            if (todayEvents.isEmpty()) {
                eventsText.append("📭 Событий на сегодня нет\n\n");
            } else {
                for (String event : todayEvents) {
                    eventsText.append("• ").append(event).append("\n");
                }
            }

            eventsText.append("\nОбновите дату для проверки новых событий");

            eventsArea.setText(eventsText.toString());
            logger.debug("Виджет событий обновлен, событий: {}", todayEvents.size());

        } catch (Exception e) {
            logger.error("Ошибка обновления виджета событий: {}", e.getMessage());
            eventsArea.setText("❌ Ошибка загрузки событий: " + e.getMessage());
        }
    }

    /**
     * Получает актуальные события на текущую дату
     */
    private List<String> getTodayEvents() {
        List<String> events = new ArrayList<>();
        String today = dashboard.getDateFormat().format(dashboard.getCurrentDate());

        try {
            List<Client> clients = clientService.getAllClients();
            List<Room> rooms = roomService.getAllRooms();

            // События заезда
            for (Client client : clients) {
                if (today.equals(client.getCheckInDate())) {
                    events.add("🏨 Заезд: " + client.getFirstName() + " " + client.getLastName() +
                            " (номер " + client.getRoomNumber() + ")");
                }
            }

            // События выезда
            for (Client client : clients) {
                if (today.equals(client.getCheckOutDate())) {
                    events.add("🚪 Выезд: " + client.getFirstName() + " " + client.getLastName() +
                            " (номер " + client.getRoomNumber() + ")");
                }
            }

            // Автоматические выселения
            for (Room room : rooms) {
                if ("occupied".equals(room.getStatus()) && today.equals(room.getCheckOutDate())) {
                    events.add("🔄 Автовыезд: номер " + room.getRoomNumber() +
                            " (клиент: " + room.getClientPassport() + ")");
                }
            }

        } catch (Exception e) {
            logger.error("Ошибка получения событий на сегодня: {}", e.getMessage());
            events.add("❌ Ошибка загрузки событий: " + e.getMessage());
        }

        return events;
    }
}