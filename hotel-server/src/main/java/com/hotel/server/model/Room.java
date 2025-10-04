package com.hotel.server.model;

public class Room {
    private Integer roomNumber;
    private String roomType;
    private String status;
    private String clientPassport;
    private String checkInDate;
    private String checkOutDate;

    public Room() {}

    public Room(Integer roomNumber, String roomType, String status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = status;
    }

    // Геттеры и сеттеры
    public Integer getRoomNumber() { return roomNumber; }
    public void setRoomNumber(Integer roomNumber) { this.roomNumber = roomNumber; }

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
}