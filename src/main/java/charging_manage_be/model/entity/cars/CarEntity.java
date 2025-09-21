package charging_manage_be.model.entity.cars;

import charging_manage_be.model.entity.users.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "cars")
public class CarEntity {
    @Id
    private String license_plate; // Biển số xe

    @ManyToOne // Nhiều xe có thể thuộc về một người dùng
    @JoinColumn(name = "user_id", nullable = false) // Khóa ngoại tham chiếu đến bảng users
    // Và thêm cột user_id vào bảng cars
    private UserEntity user;

    @Column(name = "type_car", nullable = false, length = 50)
    private String typeCar; // Loại xe
    @Column(name = "chassis_number", nullable = false, length = 50)
    private String chassisNumber; // Số khung xe


    public CarEntity() {
    }

    public CarEntity(String license_plate, UserEntity user, String typeCar, String chassisNumber) {
        this.license_plate = license_plate;
        this.user = user;
        this.typeCar = typeCar;
        this.chassisNumber = chassisNumber;
    }

    public String getLicense_plate() {
        return license_plate;
    }

    public void setLicense_plate(String license_plate) {
        this.license_plate = license_plate;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getTypeCar() {
        return typeCar;
    }

    public void setTypeCar(String typeCar) {
        this.typeCar = typeCar;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }
}
