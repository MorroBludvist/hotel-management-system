package com.hotel.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
    @JsonProperty("roomNumber")
    private int roomNumber;

    @JsonProperty("roomType")
    private String roomType;

    @JsonProperty("status")
    private String status;

    @JsonProperty("clientPassport")
    private String clientPassport;

    @JsonProperty("checkInDate")
    private String checkInDate;

    @JsonProperty("checkOutDate")
    private String checkOutDate;

    // Пустой конструктор для Jackson
    public Room() {}

    // Основной конструктор
    public Room(int roomNumber, String roomType, String status, String clientPassport,
                String checkInDate, String checkOutDate) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = status != null ? status : "free";
        this.clientPassport = clientPassport;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // Конструктор для создания комнаты без клиента
    public Room(int roomNumber, String roomType, String status) {
        this(roomNumber, roomType, status, null, null, null);
    }

    // Геттеры и сеттеры
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClientPassport() { return clientPassport; }
    public void setClientPassport(String clientPassport) { this.clientPassport = clientPassport; }

    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }

    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }

    // Вспомогательные методы
    public String getStatusDisplay() {
        return "free".equals(status) ? "Свободен" : "Занят";
    }

    public String getOccupancyInfo() {
        if ("free".equals(status)) {
            return "Свободен";
        } else {
            return "Занят с " + checkInDate + " по " + checkOutDate;
        }
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomType='" + roomType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}