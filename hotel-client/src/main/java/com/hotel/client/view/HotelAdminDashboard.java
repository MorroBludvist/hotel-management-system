package com.hotel.client.view;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hotel.client.config.AppStateManager;
import com.hotel.client.service.*;
import com.hotel.client.view.dashboard_components.*;
import com.hotel.client.view.dashboard_components.NavigationPanel;
import com.hotel.client.view.dashboard_components.DashboardActionHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главная панель администратора отеля
 * Координирует работу всех компонентов dashboard
 *
 * @author Morro Bludvist
 */
public class HotelAdminDashboard extends JFrame {
    private static final Logger logger = LogManager.getLogger(HotelAdminDashboard.class);

    // Сервисы
    private ClientService clientService;
    private RoomService roomService;
    private StaffService staffService;
    private AppStateManager appStateManager;

    // Компоненты
    private HeaderPanel headerPanel;
    private NavigationPanel navigationPanel;
    private DashboardWidgetsManager widgetsManager;
    private DashboardActionHandler actionHandler;

    // Состояние
    private Date currentDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Конструктор главной панели администратора
     * Инициализирует все компоненты и сервисы
     */
    public HotelAdminDashboard() {
        // Сначала инициализируем дату
        this.currentDate = new Date(); // Значение по умолчанию

        initializeServices();
        loadCurrentDateFromState(); // Загружаем дату ДО создания компонентов
        initializeComponents();
        setupLayout();
        loadInitialData();

        logger.info("Панель администратора инициализирована");
    }

    /**
     * Инициализирует сервисы и обработчики
     */
    private void initializeServices() {
        ApiService apiService = ApiService.getInstance();
        this.appStateManager = AppStateManager.getInstance();
        this.clientService = new ClientService(apiService);
        this.roomService = new RoomService(apiService);
        this.staffService = new StaffService(apiService);
        this.actionHandler = new DashboardActionHandler(this, clientService, roomService, staffService);
    }

    /**
     * Инициализирует UI компоненты
     */
    private void initializeComponents() {
        setTitle("Панель администратора отеля");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.headerPanel = new HeaderPanel(this, actionHandler);
        this.navigationPanel = new NavigationPanel(this, actionHandler);
        this.widgetsManager = new DashboardWidgetsManager(this, clientService, roomService, staffService);
    }

    /**
     * Настраивает layout приложения
     */
    private void setupLayout() {
        add(headerPanel, BorderLayout.NORTH);
        add(navigationPanel, BorderLayout.WEST);
        add(widgetsManager.getWidgetsPanel(), BorderLayout.CENTER);
    }

    /**
     * Загружает данные при запуске
     */
    private void loadInitialData() {
        try {
            checkServerConnection();
            widgetsManager.refreshAllWidgets();
        } catch (Exception e) {
            logger.error("Ошибка загрузки данных: {}", e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Ошибка инициализации: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Загружает текущую дату из состояния приложения
     */
    public void loadCurrentDateFromState() {
        try {
            String savedDate = appStateManager.getCurrentDate();
            if (savedDate != null && !savedDate.isEmpty()) {
                currentDate = dateFormat.parse(savedDate);
                logger.info("📅 Загружена дата из состояния: {}", savedDate);
            } else {
                logger.warn("Сохраненная дата отсутствует, используется текущая дата");
                currentDate = new Date();
                saveCurrentDateToState(); // Сохраняем текущую дату как начальную
            }
        } catch (Exception e) {
            logger.error("❌ Ошибка загрузки даты из состояния: {}", e.getMessage());
            currentDate = new Date(); // Используем текущую дату как запасной вариант
            saveCurrentDateToState(); // Сохраняем текущую дату
        }
    }

    /**
     * Проверяет соединение с сервером
     */
    private void checkServerConnection() {
        boolean serverAvailable = ApiService.getInstance().isServerAvailable();
        if (!serverAvailable) {
            JOptionPane.showMessageDialog(this,
                    "Сервер недоступен!\n\nУбедитесь что:\n" +
                            "1. Сервер запущен на localhost:8080\n" +
                            "2. Приложение имеет доступ к сети\n\n" +
                            "Приложение будет работать в ограниченном режиме.",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
        }
    }

    // === PUBLIC API FOR COMPONENTS ===

    /**
     * Возвращает текущую дату приложения
     * @return текущая дата
     */
    public Date getCurrentDate() {
        return currentDate;
    }

    /**
     * Возвращает форматтер даты
     * @return SimpleDateFormat
     */
    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Обновляет текущую дату
     * @param newDate новая дата
     */
    public void setCurrentDate(Date newDate) {
        this.currentDate = newDate;
        saveCurrentDateToState();
    }

    /**
     * Сохраняет текущую дату в состояние приложения
     */
    public void saveCurrentDateToState() {
        String dateStr = dateFormat.format(currentDate);
        appStateManager.setCurrentDate(dateStr);
    }

    /**
     * Полностью обновляет все виджеты dashboard
     */
    public void refreshAllWidgets() {
        if (widgetsManager != null) {
            widgetsManager.refreshAllWidgets();
        }
        if (headerPanel != null) {
            headerPanel.refreshDateDisplay();
        }
    }

    /**
     * Обновляет конкретный виджет
     * @param widgetName имя виджета для обновления
     */
    public void refreshWidget(String widgetName) {
        if (widgetsManager != null) {
            widgetsManager.refreshWidget(widgetName);
        }
    }

    /**
     * Возвращает обработчик действий
     * @return DashboardActionHandler
     */
    public DashboardActionHandler getActionHandler() {
        return actionHandler;
    }
}