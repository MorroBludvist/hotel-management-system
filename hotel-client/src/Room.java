public class Room {
    private int roomNumber;
    private String roomType;
    private String status;
    private String clientPassport;
    private String checkInDate;
    private String checkOutDate;

    public Room(int roomNumber, String roomType, String status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = status;
    }

    // Геттеры
    public int getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public String getStatus() { return status; }
    public String getClientPassport() { return clientPassport; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }

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
}