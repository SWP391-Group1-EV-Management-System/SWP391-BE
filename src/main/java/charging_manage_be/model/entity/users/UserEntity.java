package charging_manage_be.model.entity.users;

import charging_manage_be.model.entity.cars.CarEntity;
import charging_manage_be.model.entity.charging_station.ChargingStationEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.model.entity.reputations.User_ReputationEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Làm cho userID tự động random 4 chữ cái và 4 chữ số
    private String userID;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    @Column(name = "birth_date", nullable = false, length = 10)
    private Date birthDate;
    @Column(name = "gender", nullable = false)
    private boolean gender;
    @Column(name = "role", nullable = false, length = 20)
    private String role;
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp // Dánh dấu để Hibernate tự động gán giá trị thời gian hiện tại khi bản ghi được tạo
    private LocalDateTime createdAt; // LocalDateTime thay vì Date là để lấy giờ hiện tại dễ dàng hơn

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // @OneToMany(mappedBy = "user, cascade = CascadeType.ALL, fetch = FetchType.LAZY")
    // cascade = CascadeType.ALL, fetch = FetchType.LAZY là để quản lý quan hệ một-nhiều giữa UserEntity và CarEntity

    @OneToMany(mappedBy = "user" ) // mappedBy = "userID" là tên thuộc tính trong CarEntity tham chiếu đến UserEntity
    //Trong dòng "private UserEntity userID" ở CarEntity thì userID trong mapped ở đây chính là userID trong CarEntity
    // Và userID đó là khóa ngoại tham chiếu đến UserEntity từ CarEntity
    private List<CarEntity> cars;

    @OneToMany(mappedBy = "user")
    private List<User_ReputationEntity> userReputations;
    @OneToOne(mappedBy = "UserManager")
    private ChargingStationEntity chargingStation;
    @OneToMany(mappedBy = "user")
    private List<PaymentEntity> payments;



    public UserEntity() {
    }

    public UserEntity(String firstName, String lastName, Date birthDate, boolean gender, String role, String email, String password, String phoneNumber, LocalDateTime createdAt, String status, List<CarEntity> cars, List<User_ReputationEntity> userReputations) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.status = status;
        this.cars = cars;
        this.userReputations = userReputations;
    }

    public ChargingStationEntity getChargingStation() {
        return chargingStation;
    }

    public void setChargingStation(ChargingStationEntity chargingStation) {
        this.chargingStation = chargingStation;
    }
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CarEntity> getCars() {
        return cars;
    }

    public void setCars(List<CarEntity> cars) {
        this.cars = cars;
    }

    public List<User_ReputationEntity> getUserReputations() {
        return userReputations;
    }

    public void setUserReputations(List<User_ReputationEntity> userReputations) {
        this.userReputations = userReputations;
    }
}
