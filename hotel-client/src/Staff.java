/**
 * Модель сотрудника для клиентской части приложения.
 */
public class Staff {
    private String passportNumber;
    private String firstName;
    private String lastName;
    private String position;
    private String phoneNumber;
    private String email;
    private String hireDate;
    private double salary;
    private String department;

    public Staff(String firstName, String lastName, String passportNumber, String position,
                 String phoneNumber, String email, String hireDate,
                 double salary, String department) {
        this.passportNumber = passportNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.hireDate = hireDate;
        this.salary = salary;
        this.department = department;
    }

    // Геттеры
    public String getPassportNumber() { return passportNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPosition() { return position; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getHireDate() { return hireDate; }
    public double getSalary() { return salary; }
    public String getDepartment() { return department; }
}