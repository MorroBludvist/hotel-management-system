/**
 * Модель клиента для клиентской части приложения.
 */
public class Client {
    private String passportNumber;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String checkInDate;
    private String checkOutDate;
    private int roomNumber;
    private String roomType;

    public Client(String firstName, String lastName, String passportNumber,
                  String phoneNumber, String email, String checkInDate,
                  String checkOutDate, int roomNumber, String roomType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
    }

    // Геттеры
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPassportNumber() { return passportNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public int getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
}